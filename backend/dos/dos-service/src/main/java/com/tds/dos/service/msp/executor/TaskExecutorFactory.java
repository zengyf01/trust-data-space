package com.tds.dos.service.msp.executor;

import com.tds.dos.common.enums.TaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 任务执行器工厂
 * 根据任务类型创建对应的执行器
 */
@Component
public class TaskExecutorFactory {

    private final Map<TaskType, ITaskExecutor> executorMap = new EnumMap<>(TaskType.class);

    @Autowired
    public TaskExecutorFactory(List<ITaskExecutor> executors) {
        for (ITaskExecutor executor : executors) {
            executorMap.put(executor.getTaskType(), executor);
            System.out.println("Registered executor: " + executor.getClass().getSimpleName() + " for task type: " + executor.getTaskType());
        }
    }

    /**
     * 根据任务类型获取执行器
     * @param taskType 任务类型
     * @return 对应的执行器
     */
    public ITaskExecutor getExecutor(TaskType taskType) {
        ITaskExecutor executor = executorMap.get(taskType);
        if (executor == null) {
            throw new IllegalArgumentException("No executor found for task type: " + taskType);
        }
        return executor;
    }

    /**
     * 根据任务类型获取执行器
     * @param taskTypeCode 任务类型码
     * @return 对应的执行器
     */
    public ITaskExecutor getExecutor(Integer taskTypeCode) {
        TaskType taskType = TaskType.fromCode(taskTypeCode);
        return getExecutor(taskType);
    }

    /**
     * 检查是否存在指定任务类型的执行器
     */
    public boolean hasExecutor(TaskType taskType) {
        return executorMap.containsKey(taskType);
    }
}
