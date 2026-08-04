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
import com.tds.dos.service.privacycompute.code.PirCodeGenerator;
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
 * PIR 隐匿查询任务执行器
 * <p>
 * PIR 分为两个阶段：
 * 1. Setup 阶段：服务端（bob）对数据进行预处理，生成 OPRF 密钥和索引
 * 2. Query 阶段：客户端（alice）发送加密查询，服务端返回结果但不感知查询内容
 * <p>
 * 使用节点内置 C++ 二进制执行，通过 PirRunner 封装。
 * 支持 SealPIR（Label PIR）和 APSI（Keyword PIR）两种协议。
 */
@Slf4j
@Component
public class PirTaskExecutor extends AbstractTaskExecutor {

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

    @Autowired
    private PirRunner pirRunner;

    /** 超时时间，默认 9 分钟 */
    @Value("${privacy-compute.pir.timeout-ms:540000}")
    private long pirTimeoutMs;

    @Value("${privacy-compute.pir.poll-interval-ms:3000}")
    private long pollIntervalMs;

    /** 跨域通信端口基数 */
    private static final int CROSS_SILO_PORT_BASE = 30000;
    /** SPU通信端口基数，必须与跨域端口错开 */
    private static final int SPU_PORT_BASE = 32000;
    private static final int PORT_SLOT_COUNT = 1000;

    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_STOPPED = "STOPPED";
    private static final String STATUS_TIMEOUT = "TIMEOUT";

    /** 外部 cancel 触发的取消标志 */
    private static final ConcurrentMap<String, Boolean> CANCEL_FLAGS = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected String doExecute(String taskId, TbTask task, Map<String, String> params) throws Exception {
        log.info("===========================================");
        log.info("== PIR任务执行开始 taskId={}", taskId);
        log.info("===========================================");

        // 解析节点：serverNodeId 是服务端（bob），clientNodeId 是客户端（alice）
        String serverNodeId = params.get("serverNodeId");
        String clientNodeId = params.get("clientNodeId");
        String pirType = params.getOrDefault("pirType", "SealPIR");

        if (serverNodeId == null || clientNodeId == null) {
            throw new RuntimeException("PIR任务参数缺失: serverNodeId 或 clientNodeId");
        }

        TbNode serverNode = requireNode(serverNodeId);
        TbNode clientNode = requireNode(clientNodeId);

        String clusterId = null;
        String oprfKeyPath = null;

        try {
            // 创建 Ray 集群（用于节点间通信协调）
            log.info("【阶段1】创建Ray集群, server={}, client={}", serverNodeId, clientNodeId);
            clusterId = rayClusterService.createCluster("pir-" + taskId, List.of(serverNodeId, clientNodeId));
            String rayHeadUrl = rayClusterService.getHeadAddress(clusterId);
            log.info("【阶段1】Ray集群已创建, clusterId={}, headUrl={}", clusterId, rayHeadUrl);

            // 获取节点 IP
            String serverIp = resolveNodeIp(serverNode);
            String clientIp = resolveNodeIp(clientNode);
            int crossSiloPort = derivePort(taskId, CROSS_SILO_PORT_BASE);
            int spuPort = derivePort(taskId, SPU_PORT_BASE);
            log.info("【阶段2】节点地址: server={}, client={}, 端口={}", serverIp, clientIp, crossSiloPort);

            // 生成 OPRF 密钥路径
            oprfKeyPath = "/tmp/pir_oprf_key_" + taskId.replaceAll("[^A-Za-z0-9_-]", "_") + ".key";
            String oprfKeySavePath = oprfKeyPath;

            // 获取代码生成器
            ICodeGenerator codeGenerator = codeGeneratorFactory.getGenerator("PIR");
            PirRunner.PirProtocol protocol = PirRunner.PirProtocol.valueOf(
                pirType.substring(0, 1).toUpperCase() + pirType.substring(1).toLowerCase()
            );

            // ===== Setup 阶段：服务端（bob）执行 =====
            log.info("【阶段3】执行PIR Setup... protocol={}", pirType);
            Map<String, Object> setupParams = new LinkedHashMap<>();
            setupParams.put(PirCodeGenerator.PARAM_PIR_TYPE, pirType);
            setupParams.put(PirCodeGenerator.PARAM_SELF_PARTY, PirCodeGenerator.PARTY_BOB);
            setupParams.put(PirCodeGenerator.PARAM_PHASE, "setup");
            setupParams.put(PirCodeGenerator.PARAM_SERVER_DATA_PATH, params.get("inputPath"));
            setupParams.put(PirCodeGenerator.PARAM_KEY_COLUMN, params.get("keyColumn"));
            setupParams.put(PirCodeGenerator.PARAM_LABEL_COLUMNS, params.get("labelColumns"));
            setupParams.put(PirCodeGenerator.PARAM_OPRF_KEY_PATH, oprfKeyPath);
            setupParams.put(PirCodeGenerator.PARAM_OPRF_KEY_SAVE_PATH, oprfKeySavePath);
            setupParams.put(PirCodeGenerator.PARAM_NUM_PER_QUERY, params.getOrDefault("numPerQuery", "1"));
            setupParams.put(PirCodeGenerator.PARAM_LABEL_MAX_LEN, params.getOrDefault("labelMaxLen", "256"));

            // 填充地址参数（占位符，供未来扩展使用）
            setupParams.put("_SERVER_IP", serverIp);
            setupParams.put("_CLIENT_IP", clientIp);
            setupParams.put("_CROSS_SILO_PORT", String.valueOf(crossSiloPort));
            setupParams.put("_SPU_PORT", String.valueOf(spuPort));

            String setupJsonConfig = codeGenerator.generateCode(taskId, setupParams);
            saveAuditCode(taskId, setupJsonConfig);
            log.info("【阶段3】Setup配置已生成, {} 字符", setupJsonConfig.length());

            // 在服务端执行 Setup
            String setupJobId = "pir-setup-" + taskId;
            PirRunner.Result setupResult = pirRunner.run(
                serverNode.getfEndpoint(),
                setupJobId,
                setupJsonConfig,
                PirRunner.RunMode.SETUP,
                protocol
            );

            if (!setupResult.isSuccess()) {
                throw new RuntimeException("PIR Setup失败: " + setupResult.parseError());
            }
            log.info("【阶段3】PIR Setup完成, jobId={}", setupJobId);

            // ===== Query 阶段：客户端（alice）执行 =====
            log.info("【阶段4】执行PIR Query... protocol={}", pirType);
            String queryValue = params.get("queryValue");
            if (queryValue == null || queryValue.isEmpty()) {
                throw new RuntimeException("PIR任务参数缺失: queryValue");
            }

            Map<String, Object> queryParams = new LinkedHashMap<>();
            queryParams.put(PirCodeGenerator.PARAM_PIR_TYPE, pirType);
            queryParams.put(PirCodeGenerator.PARAM_SELF_PARTY, PirCodeGenerator.PARTY_ALICE);
            queryParams.put(PirCodeGenerator.PARAM_PHASE, "query");
            queryParams.put(PirCodeGenerator.PARAM_KEY_COLUMN, params.get("keyColumn"));
            queryParams.put(PirCodeGenerator.PARAM_QUERY_COLUMN, params.get("keyColumn"));
            queryParams.put(PirCodeGenerator.PARAM_QUERY_VALUE, queryValue);
            queryParams.put(PirCodeGenerator.PARAM_OPRF_KEY_PATH, oprfKeyPath);

            queryParams.put("_SERVER_IP", serverIp);
            queryParams.put("_CLIENT_IP", clientIp);
            queryParams.put("_CROSS_SILO_PORT", String.valueOf(crossSiloPort));
            queryParams.put("_SPU_PORT", String.valueOf(spuPort));

            String queryJsonConfig = codeGenerator.generateCode(taskId, queryParams);

            // 在客户端执行 Query
            String queryJobId = "pir-query-" + taskId;
            PirRunner.Result queryResult = pirRunner.run(
                clientNode.getfEndpoint(),
                queryJobId,
                queryJsonConfig,
                PirRunner.RunMode.QUERY,
                protocol
            );

            if (!queryResult.isSuccess()) {
                throw new RuntimeException("PIR Query失败: " + queryResult.parseError());
            }

            // 构建结果
            return buildResult(taskId, rayHeadUrl, clusterId, queryResult, oprfKeyPath);

        } finally {
            // 释放集群
            if (clusterId != null) {
                try {
                    rayClusterService.releaseCluster(clusterId);
                    log.info("Ray集群已释放, clusterId={}", clusterId);
                } catch (Exception e) {
                    log.warn("释放Ray集群失败: {}", e.getMessage());
                }
            }
            CANCEL_FLAGS.remove(taskId);
            log.info("== PIR任务执行结束 taskId={}", taskId);
        }
    }

