package com.tds.dos.msp.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * MSP Alert entity
 */
@TableName("tb_msp_alert")
public class TbMspAlert {
    @TableId
    private String fId;

    private String fAlertId;
    private String fAlertName;
    private String fAlertType;
    private String fCondition;
    private String fThreshold;
    private Integer fEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfAlertId() { return fAlertId; }
    public void setfAlertId(String fAlertId) { this.fAlertId = fAlertId; }
    public String getfAlertName() { return fAlertName; }
    public void setfAlertName(String fAlertName) { this.fAlertName = fAlertName; }
    public String getfAlertType() { return fAlertType; }
    public void setfAlertType(String fAlertType) { this.fAlertType = fAlertType; }
    public String getfCondition() { return fCondition; }
    public void setfCondition(String fCondition) { this.fCondition = fCondition; }
    public String getfThreshold() { return fThreshold; }
    public void setfThreshold(String fThreshold) { this.fThreshold = fThreshold; }
    public Integer getfEnabled() { return fEnabled; }
    public void setfEnabled(Integer fEnabled) { this.fEnabled = fEnabled; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}