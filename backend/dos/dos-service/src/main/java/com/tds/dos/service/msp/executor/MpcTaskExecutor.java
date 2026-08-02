package com.tds.dos.service.msp.executor;

import com.tds.dos.common.enums.TaskStatus;
import com.tds.dos.common.enums.TaskType;
import com.tds.dos.dal.msp.entity.TbTask;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MPC 安全多方计算任务执行器
 *
 * <p>状态说明：MpcCodeGenerator 已删除（其用的 SecretFlow API——sf.get_party / sf.read_csv /
 * 自由函数 sf.sum/mean/min/max 等——均不存在），且本执行器没有实测可用的实现方案。
 * 保留 @Component 仅为不让 TaskType.MPC 任务创建时因找不到执行器而抛 factory 异常，
 * 实际执行时直接抛错让调用方立刻看到失败原因。如未来要真正实现 MPC，请：
 * <ol>
 *   <li>在 SecretFlow 1.13 lite 文档中确认 MPC API（JPU/SPU/HEU 组合）</li>
 *   <li>实现一个真正的代码生成器与执行器，并走 PsiTaskExecutor 同款的两方 PRODUCTION 模式</li>
 * </ol>
 */
@Component
public class MpcTaskExecutor extends AbstractTaskExecutor {

    @Override
    protected String doExecute(String taskId, TbTask task, Map<String, String> params) throws Exception {
        throw new UnsupportedOperationException(
            "MPC 安全多方计算暂未实现（MpcCodeGenerator 已删除，SecretFlow lite 的 MPC API 未实测验证）。"
                + "请改用 PSI 求交（POST /api/dos/privacy/psi/executeWithResult）。");
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.MPC;
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
