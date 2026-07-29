package com.tds.dos.dal.msp.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * MSP Role entity
 */
@TableName("tb_role")
public class TbRole {
    @TableId
    private String fId;

    private String fRoleId;
    private String fRoleName;
    private String fRoleCode;
    private String fDescription;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfRoleId() { return fRoleId; }
    public void setfRoleId(String fRoleId) { this.fRoleId = fRoleId; }
    public String getfRoleName() { return fRoleName; }
    public void setfRoleName(String fRoleName) { this.fRoleName = fRoleName; }
    public String getfRoleCode() { return fRoleCode; }
    public void setfRoleCode(String fRoleCode) { this.fRoleCode = fRoleCode; }
    public String getfDescription() { return fDescription; }
    public void setfDescription(String fDescription) { this.fDescription = fDescription; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}
