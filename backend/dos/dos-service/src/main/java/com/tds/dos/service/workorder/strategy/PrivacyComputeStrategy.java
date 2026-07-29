package com.tds.dos.service.workorder.strategy;

import com.tds.dos.common.enums.WorkOrderStatus;
import com.tds.dos.dal.entity.TbWorkOrder;
import com.tds.dos.dal.mapper.TbWorkOrderMapper;
import com.tds.dos.dal.msp.entity.TbNode;
import com.tds.dos.dal.msp.entity.TbTask;
import com.tds.dos.dal.msp.mapper.TbTaskMapper;
import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.common.enums.NodeStatus;
import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.service.msp.node.INodeService;
import com.tds.dos.service.msp.task.ITaskService;
import com.tds.dos.service.msp.task.TaskDTO;
import com.tds.dos.service.psi.IPsiCodeGenerator;
import com.tds.dos.service.ray.IAgentClient;
import com.tds.dos.service.ray.IRayClusterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 隐私计算策略 - 集成MSP (SecretFlow PSI求交、联邦学习)
 */
@Slf4j
@Service
public class PrivacyComputeStrategy implements WorkOrderStrategy {

    @Autowired
    private TbWorkOrderMapper workOrderMapper;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private INodeService nodeService;

    @Autowired
    private IPsiCodeGenerator psiCodeGenerator;

    @Autowired
    private TbTaskMapper taskMapper;

    @Autowired
    private IRayClusterService rayClusterService;

    @Autowired
    private IAgentClient agentClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getWorkOrderType() {
        return "PRIVACY_COMPUTE";
    }

    @Override
    public void preProcess(String workOrderId) {
        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new RuntimeException("工单不存在");
        }
        workOrder.setWorkOrderStatus(WorkOrderStatus.PROCESSING.getCode());
        workOrderMapper.updateById(workOrder);

