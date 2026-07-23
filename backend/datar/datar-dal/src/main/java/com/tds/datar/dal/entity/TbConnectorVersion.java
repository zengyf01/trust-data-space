package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("tb_connector_version")
public class TbConnectorVersion {

    @TableId
    private String fId;
    private String fConnectorId;
    private String fVersion;
    private String fFilePath;
    private Long fFileSize;
    private String fFileMd5;
    private String fChangeLog;
    private Integer fStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfConnectorId() { return fConnectorId; }
    public void setfConnectorId(String fConnectorId) { this.fConnectorId = fConnectorId; }
    public String getfVersion() { return fVersion; }
    public void setfVersion(String fVersion) { this.fVersion = fVersion; }
    public String getfFilePath() { return fFilePath; }
    public void setfFilePath(String fFilePath) { this.fFilePath = fFilePath; }
    public Long getfFileSize() { return fFileSize; }
    public void setfFileSize(Long fFileSize) { this.fFileSize = fFileSize; }
    public String getfFileMd5() { return fFileMd5; }
    public void setfFileMd5(String fFileMd5) { this.fFileMd5 = fFileMd5; }
    public String getfChangeLog() { return fChangeLog; }
    public void setfChangeLog(String fChangeLog) { this.fChangeLog = fChangeLog; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}