package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("tb_catalog")
public class TbCatalog {

    @TableId(value = "f_id")
    private String id;

    @TableField("f_catalog_code")
    private String catalogCode;

    @TableField("f_catalog_name")
    private String catalogName;

    @TableField("f_data_source_id")
    private String dataSourceId;

    @TableField("f_schema_name")
    private String schemaName;

    @TableField("f_table_name")
    private String tableName;

    @TableField("f_description")
    private String description;

    @TableField("f_version")
    private Integer version;

    @TableField("f_status")
    private Integer status;

    @TableField("f_tenant_id")
    private String tenantId;

    @TableField("f_space_id")
    private String spaceId;

    @TableField(value = "f_create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "f_update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("f_delete_mark")
    private Integer deleteMark;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCatalogCode() { return catalogCode; }
    public void setCatalogCode(String catalogCode) { this.catalogCode = catalogCode; }
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
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDeleteMark() { return deleteMark; }
    public void setDeleteMark(Integer deleteMark) { this.deleteMark = deleteMark; }

    // Aliases for MyBatis-Plus compatibility and service calls
    public void setfTenantId(String tenantId) { this.tenantId = tenantId; }
    public LocalDateTime getfCreateTime() { return createTime; }
    public void setfCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getfUpdateTime() { return updateTime; }
    public void setfUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getfDeleteMark() { return deleteMark; }
    public void setfDeleteMark(Integer deleteMark) { this.deleteMark = deleteMark; }
}