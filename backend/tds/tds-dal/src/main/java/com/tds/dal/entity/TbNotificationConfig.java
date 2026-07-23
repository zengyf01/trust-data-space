package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 通知配置实体
 */
@TableName("tb_notification_config")
public class TbNotificationConfig {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fConfigCode;          // 配置编码
    private String fConfigName;          // 配置名称
    private String fNotificationType;    // 通知类型：EMAIL/SMS/WECHAT/WEBHOOK
    private Integer fIsEnabled;           // 是否启用：0-禁用 1-启用
    private String fHost;                // 主机地址
    private Integer fPort;               // 端口
    private String fUsername;            // 用户名
    private String fPassword;            // 密码
    private String fApiKey;              // API Key
    private String fApiSecret;           // API Secret
    private String fSignature;          // 签名
    private String fTemplateCode;        // 模板编码
    private String fWebhookUrl;          // Webhook URL
    private String fDescription;         // 描述
    private String fTenantId;            // 租户ID
    private LocalDateTime fCreateTime;    // 创建时间
    private LocalDateTime fUpdateTime;    // 更新时间
    private Integer fDeleteMark;          // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfConfigCode() { return fConfigCode; }
    public void setfConfigCode(String fConfigCode) { this.fConfigCode = fConfigCode; }
    public String getfConfigName() { return fConfigName; }
    public void setfConfigName(String fConfigName) { this.fConfigName = fConfigName; }
    public String getfNotificationType() { return fNotificationType; }
    public void setfNotificationType(String fNotificationType) { this.fNotificationType = fNotificationType; }
    public Integer getfIsEnabled() { return fIsEnabled; }
    public void setfIsEnabled(Integer fIsEnabled) { this.fIsEnabled = fIsEnabled; }
    public String getfHost() { return fHost; }
    public void setfHost(String fHost) { this.fHost = fHost; }
    public Integer getfPort() { return fPort; }
    public void setfPort(Integer fPort) { this.fPort = fPort; }
    public String getfUsername() { return fUsername; }
    public void setfUsername(String fUsername) { this.fUsername = fUsername; }
    public String getfPassword() { return fPassword; }
    public void setfPassword(String fPassword) { this.fPassword = fPassword; }
    public String getfApiKey() { return fApiKey; }
    public void setfApiKey(String fApiKey) { this.fApiKey = fApiKey; }
    public String getfApiSecret() { return fApiSecret; }
    public void setfApiSecret(String fApiSecret) { this.fApiSecret = fApiSecret; }
    public String getfSignature() { return fSignature; }
    public void setfSignature(String fSignature) { this.fSignature = fSignature; }
    public String getfTemplateCode() { return fTemplateCode; }
    public void setfTemplateCode(String fTemplateCode) { this.fTemplateCode = fTemplateCode; }
    public String getfWebhookUrl() { return fWebhookUrl; }
    public void setfWebhookUrl(String fWebhookUrl) { this.fWebhookUrl = fWebhookUrl; }
    public String getfDescription() { return fDescription; }
    public void setfDescription(String fDescription) { this.fDescription = fDescription; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}