package com.tds.dos.service.workorder;

/**
 * 工单创建DTO
 */
public class WorkOrderCreateDTO {

    private String orderCode;
    private Integer workOrderType;
    private String configJson;
    private String creator;
    private String creatorId;
    private String tenantId;
    private String spaceId;

    // Getters and Setters
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public Integer getWorkOrderType() { return workOrderType; }
    public void setWorkOrderType(Integer workOrderType) { this.workOrderType = workOrderType; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
}