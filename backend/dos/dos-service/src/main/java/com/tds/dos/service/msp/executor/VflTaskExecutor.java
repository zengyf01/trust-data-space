package com.tds.dos.service.msp.executor;

import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbTask;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 纵向联邦学习任务执行器
 *
 * <p>状态说明：原实现是 Thread.sleep(3500) + 返回伪造的 success JSON，从未真正执行过纵向联邦学习。
 * 现在直接抛错，避免用户被假成功误导。
 */
@Component
public class VflTaskExecutor extends AbstractTaskExecutor {

    @Override
    protected String doExecute(String taskId, TbTask task, Map<String, String> params) throws Exception {
        throw new UnsupportedOperationException(
            "纵向联邦学习（VFL）暂未实现（原 VflTaskExecutor 是 sleep+假成功 stub）。"
                + "请改用 PSI 求交（POST /api/dos/privacy/psi/executeWithResult）。");
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.VERTICAL_FL;
    }

    @Override
    public boolean cancel(String taskId) {
        return false;
    }

    @Override
    public TaskStatus queryStatus(String taskId) {
        return null;
    }
}
