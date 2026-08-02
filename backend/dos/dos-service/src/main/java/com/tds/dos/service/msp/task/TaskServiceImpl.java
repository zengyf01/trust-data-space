package com.tds.dos.service.msp.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.common.core.PageResult;
import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.common.exception.BusinessException;
import com.tds.dos.dal.msp.entity.TbTask;
import com.tds.dos.dal.msp.mapper.TbTaskMapper;
import com.tds.dos.service.msp.executor.ITaskExecutor;
import com.tds.dos.service.msp.executor.TaskExecutorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Task Service implementation
 */
@Service
public class TaskServiceImpl implements ITaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    // 线程池用于异步执行任务
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Autowired
    private TbTaskMapper taskMapper;

    @Autowired
    private TaskExecutorFactory taskExecutorFactory;

    @Override
    public String createTask(TaskDTO dto) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        TbTask task = new TbTask();
        task.setfId(taskId);
        task.setfTaskCode("TASK" + System.currentTimeMillis());
        task.setfName(dto.getName());
        task.setfType(dto.getType() != null ? dto.getType().getCode() : TaskType.PSI.getCode());
        task.setfStatus(TaskStatus.CREATED.getCode());
        task.setfAlgorithm(dto.getAlgorithm());
        task.setfNodeMode(dto.getNodeMode() != null ? dto.getNodeMode() : "RAY");
        task.setfCreator(dto.getCreator());
        task.setfDescription(dto.getDescription());
        task.setfCreateTime(LocalDateTime.now());
        task.setfUpdateTime(LocalDateTime.now());

        if (dto.getParticipants() != null) {
            task.setfParticipants(String.join(",", dto.getParticipants()));
        }

        // 保存任务参数到fParameters字段
        if (dto.getParameters() != null && !dto.getParameters().isEmpty()) {
            try {
                task.setfParameters(new ObjectMapper().writeValueAsString(dto.getParameters()));
            } catch (Exception e) {
                log.warn("Failed to serialize task parameters: {}", e.getMessage());
            }
        }

        taskMapper.insert(task);
        log.info("Task created: {}", taskId);
        return taskId;
    }

    @Override
    public String saveDag(TaskDTO dto) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        TbTask task = new TbTask();
        task.setfId(taskId);
        task.setfTaskCode("DAG" + System.currentTimeMillis());
        task.setfName(dto.getName());
        task.setfType(dto.getType() != null ? dto.getType().getCode() : TaskType.COMPOUND_TASK.getCode());
        task.setfStatus(TaskStatus.CREATED.getCode());
        task.setfAlgorithm(dto.getAlgorithm());
        task.setfNodeMode(dto.getNodeMode() != null ? dto.getNodeMode() : "RAY");
        task.setfCreator(dto.getCreator());
        task.setfDescription(dto.getDescription());
        task.setfCode(dto.getParameters() != null ? dto.getParameters().get("dag_definition") : null);
        task.setfCreateTime(LocalDateTime.now());
        task.setfUpdateTime(LocalDateTime.now());

        taskMapper.insert(task);
        log.info("DAG saved: {}", taskId);
        return taskId;
    }

    @Override
    public boolean updateTask(String taskId, TaskDTO dto) {
        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        if (task.getfStatus() != TaskStatus.CREATED.getCode()) {
            throw new BusinessException("Only CREATED tasks can be updated");
        }

        if (StringUtils.hasLength(dto.getName())) task.setfName(dto.getName());
        if (dto.getDescription() != null) task.setfDescription(dto.getDescription());
        if (dto.getType() != null) task.setfType(dto.getType().getCode());
        task.setfUpdateTime(LocalDateTime.now());

        taskMapper.updateById(task);
        return true;
    }

    @Override
    public void executeTask(String taskId) {
        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        if (task.getfStatus() != TaskStatus.CREATED.getCode()) {
            throw new BusinessException("Only CREATED tasks can be executed");
        }

        // 状态流转: CREATED → PENDING → RUNNING
        task.setfStatus(TaskStatus.PENDING.getCode());
        task.setfUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        log.info("Task {} status changed to PENDING", taskId);

        task.setfStatus(TaskStatus.RUNNING.getCode());
        task.setfUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        log.info("Task {} status changed to RUNNING, submitting to executor", taskId);

        // 异步执行任务
        final String taskIdForAsync = taskId;
        final TbTask taskForAsync = task;
        executor.submit(() -> {
            try {
                // 使用执行器工厂获取对应的执行器
                ITaskExecutor executorInstance = taskExecutorFactory.getExecutor(taskForAsync.getfType());
                log.info("Using executor: {} for task {}", executorInstance.getClass().getSimpleName(), taskIdForAsync);

                // 执行任务
                String result = executorInstance.execute(taskIdForAsync, taskForAsync);

                // 解析执行结果
                Map<String, Object> resultMap = new ObjectMapper().readValue(result, Map.class);
                String status = (String) resultMap.get("status");

                if ("ok".equals(status)) {
                    // 更新任务状态为完成
                    taskForAsync.setfStatus(TaskStatus.COMPLETED.getCode());
                    taskForAsync.setfResult(result);
                    taskForAsync.setfUpdateTime(LocalDateTime.now());
                    taskMapper.updateById(taskForAsync);
                    log.info("Task {} completed successfully", taskIdForAsync);
                } else if ("cancelled".equals(status)) {
                    // 任务被取消：用 taskForAsync 缓存的 f_status 是 cancel 之前的（RUNNING），
                    // 若直接 updateById 会把 cancel 端点设的 CANCELLED(6) 覆盖回 RUNNING(3)。
                    // 这里只更新 result 与 update_time，f_status 用 DB 当前值。
                    TbTask fresh = taskMapper.selectById(taskIdForAsync);
                    if (fresh != null) {
                        fresh.setfResult(result);
                        fresh.setfUpdateTime(LocalDateTime.now());
                        taskMapper.updateById(fresh);
                    } else {
                        taskForAsync.setfResult(result);
                        taskForAsync.setfUpdateTime(LocalDateTime.now());
                        taskMapper.updateById(taskForAsync);
                    }
                    log.warn("Task {} was cancelled: {}", taskIdForAsync, resultMap.get("message"));
                } else {
                    // 更新任务状态为失败
                    taskForAsync.setfStatus(TaskStatus.FAILED.getCode());
                    taskForAsync.setfResult(result);
                    taskForAsync.setfUpdateTime(LocalDateTime.now());
                    taskMapper.updateById(taskForAsync);
                    log.error("Task {} failed: {}", taskIdForAsync, resultMap.get("message"));
                }
            } catch (Exception e) {
                log.error("Task {} execution failed: {}", taskIdForAsync, e.getMessage(), e);
                markTaskFailed(taskIdForAsync, e.getMessage());
            }
        });
    }

    /**
     * 标记任务失败
     */
    private void markTaskFailed(String taskId, String errorMsg) {
        TbTask task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setfStatus(TaskStatus.FAILED.getCode());
            task.setfResult("{\"status\":\"error\",\"message\":\"" + errorMsg.replace("\"", "\\\"") + "\"}");
            task.setfUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    @Override
    public TaskStatus queryStatus(String taskId) {
        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        return TaskStatus.fromCode(task.getfStatus());
    }

    @Override
    public boolean cancelTask(String taskId) {
        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        if (task.getfStatus() == TaskStatus.COMPLETED.getCode() ||
            task.getfStatus() == TaskStatus.FAILED.getCode()) {
            return false;
        }

        task.setfStatus(TaskStatus.CANCELLED.getCode());
        task.setfUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        return true;
    }

    @Override
    public void deleteTask(String taskId) {
        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        taskMapper.deleteById(taskId);
    }

    @Override
    public String retryTask(String taskId) {
        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        if (task.getfStatus() != TaskStatus.FAILED.getCode()) {
            throw new BusinessException("Only FAILED tasks can be retried");
        }

        task.setfStatus(TaskStatus.PENDING.getCode());
        task.setfUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        return taskId;
    }

    @Override
    public String copyTask(String taskId, String newName) {
        TbTask original = taskMapper.selectById(taskId);
        if (original == null) {
            throw new BusinessException("Task not found: " + taskId);
        }

        String newTaskId = UUID.randomUUID().toString().replace("-", "");
        TbTask copy = new TbTask();
        copy.setfId(newTaskId);
        copy.setfTaskCode("COPY" + System.currentTimeMillis());
        copy.setfName(newName != null ? newName : original.getfName() + " (副本)");
        copy.setfType(original.getfType());
        copy.setfStatus(TaskStatus.CREATED.getCode());
        copy.setfAlgorithm(original.getfAlgorithm());
        copy.setfNodeMode(original.getfNodeMode());
        copy.setfCreator(original.getfCreator());
        copy.setfDescription(original.getfDescription());
        copy.setfCode(original.getfCode());
        copy.setfCreateTime(LocalDateTime.now());
        copy.setfUpdateTime(LocalDateTime.now());

        taskMapper.insert(copy);
        return newTaskId;
    }

    @Override
    public TbTask getTask(String taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public PageResult<TbTask> listTasks(int page, int size, TaskStatus status, TaskType type) {
        Page<TbTask> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TbTask> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(TbTask::getfStatus, status.getCode());
        }
        if (type != null) {
            wrapper.eq(TbTask::getfType, type.getCode());
        }

        wrapper.orderByDesc(TbTask::getfCreateTime);
        IPage<TbTask> result = taskMapper.selectPage(pageParam, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public ApiResponse<String> getTaskResult(String taskId) {
        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        return ApiResponse.success(task.getfResult());
    }

    @Override
    public ApiResponse<String> getTaskExecution(String taskId) {
        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        return ApiResponse.success(task.getfExecutionLog());
    }

    @Override
    public ApiResponse<String> getTaskCode(String taskId) {
        TbTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        return ApiResponse.success(task.getfCode());
    }
}
