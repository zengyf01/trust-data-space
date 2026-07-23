package com.tds.service.catalog;

/**
 * 目录字段DTO
 */
public class CatalogFieldDTO {

    private String fieldName;
    private String fieldType;
    private String fieldComment;
    private Integer isPrimaryKey;
    private Integer isNullable;
    private Integer isSensitive;
    private String desensitizeRule;
    private Integer sortOrder;

    // Getters and Setters
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
}