package com.tds.dos.service.msp.executor;

import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbTask;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 联邦学习任务执行器
 *
 * <p>状态说明：原实现走的是通过 Ray Dashboard 提交到旧 ray_head 的路径，
 * 且 FlCodeGenerator 用的 SecretFlow API（FLModel/SecureAggregator 配 SPU 设备等）未实测验证。
 * FlCodeGenerator 已删除。保留 @Component 仅为
 * 不让 TaskType.FEDERATED_LEARNING 任务创建时因找不到执行器而抛 factory 异常，
 * 实际执行时直接抛错让调用方立刻看到失败原因。
 */
@Component
public class FlTaskExecutor extends AbstractTaskExecutor {

    @Override
    protected String doExecute(String taskId, TbTask task, Map<String, String> params) throws Exception {
        throw new UnsupportedOperationException(
            "联邦学习（FL）暂未实现（FlCodeGenerator 已删除，原路径走的是过时的 Ray Dashboard 提交，"
                + "SecretFlow lite 的 FLModel/SecureAggregator 配 SPU 设备也未实测验证）。"
                + "请改用 PSI 求交（POST /api/dos/privacy/psi/executeWithResult）。");
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.FEDERATED_LEARNING;
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
