package com.tds.dos.service.workorder.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 工单策略工厂
 */
@Component
public class WorkOrderStrategyFactory {

    private final Map<String, WorkOrderStrategy> strategyMap = new HashMap<>();

    @Autowired
    public WorkOrderStrategyFactory(List<WorkOrderStrategy> strategies) {
        for (WorkOrderStrategy strategy : strategies) {
            strategyMap.put(strategy.getWorkOrderType(), strategy);
        }
    }

    /**
     * 获取策略
     */
    public WorkOrderStrategy getStrategy(String workOrderType) {
        WorkOrderStrategy strategy = strategyMap.get(workOrderType);
        if (strategy == null) {
            throw new RuntimeException("不支持的工单类型: " + workOrderType);
        }
        return strategy;
    }
}