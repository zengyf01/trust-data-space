package com.tds.service.billing;

import java.math.BigDecimal;

/**
 * 计费模板DTO
 */
public class BillingTemplateDTO {

    private String id;
    private String templateCode;
    private String templateName;
    private String billingModel;
    private BigDecimal basePrice;
    private BigDecimal unitPrice;
    private String unit;
    private Integer freeQuota;
    private String description;
    private String tenantId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getBillingModel() { return billingModel; }
    public void setBillingModel(String billingModel) { this.billingModel = billingModel; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getFreeQuota() { return freeQuota; }
    public void setFreeQuota(Integer freeQuota) { this.freeQuota = freeQuota; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}