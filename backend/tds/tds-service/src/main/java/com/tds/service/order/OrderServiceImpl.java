package com.tds.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.enums.OrderPayStatus;
import com.tds.common.enums.OrderStatus;
import com.tds.common.enums.ProductStatus;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbDataProduct;
import com.tds.dal.entity.TbOrderHistory;
import com.tds.dal.entity.TbTradingOrder;
import com.tds.dal.mapper.TbDataProductMapper;
import com.tds.dal.mapper.TbOrderHistoryMapper;
import com.tds.dal.mapper.TbTradingOrderMapper;
import com.tds.service.contract.ContractCreateDTO;
import com.tds.service.contract.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务实现
 */
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private TbTradingOrderMapper orderMapper;

    @Autowired
    private TbOrderHistoryMapper historyMapper;

    @Autowired
    private TbDataProductMapper productMapper;

    @Autowired
    private ContractService contractService;

    @Override
    public IPage<TbTradingOrder> getOrderPage(int currentPage, int pageSize,
            String orderCode, Integer orderStatus) {
        Page<TbTradingOrder> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbTradingOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderCode != null && !orderCode.isEmpty()) {
            wrapper.like(TbTradingOrder::getOrderCode, orderCode);
        }
        if (orderStatus != null) {
            wrapper.eq(TbTradingOrder::getOrderStatus, orderStatus);
        }
        wrapper.orderByDesc(TbTradingOrder::getfCreateTime);
        return orderMapper.selectPage(page, wrapper);
    }

    @Override
    public TbTradingOrder getOrderById(String id) {
        return orderMapper.selectById(id);
    }

    @Override
    public TbTradingOrder getOrderByCode(String orderCode) {
        LambdaQueryWrapper<TbTradingOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbTradingOrder::getOrderCode, orderCode);
        return orderMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public TbTradingOrder createOrder(OrderCreateDTO dto) {
        TbTradingOrder order = new TbTradingOrder();
        order.setId(UUID.randomUUID().toString().replace("-", ""));
        order.setOrderCode(generateOrderCode());

        // 获取产品信息并保存快照
        TbDataProduct product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        order.setProductId(product.getId());
        order.setProductCode(product.getProductCode());
        order.setProductName(product.getProductName());
        order.setProductSnapshot(buildProductSnapshot(product));

        order.setProviderInstitutionId(dto.getProviderInstitutionId());
        order.setProviderInstitutionName(dto.getProviderInstitutionName());
        order.setUseInstitutionId(dto.getUseInstitutionId());
        order.setUseInstitutionName(dto.getUseInstitutionName());
        order.setProviderConnectorSn(dto.getProviderConnectorSn());
        order.setUseConnectorSn(dto.getUseConnectorSn());
        order.setPricingModel(dto.getPricingModel());
        order.setPrice(dto.getPrice());
        order.setOrderStatus(OrderStatus.PENDING.getCode());
        order.setPayStatus(OrderPayStatus.UNPAID.getCode());
        order.setDeliveryType(dto.getDeliveryType());
        order.setValidStartTime(dto.getValidStartTime());
        order.setValidEndTime(dto.getValidEndTime());
        order.setfTenantId(dto.getTenantId());
        order.setfCreateTime(LocalDateTime.now());
        order.setfUpdateTime(LocalDateTime.now());
        order.setfDeleteMark(0);

        orderMapper.insert(order);

        // 记录历史
        saveOrderHistory(order, "CREATE", "创建订单", null, OrderStatus.PENDING.getCode(), dto.getTenantId());

        return order;
    }

    @Override
    @Transactional
    public TbTradingOrder approveOrder(String id, String approver, String remark) throws Exception {
        TbTradingOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING.getCode()) {
            throw new BusinessException("只有待审批状态可以审批");
        }

        Integer fromStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.APPROVED.getCode());
        order.setApprover(approver);
        order.setApproveTime(LocalDateTime.now());
        order.setApproveRemark(remark);
        order.setfUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 审核通过后创建数字合约
        createContractForOrder(order);

        // 记录历史
        saveOrderHistory(order, "APPROVE", "审批通过", fromStatus, OrderStatus.APPROVED.getCode(), approver);

        return order;
    }

    @Override
    @Transactional
    public TbTradingOrder rejectOrder(String id, String reason) {
        TbTradingOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING.getCode()) {
            throw new BusinessException("只有待审批状态可以拒绝");
        }

        Integer fromStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.REJECTED.getCode());
        order.setRejectReason(reason);
        order.setfUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 记录历史
        saveOrderHistory(order, "REJECT", "审批拒绝:" + reason, fromStatus, OrderStatus.REJECTED.getCode(), null);

        return order;
    }

    @Override
    @Transactional
    public TbTradingOrder cancelOrder(String id) {
        TbTradingOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() == OrderStatus.COMPLETED.getCode() ||
            order.getOrderStatus() == OrderStatus.CANCELLED.getCode()) {
            throw new BusinessException("当前状态不允许取消");
        }

        Integer fromStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.CANCELLED.getCode());
        order.setfUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 记录历史
        saveOrderHistory(order, "CANCEL", "取消订单", fromStatus, OrderStatus.CANCELLED.getCode(), null);

        return order;
    }

    @Override
    public List<TbOrderHistory> getOrderHistory(String orderId) {
        LambdaQueryWrapper<TbOrderHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbOrderHistory::getOrderId, orderId)
              .orderByAsc(TbOrderHistory::getOperateTime);
        return historyMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public TbTradingOrder updateDeliveryApiInfo(String id, String deliveryApiInfo) {
        TbTradingOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setDeliveryApiInfo(deliveryApiInfo);
        order.setfUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }

    /**
     * 为订单创建数字合约
     */
    private void createContractForOrder(TbTradingOrder order) throws Exception {
        ContractCreateDTO dto = new ContractCreateDTO();
        dto.setOrderCode(order.getOrderCode());
        dto.setContractJson(order.getProductSnapshot());
        dto.setResourceSnapshot(order.getProductSnapshot());
        dto.setProviderInstitutionId(order.getProviderInstitutionId());
        dto.setProviderInstitutionName(order.getProviderInstitutionName());
        dto.setUseInstitutionId(order.getUseInstitutionId());
        dto.setUseInstitutionName(order.getUseInstitutionName());
        dto.setProviderConnectorAddress(order.getProviderConnectorSn());
        dto.setUseConnectorAddress(order.getUseConnectorSn());
        dto.setTenantId(order.getfTenantId());
        dto.setContractStartTime(order.getValidStartTime());
        dto.setContractEndTime(order.getValidEndTime());

        contractService.createContract(dto);
    }

    /**
     * 保存订单历史
     */
    private void saveOrderHistory(TbTradingOrder order, String operateType, String operateDesc,
            Integer fromStatus, Integer toStatus, String operator) {
        TbOrderHistory history = new TbOrderHistory();
        history.setId(UUID.randomUUID().toString().replace("-", ""));
        history.setOrderId(order.getId());
        history.setOrderCode(order.getOrderCode());
        history.setOperateType(operateType);
        history.setOperateDesc(operateDesc);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setOperator(operator);
        history.setOperateTime(LocalDateTime.now());
        history.setfTenantId(order.getfTenantId());
        history.setfCreateTime(LocalDateTime.now());
        history.setfDeleteMark(0);
        historyMapper.insert(history);
    }

    /**
     * 构建产品快照JSON
     */
    private String buildProductSnapshot(TbDataProduct product) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"productCode\":\"").append(product.getProductCode()).append("\",");
        sb.append("\"productName\":\"").append(product.getProductName()).append("\",");
        sb.append("\"productDesc\":\"").append(product.getProductDesc() != null ? product.getProductDesc() : "").append("\",");
        sb.append("\"pricingModel\":\"").append(product.getPricingModel()).append("\",");
        sb.append("\"price\":").append(product.getPrice()).append("");
        sb.append("}");
        return sb.toString();
    }

    /**
     * 生成订单编号
     */
    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis();
    }
}