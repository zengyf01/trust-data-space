package com.tds.dos.service.privacycompute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbNode;
import com.tds.dos.dal.msp.entity.TbTask;
import com.tds.dos.dal.msp.mapper.TbTaskMapper;
import com.tds.dos.service.msp.node.INodeService;
import com.tds.dos.service.msp.node.NodeDTO;
import com.tds.dos.service.msp.task.ITaskService;
import com.tds.dos.service.msp.task.TaskDTO;
import com.tds.dos.service.privacycompute.code.CodeGeneratorFactory;
import com.tds.dos.service.privacycompute.code.ICodeGenerator;
import com.tds.dos.service.ray.IAgentClient;
import com.tds.dos.service.ray.IRayClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 隐私计算服务实现
 * 直接调用本地MSP Service
 */
@Slf4j
@Service
public class PrivacyComputeServiceImpl implements IPrivacyComputeService {

    @Autowired
    private ITaskService taskService;

    @Autowired
    private INodeService nodeService;

    @Autowired
    private CodeGeneratorFactory codeGeneratorFactory;

    @Autowired
    private IRayClusterService rayClusterService;

    @Autowired
    private IAgentClient agentClient;

    @Autowired
    private TbTaskMapper taskMapper;

    // 任务状态常量 (与 MSP TaskStatus 对应)
    private static final int STATUS_CREATED = 1;
    private static final int STATUS_PENDING = 2;
    private static final int STATUS_RUNNING = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_FAILED = 5;
    private static final int STATUS_CANCELLED = 6;

    @Override
    public String createTask(Map<String, Object> params) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName((String) params.getOrDefault("name", "Task-" + System.currentTimeMillis()));
        taskDTO.setDescription((String) params.get("description"));
        taskDTO.setNodeMode((String) params.getOrDefault("nodeMode", "RAY"));

        // 设置任务类型
        String computeType = (String) params.getOrDefault("computeType", "PSI");
        TaskType taskType = TaskType.PSI;
        if ("PSI".equalsIgnoreCase(computeType)) {
            taskType = TaskType.PSI;
        } else if ("MPC".equalsIgnoreCase(computeType)) {
            taskType = TaskType.MPC;
        } else if ("FEDERATED_LEARNING".equalsIgnoreCase(computeType)) {
            taskType = TaskType.FEDERATED_LEARNING;
        } else if ("VERTICAL_FL".equalsIgnoreCase(computeType)) {
            taskType = TaskType.VERTICAL_FL;
        }
        taskDTO.setType(taskType);

        // 设置参与方
        Object participants = params.get("participants");
        if (participants instanceof List) {
            taskDTO.setParticipants((List<String>) participants);
        }

        // 设置参数
        Map<String, String> taskParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                taskParams.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        taskDTO.setParameters(taskParams);

