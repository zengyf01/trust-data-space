package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@TableName("tb_user")
public class TbUser {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fUsername;             // 用户名
    private String fPassword;             // 密码（加密）
    private String fRealName;             // 真实姓名
    private String fNickName;             // 昵称
    private String fEmail;                // 邮箱
    private String fPhone;                // 手机号
    private String fAvatar;               // 头像
    private String fOrgId;                // 所属机构ID
    private String fDeptId;              // 所属部门ID
    private String fUserType;             // 用户类型
    private Integer fStatus;               // 状态：0-禁用 1-正常
    private String fLastLoginIp;          // 最后登录IP
    private LocalDateTime fLastLoginTime;  // 最后登录时间
    private String fTenantId;            // 租户ID
    private LocalDateTime fCreateTime;     // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    @TableLogic
    private Integer fDeleteMark;           // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfUsername() { return fUsername; }
    public void setfUsername(String fUsername) { this.fUsername = fUsername; }
    public String getfPassword() { return fPassword; }
    public void setfPassword(String fPassword) { this.fPassword = fPassword; }
    public String getfRealName() { return fRealName; }
    public void setfRealName(String fRealName) { this.fRealName = fRealName; }
    public String getfNickName() { return fNickName; }
    public void setfNickName(String fNickName) { this.fNickName = fNickName; }
    public String getfEmail() { return fEmail; }
    public void setfEmail(String fEmail) { this.fEmail = fEmail; }
    public String getfPhone() { return fPhone; }
    public void setfPhone(String fPhone) { this.fPhone = fPhone; }
    public String getfAvatar() { return fAvatar; }
    public void setfAvatar(String fAvatar) { this.fAvatar = fAvatar; }
    public String getfOrgId() { return fOrgId; }
    public void setfOrgId(String fOrgId) { this.fOrgId = fOrgId; }
    public String getfDeptId() { return fDeptId; }
    public void setfDeptId(String fDeptId) { this.fDeptId = fDeptId; }
    public String getfUserType() { return fUserType; }
    public void setfUserType(String fUserType) { this.fUserType = fUserType; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfLastLoginIp() { return fLastLoginIp; }
    public void setfLastLoginIp(String fLastLoginIp) { this.fLastLoginIp = fLastLoginIp; }
    public LocalDateTime getfLastLoginTime() { return fLastLoginTime; }
    public void setfLastLoginTime(LocalDateTime fLastLoginTime) { this.fLastLoginTime = fLastLoginTime; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}