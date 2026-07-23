package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 策略规则表
 */
@TableName("tb_policy_rule")
public class TbPolicyRule {

    @TableId
    private String fId;

    private String fPolicyCode;

    private String fPolicyName;

    private String fPolicyType; // ACCESS/RATE_LIMIT/DATA_MASK

    private String fPolicyContent; // 策略内容JSON

    private Integer fPriority;

    private String fStatus; // ENABLED/DISABLED

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
    public String getfPolicyCode() { return fPolicyCode; }
    public void setfPolicyCode(String fPolicyCode) { this.fPolicyCode = fPolicyCode; }
    public String getfPolicyName() { return fPolicyName; }
    public void setfPolicyName(String fPolicyName) { this.fPolicyName = fPolicyName; }
    public String getfPolicyType() { return fPolicyType; }
    public void setfPolicyType(String fPolicyType) { this.fPolicyType = fPolicyType; }
    public String getfPolicyContent() { return fPolicyContent; }
    public void setfPolicyContent(String fPolicyContent) { this.fPolicyContent = fPolicyContent; }
    public Integer getfPriority() { return fPriority; }
    public void setfPriority(Integer fPriority) { this.fPriority = fPriority; }
    public String getfStatus() { return fStatus; }
    public void setfStatus(String fStatus) { this.fStatus = fStatus; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}