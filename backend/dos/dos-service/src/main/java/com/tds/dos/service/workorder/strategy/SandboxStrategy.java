package com.tds.dos.service.workorder.strategy;

import com.tds.dos.common.enums.WorkOrderStatus;
import com.tds.dos.dal.entity.TbWorkOrder;
import com.tds.dos.dal.mapper.TbWorkOrderMapper;
import com.tds.dos.service.sandbox.KataKubernetesClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 安全沙盒策略 - Kata容器 + TEE运行时
 */
@Service
public class SandboxStrategy implements WorkOrderStrategy {

    @Autowired
    private TbWorkOrderMapper workOrderMapper;

    @Autowired
    private KataKubernetesClient kataClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getWorkOrderType() {
        return "SANDBOX";
    }

    @Override
    public void preProcess(String workOrderId) {
        TbWorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            throw new RuntimeException("工单不存在");
        }
        workOrder.setWorkOrderStatus(WorkOrderStatus.PROCESSING.getCode());
        workOrderMapper.updateById(workOrder);

        try {
            Map<String, Object> params = objectMapper.readValue(
                    workOrder.getConfigJson(), Map.class);

            String imageName = (String) params.get("imageName");
            Integer cpu = (Integer) params.get("cpu");
            Integer memory = (Integer) params.get("memory");

            // 创建沙盒Pod
            String podName = createSandboxPod(imageName, cpu, memory);
            workOrder.setResultMessage("{\"podName\": \"" + podName + "\"}");
            workOrderMapper.updateById(workOrder);

        } catch (Exception e) {
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
            Map<String, Object> params = objectMapper.readValue(
                    workOrder.getConfigJson(), Map.class);

            String podName = (String) params.get("podName");
            String workDir = (String) params.get("workDir");

            // 初始化工作目录
            initWorkDir(podName, workDir);

            // 等待JupyterLab启动
            String jupyterUrl = waitForJupyterLab(podName);

            workOrder.setWorkOrderStatus(WorkOrderStatus.COMPLETED.getCode());
            workOrder.setResultMessage("{\"jupyterUrl\": \"" + jupyterUrl + "\"}");
            workOrderMapper.updateById(workOrder);

        } catch (Exception e) {
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
            Map<String, Object> result = objectMapper.readValue(
                    workOrder.getResultMessage(), Map.class);
            String podName = (String) result.get("podName");
            destroySandboxPod(podName);
        } catch (Exception e) {
            // 忽略解析错误
        }
        workOrder.setWorkOrderStatus(WorkOrderStatus.CANCELLED.getCode());
        workOrderMapper.updateById(workOrder);
    }

    private String createSandboxPod(String imageName, Integer cpu, Integer memory) {
        String podName = "sandbox-" + System.currentTimeMillis();
        KataKubernetesClient.SandboxPodInfo info = kataClient.createSandboxPod(
            podName,
            imageName,
            cpu != null ? cpu : 2,
            memory != null ? memory : 4096,
            "/workspace",
            null
        );
        return info.getName();
    }

    private void initWorkDir(String podName, String workDir) {
        // 工作目录初始化由Pod的initContainer完成
    }

    private String waitForJupyterLab(String podName) {
        boolean running = kataClient.waitForRunning(podName, 120);
        if (!running) {
            throw new RuntimeException("JupyterLab启动超时");
        }
        var detail = kataClient.getPodDetail(podName);
        return detail.getJupyterUrl() != null ?
            detail.getJupyterUrl() :
            "http://" + detail.getHostIP() + ":8888/?token=" + podName;
    }

    private void destroySandboxPod(String podName) {
        kataClient.deletePod(podName);
    }
}