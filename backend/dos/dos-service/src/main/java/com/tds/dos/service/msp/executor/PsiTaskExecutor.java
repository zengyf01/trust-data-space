package com.tds.dos.service.msp.executor;

import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbTask;
import com.tds.dos.service.psi.IRayJobSubmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * PSI求交任务执行器
 * 使用已生成的SecretFlow Python代码提交到Ray集群执行
 */
@Component
public class PsiTaskExecutor extends AbstractTaskExecutor {

    @Autowired
    private IRayJobSubmitter rayJobSubmitter;

    // 默认超时时间30分钟
    private static final long DEFAULT_TIMEOUT_MS = 30 * 60 * 1000;

    @Override
    protected String doExecute(String taskId, TbTask task, Map<String, String> params) throws Exception {
        // 从任务中获取已生成的Python代码
        String pythonCode = task.getfCode();
        String protocol = params.getOrDefault("protocol", "ECPSI");

        log.info("=== PSI Task Execution ===");
        log.info("Task ID: {}", taskId);
        log.info("Protocol: {}", protocol);

        if (pythonCode == null || pythonCode.isEmpty()) {
            throw new RuntimeException("Python code not found in task, please regenerate the task");
        }
        log.info("Using pre-generated Python code, length: {} characters", pythonCode.length());

        // Step 1: 提交到Ray集群
        log.info("Step 1: Submitting job to Ray cluster...");
        String rayHeadUrl = getRayHeadUrl();
        String jobName = "psi-task-" + taskId;
        String jobId = rayJobSubmitter.submitJob(rayHeadUrl, jobName, pythonCode);
        log.info("Job submitted successfully, job_id: {}", jobId);

        // Step 2: 等待作业完成
        log.info("Step 2: Waiting for job completion...");
        IRayJobSubmitter.JobStatus status = rayJobSubmitter.waitForCompletion(jobId, DEFAULT_TIMEOUT_MS);
        log.info("Job finished with status: {}", status);

        // Step 3: 获取执行日志
        log.info("Step 3: Retrieving job logs...");
        String logs = rayJobSubmitter.getJobLogs(jobId);
        log.info("Job logs:\n{}", logs);

        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("task_id", taskId);
        result.put("job_id", jobId);
        result.put("status", status == IRayJobSubmitter.JobStatus.SUCCEEDED ? "ok" : "error");
        result.put("ray_head_url", rayHeadUrl);
        result.put("protocol", protocol);
        result.put("logs", logs);

        if (status == IRayJobSubmitter.JobStatus.SUCCEEDED) {
            result.put("message", "PSI task completed successfully");
        } else if (status == IRayJobSubmitter.JobStatus.FAILED) {
            result.put("message", "PSI task failed");
        } else {
            result.put("message", "PSI task timeout or cancelled");
        }

        return objectMapper.writeValueAsString(result);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.PSI;
    }

    @Override
    public boolean cancel(String taskId) {
        // TODO: 需要通过Ray API取消正在运行的作业
        log.warn("PSI task cancellation not fully implemented yet");
        return false;
    }

    @Override
    public TaskStatus queryStatus(String taskId) {
        // PSI任务状态由TaskService管理
        return null;
    }
}
