package com.tds.api.security;

import com.tds.common.core.ApiResponse;
import com.tds.service.security.ISecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 安全服务REST API
 * 提供数据加密、远程认证、密钥管理等接口
 */
@RestController
@RequestMapping("/api/tds/security")
public class SecurityController {

    @Autowired
    private ISecurityService securityService;

    // ==================== 数据加密 ====================

    /**
     * SM4加密
     */
    @PostMapping("/encrypt")
    public ApiResponse<?> encrypt(@RequestBody Map<String, Object> params) {
        String plaintext = (String) params.get("plaintext");
        String key = (String) params.get("key");
        String mode = (String) params.getOrDefault("mode", "ECB");
        String iv = (String) params.get("iv");

        String ciphertext = securityService.encrypt(plaintext, key, mode, iv);
        return ApiResponse.success(Map.of("ciphertext", ciphertext));
    }

    /**
     * SM4解密
     */
    @PostMapping("/decrypt")
    public ApiResponse<?> decrypt(@RequestBody Map<String, Object> params) {
        String ciphertext = (String) params.get("ciphertext");
        String key = (String) params.get("key");
        String mode = (String) params.getOrDefault("mode", "ECB");
        String iv = (String) params.get("iv");

        String plaintext = securityService.decrypt(ciphertext, key, mode, iv);
        return ApiResponse.success(Map.of("plaintext", plaintext));
    }

    /**
     * 生成随机密钥
     */
    @PostMapping("/key/generate")
    public ApiResponse<?> generateKey() {
        String key = securityService.generateKey();
        return ApiResponse.success(Map.of("key", key));
    }

    /**
     * SM3哈希
     */
    @PostMapping("/hash")
    public ApiResponse<?> hash(@RequestBody Map<String, Object> params) {
        String data = (String) params.get("data");
        String hashValue = securityService.hash(data);
        return ApiResponse.success(Map.of("hash", hashValue));
    }

    // ==================== 远程认证 (TEE) ====================

    /**
     * 创建远程认证请求
     */
    @PostMapping("/attestation/request")
    public ApiResponse<?> createAttestationRequest(@RequestBody Map<String, Object> params) {
        String target = (String) params.get("target");
        @SuppressWarnings("unchecked")
        Map<String, Object> report = (Map<String, Object>) params.get("report");

        Map<String, Object> request = securityService.createAttestationRequest(target, report);
        return ApiResponse.success(request);
    }

    /**
     * 验证远程认证响应
     */
    @PostMapping("/attestation/verify")
    public ApiResponse<?> verifyAttestationResponse(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) params.get("response");

        Map<String, Object> result = securityService.verifyAttestationResponse(response);
        return ApiResponse.success(result);
    }

    /**
     * 获取TEE quote
     */
    @GetMapping("/attestation/quote/{targetId}")
    public ApiResponse<?> getQuote(@PathVariable String targetId) {
        Map<String, Object> quote = securityService.getQuote(targetId);
        return ApiResponse.success(quote);
    }

    // ==================== KBS密钥服务 ====================

    /**
     * 生成数据密钥
     */
    @PostMapping("/kbs/key/generate")
    public ApiResponse<?> generateDataKey(@RequestBody Map<String, Object> params) {
        String keyId = (String) params.get("keyId");
        String algorithm = (String) params.getOrDefault("algorithm", "SM4");

        Map<String, String> keyInfo = securityService.generateDataKey(keyId, algorithm);
        return ApiResponse.success(keyInfo);
    }

    /**
     * 获取加密后的数据密钥
     */
    @PostMapping("/kbs/key/encrypted")
    public ApiResponse<?> getEncryptedDataKey(@RequestBody Map<String, Object> params) {
        String keyId = (String) params.get("keyId");
        String publicKey = (String) params.get("publicKey");

        Map<String, String> encryptedKey = securityService.getEncryptedDataKey(keyId, publicKey);
        return ApiResponse.success(encryptedKey);
    }

    /**
     * 密钥轮换
     */
    @PostMapping("/kbs/key/rotate")
    public ApiResponse<?> rotateKey(@RequestBody Map<String, Object> params) {
        String keyId = (String) params.get("keyId");

        Map<String, String> newKeyInfo = securityService.rotateKey(keyId);
        return ApiResponse.success(newKeyInfo);
    }

    /**
     * 删除密钥
     */
    @DeleteMapping("/kbs/key/{keyId}")
    public ApiResponse<?> deleteKey(@PathVariable String keyId) {
        securityService.deleteKey(keyId);
        return ApiResponse.success(Map.of("keyId", keyId, "deleted", true));
    }

    // ==================== 密钥存储 ====================

    /**
     * 存储密钥
     */
    @PostMapping("/keystore/store")
    public ApiResponse<?> storeKey(@RequestBody Map<String, Object> params) {
        String keyId = (String) params.get("keyId");
        String encryptedKey = (String) params.get("encryptedKey");
        String owner = (String) params.get("owner");

        securityService.storeKey(keyId, encryptedKey, owner);
        return ApiResponse.success(Map.of("keyId", keyId, "stored", true));
    }

    /**
     * 获取存储的密钥
     */
    @GetMapping("/keystore/{keyId}")
    public ApiResponse<?> getStoredKey(@PathVariable String keyId) {
        String encryptedKey = securityService.getStoredKey(keyId);
        return ApiResponse.success(Map.of("keyId", keyId, "encryptedKey", encryptedKey));
    }

    /**
     * 删除存储的密钥
     */
    @DeleteMapping("/keystore/{keyId}")
    public ApiResponse<?> deleteStoredKey(@PathVariable String keyId) {
        securityService.deleteStoredKey(keyId);
        return ApiResponse.success(Map.of("keyId", keyId, "deleted", true));
    }
}