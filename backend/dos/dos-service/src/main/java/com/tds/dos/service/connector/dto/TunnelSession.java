package com.tds.dos.service.connector.dto;

import java.time.LocalDateTime;

/**
 * 隧道会话数据对象
 */
public class TunnelSession {

    private String sessionId;
    private String sn;
    private String clientPublicKey;
    private String serverPublicKey;
    private int tunnelPort;
    private String tunnelHost;
    private Integer status;  // 0=PENDING_AUTH, 1=ACTIVE, 2=INACTIVE, 3=CLOSED
    private String encryptedSymmetricKey;
    private LocalDateTime createdTime;
    private LocalDateTime lastHeartbeat;

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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getEncryptedSymmetricKey() { return encryptedSymmetricKey; }
    public void setEncryptedSymmetricKey(String encryptedSymmetricKey) { this.encryptedSymmetricKey = encryptedSymmetricKey; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
}
