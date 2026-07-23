package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 工单表
 */
@TableName("tb_work_order")
public class TbWorkOrder {

    @TableId
    private String fId;

    private String fOrderId;

    private String fWorkOrderType;

    private String fWorkOrderCode;

    private Integer fStatus;

    private String fInputParams;

    private String fOutputResult;

    private String fErrorMessage;

    private String fTenantId;

    private String fSpaceId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfOrderId() { return fOrderId; }
    public void setfOrderId(String fOrderId) { this.fOrderId = fOrderId; }
    public String getfWorkOrderType() { return fWorkOrderType; }
    public void setfWorkOrderType(String fWorkOrderType) { this.fWorkOrderType = fWorkOrderType; }
    public String getfWorkOrderCode() { return fWorkOrderCode; }
    public void setfWorkOrderCode(String fWorkOrderCode) { this.fWorkOrderCode = fWorkOrderCode; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfInputParams() { return fInputParams; }
    public void setfInputParams(String fInputParams) { this.fInputParams = fInputParams; }
    public String getfOutputResult() { return fOutputResult; }
    public void setfOutputResult(String fOutputResult) { this.fOutputResult = fOutputResult; }
    public String getfErrorMessage() { return fErrorMessage; }
    public void setfErrorMessage(String fErrorMessage) { this.fErrorMessage = fErrorMessage; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public String getfSpaceId() { return fSpaceId; }
    public void setfSpaceId(String fSpaceId) { this.fSpaceId = fSpaceId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}