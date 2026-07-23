package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品定价实体
 */
@TableName("tb_product_pricing")
public class TbProductPricing {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fProductId;             // 产品ID
    private String fTemplateId;            // 计费模板ID
    private String fBillingModel;         // 计费模型
    private BigDecimal fPrice;            // 定价
    private BigDecimal fUnitPrice;        // 单价
    private String fUnit;                 // 单位
    private Integer fMinQuota;            // 最小购买量
    private Integer fMaxQuota;           // 最大购买量
    private LocalDateTime fStartTime;     // 生效时间
    private LocalDateTime fEndTime;       // 失效时间
    private String fTenantId;            // 租户ID
    private LocalDateTime fCreateTime;    // 创建时间
    private LocalDateTime fUpdateTime;    // 更新时间
    private Integer fDeleteMark;          // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfProductId() { return fProductId; }
    public void setfProductId(String fProductId) { this.fProductId = fProductId; }
    public String getfTemplateId() { return fTemplateId; }
    public void setfTemplateId(String fTemplateId) { this.fTemplateId = fTemplateId; }
    public String getfBillingModel() { return fBillingModel; }
    public void setfBillingModel(String fBillingModel) { this.fBillingModel = fBillingModel; }
    public BigDecimal getfPrice() { return fPrice; }
    public void setfPrice(BigDecimal fPrice) { this.fPrice = fPrice; }
    public BigDecimal getfUnitPrice() { return fUnitPrice; }
    public void setfUnitPrice(BigDecimal fUnitPrice) { this.fUnitPrice = fUnitPrice; }
    public String getfUnit() { return fUnit; }
    public void setfUnit(String fUnit) { this.fUnit = fUnit; }
    public Integer getfMinQuota() { return fMinQuota; }
    public void setfMinQuota(Integer fMinQuota) { this.fMinQuota = fMinQuota; }
    public Integer getfMaxQuota() { return fMaxQuota; }
    public void setfMaxQuota(Integer fMaxQuota) { this.fMaxQuota = fMaxQuota; }
    public LocalDateTime getfStartTime() { return fStartTime; }
    public void setfStartTime(LocalDateTime fStartTime) { this.fStartTime = fStartTime; }
    public LocalDateTime getfEndTime() { return fEndTime; }
    public void setfEndTime(LocalDateTime fEndTime) { this.fEndTime = fEndTime; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}