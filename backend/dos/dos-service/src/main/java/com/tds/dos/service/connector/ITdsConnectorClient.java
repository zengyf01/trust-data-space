package com.tds.dos.service.connector;

import com.tds.dos.service.connector.dto.ConnectorInfo;
import com.tds.dos.service.connector.dto.TunnelCommandResult;
import com.tds.dos.service.connector.dto.TunnelSession;

import java.util.List;

/**
 * TDS连接器客户端接口 - DOS通过此接口与TDS交互控制连接器
 */
public interface ITdsConnectorClient {

    /**
     * 获取可用连接器列表
     * @param spaceId 数据空间ID
     * @param type 连接器类型(1数据连接器,2沙盒连接器,3隐私计算连接器)
     * @return 可用连接器列表
     */
    List<ConnectorInfo> getAvailableConnectors(String spaceId, Integer type);

    /**
     * 申请隧道会话
     * @param sn 连接器序列号
     * @return 隧道会话信息
     */
    TunnelSession applyTunnelSession(String sn);

    /**
     * 通过隧道执行命令
     * @param sessionId 会话ID
     * @param command 命令内容
     * @return 命令执行结果
     */
    TunnelCommandResult executeViaTunnel(String sessionId, String command);

    /**
     * 释放隧道会话
     * @param sessionId 会话ID
     */
    void releaseTunnelSession(String sessionId);

    /**
     * 查询连接器隧道状态
     * @param sn 连接器序列号
     * @return 隧道状态信息
     */
    TunnelStatus getTunnelStatus(String sn);

    /**
     * 获取服务端公钥
     * @return SM2公钥
     */
    String getServerPublicKey();

    /**
     * 隧道状态DTO
     */
    class TunnelStatus {
        private String sn;
        private String status;
        private Integer statusCode;
        private String sessionId;
        private Integer tunnelPort;
        private String tunnelHost;

        // Getters and Setters
        public String getSn() { return sn; }
        public void setSn(String sn) { this.sn = sn; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getStatusCode() { return statusCode; }
        public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public Integer getTunnelPort() { return tunnelPort; }
        public void setTunnelPort(Integer tunnelPort) { this.tunnelPort = tunnelPort; }
        public String getTunnelHost() { return tunnelHost; }
        public void setTunnelHost(String tunnelHost) { this.tunnelHost = tunnelHost; }
    }
}
