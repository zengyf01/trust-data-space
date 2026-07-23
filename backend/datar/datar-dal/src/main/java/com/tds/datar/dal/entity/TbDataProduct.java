package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("tb_data_product")
public class TbDataProduct {

    @TableId(value = "f_id")
    private String id;

    @TableField("f_product_code")
    private String productCode;

    @TableField("f_product_name")
    private String productName;

    @TableField("f_catalog_id")
    private String catalogId;

    @TableField("f_product_desc")
    private String productDesc;

    @TableField("f_pricing_model")
    private String pricingModel;

    @TableField("f_price")
    private BigDecimal price;

    @TableField("f_status")
    private Integer status;

    @TableField("f_publish_time")
    private LocalDateTime publishTime;

    @TableField("f_tenant_id")
    private String tenantId;

    @TableField("f_space_id")
    private String spaceId;

    @TableField(value = "f_create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "f_update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("f_delete_mark")
    private Integer deleteMark;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCatalogId() { return catalogId; }
    public void setCatalogId(String catalogId) { this.catalogId = catalogId; }
    public String getProductDesc() { return productDesc; }
    public void setProductDesc(String productDesc) { this.productDesc = productDesc; }
    public String getPricingModel() { return pricingModel; }
    public void setPricingModel(String pricingModel) { this.pricingModel = pricingModel; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDeleteMark() { return deleteMark; }
    public void setDeleteMark(Integer deleteMark) { this.deleteMark = deleteMark; }

    // Aliases for MyBatis-Plus compatibility and service calls
    public void setfTenantId(String tenantId) { this.tenantId = tenantId; }
    public LocalDateTime getfCreateTime() { return createTime; }
    public void setfCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getfUpdateTime() { return updateTime; }
    public void setfUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getfDeleteMark() { return deleteMark; }
    public void setfDeleteMark(Integer deleteMark) { this.deleteMark = deleteMark; }
}