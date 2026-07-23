package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 数据消费日志表
 */
@TableName("tb_data_consume_log")
public class TbDataConsumeLog {

    @TableId
    private String fId;

    private String fContractId;

    private String fOrderId;

    private String fProductId;

    private String fConsumerTenantId;

    private String fProviderTenantId;

    private String fConsumeType;

    private String fApiEndpoint;

    private Long fApiCount;

    private Long fDataVolume;

    private String fTxHash;

    private String fTenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    private LocalDateTime fConsumeTime;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfContractId() { return fContractId; }
    public void setfContractId(String fContractId) { this.fContractId = fContractId; }
    public String getfOrderId() { return fOrderId; }
    public void setfOrderId(String fOrderId) { this.fOrderId = fOrderId; }
    public String getfProductId() { return fProductId; }
    public void setfProductId(String fProductId) { this.fProductId = fProductId; }
    public String getfConsumerTenantId() { return fConsumerTenantId; }
    public void setfConsumerTenantId(String fConsumerTenantId) { this.fConsumerTenantId = fConsumerTenantId; }
    public String getfProviderTenantId() { return fProviderTenantId; }
    public void setfProviderTenantId(String fProviderTenantId) { this.fProviderTenantId = fProviderTenantId; }
    public String getfConsumeType() { return fConsumeType; }
    public void setfConsumeType(String fConsumeType) { this.fConsumeType = fConsumeType; }
    public String getfApiEndpoint() { return fApiEndpoint; }
    public void setfApiEndpoint(String fApiEndpoint) { this.fApiEndpoint = fApiEndpoint; }
    public Long getfApiCount() { return fApiCount; }
    public void setfApiCount(Long fApiCount) { this.fApiCount = fApiCount; }
    public Long getfDataVolume() { return fDataVolume; }
    public void setfDataVolume(Long fDataVolume) { this.fDataVolume = fDataVolume; }
    public String getfTxHash() { return fTxHash; }
    public void setfTxHash(String fTxHash) { this.fTxHash = fTxHash; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfConsumeTime() { return fConsumeTime; }
    public void setfConsumeTime(LocalDateTime fConsumeTime) { this.fConsumeTime = fConsumeTime; }
}