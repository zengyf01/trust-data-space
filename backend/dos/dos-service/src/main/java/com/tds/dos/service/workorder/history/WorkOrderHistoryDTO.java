package com.tds.dos.service.workorder.history;

/**
 * 工单历史DTO
 */
public class WorkOrderHistoryDTO {

    private String workOrderId;
    private String workOrderCode;
    private String operation;
    private String operator;
    private String operatorId;
    private Integer fromStatus;
    private Integer toStatus;
    private String operationDetail;
    private String tenantId;

    // Getters and Setters
    public String getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(String workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderCode() { return workOrderCode; }
    public void setWorkOrderCode(String workOrderCode) { this.workOrderCode = workOrderCode; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public Integer getFromStatus() { return fromStatus; }
    public void setFromStatus(Integer fromStatus) { this.fromStatus = fromStatus; }
    public Integer getToStatus() { return toStatus; }
    public void setToStatus(Integer toStatus) { this.toStatus = toStatus; }
    public String getOperationDetail() { return operationDetail; }
    public void setOperationDetail(String operationDetail) { this.operationDetail = operationDetail; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}