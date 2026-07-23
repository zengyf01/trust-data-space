package com.tds.service.security;

import java.util.Map;

/**
 * 安全服务接口
 * 提供数据加密、远程认证、密钥管理等安全功能
 */
public interface ISecurityService {

    // ==================== 数据加密 ====================

    /**
     * SM4加密
     * @param plaintext 明文
     * @param key 密钥
     * @param mode 模式 (ECB/CBC/GCM)
     * @param iv IV向量（ CBC/GCM模式需要）
     * @return 密文
     */
    String encrypt(String plaintext, String key, String mode, String iv);

    /**
     * SM4解密
     * @param ciphertext 密文
     * @param key 密钥
     * @param mode 模式 (ECB/CBC/GCM)
     * @param iv IV向量
     * @return 明文
     */
    String decrypt(String ciphertext, String key, String mode, String iv);

    /**
     * 生成随机密钥
     * @return 32位十六进制密钥
     */
    String generateKey();

    /**
     * SM3哈希
     * @param data 数据
     * @return 哈希值
     */
    String hash(String data);

    // ==================== 远程认证 (TEE) ====================

    /**
     * 生成远程认证请求
     * @param target 目标标识
     * @param report 本地报告
     * @return 认证请求
     */
    Map<String, Object> createAttestationRequest(String target, Map<String, Object> report);

    /**
     * 验证远程认证响应
     * @param attestationResponse 认证响应
     * @return 验证结果
     */
    Map<String, Object> verifyAttestationResponse(Map<String, Object> attestationResponse);

    /**
     * 获取TEE平台quote
     * @param targetId 目标ID
     * @return quote信息
     */
    Map<String, Object> getQuote(String targetId);

    // ==================== KBS密钥服务 ====================

    /**
     * 生成数据密钥
     * @param keyId 密钥ID
     * @param algorithm 算法
     * @return 密钥信息
     */
    Map<String, String> generateDataKey(String keyId, String algorithm);

    /**
     * 获取加密后的数据密钥
     * @param keyId 密钥ID
     * @param publicKey 公钥（用于加密）
     * @return 加密后的密钥
     */
    Map<String, String> getEncryptedDataKey(String keyId, String publicKey);

    /**
     * 密钥轮换
     * @param keyId 密钥ID
     * @return 新密钥信息
     */
    Map<String, String> rotateKey(String keyId);

    /**
     * 删除密钥
     * @param keyId 密钥ID
     */
    void deleteKey(String keyId);

    // ==================== 密钥存储 ====================

    /**
     * 存储加密密钥
     * @param keyId 密钥ID
     * @param encryptedKey 加密后的密钥
     * @param owner 所有者
     */
    void storeKey(String keyId, String encryptedKey, String owner);

    /**
     * 获取存储的密钥
     * @param keyId 密钥ID
     * @return 加密后的密钥
     */
    String getStoredKey(String keyId);

    /**
     * 删除存储的密钥
     * @param keyId 密钥ID
     */
    void deleteStoredKey(String keyId);
}