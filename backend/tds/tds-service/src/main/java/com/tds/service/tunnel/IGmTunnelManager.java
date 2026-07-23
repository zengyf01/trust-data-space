package com.tds.service.tunnel;

import com.tds.common.dto.tunnel.TunnelCommandResult;
import com.tds.common.dto.tunnel.TunnelSession;
import com.tds.common.enums.TunnelStatus;

/**
 * GMSSL隧道管理器接口
 */
public interface IGmTunnelManager {

    /**
     * 创建隧道会话
     * @param sn 连接器序列号
     * @param clientPublicKey 客户端公钥(hex)
     * @return 隧道会话信息
     */
    TunnelSession createTunnelSession(String sn, String clientPublicKey);

    /**
     * 获取隧道会话
     */
    TunnelSession getTunnelSession(String sessionId);

    /**
     * 验证SM2双向认证
     * @param sessionId 会话ID
     * @param clientSign 客户端签名(对服务端随机数的签名)
     * @return 认证是否成功
     */
    boolean verifyMutualAuth(String sessionId, String clientSign);

    /**
     * 分配隧道端口
     */
    int allocateTunnelPort(String sessionId);

    /**
     * 通过隧道下发命令到连接器
     */
    TunnelCommandResult executeCommand(String sessionId, String command);

    /**
     * 关闭隧道会话
     */
    void closeTunnelSession(String sessionId);

    /**
     * 获取连接器对应的活动隧道
     */
    TunnelSession getActiveTunnelBySn(String sn);

    /**
     * 心跳保活
     */
    void keepAlive(String sessionId);

    /**
     * 更新会话状态
     */
    void updateSessionStatus(String sessionId, TunnelStatus status);

    /**
     * 检查并清理过期会话
     */
    void cleanupExpiredSessions();

    /**
     * 获取服务端公钥
     */
    String getServerPublicKey();
}
