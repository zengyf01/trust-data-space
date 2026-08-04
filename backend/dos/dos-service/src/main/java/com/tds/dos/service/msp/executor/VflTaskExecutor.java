package com.tds.dos.service.msp.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbNode;
import com.tds.dos.dal.msp.entity.TbTask;
import com.tds.dos.dal.msp.mapper.TbTaskMapper;
import com.tds.dos.service.msp.node.INodeService;
import com.tds.dos.service.privacycompute.code.CodeGeneratorFactory;
import com.tds.dos.service.privacycompute.code.ICodeGenerator;
import com.tds.dos.service.privacycompute.code.VflCodeGenerator;
import com.tds.dos.service.ray.IAgentClient;
import com.tds.dos.service.ray.IRayClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 纵向联邦学习任务执行器
 * <p>
 * SecretFlow PRODUCTION 模式（ray_mode=False）执行纵向联邦学习。
 * 各方拥有相同的样本ID（对齐后）、不同的特征，标签在某一参与方。
 */
@Slf4j
@Component
public class VflTaskExecutor extends AbstractTaskExecutor {

    @Autowired
    private IAgentClient agentClient;

    @Autowired
    private CodeGeneratorFactory codeGeneratorFactory;

    @Autowired
    private TbTaskMapper taskMapper;

    @Autowired
    private INodeService nodeService;

    @Autowired
    private IRayClusterService rayClusterService;

    @Value("${privacy-compute.vfl.timeout-ms:600000}")
    private long vflTimeoutMs;

    @Value("${privacy-compute.vfl.poll-interval-ms:5000}")
    private long pollIntervalMs;

    /** 跨域通信端口基数 */
    private static final int CROSS_SILO_PORT_BASE = 30000;
    /** SPU通信端口基数，必须与跨域端口错开 */
    private static final int SPU_PORT_BASE = 32000;
    private static final int PORT_SLOT_COUNT = 1000;

    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_TIMEOUT = "TIMEOUT";

    /** 外部 cancel 触发的取消标志 */
    private static final ConcurrentMap<String, Boolean> CANCEL_FLAGS = new ConcurrentHashMap<>();

    /** 同一对节点同时只允许一个 VFL */
    private static final Set<String> RUNNING_NODE_PAIRS = ConcurrentHashMap.newKeySet();

    private static final String VFL_RUNNING_STATE_PREFIX = "__VFL_RUNNING__=";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected String doExecute(String taskId, TbTask task, Map<String, String> params) throws Exception {
        log.info("===========================================");
        log.info("== VFL任务执行开始 taskId={}", taskId);
        log.info("===========================================");

        // 解析参与节点
        String partyANodeId = params.get("partyANodeId");
        String partyBNodeId = params.get("partyBNodeId");

        if (partyANodeId == null || partyBNodeId == null) {
            throw new RuntimeException("VFL任务参数缺失: partyANodeId 或 partyBNodeId");
        }

        TbNode nodeA = requireNode(partyANodeId);
        TbNode nodeB = requireNode(partyBNodeId);

        String clusterId = null;
        PartyJob aliceJob = null;
        PartyJob bobJob = null;

        String pairKey = nodePairKey(partyANodeId, partyBNodeId);
        if (!RUNNING_NODE_PAIRS.add(pairKey)) {
            throw new RuntimeException("节点对 " + pairKey + " 上已有VFL任务在执行，请等待其完成后重试");
        }

        try {
            // 创建 Ray 集群
            log.info("【阶段1】创建Ray集群, partyA={}, partyB={}", partyANodeId, partyBNodeId);
            clusterId = rayClusterService.createCluster("vfl-" + taskId, List.of(partyANodeId, partyBNodeId));
            String rayHeadUrl = rayClusterService.getHeadAddress(clusterId);
            log.info("【阶段1】Ray集群已创建, clusterId={}, headUrl={}", clusterId, rayHeadUrl);

            // 获取节点 IP
            String nodeAIp = resolveNodeIp(nodeA);
            String nodeBIp = resolveNodeIp(nodeB);
            int crossSiloPort = derivePort(taskId, CROSS_SILO_PORT_BASE);
            int spuPort = derivePort(taskId, SPU_PORT_BASE);
            log.info("【阶段2】节点地址: partyA={}, partyB={}, 跨域端口={}, SPU端口={}",
                nodeAIp, nodeBIp, crossSiloPort, spuPort);

            // 生成代码
            ICodeGenerator codeGenerator = codeGeneratorFactory.getGenerator("VFL");
            Map<String, Object> baseParams = buildCodeParams(params, nodeAIp, nodeBIp, crossSiloPort, spuPort);

            String aliceCode = generateFor(codeGenerator, taskId, baseParams, VflCodeGenerator.PARTY_ALICE);
            String bobCode = generateFor(codeGenerator, taskId, baseParams, VflCodeGenerator.PARTY_BOB);
            saveAuditCode(taskId, aliceCode);
            log.info("【阶段3】双方脚本已生成, alice={} 字符, bob={} 字符", aliceCode.length(), bobCode.length());

            // 提交作业
            bobJob = submitTo(nodeB, VflCodeGenerator.PARTY_BOB, bobCode, taskId);
            try {
                aliceJob = submitTo(nodeA, VflCodeGenerator.PARTY_ALICE, aliceCode, taskId);
            } catch (Exception e) {
                log.error("【阶段4】alice提交失败，停止已提交的bob作业");
                stopQuietly(bobJob);
                throw e;
            }
            log.info("【阶段4】双方作业已提交, aliceJobId={}, bobJobId={}",
                aliceJob != null ? aliceJob.jobId : "null",
                bobJob != null ? bobJob.jobId : "null");

            // 持久化运行态
            persistRunningState(taskId, aliceJob, bobJob, clusterId);

            // 轮询等待
            waitForBothParties(taskId, aliceJob, bobJob);

            // 构建结果
            return buildResult(taskId, rayHeadUrl, clusterId, aliceJob, bobJob);

        } finally {
            stopQuietly(aliceJob);
            stopQuietly(bobJob);
            releaseClusterQuietly(clusterId);
            RUNNING_NODE_PAIRS.remove(pairKey);
            CANCEL_FLAGS.remove(taskId);
            log.info("== VFL任务执行结束 taskId={}", taskId);
        }
    }

