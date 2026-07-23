package com.tds.datar.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.datar.common.enums.OrderStatus;
import com.tds.datar.common.enums.WorkOrderStatus;
import com.tds.datar.common.exception.BusinessException;
import com.tds.datar.dal.entity.TbOrder;
import com.tds.datar.dal.entity.TbWorkOrder;
import com.tds.datar.dal.mapper.TbOrderMapper;
import com.tds.datar.dal.mapper.TbWorkOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 订单服务实现
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;

    @Autowired
    private TbWorkOrderMapper workOrderMapper;

    @Override
    public IPage<TbOrder> getOrderPage(int currentPage, int pageSize,
            String orderCode, Integer status, String buyerSpaceId, String sellerSpaceId) {
        Page<TbOrder> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderCode != null && !orderCode.isEmpty()) {
            wrapper.eq(TbOrder::getfOrderCode, orderCode);
        }
        if (status != null) {
            wrapper.eq(TbOrder::getfStatus, status);
        }
        if (buyerSpaceId != null && !buyerSpaceId.isEmpty()) {
            wrapper.eq(TbOrder::getfBuyerSpaceId, buyerSpaceId);
        }
        if (sellerSpaceId != null && !sellerSpaceId.isEmpty()) {
            wrapper.eq(TbOrder::getfSellerSpaceId, sellerSpaceId);
        }
        wrapper.orderByDesc(TbOrder::getfCreateTime);
        return orderMapper.selectPage(page, wrapper);
    }

    @Override
    public TbOrder getOrderById(String id) {
        return orderMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbOrder createOrder(OrderDTO dto) {
        TbOrder order = new TbOrder();
        order.setfId(UUID.randomUUID().toString().replace("-", ""));
        order.setfOrderCode(generateOrderCode());
        order.setfProductId(dto.getProductId());
        order.setfBuyerTenantId(dto.getBuyerTenantId());
        order.setfSellerTenantId(dto.getSellerTenantId());
        order.setfStatus(OrderStatus.PENDING.getCode());
        order.setfTenantId(dto.getTenantId());
        order.setfBuyerSpaceId(dto.getBuyerSpaceId());
        order.setfSellerSpaceId(dto.getSellerSpaceId());
        order.setfCreateTime(LocalDateTime.now());
        order.setfUpdateTime(LocalDateTime.now());
        order.setfDeleteMark(0);

        orderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional
    public TbOrder updateOrderStatus(String id, Integer status) {
        TbOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setfStatus(status);
        order.setfUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }

    @Override
    @Transactional
    public TbOrder signOrder(String id, String contractId) {
        TbOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setfContractId(contractId);
        order.setfStatus(OrderStatus.SIGNING.getCode());
        order.setfUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }

    @Override
    @Transactional
    public void deleteOrder(String id) {
        TbOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setfDeleteMark(1);
        order.setfUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public IPage<TbWorkOrder> getWorkOrderPage(int currentPage, int pageSize,
            String orderId, Integer status) {
        Page<TbWorkOrder> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbWorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null && !orderId.isEmpty()) {
            wrapper.eq(TbWorkOrder::getfOrderId, orderId);
        }
        if (status != null) {
            wrapper.eq(TbWorkOrder::getfStatus, status);
        }
        wrapper.orderByDesc(TbWorkOrder::getfCreateTime);
        return workOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public TbWorkOrder getWorkOrderById(String id) {
        return workOrderMapper.selectById(id);
    }

    @Override
    @Transactional
    public TbWorkOrder createWorkOrder(WorkOrderDTO dto) {
        TbWorkOrder workOrder = new TbWorkOrder();
        workOrder.setfId(UUID.randomUUID().toString().replace("-", ""));
        workOrder.setfOrderId(dto.getOrderId());
        workOrder.setfWorkOrderType(dto.getWorkOrderType());
        workOrder.setfWorkOrderCode(generateWorkOrderCode());
        workOrder.setfStatus(WorkOrderStatus.PENDING.getCode());
        workOrder.setfInputParams(dto.getInputParams());
        workOrder.setfTenantId(dto.getTenantId());
        workOrder.setfSpaceId(dto.getSpaceId());
        workOrder.setfCreateTime(LocalDateTime.now());
        workOrder.setfUpdateTime(LocalDateTime.now());
        workOrder.setfDeleteMark(0);

        workOrderMapper.insert(workOrder);
        return workOrder;
    }

    @Override
    @Transactional
    public TbWorkOrder updateWorkOrderStatus(String id, Integer status,
            String outputResult, String errorMessage) {
        TbWorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }
        workOrder.setfStatus(status);
        if (outputResult != null) {
            workOrder.setfOutputResult(outputResult);
        }
        if (errorMessage != null) {
            workOrder.setfErrorMessage(errorMessage);
        }
        workOrder.setfUpdateTime(LocalDateTime.now());
        workOrderMapper.updateById(workOrder);
        return workOrder;
    }

    @Override
    @Transactional
    public void deleteWorkOrder(String id) {
        TbWorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }
        workOrder.setfDeleteMark(1);
        workOrder.setfUpdateTime(LocalDateTime.now());
        workOrderMapper.updateById(workOrder);
    }

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis();
    }

    private String generateWorkOrderCode() {
        return "WO" + System.currentTimeMillis();
    }
}