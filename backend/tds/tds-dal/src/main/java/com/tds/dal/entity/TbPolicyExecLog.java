package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 策略执行日志表
 */
@TableName("tb_policy_exec_log")
public class TbPolicyExecLog {

    @TableId
    private String fId;

    private String fPolicyId;

    private String fExecType; // CHECK/APPLY

    private String fExecResult; // SUCCESS/FAILED

    private String fExecDetail;

    private Long fDuration;

    private String fTenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfPolicyId() { return fPolicyId; }
    public void setfPolicyId(String fPolicyId) { this.fPolicyId = fPolicyId; }
    public String getfExecType() { return fExecType; }
    public void setfExecType(String fExecType) { this.fExecType = fExecType; }
    public String getfExecResult() { return fExecResult; }
    public void setfExecResult(String fExecResult) { this.fExecResult = fExecResult; }
    public String getfExecDetail() { return fExecDetail; }
    public void setfExecDetail(String fExecDetail) { this.fExecDetail = fExecDetail; }
    public Long getfDuration() { return fDuration; }
    public void setfDuration(Long fDuration) { this.fDuration = fDuration; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
}