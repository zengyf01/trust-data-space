package com.tds.service.open;

import com.tds.service.open.OpenApiDTO;

/**
 * 开放接口服务
 */
public interface IOpenApiService {

    /**
     * 创建机构
     */
    OpenApiDTO createOrganization(OpenApiDTO dto);

    /**
     * 创建账号
     */
    OpenApiDTO createAccount(OpenApiDTO dto);

    /**
     * 下发凭证（AppId/AppKey）
     */
    OpenApiDTO issueCredential(OpenApiDTO dto);

    /**
     * 验证凭证
     */
    boolean verifyCredential(String appId, String appKey);

    /**
     * 转发API请求
     */
    String forwardApi(String address, String apiPath, String method, String requestBody);
}