    private String buildResult(String taskId, String rayHeadUrl, String clusterId,
                               PirRunner.Result queryResult, String oprfKeyPath) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task_id", taskId);
        result.put("ray_head_url", rayHeadUrl);
        result.put("ray_cluster_id", clusterId);
        result.put("oprf_key_path", oprfKeyPath);
        result.put("status", "ok");

        // 解析 Query 结果
        String resultJson = queryResult.parseResultJson();
        if (resultJson != null) {
            try {
                Map<String, Object> parsed = objectMapper.readValue(resultJson, Map.class);
                result.put("result", parsed.get("result"));
                result.put("output_path", parsed.get("outputPath"));
            } catch (Exception e) {
                log.warn("解析PIR结果JSON失败: {}", e.getMessage());
                result.put("raw_result", resultJson);
            }
        }

        result.put("message", "PIR任务执行成功");
        log.info("PIR任务执行成功, taskId={}", taskId);
        return objectMapper.writeValueAsString(result);
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
            throw new RuntimeException("无法访问节点Agent: " + node.getfNodeId() + " (" + node.getfEndpoint() + ")");
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
        return TaskType.PIR;
    }

    @Override
    public boolean cancel(String taskId) {
        CANCEL_FLAGS.put(taskId, Boolean.TRUE);
        pirRunner.cancel(taskId);
        log.info("PIR任务取消标志已设置: {}", taskId);
        return true;
    }

    @Override
    public TaskStatus queryStatus(String taskId) {
        return null;
    }
}
