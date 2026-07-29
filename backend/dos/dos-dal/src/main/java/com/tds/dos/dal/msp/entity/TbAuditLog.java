package com.tds.dos.dal.msp.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * MSP Audit Log entity
 */
@TableName("tb_audit_log")
public class TbAuditLog {
    @TableId
    private String fId;

    private String fUserId;
    private String fOperation;
    private String fResourceType;
    private String fResourceId;
    private String fDetail;
    private String fIpAddress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfUserId() { return fUserId; }
    public void setfUserId(String fUserId) { this.fUserId = fUserId; }
    public String getfOperation() { return fOperation; }
    public void setfOperation(String fOperation) { this.fOperation = fOperation; }
    public String getfResourceType() { return fResourceType; }
    public void setfResourceType(String fResourceType) { this.fResourceType = fResourceType; }
    public String getfResourceId() { return fResourceId; }
    public void setfResourceId(String fResourceId) { this.fResourceId = fResourceId; }
    public String getfDetail() { return fDetail; }
    public void setfDetail(String fDetail) { this.fDetail = fDetail; }
    public String getfIpAddress() { return fIpAddress; }
    public void setfIpAddress(String fIpAddress) { this.fIpAddress = fIpAddress; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
}
