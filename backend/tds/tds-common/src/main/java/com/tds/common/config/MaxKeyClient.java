package com.tds.common.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * MaxKey OAuth2 客户端
 *
 * MaxKey 是开源的身份认证中间件，支持 OAuth2.0/OIDC 协议
 * 文档: https://maxkey.top/zh/guide/index.html
 */
@Slf4j
@Component
public class MaxKeyClient {

    @Autowired
    private MaxKeyProperties properties;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * OAuth2 授权码模式 - 获取授权URL
     */
    public String getAuthorizationUrl(String state) {
        String url = properties.getServerUrl() + "/oauth2/authorize?" +
                "response_type=code" +
                "&client_id=" + properties.getClientId() +
                "&redirect_uri=" + properties.getRedirectUri() +
                "&state=" + (state != null ? state : "");
        return url;
    }

    /**
     * OAuth2 授权码模式 - 用授权码换取访问令牌
     */
    public Map<String, Object> exchangeCodeForToken(String code) {
        String url = properties.getServerUrl() + "/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(properties.getClientId(), properties.getClientSecret());

        String body = "grant_type=" + properties.getGrantType() +
                "&code=" + code +
                "&redirect_uri=" + properties.getRedirectUri();

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class);
            return parseTokenResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("MaxKey token exchange failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException("MaxKey认证失败: " + e.getMessage());
        }
    }

    /**
     * 用刷新令牌获取新的访问令牌
     */
    public Map<String, Object> refreshAccessToken(String refreshToken) {
        String url = properties.getServerUrl() + "/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(properties.getClientId(), properties.getClientSecret());

        String body = "grant_type=refresh_token" +
                "&refresh_token=" + refreshToken;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class);
            return parseTokenResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("MaxKey refresh token failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException("刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 验证访问令牌
     */
    public Map<String, Object> introspectToken(String accessToken) {
        String url = properties.getServerUrl() + "/oauth2/introspect";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(properties.getClientId(), properties.getClientSecret());

        String body = "token=" + accessToken;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class);
            return objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            log.error("MaxKey token introspection failed", e);
            throw new RuntimeException("令牌验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        String url = properties.getServerUrl() + "/oauth2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, String.class);
            return objectMapper.readValue(response.getBody(), Map.class);
        } catch (HttpClientErrorException e) {
            log.error("MaxKey get userinfo failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("MaxKey get userinfo response parse failed: {}", e.getMessage());
            throw new RuntimeException("解析用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 登出 (使令牌失效)
     */
    public void revokeToken(String accessToken) {
        String url = properties.getServerUrl() + "/oauth2/logout";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            log.warn("MaxKey revoke token failed (non-critical)", e);
        }
    }

    /**
     * 解析令牌响应
     */
    private Map<String, Object> parseTokenResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", root.path("access_token").asText());
            result.put("tokenType", root.path("token_type").asText());
            result.put("refreshToken", root.path("refresh_token").asText());
            result.put("expiresIn", root.path("expires_in").asInt());
            result.put("scope", root.path("scope").asText());
            result.put("openid", root.path("openid").asText());
            return result;
        } catch (Exception e) {
            log.error("Failed to parse token response: {}", responseBody);
            throw new RuntimeException("解析令牌响应失败");
        }
    }
}