package com.tds.dos.service.sandbox;

import com.tds.dos.dal.entity.TbWorkOrder;
import com.tds.dos.dal.mapper.TbWorkOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 安全沙盒服务实现
 */
@Service
public class SandboxServiceImpl implements ISandboxService {

    @Autowired
    private KataKubernetesClient kataClient;

    @Autowired
    private TbWorkOrderMapper workOrderMapper;

    private static final int DEFAULT_TIMEOUT_SECONDS = 120;

    @Override
    public Map<String, Object> createSandbox(String workOrderId, String image, int cpu, int memoryMB,
                                              String workDir, String sourceUrl) {
        // 生成Pod名称
        String podName = "sandbox-" + workOrderId.substring(0, Math.min(8, workOrderId.length())) +
                         "-" + System.currentTimeMillis();

        // 调用K8s API创建Pod
        KataKubernetesClient.SandboxPodInfo podInfo = kataClient.createSandboxPod(
            podName, image, cpu, memoryMB, workDir, sourceUrl);

        Map<String, Object> result = new HashMap<>();
        result.put("podName", podInfo.getName());
        result.put("namespace", podInfo.getNamespace());
        result.put("status", podInfo.getStatus());

        return result;
    }

    @Override
    public void destroySandbox(String podName) {
        kataClient.deletePod(podName);
    }

    @Override
    public void stopSandbox(String podName) {
        kataClient.stopPod(podName);
    }

    @Override
    public Map<String, Object> getSandboxStatus(String podName) {
        String status = kataClient.getPodStatus(podName);
        Map<String, Object> result = new HashMap<>();
        result.put("podName", podName);
        result.put("status", status);
        return result;
    }

    @Override
    public Map<String, Object> getSandboxDetail(String podName) {
        KataKubernetesClient.SandboxPodDetail detail = kataClient.getPodDetail(podName);
        Map<String, Object> result = new HashMap<>();
        result.put("podName", detail.getName());
        result.put("namespace", detail.getNamespace());
        result.put("phase", detail.getPhase());
        result.put("podIP", detail.getPodIP());
        result.put("hostIP", detail.getHostIP());
        result.put("jupyterUrl", detail.getJupyterUrl());
        result.put("startTime", detail.getStartTime());

        if (detail.getContainerStatuses() != null) {
            List<Map<String, Object>> containers = new ArrayList<>();
            for (var cs : detail.getContainerStatuses()) {
                Map<String, Object> c = new HashMap<>();
                c.put("name", cs.getName());
                c.put("ready", cs.getReady());
                c.put("restartCount", cs.getRestartCount());
                c.put("state", cs.getState() != null ? cs.getState().toString() : null);
                containers.add(c);
            }
            result.put("containerStatuses", containers);
        }

        return result;
    }

    @Override
    public String getSandboxLogs(String podName, boolean tail) {
        return kataClient.getPodLogs(podName, tail);
    }

    @Override
    public List<Map<String, Object>> getSandboxEvents(String podName) {
        var events = kataClient.getPodEvents(podName);
        List<Map<String, Object>> result = new ArrayList<>();
        for (var event : events) {
            Map<String, Object> e = new HashMap<>();
            e.put("type", event.getType());
            e.put("reason", event.getReason());
            e.put("message", event.getMessage());
            e.put("firstTimestamp", event.getFirstTimestamp());
            e.put("lastTimestamp", event.getLastTimestamp());
            e.put("count", event.getCount());
            result.add(e);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listSandboxes(String userId) {
        var pods = kataClient.listSandboxPods();
        List<Map<String, Object>> result = new ArrayList<>();
        for (var pod : pods) {
            Map<String, Object> p = new HashMap<>();
            p.put("podName", pod.getMetadata().getName());
            p.put("namespace", pod.getMetadata().getNamespace());
            p.put("phase", pod.getStatus().getPhase());
            p.put("podIP", pod.getStatus().getPodIP());
            p.put("hostIP", pod.getStatus().getHostIP());
            p.put("startTime", pod.getStatus().getStartTime());
            result.add(p);
        }
        return result;
    }

    @Override
    public String waitForJupyterLab(String podName, int timeoutSeconds) {
        boolean running = kataClient.waitForRunning(podName, timeoutSeconds > 0 ? timeoutSeconds : DEFAULT_TIMEOUT_SECONDS);
        if (!running) {
            throw new RuntimeException("沙盒启动超时");
        }

        // 获取JupyterLab URL
        var detail = kataClient.getPodDetail(podName);
        if (detail.getJupyterUrl() != null) {
            return detail.getJupyterUrl();
        }

        // 如果状态已是Running但URL还未分配，构造一个
        return "http://" + detail.getHostIP() + ":8888/?token=" + podName;
    }
}