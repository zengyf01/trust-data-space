package com.tds.dos.service.msp.executor;

import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbTask;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * DAG任务执行器
 */
@Component
public class DagTaskExecutor extends AbstractTaskExecutor {

    @Override
    protected String doExecute(String taskId, TbTask task, Map<String, String> params) throws Exception {
        // TODO: 实现DAG任务执行逻辑
        log.info("DAG Task {} execution - TODO: implement DAG logic", taskId);

        // 模拟执行
        Thread.sleep(4000);

        return "{\"status\":\"ok\",\"task_id\":\"" + taskId + "\",\"result\":\"DAG execution completed\"}";
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.COMPOUND_TASK;
    }

    @Override
    public boolean cancel(String taskId) {
        log.warn("DAG task cancellation not implemented yet");
        return false;
    }

    @Override
    public TaskStatus queryStatus(String taskId) {
        return null;
    }
}
