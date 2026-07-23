package com.tds.dos.service.sandbox;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kata容器Kubernetes客户端
 * 用于管理安全沙盒Pod的生命周期
 */
@Component
public class KataKubernetesClient {

    private static final Logger log = LoggerFactory.getLogger(KataKubernetesClient.class);

    @Value("${kubernetes.api.server:https://kubernetes.default.svc}")
    private String apiServer;

    @Value("${kubernetes.api.token:}")
    private String apiToken;

    @Value("${kubernetes.namespace:default}")
    private String namespace;

    private CoreV1Api coreV1Api;

    @PostConstruct
    public void init() throws IOException {
        ApiClient client;
        if (apiToken != null && !apiToken.isEmpty()) {
            client = Config.fromToken(apiServer, apiToken);
        } else {
            client = Config.defaultClient();
        }
        Configuration.setDefaultApiClient(client);
        this.coreV1Api = new CoreV1Api(client);
    }

    public SandboxPodInfo createSandboxPod(String podName, String image, int cpu, int memoryMB,
                                           String workDir, String sourceUrl) {
        try {
            // 创建Kata容器Pod定义
            V1Pod pod = new V1Pod()
                .metadata(new V1ObjectMeta()
                    .name(podName)
                    .namespace(namespace)
                    .labels(Map.of("app", "sandbox", "creator", "dos")))
                .spec(new V1PodSpec()
                    .restartPolicy("Never")
                    .runtimeClassName("kata")
                    .containers(List.of(
                        new V1Container()
                            .name("jupyter")
                            .image(image)
                            .ports(List.of(
                                new V1ContainerPort().containerPort(8888).name("jupyter"),
                                new V1ContainerPort().containerPort(22).name("ssh")))
                            .env(List.of(
                                new V1EnvVar().name("JUPYTER_TOKEN").value(podName),
                                new V1EnvVar().name("WORK_DIR").value(workDir != null ? workDir : "/workspace")))
                            .resources(new V1ResourceRequirements()
                                .requests(Map.of(
                                    "cpu", new Quantity(String.valueOf(cpu)),
                                    "memory", new Quantity(memoryMB + "Mi")))
                                .limits(Map.of(
                                    "cpu", new Quantity(String.valueOf(cpu)),
                                    "memory", new Quantity(memoryMB + "Mi"))))
                            .volumeMounts(workDir != null ? List.of(
                                new V1VolumeMount().name("workspace").mountPath(workDir)) : null)
                            .livenessProbe(new V1Probe()
                                .httpGet(new V1HTTPGetAction()
                                    .path("/api")
                                    .port(new io.kubernetes.client.custom.IntOrString(8888)))
                                .initialDelaySeconds(30)
                                .periodSeconds(10))
                            .readinessProbe(new V1Probe()
                                .httpGet(new V1HTTPGetAction()
                                    .path("/api/status")
                                    .port(new io.kubernetes.client.custom.IntOrString(8888)))
                                .initialDelaySeconds(10)
                                .periodSeconds(5))
                    ))
                    .volumes(workDir != null ? List.of(
                        new V1Volume()
                            .name("workspace")
                            .emptyDir(new V1EmptyDirVolumeSource())) : null)
                    .imagePullSecrets(sourceUrl != null ? List.of(
                        new V1LocalObjectReference().name("reg-secret")) : null)
                    .terminationGracePeriodSeconds(30L)
                    .activeDeadlineSeconds(3600L)
                );

            // 创建Pod
            V1Pod createdPod = coreV1Api.createNamespacedPod(
                namespace, pod, null, null, null, null);

            log.info("Kata沙盒Pod创建成功: {}/{}", namespace, podName);
            return new SandboxPodInfo(createdPod.getMetadata().getName(), namespace, "Creating");

        } catch (ApiException e) {
            log.error("创建Kata沙盒Pod失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建沙盒Pod失败: " + e.getMessage(), e);
        }
    }

    public String getPodStatus(String podName) {
        try {
            V1Pod pod = coreV1Api.readNamespacedPod(podName, namespace, "true");
            return pod.getStatus().getPhase();
        } catch (ApiException e) {
            if (e.getCode() == 404) return "NotFound";
            throw new RuntimeException("获取Pod状态失败: " + e.getMessage(), e);
        }
    }

