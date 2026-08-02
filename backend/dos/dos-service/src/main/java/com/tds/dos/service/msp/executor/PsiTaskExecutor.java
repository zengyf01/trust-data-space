package com.tds.dos.service.msp.executor;

import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbNode;
import com.tds.dos.dal.msp.entity.TbTask;
import com.tds.dos.dal.msp.mapper.TbTaskMapper;
import com.tds.dos.service.msp.node.INodeService;
import com.tds.dos.service.privacycompute.code.CodeGeneratorFactory;
import com.tds.dos.service.privacycompute.code.ICodeGenerator;
import com.tds.dos.service.privacycompute.code.PsiCodeGenerator;
import com.tds.dos.service.ray.IAgentClient;
import com.tds.dos.service.ray.IRayClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PSI求交任务执行器
 * <p>
 * SecretFlow PRODUCTION 模式（ray_mode=False）要求 alice 与 bob 两个进程分别在各自节点上运行，
 * 因此这里向两个节点各提交一份 self_party 不同的脚本，并等待双方都完成。
 * <p>
 * 仍然创建 Ray 集群，原因是节点 Agent 在提交与执行前都会检查 Ray 是否运行
 * （agent.py 的 is_ray_running 与 ray_head_address 检查），生成的脚本本身并不使用 Ray。
 */
@Component
public class PsiTaskExecutor extends AbstractTaskExecutor {

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

    /**
     * 整体超时。节点 Agent 用 subprocess timeout=600 强制杀进程，
     * 所以真实上限是 10 分钟，这里默认留 9 分钟余量给状态回传。
     */
    @Value("${privacy-compute.psi.timeout-ms:540000}")
    private long psiTimeoutMs;

    @Value("${privacy-compute.psi.poll-interval-ms:3000}")
    private long pollIntervalMs;

    /** 跨域通信端口基数 */
    private static final int CROSS_SILO_PORT_BASE = 30000;
    /** SPU通信端口基数，必须与跨域端口错开 */
    private static final int SPU_PORT_BASE = 32000;
    private static final int PORT_SLOT_COUNT = 1000;
    /** 连续查询失败超过该次数判定为基础设施故障，不再等到整体超时 */
    private static final int MAX_CONSECUTIVE_POLL_FAILURES = 5;

    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_STOPPED = "STOPPED";
    private static final String STATUS_TIMEOUT = "TIMEOUT";
    private static final String STATUS_UNREACHABLE = "UNREACHABLE";

    /** 任务执行期间写入 f_execution_log 的运行态标记前缀。cancel() 据此定位两方作业。 */
    private static final String PSI_RUNNING_STATE_PREFIX = "__PSI_RUNNING__=";

    /**
     * 同一对节点同时只允许一个 PSI：端口按 taskId 派生仍可能碰撞，
     * 且 releaseCluster 会停掉 worker，并发任务会互相打断。
     * 仅在单 DOS 实例内有效。
     */
    private static final Set<String> RUNNING_NODE_PAIRS = ConcurrentHashMap.newKeySet();

    /** 外部 cancel 触发的取消标志。doExecute 在 waitForBothParties 每次轮询都检查。 */
    private static final ConcurrentMap<String, Boolean> CANCEL_FLAGS = new ConcurrentHashMap<>();

