package com.tds.dos.service.auth;

import com.tds.dos.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 认证服务实现 - 对接TDS统一用户中心
 */
@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TdsUserCenterClient tdsUserCenterClient;

    private static final String TOKEN_PREFIX = "dos:session:";
    private static final long TOKEN_EXPIRE_SECONDS = 2 * 60 * 60; // 2小时

    @Override
    public AuthDTO login(String username, String password) {
        return login(username, password, "DOS");
    }

    /**
     * 登录 - 调用TDS统一用户中心
     */
    public AuthDTO login(String username, String password, String appId) {
        if (username == null || password == null || username.isEmpty()) {
            throw new BusinessException("用户名或密码不能为空");
        }

        try {
            // 调用TDS用户中心验证
            Map<String, Object> result = tdsUserCenterClient.login(username, password, appId);

            String userId = (String) result.get("userId");
            String token = (String) result.get("token");
            String refreshToken = (String) result.get("refreshToken");
            String tenantId = (String) result.get("tenantId");

            // 保存到Redis (DOS本地会话)
            String sessionKey = TOKEN_PREFIX + token;
            redisTemplate.opsForHash().put(sessionKey, "userId", userId);
            redisTemplate.opsForHash().put(sessionKey, "username", username);
            redisTemplate.opsForHash().put(sessionKey, "tenantId", tenantId);
            redisTemplate.opsForHash().put(sessionKey, "tdsToken", token);
            redisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS));

            // 保存RefreshToken
            String refreshKey = "dos:refresh:" + refreshToken;
            redisTemplate.opsForValue().set(refreshKey, token);
            redisTemplate.expire(refreshKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS * 2L));

            // 构建返回结果
            AuthDTO auth = new AuthDTO();
            auth.setUserId(userId);
            auth.setUserName(username);
            auth.setTenantId(tenantId);
            auth.setToken(token);
            auth.setSessionId(token);
            auth.setRefreshToken(refreshToken);
            auth.setExpireTime(LocalDateTime.now().plusHours(2));

            logger.info("DOS登录成功: username={}, userId={}", username, userId);
            return auth;

        } catch (Exception e) {
            logger.error("DOS登录失败: username={}", username, e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public AuthDTO ssoCallback(String code, String state) {
        // TODO: 实现SSO回调 - 需要TDS用户中心支持SSO
        throw new BusinessException("SSO登录暂未实现");
    }

    @Override
    public AuthDTO refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException("RefreshToken不能为空");
        }

        String refreshKey = "dos:refresh:" + refreshToken;
        String oldToken = redisTemplate.opsForValue().get(refreshKey);

        if (oldToken == null) {
            throw new BusinessException("RefreshToken已过期");
        }

        // 删除旧Token
        String oldSessionKey = TOKEN_PREFIX + oldToken;
        redisTemplate.delete(oldSessionKey);
        redisTemplate.delete(refreshKey);

        // 生成新Token
        String newToken = generateToken();
        String newRefreshToken = UUID.randomUUID().toString().replace("-", "");

        // 获取用户信息
        String userId = (String) redisTemplate.opsForHash().get(oldSessionKey, "userId");
        String username = (String) redisTemplate.opsForHash().get(oldSessionKey, "username");
        String tenantId = (String) redisTemplate.opsForHash().get(oldSessionKey, "tenantId");

        // 保存新Token
        String sessionKey = TOKEN_PREFIX + newToken;
        redisTemplate.opsForHash().put(sessionKey, "userId", userId != null ? userId : "");
        redisTemplate.opsForHash().put(sessionKey, "username", username != null ? username : "");
        redisTemplate.opsForHash().put(sessionKey, "tenantId", tenantId != null ? tenantId : "");
        redisTemplate.opsForHash().put(sessionKey, "tdsToken", oldToken);
        redisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS));

        // 保存新RefreshToken
        String newRefreshKey = "dos:refresh:" + newRefreshToken;
        redisTemplate.opsForValue().set(newRefreshKey, newToken);
        redisTemplate.expire(newRefreshKey, java.time.Duration.ofSeconds(TOKEN_EXPIRE_SECONDS * 2L));

        // 构建返回结果
        AuthDTO auth = new AuthDTO();
        auth.setUserId(userId);
        auth.setUserName(username);
        auth.setTenantId(tenantId);
        auth.setToken(newToken);
        auth.setSessionId(newToken);
        auth.setRefreshToken(newRefreshToken);
        auth.setExpireTime(LocalDateTime.now().plusHours(2));

        return auth;
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        String sessionKey = TOKEN_PREFIX + token;
        String tdsToken = (String) redisTemplate.opsForHash().get(sessionKey, "tdsToken");

        // 删除DOS本地会话
        redisTemplate.delete(sessionKey);

        // TODO: 调用TDS用户中心登出

        logger.info("DOS登出成功: token={}", token);
    }

    @Override
    public boolean verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String sessionKey = TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
    }

    private String generateToken() {
        return "TOKEN_" + System.currentTimeMillis() + "_" +
               UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
