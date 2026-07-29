package com.tds.dos.service.msp.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.dos.dal.msp.entity.TbTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务执行器抽象基类
 * 提供公共逻辑：参数解析、HTTP调用Ray集群、结果解析
 */
public abstract class AbstractTaskExecutor implements ITaskExecutor {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String execute(String taskId, TbTask task) {
        try {
            // 1. 解析任务参数
            Map<String, String> params = parseParams(task.getfParameters());
            log.info("========== {} Task {} Execution Started ==========", getTaskType(), taskId);

            // 2. 执行具体任务
            return doExecute(taskId, task, params);
        } catch (Exception e) {
            log.error("Task {} execution failed: {}", taskId, e.getMessage(), e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * 子类实现具体执行逻辑
     */
    protected abstract String doExecute(String taskId, TbTask task, Map<String, String> params) throws Exception;

    /**
     * 调用Ray集群PSI服务
     */
    protected String callRayPsiService(String rayHeadUrl, Map<String, Object> requestBody) throws Exception {
        String psiUrl = rayHeadUrl + "/api/psi/execute";
        return httpPost(psiUrl, requestBody);
    }

    /**
     * 发送HTTP POST请求
     */
    protected String httpPost(String url, Map<String, Object> body) throws Exception {
        java.net.URL urlObj = new java.net.URL(url);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(60000);

        try (java.io.OutputStream os = conn.getOutputStream()) {
            objectMapper.writeValue(os, body);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            throw new RuntimeException("HTTP error: " + responseCode);
        }

        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * 解析参数字符串
     */
    protected Map<String, String> parseParams(String params) {
        Map<String, String> paramMap = new HashMap<>();
        if (params == null || params.isEmpty()) {
            return paramMap;
        }
        try {
            Map<String, String> parsed = objectMapper.readValue(params, Map.class);
            paramMap.putAll(parsed);
        } catch (Exception e) {
            log.warn("Failed to parse params: {}", params);
        }
        return paramMap;
    }

    /**
     * 获取Ray Head节点的地址
     */
    protected String getRayHeadUrl() {
        // TODO: 从数据库或配置中心获取Ray Head节点信息
        return "http://ray-head:5000";
    }

    /**
     * 解析执行轨迹
     */
    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> parseExecutionTrace(String response) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            Object traceObj = responseMap.get("execution_trace");
            if (traceObj instanceof List) {
                return (List<Map<String, Object>>) traceObj;
            }
        } catch (Exception e) {
            log.warn("Failed to parse execution trace: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 构建错误响应
     */
    protected String buildErrorResponse(String message) {
        return "{\"status\":\"error\",\"message\":\"" + message.replace("\"", "\\\"") + "\"}";
    }

    /**
     * 记录执行轨迹日志
     */
    protected void logExecutionTrace(List<Map<String, Object>> traceList) {
        if (traceList == null) return;
        log.info("========== Execution Trace ==========");
        for (Map<String, Object> trace : traceList) {
            log.info("  [{}] {} - {}: {}",
                trace.get("timestamp"),
                trace.get("step"),
                trace.get("status"),
                trace.get("message"));
        }
        log.info("======================================");
    }
}
