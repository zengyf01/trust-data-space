package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 策略绑定表
 */
@TableName("tb_policy_binding")
public class TbPolicyBinding {

    @TableId
    private String fId;

    private String fPolicyId;

    private String fResourceType; // PRODUCT/DATA_SOURCE/CATALOG

    private String fResourceId;

    private String fTenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableLogic
    private Integer fDeleteMark;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfPolicyId() { return fPolicyId; }
    public void setfPolicyId(String fPolicyId) { this.fPolicyId = fPolicyId; }
    public String getfResourceType() { return fResourceType; }
    public void setfResourceType(String fResourceType) { this.fResourceType = fResourceType; }
    public String getfResourceId() { return fResourceId; }
    public void setfResourceId(String fResourceId) { this.fResourceId = fResourceId; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}