package com.tds.service.open;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tds.common.exception.BusinessException;
import com.tds.common.util.SM2Util;
import com.tds.dal.entity.TbMemberCredential;
import com.tds.dal.entity.TbOrganization;
import com.tds.dal.mapper.TbMemberCredentialMapper;
import com.tds.dal.mapper.TbOrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 机构凭证服务实现
 */
@Service
public class CredentialServiceImpl implements ICredentialService {

    @Autowired
    private TbMemberCredentialMapper credentialMapper;

    @Autowired
    private TbOrganizationMapper organizationMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CACHE_KEY_PREFIX = "tds:credential:";
    private static final Duration DEFAULT_EXPIRE = Duration.ofDays(365);

    @Override
    public Map<String, String> issueCredential(String memberId, String connectorNumber,
                                                String credentialType, int expireDays) {
        // 校验机构是否存在
        TbOrganization org = organizationMapper.selectById(memberId);
        if (org == null) {
            throw new BusinessException("机构不存在");
        }

        // 生成AppId
        String appId = generateAppId(memberId);

        // 生成AppKey或SM2密钥对
        String appKey = null;
        String publicKey = null;
        String privateKeyEncrypted = null;

        if ("SM2_CERT".equals(credentialType)) {
            // 生成SM2密钥对
            try {
                Map<String, String> keyPair = SM2Util.generateKeyPair();
                publicKey = keyPair.get("publicKey");
                privateKeyEncrypted = keyPair.get("privateKey"); // 实际应加密存储
            } catch (Exception e) {
                throw new BusinessException("生成SM2密钥对失败: " + e.getMessage());
            }
        } else {
            // 生成AppKey
            appKey = generateAppKey();
        }

        // 创建凭证记录
        TbMemberCredential credential = new TbMemberCredential();
        credential.setfId(UUID.randomUUID().toString().replace("-", ""));
        credential.setfMemberId(memberId);
        credential.setfConnectorNumber(connectorNumber);
        credential.setfCredentialType(credentialType);
        credential.setfAppId(appId);
        credential.setfAppKey(appKey);
        credential.setfPublicKey(publicKey);
        credential.setfPrivateKeyEncrypted(privateKeyEncrypted);
        credential.setfStatus(1);
        credential.setfIssueTime(LocalDateTime.now());
        credential.setfExpireTime(LocalDateTime.now().plusDays(expireDays));
        credential.setfCreateTime(LocalDateTime.now());

        credentialMapper.insert(credential);

        // 缓存凭证到Redis
        String cacheKey = CACHE_KEY_PREFIX + appId;
        if ("SM2_CERT".equals(credentialType)) {
            redisTemplate.opsForHash().put(cacheKey, "publicKey", publicKey);
            redisTemplate.opsForHash().put(cacheKey, "privateKey", privateKeyEncrypted);
        } else {
            redisTemplate.opsForValue().set(cacheKey, appKey);
        }
        redisTemplate.expire(cacheKey, Duration.ofDays(expireDays));

        // 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("appId", appId);
        if ("SM2_CERT".equals(credentialType)) {
            result.put("publicKey", publicKey);
            result.put("privateKey", privateKeyEncrypted); // 实际场景可能不返回私钥
            result.put("credentialType", "SM2_CERT");
        } else {
            result.put("appKey", appKey);
            result.put("credentialType", "API_KEY");
        }
        result.put("expireTime", credential.getfExpireTime().toString());

        return result;
    }

    @Override
    public boolean verifyApiKey(String appId, String appKey) {
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(appKey)) {
            return false;
        }

        // 先查Redis缓存
        String cacheKey = CACHE_KEY_PREFIX + appId;
        String cachedKey = redisTemplate.opsForValue().get(cacheKey);
        if (cachedKey != null) {
            return cachedKey.equals(appKey);
        }

        // 缓存未命中，查数据库
        TbMemberCredential credential = getByAppId(appId);
        if (credential == null) {
            return false;
        }