    private Map<String, Object> buildCodeParams(Map<String, String> params, String nodeAIp, String nodeBIp,
                                                int crossSiloPort, int spuPort) {
        Map<String, Object> codeParams = new LinkedHashMap<>();
        codeParams.put(VflCodeGenerator.PARAM_PARTY_A_DATA_PATH, requireParam(params, "partyADataPath"));
        codeParams.put(VflCodeGenerator.PARAM_PARTY_B_DATA_PATH, requireParam(params, "partyBDataPath"));
        codeParams.put(VflCodeGenerator.PARAM_ID_COLUMN, requireParam(params, "idColumn"));
        codeParams.put(VflCodeGenerator.PARAM_LABEL_COLUMN, requireParam(params, "labelColumn"));
        codeParams.put(VflCodeGenerator.PARAM_LABEL_OWNER, requireParam(params, "labelOwner"));
        codeParams.put(VflCodeGenerator.PARAM_PARTY_A_FEATURE_COLUMNS, requireParam(params, "partyAFeatureColumns"));
        codeParams.put(VflCodeGenerator.PARAM_PARTY_B_FEATURE_COLUMNS, requireParam(params, "partyBFeatureColumns"));
        codeParams.put(VflCodeGenerator.PARAM_PARTY_A_CROSS_SILO_ADDRESS, nodeAIp + ":" + crossSiloPort);
        codeParams.put(VflCodeGenerator.PARAM_PARTY_B_CROSS_SILO_ADDRESS, nodeBIp + ":" + crossSiloPort);
        codeParams.put(VflCodeGenerator.PARAM_PARTY_A_SPU_ADDRESS, nodeAIp + ":" + spuPort);
        codeParams.put(VflCodeGenerator.PARAM_PARTY_B_SPU_ADDRESS, nodeBIp + ":" + spuPort);
        return codeParams;
    }

