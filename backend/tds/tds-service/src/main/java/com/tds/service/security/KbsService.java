package com.tds.service.security;

import com.tds.common.util.SM4Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KBS (Key Broker Service) 密钥管理服务
 * 提供密钥生成、分发、存储、轮换等功能
 */
@Slf4j
@Service
public class KbsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "tds:kbs:key:";
    private static final String VERSION_PREFIX = "tds:kbs:version:";
    private static final Duration DEFAULT_TTL = Duration.ofDays(365);

    // 密钥版本缓存
    private final ConcurrentHashMap<String, Integer> keyVersions = new ConcurrentHashMap<>();

    // ==================== 密钥生成 ====================

    /**
     * 生成数据密钥
     * @param keyId 密钥ID
     * @param algorithm 算法（SM4/AES）
     * @return 密钥信息
     */
    public Map<String, String> generateDataKey(String keyId, String algorithm) {
        String key = SM4Util.generateKey();
        String iv = SM4Util.generateIV();
        String version = "1";

        // 存储密钥信息
        String cacheKey = KEY_PREFIX + keyId;
        Map<String, String> keyInfo = new HashMap<>();
        keyInfo.put("key", key);
        keyInfo.put("iv", iv);
        keyInfo.put("algorithm", algorithm != null ? algorithm : "SM4");
        keyInfo.put("version", version);
        keyInfo.put("createTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        keyInfo.put("status", "ACTIVE");

        redisTemplate.opsForHash().putAll(cacheKey, keyInfo);
        redisTemplate.expire(cacheKey, DEFAULT_TTL);

        // 记录版本
        keyVersions.put(keyId, 1);

        log.info("Generated data key: {}, algorithm: {}, version: {}", keyId, algorithm, version);

        Map<String, String> result = new HashMap<>();
        result.put("keyId", keyId);
        result.put("algorithm", keyInfo.get("algorithm"));
        result.put("key", key);
        result.put("iv", iv);
        result.put("version", version);

        return result;
    }

    /**
     * 获取密钥信息
     * @param keyId 密钥ID
     * @return 密钥信息（不包含明文密钥）
     */
    public Map<String, String> getKeyInfo(String keyId) {
        String cacheKey = KEY_PREFIX + keyId;
        Map<String, String> keyInfo = (Map<String, String>) (Map<?, ?>) redisTemplate.opsForHash().entries(cacheKey);

        if (keyInfo.isEmpty()) {
            return null;
        }

        // 不返回明文密钥
        Map<String, String> result = new HashMap<>(keyInfo);
        result.remove("key");

        return result;
    }

    // ==================== 密钥加密 ====================

    /**
     * 获取加密后的数据密钥
     * @param keyId 密钥ID
     * @param publicKey 公钥（用于加密）
     * @return 加密后的密钥
     */
    public Map<String, String> getEncryptedDataKey(String keyId, String publicKey) {
        String cacheKey = KEY_PREFIX + keyId;
        String key = (String) redisTemplate.opsForHash().get(cacheKey, "key");
        String iv = (String) redisTemplate.opsForHash().get(cacheKey, "iv");

        if (key == null) {
            throw new RuntimeException("密钥不存在: " + keyId);
        }

        // 使用公钥加密密钥（简化实现，实际应使用SM2公钥加密）
        String encryptedKey = encryptWithPublicKey(key, publicKey);

        Map<String, String> result = new HashMap<>();
        result.put("keyId", keyId);
        result.put("encryptedKey", encryptedKey);
        result.put("iv", iv);
        result.put("version", (String) redisTemplate.opsForHash().get(cacheKey, "version"));

        return result;
    }

    /**
     * 使用公钥加密密钥
     * @param key 密钥
     * @param publicKey 公钥
     * @return 加密后的密钥
     */
    private String encryptWithPublicKey(String key, String publicKey) {
        // 简化实现：实际应使用SM2公钥加密
        // 这里使用XOR模拟加密
        StringBuilder encrypted = new StringBuilder();
        byte[] keyBytes = key.getBytes();
        byte[] pubKeyBytes = publicKey != null ? publicKey.getBytes() : "default".getBytes();

        for (int i = 0; i < keyBytes.length; i++) {
            encrypted.append(String.format("%02x", keyBytes[i] ^ pubKeyBytes[i % pubKeyBytes.length]));
        }

        return encrypted.toString();
    }

    // ==================== 密钥轮换 ====================

    /**
     * 密钥轮换
     * @param keyId 密钥ID
     * @param rotateIv 是否轮换IV
     * @return 新密钥信息
     */
    public Map<String, String> rotateKey(String keyId, boolean rotateIv) {
        // 获取旧版本
        Integer currentVersion = keyVersions.getOrDefault(keyId, 1);
        int newVersion = currentVersion + 1;

        // 标记旧版本为已废弃
        String oldCacheKey = KEY_PREFIX + keyId;
        redisTemplate.opsForHash().put(oldCacheKey, "status", "ROTATED");
        redisTemplate.opsForHash().put(oldCacheKey, "rotatedTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        redisTemplate.opsForHash().put(oldCacheKey, "rotatedToVersion", String.valueOf(newVersion));

        // 记录版本
        keyVersions.put(keyId, newVersion);

        // 生成新密钥
        String algorithm = (String) redisTemplate.opsForHash().get(oldCacheKey, "algorithm");
        String newKey = SM4Util.generateKey();
        String newIv = rotateIv ? SM4Util.generateIV() : (String) redisTemplate.opsForHash().get(oldCacheKey, "iv");

        // 存储新密钥
        Map<String, String> newKeyInfo = new HashMap<>();
        newKeyInfo.put("key", newKey);
        newKeyInfo.put("iv", newIv);
        newKeyInfo.put("algorithm", algorithm != null ? algorithm : "SM4");
        newKeyInfo.put("version", String.valueOf(newVersion));
        newKeyInfo.put("createTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        newKeyInfo.put("status", "ACTIVE");

        redisTemplate.opsForHash().putAll(oldCacheKey, newKeyInfo);

        log.info("Rotated key: {}, old version: {}, new version: {}", keyId, currentVersion, newVersion);

        Map<String, String> result = new HashMap<>();
        result.put("keyId", keyId);
        result.put("algorithm", algorithm);
        result.put("key", newKey);
        result.put("iv", newIv);
        result.put("version", String.valueOf(newVersion));
        result.put("previousVersion", String.valueOf(currentVersion));

        return result;
    }

    // ==================== 密钥删除 ====================

    /**
     * 删除密钥
     * @param keyId 密钥ID
     */
    public void deleteKey(String keyId) {
        String cacheKey = KEY_PREFIX + keyId;
        redisTemplate.delete(cacheKey);
        keyVersions.remove(keyId);
        log.info("Deleted key: {}", keyId);
    }

    /**
     * 检查密钥是否存在
     * @param keyId 密钥ID
     * @return 是否存在
     */
    public boolean keyExists(String keyId) {
        String cacheKey = KEY_PREFIX + keyId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }

    /**
     * 获取密钥状态
     * @param keyId 密钥ID
     * @return 状态
     */
    public String getKeyStatus(String keyId) {
        String cacheKey = KEY_PREFIX + keyId;
        String status = (String) redisTemplate.opsForHash().get(cacheKey, "status");
        return status != null ? status : "NOT_FOUND";
    }
}