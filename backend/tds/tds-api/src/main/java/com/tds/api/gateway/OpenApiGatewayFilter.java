package com.tds.api.gateway;

import com.tds.service.open.ICredentialService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 开放接口网关过滤器 - 验证凭证
 * 支持两种认证方式：
 * 1. API_KEY认证：X-App-Id + X-App-Key
 * 2. SM2签名认证：X-App-Id + X-Timestamp + Authorization(appId:timestamp:signature)
 */
@Component
@Order(1)
public class OpenApiGatewayFilter implements Filter {

    @Autowired
    private ICredentialService credentialService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestUri = httpRequest.getRequestURI();

        // 只过滤/open路径的请求
        if (requestUri.startsWith("/open/")) {
            String appId = httpRequest.getHeader("X-App-Id");
            String timestamp = httpRequest.getHeader("X-Timestamp");
            String authorization = httpRequest.getHeader("Authorization");

            // SM2签名认证方式：Authorization header 格式为 appId:timestamp:signature
            if (authorization != null && authorization.contains(":")) {
                String[] parts = authorization.split(":");
                if (parts.length >= 3) {
                    String sigAppId = parts[0];
                    String sigTimestamp = parts[1];
                    String signature = authorization.substring(sigAppId.length() + sigTimestamp.length() + 2);

                    // 验证时间戳（5分钟有效期）
                    if (!isTimestampValid(sigTimestamp)) {
                        sendError(httpResponse, 401, "请求已过期");
                        return;
                    }

                    // 获取请求体作为签名原文
                    // 注意：对于GET请求，signData为空
                    String signData = "";
                    if (httpRequest.getContentLength() > 0) {
                        // 对于已读取过body的请求，这里简化处理
                        // 实际应使用CachedBodyHttpServletRequest
                        signData = "body_should_be_read";
                    }

                    // SM2签名验证
                    if (!credentialService.verifySm2Signature(sigAppId, sigTimestamp, signData, signature)) {
                        sendError(httpResponse, 401, "签名验证失败");
                        return;
                    }

                    chain.doFilter(request, response);
                    return;
                }
            }

            // API_KEY认证方式：X-App-Id + X-App-Key
            String appKey = httpRequest.getHeader("X-App-Key");

            // 验证时间戳（5分钟有效期）
            if (timestamp != null) {
                if (!isTimestampValid(timestamp)) {
                    sendError(httpResponse, 401, "请求已过期");
                    return;
                }
            }

            // 验证凭证
            if (appId == null || appKey == null) {
                sendError(httpResponse, 401, "缺少凭证，请使用 X-App-Id 和 X-App-Key");
                return;
            }

            if (!credentialService.verifyApiKey(appId, appKey)) {
                sendError(httpResponse, 401, "凭证无效");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 验证时间戳是否在有效期内（5分钟）
     */
    private boolean isTimestampValid(String timestamp) {
        try {
            long ts = Long.parseLong(timestamp);
            long now = System.currentTimeMillis();
            return Math.abs(now - ts) <= 5 * 60 * 1000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().write("{\"code\": " + code + ", \"msg\": \"" + message + "\"}");
    }
}