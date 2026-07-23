package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 角色实体
 */
@TableName("tb_role")
public class TbRole {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fRoleCode;            // 角色编码
    private String fRoleName;             // 角色名称
    private String fRoleType;             // 角色类型：SYSTEM/BUSINESS
    private String fOrgId;                // 所属机构ID
    private String fRoleDesc;             // 角色描述
    private Integer fIsSystem;            // 是否系统角色：0-否 1-是
    private String fTenantId;            // 租户ID
    private LocalDateTime fCreateTime;     // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    @TableLogic
    private Integer fDeleteMark;           // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfRoleCode() { return fRoleCode; }
    public void setfRoleCode(String fRoleCode) { this.fRoleCode = fRoleCode; }
    public String getfRoleName() { return fRoleName; }
    public void setfRoleName(String fRoleName) { this.fRoleName = fRoleName; }
    public String getfRoleType() { return fRoleType; }
    public void setfRoleType(String fRoleType) { this.fRoleType = fRoleType; }
    public String getfOrgId() { return fOrgId; }
    public void setfOrgId(String fOrgId) { this.fOrgId = fOrgId; }
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