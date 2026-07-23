package com.tds.dos.service.proxy;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * API代理服务实现
 * 使用HTTP客户端转发外部API请求
 */
@Slf4j
@Service
public class ApiProxyServiceImpl implements IApiProxyService {

    @Value("${api.proxy.timeout:30000}")
    private int proxyTimeout;

    @Value("${api.proxy.connect.timeout:10000}")
    private int connectTimeout;

    @Override
    public String forwardGet(String targetUrl, Map<String, String> headers) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(targetUrl);

            // 设置请求头
            if (headers != null) {
                headers.forEach(request::setHeader);
            }
            // 默认Accept头
            if (!headers.containsKey("Accept")) {
                request.setHeader("Accept", "application/json");
            }

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = response.getEntity() != null ?
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : "";

                log.info("API代理GET请求: {} -> {}", targetUrl, statusCode);

                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new RuntimeException("API返回错误状态码: " + statusCode + ", 响应: " + responseBody);
                }
            }
        }
    }

    @Override
    public String forwardPost(String targetUrl, String body, String contentType, Map<String, String> headers) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(targetUrl);

            // 设置请求头
            if (headers != null) {
                headers.forEach(request::setHeader);
            }
            // 默认Content-Type
            String actualContentType = contentType != null ? contentType : "application/json";
            request.setHeader("Content-Type", actualContentType);
            if (!headers.containsKey("Accept")) {
                request.setHeader("Accept", "application/json");
            }

            // 设置请求体
            if (body != null && !body.isEmpty()) {
                request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            }

            long startTime = System.currentTimeMillis();
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                long endTime = System.currentTimeMillis();

                int statusCode = response.getCode();
                String responseBody = response.getEntity() != null ?
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : "";

                log.info("API代理POST请求: {} -> {} ({}ms)", targetUrl, statusCode, endTime - startTime);

                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new RuntimeException("API返回错误状态码: " + statusCode + ", 响应: " + responseBody);
                }
            }
        }
    }

    @Override
    public String forwardPut(String targetUrl, String body, String contentType, Map<String, String> headers) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(targetUrl);

            // 设置请求头
            if (headers != null) {
                headers.forEach(request::setHeader);
            }
            // 默认Content-Type
            String actualContentType = contentType != null ? contentType : "application/json";
            request.setHeader("Content-Type", actualContentType);
            if (!headers.containsKey("Accept")) {
                request.setHeader("Accept", "application/json");
            }

            // 设置请求体
            if (body != null && !body.isEmpty()) {
                request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            }

            long startTime = System.currentTimeMillis();
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                long endTime = System.currentTimeMillis();

                int statusCode = response.getCode();
                String responseBody = response.getEntity() != null ?
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : "";

                log.info("API代理PUT请求: {} -> {} ({}ms)", targetUrl, statusCode, endTime - startTime);

                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new RuntimeException("API返回错误状态码: " + statusCode + ", 响应: " + responseBody);
                }
            }
        }
    }

    @Override
    public String forwardDelete(String targetUrl, Map<String, String> headers) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpDelete request = new HttpDelete(targetUrl);

            // 设置请求头
            if (headers != null) {
                headers.forEach(request::setHeader);
            }
            // 默认Accept头
            if (!headers.containsKey("Accept")) {
                request.setHeader("Accept", "application/json");
            }

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = response.getEntity() != null ?
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : "";

                log.info("API代理DELETE请求: {} -> {}", targetUrl, statusCode);

                if (statusCode >= 200 && statusCode < 300) {
                    return responseBody;
                } else {
                    throw new RuntimeException("API返回错误状态码: " + statusCode + ", 响应: " + responseBody);
                }
            }
        }
    }

    @Override
    public boolean testConnection(String targetUrl) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHead request = new HttpHead(targetUrl);
            request.setHeader("Accept", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                log.info("API连通性测试: {} -> {}", targetUrl, statusCode);
                return statusCode >= 200 && statusCode < 400;
            }
        } catch (Exception e) {
            log.warn("API连通性测试失败: {} -> {}", targetUrl, e.getMessage());
            return false;
        }
    }
}
