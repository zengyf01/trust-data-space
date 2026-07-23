package com.tds.common.util;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
import java.util.Map;

/**
 * 国密SM2签名/验签/加解密工具类
 * 基于 BouncyCastle 的标准 SM2 实现（sm2p256v1 曲线）
 */
public class SM2Util {

    private static final String CURVE_NAME = "sm2p256v1";
    private static final String ALGORITHM = "EC"; // JDK 标准算法名，Provider 用 BC
    private static final String SIGN_ALGORITHM = "SM3withSM2";

    /** SM2 曲线域参数（全局复用） */
    private static final X9ECParameters X9_PARAMS = GMNamedCurves.getByName(CURVE_NAME);
    private static final ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(
            X9_PARAMS.getCurve(), X9_PARAMS.getG(), X9_PARAMS.getN(), X9_PARAMS.getH());
    /** BouncyCastle 风格的 ECParameterSpec（用于 JCE KeySpec） */
    private static final ECParameterSpec EC_SPEC = new ECParameterSpec(
            X9_PARAMS.getCurve(), X9_PARAMS.getG(), X9_PARAMS.getN(), X9_PARAMS.getH(),
            X9_PARAMS.getSeed());

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private SM2Util() {
    }

    // ============================================================
    // 密钥对生成
    // ============================================================

    /**
     * 生成 SM2 密钥对
     * @return Map: publicKey(压缩格式十六进制) / privateKey(十六进制)
     */
    public static Map<String, String> generateKeyPair() throws Exception {
        KeyPair keyPair = generateKeyPairInternal();
        // BouncyCastle 提供的 ECPublicKey 实现有 getQ() 方法
        org.bouncycastle.jce.interfaces.ECPublicKey bcPub =
                (org.bouncycastle.jce.interfaces.ECPublicKey) keyPair.getPublic();
        org.bouncycastle.jce.interfaces.ECPrivateKey bcPriv =
                (org.bouncycastle.jce.interfaces.ECPrivateKey) keyPair.getPrivate();

        // 压缩格式公钥（04 开头为非压缩，02/03 开头为压缩）
        String publicKey = Hex.toHexString(bcPub.getQ().getEncoded(true));
        String privateKey = Hex.toHexString(bcPriv.getD().toByteArray());

        Map<String, String> result = new HashMap<>();
        result.put("publicKey", publicKey);
        result.put("privateKey", privateKey);
        return result;
    }

    /**
     * 生成 SM2 密钥对（返回 KeyPair 对象）
     */
    public static KeyPair generateKeyPairObject() throws Exception {
        return generateKeyPairInternal();
    }

    private static KeyPair generateKeyPairInternal() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        keyPairGenerator.initialize(new ECGenParameterSpec(CURVE_NAME), new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    // ============================================================
    // 签名 / 验签
    // ============================================================

    /**
     * SM2 签名（使用私钥字节数组）
     * @param privateKeyHex 私钥十六进制
     * @param data          待签名数据
     */
    public static String sign(String privateKeyHex, byte[] data) throws Exception {
        BigInteger d = new BigInteger(privateKeyHex, 16);
        ECPrivateKeyParameters privParams = new ECPrivateKeyParameters(d, DOMAIN_PARAMS);
        SM2Signer signer = new SM2Signer();
        signer.init(true, new ParametersWithRandom(privParams, new SecureRandom()));
        signer.update(data, 0, data.length);
        return Hex.toHexString(signer.generateSignature());
    }

    /**
     * SM2 验签（使用公钥字节数组）
     * @param publicKeyHex  公钥十六进制
     * @param data          原始数据
     * @param signatureHex  签名十六进制
     */
    public static boolean verify(String publicKeyHex, byte[] data, String signatureHex) throws Exception {
        ECPoint point = X9_PARAMS.getCurve().decodePoint(Hex.decode(publicKeyHex));
        ECPublicKeyParameters pubParams = new ECPublicKeyParameters(point, DOMAIN_PARAMS);

        SM2Signer signer = new SM2Signer();
        signer.init(false, pubParams);
        signer.update(data, 0, data.length);
        return signer.verifySignature(Hex.decode(signatureHex));
    }

    /**
     * SM2 验签（String 数据版本，兼容旧 API）
     */
    public static boolean verify(String publicKeyHex, String data, String signatureHex) throws Exception {
        return verify(publicKeyHex, data.getBytes("UTF-8"), signatureHex);
    }

    // ============================================================
    // 加密 / 解密
    // ============================================================

    /**
     * SM2 加密（C1C3C2 模式，BouncyCastle 默认）
     * @param publicKeyHex 公钥十六进制
     * @param data         明文
     */
    public static String encrypt(String publicKeyHex, byte[] data) throws Exception {
        ECPoint point = X9_PARAMS.getCurve().decodePoint(Hex.decode(publicKeyHex));
        ECPublicKeyParameters pubParams = new ECPublicKeyParameters(point, DOMAIN_PARAMS);

        SM2Engine engine = new SM2Engine();
        engine.init(true, new ParametersWithRandom(pubParams, new SecureRandom()));
        return Hex.toHexString(engine.processBlock(data, 0, data.length));
    }

    /**
     * SM2 解密
     * @param privateKeyHex 私钥十六进制
     * @param encryptedHex  密文十六进制
     */
    public static byte[] decrypt(String privateKeyHex, String encryptedHex) throws Exception {
        BigInteger d = new BigInteger(privateKeyHex, 16);
        ECPrivateKeyParameters privParams = new ECPrivateKeyParameters(d, DOMAIN_PARAMS);

        SM2Engine engine = new SM2Engine();
        engine.init(false, privParams);
        byte[] cipherBytes = Hex.decode(encryptedHex);
        return engine.processBlock(cipherBytes, 0, cipherBytes.length);
    }

    // ============================================================
    // SM3 哈希
    // ============================================================

    /**
     * SM3 哈希（返回十六进制字符串）
     */
    public static String hash(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SM3", BouncyCastleProvider.PROVIDER_NAME);
        return Hex.toHexString(digest.digest(data.getBytes("UTF-8")));
    }

    /**
     * SM3 哈希（返回字节数组）
     */
    public static byte[] hash(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SM3", BouncyCastleProvider.PROVIDER_NAME);
        return digest.digest(data);
    }

    // ============================================================
    // 业务工具
    // ============================================================

    /**
     * 构建签名原文：appId + timestamp + requestBody
     */
    public static String buildSignPlainText(String appId, long timestamp, String requestBody) {
        return appId + timestamp + (requestBody != null ? requestBody : "");
    }

    /** 获取 ECParameterSpec（供外部使用，构造 KeySpec） */
    public static ECParameterSpec getEcParameterSpec() {
        return EC_SPEC;
    }
}
