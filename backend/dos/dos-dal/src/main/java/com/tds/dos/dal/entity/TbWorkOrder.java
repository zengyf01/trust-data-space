package com.tds.dos.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 工单实体
 */
@TableName("tb_work_order")
public class TbWorkOrder {

    @TableId
    private String id;

    /** 工单编号 */
    private String workOrderCode;

    /** 关联订单编号 */
    private String orderCode;

    /** 工单类型(1数据服务,2安全沙盒,3隐私计算) */
    private Integer workOrderType;

    /** 工单状态(1待处理,2处理中,3已完成,4失败,5已取消) */
    private Integer workOrderStatus;

    /** 处理结果 */
    private String resultMessage;

    /** 处理配置JSON */
    private String configJson;

    /** 产出文件路径 */
    private String outputFilePath;

    /** 产出文件URL */
    private String outputFileUrl;

    /** 执行开始时间 */
    private LocalDateTime startTime;

    /** 执行结束时间 */
    private LocalDateTime endTime;

    /** 耗时(秒) */
    private Integer duration;

    /** 创建人 */
    private String creator;

    /** 创建人ID */
    private String creatorId;

    /** 租户ID */
    private String fTenantId;

    /** 数据空间ID */
    private String fSpaceId;

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
    public String getWorkOrderCode() { return workOrderCode; }
    public void setWorkOrderCode(String workOrderCode) { this.workOrderCode = workOrderCode; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public Integer getWorkOrderType() { return workOrderType; }
    public void setWorkOrderType(Integer workOrderType) { this.workOrderType = workOrderType; }
    public Integer getWorkOrderStatus() { return workOrderStatus; }
    public void setWorkOrderStatus(Integer workOrderStatus) { this.workOrderStatus = workOrderStatus; }
    public String getResultMessage() { return resultMessage; }
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public String getOutputFilePath() { return outputFilePath; }
    public void setOutputFilePath(String outputFilePath) { this.outputFilePath = outputFilePath; }
    public String getOutputFileUrl() { return outputFileUrl; }
    public void setOutputFileUrl(String outputFileUrl) { this.outputFileUrl = outputFileUrl; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public String getfSpaceId() { return fSpaceId; }
    public void setfSpaceId(String fSpaceId) { this.fSpaceId = fSpaceId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}