package com.tds.service.evidence;

import java.time.LocalDateTime;

/**
 * 数据消费DTO
 */
public class DataConsumeDTO {

    private String contractId;
    private String orderId;
    private String productId;
    private String consumerTenantId;
    private String providerTenantId;
    private String consumeType;
    private String apiEndpoint;
    private Long apiCount;
    private Long dataVolume;
    private String txHash;
    private String tenantId;

    // Getters and Setters
    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getConsumerTenantId() { return consumerTenantId; }
    public void setConsumerTenantId(String consumerTenantId) { this.consumerTenantId = consumerTenantId; }
    public String getProviderTenantId() { return providerTenantId; }
    public void setProviderTenantId(String providerTenantId) { this.providerTenantId = providerTenantId; }
    public String getConsumeType() { return consumeType; }
    public void setConsumeType(String consumeType) { this.consumeType = consumeType; }
    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }
    public Long getApiCount() { return apiCount; }
    public void setApiCount(Long apiCount) { this.apiCount = apiCount; }
    public Long getDataVolume() { return dataVolume; }
    public void setDataVolume(Long dataVolume) { this.dataVolume = dataVolume; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}