        return taskService.createTask(taskDTO);
    }

    @Override
    public void executeTask(String taskId) {
        taskService.executeTask(taskId);
    }

    @Override
    public Integer queryTaskStatus(String taskId) {
        TaskStatus status = taskService.queryStatus(taskId);
        if (status != null) {
            return status.getCode();
        }
        return null;
    }

    @Override
    public String getTaskResult(String taskId) {
        ApiResponse<String> response = taskService.getTaskResult(taskId);
        if (response != null && response.getData() != null) {
            return response.getData();
        }
        return null;
    }

    @Override
    public String getTaskCode(String taskId) {
        ApiResponse<String> response = taskService.getTaskCode(taskId);
        if (response != null && response.getData() != null) {
            return response.getData();
        }
        return null;
    }

    @Override
    public void cancelTask(String taskId) {
        taskService.cancelTask(taskId);
    }

    @Override
    public void deleteTask(String taskId) {
        taskService.deleteTask(taskId);
    }

    @Override
    public com.tds.dos.dal.msp.entity.TbTask getTaskById(String taskId) {
        return taskService.getTask(taskId);
    }

    @Override
    public Map<String, Object> listTasks(int page, int size) {
        com.tds.dos.common.core.PageResult<com.tds.dos.dal.msp.entity.TbTask> pageResult =
            taskService.listTasks(page, size, null, null);

        List<Map<String, Object>> list = new ArrayList<>();
        if (pageResult != null && pageResult.getList() != null) {
            for (com.tds.dos.dal.msp.entity.TbTask task : pageResult.getList()) {
                Map<String, Object> item = new HashMap<>();
                item.put("taskId", task.getfId());
                item.put("name", task.getfName());
                item.put("type", task.getfType());
                item.put("status", task.getfStatus());
                item.put("createTime", task.getfCreateTime() != null ? task.getfCreateTime().toString() : null);
                item.put("nodeMode", task.getfNodeMode());
                item.put("description", task.getfDescription());
                item.put("algorithm", task.getfAlgorithm());
                item.put("parameters", task.getfParameters());
                item.put("code", task.getfCode());
                list.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        long total = 0;
        if (pageResult != null && pageResult.getPagination() != null) {
            total = pageResult.getPagination().getTotal();
        }
        result.put("pagination", Map.of(
            "currentPage", page,
            "pageSize", size,
            "total", total
        ));
        return result;
    }

    @Override
    public String executePsiTask(String taskName, String partyADataPath, String partyBDataPath,
                                   String keyColumn, Map<String, Object> params) {
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("computeType", "PSI");
        taskParams.put("name", taskName);
        taskParams.put("partyADataPath", partyADataPath);
        taskParams.put("partyBDataPath", partyBDataPath);
        taskParams.put("keyColumn", keyColumn);
        taskParams.put("nodeMode", params.getOrDefault("nodeMode", "RAY"));
        taskParams.put("protocol", params.getOrDefault("protocol", "ECPSI")); // ECPSI/RR22PSI

        if (params.get("resultType") != null) {
            taskParams.put("resultType", params.get("resultType")); // INTERSECTION/UNION/...
        }
        if (params.get("partyANodeId") != null) {
            taskParams.put("partyANodeId", params.get("partyANodeId"));
        }
        if (params.get("partyBNodeId") != null) {
            taskParams.put("partyBNodeId", params.get("partyBNodeId"));
        }

        // 获取参与节点列表 - 优先使用参数中指定的节点，否则使用在线节点
        List<String> participantNodeIds = new ArrayList<>();
        String partyANodeIdParam = params.get("partyANodeId") != null ? (String) params.get("partyANodeId") : null;
        String partyBNodeIdParam = params.get("partyBNodeId") != null ? (String) params.get("partyBNodeId") : null;

        if (partyANodeIdParam != null && partyBNodeIdParam != null) {
            // 使用参数中指定的节点
            participantNodeIds.add(partyANodeIdParam);
            participantNodeIds.add(partyBNodeIdParam);
            log.info("Using specified nodes for PSI task: partyA={}, partyB={}", partyANodeIdParam, partyBNodeIdParam);
        } else {
            // 使用在线节点作为后备
            List<String> availableNodes = getAvailableNodes();
            if (availableNodes == null || availableNodes.isEmpty()) {
                throw new RuntimeException("没有可用的节点来创建Ray集群");
            }
            // 至少需要2个节点进行PSI
            if (availableNodes.size() < 2) {
                throw new RuntimeException("PSI任务至少需要2个节点，当前可用节点数量: " + availableNodes.size());
            }
            // 取前2个节点
            participantNodeIds.add(availableNodes.get(0));
            participantNodeIds.add(availableNodes.get(1));
            log.info("Using first two available nodes for PSI task: partyA={}, partyB={}", participantNodeIds.get(0), participantNodeIds.get(1));
        }

        // 创建Ray集群
        String clusterId = null;
        try {
            log.info("Creating Ray cluster for PSI task {}, participants: {}", taskName, participantNodeIds);
            clusterId = rayClusterService.createCluster(taskName, participantNodeIds);
            log.info("Ray cluster {} created successfully", clusterId);
        } catch (Exception e) {
            log.error("Failed to create Ray cluster for PSI task: {}", e.getMessage());
            throw new RuntimeException("创建Ray集群失败: " + e.getMessage());
        }

        // 创建任务
        String taskId = createTask(taskParams);

        // 生成PSI代码并保存
        String protocol = (String) params.getOrDefault("protocol", "ECPSI");
        String resultType = params.get("resultType") != null ? (String) params.get("resultType") : "INTERSECTION";

        // 获取集群Head地址
        String headAddress = rayClusterService.getHeadAddress(clusterId);
        if (headAddress == null) {
            throw new RuntimeException("无法获取集群Head地址");
        }

        // 获取参与节点的Agent HTTP地址（用于SecretFlow集群配置）
        // SecretFlow的cluster_config中parties需要各方的Agent地址（HTTP端口），而非Ray地址
        String partyAAddress = null;
        String partyBAddress = null;
        if (participantNodeIds.size() >= 2) {
            TbNode nodeA = nodeService.getNode(participantNodeIds.get(0));
            TbNode nodeB = nodeService.getNode(participantNodeIds.get(1));
            if (nodeA != null && nodeA.getfEndpoint() != null) {
                partyAAddress = nodeA.getfEndpoint();
            }
            if (nodeB != null && nodeB.getfEndpoint() != null) {
                partyBAddress = nodeB.getfEndpoint();
            }
        } else if (participantNodeIds.size() == 1) {
            // 单节点情况：partyA和partyB都使用同一个节点（仅用于测试）
            TbNode node = nodeService.getNode(participantNodeIds.get(0));
            if (node != null && node.getfEndpoint() != null) {
                partyAAddress = node.getfEndpoint();
                partyBAddress = node.getfEndpoint();
            }
        }

        // 使用代码生成器工厂生成代码
        ICodeGenerator codeGenerator = codeGeneratorFactory.getGenerator("PSI");
        Map<String, Object> codeParams = new HashMap<>();
        codeParams.put("partyADataPath", partyADataPath);
        codeParams.put("partyBDataPath", partyBDataPath);
        codeParams.put("keyColumn", keyColumn);
        codeParams.put("protocol", protocol);
        codeParams.put("resultType", resultType);
        codeParams.put("rayAddress", headAddress);
        codeParams.put("partyAAddress", partyAAddress);
        codeParams.put("partyBAddress", partyBAddress);
        String pythonCode = codeGenerator.generateCode(taskId, codeParams);

        // 更新任务的fCode字段
        TbTask task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setfCode(pythonCode);
            task.setfUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
            log.info("PSI task {} code generated and saved, code length: {}", taskId, pythonCode.length());
        }

        // 提交任务到多个节点
        // PSI需要alice和bob两方都执行，每个节点执行相同的代码但根据sf.get_party()决定执行哪个分支
        // Head节点作为alice，Worker节点作为bob
        Map<String, String> jobIds = new HashMap<>();
        try {
            String clusterStatus = rayClusterService.getClusterStatus(clusterId);
            if ("RUNNING".equals(clusterStatus) && !participantNodeIds.isEmpty()) {
                // 更新任务状态为执行中
                if (task != null) {
                    task.setfStatus(3); // RUNNING
                    taskMapper.updateById(task);
                }

                // 向每个参与节点提交任务
                for (int i = 0; i < participantNodeIds.size(); i++) {
                    String nodeId = participantNodeIds.get(i);
                    TbNode node = nodeService.getNode(nodeId);
                    if (node != null) {
                        String nodeJobId = agentClient.submitJob(node.getfEndpoint(), pythonCode, taskId + "_node" + i);
                        jobIds.put(nodeId, nodeJobId);
                        log.info("PSI task {} submitted to node {}, jobId: {}", taskId, nodeId, nodeJobId);
                    }
                }

                // 等待所有节点的任务完成
                // 使用Head节点作为主查询节点
                TbNode headNode = nodeService.getNode(participantNodeIds.get(0));
                if (headNode != null && !jobIds.isEmpty()) {
                    String mainJobId = jobIds.get(participantNodeIds.get(0));
                    waitForJobCompletion(headNode.getfEndpoint(), mainJobId, taskId, task);
                }
            }
        } catch (Exception e) {
            log.error("Failed to submit PSI task to cluster: {}", e.getMessage());
            // 不抛出异常，任务已创建，代码已保存，后续可以手动执行
        }

        return taskId;
    }

    /**
     * 等待Ray Job完成并获取结果
     */
    private void waitForJobCompletion(String agentEndpoint, String jobId, String taskId, TbTask task) {
        int maxRetries = 60; // 最多等待60次（约5分钟）
        int retryInterval = 5000; // 5秒间隔

        for (int i = 0; i < maxRetries; i++) {
            try {
                Thread.sleep(retryInterval);

                IAgentClient.TaskStatus status = agentClient.getTaskStatus(agentEndpoint, jobId);
                if (status == null) {
                    log.warn("Task status query returned null for jobId: {}", jobId);
                    continue;
                }

                String taskStatus = status.getStatus();
                log.info("Job {} status: {}, attempt: {}/{}", jobId, taskStatus, i + 1, maxRetries);

                if ("SUCCEEDED".equalsIgnoreCase(taskStatus)) {
                    // 任务成功，更新结果
                    if (task != null) {
                        task.setfStatus(4); // COMPLETED
                        task.setfResult(status.getResult());
                        task.setfExecutionLog("Job succeeded\n" + status.getResult());
                        task.setfUpdateTime(LocalDateTime.now());
                        taskMapper.updateById(task);
                    }
                    log.info("PSI task {} completed successfully", taskId);
                    return;

                } else if ("FAILED".equalsIgnoreCase(taskStatus)) {
                    // 任务失败
                    if (task != null) {
                        task.setfStatus(5); // FAILED
                        task.setfResult("{\"status\":\"error\",\"message\":\"" + (status.getError() != null ? status.getError().replace("\"", "\\\"") : "Unknown error") + "\"}");
                        task.setfExecutionLog("Job failed: " + status.getError());
                        task.setfUpdateTime(LocalDateTime.now());
                        taskMapper.updateById(task);
                    }
                    log.error("PSI task {} failed: {}", taskId, status.getError());
                    return;

                } else if ("STOPPED".equalsIgnoreCase(taskStatus)) {
                    // 任务被停止
                    if (task != null) {
                        task.setfStatus(6); // CANCELLED
                        task.setfExecutionLog("Job stopped by user");
                        task.setfUpdateTime(LocalDateTime.now());
                        taskMapper.updateById(task);
                    }
                    return;
                }
                // PENDING, RUNNING, SUBMITTED 继续等待

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Wait for job completion interrupted: {}", jobId);
                return;
            } catch (Exception e) {
                log.error("Error waiting for job completion: {}", e.getMessage());
                // 继续等待，不立即退出
            }
        }

        // 超时
        if (task != null) {
            task.setfStatus(5); // FAILED
            task.setfResult("{\"status\":\"error\",\"message\":\"Task execution timeout\"}");
            task.setfExecutionLog("Job execution timeout after " + (maxRetries * retryInterval / 1000) + " seconds");
            task.setfUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
        log.error("PSI task {} wait timeout", taskId);
    }

    @Override
    public Map<String, Object> executePsiTaskWithResult(String taskName, String partyADataPath,
                                                          String partyBDataPath, String keyColumn,
                                                          int timeoutSeconds) {
        String taskId = executePsiTask(taskName, partyADataPath, partyBDataPath, keyColumn, new HashMap<>());
        return waitForTaskResult(taskId, timeoutSeconds);
    }

    @Override
    public String executeMpcTask(String taskName, List<String> participants,
                                  String algorithm, Map<String, Object> params) {
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("computeType", "MPC");
        taskParams.put("name", taskName);
        taskParams.put("algorithm", algorithm);
        taskParams.put("participants", participants);

        taskParams.putAll(params);
        return createTask(taskParams);
    }

    @Override
    public String executeFederatedLearningTask(String taskName, List<String> participants,
                                                String labelColumn, List<String> featureColumns,
                                                Map<String, Object> params) {
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("computeType", "FEDERATED_LEARNING");
        taskParams.put("name", taskName);
        taskParams.put("participants", participants);
        taskParams.put("labelColumn", labelColumn);
        taskParams.put("featureColumns", featureColumns);
        taskParams.put("nodeMode", params.getOrDefault("nodeMode", "RAY"));

        // 联邦学习特定参数
        if (params.get("modelType") != null) {
            taskParams.put("modelType", params.get("modelType")); // LR/NN/XGB
        }
        if (params.get("epochs") != null) {
            taskParams.put("epochs", params.get("epochs"));
        }
        if (params.get("batchSize") != null) {
            taskParams.put("batchSize", params.get("batchSize"));
        }
        if (params.get("learningRate") != null) {
            taskParams.put("learningRate", params.get("learningRate"));
        }

        return createTask(taskParams);
    }

    @Override
    public String executeVerticalFlTask(String taskName, List<String> participants,
                                         String labelColumn, Map<String, List<String>> featureColumns,
                                         Map<String, Object> params) {
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("computeType", "VERTICAL_FL");
        taskParams.put("name", taskName);
        taskParams.put("participants", participants);
        taskParams.put("labelColumn", labelColumn);
        taskParams.put("featureColumns", featureColumns);
        taskParams.put("nodeMode", params.getOrDefault("nodeMode", "RAY"));

        return createTask(taskParams);
    }

    @Override
    public String createDagTask(String dagName, String dagDefinition, List<String> participants, Map<String, Object> params) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName(dagName != null ? dagName : "DAG-" + System.currentTimeMillis());
        taskDTO.setType(TaskType.COMPONENT_DAG);
        taskDTO.setNodeMode((String) params.getOrDefault("nodeMode", "RAY"));

        if (participants != null) {
            taskDTO.setParticipants(participants);
        }

        // 将 DAG 定义放入参数中
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("dag_definition", dagDefinition);
        taskParams.put("algorithm", "component_dag");

        // 添加其他参数
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() != null && !"nodeMode".equals(entry.getKey())) {
                    taskParams.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        taskDTO.setParameters(taskParams);

        // 调用 MSP 的 TaskService 创建 DAG 任务（需要通过 HTTP 调用 MSP 后端）
        // 由于当前是代理模式，需要通过 HTTP 调用 MSP 的 saveDag 接口
        return callMspSaveDag(taskDTO);
    }

    @Override
    public void executeDagTask(String taskId) {
        // 通过 HTTP 调用 MSP 的 executeTask 接口
        callMspExecuteTask(taskId);
    }

    @Override
    public String submitDagTask(String dagName, String dagDefinition, List<String> participants, Map<String, Object> params) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName(dagName != null ? dagName : "DAG-" + System.currentTimeMillis());
        taskDTO.setType(TaskType.COMPONENT_DAG);
        taskDTO.setNodeMode((String) params.getOrDefault("nodeMode", "RAY"));

        if (participants != null) {
            taskDTO.setParticipants(participants);
        }

        // 将 DAG 定义放入参数中
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("dag_definition", dagDefinition);
        taskParams.put("algorithm", "component_dag");

        // 添加其他参数
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() != null && !"nodeMode".equals(entry.getKey())) {
                    taskParams.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        taskDTO.setParameters(taskParams);

        // 调用 MSP 的 TaskService 提交 DAG 任务
        return callMspSubmitTask(taskDTO);
    }

    /**
     * 调用 MSP 后端保存 DAG 任务
     */
    private String callMspSaveDag(TaskDTO taskDTO) {
        // 构建 TaskRequest JSON
        Map<String, Object> request = new HashMap<>();
        request.put("name", taskDTO.getName());
        request.put("type", "COMPONENT_DAG");
        request.put("algorithm", taskDTO.getParameters().get("algorithm"));
        request.put("nodeMode", taskDTO.getNodeMode());

        if (taskDTO.getParticipants() != null) {
            request.put("participants", taskDTO.getParticipants());
        }

        Map<String, String> params = taskDTO.getParameters();
        request.put("parameters", params);

        try {
            // 调用 MSP 的 REST API: POST /api/v1/msp/tasks/save
            String mspUrl = getMspApiUrl("/api/v1/msp/tasks/save");
            String response = httpPost(mspUrl, request);
            // 解析返回的 taskId
            return parseTaskIdFromResponse(response);
        } catch (Exception e) {
            log.error("Failed to save DAG task: {}", e.getMessage());
            throw new RuntimeException("Failed to save DAG task: " + e.getMessage());
        }
    }

    /**
     * 调用 MSP 后端执行 DAG 任务
     */
    private void callMspExecuteTask(String taskId) {
        try {
            // 调用 MSP 的 REST API: POST /api/v1/msp/tasks/{taskId}/execute
            String mspUrl = getMspApiUrl("/api/v1/msp/tasks/" + taskId + "/execute");
            httpPost(mspUrl, null);
        } catch (Exception e) {
            log.error("Failed to execute DAG task {}: {}", taskId, e.getMessage());
            throw new RuntimeException("Failed to execute DAG task: " + e.getMessage());
        }
    }

    /**
     * 调用 MSP 后端提交 DAG 任务
     */
    private String callMspSubmitTask(TaskDTO taskDTO) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", taskDTO.getName());
        request.put("type", "COMPONENT_DAG");
        request.put("algorithm", taskDTO.getParameters().get("algorithm"));
        request.put("nodeMode", taskDTO.getNodeMode());

        if (taskDTO.getParticipants() != null) {
            request.put("participants", taskDTO.getParticipants());
        }
        request.put("parameters", taskDTO.getParameters());

        try {
            // 调用 MSP 的 REST API: POST /api/v1/msp/tasks
            String mspUrl = getMspApiUrl("/api/v1/msp/tasks");
            String response = httpPost(mspUrl, request);
            return parseTaskIdFromResponse(response);
        } catch (Exception e) {
            log.error("Failed to submit DAG task: {}", e.getMessage());
            throw new RuntimeException("Failed to submit DAG task: " + e.getMessage());
        }
    }

    /**
     * 获取 MSP API URL
     */
    private String getMspApiUrl(String path) {
        String mspHost = System.getenv("MSP_API_HOST");
        if (mspHost == null || mspHost.isEmpty()) {
            mspHost = "http://localhost:8084";
        }
        return mspHost + path;
    }

    /**
     * 解析响应中的 taskId
     */
    private String parseTaskIdFromResponse(String response) {
        try {
            // 简单解析 JSON 响应中的 taskId
            if (response != null && response.contains("taskId")) {
                int idx = response.indexOf("taskId");
                int start = response.indexOf("\"", idx) + 1;
                int end = response.indexOf("\"", start);
                if (end > start) {
                    return response.substring(start, end);
                }
            }
            // 如果解析失败，生成一个 UUID 作为任务ID
            return java.util.UUID.randomUUID().toString();
        } catch (Exception e) {
            return java.util.UUID.randomUUID().toString();
        }
    }

    /**
     * 发送 HTTP POST 请求
     */
    private String httpPost(String url, Map<String, Object> body) throws Exception {
        java.net.URL urlObj = new java.net.URL(url);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        if (body != null) {
            try (java.io.OutputStream os = conn.getOutputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(os, body);
            }
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            throw new RuntimeException("HTTP error: " + responseCode);
        }

        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    @Override
    public String registerNode(String nodeId, String nodeName, String endpoint, String nodeMode) {
        NodeDTO nodeDTO = new NodeDTO();
        nodeDTO.setNodeId(nodeId);
        nodeDTO.setNodeName(nodeName);
        nodeDTO.setEndpoint(endpoint);
        nodeDTO.setNodeMode(nodeMode != null ? nodeMode : "RAY");
        return nodeService.registerNode(nodeDTO);
    }

    @Override
    public void nodeHeartbeat(String nodeId) {
        nodeService.heartbeat(nodeId);
    }

    @Override
    public Map<String, Object> listNodes(int page, int size, Integer status) {
        com.tds.dos.common.enums.NodeStatus nodeStatus = null;
        if (status != null && status > 0) {
            nodeStatus = com.tds.dos.common.enums.NodeStatus.fromCode(status);
        }
        com.tds.dos.common.core.PageResult<com.tds.dos.dal.msp.entity.TbNode> pageResult =
            nodeService.listNodes(page, size, nodeStatus);

        // 转换为前端期望的格式
        List<Map<String, Object>> list = new ArrayList<>();
        if (pageResult != null && pageResult.getList() != null) {
            for (com.tds.dos.dal.msp.entity.TbNode node : pageResult.getList()) {
                Map<String, Object> item = new HashMap<>();
                item.put("nodeId", node.getfNodeId());
                item.put("nodeName", node.getfNodeName());
                item.put("endpoint", node.getfEndpoint());
                item.put("externalEndpoint", node.getfExternalEndpoint());
                item.put("nodeMode", node.getfNodeMode());
                item.put("status", node.getfStatus() != null && node.getfStatus() == 1 ? "ONLINE" : "OFFLINE");
                item.put("lastHeartbeat", node.getfLastHeartbeat() != null ? node.getfLastHeartbeat().toString() : null);
                item.put("capabilities", node.getfCapabilities());
                item.put("tags", node.getfTags());
                list.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        long total = 0;
        if (pageResult != null && pageResult.getPagination() != null) {
            total = pageResult.getPagination().getTotal();
        }
        result.put("pagination", Map.of(
            "currentPage", page,
            "pageSize", size,
            "total", total
        ));
        return result;
    }

    @Override
    public boolean unregisterNode(String nodeId) {
        return nodeService.unregisterNode(nodeId);
    }

    @Override
    public void updateNodeName(String nodeId, String nodeName) {
        nodeService.updateNodeName(nodeId, nodeName);
    }

    @Override
    public String registerDatasource(String datasourceId, String datasourceType, Map<String, String> connectionInfo) {
        // 数据源注册 - 实际实现取决于MSP平台API
        Map<String, Object> params = new HashMap<>();
        params.put("datasourceId", datasourceId);
        params.put("datasourceType", datasourceType);
        params.put("connectionInfo", connectionInfo);
        return createTask(params);
    }

    @Override
    public Map<String, Object> getDatasource(String datasourceId) {
        // 获取数据源信息 - 返回空实现
        return new HashMap<>();
    }

    /**
     * 获取可用的节点列表（在线状态）
     */
    private List<String> getAvailableNodes() {
        try {
            Map<String, Object> nodeResult = listNodes(1, 100, 1); // status=1 表示在线
            List<Map<String, Object>> nodeList = (List<Map<String, Object>>) nodeResult.get("list");
            if (nodeList == null || nodeList.isEmpty()) {
                return new ArrayList<>();
            }
            List<String> nodeIds = new ArrayList<>();
            for (Map<String, Object> node : nodeList) {
                nodeIds.add((String) node.get("nodeId"));
            }
            return nodeIds;
        } catch (Exception e) {
            log.error("Failed to get available nodes: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 等待任务完成并返回结果
     */
    private Map<String, Object> waitForTaskResult(String taskId, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            Integer status = queryTaskStatus(taskId);
            if (status == null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            if (status == STATUS_COMPLETED) {
                Map<String, Object> result = new HashMap<>();
                result.put("taskId", taskId);
                result.put("status", "COMPLETED");
                result.put("result", getTaskResult(taskId));
                return result;
            } else if (status == STATUS_FAILED) {
                Map<String, Object> result = new HashMap<>();
                result.put("taskId", taskId);
                result.put("status", "FAILED");
                result.put("error", "Task execution failed");
                return result;
            } else if (status == STATUS_CANCELLED) {
                Map<String, Object> result = new HashMap<>();
                result.put("taskId", taskId);
                result.put("status", "CANCELLED");
                return result;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("status", "TIMEOUT");
        result.put("error", "Task execution timeout");
        return result;
    }
}
