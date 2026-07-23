package com.tds.service.tunnel;

import com.tds.common.dto.tunnel.TunnelSession;
import com.tds.common.util.SM2Util;
import com.tds.common.util.SM4Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 对等通信服务实现
 */
@Service
public class PeerCommunicationService implements IPeerCommunicationService {

    private static final Logger logger = LoggerFactory.getLogger(PeerCommunicationService.class);

    private static final String PEER_SESSION_PREFIX = "tds:peer:session:";
    private static final String PEER_CONNECT_PREFIX = "tds:peer:connect:";
    private static final long PEER_SESSION_TTL_MINUTES = 60;

    @Autowired
    private IGmTunnelManager tunnelManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PeerSession initiatePeerConnection(String initiatorSn, String targetSn) {
        logger.info("发起对等连接: initiator={}, target={}", initiatorSn, targetSn);

        // 1. 获取双方的活动隧道
        TunnelSession initiatorTunnel = tunnelManager.getActiveTunnelBySn(initiatorSn);
        TunnelSession targetTunnel = tunnelManager.getActiveTunnelBySn(targetSn);

        if (initiatorTunnel == null) {
            throw new RuntimeException("发起方隧道不存在: " + initiatorSn);
        }
        if (targetTunnel == null) {
            throw new RuntimeException("目标方隧道不存在: " + targetSn);
        }

        // 2. 生成对等会话ID
        String peerSessionId = "peer_" + UUID.randomUUID().toString().replace("-", "");

        // 3. 生成对等会话密钥并用双方的SM2公钥加密
        String peerKey = SM4Util.generateKey();
        String encryptedKeyForInitiator;
        String encryptedKeyForTarget;
        try {
            encryptedKeyForInitiator = SM2Util.encrypt(initiatorTunnel.getClientPublicKey(), peerKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            encryptedKeyForTarget = SM2Util.encrypt(targetTunnel.getClientPublicKey(), peerKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("SM2加密失败", e);
        }

        // 4. 生成连接确认随机数
        String confirmToken = generateRandomString(32);

        // 5. 创建对等会话
        PeerSession peerSession = new PeerSession();
        peerSession.setPeerSessionId(peerSessionId);
        peerSession.setInitiatorSn(initiatorSn);
        peerSession.setTargetSn(targetSn);
        peerSession.setInitiatorSessionId(initiatorTunnel.getSessionId());
        peerSession.setTargetSessionId(targetTunnel.getSessionId());
        peerSession.setEncryptedKey(encryptedKeyForInitiator + ":" + encryptedKeyForTarget);
        peerSession.setStatus("PENDING");
        peerSession.setCreateTime(System.currentTimeMillis());

        // 6. 存储到Redis
        String sessionKey = PEER_SESSION_PREFIX + peerSessionId;
        redisTemplate.opsForHash().putAll(sessionKey, Map.of(
            "peerSessionId", peerSessionId,
            "initiatorSn", initiatorSn,
            "targetSn", targetSn,
            "initiatorSessionId", initiatorTunnel.getSessionId(),
            "targetSessionId", targetTunnel.getSessionId(),
            "encryptedKey", peerSession.getEncryptedKey(),
            "status", "PENDING",
            "confirmToken", confirmToken,
            "createTime", String.valueOf(peerSession.getCreateTime())
        ));
        redisTemplate.expire(sessionKey, Duration.ofMinutes(PEER_SESSION_TTL_MINUTES));

        // 7. 存储连接请求（用于目标方确认）
        String connectKey = PEER_CONNECT_PREFIX + targetSn + ":" + initiatorSn;
        redisTemplate.opsForValue().set(connectKey, peerSessionId, Duration.ofMinutes(5));

        logger.info("对等连接发起成功: peerSessionId={}", peerSessionId);
        return peerSession;
    }

    @Override
    public boolean confirmPeerConnection(String peerSessionId, String targetSign) {
        logger.info("确认对等连接: peerSessionId={}", peerSessionId);

        String sessionKey = PEER_SESSION_PREFIX + peerSessionId;
        Map<Object, Object> map = redisTemplate.opsForHash().entries(sessionKey);
        if (map.isEmpty()) {
            logger.warn("对等会话不存在: peerSessionId={}", peerSessionId);
            return false;
        }

        String status = (String) map.get("status");
        if (!"PENDING".equals(status)) {
            logger.warn("对等会话状态不是PENDING: peerSessionId={}, status={}", peerSessionId, status);
            return false;
        }

        String targetSn = (String) map.get("targetSn");
        String targetPublicKey = tunnelManager.getActiveTunnelBySn(targetSn).getClientPublicKey();
        String confirmToken = (String) map.get("confirmToken");

        // 验证目标方签名（使用confirmToken）
        try {
            boolean valid = SM2Util.verify(targetPublicKey, confirmToken, targetSign);
            if (valid) {
                // 更新状态为ACTIVE
                redisTemplate.opsForHash().put(sessionKey, "status", "ACTIVE");
                logger.info("对等连接确认成功: peerSessionId={}", peerSessionId);
            }
            return valid;
        } catch (Exception e) {
            logger.error("对等连接确认异常: peerSessionId={}", peerSessionId, e);
            return false;
        }
    }

    @Override
    public byte[] sendPeerData(String peerSessionId, byte[] data) {
        // TODO: 通过Netty GMSSL通道发送数据
        logger.info("通过对等连接发送数据: peerSessionId={}, length={}", peerSessionId, data.length);
        // 模拟实现：直接返回数据
        return data;
    }

    @Override
    public void closePeerSession(String peerSessionId) {
        logger.info("关闭对等连接: peerSessionId={}", peerSessionId);

        String sessionKey = PEER_SESSION_PREFIX + peerSessionId;
        Map<Object, Object> map = redisTemplate.opsForHash().entries(sessionKey);
        if (!map.isEmpty()) {
            String initiatorSn = (String) map.get("initiatorSn");
            String targetSn = (String) map.get("targetSn");

            // 清理连接请求
            redisTemplate.delete(PEER_CONNECT_PREFIX + targetSn + ":" + initiatorSn);
        }

        redisTemplate.delete(sessionKey);
    }

    @Override
    public PeerSession getPeerSession(String peerSessionId) {
        String sessionKey = PEER_SESSION_PREFIX + peerSessionId;
        Map<Object, Object> map = redisTemplate.opsForHash().entries(sessionKey);
        if (map.isEmpty()) {
            return null;
        }

        PeerSession peerSession = new PeerSession();
        peerSession.setPeerSessionId((String) map.get("peerSessionId"));
        peerSession.setInitiatorSn((String) map.get("initiatorSn"));
        peerSession.setTargetSn((String) map.get("targetSn"));
        peerSession.setInitiatorSessionId((String) map.get("initiatorSessionId"));
        peerSession.setTargetSessionId((String) map.get("targetSessionId"));
        peerSession.setEncryptedKey((String) map.get("encryptedKey"));
        peerSession.setStatus((String) map.get("status"));
        String createTimeStr = (String) map.get("createTime");
        if (createTimeStr != null) {
            peerSession.setCreateTime(Long.parseLong(createTimeStr));
        }
        return peerSession;
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
}
