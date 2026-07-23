package com.tds.common.dto.tunnel;

/**
 * 隧道命令执行结果
 */
public class TunnelCommandResult {

    private String sessionId;          // 会话ID
    private boolean success;           // 是否成功
    private String output;             // 命令输出
    private String errorMessage;       // 错误信息
    private long executionTimeMs;      // 执行耗时(毫秒)

    public TunnelCommandResult() {}

    public TunnelCommandResult(String sessionId, boolean success, String output, String errorMessage, long executionTimeMs) {
        this.sessionId = sessionId;
        this.success = success;
        this.output = output;
        this.errorMessage = errorMessage;
        this.executionTimeMs = executionTimeMs;
    }

    public static TunnelCommandResult success(String sessionId, String output, long executionTimeMs) {
        return new TunnelCommandResult(sessionId, true, output, null, executionTimeMs);
    }

    public static TunnelCommandResult failure(String sessionId, String errorMessage) {
        return new TunnelCommandResult(sessionId, false, null, errorMessage, 0);
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
