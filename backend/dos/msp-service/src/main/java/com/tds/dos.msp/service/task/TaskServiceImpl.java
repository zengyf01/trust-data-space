package com.tds.dos.msp.service.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.dos.msp.common.core.ApiResponse;
import com.tds.dos.msp.common.core.PageResult;
import com.tds.dos.msp.common.enums.TaskStatus;
import com.tds.dos.msp.common.enums.TaskType;
import com.tds.dos.msp.common.exception.BusinessException;
import com.tds.dos.msp.dal.entity.TbMspTask;
import com.tds.dos.msp.dal.mapper.TbMspTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Task Service implementation
 */
@Service
public class TaskServiceImpl implements ITaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private TbMspTaskMapper taskMapper;

    @Override
    public String createTask(TaskDTO dto) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        TbMspTask task = new TbMspTask();
        task.setfId(taskId);
        task.setfTaskCode("TASK" + System.currentTimeMillis());
        task.setfName(dto.getName());
        task.setfType(dto.getType() != null ? dto.getType().getCode() : TaskType.PSI.getCode());
        task.setfStatus(TaskStatus.PENDING.getCode());
        task.setfAlgorithm(dto.getAlgorithm());
        task.setfNodeMode(dto.getNodeMode() != null ? dto.getNodeMode() : "RAY");
        task.setfCreator(dto.getCreator());
        task.setfDescription(dto.getDescription());
        task.setfCreateTime(LocalDateTime.now());
        task.setfUpdateTime(LocalDateTime.now());

        if (dto.getParticipants() != null) {
            task.setfParticipants(String.join(",", dto.getParticipants()));
        }

        taskMapper.insert(task);
        log.info("Task created: {}", taskId);
        return taskId;
    }

    @Override
    public String saveDag(TaskDTO dto) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        TbMspTask task = new TbMspTask();
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
        TbMspTask task = taskMapper.selectById(taskId);
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
        TbMspTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        if (task.getfStatus() != TaskStatus.CREATED.getCode()) {
            throw new BusinessException("Only CREATED tasks can be executed");
        }

        task.setfStatus(TaskStatus.RUNNING.getCode());
        task.setfUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        // Simulate task execution (in real implementation, this would call RayTaskExecutor or KusciaClient)
        log.info("Task execution started: {}", taskId);

        // For now, mark as completed after a delay (placeholder)
        task.setfStatus(TaskStatus.COMPLETED.getCode());
        task.setfResult("{\"status\":\"ok\",\"message\":\"Task completed\"}");
        task.setfUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Override
    public TaskStatus queryStatus(String taskId) {
        TbMspTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        return TaskStatus.fromCode(task.getfStatus());
    }

    @Override
    public boolean cancelTask(String taskId) {
        TbMspTask task = taskMapper.selectById(taskId);
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
        TbMspTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        taskMapper.deleteById(taskId);
    }

    @Override
    public String retryTask(String taskId) {
        TbMspTask task = taskMapper.selectById(taskId);
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
        TbMspTask original = taskMapper.selectById(taskId);
        if (original == null) {
            throw new BusinessException("Task not found: " + taskId);
        }

        String newTaskId = UUID.randomUUID().toString().replace("-", "");
        TbMspTask copy = new TbMspTask();
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
    public TbMspTask getTask(String taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public PageResult<TbMspTask> listTasks(int page, int size, TaskStatus status, TaskType type) {
        Page<TbMspTask> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TbMspTask> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(TbMspTask::getfStatus, status.getCode());
        }
        if (type != null) {
            wrapper.eq(TbMspTask::getfType, type.getCode());
        }

        wrapper.orderByDesc(TbMspTask::getfCreateTime);
        IPage<TbMspTask> result = taskMapper.selectPage(pageParam, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public ApiResponse<String> getTaskResult(String taskId) {
        TbMspTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        return ApiResponse.success(task.getfResult());
    }

    @Override
    public ApiResponse<String> getTaskExecution(String taskId) {
        TbMspTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        return ApiResponse.success(task.getfExecutionLog());
    }
}