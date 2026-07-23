package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 连接器实体
 */
@TableName("tb_connector")
public class TbConnector {

    @TableId
    private String id;

    /** 序列号(设备唯一标识) */
    private String sn;

    /** 连接器名称 */
    private String name;

    /** 连接器类型(1数据连接器,2沙盒连接器,3隐私计算连接器) */
    @TableField("type")
    private Integer type;

    /** 状态(1在线,2离线,3离线待注册) */
    private Integer status;

    /** 当前版本 */
    private String version;

    /** IP地址 */
    private String ipAddress;

    /** SSH端口 */
    private Integer sshPort;

    /** SSH用户名 */
    private String sshUsername;

    /** SSH密码(加密存储) */
    private String sshPassword;

    /** SSH私钥(加密存储) */
    private String sshPrivateKey;

    /** MAC地址 */
    private String macAddress;

    /** 最后心跳时间 */
    private LocalDateTime lastHeartbeat;

    /** 注册时间 */
    private LocalDateTime registeredTime;

    /** 所属机构ID */
    private String institutionId;

    /** 所属机构名称 */
    private String institutionName;

    /** 所属数据空间ID */
    private String fSpaceId;

    /** 所属区域 */
    private String region;

    /** 描述 */
    private String description;

    /** 租户ID */
    private String fTenantId;

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
    public String getSn() { return sn; }
    public void setSn(String sn) { this.sn = sn; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Integer getSshPort() { return sshPort; }
    public void setSshPort(Integer sshPort) { this.sshPort = sshPort; }
    public String getSshUsername() { return sshUsername; }
    public void setSshUsername(String sshUsername) { this.sshUsername = sshUsername; }
    public String getSshPassword() { return sshPassword; }
    public void setSshPassword(String sshPassword) { this.sshPassword = sshPassword; }
    public String getSshPrivateKey() { return sshPrivateKey; }
    public void setSshPrivateKey(String sshPrivateKey) { this.sshPrivateKey = sshPrivateKey; }
    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public LocalDateTime getRegisteredTime() { return registeredTime; }
    public void setRegisteredTime(LocalDateTime registeredTime) { this.registeredTime = registeredTime; }
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
    public String getfSpaceId() { return fSpaceId; }
    public void setfSpaceId(String fSpaceId) { this.fSpaceId = fSpaceId; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}