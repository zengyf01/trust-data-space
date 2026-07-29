package com.tds.dos.dal.msp.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * MSP System Config entity
 */
@TableName("tb_system_config")
public class TbSystemConfig {
    @TableId
    private String fId;

    private String fConfigKey;
    private String fConfigValue;
    private String fConfigType;
    private String fDescription;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfConfigKey() { return fConfigKey; }
    public void setfConfigKey(String fConfigKey) { this.fConfigKey = fConfigKey; }
    public String getfConfigValue() { return fConfigValue; }
    public void setfConfigValue(String fConfigValue) { this.fConfigValue = fConfigValue; }
    public String getfConfigType() { return fConfigType; }
    public void setfConfigType(String fConfigType) { this.fConfigType = fConfigType; }
    public String getfDescription() { return fDescription; }
    public void setfDescription(String fDescription) { this.fDescription = fDescription; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}
