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
import com.tds.dos.service.ray.IAgentClient;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ITaskService taskService;

    @Autowired
    private INodeService nodeService;

    @Autowired
    private TbTaskMapper taskMapper;

    @Autowired
    private IAgentClient agentClient;

    @Autowired
    private com.tds.dos.service.msp.executor.PsiTaskExecutor psiTaskExecutor;

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
        // 任务名称：优先取 taskName，其次取 name
        String taskName = (String) params.getOrDefault("taskName", params.get("name"));
        taskDTO.setName(taskName != null ? taskName : "Task-" + System.currentTimeMillis());
        taskDTO.setDescription((String) params.get("description"));
        taskDTO.setNodeMode((String) params.getOrDefault("nodeMode", "RAY"));

        // 设置任务类型：优先取 computeType，默认 PSI
        String computeType = (String) params.getOrDefault("computeType", "PSI");
        TaskType taskType = TaskType.PSI;
        if ("PSI".equalsIgnoreCase(computeType)) {
            taskType = TaskType.PSI;
        } else if ("MPC".equalsIgnoreCase(computeType)) {
            taskType = TaskType.MPC;
        } else if ("FEDERATED_LEARNING".equalsIgnoreCase(computeType)) {
            taskType = TaskType.HORIZONTAL_FL;
        } else if ("VERTICAL_FL".equalsIgnoreCase(computeType)) {
            taskType = TaskType.VERTICAL_FL;
        } else if ("PIR".equalsIgnoreCase(computeType)) {
            taskType = TaskType.PIR;
        }
        taskDTO.setType(taskType);

        // 设置参与方：如果传了 partyANodeId 和 partyBNodeId，构造 participants
        String partyANodeId = (String) params.get("partyANodeId");
        String partyBNodeId = (String) params.get("partyBNodeId");
        if (partyANodeId != null && partyBNodeId != null) {
            List<String> participants = new ArrayList<>();
            participants.add(partyANodeId);
            participants.add(partyBNodeId);
            taskDTO.setParticipants(participants);
        } else {
            Object participants = params.get("participants");
            if (participants instanceof List) {
                taskDTO.setParticipants((List<String>) participants);
            }
        }

        // 设置参数：将所有非空参数保存到 taskParameters
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
        // PSI 任务额外：先发取消标志 + 调两方 stopJob，再让 TaskService 标 CANCELLED。
        // 非 PSI 任务走原流程。
        TbTask task = taskService.getTask(taskId);
        if (task != null && task.getfType() != null && task.getfType() == 1
            && psiTaskExecutor != null) {
            psiTaskExecutor.cancel(taskId);
        }
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
    public byte[] downloadPsiResultFile(String taskId, String party) {
        if (!"alice".equalsIgnoreCase(party) && !"bob".equalsIgnoreCase(party)) {
            throw new IllegalArgumentException("party 只能是 alice 或 bob，当前值: " + party);
        }
        String normalized = party.toLowerCase();

        TbTask task = taskService.getTask(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        if (task.getfResult() == null || task.getfResult().isEmpty()) {
            throw new RuntimeException("任务尚未产生结果，请等待完成后再下载");
        }

        String jobId;
        String outputPath;
        try {
            Map<String, Object> result = objectMapper.readValue(task.getfResult(), Map.class);
            Map<String, Object> jobs = (Map<String, Object>) result.get("jobs");
            Map<String, Object> outputPaths = (Map<String, Object>) result.get("output_path");
            if (jobs == null || outputPaths == null) {
                throw new RuntimeException("任务结果不含 jobs/output_path，可能不是 PSI 任务");
            }
            Map<String, Object> jobInfo = (Map<String, Object>) jobs.get(normalized);
            if (jobInfo == null) {
                throw new RuntimeException("任务结果中没有 party=" + normalized + " 的信息");
            }
            Object statusObj = jobInfo.get("status");
            if (!"SUCCEEDED".equals(statusObj)) {
                throw new RuntimeException("party=" + normalized + " 任务未成功，当前状态: " + statusObj);
            }
            jobId = String.valueOf(jobInfo.get("job_id"));
            outputPath = String.valueOf(outputPaths.get(normalized));
            if (outputPath == null || outputPath.isEmpty() || "null".equals(outputPath)) {
                throw new RuntimeException("任务结果中缺少 party=" + normalized + " 的 output_path");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("解析任务结果失败: " + e.getMessage(), e);
        }

        // 找到该方对应的节点
        String partyNodeId;
        if ("alice".equals(normalized)) {
            partyNodeId = task.getfParticipants() != null && task.getfParticipants().contains(",")
                ? task.getfParticipants().split(",")[0].trim()
                : task.getfParticipants();
        } else {
            String[] parts = task.getfParticipants().split(",");
            partyNodeId = parts.length > 1 ? parts[1].trim() : parts[0].trim();
        }
        TbNode node = nodeService.getNode(partyNodeId);
        if (node == null || node.getfEndpoint() == null) {
            throw new RuntimeException("节点未找到或未注册端点: " + partyNodeId);
        }

        // 用旁路端点下载，不依赖 Ray 集群和 jobId 鉴权
        // outputPath 是 Agent 本地文件系统路径，Ray 集群停止后仍可访问
        return agentClient.downloadNodeFile(node.getfEndpoint(), outputPath);
    }

    @Override
    public byte[] downloadModelFile(String taskId, String party) {
        if (!"alice".equalsIgnoreCase(party) && !"bob".equalsIgnoreCase(party)) {
            throw new IllegalArgumentException("party 只能是 alice 或 bob，当前值: " + party);
        }
        String normalized = party.toLowerCase();

        TbTask task = taskService.getTask(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        if (task.getfResult() == null || task.getfResult().isEmpty()) {
            throw new RuntimeException("任务尚未产生结果，请等待完成后再下载");
        }

        String modelPath;
        String jobId = null;
        try {
            Map<String, Object> result = objectMapper.readValue(task.getfResult(), Map.class);
            Map<String, Object> partyInfo = (Map<String, Object>) result.get("party_" + normalized);
            if (partyInfo == null) {
                throw new RuntimeException("任务结果中没有 party=" + normalized + " 的信息");
            }
            Object statusObj = partyInfo.get("status");
            if (!"SUCCEEDED".equals(statusObj)) {
                throw new RuntimeException("party=" + normalized + " 任务未成功，当前状态: " + statusObj);
            }
            Object pathObj = partyInfo.get("modelPath");
            if (pathObj == null || "null".equals(String.valueOf(pathObj))) {
                throw new RuntimeException("party=" + normalized + " 没有模型文件路径");
            }
            modelPath = String.valueOf(pathObj);

            // 从 jobs 字段取 jobId
            Map<String, Object> jobs = (Map<String, Object>) result.get("jobs");
            if (jobs != null) {
                Map<String, Object> jobInfo = (Map<String, Object>) jobs.get(normalized);
                if (jobInfo != null) {
                    jobId = (String) jobInfo.get("job_id");
                }
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("解析任务结果失败: " + e.getMessage(), e);
        }

        // 找到该方对应的节点
        String partyNodeId;
        if ("alice".equals(normalized)) {
            partyNodeId = task.getfParticipants() != null && task.getfParticipants().contains(",")
                ? task.getfParticipants().split(",")[0].trim()
                : task.getfParticipants();
        } else {
            String[] parts = task.getfParticipants().split(",");
            partyNodeId = parts.length > 1 ? parts[1].trim() : parts[0].trim();
        }
        TbNode node = nodeService.getNode(partyNodeId);
        if (node == null || node.getfEndpoint() == null) {
            throw new RuntimeException("节点未找到或未注册端点: " + partyNodeId);
        }

        // 用旁路端点下载，不依赖 Ray 集群和 jobId 鉴权
        // modelPath 是 Agent 本地文件系统路径，Ray 集群停止后仍可访问
        return agentClient.downloadNodeFile(node.getfEndpoint(), modelPath);
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
        if (pageResult != null && pageResult.getPagination() != null && pageResult.getPagination().getTotal() > 0) {
            total = pageResult.getPagination().getTotal();
        } else {
            total = list.size();
        }
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("pageSize", size);
        pagination.put("total", total);
        result.put("pagination", pagination);
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
        taskParams.put("protocol", params.getOrDefault("protocol", "ECPSI"));
        taskParams.put("resultType", params.getOrDefault("resultType", "INTERSECTION"));

        // 两方节点必须明确，Executor 不再自动挑选，否则角色与数据路径会错配
        String partyANodeId = params.get("partyANodeId") != null ? String.valueOf(params.get("partyANodeId")) : null;
        String partyBNodeId = params.get("partyBNodeId") != null ? String.valueOf(params.get("partyBNodeId")) : null;
        if (partyANodeId == null || partyBNodeId == null) {
            List<String> availableNodes = getAvailableNodes();
            if (availableNodes == null || availableNodes.size() < 2) {
                throw new RuntimeException("PSI任务至少需要2个节点，且必须显式指定 partyANodeId / partyBNodeId");
            }
            partyANodeId = availableNodes.get(0);
            partyBNodeId = availableNodes.get(1);
            log.info("未指定PSI参与方节点，回退到在线节点: partyA={}, partyB={}", partyANodeId, partyBNodeId);
        }
        taskParams.put("partyANodeId", partyANodeId);
        taskParams.put("partyBNodeId", partyBNodeId);

        // 不再自行建集群/生成代码/双边提交/等待：交给 PsiTaskExecutor 统一处理
        String taskId = createTask(taskParams);
        log.info("PSI task {} created, 调度到 PsiTaskExecutor 统一执行", taskId);
        return taskId;
    }

    @Override
    public Map<String, Object> executePsiTaskWithResult(String taskName, String partyADataPath,
                                                          String partyBDataPath, String keyColumn,
                                                          Map<String, Object> params,
                                                          int timeoutSeconds) {
        Map<String, Object> effectiveParams = params != null ? params : new HashMap<>();
        String taskId = executePsiTask(taskName, partyADataPath, partyBDataPath, keyColumn, effectiveParams);
        executeTask(taskId);
        return waitForTaskResult(taskId, timeoutSeconds);

    }
    @Override
    public String createPirTask(Map<String, Object> params) {
        // PIR 任务参数
        String taskName = (String) params.getOrDefault("taskName", "PIR-" + System.currentTimeMillis());

        // 创建 PIR 任务（只创建，不执行）
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("computeType", "PIR");
        taskParams.put("name", taskName);
        taskParams.put("serverNodeId", params.get("serverNodeId"));
        taskParams.put("clientNodeId", params.get("clientNodeId"));
        taskParams.put("inputPath", params.get("inputPath"));
        taskParams.put("keyColumn", params.get("keyColumn"));
        taskParams.put("labelColumns", params.get("labelColumns"));
        taskParams.put("queryValue", params.get("queryValue"));
        taskParams.put("pirType", params.getOrDefault("pirType", "SealPIR"));
        taskParams.put("nodeMode", params.getOrDefault("nodeMode", "RAY"));

        String taskId = createTask(taskParams);
        log.info("PIR task {} created (待执行), pirType={}", taskId, params.get("pirType"));
        return taskId;
    }

    @Override
    public Map<String, Object> executePirTaskWithResult(Map<String, Object> params) {
        // 创建 + 执行 + 等待结果（向后兼容）
        String taskId = createPirTask(params);
        executeTask(taskId);
        return waitForTaskResult(taskId, 300);
    }

    @Override
    public String createMpcTask(String taskName, List<String> participants,
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
    public String createFederatedLearningTask(String taskName, List<String> participants,
                                               String labelColumn, List<String> featureColumns,
                                               Map<String, Object> params) {
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("computeType", "FEDERATED_LEARNING");
        taskParams.put("name", taskName);
        taskParams.put("participants", participants);
        taskParams.put("labelColumn", labelColumn);
        taskParams.put("featureColumns", featureColumns != null ? String.join(",", featureColumns) : null);
        taskParams.put("nodeMode", params.getOrDefault("nodeMode", "RAY"));

        // 节点和数据路径参数
        if (params.get("partyANodeId") != null) {
            taskParams.put("partyANodeId", String.valueOf(params.get("partyANodeId")));
        } else if (participants != null && participants.size() >= 1) {
            taskParams.put("partyANodeId", participants.get(0));
        }
        if (params.get("partyBNodeId") != null) {
            taskParams.put("partyBNodeId", String.valueOf(params.get("partyBNodeId")));
        } else if (participants != null && participants.size() >= 2) {
            taskParams.put("partyBNodeId", participants.get(1));
        }
        if (params.get("partyADataPath") != null) {
            taskParams.put("partyADataPath", String.valueOf(params.get("partyADataPath")));
        }
        if (params.get("partyBDataPath") != null) {
            taskParams.put("partyBDataPath", String.valueOf(params.get("partyBDataPath")));
        }

        // 横向联邦特定参数
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
    public String createVerticalFlTask(String taskName, List<String> participants,
                                        String labelColumn, Map<String, List<String>> featureColumns,
                                        Map<String, Object> params) {
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("computeType", "VERTICAL_FL");
        taskParams.put("name", taskName);
        taskParams.put("participants", participants);
        taskParams.put("labelColumn", labelColumn);
        taskParams.put("nodeMode", params.getOrDefault("nodeMode", "RAY"));

        // VFL 特定参数：节点ID、数据路径、ID列、标签持有方
        if (params.get("partyANodeId") != null) {
            taskParams.put("partyANodeId", String.valueOf(params.get("partyANodeId")));
        } else if (participants != null && participants.size() >= 1) {
            taskParams.put("partyANodeId", participants.get(0));
        }
        if (params.get("partyBNodeId") != null) {
            taskParams.put("partyBNodeId", String.valueOf(params.get("partyBNodeId")));
        } else if (participants != null && participants.size() >= 2) {
            taskParams.put("partyBNodeId", participants.get(1));
        }
        if (params.get("partyADataPath") != null) {
            taskParams.put("partyADataPath", String.valueOf(params.get("partyADataPath")));
        }
        if (params.get("partyBDataPath") != null) {
            taskParams.put("partyBDataPath", String.valueOf(params.get("partyBDataPath")));
        }
        if (params.get("idColumn") != null) {
            taskParams.put("idColumn", String.valueOf(params.get("idColumn")));
        }
        if (params.get("labelOwner") != null) {
            taskParams.put("labelOwner", String.valueOf(params.get("labelOwner")));
        } else {
            // 默认标签持有方为第一个参与方
            taskParams.put("labelOwner", participants != null && !participants.isEmpty() ? participants.get(0) : "alice");
        }

        // featureColumns 是 Map<String, List<String>>，需要序列化
        if (featureColumns != null) {
            taskParams.put("partyAFeatureColumns", String.join(",", featureColumns.getOrDefault("alice", List.of())));
            taskParams.put("partyBFeatureColumns", String.join(",", featureColumns.getOrDefault("bob", List.of())));
        }

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
        if (pageResult != null && pageResult.getPagination() != null && pageResult.getPagination().getTotal() > 0) {
            total = pageResult.getPagination().getTotal();
        } else {
            total = list.size();
        }
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("pageSize", size);
        pagination.put("total", total);
        result.put("pagination", pagination);
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
