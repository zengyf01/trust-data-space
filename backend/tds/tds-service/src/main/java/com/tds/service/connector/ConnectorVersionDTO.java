package com.tds.service.connector;

/**
 * 连接器版本DTO
 */
public class ConnectorVersionDTO {

    private String connectorId;
    private String version;
    private String filePath;
    private Long fileSize;
    private String fileMd5;
    private String changeLog;
    private String tenantId;

    // Getters and Setters
    public String getConnectorId() { return connectorId; }
    public void setConnectorId(String connectorId) { this.connectorId = connectorId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFileMd5() { return fileMd5; }
    public void setFileMd5(String fileMd5) { this.fileMd5 = fileMd5; }
    public String getChangeLog() { return changeLog; }
    public void setChangeLog(String changeLog) { this.changeLog = changeLog; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}