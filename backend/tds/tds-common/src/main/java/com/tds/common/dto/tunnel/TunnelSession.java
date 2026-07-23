package com.tds.common.dto.tunnel;

import com.tds.common.enums.TunnelStatus;

import java.time.LocalDateTime;

/**
 * 隧道会话数据对象
 */
public class TunnelSession {

    private String sessionId;                    // 隧道会话ID
    private String sn;                           // 关联的连接器SN
    private String clientPublicKey;              // 客户端SM2公钥
    private String serverPublicKey;              // 服务端SM2公钥
    private int tunnelPort;                      // 分配的隧道端口
    private String tunnelHost;                   // 隧道主机地址
    private TunnelStatus status;                 // 状态
    private String encryptedSymmetricKey;        // SM2加密后的SM4会话密钥
    private LocalDateTime createdTime;           // 创建时间
    private LocalDateTime lastHeartbeat;         // 最后心跳时间
    private String institutionId;                // 机构ID
    private String fSpaceId;                     // 数据空间ID
    private Integer connectorType;               // 连接器类型

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getSn() { return sn; }
    public void setSn(String sn) { this.sn = sn; }
    public String getClientPublicKey() { return clientPublicKey; }
    public void setClientPublicKey(String clientPublicKey) { this.clientPublicKey = clientPublicKey; }
    public String getServerPublicKey() { return serverPublicKey; }
    public void setServerPublicKey(String serverPublicKey) { this.serverPublicKey = serverPublicKey; }
    public int getTunnelPort() { return tunnelPort; }
    public void setTunnelPort(int tunnelPort) { this.tunnelPort = tunnelPort; }
    public String getTunnelHost() { return tunnelHost; }
    public void setTunnelHost(String tunnelHost) { this.tunnelHost = tunnelHost; }
    public TunnelStatus getStatus() { return status; }
    public void setStatus(TunnelStatus status) { this.status = status; }
    public String getEncryptedSymmetricKey() { return encryptedSymmetricKey; }
    public void setEncryptedSymmetricKey(String encryptedSymmetricKey) { this.encryptedSymmetricKey = encryptedSymmetricKey; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
    public String getfSpaceId() { return fSpaceId; }
    public void setfSpaceId(String fSpaceId) { this.fSpaceId = fSpaceId; }
    public Integer getConnectorType() { return connectorType; }
    public void setConnectorType(Integer connectorType) { this.connectorType = connectorType; }
}
