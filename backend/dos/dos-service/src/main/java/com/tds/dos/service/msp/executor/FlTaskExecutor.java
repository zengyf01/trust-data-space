package com.tds.dos.service.msp.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbNode;
import com.tds.dos.dal.msp.entity.TbTask;
import com.tds.dos.dal.msp.mapper.TbTaskMapper;
import com.tds.dos.service.msp.node.INodeService;
import com.tds.dos.service.privacycompute.code.CodeGeneratorFactory;
import com.tds.dos.service.privacycompute.code.FlCodeGenerator;
import com.tds.dos.service.privacycompute.code.ICodeGenerator;
import com.tds.dos.service.ray.IAgentClient;
import com.tds.dos.service.ray.IRayClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 横向联邦学习任务执行器
 * <p>
 * SecretFlow PRODUCTION 模式（ray_mode=False）执行横向联邦学习。
 * 各方拥有相同的特征，不同的样本数据。
 */
@Slf4j
@Component
public class FlTaskExecutor extends AbstractTaskExecutor {

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

    @Value("${privacy-compute.fl.timeout-ms:600000}")
    private long flTimeoutMs;

    @Value("${privacy-compute.fl.poll-interval-ms:5000}")
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected String doExecute(String taskId, TbTask task, Map<String, String> params) throws Exception {
        log.info("===========================================");
        log.info("== FL任务执行开始 taskId={}", taskId);
        log.info("===========================================");

        // 解析参与节点
        String partyANodeId = params.get("partyANodeId");
        String partyBNodeId = params.get("partyBNodeId");

        if (partyANodeId == null || partyBNodeId == null) {
            throw new RuntimeException("FL任务参数缺失: partyANodeId 或 partyBNodeId");
        }

        TbNode nodeA = requireNode(partyANodeId);
        TbNode nodeB = requireNode(partyBNodeId);

        String clusterId = null;
        String jobIdA = null;
        String jobIdB = null;

        try {
            // 创建 Ray 集群
            log.info("【阶段1】创建Ray集群, partyA={}, partyB={}", partyANodeId, partyBNodeId);
            clusterId = rayClusterService.createCluster("fl-" + taskId, List.of(partyANodeId, partyBNodeId));
            String rayHeadUrl = rayClusterService.getHeadAddress(clusterId);
            log.info("【阶段1】Ray集群已创建, clusterId={}, headUrl={}", clusterId, rayHeadUrl);

            // 获取节点 IP
            String nodeAIp = resolveNodeIp(nodeA);
            String nodeBIp = resolveNodeIp(nodeB);
            int crossSiloPort = derivePort(taskId, CROSS_SILO_PORT_BASE);
            int spuPort = derivePort(taskId, SPU_PORT_BASE);
            log.info("【阶段2】节点地址: partyA={}, partyB={}, 端口={}", nodeAIp, nodeBIp, crossSiloPort);

            // 生成代码
            ICodeGenerator codeGenerator = codeGeneratorFactory.getGenerator("FL");
            Map<String, Object> baseParams = buildCodeParams(params, nodeAIp, nodeBIp, crossSiloPort, spuPort);

            String aliceCode = generateFor(codeGenerator, taskId, baseParams, FlCodeGenerator.PARTY_ALICE);
            String bobCode = generateFor(codeGenerator, taskId, baseParams, FlCodeGenerator.PARTY_BOB);
            saveAuditCode(taskId, aliceCode);
            log.info("【阶段3】双方脚本已生成, alice={} 字符, bob={} 字符", aliceCode.length(), bobCode.length());

            // 提交作业
            jobIdA = agentClient.submitJob(nodeA.getfEndpoint(), aliceCode, taskId + "-alice");
            jobIdB = agentClient.submitJob(nodeB.getfEndpoint(), bobCode, taskId + "-bob");
            log.info("【阶段4】双方作业已提交, aliceJobId={}, bobJobId={}", jobIdA, jobIdB);

            // 轮询等待
            IAgentClient.TaskStatus statusA = waitForJob(taskId, nodeA.getfEndpoint(), jobIdA, "alice");
            IAgentClient.TaskStatus statusB = waitForJob(taskId, nodeB.getfEndpoint(), jobIdB, "bob");

            // 构建结果
            return buildResult(taskId, rayHeadUrl, clusterId, jobIdA, jobIdB, statusA, statusB);

        } finally {
            if (jobIdA != null) {
                try {
                    agentClient.stopJob(nodeA.getfEndpoint(), jobIdA);
                } catch (Exception e) {
                    log.warn("停止alice作业失败: {}", e.getMessage());
                }
            }
            if (jobIdB != null) {
                try {
                    agentClient.stopJob(nodeB.getfEndpoint(), jobIdB);
                } catch (Exception e) {
                    log.warn("停止bob作业失败: {}", e.getMessage());
                }
            }
            if (clusterId != null) {
                try {
                    rayClusterService.releaseCluster(clusterId);
                    log.info("Ray集群已释放, clusterId={}", clusterId);
                } catch (Exception e) {
                    log.warn("释放Ray集群失败: {}", e.getMessage());
                }
            }
            CANCEL_FLAGS.remove(taskId);
            log.info("== FL任务执行结束 taskId={}", taskId);
        }
    }

