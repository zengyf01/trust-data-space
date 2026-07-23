package com.tds.service.catalog;

import java.util.List;

/**
 * 目录创建DTO
 */
public class CatalogCreateDTO {

    private String catalogName;
    private String dataSourceId;
    private String schemaName;
    private String tableName;
    private String description;
    private List<CatalogFieldDTO> fields;
    private String tenantId;

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
    public List<CatalogFieldDTO> getFields() { return fields; }
    public void setFields(List<CatalogFieldDTO> fields) { this.fields = fields; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}