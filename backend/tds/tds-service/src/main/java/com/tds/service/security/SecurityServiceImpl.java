package com.tds.service.security;

import com.tds.common.util.SM4Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 安全服务实现
 * 提供数据加密、远程认证、密钥管理等安全功能
 */
@Slf4j
@Service
public class SecurityServiceImpl implements ISecurityService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AttestationService attestationService;

    @Autowired
    private KbsService kbsService;

    private static final String KEY_PREFIX = "tds:security:key:";
    private static final String KEYSTORE_PREFIX = "tds:security:keystore:";
    private static final Duration DEFAULT_TTL = Duration.ofDays(365);

    // ==================== 数据加密 ====================

    @Override
    public String encrypt(String plaintext, String key, String mode, String iv) {
        if (plaintext == null || key == null) {
            throw new IllegalArgumentException("明文和密钥不能为空");
        }

        String upperMode = (mode != null ? mode : "ECB").toUpperCase();
        switch (upperMode) {
            case "ECB":
                return SM4Util.encryptECB(key, plaintext);
            case "CBC":
                if (iv == null) {
                    throw new IllegalArgumentException("CBC模式需要IV向量");
                }
                return SM4Util.encryptCBC(key, iv, plaintext);
            case "GCM":
                if (iv == null) {
                    throw new IllegalArgumentException("GCM模式需要IV向量");
                }
                return SM4Util.encryptGCM(key, iv, plaintext);
            default:
                throw new IllegalArgumentException("不支持的加密模式: " + mode);
        }
    }

    @Override
    public String decrypt(String ciphertext, String key, String mode, String iv) {
        if (ciphertext == null || key == null) {
            throw new IllegalArgumentException("密文和密钥不能为空");
        }

        String upperMode = (mode != null ? mode : "ECB").toUpperCase();
        switch (upperMode) {
            case "ECB":
                return SM4Util.decryptECB(key, ciphertext);
            case "CBC":
                if (iv == null) {
                    throw new IllegalArgumentException("CBC模式需要IV向量");
                }
                return SM4Util.decryptCBC(key, iv, ciphertext);
            case "GCM":
                if (iv == null) {
                    throw new IllegalArgumentException("GCM模式需要IV向量");
                }
                return SM4Util.decryptGCM(key, iv, ciphertext);
            default:
                throw new IllegalArgumentException("不支持的加密模式: " + mode);
        }
    }

    @Override
    public String generateKey() {
        return SM4Util.generateKey();
    }

    @Override
    public String hash(String data) {
        return SM4Util.hash(data);
    }

    // ==================== 远程认证 (TEE) ====================

    @Override
    public Map<String, Object> createAttestationRequest(String target, Map<String, Object> report) {
        return attestationService.createAttestationRequest(target, report);
    }

    @Override
    public Map<String, Object> verifyAttestationResponse(Map<String, Object> attestationResponse) {
        Map<String, Object> result = new HashMap<>();
        String requestId = (String) attestationResponse.get("requestId");
        Integer status = (Integer) attestationResponse.get("status");

        if (status == null) {
            result.put("valid", false);
            result.put("error", "Invalid response: missing status");
            return result;
        }

        // 状态码: 0=未认证, 1=认证中, 2=已认证, 3=认证失败
        switch (status) {
            case 2:
                result.put("valid", true);
                result.put("status", "ATTESTED");
                result.put("requestId", requestId);
                break;
            case 3:
                result.put("valid", false);
                result.put("status", "FAILED");
                result.put("error", attestationResponse.get("error"));
                break;
            default:
                result.put("valid", false);
                result.put("status", "PENDING");
                result.put("requestId", requestId);
                break;
        }

        return result;
    }

    @Override
    public Map<String, Object> getQuote(String targetId) {
        return attestationService.generateQuote(targetId, null);
    }

    // ==================== KBS密钥服务 ====================

    @Override
    public Map<String, String> generateDataKey(String keyId, String algorithm) {
        return kbsService.generateDataKey(keyId, algorithm);
    }

    @Override
    public Map<String, String> getEncryptedDataKey(String keyId, String publicKey) {
        return kbsService.getEncryptedDataKey(keyId, publicKey);
    }

    @Override
    public Map<String, String> rotateKey(String keyId) {
        return kbsService.rotateKey(keyId, true);
    }

    @Override
    public void deleteKey(String keyId) {
        kbsService.deleteKey(keyId);
    }

    // ==================== 密钥存储 ====================

    @Override
    public void storeKey(String keyId, String encryptedKey, String owner) {
        String storeKey = KEYSTORE_PREFIX + keyId;
        Map<String, String> keyData = new HashMap<>();
        keyData.put("encryptedKey", encryptedKey);
        keyData.put("owner", owner);
        keyData.put("createTime", String.valueOf(System.currentTimeMillis()));

        redisTemplate.opsForHash().putAll(storeKey, keyData);
        redisTemplate.expire(storeKey, DEFAULT_TTL);
        log.info("Stored key: {}, owner: {}", keyId, owner);
    }

    @Override
    public String getStoredKey(String keyId) {
        String storeKey = KEYSTORE_PREFIX + keyId;
        return (String) redisTemplate.opsForHash().get(storeKey, "encryptedKey");
    }

    @Override
    public void deleteStoredKey(String keyId) {
        String storeKey = KEYSTORE_PREFIX + keyId;
        redisTemplate.delete(storeKey);
        log.info("Deleted stored key: {}", keyId);
    }

    /**
     * 使用公钥加密密钥（简化实现）
     */
    private String encryptKeyWithPublicKey(String key, String publicKey) {
        // 实际实现应使用SM2公钥加密
        // 这里返回简化值
        return key + "_encrypted_" + (publicKey != null ? publicKey.substring(0, Math.min(16, publicKey.length())) : "default");
    }
}