package com.tds.dos.service.msp.executor;

import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbTask;

/**
 * 任务执行器接口
 * 使用策略模式，每种任务类型有独立的执行器
 */
public interface ITaskExecutor {

    /**
     * 执行任务
     * @param taskId 任务ID
     * @param task 任务实体
     * @return 执行结果JSON字符串
     */
    String execute(String taskId, TbTask task);

    /**
     * 获取任务类型
     */
    TaskType getTaskType();

    /**
     * 取消任务
     * @param taskId 任务ID
     * @return 是否取消成功
     */
    boolean cancel(String taskId);

    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    TaskStatus queryStatus(String taskId);
}
