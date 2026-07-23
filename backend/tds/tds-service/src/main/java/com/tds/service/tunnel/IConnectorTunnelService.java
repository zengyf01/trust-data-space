package com.tds.service.tunnel;

import com.tds.common.dto.tunnel.TunnelRegistrationResult;
import com.tds.common.dto.tunnel.TunnelSession;
import com.tds.common.enums.TunnelStatus;

/**
 * 连接器隧道服务接口 - 处理连接器注册时的隧道建立
 */
public interface IConnectorTunnelService {

    /**
     * 连接器注册并建立GMSSL隧道
     * @param sn 连接器序列号
     * @param publicKey SM2公钥
     * @param certificate SM2证书
     * @param connectorType 连接器类型
     * @param institutionId 机构ID
     * @param spaceId 数据空间ID
     * @return 隧道注册结果
     */
    TunnelRegistrationResult registerAndCreateTunnel(String sn, String publicKey, String certificate,
                                                      Integer connectorType, String institutionId, String spaceId);

    /**
     * 完成SM2双向认证
     */
    boolean completeMutualAuth(String sessionId, String clientSign);

    /**
     * 连接器断开连接，清理隧道
     */
    void disconnectConnector(String sn);

    /**
     * 获取连接器的隧道状态
     */
    TunnelStatus getTunnelStatus(String sn);

    /**
     * 获取连接器的活动隧道会话
     */
    TunnelSession getActiveTunnel(String sn);

    /**
     * 获取服务端公钥（用于连接器初始化）
     */
    String getServerPublicKey();
}
