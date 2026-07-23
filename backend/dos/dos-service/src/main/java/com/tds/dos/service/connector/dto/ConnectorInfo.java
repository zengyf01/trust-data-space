package com.tds.dos.service.connector.dto;

/**
 * 连接器信息DTO
 */
public class ConnectorInfo {

    private String id;
    private String sn;
    private String name;
    private Integer type;
    private String status;
    private String ipAddress;
    private String institutionId;
    private String institutionName;

    public ConnectorInfo() {}

    public ConnectorInfo(String id, String sn, String name, Integer type, String status, String ipAddress) {
        this.id = id;
        this.sn = sn;
        this.name = name;
        this.type = type;
        this.status = status;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSn() { return sn; }
    public void setSn(String sn) { this.sn = sn; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
}
