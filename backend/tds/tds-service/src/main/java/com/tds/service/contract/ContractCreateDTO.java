package com.tds.service.contract;

import java.time.LocalDateTime;

/**
 * 合约创建DTO
 */
public class ContractCreateDTO {
    private String orderCode;
    private Integer contractType;
    private LocalDateTime contractStartTime;
    private LocalDateTime contractEndTime;
    private String contractJson;
    private String resourceSnapshot;

    // 供应方信息
    private String providerInstitutionId;
    private String providerInstitutionName;
    private String providerContactName;
    private String providerPhone;
    private String providerEmail;
    private String providerConnectorAddress;
    private String providerInstitutionAddress;
    private String providerPublicKey;

    // 使用方信息
    private String useInstitutionId;
    private String useInstitutionName;
    private String useContactName;
    private String usePhone;
    private String useEmail;
    private String useConnectorAddress;
    private String useInstitutionAddress;
    private String usePublicKey;

    private String tenantId;

    // Getters and Setters
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public Integer getContractType() { return contractType; }
    public void setContractType(Integer contractType) { this.contractType = contractType; }
    public LocalDateTime getContractStartTime() { return contractStartTime; }
    public void setContractStartTime(LocalDateTime contractStartTime) { this.contractStartTime = contractStartTime; }
    public LocalDateTime getContractEndTime() { return contractEndTime; }
    public void setContractEndTime(LocalDateTime contractEndTime) { this.contractEndTime = contractEndTime; }
    public String getContractJson() { return contractJson; }
    public void setContractJson(String contractJson) { this.contractJson = contractJson; }
    public String getResourceSnapshot() { return resourceSnapshot; }
    public void setResourceSnapshot(String resourceSnapshot) { this.resourceSnapshot = resourceSnapshot; }
    public String getProviderInstitutionId() { return providerInstitutionId; }
    public void setProviderInstitutionId(String providerInstitutionId) { this.providerInstitutionId = providerInstitutionId; }
    public String getProviderInstitutionName() { return providerInstitutionName; }
    public void setProviderInstitutionName(String providerInstitutionName) { this.providerInstitutionName = providerInstitutionName; }
    public String getProviderContactName() { return providerContactName; }
    public void setProviderContactName(String providerContactName) { this.providerContactName = providerContactName; }
    public String getProviderPhone() { return providerPhone; }
    public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }
    public String getProviderEmail() { return providerEmail; }
    public void setProviderEmail(String providerEmail) { this.providerEmail = providerEmail; }
    public String getProviderConnectorAddress() { return providerConnectorAddress; }
    public void setProviderConnectorAddress(String providerConnectorAddress) { this.providerConnectorAddress = providerConnectorAddress; }
    public String getProviderInstitutionAddress() { return providerInstitutionAddress; }
    public void setProviderInstitutionAddress(String providerInstitutionAddress) { this.providerInstitutionAddress = providerInstitutionAddress; }
    public String getProviderPublicKey() { return providerPublicKey; }
    public void setProviderPublicKey(String providerPublicKey) { this.providerPublicKey = providerPublicKey; }
    public String getUseInstitutionId() { return useInstitutionId; }
    public void setUseInstitutionId(String useInstitutionId) { this.useInstitutionId = useInstitutionId; }
    public String getUseInstitutionName() { return useInstitutionName; }
    public void setUseInstitutionName(String useInstitutionName) { this.useInstitutionName = useInstitutionName; }
    public String getUseContactName() { return useContactName; }
    public void setUseContactName(String useContactName) { this.useContactName = useContactName; }
    public String getUsePhone() { return usePhone; }
    public void setUsePhone(String usePhone) { this.usePhone = usePhone; }
    public String getUseEmail() { return useEmail; }
    public void setUseEmail(String useEmail) { this.useEmail = useEmail; }
    public String getUseConnectorAddress() { return useConnectorAddress; }
    public void setUseConnectorAddress(String useConnectorAddress) { this.useConnectorAddress = useConnectorAddress; }
    public String getUseInstitutionAddress() { return useInstitutionAddress; }
    public void setUseInstitutionAddress(String useInstitutionAddress) { this.useInstitutionAddress = useInstitutionAddress; }
    public String getUsePublicKey() { return usePublicKey; }
    public void setUsePublicKey(String usePublicKey) { this.usePublicKey = usePublicKey; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}