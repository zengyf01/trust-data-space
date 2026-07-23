package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@TableName("tb_user")
public class TbUser {

    @TableId
    private String fId;

    private String fUsername;
    private String fPassword;
    private String fRealName;
    private String fPhone;
    private String fEmail;
    private String fUserType;
    private Integer fStatus;
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
    public String getfUsername() { return fUsername; }
    public void setfUsername(String fUsername) { this.fUsername = fUsername; }
    public String getfPassword() { return fPassword; }
    public void setfPassword(String fPassword) { this.fPassword = fPassword; }
    public String getfRealName() { return fRealName; }
    public void setfRealName(String fRealName) { this.fRealName = fRealName; }
    public String getfPhone() { return fPhone; }
    public void setfPhone(String fPhone) { this.fPhone = fPhone; }
    public String getfEmail() { return fEmail; }
    public void setfEmail(String fEmail) { this.fEmail = fEmail; }
    public String getfUserType() { return fUserType; }
    public void setfUserType(String fUserType) { this.fUserType = fUserType; }
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
}
