package com.tds.dos.api.proxy;

import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.service.proxy.ApiProxyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * API代理控制器
 * 用于转发外部数据服务API请求，实现数据服务API代理功能
 */
@RestController
@RequestMapping("/api/dos/proxy")
public class ApiProxyController {

    @Autowired
    private ApiProxyServiceImpl apiProxyService;

    /**
     * 转发GET请求
     */
    @GetMapping("/forward")
    public ApiResponse<String> forwardGet(
            @RequestParam String url,
            @RequestHeader Map<String, String> headers) {
        try {
            // 过滤掉不必要的headers
            Map<String, String> filteredHeaders = filterHeaders(headers);
            String response = apiProxyService.forwardGet(url, filteredHeaders);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("API转发失败: " + e.getMessage());
        }
    }

    /**
     * 转发POST请求
     */
    @PostMapping("/forward")
    public ApiResponse<String> forwardPost(
            @RequestParam String url,
            @RequestBody(required = false) String body,
            @RequestHeader Map<String, String> headers,
            @RequestParam(required = false, defaultValue = "application/json") String contentType) {
        try {
            Map<String, String> filteredHeaders = filterHeaders(headers);
            String response = apiProxyService.forwardPost(url, body, contentType, filteredHeaders);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("API转发失败: " + e.getMessage());
        }
    }

    /**
     * 转发PUT请求
     */
    @PutMapping("/forward")
    public ApiResponse<String> forwardPut(
            @RequestParam String url,
            @RequestBody(required = false) String body,
            @RequestHeader Map<String, String> headers,
            @RequestParam(required = false, defaultValue = "application/json") String contentType) {
        try {
            Map<String, String> filteredHeaders = filterHeaders(headers);
            String response = apiProxyService.forwardPut(url, body, contentType, filteredHeaders);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("API转发失败: " + e.getMessage());
        }
    }

    /**
     * 转发DELETE请求
     */
    @DeleteMapping("/forward")
    public ApiResponse<String> forwardDelete(
            @RequestParam String url,
            @RequestHeader Map<String, String> headers) {
        try {
            Map<String, String> filteredHeaders = filterHeaders(headers);
            String response = apiProxyService.forwardDelete(url, filteredHeaders);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("API转发失败: " + e.getMessage());
        }
    }

    /**
     * 测试API连通性
     */
    @GetMapping("/test")
    public ApiResponse<Map<String, Object>> testConnection(@RequestParam String url) {
        try {
            boolean reachable = apiProxyService.testConnection(url);
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("reachable", reachable);
            return ApiResponse.success(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("reachable", false);
            result.put("error", e.getMessage());
            return ApiResponse.success(result);
        }
    }

    /**
     * 过滤请求头，移除Spring MVC自动添加的headers
     */
    private Map<String, String> filterHeaders(Map<String, String> headers) {
        Map<String, String> filtered = new HashMap<>();
        headers.forEach((key, value) -> {
            // 过滤掉Spring MVC和连接相关的headers
            if (!key.equalsIgnoreCase("host")
                && !key.equalsIgnoreCase("content-length")
                && !key.equalsIgnoreCase("connection")
                && !key.startsWith("sec-")
                && !key.startsWith("purpose")) {
                filtered.put(key, value);
            }
        });
        return filtered;
    }
}
