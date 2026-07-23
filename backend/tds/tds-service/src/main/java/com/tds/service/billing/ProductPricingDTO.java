package com.tds.service.billing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品定价DTO
 */
public class ProductPricingDTO {

    private String id;
    private String productId;
    private String templateId;
    private String billingModel;
    private BigDecimal price;
    private BigDecimal unitPrice;
    private String unit;
    private Integer minQuota;
    private Integer maxQuota;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String tenantId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getBillingModel() { return billingModel; }
    public void setBillingModel(String billingModel) { this.billingModel = billingModel; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getMinQuota() { return minQuota; }
    public void setMinQuota(Integer minQuota) { this.minQuota = minQuota; }
    public Integer getMaxQuota() { return maxQuota; }
    public void setMaxQuota(Integer maxQuota) { this.maxQuota = maxQuota; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}