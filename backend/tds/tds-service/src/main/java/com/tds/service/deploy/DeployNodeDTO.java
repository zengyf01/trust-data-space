package com.tds.service.deploy;

/**
 * 部署节点DTO
 */
public class DeployNodeDTO {

    private String id;
    private String nodeCode;
    private String nodeName;
    private String nodeType;
    private String deployMode;
    private String ipAddress;
    private Integer port;
    private String region;
    private Integer cpuCores;
    private Long memorySize;
    private Long diskSize;
    private Integer status;
    private String tenantId;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNodeCode() { return nodeCode; }
    public void setNodeCode(String nodeCode) { this.nodeCode = nodeCode; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public String getDeployMode() { return deployMode; }
    public void setDeployMode(String deployMode) { this.deployMode = deployMode; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Integer getCpuCores() { return cpuCores; }
    public void setCpuCores(Integer cpuCores) { this.cpuCores = cpuCores; }
    public Long getMemorySize() { return memorySize; }
    public void setMemorySize(Long memorySize) { this.memorySize = memorySize; }
    public Long getDiskSize() { return diskSize; }
    public void setDiskSize(Long diskSize) { this.diskSize = diskSize; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}