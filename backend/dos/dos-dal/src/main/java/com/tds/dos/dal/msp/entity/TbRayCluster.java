package com.tds.dos.dal.msp.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * Ray集群实体
 */
@TableName("tb_ray_cluster")
public class TbRayCluster {
    @TableId
    private String fId;

    private String fClusterId;
    private String fClusterName;
    private String fHeadNodeId;
    private String fHeadAddress;
    private String fStatus;  // CREATING/RUNNING/STOPPING/STOPPED
    private String fParticipants;  // 参与节点列表JSON

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfClusterId() { return fClusterId; }
    public void setfClusterId(String fClusterId) { this.fClusterId = fClusterId; }
    public String getfClusterName() { return fClusterName; }
    public void setfClusterName(String fClusterName) { this.fClusterName = fClusterName; }
    public String getfHeadNodeId() { return fHeadNodeId; }
    public void setfHeadNodeId(String fHeadNodeId) { this.fHeadNodeId = fHeadNodeId; }
    public String getfHeadAddress() { return fHeadAddress; }
    public void setfHeadAddress(String fHeadAddress) { this.fHeadAddress = fHeadAddress; }
    public String getfStatus() { return fStatus; }
    public void setfStatus(String fStatus) { this.fStatus = fStatus; }
    public String getfParticipants() { return fParticipants; }
    public void setfParticipants(String fParticipants) { this.fParticipants = fParticipants; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}
