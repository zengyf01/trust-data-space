package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 角色实体
 */
@TableName("tb_role")
public class TbRole {

    @TableId
    private String fId;

    private String fRoleCode;
    private String fRoleName;
    private String fRoleType;
    private String fRoleDesc;
    private Integer fIsSystem;
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
    public String getfRoleCode() { return fRoleCode; }
    public void setfRoleCode(String fRoleCode) { this.fRoleCode = fRoleCode; }
    public String getfRoleName() { return fRoleName; }
    public void setfRoleName(String fRoleName) { this.fRoleName = fRoleName; }
    public String getfRoleType() { return fRoleType; }
    public void setfRoleType(String fRoleType) { this.fRoleType = fRoleType; }
    public String getfRoleDesc() { return fRoleDesc; }
    public void setfRoleDesc(String fRoleDesc) { this.fRoleDesc = fRoleDesc; }
    public Integer getfIsSystem() { return fIsSystem; }
    public void setfIsSystem(Integer fIsSystem) { this.fIsSystem = fIsSystem; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}
