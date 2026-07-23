package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@TableName("tb_order")
public class TbOrder {

    @TableId
    private String fId;

    private String fOrderCode;

    private String fProductId;

    private String fBuyerTenantId;

    private String fSellerTenantId;

    private String fBuyerSpaceId;

    private String fSellerSpaceId;

    private String fContractId;

    private Integer fStatus;

    private String fTenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfOrderCode() { return fOrderCode; }
    public void setfOrderCode(String fOrderCode) { this.fOrderCode = fOrderCode; }
    public String getfProductId() { return fProductId; }
    public void setfProductId(String fProductId) { this.fProductId = fProductId; }
    public String getfBuyerTenantId() { return fBuyerTenantId; }
    public void setfBuyerTenantId(String fBuyerTenantId) { this.fBuyerTenantId = fBuyerTenantId; }
    public String getfSellerTenantId() { return fSellerTenantId; }
    public void setfSellerTenantId(String fSellerTenantId) { this.fSellerTenantId = fSellerTenantId; }
    public String getfBuyerSpaceId() { return fBuyerSpaceId; }
    public void setfBuyerSpaceId(String fBuyerSpaceId) { this.fBuyerSpaceId = fBuyerSpaceId; }
    public String getfSellerSpaceId() { return fSellerSpaceId; }
    public void setfSellerSpaceId(String fSellerSpaceId) { this.fSellerSpaceId = fSellerSpaceId; }
    public String getfContractId() { return fContractId; }
    public void setfContractId(String fContractId) { this.fContractId = fContractId; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}