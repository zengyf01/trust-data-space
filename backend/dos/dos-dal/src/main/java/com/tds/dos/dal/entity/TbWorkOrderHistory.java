package com.tds.dos.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 工单历史表
 */
@TableName("tb_work_order_history")
public class TbWorkOrderHistory {

    @TableId
    private String fId;

    private String fWorkOrderId;

    private String fWorkOrderCode;

    private String fOperation;

    private String fOperator;

    private String fOperatorId;

    private Integer fFromStatus;

    private Integer fToStatus;

    private String fOperationDetail;

    private String fTenantId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }

    public String getfWorkOrderId() { return fWorkOrderId; }
    public void setfWorkOrderId(String fWorkOrderId) { this.fWorkOrderId = fWorkOrderId; }

    public String getfWorkOrderCode() { return fWorkOrderCode; }
    public void setfWorkOrderCode(String fWorkOrderCode) { this.fWorkOrderCode = fWorkOrderCode; }

    public String getfOperation() { return fOperation; }
    public void setfOperation(String fOperation) { this.fOperation = fOperation; }

    public String getfOperator() { return fOperator; }
    public void setfOperator(String fOperator) { this.fOperator = fOperator; }

    public String getfOperatorId() { return fOperatorId; }
    public void setfOperatorId(String fOperatorId) { this.fOperatorId = fOperatorId; }

    public Integer getfFromStatus() { return fFromStatus; }
    public void setfFromStatus(Integer fFromStatus) { this.fFromStatus = fFromStatus; }

    public Integer getfToStatus() { return fToStatus; }
    public void setfToStatus(Integer fToStatus) { this.fToStatus = fToStatus; }

    public String getfOperationDetail() { return fOperationDetail; }
    public void setfOperationDetail(String fOperationDetail) { this.fOperationDetail = fOperationDetail; }

    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }

    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
}