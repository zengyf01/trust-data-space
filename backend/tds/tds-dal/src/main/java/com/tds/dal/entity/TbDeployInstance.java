package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 部署实例实体
 */
@TableName("tb_deploy_instance")
public class TbDeployInstance {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fInstanceCode;          // 实例编码
    private String fInstanceName;          // 实例名称
    private String fNodeId;               // 节点ID
    private String fServiceType;          // 服务类型：TDS/DOS/DATAR
    private String fServiceVersion;       // 服务版本
    private Integer fStatus;              // 状态
    private String fAccessUrl;            // 访问地址
    private Integer fReplicaCount;         // 副本数
    private Integer fCurrentReplicas;      // 当前副本数
    private String fTenantId;            // 租户ID
    private LocalDateTime fStartTime;      // 启动时间
    private LocalDateTime fCreateTime;     // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    private Integer fDeleteMark;           // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfInstanceCode() { return fInstanceCode; }
    public void setfInstanceCode(String fInstanceCode) { this.fInstanceCode = fInstanceCode; }
    public String getfInstanceName() { return fInstanceName; }
    public void setfInstanceName(String fInstanceName) { this.fInstanceName = fInstanceName; }
    public String getfNodeId() { return fNodeId; }
    public void setfNodeId(String fNodeId) { this.fNodeId = fNodeId; }
    public String getfServiceType() { return fServiceType; }
    public void setfServiceType(String fServiceType) { this.fServiceType = fServiceType; }
    public String getfServiceVersion() { return fServiceVersion; }
    public void setfServiceVersion(String fServiceVersion) { this.fServiceVersion = fServiceVersion; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfAccessUrl() { return fAccessUrl; }
    public void setfAccessUrl(String fAccessUrl) { this.fAccessUrl = fAccessUrl; }
    public Integer getfReplicaCount() { return fReplicaCount; }
    public void setfReplicaCount(Integer fReplicaCount) { this.fReplicaCount = fReplicaCount; }
    public Integer getfCurrentReplicas() { return fCurrentReplicas; }
    public void setfCurrentReplicas(Integer fCurrentReplicas) { this.fCurrentReplicas = fCurrentReplicas; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfStartTime() { return fStartTime; }
    public void setfStartTime(LocalDateTime fStartTime) { this.fStartTime = fStartTime; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}