package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 通知发送记录实体
 */
@TableName("tb_notification_log")
public class TbNotificationLog {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fNotificationType;    // 通知类型
    private String fRecipient;           // 接收人
    private String fRecipientName;       // 接收人名称
    private String fSubject;             // 主题
    private String fContent;             // 内容
    private Integer fStatus;              // 状态：0-待发送 1-发送中 2-成功 3-失败
    private String fErrorMessage;         // 错误信息
    private Integer fRetryCount;          // 重试次数
    private String fTenantId;            // 租户ID
    private LocalDateTime fSendTime;      // 发送时间
    private LocalDateTime fCreateTime;    // 创建时间

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfNotificationType() { return fNotificationType; }
    public void setfNotificationType(String fNotificationType) { this.fNotificationType = fNotificationType; }
    public String getfRecipient() { return fRecipient; }
    public void setfRecipient(String fRecipient) { this.fRecipient = fRecipient; }
    public String getfRecipientName() { return fRecipientName; }
    public void setfRecipientName(String fRecipientName) { this.fRecipientName = fRecipientName; }
    public String getfSubject() { return fSubject; }
    public void setfSubject(String fSubject) { this.fSubject = fSubject; }
    public String getfContent() { return fContent; }
    public void setfContent(String fContent) { this.fContent = fContent; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfErrorMessage() { return fErrorMessage; }
    public void setfErrorMessage(String fErrorMessage) { this.fErrorMessage = fErrorMessage; }
    public Integer getfRetryCount() { return fRetryCount; }
    public void setfRetryCount(Integer fRetryCount) { this.fRetryCount = fRetryCount; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfSendTime() { return fSendTime; }
    public void setfSendTime(LocalDateTime fSendTime) { this.fSendTime = fSendTime; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
}