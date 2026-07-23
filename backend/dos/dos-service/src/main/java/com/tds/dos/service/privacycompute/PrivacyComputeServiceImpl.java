package com.tds.dos.service.privacycompute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.dos.msp.common.core.ApiResponse;
import com.tds.dos.msp.common.enums.TaskStatus;
import com.tds.dos.msp.common.enums.TaskType;
import com.tds.dos.msp.service.node.INodeService;
import com.tds.dos.msp.service.node.NodeDTO;
import com.tds.dos.msp.service.task.ITaskService;
import com.tds.dos.msp.service.task.TaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void cancelTask(String taskId) {
        taskService.cancelTask(taskId);
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

        return createTask(taskParams);
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
        com.tds.dos.msp.common.enums.NodeStatus nodeStatus = null;
        if (status != null && status > 0) {
            nodeStatus = com.tds.dos.msp.common.enums.NodeStatus.fromCode(status);
        }
        com.tds.dos.msp.common.core.PageResult<com.tds.dos.msp.dal.entity.TbMspNode> pageResult =
            nodeService.listNodes(page, size, nodeStatus);

        // 转换为前端期望的格式
        List<Map<String, Object>> list = new ArrayList<>();
        if (pageResult != null && pageResult.getList() != null) {
            for (com.tds.dos.msp.dal.entity.TbMspNode node : pageResult.getList()) {
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
