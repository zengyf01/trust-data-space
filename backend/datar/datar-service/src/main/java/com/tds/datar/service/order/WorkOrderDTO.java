package com.tds.datar.service.order;

/**
 * 工单DTO
 */
public class WorkOrderDTO {

    private String orderId;
    private String workOrderType;
    private String inputParams;
    private String tenantId;
    private String spaceId;

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getWorkOrderType() { return workOrderType; }
    public void setWorkOrderType(String workOrderType) { this.workOrderType = workOrderType; }
    public String getInputParams() { return inputParams; }
    public void setInputParams(String inputParams) { this.inputParams = inputParams; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
}