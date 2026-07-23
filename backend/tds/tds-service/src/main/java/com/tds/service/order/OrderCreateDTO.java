package com.tds.service.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单创建DTO
 */
public class OrderCreateDTO {

    private String productId;
    private String providerInstitutionId;
    private String providerInstitutionName;
    private String useInstitutionId;
    private String useInstitutionName;
    private String providerConnectorSn;
    private String useConnectorSn;
    private String pricingModel;
    private BigDecimal price;
    private Integer deliveryType;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private String tenantId;

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
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
    public Integer getDeliveryType() { return deliveryType; }
    public void setDeliveryType(Integer deliveryType) { this.deliveryType = deliveryType; }
    public LocalDateTime getValidStartTime() { return validStartTime; }
    public void setValidStartTime(LocalDateTime validStartTime) { this.validStartTime = validStartTime; }
    public LocalDateTime getValidEndTime() { return validEndTime; }
    public void setValidEndTime(LocalDateTime validEndTime) { this.validEndTime = validEndTime; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}