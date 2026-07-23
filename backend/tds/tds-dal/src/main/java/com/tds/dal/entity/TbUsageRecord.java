package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用量记录实体
 */
@TableName("tb_usage_record")
public class TbUsageRecord {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fTenantId;             // 租户ID
    private String fProductId;            // 产品ID
    private String fOrderId;              // 订单ID
    private String fUsageType;            // 用量类型：API_CALL/DATA_VOLUME/STORAGE/COMPUTE
    private Long fUsageCount;             // 用量数量
    private BigDecimal fUnitPrice;        // 单价
    private BigDecimal fAmount;           // 金额
    private String fUsagePeriod;          // 用量周期：YYYY-MM
    private LocalDateTime fUsageTime;     // 用量时间
    private String fDescription;         // 描述
    private LocalDateTime fCreateTime;    // 创建时间

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public String getfProductId() { return fProductId; }
    public void setfProductId(String fProductId) { this.fProductId = fProductId; }
    public String getfOrderId() { return fOrderId; }
    public void setfOrderId(String fOrderId) { this.fOrderId = fOrderId; }
    public String getfUsageType() { return fUsageType; }
    public void setfUsageType(String fUsageType) { this.fUsageType = fUsageType; }
    public Long getfUsageCount() { return fUsageCount; }
    public void setfUsageCount(Long fUsageCount) { this.fUsageCount = fUsageCount; }
    public BigDecimal getfUnitPrice() { return fUnitPrice; }
    public void setfUnitPrice(BigDecimal fUnitPrice) { this.fUnitPrice = fUnitPrice; }
    public BigDecimal getfAmount() { return fAmount; }
    public void setfAmount(BigDecimal fAmount) { this.fAmount = fAmount; }
    public String getfUsagePeriod() { return fUsagePeriod; }
    public void setfUsagePeriod(String fUsagePeriod) { this.fUsagePeriod = fUsagePeriod; }
    public LocalDateTime getfUsageTime() { return fUsageTime; }
    public void setfUsageTime(LocalDateTime fUsageTime) { this.fUsageTime = fUsageTime; }
    public String getfDescription() { return fDescription; }
    public void setfDescription(String fDescription) { this.fDescription = fDescription; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
}