package com.tds.dos.service.psi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Ray Job Submission API 实现
 * 使用Ray的REST API提交和管理Python作业
 */
@Component
public class RayJobSubmitterImpl implements IRayJobSubmitter {

    private static final Logger log = LoggerFactory.getLogger(RayJobSubmitterImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ray.head.url:http://ray-head:5000}")
    private String defaultRayHeadUrl;

    @Override
    public String submitJob(String rayHeadUrl, String jobName, String pythonCode) throws Exception {
        String url = (rayHeadUrl != null ? rayHeadUrl : defaultRayHeadUrl) + "/api/jobs";

        // 构建请求体
        Map<String, Object> requestBody = Map.of(
            "entrypoint", "python /tmp/psi_task.py",
            "runtime_env", Map.of("working_dir", "/tmp"),
            "metadata", Map.of("job_name", jobName, "task_type", "PSI")
        );

        // 先上传Python代码到Ray集群
        uploadPythonCode(rayHeadUrl, pythonCode);

        // 提交作业
        String response = httpPost(url, requestBody);
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

        String jobId = (String) responseMap.get("job_id");
        log.info("Ray job submitted successfully: {}, job_id: {}", jobName, jobId);
        return jobId;
    }

    @Override
    public JobStatus waitForCompletion(String jobId, long timeoutMs) throws Exception {
        long startTime = System.currentTimeMillis();
        long deadline = startTime + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            JobStatus status = getJobStatus(jobId);
            log.debug("Job {} status: {}", jobId, status);

            if (status == JobStatus.SUCCEEDED ||
                status == JobStatus.FAILED ||
                status == JobStatus.CANCELLED) {
                return status;
            }

            Thread.sleep(5000); // 5秒轮询间隔
        }

        log.warn("Job {} wait timeout after {}ms", jobId, timeoutMs);
        return JobStatus.PENDING;
    }

    @Override
    public String getJobLogs(String jobId) throws Exception {
        String rayHeadUrl = getDefaultRayHeadUrl();
        String url = rayHeadUrl + "/api/jobs/" + jobId + "/logs";

        try {
            return httpGet(url);
        } catch (Exception e) {
            log.error("Failed to get job logs for {}: {}", jobId, e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @Override
    public JobStatus getJobStatus(String jobId) throws Exception {
        String rayHeadUrl = getDefaultRayHeadUrl();
        String url = rayHeadUrl + "/api/jobs/" + jobId;

        try {
            String response = httpGet(url);
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            String statusStr = (String) responseMap.get("status");

            if (statusStr == null) {
                return JobStatus.PENDING;
            }

            return parseJobStatus(statusStr);
        } catch (Exception e) {
            log.error("Failed to get job status for {}: {}", jobId, e.getMessage());
            return JobStatus.PENDING;
        }
    }

    @Override
    public boolean cancelJob(String jobId) throws Exception {
        String rayHeadUrl = getDefaultRayHeadUrl();
        String url = rayHeadUrl + "/api/jobs/" + jobId;

        try {
            // Ray Job API不支持直接取消，使用DELETE方法
            URL deleteUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) deleteUrl.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            return responseCode == 200 || responseCode == 204;
        } catch (Exception e) {
            log.error("Failed to cancel job {}: {}", jobId, e.getMessage());
            return false;
        }
    }

    /**
     * 上传Python代码到Ray集群
     */
    private void uploadPythonCode(String rayHeadUrl, String pythonCode) throws Exception {
        String url = (rayHeadUrl != null ? rayHeadUrl : defaultRayHeadUrl) + "/api/jobs/scripts";

        // Base64编码
        String encoded = java.util.Base64.getEncoder().encodeToString(pythonCode.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> requestBody = Map.of(
            "script", encoded,
            "filename", "psi_task.py",
            "encoding", "base64"
        );

        httpPost(url, requestBody);
        log.debug("Python code uploaded to Ray cluster");
    }

    /**
     * 发送HTTP POST请求
     */
    private String httpPost(String urlStr, Object body) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            objectMapper.writeValue(os, body);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            throw new RuntimeException("HTTP error: " + responseCode);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * 发送HTTP GET请求
     */
    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            throw new RuntimeException("HTTP error: " + responseCode);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * 解析作业状态
     */
    private JobStatus parseJobStatus(String statusStr) {
        switch (statusStr.toUpperCase()) {
            case "PENDING":
            case "STARTING":
                return JobStatus.PENDING;
            case "RUNNING":
                return JobStatus.RUNNING;
            case "SUCCEEDED":
            case "COMPLETED":
            case "OK":
                return JobStatus.SUCCEEDED;
            case "FAILED":
            case "ERROR":
                return JobStatus.FAILED;
            case "CANCELLED":
            case "CANCELED":
                return JobStatus.CANCELLED;
            default:
                return JobStatus.PENDING;
        }
    }

    private String getDefaultRayHeadUrl() {
        return System.getenv("RAY_HEAD_URL") != null ? System.getenv("RAY_HEAD_URL") : defaultRayHeadUrl;
    }
}
