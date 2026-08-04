package com.tds.dos.api.privacy;

import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.service.privacycompute.IPrivacyComputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 隐私计算管理接口
 * 提供 PSI、MPC、横向联邦等隐私计算任务的REST API
 */
@RestController
@RequestMapping("/privacy")
public class PrivacyComputeController {

    @Autowired
    private IPrivacyComputeService privacyComputeService;

    // ==================== 任务管理 ====================

    /**
     * 创建隐私计算任务
     */
    @PostMapping("/task/create")
    public ApiResponse<?> createTask(@RequestBody Map<String, Object> params) {
        String taskId = privacyComputeService.createTask(params);
        return ApiResponse.success(Map.of("taskId", taskId));
    }

    /**
     * 执行任务
     */
    @PostMapping("/task/{taskId}/execute")
    public ApiResponse<?> executeTask(@PathVariable String taskId) {
        privacyComputeService.executeTask(taskId);
        return ApiResponse.success(Map.of("taskId", taskId, "status", "EXECUTING"));
    }

    /**
     * 查询任务状态
     */
    @GetMapping("/task/{taskId}/status")
    public ApiResponse<?> getTaskStatus(@PathVariable String taskId) {
        Integer status = privacyComputeService.queryTaskStatus(taskId);
        String statusDesc = getStatusDescription(status);
        return ApiResponse.success(Map.of("taskId", taskId, "status", status, "description", statusDesc));
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/task/{taskId}/detail")
    public ApiResponse<?> getTaskDetail(@PathVariable String taskId) {
        com.tds.dos.dal.msp.entity.TbTask task = privacyComputeService.getTaskById(taskId);
        if (task == null) {
            return ApiResponse.error("任务不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getfId());
        data.put("taskCode", task.getfTaskCode());
        data.put("name", task.getfName());
        data.put("type", task.getfType());
        data.put("status", task.getfStatus());
        data.put("algorithm", task.getfAlgorithm());
        data.put("participants", task.getfParticipants());
        data.put("nodeMode", task.getfNodeMode());
        data.put("description", task.getfDescription());
        data.put("parameters", task.getfParameters());
        data.put("code", task.getfCode());
        data.put("result", task.getfResult());
        data.put("executionLog", task.getfExecutionLog());
        data.put("creator", task.getfCreator());
        data.put("createTime", task.getfCreateTime() != null ? task.getfCreateTime().toString() : null);
        data.put("updateTime", task.getfUpdateTime() != null ? task.getfUpdateTime().toString() : null);
        return ApiResponse.success(data);
    }

    /**
     * 获取任务结果
     */
    @GetMapping("/task/{taskId}/result")
    public ApiResponse<?> getTaskResult(@PathVariable String taskId) {
        String result = privacyComputeService.getTaskResult(taskId);
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId);
        data.put("result", result);  // 任务未完成时 result=null
        return ApiResponse.success(data);
    }

    /**
     * 获取任务生成的代码（Python代码）
     */
    @GetMapping("/task/{taskId}/code")
    public ApiResponse<?> getTaskCode(@PathVariable String taskId) {
        String code = privacyComputeService.getTaskCode(taskId);
        return ApiResponse.success(Map.of("taskId", taskId, "code", code));
    }

    /**
     * 取消任务
     */
    @PostMapping("/task/{taskId}/cancel")
    public ApiResponse<?> cancelTask(@PathVariable String taskId) {
        privacyComputeService.cancelTask(taskId);
        return ApiResponse.success(Map.of("taskId", taskId, "status", "CANCELLED"));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/task/{taskId}")
    public ApiResponse<?> deleteTask(@PathVariable String taskId) {
        privacyComputeService.deleteTask(taskId);
        return ApiResponse.success(Map.of("taskId", taskId, "result", "删除成功"));
    }

    /**
     * 获取任务列表
     */
    @GetMapping("/task/list")
    public ApiResponse<Map<String, Object>> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = privacyComputeService.listTasks(page, size);
        return ApiResponse.success(result);
    }

    /**
     * 下载 PSI 任务一方的结果 CSV
     */
    @GetMapping("/psi/{taskId}/result")
    public org.springframework.http.ResponseEntity<byte[]> downloadPsiResult(
            @PathVariable String taskId,
            @RequestParam(defaultValue = "alice") String party) {
        byte[] data = privacyComputeService.downloadPsiResultFile(taskId, party);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "psi_result_" + taskId + "_" + party + ".csv");
        return new org.springframework.http.ResponseEntity<>(data, headers,
            org.springframework.http.HttpStatus.OK);
    }

    /**
     * 下载 FL/VFL 任务一方的模型文件
     */
    @GetMapping("/model/{taskId}/download")
    public org.springframework.http.ResponseEntity<byte[]> downloadModel(
            @PathVariable String taskId,
            @RequestParam(defaultValue = "alice") String party) {
        byte[] data = privacyComputeService.downloadModelFile(taskId, party);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/octet-stream"));
        headers.setContentDispositionFormData("attachment", "model_" + taskId + "_" + party + ".pkl");
        return new org.springframework.http.ResponseEntity<>(data, headers,
            org.springframework.http.HttpStatus.OK);
    }

    // ==================== PSI 求交 ====================

    /**
     * 创建 PSI 求交任务（只创建，不执行）
     * @param taskName 任务名称
     * @param partyANodeId A方节点ID
     * @param partyBNodeId B方节点ID
     * @param partyADataPath A方数据路径
     * @param partyBDataPath B方数据路径
     * @param keyColumn 关联键列
     * @param protocol 协议类型 (ECPSI/RR22PSI)
     * @param resultType 结果类型 (INTERSECTION/UNION/...)
     * @param nodeMode 节点模式 (RAY/KUSCIA)
     * @return 任务ID
     */
    @PostMapping("/psi/create")
    public ApiResponse<?> createPsiTask(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        String partyANodeId = (String) params.get("partyANodeId");
        String partyBNodeId = (String) params.get("partyBNodeId");
        String partyADataPath = (String) params.get("partyADataPath");
        String partyBDataPath = (String) params.get("partyBDataPath");
        String keyColumn = (String) params.get("keyColumn");
        String protocol = (String) params.getOrDefault("protocol", "ECPSI");
        String resultType = (String) params.getOrDefault("resultType", "INTERSECTION");
        String nodeMode = (String) params.getOrDefault("nodeMode", "RAY");

        // 构建任务参数
        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("taskName", taskName);
        taskParams.put("protocol", protocol);
        taskParams.put("resultType", resultType);
        taskParams.put("nodeMode", nodeMode);
        taskParams.put("partyANodeId", partyANodeId);
        taskParams.put("partyBNodeId", partyBNodeId);
        taskParams.put("partyADataPath", partyADataPath);
        taskParams.put("partyBDataPath", partyBDataPath);
        taskParams.put("keyColumn", keyColumn);

        // 调用通用的任务创建接口
        String taskId = privacyComputeService.createTask(taskParams);

        // 返回任务ID，状态为CREATED
        return ApiResponse.success(Map.of(
            "taskId", taskId,
            "computeType", "PSI",
            "status", "CREATED"
        ));
    }

    /**
     * 执行已创建的 PSI 任务
     * @param taskId 任务ID
     */
    @PostMapping("/psi/{taskId}/execute")
    public ApiResponse<?> executePsiTask(@PathVariable String taskId) {
        // 查询任务是否存在
        com.tds.dos.dal.msp.entity.TbTask task = privacyComputeService.getTaskById(taskId);
        if (task == null) {
            return ApiResponse.error("任务不存在");
        }
        if (task.getfStatus() != 1) { // CREATED = 1
            return ApiResponse.error("任务状态不是已创建，无法执行。当前状态: " + task.getfStatus());
        }

        // 执行任务
        privacyComputeService.executeTask(taskId);

        return ApiResponse.success(Map.of("taskId", taskId, "status", "EXECUTING"));
    }

    /**
     * 执行 PSI 求交并等待结果（一步完成，创建+执行+等待）
     */
    @PostMapping("/psi/executeWithResult")
    public ApiResponse<?> executePsiWithResult(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        String partyADataPath = (String) params.get("partyADataPath");
        String partyBDataPath = (String) params.get("partyBDataPath");
        String keyColumn = (String) params.get("keyColumn");
        int timeoutSeconds = params.get("timeoutSeconds") != null ?
            Integer.parseInt(params.get("timeoutSeconds").toString()) : 300;

        Map<String, Object> result = privacyComputeService.executePsiTaskWithResult(
            taskName, partyADataPath, partyBDataPath, keyColumn, params, timeoutSeconds);

        return ApiResponse.success(result);
    }

    // ==================== PIR 隐匿查询 ====================

    /**
     * 创建 PIR 任务（只创建，不执行）
     */
    @PostMapping("/pir/create")
    public ApiResponse<?> createPir(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        String serverNodeId = (String) params.get("serverNodeId");
        String clientNodeId = (String) params.get("clientNodeId");
        String inputPath = (String) params.get("inputPath");
        String keyColumn = (String) params.get("keyColumn");
        String labelColumns = (String) params.get("labelColumns");
        String queryValue = (String) params.get("queryValue");
        String pirType = (String) params.getOrDefault("pirType", "SealPIR");

        if (serverNodeId == null || clientNodeId == null) {
            return ApiResponse.error("serverNodeId 和 clientNodeId 不能为空");
        }
        if (inputPath == null || keyColumn == null) {
            return ApiResponse.error("inputPath、keyColumn 不能为空");
        }
        if (queryValue == null) {
            return ApiResponse.error("queryValue 不能为空");
        }

        Map<String, Object> taskParams = new java.util.HashMap<>();
        taskParams.put("taskName", taskName);
        taskParams.put("serverNodeId", serverNodeId);
        taskParams.put("clientNodeId", clientNodeId);
        taskParams.put("inputPath", inputPath);
        taskParams.put("keyColumn", keyColumn);
        taskParams.put("labelColumns", labelColumns);
        taskParams.put("queryValue", queryValue);
        taskParams.put("pirType", pirType);
        taskParams.put("nodeMode", params.getOrDefault("nodeMode", "RAY"));

        String taskId = privacyComputeService.createPirTask(taskParams);

        return ApiResponse.success(Map.of(
            "taskId", taskId,
            "computeType", "PIR",
            "pirType", pirType,
            "status", "CREATED"
        ));
    }

    // ==================== MPC 多方计算 ====================

    /**
     * 创建 MPC 任务（只创建，不执行）
     */
    @PostMapping("/mpc/create")
    public ApiResponse<?> createMpc(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        List<String> participants = (List<String>) params.get("participants");
        String algorithm = (String) params.get("algorithm");

        // 把 taskName 放进 params 让 service 透传
        if (taskName != null) params.put("name", taskName);
        String taskId = privacyComputeService.createMpcTask(taskName, participants, algorithm, params);

        return ApiResponse.success(Map.of(
            "taskId", taskId,
            "computeType", "MPC",
            "status", "CREATED"
        ));
    }

    // ==================== 横向联邦 ====================

    /**
     * 创建横向联邦任务（只创建，不执行）
     */
    @PostMapping("/fl/create")
    public ApiResponse<?> createFederatedLearning(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        List<String> participants = (List<String>) params.get("participants");
        String labelColumn = (String) params.get("labelColumn");
        List<String> featureColumns = (List<String>) params.get("featureColumns");

        if (taskName != null) params.put("name", taskName);
        String taskId = privacyComputeService.createFederatedLearningTask(
            taskName, participants, labelColumn, featureColumns, params);

        return ApiResponse.success(Map.of(
            "taskId", taskId,
            "computeType", "FL",
            "status", "CREATED"
        ));
    }

    /**
     * 创建纵向联邦学习任务（只创建，不执行）
     */
    @PostMapping("/vfl/create")
    public ApiResponse<?> createVerticalFl(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        List<String> participants = (List<String>) params.get("participants");
        String labelColumn = (String) params.get("labelColumn");
        Map<String, List<String>> featureColumns = (Map<String, List<String>>) params.get("featureColumns");

        if (taskName != null) params.put("name", taskName);
        String taskId = privacyComputeService.createVerticalFlTask(
            taskName, participants, labelColumn, featureColumns, params);

        return ApiResponse.success(Map.of(
            "taskId", taskId,
            "computeType", "VERTICAL_FL",
            "status", "CREATED"
        ));
    }

    // ==================== DAG 任务 ====================

    /**
     * 创建 DAG 任务（只保存，不执行）
     * @param dagName DAG名称
     * @param dagDefinition DAG定义JSON，包含nodes和edges
     * @param participants 参与方列表
     * @param params 其他参数
     * @return 任务ID
     */
    @PostMapping("/dag/create")
    public ApiResponse<?> createDagTask(@RequestBody Map<String, Object> params) {
        String dagName = (String) params.get("dagName");
        String dagDefinition = params.get("dagDefinition") != null ? params.get("dagDefinition").toString() : null;
        List<String> participants = (List<String>) params.get("participants");

        String taskId = privacyComputeService.createDagTask(dagName, dagDefinition, participants, params);

        return ApiResponse.success(Map.of("taskId", taskId, "type", "COMPONENT_DAG", "status", "CREATED"));
    }

    /**
     * 执行已保存的 DAG 任务
     * @param taskId 任务ID
     */
    @PostMapping("/dag/{taskId}/execute")
    public ApiResponse<?> executeDagTask(@PathVariable String taskId) {
        privacyComputeService.executeDagTask(taskId);
        return ApiResponse.success(Map.of("taskId", taskId, "status", "EXECUTING"));
    }

    /**
     * 提交并执行 DAG 任务（保存+执行）
     * @param dagName DAG名称
     * @param dagDefinition DAG定义JSON
     * @param participants 参与方列表
     * @param params 其他参数
     * @return 任务ID
     */
    @PostMapping("/dag/submit")
    public ApiResponse<?> submitDagTask(@RequestBody Map<String, Object> params) {
        String dagName = (String) params.get("dagName");
        String dagDefinition = params.get("dagDefinition") != null ? params.get("dagDefinition").toString() : null;
        List<String> participants = (List<String>) params.get("participants");

        String taskId = privacyComputeService.submitDagTask(dagName, dagDefinition, participants, params);

        return ApiResponse.success(Map.of("taskId", taskId, "type", "COMPONENT_DAG", "status", "PENDING"));
    }

    // ==================== 节点管理 ====================

    /**
     * 注册计算节点
     */
    @PostMapping("/node/register")
    public ApiResponse<?> registerNode(@RequestBody Map<String, Object> params) {
        String nodeId = (String) params.get("nodeId");
        String nodeName = (String) params.get("nodeName");
        String endpoint = (String) params.get("endpoint");
        String nodeMode = (String) params.getOrDefault("nodeMode", "RAY");

        String result = privacyComputeService.registerNode(nodeId, nodeName, endpoint, nodeMode);
        return ApiResponse.success(Map.of("nodeId", nodeId, "result", result));
    }

    /**
     * 节点心跳
     */
    @PostMapping("/node/{nodeId}/heartbeat")
    public ApiResponse<?> nodeHeartbeat(@PathVariable String nodeId) {
        privacyComputeService.nodeHeartbeat(nodeId);
        return ApiResponse.success(Map.of("nodeId", nodeId, "status", "OK"));
    }

    /**
     * 获取节点列表
     */
    @GetMapping("/node/list")
    public ApiResponse<Map<String, Object>> listNodes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Map<String, Object> result = privacyComputeService.listNodes(page, size, status);
        return ApiResponse.success(result);
    }

