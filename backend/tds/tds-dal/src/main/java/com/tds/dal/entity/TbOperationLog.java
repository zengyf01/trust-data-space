package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 操作日志表
 */
@TableName("tb_operation_log")
public class TbOperationLog {

    @TableId
    private String fId;

    private String fUserId;

    private String fUserName;

    private String fTenantId;

    private String fModule;

    private String fOperation;

    private String fMethod;

    private String fUrl;

    private String fRequestParams;

    private String fResponseResult;

    private Integer fStatus;

    private String fErrorMessage;

    private String fIpAddress;

    private String fUserAgent;

    private Long fDuration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfUserId() { return fUserId; }
    public void setfUserId(String fUserId) { this.fUserId = fUserId; }
    public String getfUserName() { return fUserName; }
    public void setfUserName(String fUserName) { this.fUserName = fUserName; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public String getfModule() { return fModule; }
    public void setfModule(String fModule) { this.fModule = fModule; }
    public String getfOperation() { return fOperation; }
    public void setfOperation(String fOperation) { this.fOperation = fOperation; }
    public String getfMethod() { return fMethod; }
    public void setfMethod(String fMethod) { this.fMethod = fMethod; }
    public String getfUrl() { return fUrl; }
    public void setfUrl(String fUrl) { this.fUrl = fUrl; }
    public String getfRequestParams() { return fRequestParams; }
    public void setfRequestParams(String fRequestParams) { this.fRequestParams = fRequestParams; }
    public String getfResponseResult() { return fResponseResult; }
    public void setfResponseResult(String fResponseResult) { this.fResponseResult = fResponseResult; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfErrorMessage() { return fErrorMessage; }
    public void setfErrorMessage(String fErrorMessage) { this.fErrorMessage = fErrorMessage; }
    public String getfIpAddress() { return fIpAddress; }
    public void setfIpAddress(String fIpAddress) { this.fIpAddress = fIpAddress; }
    public String getfUserAgent() { return fUserAgent; }
    public void setfUserAgent(String fUserAgent) { this.fUserAgent = fUserAgent; }
    public Long getfDuration() { return fDuration; }
    public void setfDuration(Long fDuration) { this.fDuration = fDuration; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
}