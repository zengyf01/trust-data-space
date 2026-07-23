package com.tds.datar.service.open;

/**
 * 连接器开放接口DTO
 */
public class ConnectorOpenDTO {

    private String connectorId;
    private String connectorName;
    private String connectorType;
    private String sn;
    private String address;
    private Integer port;
    private String appId;
    private String appKey;
    private String defaultUsername;
    private String defaultPassword;
    private String tenantId;

    // Getters and Setters
    public String getConnectorId() { return connectorId; }
    public void setConnectorId(String connectorId) { this.connectorId = connectorId; }
    public String getConnectorName() { return connectorName; }
    public void setConnectorName(String connectorName) { this.connectorName = connectorName; }
    public String getConnectorType() { return connectorType; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }
    public String getSn() { return sn; }
    public void setSn(String sn) { this.sn = sn; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getDefaultUsername() { return defaultUsername; }
    public void setDefaultUsername(String defaultUsername) { this.defaultUsername = defaultUsername; }
    public String getDefaultPassword() { return defaultPassword; }
    public void setDefaultPassword(String defaultPassword) { this.defaultPassword = defaultPassword; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}