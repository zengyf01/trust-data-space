package com.tds.service.billing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用量记录DTO
 */
public class UsageRecordDTO {

    private String id;
    private String tenantId;
    private String productId;
    private String orderId;
    private String usageType;
    private Long usageCount;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String usagePeriod;
    private LocalDateTime usageTime;
    private String description;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUsageType() { return usageType; }
    public void setUsageType(String usageType) { this.usageType = usageType; }
    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getUsagePeriod() { return usagePeriod; }
    public void setUsagePeriod(String usagePeriod) { this.usagePeriod = usagePeriod; }
    public LocalDateTime getUsageTime() { return usageTime; }
    public void setUsageTime(LocalDateTime usageTime) { this.usageTime = usageTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}