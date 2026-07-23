package com.tds.dos.msp.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * MSP User entity
 */
@TableName("tb_msp_user")
public class TbMspUser {
    @TableId
    private String fId;

    private String fUserId;
    private String fUsername;
    private String fPassword;
    private String fEmail;
    private String fPhone;
    private String fRole;
    private String fStatus;
    private Integer fEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfUserId() { return fUserId; }
    public void setfUserId(String fUserId) { this.fUserId = fUserId; }
    public String getfUsername() { return fUsername; }
    public void setfUsername(String fUsername) { this.fUsername = fUsername; }
    public String getfPassword() { return fPassword; }
    public void setfPassword(String fPassword) { this.fPassword = fPassword; }
    public String getfEmail() { return fEmail; }
    public void setfEmail(String fEmail) { this.fEmail = fEmail; }
    public String getfPhone() { return fPhone; }
    public void setfPhone(String fPhone) { this.fPhone = fPhone; }
    public String getfRole() { return fRole; }
    public void setfRole(String fRole) { this.fRole = fRole; }
    public String getfStatus() { return fStatus; }
    public void setfStatus(String fStatus) { this.fStatus = fStatus; }
    public Integer getfEnabled() { return fEnabled; }
    public void setfEnabled(Integer fEnabled) { this.fEnabled = fEnabled; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}