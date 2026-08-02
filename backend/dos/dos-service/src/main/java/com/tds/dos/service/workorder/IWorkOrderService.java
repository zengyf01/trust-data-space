package com.tds.dos.service.workorder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dos.dal.entity.TbWorkOrder;

/**
 * 工单服务接口
 */
public interface IWorkOrderService {

    /**
     * 分页查询工单
     */
    IPage<TbWorkOrder> getWorkOrderPage(int currentPage, int pageSize, String orderCode, Integer workOrderType, Integer workOrderStatus, String spaceId);

    /**
     * 获取工单详情
     */
    TbWorkOrder getWorkOrderById(String id);

    /**
     * 创建工单
     */
    TbWorkOrder createWorkOrder(WorkOrderCreateDTO dto);

    /**
     * 开始处理工单
     */
    TbWorkOrder startProcess(String id);

    /**
     * 按工单类型调度到对应策略执行（同步阻塞到策略完成）
     */
    void executeWithStrategy(String id);

    /**
     * 完成工单
     */
    TbWorkOrder completeWorkOrder(String id, String resultMessage, String outputFilePath, String outputFileUrl);

    /**
     * 工单失败
     */
    TbWorkOrder failWorkOrder(String id, String errorMessage);

    /**
     * 取消工单
     */
    TbWorkOrder cancelWorkOrder(String id);

    /**
     * 删除工单
     */
    void deleteWorkOrder(String id);
}