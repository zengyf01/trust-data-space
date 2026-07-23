package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易订单实体
 */
@TableName("tb_trading_order")
public class TbTradingOrder {

    @TableId
    private String id;

    /** 订单编号 */
    private String orderCode;

    /** 关联产品ID */
    private String productId;

    /** 产品编号 */
    private String productCode;

    /** 产品名称 */
    private String productName;

    /** 产品快照JSON */
    private String productSnapshot;

    /** 供方机构ID */
    private String providerInstitutionId;

    /** 供方机构名称 */
    private String providerInstitutionName;

    /** 需方机构ID */
    private String useInstitutionId;

    /** 需方机构名称 */
    private String useInstitutionName;

    /** 供方连接器编号 */
    private String providerConnectorSn;

    /** 需方连接器编号 */
    private String useConnectorSn;

    /** 计费模式 */
    private String pricingModel;

    /** 价格 */
    private BigDecimal price;

    /** 订单状态(1待审批,2已审批,3签署中,4执行中,5已完成,6已驳回,7已取消) */
    private Integer orderStatus;

    /** 支付状态(1未支付,2已支付,3已退款) */
    private Integer payStatus;

    /** 交付类型(1数据服务,2安全沙盒,3隐私计算) */
    private Integer deliveryType;

    /** 交付API信息 */
    private String deliveryApiInfo;

    /** 订单有效期开始 */
    private LocalDateTime validStartTime;

    /** 订单有效期结束 */
    private LocalDateTime validEndTime;

    /** 驳回原因 */
    private String rejectReason;

    /** 审批人 */
    private String approver;

    /** 审批时间 */
    private LocalDateTime approveTime;

    /** 审批备注 */
    private String approveRemark;

    /** 租户ID */
    private String fTenantId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    /** 删除标志(0未删,1已删) */
    @TableLogic
    private Integer fDeleteMark;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductSnapshot() { return productSnapshot; }
    public void setProductSnapshot(String productSnapshot) { this.productSnapshot = productSnapshot; }
    public String getProviderInstitutionId() { return providerInstitutionId; }
    public void setProviderInstitutionId(String providerInstitutionId) { this.providerInstitutionId = providerInstitutionId; }
    public String getProviderInstitutionName() { return providerInstitutionName; }
    public void setProviderInstitutionName(String providerInstitutionName) { this.providerInstitutionName = providerInstitutionName; }
    public String getUseInstitutionId() { return useInstitutionId; }
    public void setUseInstitutionId(String useInstitutionId) { this.useInstitutionId = useInstitutionId; }
    public String getUseInstitutionName() { return useInstitutionName; }
    public void setUseInstitutionName(String useInstitutionName) { this.useInstitutionName = useInstitutionName; }
    public String getProviderConnectorSn() { return providerConnectorSn; }
    public void setProviderConnectorSn(String providerConnectorSn) { this.providerConnectorSn = providerConnectorSn; }
    public String getUseConnectorSn() { return useConnectorSn; }
    public void setUseConnectorSn(String useConnectorSn) { this.useConnectorSn = useConnectorSn; }
    public String getPricingModel() { return pricingModel; }
    public void setPricingModel(String pricingModel) { this.pricingModel = pricingModel; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }
    public Integer getPayStatus() { return payStatus; }
    public void setPayStatus(Integer payStatus) { this.payStatus = payStatus; }
    public Integer getDeliveryType() { return deliveryType; }
    public void setDeliveryType(Integer deliveryType) { this.deliveryType = deliveryType; }
    public String getDeliveryApiInfo() { return deliveryApiInfo; }
    public void setDeliveryApiInfo(String deliveryApiInfo) { this.deliveryApiInfo = deliveryApiInfo; }
    public LocalDateTime getValidStartTime() { return validStartTime; }
    public void setValidStartTime(LocalDateTime validStartTime) { this.validStartTime = validStartTime; }
    public LocalDateTime getValidEndTime() { return validEndTime; }
    public void setValidEndTime(LocalDateTime validEndTime) { this.validEndTime = validEndTime; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }
    public LocalDateTime getApproveTime() { return approveTime; }
    public void setApproveTime(LocalDateTime approveTime) { this.approveTime = approveTime; }
    public String getApproveRemark() { return approveRemark; }
    public void setApproveRemark(String approveRemark) { this.approveRemark = approveRemark; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}