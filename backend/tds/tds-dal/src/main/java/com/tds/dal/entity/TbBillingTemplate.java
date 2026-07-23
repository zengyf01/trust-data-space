package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计费模板实体
 */
@TableName("tb_billing_template")
public class TbBillingTemplate {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fTemplateCode;         // 模板编码
    private String fTemplateName;         // 模板名称
    private String fBillingModel;         // 计费模型：FIXED/API_CALL/VOLUME/SUBSCRIPTION/CUSTOM
    private BigDecimal fBasePrice;        // 基础价格
    private BigDecimal fUnitPrice;        // 单价（按量计费时使用）
    private String fUnit;                 // 单位：次/GB/小时
    private Integer fFreeQuota;           // 免费额度
    private String fDescription;          // 描述
    private String fTenantId;            // 租户ID
    private LocalDateTime fCreateTime;    // 创建时间
    private LocalDateTime fUpdateTime;    // 更新时间
    private Integer fDeleteMark;          // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfTemplateCode() { return fTemplateCode; }
    public void setfTemplateCode(String fTemplateCode) { this.fTemplateCode = fTemplateCode; }
    public String getfTemplateName() { return fTemplateName; }
    public void setfTemplateName(String fTemplateName) { this.fTemplateName = fTemplateName; }
    public String getfBillingModel() { return fBillingModel; }
    public void setfBillingModel(String fBillingModel) { this.fBillingModel = fBillingModel; }
    public BigDecimal getfBasePrice() { return fBasePrice; }
    public void setfBasePrice(BigDecimal fBasePrice) { this.fBasePrice = fBasePrice; }
    public BigDecimal getfUnitPrice() { return fUnitPrice; }
    public void setfUnitPrice(BigDecimal fUnitPrice) { this.fUnitPrice = fUnitPrice; }
    public String getfUnit() { return fUnit; }
    public void setfUnit(String fUnit) { this.fUnit = fUnit; }
    public Integer getfFreeQuota() { return fFreeQuota; }
    public void setfFreeQuota(Integer fFreeQuota) { this.fFreeQuota = fFreeQuota; }
    public String getfDescription() { return fDescription; }
    public void setfDescription(String fDescription) { this.fDescription = fDescription; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}