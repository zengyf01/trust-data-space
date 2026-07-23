package com.tds.common.dto.tunnel;

import com.tds.common.enums.TunnelStatus;

/**
 * 隧道注册结果
 */
public class TunnelRegistrationResult {

    private String sessionId;          // 会话ID
    private int tunnelPort;            // 分配的隧道端口
    private String tunnelHost;         // 隧道主机地址
    private String serverPublicKey;    // 服务端公钥
    private TunnelStatus status;       // 状态
    private String message;            // 消息

    public TunnelRegistrationResult() {}

    public static TunnelRegistrationResult success(String sessionId, int tunnelPort, String tunnelHost, String serverPublicKey) {
        TunnelRegistrationResult result = new TunnelRegistrationResult();
        result.setSessionId(sessionId);
        result.setTunnelPort(tunnelPort);
        result.setTunnelHost(tunnelHost);
        result.setServerPublicKey(serverPublicKey);
        result.setStatus(TunnelStatus.PENDING_AUTH);
        result.setMessage("隧道创建成功，等待SM2双向认证");
        return result;
    }

    public static TunnelRegistrationResult failure(String message) {
        TunnelRegistrationResult result = new TunnelRegistrationResult();
        result.setStatus(TunnelStatus.CLOSED);
        result.setMessage(message);
        return result;
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public int getTunnelPort() { return tunnelPort; }
    public void setTunnelPort(int tunnelPort) { this.tunnelPort = tunnelPort; }
    public String getTunnelHost() { return tunnelHost; }
    public void setTunnelHost(String tunnelHost) { this.tunnelHost = tunnelHost; }
    public String getServerPublicKey() { return serverPublicKey; }
    public void setServerPublicKey(String serverPublicKey) { this.serverPublicKey = serverPublicKey; }
    public TunnelStatus getStatus() { return status; }
    public void setStatus(TunnelStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
