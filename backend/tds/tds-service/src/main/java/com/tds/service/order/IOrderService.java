package com.tds.service.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dal.entity.TbOrderHistory;
import com.tds.dal.entity.TbTradingOrder;

import java.util.List;

/**
 * 订单服务接口
 */
public interface IOrderService {

    /**
     * 分页查询订单
     */
    IPage<TbTradingOrder> getOrderPage(int currentPage, int pageSize, String orderCode, Integer orderStatus);

    /**
     * 获取订单详情
     */
    TbTradingOrder getOrderById(String id);

    /**
     * 获取订单详情（按订单编号）
     */
    TbTradingOrder getOrderByCode(String orderCode);

    /**
     * 创建订单
     */
    TbTradingOrder createOrder(OrderCreateDTO dto);

    /**
     * 审核通过
     */
    TbTradingOrder approveOrder(String id, String approver, String remark) throws Exception;

    /**
     * 审核拒绝
     */
    TbTradingOrder rejectOrder(String id, String reason);

    /**
     * 取消订单
     */
    TbTradingOrder cancelOrder(String id);

    /**
     * 获取订单历史
     */
    List<TbOrderHistory> getOrderHistory(String orderId);

    /**
     * 更新交付API信息
     */
    TbTradingOrder updateDeliveryApiInfo(String id, String deliveryApiInfo);
}