    /**
     * 注销节点
     */
    @DeleteMapping("/node/{nodeId}")
    public ApiResponse<?> unregisterNode(@PathVariable String nodeId) {
        boolean success = privacyComputeService.unregisterNode(nodeId);
        if (success) {
            return ApiResponse.success(Map.of("nodeId", nodeId, "result", "注销成功"));
        }
        return ApiResponse.error("注销节点失败");
    }

    /**
     * 更新节点名称
     */
    @PutMapping("/node/{nodeId}/name")
    public ApiResponse<?> updateNodeName(@PathVariable String nodeId, @RequestBody Map<String, String> params) {
        String nodeName = params.get("nodeName");
        if (nodeName == null || nodeName.trim().isEmpty()) {
            return ApiResponse.error("节点名称不能为空");
        }
        privacyComputeService.updateNodeName(nodeId, nodeName.trim());
        return ApiResponse.success(Map.of("nodeId", nodeId, "nodeName", nodeName.trim()));
    }

    // ==================== 数据源管理 ====================

    /**
     * 注册数据源
     */
    @PostMapping("/datasource/register")
    public ApiResponse<?> registerDatasource(@RequestBody Map<String, Object> params) {
        String datasourceId = (String) params.get("datasourceId");
        String datasourceType = (String) params.get("datasourceType");
        Map<String, String> connectionInfo = (Map<String, String>) params.get("connectionInfo");

        String result = privacyComputeService.registerDatasource(datasourceId, datasourceType, connectionInfo);
        return ApiResponse.success(Map.of("datasourceId", datasourceId, "result", result));
    }

    /**
     * 获取数据源信息
     */
    @GetMapping("/datasource/{datasourceId}")
    public ApiResponse<?> getDatasource(@PathVariable String datasourceId) {
        Map<String, Object> datasource = privacyComputeService.getDatasource(datasourceId);
        return ApiResponse.success(datasource);
    }

    // ==================== 辅助方法 ====================

    private String getStatusDescription(Integer status) {
        if (status == null) return "UNKNOWN";
        switch (status) {
            case 1: return "CREATED";
            case 2: return "PENDING";
            case 3: return "RUNNING";
            case 4: return "COMPLETED";
            case 5: return "FAILED";
            case 6: return "CANCELLED";
            default: return "UNKNOWN";
        }
    }
}