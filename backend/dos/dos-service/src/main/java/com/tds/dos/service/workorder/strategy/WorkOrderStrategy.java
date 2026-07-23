package com.tds.dos.service.workorder.strategy;

/**
 * 工单策略接口
 */
public interface WorkOrderStrategy {

    /**
     * 获取工单类型
     */
    String getWorkOrderType();

    /**
     * 预处理
     */
    void preProcess(String workOrderId);

    /**
     * 执行工单
     */
    void execute(String workOrderId);

    /**
     * 取消工单
     */
    void cancel(String workOrderId);
}