package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单实体
 */
@TableName("tb_menu")
public class TbMenu {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fParentId;             // 父菜单ID
    private String fMenuName;              // 菜单名称
    private String fMenuCode;              // 菜单编码
    private Integer fMenuType;            // 类型：1-分组 2-菜单
    private String fPath;                  // 路由路径
    private String fIcon;                  // 图标
    private Integer fSortOrder;           // 排序
    private Integer fStatus;              // 状态：0-禁用 1-启用
    private String fTenantId;             // 租户ID
    private LocalDateTime fCreateTime;    // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    private Integer fDeleteMark;          // 删除标记

    @TableField(exist = false)
    private List<TbMenu> children;        // 子菜单（非数据库字段）

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfParentId() { return fParentId; }
    public void setfParentId(String fParentId) { this.fParentId = fParentId; }
    public String getfMenuName() { return fMenuName; }
    public void setfMenuName(String fMenuName) { this.fMenuName = fMenuName; }
    public String getfMenuCode() { return fMenuCode; }
    public void setfMenuCode(String fMenuCode) { this.fMenuCode = fMenuCode; }
    public Integer getfMenuType() { return fMenuType; }
    public void setfMenuType(Integer fMenuType) { this.fMenuType = fMenuType; }
    public String getfPath() { return fPath; }
    public void setfPath(String fPath) { this.fPath = fPath; }
    public String getfIcon() { return fIcon; }
    public void setfIcon(String fIcon) { this.fIcon = fIcon; }
    public Integer getfSortOrder() { return fSortOrder; }
    public void setfSortOrder(Integer fSortOrder) { this.fSortOrder = fSortOrder; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
    public List<TbMenu> getChildren() { return children; }
    public void setChildren(List<TbMenu> children) { this.children = children; }
}
