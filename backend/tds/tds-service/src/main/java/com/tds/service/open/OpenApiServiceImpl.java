package com.tds.service.open;

import com.tds.common.exception.BusinessException;
import com.tds.service.open.OpenApiDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 开放接口服务实现
 */
@Service
public class OpenApiServiceImpl implements IOpenApiService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CREDENTIAL_KEY_PREFIX = "tds:credential:";

    @Override
    public OpenApiDTO createOrganization(OpenApiDTO dto) {
        // 生成机构编码
        String orgCode = "ORG" + System.currentTimeMillis();
        dto.setOrganizationCode(orgCode);

        // 生成租户ID
        String tenantId = UUID.randomUUID().toString().replace("-", "");
        dto.setTenantId(tenantId);

        // 保存机构信息到Redis（简化版，实际应存数据库）
        String orgKey = "tds:organization:" + orgCode;
        redisTemplate.opsForValue().set(orgKey, dto.getOrganizationName());

        return dto;
    }

    @Override
    public OpenApiDTO createAccount(OpenApiDTO dto) {
        if (dto.getTenantId() == null || dto.getTenantId().isEmpty()) {
            throw new BusinessException("租户ID不能为空");
        }

        // 生成账号ID
        String accountId = UUID.randomUUID().toString().replace("-", "");
        String accountKey = "tds:account:" + dto.getTenantId() + ":" + accountId;

        // 保存账号信息
        redisTemplate.opsForValue().set(accountKey + ":name", dto.getAccountName());
        if (dto.getAccountPhone() != null) {
            redisTemplate.opsForValue().set(accountKey + ":phone", dto.getAccountPhone());
        }
        if (dto.getAccountEmail() != null) {
            redisTemplate.opsForValue().set(accountKey + ":email", dto.getAccountEmail());
        }

        return dto;
    }

    @Override
    public OpenApiDTO issueCredential(OpenApiDTO dto) {
        if (dto.getTenantId() == null || dto.getTenantId().isEmpty()) {
            throw new BusinessException("租户ID不能为空");
        }

        // 生成AppId和AppKey
        String appId = "APP" + System.currentTimeMillis();
        String appKey = generateAppKey();

        dto.setAppId(appId);
        dto.setAppKey(appKey);

        // 保存凭证到Redis
        String credentialKey = CREDENTIAL_KEY_PREFIX + appId;
        redisTemplate.opsForValue().set(credentialKey, appKey + ":" + dto.getTenantId());

        // 设置凭证有效期（5分钟）
        redisTemplate.expire(credentialKey, java.time.Duration.ofMinutes(5));

        return dto;
    }

    @Override
    public boolean verifyCredential(String appId, String appKey) {
        if (appId == null || appKey == null) {
            return false;
        }

        String credentialKey = CREDENTIAL_KEY_PREFIX + appId;
        String storedKey = redisTemplate.opsForValue().get(credentialKey);

        return appKey.equals(storedKey);
    }

    @Override
    public String forwardApi(String address, String apiPath, String method, String requestBody) {
        // 简化实现：实际应使用HTTP客户端转发请求
        // 这里返回模拟响应
        return "{\"code\": 200, \"msg\": \"success\", \"data\": {\"forwarded\": true, \"address\": \"" + address + "\"}}";
    }

    private String generateAppKey() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
}