package com.tds.dos.msp.service.datasource;

import com.tds.dos.msp.common.enums.DataSourceType;

/**
 * DataSource DTO
 */
public class DataSourceDTO {
    private String datasourceId;
    private String nodeId;
    private String name;
    private DataSourceType type;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String password;
    private String tableName;
    private String columns;

    public String getDatasourceId() { return datasourceId; }
    public void setDatasourceId(String datasourceId) { this.datasourceId = datasourceId; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DataSourceType getType() { return type; }
    public void setType(DataSourceType type) { this.type = type; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getColumns() { return columns; }
    public void setColumns(String columns) { this.columns = columns; }
}