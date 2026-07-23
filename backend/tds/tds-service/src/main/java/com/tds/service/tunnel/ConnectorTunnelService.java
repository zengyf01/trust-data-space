package com.tds.service.tunnel;

import com.tds.common.dto.tunnel.TunnelRegistrationResult;
import com.tds.common.dto.tunnel.TunnelSession;
import com.tds.common.enums.ConnectorStatus;
import com.tds.common.enums.TunnelStatus;
import com.tds.common.util.SM2Util;
import com.tds.dal.entity.TbConnector;
import com.tds.dal.mapper.TbConnectorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 连接器隧道服务实现
 */
@Service
public class ConnectorTunnelService implements IConnectorTunnelService {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorTunnelService.class);

    @Autowired
    private IGmTunnelManager tunnelManager;

    @Autowired
    private TbConnectorMapper connectorMapper;

    @Value("${tunnel.host:localhost}")
    private String tunnelHost;

    @Override
    public TunnelRegistrationResult registerAndCreateTunnel(String sn, String publicKey, String certificate,
                                                              Integer connectorType, String institutionId, String spaceId) {
        logger.info("连接器注册并建立隧道: sn={}, connectorType={}", sn, connectorType);

        try {
            // 1. 验证SM2证书（简化验证：检查证书格式）
            if (certificate != null && !certificate.isEmpty()) {
                // TODO: 集成KBS进行完整的证书验证
                if (!verifyCertificate(certificate)) {
                    return TunnelRegistrationResult.failure("SM2证书验证失败");
                }
            }

            // 2. 查找或创建连接器记录
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TbConnector> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(TbConnector::getSn, sn);
            TbConnector connector = connectorMapper.selectOne(wrapper);

            if (connector == null) {
                // 创建新连接器记录
                connector = new TbConnector();
                connector.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
                connector.setSn(sn);
                connector.setName("Connector-" + sn);
                connector.setType(connectorType != null ? connectorType : 1);
                connector.setStatus(TunnelStatus.ACTIVE.getCode());
                connector.setInstitutionId(institutionId);
                connector.setfSpaceId(spaceId);
                connector.setRegisteredTime(LocalDateTime.now());
                connector.setfCreateTime(LocalDateTime.now());
                connector.setfUpdateTime(LocalDateTime.now());
                connector.setfDeleteMark(0);
                connectorMapper.insert(connector);
                logger.info("新连接器注册: sn={}", sn);
            } else {
                // 更新现有连接器状态
                connector.setStatus(TunnelStatus.ACTIVE.getCode());
                connector.setLastHeartbeat(LocalDateTime.now());
                connector.setfUpdateTime(LocalDateTime.now());
                connectorMapper.updateById(connector);
                logger.info("连接器更新: sn={}", sn);
            }

            // 3. 检查是否已有活跃隧道，有则关闭
            TunnelSession existingTunnel = tunnelManager.getActiveTunnelBySn(sn);
            if (existingTunnel != null) {
                logger.info("关闭已有隧道: sn={}, sessionId={}", sn, existingTunnel.getSessionId());
                tunnelManager.closeTunnelSession(existingTunnel.getSessionId());
            }

            // 4. 创建新隧道会话
            TunnelSession session = tunnelManager.createTunnelSession(sn, publicKey);

            // 5. 分配隧道端口
            int port = tunnelManager.allocateTunnelPort(session.getSessionId());

            // 6. 构建注册结果
            TunnelRegistrationResult result = TunnelRegistrationResult.success(
                session.getSessionId(),
                port,
                tunnelHost,
                tunnelManager.getServerPublicKey()
            );

            logger.info("连接器隧道创建成功: sn={}, sessionId={}, port={}", sn, session.getSessionId(), port);
            return result;

        } catch (Exception e) {
            logger.error("连接器隧道创建失败: sn={}", sn, e);
            return TunnelRegistrationResult.failure("隧道创建失败: " + e.getMessage());
        }
    }

    @Override
    public boolean completeMutualAuth(String sessionId, String clientSign) {
        logger.info("完成SM2双向认证: sessionId={}", sessionId);
        return tunnelManager.verifyMutualAuth(sessionId, clientSign);
    }

    @Override
    public void disconnectConnector(String sn) {
        logger.info("连接器断开连接: sn={}", sn);

        TunnelSession session = tunnelManager.getActiveTunnelBySn(sn);
        if (session != null) {
            tunnelManager.closeTunnelSession(session.getSessionId());
        }

        // 更新连接器状态为离线
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TbConnector> disconnectWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        disconnectWrapper.eq(TbConnector::getSn, sn);
        TbConnector connector = connectorMapper.selectOne(disconnectWrapper);
        if (connector != null) {
            connector.setStatus(ConnectorStatus.OFFLINE.getCode());
            connector.setfUpdateTime(LocalDateTime.now());
            connectorMapper.updateById(connector);
        }
    }

    @Override
    public TunnelStatus getTunnelStatus(String sn) {
        TunnelSession session = tunnelManager.getActiveTunnelBySn(sn);
        if (session == null) {
            return TunnelStatus.CLOSED;
        }
        return session.getStatus();
    }

    @Override
    public TunnelSession getActiveTunnel(String sn) {
        return tunnelManager.getActiveTunnelBySn(sn);
    }

    @Override
    public String getServerPublicKey() {
        return tunnelManager.getServerPublicKey();
    }

    /**
     * 验证SM2证书（简化实现）
     * TODO: 集成KBS进行完整的证书验证
     */
    private boolean verifyCertificate(String certificate) {
        // 简化验证：检查证书是否以常见格式开头
        if (certificate == null || certificate.isEmpty()) {
            return false;
        }
        // 实际生产环境应调用KBS验证证书
        return certificate.length() > 50;
    }
}
