package com.tds.dos.service.auth;

import com.tds.dos.common.core.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * TDS用户中心客户端 - DOS通过HTTP调用TDS统一用户中心API
 */
@Component
public class TdsUserCenterClient {

    private static final Logger logger = LoggerFactory.getLogger(TdsUserCenterClient.class);

    @Value("${tds.api.url:http://tds-api/api/tds}")
    private String tdsApiUrl;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 调用TDS用户中心登录
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> login(String username, String password, String appId) {
        logger.info("调用TDS用户中心登录: username={}, appId={}", username, appId);

        try {
            String url = tdsApiUrl + "/organization/uc/login?username=" + username
                + "&password=" + password + "&appId=" + appId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "200".equals(String.valueOf(body.get("code")))) {
                logger.info("TDS用户中心登录成功: username={}", username);
                return (Map<String, Object>) body.get("data");
            } else {
                String msg = body != null ? (String) body.get("msg") : "登录失败";
                logger.warn("TDS用户中心登录失败: username={}, msg={}", username, msg);
                throw new RuntimeException(msg);
            }
        } catch (Exception e) {
            logger.error("调用TDS用户中心登录异常: username={}", username, e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("调用用户中心失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户分页列表
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserPage(int currentPage, int pageSize, String keyword) {
        try {
            String url = tdsApiUrl + "/organization/user/page?currentPage=" + currentPage
                + "&pageSize=" + pageSize;
            if (keyword != null && !keyword.isEmpty()) {
                url += "&keyword=" + keyword;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "200".equals(String.valueOf(body.get("code")))) {
                return (Map<String, Object>) body.get("data");
            }
            return null;
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            return null;
        }
    }

    /**
     * 获取单个用户信息
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserById(String userId) {
        try {
            String url = tdsApiUrl + "/organization/user/" + userId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "200".equals(String.valueOf(body.get("code")))) {
                return (Map<String, Object>) body.get("data");
            }
            return null;
        } catch (Exception e) {
            logger.error("获取用户信息失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 验证Token有效性
     */
    public boolean verifyToken(String token) {
        try {
            String url = tdsApiUrl + "/organization/token/verify?token=" + token;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            return body != null && "200".equals(String.valueOf(body.get("code")));
        } catch (Exception e) {
            logger.error("验证Token异常: token={}", token, e);
            return false;
        }
    }

    /**
     * 创建用户
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> createUser(Map<String, Object> userData) {
        try {
            String url = tdsApiUrl + "/organization/user";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userData, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "200".equals(String.valueOf(body.get("code")))) {
                return (Map<String, Object>) body.get("data");
            }
            String msg = body != null ? (String) body.get("msg") : "创建用户失败";
            throw new RuntimeException(msg);
        } catch (Exception e) {
            logger.error("创建用户失败", e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> updateUser(String userId, Map<String, Object> userData) {
        try {
            String url = tdsApiUrl + "/organization/user/" + userId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userData, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && "200".equals(String.valueOf(body.get("code")))) {
                return (Map<String, Object>) body.get("data");
            }
            String msg = body != null ? (String) body.get("msg") : "更新用户失败";
            throw new RuntimeException(msg);
        } catch (Exception e) {
            logger.error("更新用户失败: userId={}", userId, e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("更新用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(String userId) {
        try {
            String url = tdsApiUrl + "/organization/user/" + userId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Map.class);
            Map<String, Object> body = response.getBody();

            return body != null && "200".equals(String.valueOf(body.get("code")));
        } catch (Exception e) {
            logger.error("删除用户失败: userId={}", userId, e);
            return false;
        }
    }
}