    private Map<String, Object> buildCodeParams(Map<String, String> params, String nodeAIp, String nodeBIp,
                                            int crossSiloPort, int spuPort) {
        Map<String, Object> codeParams = new LinkedHashMap<>();
        codeParams.put(FlCodeGenerator.PARAM_PARTY_A_DATA_PATH, params.get("partyADataPath"));
        codeParams.put(FlCodeGenerator.PARAM_PARTY_B_DATA_PATH, params.get("partyBDataPath"));
        codeParams.put(FlCodeGenerator.PARAM_LABEL_COLUMN, params.get("labelColumn"));
        codeParams.put(FlCodeGenerator.PARAM_FEATURE_COLUMNS, params.get("featureColumns"));
        codeParams.put(FlCodeGenerator.PARAM_MODEL_TYPE, params.getOrDefault("modelType", "LR"));
        codeParams.put(FlCodeGenerator.PARAM_DELIVERY_MODE, params.getOrDefault("deliveryMode", "AGGREGATOR_ONLY"));
        codeParams.put(FlCodeGenerator.PARAM_EPOCHS, params.getOrDefault("epochs", "10"));
        codeParams.put(FlCodeGenerator.PARAM_BATCH_SIZE, params.getOrDefault("batchSize", "32"));
        codeParams.put(FlCodeGenerator.PARAM_LEARNING_RATE, params.getOrDefault("learningRate", "0.01"));
        codeParams.put(FlCodeGenerator.PARAM_PARTY_A_CROSS_SILO_ADDRESS, nodeAIp + ":" + crossSiloPort);
        codeParams.put(FlCodeGenerator.PARAM_PARTY_B_CROSS_SILO_ADDRESS, nodeBIp + ":" + crossSiloPort);
        codeParams.put(FlCodeGenerator.PARAM_PARTY_A_SPU_ADDRESS, nodeAIp + ":" + spuPort);
        codeParams.put(FlCodeGenerator.PARAM_PARTY_B_SPU_ADDRESS, nodeBIp + ":" + spuPort);
        return codeParams;
    }