        // 检查状态和有效期
        if (credential.getfStatus() != 1 ||
            credential.getfExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 更新Redis缓存
        redisTemplate.opsForValue().set(cacheKey, credential.getfAppKey());
        redisTemplate.expire(cacheKey, Duration.between(LocalDateTime.now(), credential.getfExpireTime()));

        return credential.getfAppKey().equals(appKey);
    }

    @Override
    public boolean verifySm2Signature(String appId, String timestamp, String signData, String signature) {
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(timestamp) ||
            !StringUtils.hasText(signData) || !StringUtils.hasText(signature)) {
            return false;
        }

        // 检查时间戳有效期（5分钟）
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (Math.abs(now - ts) > 5 * 60 * 1000) {
            return false; // 时间戳超过5分钟
        }

        // 获取公钥
        String publicKey = null;
        String cacheKey = CACHE_KEY_PREFIX + appId;
        Object cachedPublicKey = redisTemplate.opsForHash().get(cacheKey, "publicKey");
        if (cachedPublicKey != null) {
            publicKey = cachedPublicKey.toString();
        } else {
            TbMemberCredential credential = getByAppId(appId);
            if (credential == null || credential.getfStatus() != 1) {
                return false;
            }
            if (credential.getfExpireTime().isBefore(LocalDateTime.now())) {
                return false;
            }
            publicKey = credential.getfPublicKey();
            // 更新缓存
            redisTemplate.opsForHash().put(cacheKey, "publicKey", publicKey);
            redisTemplate.expire(cacheKey, Duration.between(LocalDateTime.now(), credential.getfExpireTime()));
        }

        // 签名原文 = appId + timestamp + signData
        String plainText = appId + timestamp + signData;

        // SM2验签
        try {
            return SM2Util.verify(publicKey, plainText, signature);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public TbMemberCredential getByAppId(String appId) {
        LambdaQueryWrapper<TbMemberCredential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMemberCredential::getfAppId, appId)
               .eq(TbMemberCredential::getfStatus, 1);
        return credentialMapper.selectOne(wrapper);
    }

    @Override
    public void revokeCredential(String appId) {
        LambdaQueryWrapper<TbMemberCredential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMemberCredential::getfAppId, appId);
        TbMemberCredential credential = credentialMapper.selectOne(wrapper);

        if (credential != null) {
            credential.setfStatus(0);
            credential.setfUpdateTime(LocalDateTime.now());
            credentialMapper.updateById(credential);

            // 删除Redis缓存
            redisTemplate.delete(CACHE_KEY_PREFIX + appId);
        }
    }

    @Override
    public Map<String, String> refreshCredential(String appId, int expireDays) {
        TbMemberCredential credential = getByAppId(appId);
        if (credential == null) {
            throw new BusinessException("凭证不存在");
        }

        // 作废旧凭证
        credential.setfStatus(0);
        credential.setfUpdateTime(LocalDateTime.now());
        credentialMapper.updateById(credential);

        // 删除旧缓存
        redisTemplate.delete(CACHE_KEY_PREFIX + appId);

        // 颁发新凭证
        return issueCredential(credential.getfMemberId(), credential.getfConnectorNumber(),
                               credential.getfCredentialType(), expireDays);
    }

    @Override
    public List<TbMemberCredential> getValidCredentials(String memberId) {
        LambdaQueryWrapper<TbMemberCredential> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbMemberCredential::getfMemberId, memberId)
               .eq(TbMemberCredential::getfStatus, 1)
               .gt(TbMemberCredential::getfExpireTime, LocalDateTime.now())
               .orderByDesc(TbMemberCredential::getfCreateTime);
        return credentialMapper.selectList(wrapper);
    }

    /**
     * 生成AppId
     */
    private String generateAppId(String memberId) {
        String prefix = "APP";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return prefix + timestamp + random;
    }

    /**
     * 生成AppKey
     */
    private String generateAppKey() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}