    @Override
    protected String doExecute(String taskId, TbTask task, Map<String, String> params) throws Exception {
        String protocol = params.getOrDefault("protocol", "ECPSI");

        log.info("===========================================");
        log.info("== PSI任务执行开始 taskId={}, 协议={}", taskId, protocol);
        log.info("===========================================");

        List<String> nodeIds = resolveParticipantNodeIds(task, params);
        TbNode nodeA = requireNode(nodeIds.get(0));
        TbNode nodeB = requireNode(nodeIds.get(1));

        String pairKey = nodePairKey(nodeIds.get(0), nodeIds.get(1));
        if (!RUNNING_NODE_PAIRS.add(pairKey)) {
            throw new RuntimeException("节点对 " + pairKey + " 上已有PSI任务在执行，请等待其完成后重试");
        }

        String clusterId = null;
        PartyJob aliceJob = null;
        PartyJob bobJob = null;
        try {
            // 建集群只为放行 Agent 的 Ray 检查，生成的脚本不使用 Ray
            log.info("【阶段1】创建Ray集群, 参与节点: {}", nodeIds);
            clusterId = rayClusterService.createCluster("PSI-" + taskId, nodeIds);
            String rayHeadUrl = rayClusterService.getHeadAddress(clusterId);
            log.info("【阶段1】Ray集群已创建, clusterId={}, headUrl={}", clusterId, rayHeadUrl);

            // 实时拿容器IP：tb_node 里的 f_machine_ip 为空、f_ray_endpoint 会过期
            String aliceIp = resolveNodeIp(nodeA);
            String bobIp = resolveNodeIp(nodeB);
            int crossSiloPort = derivePort(taskId, CROSS_SILO_PORT_BASE);
            int spuPort = derivePort(taskId, SPU_PORT_BASE);
            log.info("【阶段2】节点实时地址: alice={}, bob={}, 跨域端口={}, SPU端口={}",
                aliceIp, bobIp, crossSiloPort, spuPort);

            ICodeGenerator codeGenerator = codeGeneratorFactory.getGenerator("PSI");
            Map<String, Object> baseParams = buildCodeParams(params, protocol,
                aliceIp, bobIp, crossSiloPort, spuPort);

            String aliceCode = generateFor(codeGenerator, taskId, baseParams, PsiCodeGenerator.PARTY_ALICE);
            String bobCode = generateFor(codeGenerator, taskId, baseParams, PsiCodeGenerator.PARTY_BOB);
            saveAuditCode(taskId, aliceCode);
            log.info("【阶段3】双方脚本已生成, alice={} 字符, bob={} 字符", aliceCode.length(), bobCode.length());

            // 连续提交，不 sleep：sf.init 的 enable_waiting_for_other_parties_ready 默认为 True，
            // 能覆盖正常的启动时间差。先提交 bob，让 receiver 侧启动时对端更可能已在初始化。
            bobJob = submitTo(nodeB, PsiCodeGenerator.PARTY_BOB, bobCode, taskId);
            try {
                aliceJob = submitTo(nodeA, PsiCodeGenerator.PARTY_ALICE, aliceCode, taskId);
            } catch (Exception e) {
                log.error("【阶段4】alice提交失败，停止已提交的bob作业");
                stopQuietly(bobJob);
                throw e;
            }
            log.info("【阶段4】双方作业已提交, aliceJobId={}, bobJobId={}", aliceJob.jobId, bobJob.jobId);

            // 持久化运行态（jobId + agent endpoint + clusterId）到 f_execution_log，
            // cancel() 与服务重启后能定位两方作业并停掉。
            persistRunningState(taskId, aliceJob, bobJob, clusterId);

            waitForBothParties(taskId, aliceJob, bobJob);

            return buildResult(taskId, protocol, rayHeadUrl, clusterId, aliceJob, bobJob);
        } finally {
            // 先停未终态作业，再释放集群
            stopQuietly(aliceJob);
            stopQuietly(bobJob);
            releaseClusterQuietly(clusterId);
            RUNNING_NODE_PAIRS.remove(pairKey);
            CANCEL_FLAGS.remove(taskId);
            log.info("== PSI任务执行结束 taskId={}", taskId);
        }
    }

