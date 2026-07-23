package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 资源目录实体
 */
@TableName("tb_catalog")
public class TbCatalog {

    @TableId
    private String id;

    /** 目录编号 */
    private String catalogCode;

    /** 目录名称 */
    private String catalogName;

    /** 关联数据源ID */
    private String dataSourceId;

    /** schema名 */
    private String schemaName;

    /** 表名 */
    private String tableName;

    /** 目录描述 */
    private String description;

    /** 版本号 */
    private Integer version;

    /** 状态(1草稿,2已发布,3已下线) */
    private Integer status;

    /** 租户ID */
    private String fTenantId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    /** 删除标志(0未删,1已删) */
    @TableLogic
    private Integer fDeleteMark;

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
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}