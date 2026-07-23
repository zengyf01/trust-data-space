package com.tds.service.connector;

/**
 * 连接器操作DTO
 */
public class ConnectorOperateDTO {

    private String connectorId;
    private String operateType;
    private String operateContent;

    // Getters and Setters
    public String getConnectorId() { return connectorId; }
    public void setConnectorId(String connectorId) { this.connectorId = connectorId; }
    public String getOperateType() { return operateType; }
    public void setOperateType(String operateType) { this.operateType = operateType; }
    public String getOperateContent() { return operateContent; }
    public void setOperateContent(String operateContent) { this.operateContent = operateContent; }
}