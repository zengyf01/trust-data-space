package com.tds.dos.service.ray;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Agent客户端实现 - 通过HTTP调用节点Agent
 */
@Slf4j
@Service
public class AgentClientImpl implements IAgentClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${agent.timeout:30000}")
    private int timeout;

    public AgentClientImpl() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String startHead(String agentEndpoint, int rayPort) {
        try {
            String url = agentEndpoint + "/agent/ray/start-head";
            Map<String, Object> body = Map.of(
                "rayPort", rayPort,
                "dashboardPort", 8265
            );

            HttpResponse<String> response = doPost(url, body);
            if (response.statusCode() == 200) {
                Map<String, Object> resp = objectMapper.readValue(response.body(), Map.class);
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                if (data == null) {
                    log.error("Agent {}/agent/ray/start-head returned null data. Response: {}", agentEndpoint, response.body());
                    throw new RuntimeException("Agent returned null data. Is the Agent service running? Response: " + response.body());
                }
                String rayAddress = (String) data.get("rayAddress");
                log.info("Started Ray Head at {}, agent: {}", rayAddress, agentEndpoint);
                return rayAddress;
            } else {
                log.error("Failed to start Ray Head at {}, status: {}, body: {}",
                    agentEndpoint, response.statusCode(), response.body());
                throw new RuntimeException("Failed to start Ray Head: " + response.body());
            }
        } catch (Exception e) {
            log.error("Error starting Ray Head at {}: {}", agentEndpoint, e.getMessage());
            throw new RuntimeException("Error starting Ray Head", e);
        }
    }

    @Override
    public String startWorker(String agentEndpoint, String headAddress, int rayPort) {
        try {
            String url = agentEndpoint + "/agent/ray/start-worker";
            Map<String, Object> body = Map.of(
                "headAddress", headAddress,
                "rayPort", rayPort
            );

            HttpResponse<String> response = doPost(url, body);
            if (response.statusCode() == 200) {
                Map<String, Object> resp = objectMapper.readValue(response.body(), Map.class);
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                if (data == null) {
                    log.error("Agent {}/agent/ray/start-worker returned null data. Response: {}", agentEndpoint, response.body());
                    return null;
                }
                String workerRayAddress = (String) data.get("rayAddress");
                log.info("Worker joined cluster at {}, agent: {}, worker ray address: {}", headAddress, agentEndpoint, workerRayAddress);
                return workerRayAddress;
            } else {
                log.error("Failed to start Worker at {}, status: {}, body: {}",
                    agentEndpoint, response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("Error starting Worker at {}: {}", agentEndpoint, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean stopRay(String agentEndpoint) {
        try {
            String url = agentEndpoint + "/agent/ray/stop";

            HttpResponse<String> response = doPost(url, Map.of());
            if (response.statusCode() == 200) {
                log.info("Stopped Ray at {}", agentEndpoint);
                return true;
            } else {
                log.error("Failed to stop Ray at {}, status: {}", agentEndpoint, response.statusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Error stopping Ray at {}: {}", agentEndpoint, e.getMessage());
            return false;
        }
    }

    @Override
    public RayStatus getRayStatus(String agentEndpoint) {
        try {
            String url = agentEndpoint + "/agent/ray/status";

            HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofMillis(timeout))
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                Map<String, Object> resp = objectMapper.readValue(response.body(), Map.class);
                if (!isBusinessSuccess(resp)) {
                    log.error("Agent {}/agent/ray/status returned business error. Response: {}", agentEndpoint, response.body());
                    return null;
                }
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                if (data == null) {
                    log.error("Agent {}/agent/ray/status returned null data. Response: {}", agentEndpoint, response.body());
                    return null;
                }
                RayStatus status = new RayStatus();
                status.setRunning((Boolean) data.getOrDefault("running", false));
                status.setClusterId((String) data.get("clusterId"));
                status.setRayAddress((String) data.get("rayAddress"));
                status.setNodeIp((String) data.get("nodeIp"));
                return status;
            } else {
                log.error("Failed to get Ray status from {}, status: {}", agentEndpoint, response.statusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error getting Ray status from {}: {}", agentEndpoint, e.getMessage());
            return null;
        }
    }

    @Override
    public String submitJob(String agentEndpoint, String script, String taskId) {
        try {
            String url = agentEndpoint + "/agent/task/run";
            Map<String, Object> body = Map.of(
                "script", script,
                "taskId", taskId
            );

            HttpResponse<String> response = doPost(url, body);
            if (response.statusCode() == 200) {
                Map<String, Object> resp = objectMapper.readValue(response.body(), Map.class);
                if (!isBusinessSuccess(resp)) {
                    log.error("Agent {}/agent/task/run returned business error. Response: {}", agentEndpoint, response.body());
                    throw new RuntimeException("Agent拒绝任务提交: " + resp.get("msg"));
                }
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                if (data == null) {
                    log.error("Agent {}/agent/task/run returned null data. Response: {}", agentEndpoint, response.body());
                    throw new RuntimeException("Agent returned null data. Response: " + response.body());
                }
                String jobId = (String) data.get("jobId");
                log.info("Submitted job {} to {}, jobId: {}", taskId, agentEndpoint, jobId);
                return jobId;
            } else {
                log.error("Failed to submit job to {}, status: {}, body: {}",
                    agentEndpoint, response.statusCode(), response.body());
                throw new RuntimeException("Failed to submit job: " + response.body());
            }
        } catch (Exception e) {
            log.error("Error submitting job to {}: {}", agentEndpoint, e.getMessage());
            throw new RuntimeException("Error submitting job", e);
        }
    }

    @Override
    public TaskStatus getTaskStatus(String agentEndpoint, String jobId) {
        try {
            String url = agentEndpoint + "/agent/task/status/" + jobId;

            HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofMillis(timeout))
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                Map<String, Object> resp = objectMapper.readValue(response.body(), Map.class);
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                if (data == null) {
                    log.error("Agent {}/agent/task/status/{} returned null data. Response: {}", agentEndpoint, jobId, response.body());
                    return null;
                }
                TaskStatus status = new TaskStatus();
                status.setStatus((String) data.get("status"));
                status.setResult((String) data.get("result"));
                status.setError((String) data.get("error"));
                return status;
            } else {
                log.error("Failed to get task status from {}, jobId: {}, status: {}",
                    agentEndpoint, jobId, response.statusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error getting task status from {}, jobId: {}: {}", agentEndpoint, jobId, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean stopJob(String agentEndpoint, String jobId) {
        try {
            String url = agentEndpoint + "/agent/task/stop/" + jobId;

            HttpResponse<String> response = doPost(url, Map.of());
            if (response.statusCode() == 200) {
                log.info("Stopped job {} at {}", jobId, agentEndpoint);
                return true;
            } else {
                log.error("Failed to stop job {} at {}, status: {}", jobId, agentEndpoint, response.statusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Error stopping job {} at {}: {}", jobId, agentEndpoint, e.getMessage());
            return false;
        }
    }

    @Override
    public byte[] downloadTaskFile(String agentEndpoint, String jobId, String filePath) {
        try {
            String encodedJob = java.net.URLEncoder.encode(jobId, java.nio.charset.StandardCharsets.UTF_8);
            String encodedPath = java.net.URLEncoder.encode(filePath, java.nio.charset.StandardCharsets.UTF_8);
            String url = agentEndpoint + "/agent/task/file?jobId=" + encodedJob + "&path=" + encodedPath;

            HttpResponse<byte[]> response = httpClient.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofMillis(timeout))
                    .build(),
                HttpResponse.BodyHandlers.ofByteArray()
            );

            if (response.statusCode() != 200) {
                String body = new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
                log.error("下载任务文件失败: agent={}, jobId={}, status={}, body={}", agentEndpoint, jobId, response.statusCode(), body);
                throw new RuntimeException("Agent 拒绝下载: HTTP " + response.statusCode() + " - " + body);
            }
            // Agent 业务失败时也返 200，但 Content-Type 是 application/json；正常文件是 text/csv
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (contentType.contains("application/json")) {
                String body = new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
                throw new RuntimeException("Agent 拒绝下载: " + body);
            }
            log.info("下载任务文件成功: agent={}, jobId={}, path={}, bytes={}", agentEndpoint, jobId, filePath, response.body().length);
            return response.body();
        } catch (Exception e) {
            log.error("Error downloading task file {} from {}: {}", filePath, agentEndpoint, e.getMessage());
            throw new RuntimeException("下载任务文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadNodeFile(String agentEndpoint, String filePath) {
        try {
            String encodedPath = java.net.URLEncoder.encode(filePath, java.nio.charset.StandardCharsets.UTF_8);
            String url = agentEndpoint + "/agent/file/download?path=" + encodedPath;

            HttpResponse<byte[]> response = httpClient.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofMillis(timeout))
                    .build(),
                HttpResponse.BodyHandlers.ofByteArray()
            );

            if (response.statusCode() != 200) {
                String body = new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
                log.error("旁路下载失败: agent={}, path={}, status={}, body={}", agentEndpoint, filePath, response.statusCode(), body);
                throw new RuntimeException("Agent 拒绝下载: HTTP " + response.statusCode() + " - " + body);
            }
            // Agent 业务失败时返回 JSON
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (contentType.contains("application/json")) {
                String body = new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
                throw new RuntimeException("Agent 拒绝下载: " + body);
            }
            log.info("旁路下载成功: agent={}, path={}, bytes={}", agentEndpoint, filePath, response.body().length);
            return response.body();
        } catch (Exception e) {
            log.error("旁路下载失败: {} from {}: {}", filePath, agentEndpoint, e.getMessage());
            throw new RuntimeException("旁路下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * Agent业务失败时同样返回HTTP 200，需要检查响应体里的code
     */
    private boolean isBusinessSuccess(Map<String, Object> resp) {
        Object code = resp.get("code");
        return code instanceof Number && ((Number) code).intValue() == 200;
    }

    private HttpResponse<String> doPost(String url, Map<String, Object> body) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(body);
        return httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofMillis(timeout))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }
}
