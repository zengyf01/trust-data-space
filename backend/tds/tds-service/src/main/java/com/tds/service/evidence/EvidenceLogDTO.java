package com.tds.service.evidence;

import java.time.LocalDateTime;

/**
 * 存证DTO
 */
public class EvidenceLogDTO {

    private String evidenceType;
    private String evidenceData;
    private String contractId;
    private String orderId;
    private String tenantId;
    private String createUser;

    // Getters and Setters
    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }
    public String getEvidenceData() { return evidenceData; }
    public void setEvidenceData(String evidenceData) { this.evidenceData = evidenceData; }
    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
}