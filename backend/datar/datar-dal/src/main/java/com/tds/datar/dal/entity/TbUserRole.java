package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 */
@TableName("tb_user_role")
public class TbUserRole {

    @TableId
    private String fId;

    private String fUserId;
    private String fRoleId;
    private String fTenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfUserId() { return fUserId; }
    public void setfUserId(String fUserId) { this.fUserId = fUserId; }
    public String getfRoleId() { return fRoleId; }
    public void setfRoleId(String fRoleId) { this.fRoleId = fRoleId; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
}
