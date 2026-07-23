package com.tds.service.tunnel;

import com.tds.common.dto.tunnel.TunnelCommandResult;
import com.tds.common.dto.tunnel.TunnelSession;
import com.tds.common.enums.TunnelStatus;
import com.tds.common.util.SM2Util;
import com.tds.common.util.SM4Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * GMSSL隧道管理器实现
 */
@Service
public class GmTunnelManager implements IGmTunnelManager {

    private static final Logger logger = LoggerFactory.getLogger(GmTunnelManager.class);

    private static final String TUNNEL_SESSION_PREFIX = "tds:tunnel:session:";
    private static final String TUNNEL_SN_PREFIX = "tds:tunnel:sn:";
    private static final String TUNNEL_PORT_PREFIX = "tds:tunnel:port:";
    private static final long SESSION_TTL_MINUTES = 30;

    @Value("${tunnel.port-range-start:34400}")
    private int portRangeStart;

    @Value("${tunnel.port-range-end:34500}")
    private int portRangeEnd;

    @Value("${tunnel.host:localhost}")
    private String tunnelHost;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 内存缓存，用于存储服务端随机数（认证用）
    private final Map<String, String> serverRandomMap = new ConcurrentHashMap<>();

    // SM2密钥对
    private String serverPrivateKey;
    private String serverPublicKey;

    public GmTunnelManager() {
        // 生成服务端SM2密钥对
        try {
            java.util.Map<String, String> keyPair = SM2Util.generateKeyPair();
            this.serverPublicKey = keyPair.get("publicKey");
            this.serverPrivateKey = keyPair.get("privateKey");
            logger.info("GMSSL隧道管理器初始化，服务端公钥: {}", serverPublicKey.substring(0, 20) + "...");
        } catch (Exception e) {
            logger.error("SM2密钥对生成失败", e);
            throw new RuntimeException("GMSSL隧道管理器初始化失败", e);
        }
    }

    @Override
    public TunnelSession createTunnelSession(String sn, String clientPublicKey) {
        logger.info("创建隧道会话: sn={}", sn);

        // 生成会话ID
        String sessionId = "tun_" + UUID.randomUUID().toString().replace("-", "");

        // 生成服务端随机数用于双向认证
        String serverRandom = generateRandomString(32);
        serverRandomMap.put(sessionId, serverRandom);

        // 创建会话对象
        TunnelSession session = new TunnelSession();
        session.setSessionId(sessionId);
        session.setSn(sn);
        session.setClientPublicKey(clientPublicKey);
        session.setServerPublicKey(serverPublicKey);
        session.setStatus(TunnelStatus.PENDING_AUTH);
        session.setCreatedTime(LocalDateTime.now());
        session.setLastHeartbeat(LocalDateTime.now());

        // 存储到Redis
        String sessionKey = TUNNEL_SESSION_PREFIX + sessionId;
        redisTemplate.opsForHash().putAll(sessionKey, Map.of(
            "sessionId", sessionId,
            "sn", sn,
            "clientPublicKey", clientPublicKey,
            "serverPublicKey", serverPublicKey,
            "serverRandom", serverRandom,
            "status", String.valueOf(TunnelStatus.PENDING_AUTH.getCode()),
            "createdTime", session.getCreatedTime().toString()
        ));
        redisTemplate.expire(sessionKey, Duration.ofMinutes(SESSION_TTL_MINUTES));

        // SN到sessionId的映射
        String snKey = TUNNEL_SN_PREFIX + sn;
        redisTemplate.opsForValue().set(snKey, sessionId, Duration.ofMinutes(SESSION_TTL_MINUTES));

        logger.info("隧道会话创建成功: sessionId={}, sn={}", sessionId, sn);
        return session;
    }

    @Override
    public TunnelSession getTunnelSession(String sessionId) {
        String sessionKey = TUNNEL_SESSION_PREFIX + sessionId;
        Map<Object, Object> map = redisTemplate.opsForHash().entries(sessionKey);
        if (map.isEmpty()) {
            return null;
        }
        return mapToSession(map);
    }

