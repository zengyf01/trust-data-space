package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 策略访问记录表
 */
@TableName("tb_policy_access_log")
public class TbPolicyAccessLog {

    @TableId
    private String fId;

    private String fPolicyId;

    private String fResourceType;

    private String fResourceId;

    private String fVisitorId;

    private String fVisitorTenantId;

    private String fAccessResult; // ALLOW/DENY

    private String fDenyReason;

    private String fRequestParams;

    private String fTenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fAccessTime;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfPolicyId() { return fPolicyId; }
    public void setfPolicyId(String fPolicyId) { this.fPolicyId = fPolicyId; }
    public String getfResourceType() { return fResourceType; }
    public void setfResourceType(String fResourceType) { this.fResourceType = fResourceType; }
    public String getfResourceId() { return fResourceId; }
    public void setfResourceId(String fResourceId) { this.fResourceId = fResourceId; }
    public String getfVisitorId() { return fVisitorId; }
    public void setfVisitorId(String fVisitorId) { this.fVisitorId = fVisitorId; }
    public String getfVisitorTenantId() { return fVisitorTenantId; }
    public void setfVisitorTenantId(String fVisitorTenantId) { this.fVisitorTenantId = fVisitorTenantId; }
    public String getfAccessResult() { return fAccessResult; }
    public void setfAccessResult(String fAccessResult) { this.fAccessResult = fAccessResult; }
    public String getfDenyReason() { return fDenyReason; }
    public void setfDenyReason(String fDenyReason) { this.fDenyReason = fDenyReason; }
    public String getfRequestParams() { return fRequestParams; }
    public void setfRequestParams(String fRequestParams) { this.fRequestParams = fRequestParams; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfAccessTime() { return fAccessTime; }
    public void setfAccessTime(LocalDateTime fAccessTime) { this.fAccessTime = fAccessTime; }
}