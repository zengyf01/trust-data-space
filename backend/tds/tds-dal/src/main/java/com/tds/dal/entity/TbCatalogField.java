package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 目录字段实体
 */
@TableName("tb_catalog_field")
public class TbCatalogField {

    @TableId
    private String id;

    /** 目录ID */
    private String catalogId;

    /** 字段名 */
    private String fieldName;

    /** 字段类型 */
    private String fieldType;

    /** 字段注释 */
    private String fieldComment;

    /** 是否主键(0否,1是) */
    private Integer isPrimaryKey;

    /** 是否可空(0否,1是) */
    private Integer isNullable;

    /** 是否敏感字段(0否,1是) */
    private Integer isSensitive;

    /** 脱敏规则 */
    private String desensitizeRule;

    /** 排序 */
    private Integer sortOrder;

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
    public String getCatalogId() { return catalogId; }
    public void setCatalogId(String catalogId) { this.catalogId = catalogId; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
    public String getFieldComment() { return fieldComment; }
    public void setFieldComment(String fieldComment) { this.fieldComment = fieldComment; }
    public Integer getIsPrimaryKey() { return isPrimaryKey; }
    public void setIsPrimaryKey(Integer isPrimaryKey) { this.isPrimaryKey = isPrimaryKey; }
    public Integer getIsNullable() { return isNullable; }
    public void setIsNullable(Integer isNullable) { this.isNullable = isNullable; }
    public Integer getIsSensitive() { return isSensitive; }
    public void setIsSensitive(Integer isSensitive) { this.isSensitive = isSensitive; }
    public String getDesensitizeRule() { return desensitizeRule; }
    public void setDesensitizeRule(String desensitizeRule) { this.desensitizeRule = desensitizeRule; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}