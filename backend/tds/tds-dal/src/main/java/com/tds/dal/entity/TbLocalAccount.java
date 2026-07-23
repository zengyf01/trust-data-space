package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 本地账户实体
 */
@TableName("tb_local_account")
public class TbLocalAccount {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fAccountCode;          // 账户编码
    private String fAccountName;          // 账户名称
    private String fAccountType;          // 账户类型：LOCAL/FEDERATED
    private String fOrgId;               // 所属机构ID
    private String fUserId;              // 关联用户ID
    private String fIdCard;              // 身份证号
    private String fPhone;              // 手机号
    private String fEmail;                // 邮箱
    private String fAuthMode;            // 认证模式：LOCAL/PASSWORD/CERTIFICATE
    private String fCredential;          // 凭证（加密存储）
    private Integer fIsVerified;          // 是否已认证
    private String fTenantId;            // 租户ID
    private LocalDateTime fCreateTime;    // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    private Integer fDeleteMark;           // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfAccountCode() { return fAccountCode; }
    public void setfAccountCode(String fAccountCode) { this.fAccountCode = fAccountCode; }
    public String getfAccountName() { return fAccountName; }
    public void setfAccountName(String fAccountName) { this.fAccountName = fAccountName; }
    public String getfAccountType() { return fAccountType; }
    public void setfAccountType(String fAccountType) { this.fAccountType = fAccountType; }
    public String getfOrgId() { return fOrgId; }
    public void setfOrgId(String fOrgId) { this.fOrgId = fOrgId; }
    public String getfUserId() { return fUserId; }
    public void setfUserId(String fUserId) { this.fUserId = fUserId; }
    public String getfIdCard() { return fIdCard; }
    public void setfIdCard(String fIdCard) { this.fIdCard = fIdCard; }
    public String getfPhone() { return fPhone; }
    public void setfPhone(String fPhone) { this.fPhone = fPhone; }
    public String getfEmail() { return fEmail; }
    public void setfEmail(String fEmail) { this.fEmail = fEmail; }
    public String getfAuthMode() { return fAuthMode; }
    public void setfAuthMode(String fAuthMode) { this.fAuthMode = fAuthMode; }
    public String getfCredential() { return fCredential; }
    public void setfCredential(String fCredential) { this.fCredential = fCredential; }
    public Integer getfIsVerified() { return fIsVerified; }
    public void setfIsVerified(Integer fIsVerified) { this.fIsVerified = fIsVerified; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}