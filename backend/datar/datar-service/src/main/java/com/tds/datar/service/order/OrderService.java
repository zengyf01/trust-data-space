package com.tds.datar.service.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.datar.dal.entity.TbOrder;
import com.tds.datar.dal.entity.TbWorkOrder;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 分页查询订单
     */
    IPage<TbOrder> getOrderPage(int currentPage, int pageSize, String orderCode, Integer status, String buyerSpaceId, String sellerSpaceId);

    /**
     * 获取订单详情
     */
    TbOrder getOrderById(String id);

    /**
     * 创建订单
     */
    TbOrder createOrder(OrderDTO dto);

    /**
     * 更新订单状态
     */
    TbOrder updateOrderStatus(String id, Integer status);

    /**
     * 签约订单
     */
    TbOrder signOrder(String id, String contractId);

    /**
     * 删除订单
     */
    void deleteOrder(String id);

    /**
     * 分页查询工单
     */
    IPage<TbWorkOrder> getWorkOrderPage(int currentPage, int pageSize, String orderId, Integer status);

    /**
     * 获取工单详情
     */
    TbWorkOrder getWorkOrderById(String id);

    /**
     * 创建工单
     */
    TbWorkOrder createWorkOrder(WorkOrderDTO dto);

    /**
     * 更新工单状态
     */
    TbWorkOrder updateWorkOrderStatus(String id, Integer status, String outputResult, String errorMessage);

    /**
     * 删除工单
     */
    void deleteWorkOrder(String id);
}