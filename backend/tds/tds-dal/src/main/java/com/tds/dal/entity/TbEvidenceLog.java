package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 区块链存证表
 */
@TableName("tb_evidence_log")
public class TbEvidenceLog {

    @TableId
    private String fId;

    private String fTxHash;

    private String fBlockHash;

    private Long fBlockNumber;

    private String fEvidenceType;

    private String fEvidenceData;

    private String fContractId;

    private String fOrderId;

    private String fTenantId;

    private String fCreateUser;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    private LocalDateTime fChainTime;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfTxHash() { return fTxHash; }
    public void setfTxHash(String fTxHash) { this.fTxHash = fTxHash; }
    public String getfBlockHash() { return fBlockHash; }
    public void setfBlockHash(String fBlockHash) { this.fBlockHash = fBlockHash; }
    public Long getfBlockNumber() { return fBlockNumber; }
    public void setfBlockNumber(Long fBlockNumber) { this.fBlockNumber = fBlockNumber; }
    public String getfEvidenceType() { return fEvidenceType; }
    public void setfEvidenceType(String fEvidenceType) { this.fEvidenceType = fEvidenceType; }
    public String getfEvidenceData() { return fEvidenceData; }
    public void setfEvidenceData(String fEvidenceData) { this.fEvidenceData = fEvidenceData; }
    public String getfContractId() { return fContractId; }
    public void setfContractId(String fContractId) { this.fContractId = fContractId; }
    public String getfOrderId() { return fOrderId; }
    public void setfOrderId(String fOrderId) { this.fOrderId = fOrderId; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public String getfCreateUser() { return fCreateUser; }
    public void setfCreateUser(String fCreateUser) { this.fCreateUser = fCreateUser; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfChainTime() { return fChainTime; }
    public void setfChainTime(LocalDateTime fChainTime) { this.fChainTime = fChainTime; }
}