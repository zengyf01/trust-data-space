package com.tds.datar.service.dataflow;

/**
 * 数据同步DTO
 */
public class DataSyncDTO {

    private String syncId;
    private String sourceDataSourceId;
    private String targetDataSourceId;
    private String sourceTable;
    private String targetTable;
    private String syncType; // FULL/INCREMENTAL
    private String schedule; // cron表达式
    private String mappingJson; // 字段映射JSON
    private String filterCondition;
    private String status;
    private String tenantId;

    // Getters and Setters
    public String getSyncId() { return syncId; }
    public void setSyncId(String syncId) { this.syncId = syncId; }
    public String getSourceDataSourceId() { return sourceDataSourceId; }
    public void setSourceDataSourceId(String sourceDataSourceId) { this.sourceDataSourceId = sourceDataSourceId; }
    public String getTargetDataSourceId() { return targetDataSourceId; }
    public void setTargetDataSourceId(String targetDataSourceId) { this.targetDataSourceId = targetDataSourceId; }
    public String getSourceTable() { return sourceTable; }
    public void setSourceTable(String sourceTable) { this.sourceTable = sourceTable; }
    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }
    public String getSyncType() { return syncType; }
    public void setSyncType(String syncType) { this.syncType = syncType; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public String getMappingJson() { return mappingJson; }
    public void setMappingJson(String mappingJson) { this.mappingJson = mappingJson; }
    public String getFilterCondition() { return filterCondition; }
    public void setFilterCondition(String filterCondition) { this.filterCondition = filterCondition; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}