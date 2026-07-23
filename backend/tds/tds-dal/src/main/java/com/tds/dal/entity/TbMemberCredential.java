package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 机构凭证表
 * 用于存储机构的AppId/AppKey和SM2密钥对
 */
@TableName("tb_member_credential")
public class TbMemberCredential {

    @TableId(type = IdType.INPUT)
    private String fId;                       // 主键ID
    private String fMemberId;                 // 机构ID
    private String fConnectorId;              // 连接器ID
    private String fConnectorNumber;          // 连接器编号
    private String fCredentialType;           // 凭证类型：API_KEY / SM2_CERT
    private String fAppId;                    // 应用ID
    private String fAppKey;                   // 应用密钥（加密存储）
    private String fPublicKey;                // 公钥（SM2）
    private String fPrivateKeyEncrypted;      // 加密私钥（SM2）
    private Integer fStatus;                  // 状态：0-无效 1-有效 2-过期
    private LocalDateTime fIssueTime;         // 发放时间
    private LocalDateTime fExpireTime;        // 过期时间
    private LocalDateTime fCreateTime;        // 创建时间
    private LocalDateTime fUpdateTime;        // 更新时间

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfMemberId() { return fMemberId; }
    public void setfMemberId(String fMemberId) { this.fMemberId = fMemberId; }
    public String getfConnectorId() { return fConnectorId; }
    public void setfConnectorId(String fConnectorId) { this.fConnectorId = fConnectorId; }
    public String getfConnectorNumber() { return fConnectorNumber; }
    public void setfConnectorNumber(String fConnectorNumber) { this.fConnectorNumber = fConnectorNumber; }
    public String getfCredentialType() { return fCredentialType; }
    public void setfCredentialType(String fCredentialType) { this.fCredentialType = fCredentialType; }
    public String getfAppId() { return fAppId; }
    public void setfAppId(String fAppId) { this.fAppId = fAppId; }
    public String getfAppKey() { return fAppKey; }
    public void setfAppKey(String fAppKey) { this.fAppKey = fAppKey; }
    public String getfPublicKey() { return fPublicKey; }
    public void setfPublicKey(String fPublicKey) { this.fPublicKey = fPublicKey; }
    public String getfPrivateKeyEncrypted() { return fPrivateKeyEncrypted; }
    public void setfPrivateKeyEncrypted(String fPrivateKeyEncrypted) { this.fPrivateKeyEncrypted = fPrivateKeyEncrypted; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public LocalDateTime getfIssueTime() { return fIssueTime; }
    public void setfIssueTime(LocalDateTime fIssueTime) { this.fIssueTime = fIssueTime; }
    public LocalDateTime getfExpireTime() { return fExpireTime; }
    public void setfExpireTime(LocalDateTime fExpireTime) { this.fExpireTime = fExpireTime; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
}