    /**
     * 解析两方节点。不自动挑选在线节点顶替，否则角色与数据路径会错配。
     */
    private List<String> resolveParticipantNodeIds(TbTask task, Map<String, String> params) {
        String partyANodeId = params.get("partyANodeId");
        String partyBNodeId = params.get("partyBNodeId");

        List<String> nodeIds = new ArrayList<>();
        if (partyANodeId != null && !partyANodeId.isEmpty()
            && partyBNodeId != null && !partyBNodeId.isEmpty()) {
            nodeIds.add(partyANodeId);
            nodeIds.add(partyBNodeId);
        } else if (task.getfParticipants() != null && !task.getfParticipants().isEmpty()) {
            for (String participant : task.getfParticipants().split(",")) {
                String trimmed = participant.trim();
                if (!trimmed.isEmpty()) {
                    nodeIds.add(trimmed);
                }
            }
        }

        if (nodeIds.size() != 2) {
            throw new RuntimeException("PSI需要且只需要两个参与节点，当前解析到: " + nodeIds
                + "（请在任务参数中提供 partyANodeId 与 partyBNodeId）");
        }
        if (nodeIds.get(0).equals(nodeIds.get(1))) {
            throw new RuntimeException("PSI两方不能是同一个节点: " + nodeIds.get(0));
        }
        return nodeIds;
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

    /**
     * 通过 Agent 实时获取节点容器IP。
     * 不从 rayAddress 兜底解析——那正是过期地址的来源。
     */
    private String resolveNodeIp(TbNode node) {
        IAgentClient.RayStatus status = agentClient.getRayStatus(node.getfEndpoint());
        if (status == null) {
            throw new RuntimeException("无法访问节点Agent: " + node.getfNodeId() + " (" + node.getfEndpoint() + ")");
        }
        if (!status.isRunning()) {
            throw new RuntimeException("节点Ray未运行: " + node.getfNodeId());
        }
        String nodeIp = status.getNodeIp();
        if (nodeIp == null || nodeIp.trim().isEmpty()) {
            throw new RuntimeException("节点未返回真实IP，无法配置SecretFlow通信地址: " + node.getfNodeId());
        }
        return nodeIp.trim();
    }

    /**
     * 按 taskId 确定性派生端口，保证同一任务重试拿到相同端口
     */
    private int derivePort(String taskId, int base) {
        return base + Math.floorMod(taskId.hashCode(), PORT_SLOT_COUNT);
    }

    private Map<String, Object> buildCodeParams(Map<String, String> params, String protocol,
                                                String aliceIp, String bobIp,
                                                int crossSiloPort, int spuPort) {
        String partyADataPath = requireParam(params, "partyADataPath");
        String partyBDataPath = requireParam(params, "partyBDataPath");
        String keyColumn = requireParam(params, "keyColumn");

        Map<String, Object> codeParams = new LinkedHashMap<>();
        codeParams.put(PsiCodeGenerator.PARAM_PARTY_A_DATA_PATH, partyADataPath);
        codeParams.put(PsiCodeGenerator.PARAM_PARTY_B_DATA_PATH, partyBDataPath);
        codeParams.put(PsiCodeGenerator.PARAM_KEY_COLUMN, keyColumn);
        codeParams.put(PsiCodeGenerator.PARAM_PROTOCOL, protocol);
        codeParams.put(PsiCodeGenerator.PARAM_RESULT_TYPE, params.getOrDefault("resultType", "INTERSECTION"));
        codeParams.put(PsiCodeGenerator.PARAM_RECEIVER, PsiCodeGenerator.PARTY_ALICE);
        codeParams.put(PsiCodeGenerator.PARAM_PARTY_A_CROSS_SILO_ADDRESS, aliceIp + ":" + crossSiloPort);
        codeParams.put(PsiCodeGenerator.PARAM_PARTY_B_CROSS_SILO_ADDRESS, bobIp + ":" + crossSiloPort);
        codeParams.put(PsiCodeGenerator.PARAM_PARTY_A_SPU_ADDRESS, aliceIp + ":" + spuPort);
        codeParams.put(PsiCodeGenerator.PARAM_PARTY_B_SPU_ADDRESS, bobIp + ":" + spuPort);
        return codeParams;
    }

    private String requireParam(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("PSI任务参数缺失: " + key);
        }
        return value.trim();
    }

