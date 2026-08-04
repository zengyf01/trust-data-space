package com.tds.dos.service.workorder.strategy;

import com.tds.dos.common.enums.WorkOrderStatus;
import com.tds.dos.dal.entity.TbWorkOrder;
import com.tds.dos.dal.mapper.TbWorkOrderMapper;
import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.common.enums.NodeStatus;
import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.service.msp.node.INodeService;
import com.tds.dos.service.msp.task.ITaskService;
import com.tds.dos.service.msp.task.TaskDTO;
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
 * 隐私计算策略 - 集成MSP (SecretFlow PSI求交、横向联邦)
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
    private IRayClusterService rayClusterService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getWorkOrderType() {
        // WorkOrderStrategyFactory 按该值作为 map key 注册策略；工单的 f_work_order_type 存的是数字
        // （见 com.tds.dos.common.enums.WorkOrderType.PRIVACY_COMPUTE=3），必须返回数字字符串才能被找到。
        return "3";
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

            // 如果有参与节点且不是PSI任务（PSI统一由 PsiTaskExecutor 创建集群）
            boolean isPsi = "PSI".equalsIgnoreCase(computeType);
            if (participantNodeIds != null && !participantNodeIds.isEmpty() && !isPsi) {
                log.info("Creating Ray cluster for task {}, participants: {}", taskName, participantNodeIds);
                clusterId = rayClusterService.createCluster(taskName, participantNodeIds);
                log.info("Ray cluster {} created successfully", clusterId);
            } else {
                log.warn("No participants specified for task {} (or PSI type), will rely on task executor to manage cluster", taskName);
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
                taskType = TaskType.HORIZONTAL_FL;
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
            if (params.get("keyColumn") != null) {
                taskParams.put("keyColumn", String.valueOf(params.get("keyColumn")));
            }
            if (params.get("protocol") != null) {
                taskParams.put("protocol", String.valueOf(params.get("protocol")));
            }
            if (params.get("resultType") != null) {
                taskParams.put("resultType", String.valueOf(params.get("resultType")));
            }
            if (isPsi && participantNodeIds != null && participantNodeIds.size() >= 2) {
                taskParams.put("partyANodeId", participantNodeIds.get(0));
                taskParams.put("partyBNodeId", participantNodeIds.get(1));
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

            // PSI 任务不再自行建集群/提交，统一由 PsiTaskExecutor 接管
            // FL/VFL 已实现，由对应 TaskExecutor 接管

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

}
