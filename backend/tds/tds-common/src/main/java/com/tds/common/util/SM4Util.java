package com.tds.common.util;

import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Arrays;

/**
 * 国密SM4对称加密工具类
 * 支持ECB和GCM模式
 */
public class SM4Util {

    private static final String ALGORITHM = "SM4";
    private static final String ECB_MODE = "SM4/ECB/PKCS5Padding";
    private static final String GCM_MODE = "SM4/GCM/NoPadding";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成随机SM4密钥
     * @return 32位十六进制字符串（128位密钥）
     */
    public static String generateKey() {
        byte[] key = new byte[16];
        new java.security.SecureRandom().nextBytes(key);
        return Hex.toHexString(key);
    }

    /**
     * 生成随机IV向量（GCM模式）
     * @return 12位十六进制字符串（96位IV）
     */
    public static String generateIV() {
        byte[] iv = new byte[12];
        new java.security.SecureRandom().nextBytes(iv);
        return Hex.toHexString(iv);
    }

    /**
     * SM4 ECB模式加密
     * @param key 密钥（16字节，32位十六进制或直接传入字节数组）
     * @param data 待加密数据
     * @return 加密后十六进制字符串
     */
    public static String encryptECB(String key, String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(hexToBytes(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ECB_MODE, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("SM4 ECB加密失败", e);
        }
    }

    /**
     * SM4 ECB模式解密
     * @param key 密钥
     * @param encryptedData 加密后十六进制字符串
     * @return 解密后字符串
     */
    public static String decryptECB(String key, String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(hexToBytes(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ECB_MODE, "BC");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Hex.decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4 ECB解密失败", e);
        }
    }

    /**
     * SM4 GCM模式加密（带认证）
     * @param key 密钥
     * @param iv IV向量
     * @param data 待加密数据
     * @return 加密后数据（IV || CipherText || AuthTag）
     */
    public static String encryptGCM(String key, String iv, String data) {
        try {
            byte[] keyBytes = hexToBytes(key);
            byte[] ivBytes = hexToBytes(iv);

            SM4Engine engine = new SM4Engine();
            GCMBlockCipher gcmCipher = new GCMBlockCipher(engine);

            ParametersWithIV params = new ParametersWithIV(new KeyParameter(keyBytes), ivBytes);
            gcmCipher.init(true, params);

            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            byte[] output = new byte[gcmCipher.getOutputSize(input.length)];
            int len = gcmCipher.processBytes(input, 0, input.length, output, 0);
            gcmCipher.doFinal(output, len);

            return Hex.toHexString(output);
        } catch (Exception e) {
            throw new RuntimeException("SM4 GCM加密失败", e);
        }
    }

    /**
     * SM4 GCM模式解密
     * @param key 密钥
     * @param iv IV向量
     * @param encryptedData 加密后数据
     * @return 解密后字符串
     */
    public static String decryptGCM(String key, String iv, String encryptedData) {
        try {
            byte[] keyBytes = hexToBytes(key);
            byte[] ivBytes = hexToBytes(iv);

            SM4Engine engine = new SM4Engine();
            GCMBlockCipher gcmCipher = new GCMBlockCipher(engine);

            ParametersWithIV params = new ParametersWithIV(new KeyParameter(keyBytes), ivBytes);
            gcmCipher.init(false, params);

            byte[] input = Hex.decode(encryptedData);
            byte[] output = new byte[gcmCipher.getOutputSize(input.length)];
            int len = gcmCipher.processBytes(input, 0, input.length, output, 0);
            gcmCipher.doFinal(output, len);

            // 去除PKCS5Padding
            int padLen = output[output.length - 1] & 0xff;
            if (padLen > 16) padLen = 16;
            return new String(Arrays.copyOf(output, output.length - padLen), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4 GCM解密失败", e);
        }
    }

    /**
     * SM4 CBC模式加密
     * @param key 密钥
     * @param iv IV向量（16字节）
     * @param data 待加密数据
     * @return 加密后十六进制字符串
     */
    public static String encryptCBC(String key, String iv, String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(hexToBytes(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new javax.crypto.spec.IvParameterSpec(hexToBytes(iv)));
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("SM4 CBC加密失败", e);
        }
    }

    /**
     * SM4 CBC模式解密
     * @param key 密钥
     * @param iv IV向量
     * @param encryptedData 加密后十六进制字符串
     * @return 解密后字符串
     */
    public static String decryptCBC(String key, String iv, String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(hexToBytes(key), ALGORITHM);
            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new javax.crypto.spec.IvParameterSpec(hexToBytes(iv)));
            byte[] decrypted = cipher.doFinal(Hex.decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4 CBC解密失败", e);
        }
    }

    /**
     * SM3哈希
     * @param data 待哈希数据
     * @return 哈希值十六进制字符串
     */
    public static String hash(String data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SM3", "BC");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException("SM3哈希失败", e);
        }
    }

    /**
     * 验证密钥格式
     * @param key 密钥
     * @return 是否有效
     */
    public static boolean isValidKey(String key) {
        if (key == null) return false;
        byte[] keyBytes = Hex.decode(key);
        return keyBytes != null && keyBytes.length == 16;
    }

    /**
     * 十六进制字符串转字节数组
     */
    private static byte[] hexToBytes(String hex) {
        if (hex == null) return null;
        // 如果是十六进制字符串
        if (hex.length() == 32) {
            return Hex.decode(hex);
        }
        // 如果是直接传入的密钥字符串
        return hex.getBytes(StandardCharsets.UTF_8);
    }
}