    private IAgentClient.TaskStatus waitForJob(String taskId, String agentEndpoint, String jobId, String partyName) {
        long deadline = System.currentTimeMillis() + flTimeoutMs;
        int consecutiveFailures = 0;

        while (System.currentTimeMillis() < deadline) {
            if (Boolean.TRUE.equals(CANCEL_FLAGS.get(taskId))) {
                throw new RuntimeException("FL任务已取消");
            }

            try {
                IAgentClient.TaskStatus status = agentClient.getTaskStatus(agentEndpoint, jobId);
                if (status != null) {
                    consecutiveFailures = 0;
                    log.info("[FL {}] jobId={}, status={}", partyName, jobId, status.getStatus());

                    if (STATUS_SUCCEEDED.equals(status.getStatus())) {
                        return status;
                    } else if (STATUS_FAILED.equals(status.getStatus())) {
                        throw new RuntimeException("FL " + partyName + "失败: " + status.getError());
                    }
                }
            } catch (Exception e) {
                consecutiveFailures++;
                log.warn("[FL {}] 查询状态异常: {}", partyName, e.getMessage());
                if (consecutiveFailures >= 5) {
                    throw new RuntimeException("FL " + partyName + "执行失败: 连续查询失败 " + consecutiveFailures + " 次");
                }
            }

            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("FL等待被中断");
            }
        }

        throw new RuntimeException("FL任务执行超时");
    }

    private String buildResult(String taskId, String rayHeadUrl, String clusterId,
                            String jobIdA, String jobIdB,
                            IAgentClient.TaskStatus statusA, IAgentClient.TaskStatus statusB) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task_id", taskId);
        result.put("ray_head_url", rayHeadUrl);
        result.put("ray_cluster_id", clusterId);

        // 包含 jobs 信息供下载使用
        Map<String, Object> jobs = new LinkedHashMap<>();
        Map<String, Object> aliceInfo = new LinkedHashMap<>();
        aliceInfo.put("job_id", jobIdA);
        aliceInfo.put("status", statusA.getStatus());
        jobs.put("alice", aliceInfo);
        Map<String, Object> bobInfo = new LinkedHashMap<>();
        bobInfo.put("job_id", jobIdB);
        bobInfo.put("status", statusB.getStatus());
        jobs.put("bob", bobInfo);
        result.put("jobs", jobs);

        if (!STATUS_SUCCEEDED.equals(statusA.getStatus()) || !STATUS_SUCCEEDED.equals(statusB.getStatus())) {
            result.put("status", "error");
            result.put("message", "FL任务执行失败");
            return objectMapper.writeValueAsString(result);
        }

        // 解析结果摘要
        Map<String, Object> summaryA = parseSummary(statusA.getResult());
        Map<String, Object> summaryB = parseSummary(statusB.getResult());

        result.put("status", "ok");
        result.put("party_alice", summaryA != null ? summaryA : new LinkedHashMap<>());
        result.put("party_bob", summaryB != null ? summaryB : new LinkedHashMap<>());
        result.put("message", "FL任务执行成功");

        log.info("FL任务执行成功, taskId={}", taskId);
        return objectMapper.writeValueAsString(result);
    }

    private Map<String, Object> parseSummary(String stdout) {
        if (stdout == null || stdout.isEmpty()) {
            return null;
        }
        int index = stdout.lastIndexOf(FlCodeGenerator.RESULT_PREFIX);
        if (index < 0) {
            return null;
        }
        int start = index + FlCodeGenerator.RESULT_PREFIX.length();
        int end = stdout.indexOf('\n', start);
        String json = (end < 0 ? stdout.substring(start) : stdout.substring(start, end)).trim();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("解析FL结果摘要失败: {}", e.getMessage());
            return null;
        }
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
        partyParams.put(FlCodeGenerator.PARAM_SELF_PARTY, selfParty);
        String code = generator.generateCode(taskId, partyParams);
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("生成的FL脚本为空, selfParty=" + selfParty);
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

    @Override
    public TaskType getTaskType() {
        return TaskType.HORIZONTAL_FL;
    }

    @Override
    public boolean cancel(String taskId) {
        CANCEL_FLAGS.put(taskId, Boolean.TRUE);
        log.info("FL任务取消标志已设置: {}", taskId);
        return true;
    }

    @Override
    public TaskStatus queryStatus(String taskId) {
        return null;
    }
}
