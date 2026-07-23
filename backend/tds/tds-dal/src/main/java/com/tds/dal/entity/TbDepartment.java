package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 部门实体
 */
@TableName("tb_department")
public class TbDepartment {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fDeptCode;             // 部门编码
    private String fDeptName;             // 部门名称
    private String fParentId;             // 上级部门ID
    private String fOrgId;                // 机构ID
    private Integer fDeptLevel;            // 部门层级
    private Integer fSortOrder;            // 排序
    private String fManagerId;            // 部门负责人ID
    private String fManagerName;           // 部门负责人名称
    private String fTenantId;            // 租户ID
    private LocalDateTime fCreateTime;     // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    @TableLogic
    private Integer fDeleteMark;           // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfDeptCode() { return fDeptCode; }
    public void setfDeptCode(String fDeptCode) { this.fDeptCode = fDeptCode; }
    public String getfDeptName() { return fDeptName; }
    public void setfDeptName(String fDeptName) { this.fDeptName = fDeptName; }
    public String getfParentId() { return fParentId; }
    public void setfParentId(String fParentId) { this.fParentId = fParentId; }
    public String getfOrgId() { return fOrgId; }
    public void setfOrgId(String fOrgId) { this.fOrgId = fOrgId; }
    public Integer getfDeptLevel() { return fDeptLevel; }
    public void setfDeptLevel(Integer fDeptLevel) { this.fDeptLevel = fDeptLevel; }
    public Integer getfSortOrder() { return fSortOrder; }
    public void setfSortOrder(Integer fSortOrder) { this.fSortOrder = fSortOrder; }
    public String getfManagerId() { return fManagerId; }
    public void setfManagerId(String fManagerId) { this.fManagerId = fManagerId; }
    public String getfManagerName() { return fManagerName; }
    public void setfManagerName(String fManagerName) { this.fManagerName = fManagerName; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}