    @Override
    public boolean verifyMutualAuth(String sessionId, String clientSign) {
        TunnelSession session = getTunnelSession(sessionId);
        if (session == null) {
            logger.warn("隧道会话不存在: sessionId={}", sessionId);
            return false;
        }

        String serverRandom = serverRandomMap.get(sessionId);
        if (serverRandom == null) {
            logger.warn("服务端随机数不存在: sessionId={}", sessionId);
            return false;
        }

        // 验证客户端签名（客户端对服务端随机数签名）
        try {
            boolean valid = SM2Util.verify(session.getClientPublicKey(), serverRandom, clientSign);
            if (valid) {
                // 认证成功，更新状态
                updateSessionStatus(sessionId, TunnelStatus.ACTIVE);

                // 生成SM4会话密钥并用SM2加密
                String sm4Key = SM4Util.generateKey();
                String encryptedKey = SM2Util.encrypt(session.getClientPublicKey(), sm4Key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                session.setEncryptedSymmetricKey(encryptedKey);

                // 更新Redis
                String sessionKey = TUNNEL_SESSION_PREFIX + sessionId;
                redisTemplate.opsForHash().put(sessionKey, "encryptedSymmetricKey", encryptedKey);
                redisTemplate.opsForHash().put(sessionKey, "status", String.valueOf(TunnelStatus.ACTIVE.getCode()));

                // 清理服务端随机数
                serverRandomMap.remove(sessionId);

                logger.info("SM2双向认证成功: sessionId={}", sessionId);
            } else {
                logger.warn("SM2签名验证失败: sessionId={}", sessionId);
            }
            return valid;
        } catch (Exception e) {
            logger.error("SM2双向认证异常: sessionId={}", sessionId, e);
            return false;
        }
    }

    @Override
    public int allocateTunnelPort(String sessionId) {
        SecureRandom random = new SecureRandom();
        int attempts = 0;
        while (attempts < 100) {
            int port = portRangeStart + random.nextInt(portRangeEnd - portRangeStart);
            String portKey = TUNNEL_PORT_PREFIX + port;

            Boolean success = redisTemplate.opsForValue().setIfAbsent(portKey, sessionId, Duration.ofMinutes(SESSION_TTL_MINUTES));
            if (success != null && success) {
                // 更新会话的端口信息
                String sessionKey = TUNNEL_SESSION_PREFIX + sessionId;
                java.util.Map<String, String> portInfo = new java.util.HashMap<>();
                portInfo.put("tunnelPort", String.valueOf(port));
                portInfo.put("tunnelHost", tunnelHost);
                redisTemplate.opsForHash().putAll(sessionKey, portInfo);
                logger.info("分配隧道端口: sessionId={}, port={}", sessionId, port);
                return port;
            }
            attempts++;
        }
        throw new RuntimeException("无可用隧道端口");
    }

    @Override
    public TunnelCommandResult executeCommand(String sessionId, String command) {
        long startTime = System.currentTimeMillis();
        TunnelSession session = getTunnelSession(sessionId);
        if (session == null) {
            return TunnelCommandResult.failure(sessionId, "隧道会话不存在");
        }
        if (session.getStatus() != TunnelStatus.ACTIVE) {
            return TunnelCommandResult.failure(sessionId, "隧道未激活");
        }

        try {
            // TODO: 通过Netty GMSSL通道发送命令到连接器
            // 目前模拟实现
            logger.info("通过隧道下发命令: sessionId={}, command={}", sessionId, command);

            // 模拟命令执行
            Thread.sleep(100);
            String output = "Command executed successfully";

            return TunnelCommandResult.success(sessionId, output, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            logger.error("命令执行异常: sessionId={}", sessionId, e);
            return TunnelCommandResult.failure(sessionId, e.getMessage());
        }
    }

    @Override
    public void closeTunnelSession(String sessionId) {
        TunnelSession session = getTunnelSession(sessionId);
        if (session != null) {
            // 更新状态
            updateSessionStatus(sessionId, TunnelStatus.CLOSED);

            // 清理端口
            String portKey = TUNNEL_PORT_PREFIX + session.getTunnelPort();
            redisTemplate.delete(portKey);

            // 清理SN映射
            String snKey = TUNNEL_SN_PREFIX + session.getSn();
            redisTemplate.delete(snKey);

            // 清理会话
            String sessionKey = TUNNEL_SESSION_PREFIX + sessionId;
            redisTemplate.delete(sessionKey);

            // 清理随机数
            serverRandomMap.remove(sessionId);

            logger.info("隧道会话已关闭: sessionId={}, sn={}", sessionId, session.getSn());
        }
    }

    @Override
    public TunnelSession getActiveTunnelBySn(String sn) {
        String snKey = TUNNEL_SN_PREFIX + sn;
        String sessionId = redisTemplate.opsForValue().get(snKey);
        if (sessionId == null) {
            return null;
        }
        return getTunnelSession(sessionId);
    }

    @Override
    public void keepAlive(String sessionId) {
        TunnelSession session = getTunnelSession(sessionId);
        if (session != null) {
            session.setLastHeartbeat(LocalDateTime.now());
            String sessionKey = TUNNEL_SESSION_PREFIX + sessionId;
            redisTemplate.opsForHash().put(sessionKey, "lastHeartbeat", session.getLastHeartbeat().toString());

            // 延长TTL
            redisTemplate.expire(sessionKey, Duration.ofMinutes(SESSION_TTL_MINUTES));
        }
    }

    @Override
    public void updateSessionStatus(String sessionId, TunnelStatus status) {
        String sessionKey = TUNNEL_SESSION_PREFIX + sessionId;
        redisTemplate.opsForHash().put(sessionKey, "status", String.valueOf(status.getCode()));
    }

    @Override
    public void cleanupExpiredSessions() {
        // 清理过期会话由Redis TTL自动处理
        // 这里可以添加额外的清理逻辑
    }

    /**
     * 获取服务端公钥
     */
    public String getServerPublicKey() {
        return serverPublicKey;
    }

    /**
     * 生成随机字符串
     */
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 将Redis Hash转换为TunnelSession对象
     */
    private TunnelSession mapToSession(Map<Object, Object> map) {
        TunnelSession session = new TunnelSession();
        session.setSessionId((String) map.get("sessionId"));
        session.setSn((String) map.get("sn"));
        session.setClientPublicKey((String) map.get("clientPublicKey"));
        session.setServerPublicKey((String) map.get("serverPublicKey"));
        session.setEncryptedSymmetricKey((String) map.get("encryptedSymmetricKey"));

        String portStr = (String) map.get("tunnelPort");
        if (portStr != null) {
            session.setTunnelPort(Integer.parseInt(portStr));
        }
        session.setTunnelHost((String) map.get("tunnelHost"));

        String statusStr = (String) map.get("status");
        if (statusStr != null) {
            session.setStatus(TunnelStatus.fromCode(Integer.parseInt(statusStr)));
        }

        String createdTimeStr = (String) map.get("createdTime");
        if (createdTimeStr != null) {
            session.setCreatedTime(LocalDateTime.parse(createdTimeStr));
        }

        String lastHeartbeatStr = (String) map.get("lastHeartbeat");
        if (lastHeartbeatStr != null) {
            session.setLastHeartbeat(LocalDateTime.parse(lastHeartbeatStr));
        }

        return session;
    }
}