    private String generateFor(ICodeGenerator generator, String taskId,
                               Map<String, Object> baseParams, String selfParty) {
        Map<String, Object> partyParams = new LinkedHashMap<>(baseParams);
        partyParams.put(PsiCodeGenerator.PARAM_SELF_PARTY, selfParty);
        String code = generator.generateCode(taskId, partyParams);
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("生成的PSI脚本为空, selfParty=" + selfParty);
        }
        return code;
    }

    /**
     * 保存 alice 侧脚本仅供审计。执行时始终按当前参数重新生成：
     * 容器IP是动态的、端口是任务级的、self_party是节点级的，旧脚本必然带过期地址。
     */
    private void saveAuditCode(String taskId, String code) {
        TbTask task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setfCode(code);
            task.setfUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    private PartyJob submitTo(TbNode node, String party, String code, String taskId) {
        String jobId = agentClient.submitJob(node.getfEndpoint(), code, taskId + "-" + party);
        return new PartyJob(party, node.getfNodeId(), node.getfEndpoint(), jobId);
    }

    /**
     * 双方轮询。成功条件严格为双方都 SUCCEEDED；任一方失败立即停止另一方，不再空等。
     */
    private void waitForBothParties(String taskId, PartyJob aliceJob, PartyJob bobJob) {
        long deadline = System.currentTimeMillis() + psiTimeoutMs;
        List<PartyJob> jobs = Arrays.asList(aliceJob, bobJob);
        int round = 0;

        while (System.currentTimeMillis() < deadline) {
            round++;
            for (PartyJob job : jobs) {
                if (job.isTerminal()) {
                    continue;
                }
                try {
                    IAgentClient.TaskStatus status = agentClient.getTaskStatus(job.agentEndpoint, job.jobId);
                    if (status == null) {
                        job.recordPollFailure();
                    } else {
                        job.update(status);
                    }
                } catch (Exception e) {
                    log.warn("[轮询] 查询 {} 作业状态异常: {}", job.party, e.getMessage());
                    job.recordPollFailure();
                }

                if (job.consecutivePollFailures >= MAX_CONSECUTIVE_POLL_FAILURES) {
                    log.error("[轮询] {} 节点连续 {} 次查询失败，判定为基础设施故障",
                        job.party, job.consecutivePollFailures);
                    job.markUnreachable();
                }
            }

            log.info("[轮询] #{} alice={}, bob={}", round, aliceJob.status, bobJob.status);

            if (Boolean.TRUE.equals(CANCEL_FLAGS.get(taskId))) {
                log.warn("[轮询] 检测到外部 cancel 标志，退出等待并停止双方作业");
                stopQuietly(aliceJob);
                stopQuietly(bobJob);
                return;
            }

            if (aliceJob.isTerminal() && bobJob.isTerminal()) {
                return;
            }
            // 一方已失败，另一方继续跑没有意义
            PartyJob failed = aliceJob.isFailed() ? aliceJob : (bobJob.isFailed() ? bobJob : null);
            if (failed != null) {
                PartyJob counterpart = failed == aliceJob ? bobJob : aliceJob;
                log.error("[轮询] {} 作业失败({})，立即停止 {} 作业", failed.party, failed.status, counterpart.party);
                stopQuietly(counterpart);
                return;
            }

            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        for (PartyJob job : jobs) {
            if (!job.isTerminal()) {
                log.warn("[轮询] {} 作业等待超时({}ms)", job.party, psiTimeoutMs);
                job.markTimeout();
                stopQuietly(job);
            }
        }
    }

    private String buildResult(String taskId, String protocol, String rayHeadUrl, String clusterId,
                               PartyJob aliceJob, PartyJob bobJob) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task_id", taskId);
        result.put("protocol", protocol);
        result.put("ray_head_url", rayHeadUrl);
        result.put("ray_cluster_id", clusterId);

        Map<String, Object> jobs = new LinkedHashMap<>();
        jobs.put(PsiCodeGenerator.PARTY_ALICE, aliceJob.toMap());
        jobs.put(PsiCodeGenerator.PARTY_BOB, bobJob.toMap());
        result.put("jobs", jobs);

        if (!aliceJob.isSucceeded() || !bobJob.isSucceeded()) {
            // 区分"被取消"和"真的失败"：被取消的任务不要标 error/失败，否则用户在 HTTP 响应（看到 CANCELLED）
            // 与 f_result（看到 error/失败）之间产生矛盾。
            if (Boolean.TRUE.equals(CANCEL_FLAGS.get(taskId))) {
                PartyJob stopped = aliceJob.isSucceeded() ? bobJob : aliceJob;
                result.put("status", "cancelled");
                result.put("failed_party", stopped.party);
                result.put("message", "PSI任务已取消（" + stopped.party + " 状态=" + stopped.status + "）");
                log.warn("PSI任务已取消, taskId={}, 停止方={}, 状态={}", taskId, stopped.party, stopped.status);
                return objectMapper.writeValueAsString(result);
            }
            PartyJob failed = aliceJob.isSucceeded() ? bobJob : aliceJob;
            result.put("status", "error");
            result.put("failed_party", failed.party);
            result.put("message", "PSI任务执行失败: " + failed.party + " 状态=" + failed.status
                + (failed.error != null ? ", " + tail(failed.error, 1000) : ""));
            log.error("PSI任务失败, taskId={}, 失败方={}, 状态={}", taskId, failed.party, failed.status);
            return objectMapper.writeValueAsString(result);
        }

        // Agent 返回 SUCCEEDED 只代表 returncode=0，摘要缺失一律判失败
        Map<String, Object> aliceSummary = parseSummary(aliceJob.result);
        Map<String, Object> bobSummary = parseSummary(bobJob.result);
        if (aliceSummary == null || bobSummary == null) {
            result.put("status", "error");
            result.put("message", "PSI任务执行失败: 双方进程退出码为0但未解析到结果摘要("
                + PsiCodeGenerator.RESULT_PREFIX + ")");
            log.error("PSI任务缺少结果摘要, taskId={}, alice摘要={}, bob摘要={}",
                taskId, aliceSummary != null, bobSummary != null);
            return objectMapper.writeValueAsString(result);
        }

        Integer aliceCount = intersectionCount(aliceSummary);
        Integer bobCount = intersectionCount(bobSummary);
        if (aliceCount == null || !Objects.equals(aliceCount, bobCount)) {
            result.put("status", "error");
            result.put("message", "PSI任务执行失败: 双方交集数量不一致, alice=" + aliceCount + ", bob=" + bobCount);
            log.error("PSI双方交集数量不一致, taskId={}, alice={}, bob={}", taskId, aliceCount, bobCount);
            return objectMapper.writeValueAsString(result);
        }

        result.put("status", "ok");
        result.put("intersection_count", aliceCount);
        result.put("party_statistics", partyStatistics(aliceSummary));
        result.put("output_path", partySummaryField(aliceSummary, bobSummary));
        result.put("message", "PSI任务执行成功");
        log.info("PSI任务执行成功, taskId={}, 交集数量={}", taskId, aliceCount);
        return objectMapper.writeValueAsString(result);
    }

    /**
     * 从 stdout 末尾往前找最后一条摘要行：SPU 的 C++ 日志会在摘要之后继续输出
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseSummary(String stdout) {
        if (stdout == null || stdout.isEmpty()) {
            return null;
        }
        int index = stdout.lastIndexOf(PsiCodeGenerator.RESULT_PREFIX);
        if (index < 0) {
            return null;
        }
        int start = index + PsiCodeGenerator.RESULT_PREFIX.length();
        int end = stdout.indexOf('\n', start);
        String json = (end < 0 ? stdout.substring(start) : stdout.substring(start, end)).trim();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("解析PSI结果摘要失败: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Integer intersectionCount(Map<String, Object> summary) {
        Object statistics = summary.get("statistics");
        if (!(statistics instanceof List)) {
            return null;
        }
        for (Object item : (List<Object>) statistics) {
            if (item instanceof Map) {
                Object count = ((Map<String, Object>) item).get("intersection_count");
                if (count instanceof Number) {
                    return ((Number) count).intValue();
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> partyStatistics(Map<String, Object> summary) {
        Map<String, Object> statistics = new LinkedHashMap<>();
        Object raw = summary.get("statistics");
        if (raw instanceof List) {
            for (Object item : (List<Object>) raw) {
                if (item instanceof Map) {
                    Map<String, Object> entry = (Map<String, Object>) item;
                    Object party = entry.get("party");
                    if (party != null) {
                        statistics.put(String.valueOf(party), entry);
                    }
                }
            }
        }
        return statistics;
    }

    private Map<String, Object> partySummaryField(Map<String, Object> aliceSummary, Map<String, Object> bobSummary) {
        // PSI 结果只发送给 receiver（默认是 alice），只返回接收方的路径
        Map<String, Object> paths = new LinkedHashMap<>();
        paths.put(PsiCodeGenerator.PARTY_ALICE, aliceSummary.get("outputPath"));
        return paths;
    }

    private void stopQuietly(PartyJob job) {
        if (job == null || job.isTerminal()) {
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

    private String tail(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(text.length() - maxLength);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.PSI;
    }

    @Override
    public boolean cancel(String taskId) {
        // 设置取消标志——doExecute 的轮询循环每次都会检查，发现后立即退出
        CANCEL_FLAGS.put(taskId, Boolean.TRUE);

        // 尝试停止已经在 agent 上跑着的两方作业（如果 doExecute 还没结束）
        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("cancel PSI 任务 {} 时未找到任务", taskId);
            return false;
        }
        PsiRunningState state = parseRunningState(task.getfExecutionLog());
        if (state == null) {
            // doExecute 还没来得及写状态（或任务已完成/失败），只设标志即可
            return true;
        }
        boolean anyStopped = false;
        for (PsiRunningState.Party p : new PsiRunningState.Party[]{state.alice, state.bob}) {
            if (p == null || p.jobId == null || p.agentEndpoint == null) {
                continue;
            }
            try {
                boolean ok = agentClient.stopJob(p.agentEndpoint, p.jobId);
                log.info("PSI cancel: 通知 agent 停止 {} (jobId={}), agent 响应={}", p.party, p.jobId, ok);
                if (ok) {
                    anyStopped = true;
                }
            } catch (Exception e) {
                log.warn("PSI cancel: stop {} (jobId={}) 失败: {}", p.party, p.jobId, e.getMessage());
            }
        }
        if (state.clusterId != null) {
            releaseClusterQuietly(state.clusterId);
        }
        return anyStopped;
    }

    private void persistRunningState(String taskId, PartyJob aliceJob, PartyJob bobJob, String clusterId) {
        try {
            Map<String, Object> state = new LinkedHashMap<>();
            state.put("alice", partyToMap(aliceJob));
            state.put("bob", partyToMap(bobJob));
            state.put("clusterId", clusterId);
            String marker = PSI_RUNNING_STATE_PREFIX + objectMapper.writeValueAsString(state);
            TbTask task = taskMapper.selectById(taskId);
            if (task != null) {
                String existing = task.getfExecutionLog();
                String updated = existing == null || existing.isEmpty() ? marker : existing + "\n" + marker;
                task.setfExecutionLog(updated);
                task.setfUpdateTime(LocalDateTime.now());
                taskMapper.updateById(task);
            }
        } catch (Exception e) {
            log.warn("持久化 PSI 运行态失败（cancel 能力会受影响）: {}", e.getMessage());
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

    private PsiRunningState parseRunningState(String executionLog) {
        if (executionLog == null || executionLog.isEmpty()) {
            return null;
        }
        int idx = executionLog.lastIndexOf(PSI_RUNNING_STATE_PREFIX);
        if (idx < 0) {
            return null;
        }
        int start = idx + PSI_RUNNING_STATE_PREFIX.length();
        int end = executionLog.indexOf('\n', start);
        String json = end < 0 ? executionLog.substring(start) : executionLog.substring(start, end);
        try {
            Map<String, Object> m = objectMapper.readValue(json, Map.class);
            PsiRunningState state = new PsiRunningState();
            state.clusterId = (String) m.get("clusterId");
            state.alice = parseParty((Map<String, Object>) m.get("alice"));
            state.bob = parseParty((Map<String, Object>) m.get("bob"));
            return state;
        } catch (Exception e) {
            log.warn("解析 PSI 运行态失败: {}", e.getMessage());
            return null;
        }
    }

    private PsiRunningState.Party parseParty(Map<String, Object> m) {
        if (m == null) {
            return null;
        }
        PsiRunningState.Party p = new PsiRunningState.Party();
        p.party = (String) m.get("party");
        p.nodeId = (String) m.get("nodeId");
        p.jobId = (String) m.get("jobId");
        p.agentEndpoint = (String) m.get("agentEndpoint");
        return p;
    }

    @Override
    public TaskStatus queryStatus(String taskId) {
        // PSI任务状态由TaskService管理
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
        private int consecutivePollFailures;

        PartyJob(String party, String nodeId, String agentEndpoint, String jobId) {
            this.party = party;
            this.nodeId = nodeId;
            this.agentEndpoint = agentEndpoint;
            this.jobId = jobId;
        }

        void update(IAgentClient.TaskStatus taskStatus) {
            this.consecutivePollFailures = 0;
            this.status = taskStatus.getStatus();
            this.result = taskStatus.getResult();
            this.error = taskStatus.getError();
        }

        void recordPollFailure() {
            this.consecutivePollFailures++;
        }

        void markUnreachable() {
            this.status = STATUS_UNREACHABLE;
            this.error = "连续查询作业状态失败，节点Agent不可达";
        }

        void markTimeout() {
            this.status = STATUS_TIMEOUT;
        }

        void markStopped() {
            this.status = STATUS_STOPPED;
        }

        boolean isSucceeded() {
            return STATUS_SUCCEEDED.equals(status);
        }

        boolean isFailed() {
            return STATUS_FAILED.equals(status) || STATUS_STOPPED.equals(status)
                || STATUS_UNREACHABLE.equals(status);
        }

        boolean isTerminal() {
            return isSucceeded() || isFailed() || STATUS_TIMEOUT.equals(status);
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
     * 任务运行态（由 doExecute 写入 f_execution_log，cancel 时读取）。
     * 当前是 PSI 专用，后续如 FL 也要取消可抽出独立类。
     */
    private static class PsiRunningState {
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