        String clusterId = null;
        try {
            Map<String, Object> params = parseParams(workOrder.getConfigJson());

            String computeType = (String) params.get("computeType");
            String taskName = (String) params.getOrDefault("taskName", "PrivacyCompute-" + workOrderId);

            // 获取参与节点列表
            Object participantsObj = params.get("participants");
            List<String> participantNodeIds = null;
            if (participantsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                List<String> list = (java.util.List<String>) participantsObj;
                participantNodeIds = list;
            }

            // 如果有参与节点，动态创建Ray集群
            if (participantNodeIds != null && !participantNodeIds.isEmpty()) {
                log.info("Creating Ray cluster for task {}, participants: {}", taskName, participantNodeIds);
                clusterId = rayClusterService.createCluster(taskName, participantNodeIds);
                log.info("Ray cluster {} created successfully", clusterId);
            } else {
                log.warn("No participants specified for task {}, will use default ray address", taskName);
            }

            // 构建MSP任务DTO
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setName(taskName);
            taskDTO.setDescription("DOS工单触发: " + workOrderId);

            // 设置任务类型
            TaskType taskType = TaskType.PSI;
            if ("PSI".equalsIgnoreCase(computeType)) {
                taskType = TaskType.PSI;
            } else if ("MPC".equalsIgnoreCase(computeType)) {
                taskType = TaskType.MPC;
            } else if ("FEDERATED_LEARNING".equalsIgnoreCase(computeType)) {
                taskType = TaskType.FEDERATED_LEARNING;
            } else if ("VERTICAL_FL".equalsIgnoreCase(computeType)) {
                taskType = TaskType.VERTICAL_FL;
            } else {
                taskType = TaskType.COMPOUND_TASK;
            }
            taskDTO.setType(taskType);

            // 节点模式
            String nodeMode = (String) params.getOrDefault("nodeMode", "RAY");
            taskDTO.setNodeMode(nodeMode);

            // 参与方列表
            taskDTO.setParticipants(participantNodeIds);

            // 参数传递
            Map<String, String> taskParams = new HashMap<>();
            taskParams.put("workOrderId", workOrderId);
            if (clusterId != null) {
                taskParams.put("clusterId", clusterId);
            }
            if (params.get("partyADataPath") != null) {
                taskParams.put("partyADataPath", String.valueOf(params.get("partyADataPath")));
            }
            if (params.get("partyBDataPath") != null) {
                taskParams.put("partyBDataPath", String.valueOf(params.get("partyBDataPath")));
            }
            if (params.get("labelColumn") != null) {
                taskParams.put("labelColumn", String.valueOf(params.get("labelColumn")));
            }
            if (params.get("featureColumns") != null) {
                taskParams.put("featureColumns", String.valueOf(params.get("featureColumns")));
            }
            taskDTO.setParameters(taskParams);

            // 创建MSP任务
            String taskId = taskService.createTask(taskDTO);

            // 如果是PSI任务，生成PSI执行脚本并提交到Ray集群
            if (taskType == TaskType.PSI && clusterId != null) {
                submitPsiTaskToCluster(taskId, clusterId, params);
            }

            // 保存任务ID和集群ID到工单结果
            Map<String, Object> result = new HashMap<>();
            result.put("mspTaskId", taskId);
            result.put("computeType", computeType);
            result.put("nodeMode", nodeMode);
            if (clusterId != null) {
                result.put("clusterId", clusterId);
            }

            workOrder.setResultMessage(objectMapper.writeValueAsString(result));
            workOrderMapper.updateById(workOrder);

            log.info("Privacy compute preProcess completed. WorkOrderId={}, MspTaskId={}, ClusterId={}",
                workOrderId, taskId, clusterId);

        } catch (Exception e) {
            log.error("Privacy compute preProcess failed. WorkOrderId={}", workOrderId, e);
            // 如果创建了集群，回滚
            if (clusterId != null) {
                try {
                    rayClusterService.destroyCluster(clusterId);
                    log.info("Rolled back cluster {} due to preProcess failure", clusterId);
                } catch (Exception rollbackEx) {
                    log.warn("Failed to rollback cluster {}: {}", clusterId, rollbackEx.getMessage());
                }
            }
            workOrder.setWorkOrderStatus(WorkOrderStatus.FAILED.getCode());
            workOrder.setResultMessage("预处理失败: " + e.getMessage());
            workOrderMapper.updateById(workOrder);
            throw new RuntimeException("预处理失败", e);
        }
    }

    @Override
    public void execute(String workOrderId) {
        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        try {
            Map<String, Object> result = parseParams(workOrder.getResultMessage());
            String mspTaskId = (String) result.get("mspTaskId");

            if (mspTaskId == null || mspTaskId.isEmpty()) {
                throw new RuntimeException("未找到MSP任务ID");
            }

            // 执行MSP任务
            taskService.executeTask(mspTaskId);

            // 等待任务完成并获取结果
            String taskResult = waitForTaskCompletion(mspTaskId);

            workOrder.setWorkOrderStatus(WorkOrderStatus.COMPLETED.getCode());
            workOrder.setResultMessage(taskResult);
            workOrderMapper.updateById(workOrder);

            log.info("Privacy compute execute completed. WorkOrderId={}, MspTaskId={}", workOrderId, mspTaskId);

        } catch (Exception e) {
            log.error("Privacy compute execute failed. WorkOrderId={}", workOrderId, e);
            workOrder.setWorkOrderStatus(WorkOrderStatus.FAILED.getCode());
            workOrder.setResultMessage("执行失败: " + e.getMessage());
            workOrderMapper.updateById(workOrder);
            throw new RuntimeException("执行失败", e);
        }
    }

    @Override
    public void cancel(String workOrderId) {
        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        try {
            Map<String, Object> result = parseParams(workOrder.getResultMessage());
            String mspTaskId = (String) result.get("mspTaskId");
            String clusterId = (String) result.get("clusterId");

            if (mspTaskId != null && !mspTaskId.isEmpty()) {
                // 取消MSP任务
                taskService.cancelTask(mspTaskId);
            }

            // 释放Ray集群
            if (clusterId != null && !clusterId.isEmpty()) {
                try {
                    rayClusterService.releaseCluster(clusterId);
                    log.info("Released cluster {} for cancelled workOrder {}", clusterId, workOrderId);
                } catch (Exception clusterEx) {
                    log.warn("Failed to release cluster {} for WorkOrderId={}: {}",
                        clusterId, workOrderId, clusterEx.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to cancel MSP task for WorkOrderId={}", workOrderId, e);
        }

        workOrder.setWorkOrderStatus(WorkOrderStatus.CANCELLED.getCode());
        workOrderMapper.updateById(workOrder);
        log.info("Privacy compute cancelled. WorkOrderId={}", workOrderId);
    }

    /**
     * 等待任务完成（轮询）
     */
    private String waitForTaskCompletion(String taskId) {
        int maxRetries = 60; // 最多等待60次
        int retryInterval = 5000; // 5秒间隔
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                TaskStatus status = taskService.queryStatus(taskId);
                if (status != null) {
                    // 状态: CREATED, PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
                    if (status == TaskStatus.COMPLETED) {
                        // 任务完成，获取结果
                        ApiResponse<String> response = taskService.getTaskResult(taskId);
                        return response != null ? response.getData() : null;
                    } else if (status == TaskStatus.FAILED) {
                        throw new RuntimeException("MSP任务执行失败");
                    } else if (status == TaskStatus.CANCELLED) {
                        throw new RuntimeException("MSP任务已取消");
                    }
                }

                retryCount++;
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("等待任务完成被中断");
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("等待任务完成超时");
                }
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待任务完成被中断");
                }
            }
        }

        throw new RuntimeException("等待任务完成超时");
    }

    /**
     * 解析JSON参数字符串
     */
    private Map<String, Object> parseParams(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonStr, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse params JSON: {}", jsonStr);
            return new HashMap<>();
        }
    }

    /**
     * 提交PSI任务到Ray集群
     */
    private void submitPsiTaskToCluster(String taskId, String clusterId, Map<String, Object> params) {
        try {
            // 获取集群Head地址
            String headAddress = rayClusterService.getHeadAddress(clusterId);
            if (headAddress == null) {
                throw new RuntimeException("无法获取集群Head地址，clusterId: " + clusterId);
            }

            // 获取参与节点信息来确定PartyA和PartyB的地址
            Object participantsObj = params.get("participants");
            List<String> participantNodeIds = null;
            if (participantsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                List<String> list = (java.util.List<String>) participantsObj;
                participantNodeIds = list;
            }

            // Party地址使用Head地址（简化处理，实际多节点时需要区分）
            String partyAAddress = headAddress;
            String partyBAddress = headAddress;

            // 如果有多个参与节点，根据节点角色分配地址
            if (participantNodeIds != null && participantNodeIds.size() >= 2) {
                TbNode nodeA = nodeService.getNode(participantNodeIds.get(0));
                TbNode nodeB = nodeService.getNode(participantNodeIds.get(1));
                if (nodeA != null && nodeA.getfRayEndpoint() != null) {
                    partyAAddress = nodeA.getfRayEndpoint();
                }
                if (nodeB != null && nodeB.getfRayEndpoint() != null) {
                    partyBAddress = nodeB.getfRayEndpoint();
                }
            }

            String partyADataPath = params.get("partyADataPath") != null
                ? String.valueOf(params.get("partyADataPath")) : "/tmp/party_a.csv";
            String partyBDataPath = params.get("partyBDataPath") != null
                ? String.valueOf(params.get("partyBDataPath")) : "/tmp/party_b.csv";
            String keyColumn = params.get("keyColumn") != null
                ? String.valueOf(params.get("keyColumn")) : "id";
            String protocol = params.get("protocol") != null
                ? String.valueOf(params.get("protocol")) : "ECPSI";
            String resultType = params.get("resultType") != null
                ? String.valueOf(params.get("resultType")) : "INTERSECTION";

            // 生成PSI代码
            String pythonCode = psiCodeGenerator.generatePsiCode(
                taskId, partyADataPath, partyBDataPath,
                keyColumn, protocol, resultType, "A",
                headAddress, partyAAddress, partyBAddress
            );

            // 更新任务的fCode字段
            TbTask task = taskMapper.selectById(taskId);
            if (task != null) {
                task.setfCode(pythonCode);
                task.setfUpdateTime(LocalDateTime.now());
                taskMapper.updateById(task);
            }

            // 获取Head节点作为任务提交节点
            String clusterStatus = rayClusterService.getClusterStatus(clusterId);
            if ("RUNNING".equals(clusterStatus)) {
                // 向Head节点提交任务
                TbNode headNode = nodeService.getNode(
                    rayClusterService.getNodeClusterId(participantNodeIds.get(0)) != null
                        ? participantNodeIds.get(0) : null
                );
                if (headNode != null) {
                    String jobId = agentClient.submitJob(headNode.getfEndpoint(), pythonCode, taskId);
                    log.info("PSI task {} submitted to cluster {}, jobId: {}", taskId, clusterId, jobId);

                    // 更新任务状态
                    if (task != null) {
                        task.setfStatus(3); // 执行中
                        taskMapper.updateById(task);
                    }
                }
            }

            log.info("PSI task {} code generated and submitted, cluster: {}, code length: {}",
                taskId, clusterId, pythonCode.length());

        } catch (Exception e) {
            log.error("Failed to submit PSI task {} to cluster {}: {}", taskId, clusterId, e.getMessage());
            // 不抛出异常，避免影响工单创建流程
        }
    }
}
