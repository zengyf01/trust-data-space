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
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                RayStatus status = new RayStatus();
                status.setRunning((Boolean) data.getOrDefault("running", false));
                status.setClusterId((String) data.get("clusterId"));
                status.setRayAddress((String) data.get("rayAddress"));
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
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
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
