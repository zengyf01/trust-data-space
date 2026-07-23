package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 连接器操作日志实体
 */
@TableName("tb_connector_log")
public class TbConnectorLog {

    @TableId
    private String id;

    /** 连接器ID */
    private String connectorId;

    /** 操作类型(DEPLOY部署,UPGRADE升级,UNINSTALL卸载,RESTART重启,STOP停止) */
    private String operateType;

    /** 操作内容 */
    private String operateContent;

    /** 操作结果(成功SUCCESS,失败FAIL) */
    private String operateResult;

    /** 错误信息 */
    private String errorMessage;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 耗时(秒) */
    private Integer duration;

    /** 租户ID */
    private String fTenantId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    /** 删除标志(0未删,1已删) */
    @TableLogic
    private Integer fDeleteMark;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnectorId() { return connectorId; }
    public void setConnectorId(String connectorId) { this.connectorId = connectorId; }
    public String getOperateType() { return operateType; }
    public void setOperateType(String operateType) { this.operateType = operateType; }
    public String getOperateContent() { return operateContent; }
    public void setOperateContent(String operateContent) { this.operateContent = operateContent; }
    public String getOperateResult() { return operateResult; }
    public void setOperateResult(String operateResult) { this.operateResult = operateResult; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}