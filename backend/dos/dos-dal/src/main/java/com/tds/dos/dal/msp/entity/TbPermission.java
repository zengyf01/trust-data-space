package com.tds.dos.dal.msp.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * MSP Permission entity
 */
@TableName("tb_permission")
public class TbPermission {
    @TableId
    private String fId;

    private String fPermissionId;
    private String fPermissionName;
    private String fPermissionCode;
    private String fParentId;
    private String fResourceType;
    private String fPath;
    private String fIcon;
    private Integer fSortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfPermissionId() { return fPermissionId; }
    public void setfPermissionId(String fPermissionId) { this.fPermissionId = fPermissionId; }
    public String getfPermissionName() { return fPermissionName; }
    public void setfPermissionName(String fPermissionName) { this.fPermissionName = fPermissionName; }
    public String getfPermissionCode() { return fPermissionCode; }
    public void setfPermissionCode(String fPermissionCode) { this.fPermissionCode = fPermissionCode; }
    public String getfParentId() { return fParentId; }
    public void setfParentId(String fParentId) { this.fParentId = fParentId; }
    public String getfResourceType() { return fResourceType; }
    public void setfResourceType(String fResourceType) { this.fResourceType = fResourceType; }
    public String getfPath() { return fPath; }
    public void setfPath(String fPath) { this.fPath = fPath; }
    public String getfIcon() { return fIcon; }
    public void setfIcon(String fIcon) { this.fIcon = fIcon; }
    public Integer getfSortOrder() { return fSortOrder; }
    public void setfSortOrder(Integer fSortOrder) { this.fSortOrder = fSortOrder; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}
