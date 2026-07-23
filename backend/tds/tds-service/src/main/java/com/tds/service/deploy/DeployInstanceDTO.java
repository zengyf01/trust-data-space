package com.tds.service.deploy;

/**
 * 部署实例DTO
 */
public class DeployInstanceDTO {

    private String id;
    private String instanceCode;
    private String instanceName;
    private String nodeId;
    private String serviceType;
    private String serviceVersion;
    private Integer status;
    private String accessUrl;
    private Integer replicaCount;
    private Integer currentReplicas;
    private String tenantId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInstanceCode() { return instanceCode; }
    public void setInstanceCode(String instanceCode) { this.instanceCode = instanceCode; }
    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getServiceVersion() { return serviceVersion; }
    public void setServiceVersion(String serviceVersion) { this.serviceVersion = serviceVersion; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getAccessUrl() { return accessUrl; }
    public void setAccessUrl(String accessUrl) { this.accessUrl = accessUrl; }
    public Integer getReplicaCount() { return replicaCount; }
    public void setReplicaCount(Integer replicaCount) { this.replicaCount = replicaCount; }
    public Integer getCurrentReplicas() { return currentReplicas; }
    public void setCurrentReplicas(Integer currentReplicas) { this.currentReplicas = currentReplicas; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}