    private String requireParam(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("VFL任务参数缺失: " + key);
        }
        return value.trim();
    }

    private PartyJob submitTo(TbNode node, String party, String code, String taskId) {
        String jobId = agentClient.submitJob(node.getfEndpoint(), code, taskId + "-" + party);
        return new PartyJob(party, node.getfNodeId(), node.getfEndpoint(), jobId);
    }

    private void waitForBothParties(String taskId, PartyJob aliceJob, PartyJob bobJob) {
        long deadline = System.currentTimeMillis() + vflTimeoutMs;
        List<PartyJob> jobs = Arrays.asList(
            aliceJob != null ? aliceJob : new PartyJob("alice", null, null, null),
            bobJob != null ? bobJob : new PartyJob("bob", null, null, null)
        );
        int round = 0;

        while (System.currentTimeMillis() < deadline) {
            round++;

            if (Boolean.TRUE.equals(CANCEL_FLAGS.get(taskId))) {
                log.warn("[轮询] 检测到外部 cancel 标志，退出等待并停止双方作业");
                stopQuietly(aliceJob);
                stopQuietly(bobJob);
                return;
            }

            for (PartyJob job : jobs) {
                if (job == null || job.isTerminal()) {
                    continue;
                }
                try {
                    IAgentClient.TaskStatus status = agentClient.getTaskStatus(job.agentEndpoint, job.jobId);
                    if (status != null) {
                        job.update(status);
                    }
                } catch (Exception e) {
                    log.warn("[轮询] 查询 {} 作业状态异常: {}", job.party, e.getMessage());
                }
            }

            log.info("[轮询] #{} alice={}, bob={}",
                round,
                aliceJob != null ? aliceJob.status : "N/A",
                bobJob != null ? bobJob.status : "N/A");

            if (aliceJob != null && bobJob != null && aliceJob.isTerminal() && bobJob.isTerminal()) {
                return;
            }

            // 一方失败则停止另一方
            if (aliceJob != null && aliceJob.isFailed()) {
                log.error("[轮询] alice作业失败({})，停止bob作业", aliceJob.status);
                stopQuietly(bobJob);
                return;
            }
            if (bobJob != null && bobJob.isFailed()) {
                log.error("[轮询] bob作业失败({})，停止alice作业", bobJob.status);
                stopQuietly(aliceJob);
                return;
            }

            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // 超时
        for (PartyJob job : jobs) {
            if (job != null && !job.isTerminal()) {
                log.warn("[轮询] {} 作业等待超时({}ms)", job.party, vflTimeoutMs);
                job.markTimeout();
                stopQuietly(job);
            }
        }
    }

    private String buildResult(String taskId, String rayHeadUrl, String clusterId,
                               PartyJob aliceJob, PartyJob bobJob) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task_id", taskId);
        result.put("ray_head_url", rayHeadUrl);
        result.put("ray_cluster_id", clusterId);

        Map<String, Object> jobs = new LinkedHashMap<>();
        if (aliceJob != null) {
            jobs.put(VflCodeGenerator.PARTY_ALICE, aliceJob.toMap());
        }
        if (bobJob != null) {
            jobs.put(VflCodeGenerator.PARTY_BOB, bobJob.toMap());
        }
        result.put("jobs", jobs);

        boolean aliceOk = aliceJob != null && aliceJob.isSucceeded();
        boolean bobOk = bobJob != null && bobJob.isSucceeded();

        if (!aliceOk || !bobOk) {
            if (Boolean.TRUE.equals(CANCEL_FLAGS.get(taskId))) {
                result.put("status", "cancelled");
                result.put("message", "VFL任务已取消");
                return objectMapper.writeValueAsString(result);
            }
            result.put("status", "error");
            PartyJob failed = !aliceOk ? aliceJob : bobJob;
            result.put("message", "VFL任务执行失败: " + failed.party + " 状态=" + failed.status
                + (failed.error != null ? ", " + failed.error : ""));
            log.error("VFL任务失败, taskId={}, 失败方={}, 状态={}", taskId, failed.party, failed.status);
            return objectMapper.writeValueAsString(result);
        }

        // 解析结果摘要
        Map<String, Object> aliceSummary = parseSummary(aliceJob.result);
        Map<String, Object> bobSummary = parseSummary(bobJob.result);

        result.put("status", "ok");
        result.put("party_alice", aliceSummary != null ? aliceSummary : new LinkedHashMap<>());
        result.put("party_bob", bobSummary != null ? bobSummary : new LinkedHashMap<>());
        result.put("message", "VFL任务执行成功");

        log.info("VFL任务执行成功, taskId={}", taskId);
        return objectMapper.writeValueAsString(result);
    }

    private Map<String, Object> parseSummary(String stdout) {
        if (stdout == null || stdout.isEmpty()) {
            return null;
        }
        int index = stdout.lastIndexOf(VflCodeGenerator.RESULT_PREFIX);
        if (index < 0) {
            return null;
        }
        int start = index + VflCodeGenerator.RESULT_PREFIX.length();
        int end = stdout.indexOf('\n', start);
        String json = (end < 0 ? stdout.substring(start) : stdout.substring(start, end)).trim();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("解析VFL结果摘要失败: {}", e.getMessage());
            return null;
        }
    }

    private void stopQuietly(PartyJob job) {
        if (job == null || job.isTerminal() || job.jobId == null) {
            return;
        }
        try {
            agentClient.stopJob(job.agentEndpoint, job.jobId);
            job.markStopped();
        } catch (Exception e) {
            log.warn("停止 {} 作业失败: {}", job.party, e.getMessage());
        }
    }

    private void releaseClusterQuietly(String clusterId) {
        if (clusterId == null) {
            return;
        }
        try {
            rayClusterService.releaseCluster(clusterId);
            log.info("Ray集群已释放, clusterId={}", clusterId);
        } catch (Exception e) {
            log.warn("释放Ray集群失败, clusterId={}: {}", clusterId, e.getMessage());
        }
    }

    private String nodePairKey(String nodeAId, String nodeBId) {
        return nodeAId.compareTo(nodeBId) <= 0 ? nodeAId + "|" + nodeBId : nodeBId + "|" + nodeAId;
    }

    private TbNode requireNode(String nodeId) {
        TbNode node = nodeService.getNode(nodeId);
        if (node == null) {
            throw new RuntimeException("节点未找到: " + nodeId);
        }
        if (node.getfEndpoint() == null || node.getfEndpoint().isEmpty()) {
            throw new RuntimeException("节点未注册Agent地址: " + nodeId);
        }
        return node;
    }

    private String resolveNodeIp(TbNode node) {
        IAgentClient.RayStatus status = agentClient.getRayStatus(node.getfEndpoint());
        if (status == null) {
            throw new RuntimeException("无法访问节点Agent: " + node.getfNodeId());
        }
        if (!status.isRunning()) {
            throw new RuntimeException("节点Ray未运行: " + node.getfNodeId());
        }
        String nodeIp = status.getNodeIp();
        if (nodeIp == null || nodeIp.trim().isEmpty()) {
            throw new RuntimeException("节点未返回真实IP: " + node.getfNodeId());
        }
        return nodeIp.trim();
    }

    private int derivePort(String taskId, int base) {
        return base + Math.floorMod(taskId.hashCode(), PORT_SLOT_COUNT);
    }

    private String generateFor(ICodeGenerator generator, String taskId,
                               Map<String, Object> baseParams, String selfParty) {
        Map<String, Object> partyParams = new LinkedHashMap<>(baseParams);
        partyParams.put(VflCodeGenerator.PARAM_SELF_PARTY, selfParty);
        String code = generator.generateCode(taskId, partyParams);
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("生成的VFL脚本为空, selfParty=" + selfParty);
        }
        return code;
    }

    private void saveAuditCode(String taskId, String code) {
        TbTask task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setfCode(code);
            task.setfUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    private void persistRunningState(String taskId, PartyJob aliceJob, PartyJob bobJob, String clusterId) {
        try {
            Map<String, Object> state = new LinkedHashMap<>();
            if (aliceJob != null) {
                state.put("alice", partyToMap(aliceJob));
            }
            if (bobJob != null) {
                state.put("bob", partyToMap(bobJob));
            }
            state.put("clusterId", clusterId);
            String marker = VFL_RUNNING_STATE_PREFIX + objectMapper.writeValueAsString(state);
            TbTask task = taskMapper.selectById(taskId);
            if (task != null) {
                String existing = task.getfExecutionLog();
                String updated = existing == null || existing.isEmpty() ? marker : existing + "\n" + marker;
                task.setfExecutionLog(updated);
                task.setfUpdateTime(LocalDateTime.now());
                taskMapper.updateById(task);
            }
        } catch (Exception e) {
            log.warn("持久化 VFL 运行态失败（cancel 能力会受影响）: {}", e.getMessage());
        }
    }

    private Map<String, Object> partyToMap(PartyJob job) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("party", job.party);
        m.put("nodeId", job.nodeId);
        m.put("agentEndpoint", job.agentEndpoint);
        m.put("jobId", job.jobId);
        return m;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.VERTICAL_FL;
    }

    @Override
    public boolean cancel(String taskId) {
        CANCEL_FLAGS.put(taskId, Boolean.TRUE);

        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("cancel VFL 任务 {} 时未找到任务", taskId);
            return false;
        }

        VflRunningState state = parseRunningState(task.getfExecutionLog());
        if (state == null) {
            return true;
        }

        boolean anyStopped = false;
        for (VflRunningState.Party p : new VflRunningState.Party[]{state.alice, state.bob}) {
            if (p == null || p.jobId == null || p.agentEndpoint == null) {
                continue;
            }
            try {
                boolean ok = agentClient.stopJob(p.agentEndpoint, p.jobId);
                log.info("VFL cancel: 通知 agent 停止 {} (jobId={}), agent 响应={}", p.party, p.jobId, ok);
                if (ok) {
                    anyStopped = true;
                }
            } catch (Exception e) {
                log.warn("VFL cancel: stop {} (jobId={}) 失败: {}", p.party, p.jobId, e.getMessage());
            }
        }
        if (state.clusterId != null) {
            releaseClusterQuietly(state.clusterId);
        }
        return anyStopped;
    }

    private VflRunningState parseRunningState(String executionLog) {
        if (executionLog == null || executionLog.isEmpty()) {
            return null;
        }
        int idx = executionLog.lastIndexOf(VFL_RUNNING_STATE_PREFIX);
        if (idx < 0) {
            return null;
        }
        int start = idx + VFL_RUNNING_STATE_PREFIX.length();
        int end = executionLog.indexOf('\n', start);
        String json = end < 0 ? executionLog.substring(start) : executionLog.substring(start, end);
        try {
            Map<String, Object> m = objectMapper.readValue(json, Map.class);
            VflRunningState state = new VflRunningState();
            state.clusterId = (String) m.get("clusterId");
            state.alice = parseParty((Map<String, Object>) m.get("alice"));
            state.bob = parseParty((Map<String, Object>) m.get("bob"));
            return state;
        } catch (Exception e) {
            log.warn("解析 VFL 运行态失败: {}", e.getMessage());
            return null;
        }
    }

    private VflRunningState.Party parseParty(Map<String, Object> m) {
        if (m == null) {
            return null;
        }
        VflRunningState.Party p = new VflRunningState.Party();
        p.party = (String) m.get("party");
        p.nodeId = (String) m.get("nodeId");
        p.jobId = (String) m.get("jobId");
        p.agentEndpoint = (String) m.get("agentEndpoint");
        return p;
    }

    @Override
    public TaskStatus queryStatus(String taskId) {
        return null;
    }

    /**
     * 一方作业的执行上下文
     */
    private static class PartyJob {
        private final String party;
        private final String nodeId;
        private final String agentEndpoint;
        private final String jobId;
        private String status = "SUBMITTED";
        private String result;
        private String error;

        PartyJob(String party, String nodeId, String agentEndpoint, String jobId) {
            this.party = party;
            this.nodeId = nodeId;
            this.agentEndpoint = agentEndpoint;
            this.jobId = jobId;
        }

        void update(IAgentClient.TaskStatus taskStatus) {
            this.status = taskStatus.getStatus();
            this.result = taskStatus.getResult();
            this.error = taskStatus.getError();
        }

        boolean isSucceeded() {
            return STATUS_SUCCEEDED.equals(status);
        }

        boolean isFailed() {
            return STATUS_FAILED.equals(status) || "STOPPED".equals(status);
        }

        boolean isTerminal() {
            return isSucceeded() || isFailed() || STATUS_TIMEOUT.equals(status);
        }

        void markTimeout() {
            this.status = STATUS_TIMEOUT;
        }

        void markStopped() {
            this.status = "STOPPED";
        }

        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("node_id", nodeId);
            map.put("job_id", jobId);
            map.put("status", status);
            if (error != null && !error.isEmpty()) {
                map.put("error", error.length() > 1000 ? error.substring(error.length() - 1000) : error);
            }
            return map;
        }
    }

    /**
     * 任务运行态（由 doExecute 写入 f_execution_log，cancel 时读取）
     */
    private static class VflRunningState {
        Party alice;
        Party bob;
        String clusterId;

        static class Party {
            String party;
            String nodeId;
            String jobId;
            String agentEndpoint;
        }
    }
}