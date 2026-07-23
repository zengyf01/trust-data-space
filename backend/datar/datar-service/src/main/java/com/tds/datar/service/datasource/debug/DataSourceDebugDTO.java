package com.tds.datar.service.datasource.debug;

/**
 * 数据源调试DTO
 */
public class DataSourceDebugDTO {

    private String dataSourceId;
    private String tableName;
    private String sql;
    private String condition;
    private Integer limit;
    private String filePath;
    private String targetUrl;
    private String httpMethod;
    private String httpHeaders;
    private String httpBody;

    // Getters and Setters
    public String getDataSourceId() { return dataSourceId; }
    public void setDataSourceId(String dataSourceId) { this.dataSourceId = dataSourceId; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public String getHttpHeaders() { return httpHeaders; }
    public void setHttpHeaders(String httpHeaders) { this.httpHeaders = httpHeaders; }
    public String getHttpBody() { return httpBody; }
    public void setHttpBody(String httpBody) { this.httpBody = httpBody; }
}