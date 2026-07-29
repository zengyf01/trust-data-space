package com.tds.dos.service.privacycompute.code;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成器工厂
 * 根据任务类型获取对应的代码生成器
 */
@Slf4j
@Component
public class CodeGeneratorFactory {

    private final Map<String, ICodeGenerator> generators = new HashMap<>();

    @Autowired
    public CodeGeneratorFactory(List<ICodeGenerator> codeGenerators) {
        for (ICodeGenerator generator : codeGenerators) {
            generators.put(generator.getTaskType().toUpperCase(), generator);
            log.info("Registered code generator for task type: {}", generator.getTaskType());
        }
    }

    /**
     * 根据任务类型获取代码生成器
     * @param taskType 任务类型，如 "PSI", "MPC", "FEDERATED_LEARNING"
     * @return 对应的代码生成器
     * @throws IllegalArgumentException 如果没有找到对应类型的生成器
     */
    public ICodeGenerator getGenerator(String taskType) {
        if (taskType == null || taskType.isEmpty()) {
            throw new IllegalArgumentException("任务类型不能为空");
        }

        ICodeGenerator generator = generators.get(taskType.toUpperCase());
        if (generator == null) {
            throw new IllegalArgumentException("不支持的任务类型: " + taskType
                + ", 可用类型: " + generators.keySet());
        }
        return generator;
    }

    /**
     * 检查是否支持指定的任务类型
     * @param taskType 任务类型
     * @return 是否支持
     */
    public boolean isSupported(String taskType) {
        return taskType != null && generators.containsKey(taskType.toUpperCase());
    }

    /**
     * 获取所有支持的任务类型
     * @return 任务类型列表
     */
    public java.util.Set<String> getSupportedTaskTypes() {
        return generators.keySet();
    }
}
