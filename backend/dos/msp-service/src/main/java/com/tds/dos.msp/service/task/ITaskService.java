package com.tds.dos.msp.service.task;

import com.tds.dos.msp.common.core.PageResult;
import com.tds.dos.msp.common.core.ApiResponse;
import com.tds.dos.msp.common.enums.TaskStatus;
import com.tds.dos.msp.common.enums.TaskType;
import com.tds.dos.msp.dal.entity.TbMspTask;

/**
 * Task Service interface
 */
public interface ITaskService {
    String createTask(TaskDTO dto);
    String saveDag(TaskDTO dto);
    boolean updateTask(String taskId, TaskDTO dto);
    void executeTask(String taskId);
    TaskStatus queryStatus(String taskId);
    boolean cancelTask(String taskId);
    void deleteTask(String taskId);
    String retryTask(String taskId);
    String copyTask(String taskId, String newName);
    TbMspTask getTask(String taskId);
    PageResult<TbMspTask> listTasks(int page, int size, TaskStatus status, TaskType type);
    ApiResponse<String> getTaskResult(String taskId);
    ApiResponse<String> getTaskExecution(String taskId);
}