package com.tds.datar.service.catalog;

/**
 * 目录DTO
 */
public class CatalogDTO {

    private String catalogName;
    private String dataSourceId;
    private String schemaName;
    private String tableName;
    private String description;
    private String tenantId;
    private String spaceId;

    // Getters and Setters
    public String getCatalogName() { return catalogName; }
    public void setCatalogName(String catalogName) { this.catalogName = catalogName; }
    public String getDataSourceId() { return dataSourceId; }
    public void setDataSourceId(String dataSourceId) { this.dataSourceId = dataSourceId; }
    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
}