    public SandboxPodDetail getPodDetail(String podName) {
        try {
            V1Pod pod = coreV1Api.readNamespacedPod(podName, namespace, "true");
            V1PodStatus status = pod.getStatus();
            String podIP = status.getPodIP();
            // 构造JupyterLab URL
            String jupyterUrl = podIP != null ?
                "http://" + podIP + ":8888/?token=" + podName : null;
            return new SandboxPodDetail(
                pod.getMetadata().getName(),
                namespace,
                status.getPhase(),
                podIP,
                status.getHostIP(),
                jupyterUrl,
                null, // startTime conversion issue
                status.getContainerStatuses()
            );
        } catch (ApiException e) {
            throw new RuntimeException("获取Pod详情失败: " + e.getMessage(), e);
        }
    }

    public String getPodLogs(String podName, boolean tail) {
        try {
            int tailLines = tail ? 100 : 0;
            return coreV1Api.readNamespacedPodLog(podName, namespace, "jupyter",
                null, Boolean.valueOf(tail), null, null, null, null, null, null);
        } catch (ApiException e) {
            throw new RuntimeException("获取Pod日志失败: " + e.getMessage(), e);
        }
    }

    public void deletePod(String podName) {
        try {
            coreV1Api.deleteNamespacedPod(podName, namespace, null, null, null, null, null, new V1DeleteOptions());
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                throw new RuntimeException("删除Pod失败: " + e.getMessage(), e);
            }
        }
    }

    public void stopPod(String podName) {
        try {
            coreV1Api.deleteNamespacedPod(podName, namespace, null, null, null, null, null, new V1DeleteOptions());
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                throw new RuntimeException("停止Pod失败: " + e.getMessage(), e);
            }
        }
    }

    public boolean waitForRunning(String podName, int timeoutSeconds) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutSeconds * 1000L) {
            try {
                String status = getPodStatus(podName);
                if ("Running".equals(status)) return true;
                if ("Failed".equals(status) || "Unknown".equals(status)) return false;
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    public List<V1Pod> listSandboxPods() {
        try {
            V1PodList list = coreV1Api.listNamespacedPod(namespace, null, null, null, null, null,
                null, null, "app=sandbox", null, null);
            return list.getItems();
        } catch (ApiException e) {
            throw new RuntimeException("获取沙盒列表失败: " + e.getMessage(), e);
        }
    }

    public List<io.kubernetes.client.openapi.models.CoreV1Event> getPodEvents(String podName) {
        try {
            io.kubernetes.client.openapi.models.CoreV1EventList events =
                coreV1Api.listNamespacedEvent(namespace, null, null, null, null, null,
                    null, null, "involvedObject.name=" + podName, null, null);
            return events.getItems();
        } catch (ApiException e) {
            throw new RuntimeException("获取Pod事件失败: " + e.getMessage(), e);
        }
    }

    public static class SandboxPodInfo {
        private final String name;
        private final String namespace;
        private final String status;
        public SandboxPodInfo(String name, String namespace, String status) {
            this.name = name; this.namespace = namespace; this.status = status;
        }
        public String getName() { return name; }
        public String getNamespace() { return namespace; }
        public String getStatus() { return status; }
    }

    public static class SandboxPodDetail {
        private final String name;
        private final String namespace;
        private final String phase;
        private final String podIP;
        private final String hostIP;
        private final String jupyterUrl;
        private final OffsetDateTime startTime;
        private final List<V1ContainerStatus> containerStatuses;
        public SandboxPodDetail(String name, String namespace, String phase, String podIP,
                                String hostIP, String jupyterUrl, OffsetDateTime startTime,
                                List<V1ContainerStatus> containerStatuses) {
            this.name = name; this.namespace = namespace; this.phase = phase;
            this.podIP = podIP; this.hostIP = hostIP; this.jupyterUrl = jupyterUrl;
            this.startTime = startTime; this.containerStatuses = containerStatuses;
        }
        public String getName() { return name; }
        public String getNamespace() { return namespace; }
        public String getPhase() { return phase; }
        public String getPodIP() { return podIP; }
        public String getHostIP() { return hostIP; }
        public String getJupyterUrl() { return jupyterUrl; }
        public OffsetDateTime getStartTime() { return startTime; }
        public List<V1ContainerStatus> getContainerStatuses() { return containerStatuses; }
    }
}