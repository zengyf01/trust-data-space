package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单实体
 */
@TableName("tb_bill")
public class TbBill {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fBillCode;             // 账单编码
    private String fTenantId;             // 租户ID
    private String fBillingPeriod;        // 账期：YYYY-MM
    private BigDecimal fTotalAmount;      // 总金额
    private BigDecimal fPaidAmount;       // 已支付金额
    private BigDecimal fPendingAmount;    // 待支付金额
    private Integer fStatus;              // 状态：0-待结算 1-已确认 2-已支付 3-已逾期
    private LocalDateTime fDueDate;       // 到期日
    private LocalDateTime fPaidTime;       // 支付时间
    private String fPaymentMethod;         // 支付方式
    private String fRemark;               // 备注
    private LocalDateTime fCreateTime;     // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    private Integer fDeleteMark;          // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfBillCode() { return fBillCode; }
    public void setfBillCode(String fBillCode) { this.fBillCode = fBillCode; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public String getfBillingPeriod() { return fBillingPeriod; }
    public void setfBillingPeriod(String fBillingPeriod) { this.fBillingPeriod = fBillingPeriod; }
    public BigDecimal getfTotalAmount() { return fTotalAmount; }
    public void setfTotalAmount(BigDecimal fTotalAmount) { this.fTotalAmount = fTotalAmount; }
    public BigDecimal getfPaidAmount() { return fPaidAmount; }
    public void setfPaidAmount(BigDecimal fPaidAmount) { this.fPaidAmount = fPaidAmount; }
    public BigDecimal getfPendingAmount() { return fPendingAmount; }
    public void setfPendingAmount(BigDecimal fPendingAmount) { this.fPendingAmount = fPendingAmount; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public LocalDateTime getfDueDate() { return fDueDate; }
    public void setfDueDate(LocalDateTime fDueDate) { this.fDueDate = fDueDate; }
    public LocalDateTime getfPaidTime() { return fPaidTime; }
    public void setfPaidTime(LocalDateTime fPaidTime) { this.fPaidTime = fPaidTime; }
    public String getfPaymentMethod() { return fPaymentMethod; }
    public void setfPaymentMethod(String fPaymentMethod) { this.fPaymentMethod = fPaymentMethod; }
    public String getfRemark() { return fRemark; }
    public void setfRemark(String fRemark) { this.fRemark = fRemark; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}