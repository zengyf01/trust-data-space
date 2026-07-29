package com.tds.dos.service.privacycompute.code;

import java.util.Map;

/**
 * 隐私计算代码生成器接口
 * 统一的任务代码生成接口，不同任务类型有不同的实现
 */
public interface ICodeGenerator {

    /**
     * 获取支持的任务类型
     * @return 任务类型标识，如 "PSI", "MPC", "FEDERATED_LEARNING"
     */
    String getTaskType();

    /**
     * 生成Python执行代码
     * @param taskId 任务ID
     * @param params 任务参数
     * @return 生成的Python代码
     */
    String generateCode(String taskId, Map<String, Object> params);

    /**
     * 验证参数是否完整
     * @param params 任务参数
     * @return 验证通过返回null，否则返回错误信息
     */
    String validateParams(Map<String, Object> params);
}
