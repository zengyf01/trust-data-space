package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 */
@TableName("tb_user_role")
public class TbUserRole {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fUserId;               // 用户ID
    private String fRoleId;               // 角色ID
    private String fTenantId;            // 租户ID
    private LocalDateTime fCreateTime;     // 创建时间

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