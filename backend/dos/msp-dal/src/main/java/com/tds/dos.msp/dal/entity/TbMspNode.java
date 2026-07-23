package com.tds.dos.msp.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * MSP Node entity
 */
@TableName("tb_msp_node")
public class TbMspNode {
    @TableId
    private String fId;

    private String fNodeId;
    private String fNodeName;
    private Integer fStatus;
    private String fNodeMode;
    private String fEndpoint;
    private String fExternalEndpoint;
    private String fCapabilities;
    private String fTags;
    private LocalDateTime fLastHeartbeat;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfNodeId() { return fNodeId; }
    public void setfNodeId(String fNodeId) { this.fNodeId = fNodeId; }
    public String getfNodeName() { return fNodeName; }
    public void setfNodeName(String fNodeName) { this.fNodeName = fNodeName; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfNodeMode() { return fNodeMode; }
    public void setfNodeMode(String fNodeMode) { this.fNodeMode = fNodeMode; }
    public String getfEndpoint() { return fEndpoint; }
    public void setfEndpoint(String fEndpoint) { this.fEndpoint = fEndpoint; }
    public String getfExternalEndpoint() { return fExternalEndpoint; }
    public void setfExternalEndpoint(String fExternalEndpoint) { this.fExternalEndpoint = fExternalEndpoint; }
    public String getfCapabilities() { return fCapabilities; }
    public void setfCapabilities(String fCapabilities) { this.fCapabilities = fCapabilities; }
    public String getfTags() { return fTags; }
    public void setfTags(String fTags) { this.fTags = fTags; }
    public LocalDateTime getfLastHeartbeat() { return fLastHeartbeat; }
    public void setfLastHeartbeat(LocalDateTime fLastHeartbeat) { this.fLastHeartbeat = fLastHeartbeat; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}