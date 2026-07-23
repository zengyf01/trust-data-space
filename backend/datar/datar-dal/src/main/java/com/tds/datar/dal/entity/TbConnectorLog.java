package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("tb_connector_log")
public class TbConnectorLog {

    @TableId
    private String fId;
    private String fConnectorId;
    private String fOperateType;
    private String fOperateContent;
    private String fOperateResult;
    private String fErrorMessage;
    private LocalDateTime fStartTime;
    private LocalDateTime fEndTime;
    private Integer fDuration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableLogic
    private Integer fDeleteMark;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfConnectorId() { return fConnectorId; }
    public void setfConnectorId(String fConnectorId) { this.fConnectorId = fConnectorId; }
    public String getfOperateType() { return fOperateType; }
    public void setfOperateType(String fOperateType) { this.fOperateType = fOperateType; }
    public String getfOperateContent() { return fOperateContent; }
    public void setfOperateContent(String fOperateContent) { this.fOperateContent = fOperateContent; }
    public String getfOperateResult() { return fOperateResult; }
    public void setfOperateResult(String fOperateResult) { this.fOperateResult = fOperateResult; }
    public String getfErrorMessage() { return fErrorMessage; }
    public void setfErrorMessage(String fErrorMessage) { this.fErrorMessage = fErrorMessage; }
    public LocalDateTime getfStartTime() { return fStartTime; }
    public void setfStartTime(LocalDateTime fStartTime) { this.fStartTime = fStartTime; }
    public LocalDateTime getfEndTime() { return fEndTime; }
    public void setfEndTime(LocalDateTime fEndTime) { this.fEndTime = fEndTime; }
    public Integer getfDuration() { return fDuration; }
    public void setfDuration(Integer fDuration) { this.fDuration = fDuration; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}