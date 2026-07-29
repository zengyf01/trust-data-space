package com.tds.dos.service.msp.executor;

import com.tds.dos.common.enums.TaskStatus;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 任务执行结果
 */
@Data
public class TaskResult {
    private boolean success;
    private TaskStatus status;
    private String message;
    private String resultData;
    private List<Map<String, Object>> executionTrace;

    public static TaskResult ok(String resultData, List<Map<String, Object>> trace) {
        TaskResult r = new TaskResult();
        r.setSuccess(true);
        r.setStatus(TaskStatus.COMPLETED);
        r.setResultData(resultData);
        r.setExecutionTrace(trace);
        return r;
    }

    public static TaskResult fail(String message) {
        TaskResult r = new TaskResult();
        r.setSuccess(false);
        r.setStatus(TaskStatus.FAILED);
        r.setMessage(message);
        return r;
    }
}
