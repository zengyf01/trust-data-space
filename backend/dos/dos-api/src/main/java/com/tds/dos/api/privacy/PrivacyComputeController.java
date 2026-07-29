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
 * 提供 PSI、MPC、联邦学习等隐私计算任务的REST API
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
        return ApiResponse.success(Map.of("taskId", taskId, "result", result));
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

    // ==================== PSI 求交 ====================

    /**
     * 执行 PSI 求交任务
     * @param taskName 任务名称
     * @param partyADataPath A方数据路径
     * @param partyBDataPath B方数据路径
     * @param keyColumn 关联键列
     * @param protocol 协议类型 (ECPSI/RR22PSI)
     * @param resultType 结果类型 (INTERSECTION/UNION/...)
     */
    @PostMapping("/psi/execute")
    public ApiResponse<?> executePsi(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        String partyADataPath = (String) params.get("partyADataPath");
        String partyBDataPath = (String) params.get("partyBDataPath");
        String keyColumn = (String) params.get("keyColumn");
        String protocol = (String) params.getOrDefault("protocol", "ECPSI");
        String resultType = (String) params.getOrDefault("resultType", "INTERSECTION");

        Map<String, Object> taskParams = Map.of(
            "protocol", protocol,
            "resultType", resultType,
            "nodeMode", params.getOrDefault("nodeMode", "RAY")
        );

        String taskId = privacyComputeService.executePsiTask(
            taskName, partyADataPath, partyBDataPath, keyColumn, taskParams);

        return ApiResponse.success(Map.of("taskId", taskId, "computeType", "PSI"));
    }

    /**
     * 执行 PSI 求交并等待结果
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
            taskName, partyADataPath, partyBDataPath, keyColumn, timeoutSeconds);

        return ApiResponse.success(result);
    }

    // ==================== MPC 多方计算 ====================

    /**
     * 执行 MPC 任务
     */
    @PostMapping("/mpc/execute")
    public ApiResponse<?> executeMpc(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        List<String> participants = (List<String>) params.get("participants");
        String algorithm = (String) params.get("algorithm");

        String taskId = privacyComputeService.executeMpcTask(taskName, participants, algorithm, params);

        return ApiResponse.success(Map.of("taskId", taskId, "computeType", "MPC"));
    }

    // ==================== 联邦学习 ====================

    /**
     * 执行联邦学习任务
     */
    @PostMapping("/fl/execute")
    public ApiResponse<?> executeFederatedLearning(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        List<String> participants = (List<String>) params.get("participants");
        String labelColumn = (String) params.get("labelColumn");
        List<String> featureColumns = (List<String>) params.get("featureColumns");

        String taskId = privacyComputeService.executeFederatedLearningTask(
            taskName, participants, labelColumn, featureColumns, params);

        return ApiResponse.success(Map.of("taskId", taskId, "computeType", "FEDERATED_LEARNING"));
    }

    /**
     * 执行纵向联邦学习任务
     */
    @PostMapping("/vfl/execute")
    public ApiResponse<?> executeVerticalFl(@RequestBody Map<String, Object> params) {
        String taskName = (String) params.get("taskName");
        List<String> participants = (List<String>) params.get("participants");
        String labelColumn = (String) params.get("labelColumn");
        Map<String, List<String>> featureColumns = (Map<String, List<String>>) params.get("featureColumns");

        String taskId = privacyComputeService.executeVerticalFlTask(
            taskName, participants, labelColumn, featureColumns, params);

        return ApiResponse.success(Map.of("taskId", taskId, "computeType", "VERTICAL_FL"));
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