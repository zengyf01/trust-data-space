package com.tds.dos.service.proxy;

import java.util.Map;

/**
 * API代理服务接口
 * 用于转发外部数据服务API请求
 */
public interface IApiProxyService {

    /**
     * 转发GET请求
     * @param targetUrl 目标URL
     * @param headers 请求头
     * @return 响应内容
     */
    String forwardGet(String targetUrl, Map<String, String> headers) throws Exception;

    /**
     * 转发POST请求
     * @param targetUrl 目标URL
     * @param body 请求体
     * @param contentType 内容类型
     * @param headers 请求头
     * @return 响应内容
     */
    String forwardPost(String targetUrl, String body, String contentType, Map<String, String> headers) throws Exception;

    /**
     * 转发PUT请求
     * @param targetUrl 目标URL
     * @param body 请求体
     * @param contentType 内容类型
     * @param headers 请求头
     * @return 响应内容
     */
    String forwardPut(String targetUrl, String body, String contentType, Map<String, String> headers) throws Exception;

    /**
     * 转发DELETE请求
     * @param targetUrl 目标URL
     * @param headers 请求头
     * @return 响应内容
     */
    String forwardDelete(String targetUrl, Map<String, String> headers) throws Exception;

    /**
     * 测试API连通性
     * @param targetUrl 目标URL
     * @return 是否可达
     */
    boolean testConnection(String targetUrl) throws Exception;
}
