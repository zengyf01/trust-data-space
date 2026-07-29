package com.tds.dos.service.msp.task;

import com.tds.dos.common.core.PageResult;
import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbTask;

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
    TbTask getTask(String taskId);
    PageResult<TbTask> listTasks(int page, int size, TaskStatus status, TaskType type);
    ApiResponse<String> getTaskResult(String taskId);
    ApiResponse<String> getTaskExecution(String taskId);
    ApiResponse<String> getTaskCode(String taskId);
}
