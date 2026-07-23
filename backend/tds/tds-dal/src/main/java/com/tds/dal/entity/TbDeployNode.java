package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 部署节点实体
 */
@TableName("tb_deploy_node")
public class TbDeployNode {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fNodeCode;             // 节点编码
    private String fNodeName;             // 节点名称
    private String fNodeType;             // 节点类型：CENTER/EDGE
    private String fDeployMode;           // 部署模式
    private String fIpAddress;            // IP地址
    private Integer fPort;               // 端口
    private String fRegion;              // 区域
    private Integer fCpuCores;            // CPU核心数
    private Long fMemorySize;            // 内存大小(MB)
    private Long fDiskSize;             // 磁盘大小(GB)
    private Integer fStatus;              // 状态
    private String fTenantId;            // 租户ID
    private LocalDateTime fLastHeartbeat; // 最后心跳时间
    private LocalDateTime fCreateTime;    // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    private Integer fDeleteMark;           // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfNodeCode() { return fNodeCode; }
    public void setfNodeCode(String fNodeCode) { this.fNodeCode = fNodeCode; }
    public String getfNodeName() { return fNodeName; }
    public void setfNodeName(String fNodeName) { this.fNodeName = fNodeName; }
    public String getfNodeType() { return fNodeType; }
    public void setfNodeType(String fNodeType) { this.fNodeType = fNodeType; }
    public String getfDeployMode() { return fDeployMode; }
    public void setfDeployMode(String fDeployMode) { this.fDeployMode = fDeployMode; }
    public String getfIpAddress() { return fIpAddress; }
    public void setfIpAddress(String fIpAddress) { this.fIpAddress = fIpAddress; }
    public Integer getfPort() { return fPort; }
    public void setfPort(Integer fPort) { this.fPort = fPort; }
    public String getfRegion() { return fRegion; }
    public void setfRegion(String fRegion) { this.fRegion = fRegion; }
    public Integer getfCpuCores() { return fCpuCores; }
    public void setfCpuCores(Integer fCpuCores) { this.fCpuCores = fCpuCores; }
    public Long getfMemorySize() { return fMemorySize; }
    public void setfMemorySize(Long fMemorySize) { this.fMemorySize = fMemorySize; }
    public Long getfDiskSize() { return fDiskSize; }
    public void setfDiskSize(Long fDiskSize) { this.fDiskSize = fDiskSize; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfLastHeartbeat() { return fLastHeartbeat; }
    public void setfLastHeartbeat(LocalDateTime fLastHeartbeat) { this.fLastHeartbeat = fLastHeartbeat; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}