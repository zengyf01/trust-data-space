package com.tds.api.open;

import com.tds.common.core.ApiResponse;
import com.tds.dal.entity.TbMemberCredential;
import com.tds.service.open.ICredentialService;
import com.tds.service.open.IOpenApiService;
import com.tds.service.open.OpenApiDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 开放接口
 */
@RestController
@RequestMapping("/open")
public class OpenApiController {

    @Autowired
    private IOpenApiService openApiService;

    @Autowired
    private ICredentialService credentialService;

    /**
     * 创建机构
     */
    @PostMapping("/organization")
    public ApiResponse<?> createOrganization(@RequestBody OpenApiDTO dto) {
        return ApiResponse.success(openApiService.createOrganization(dto));
    }

    /**
     * 创建账号
     */
    @PostMapping("/account")
    public ApiResponse<?> createAccount(@RequestBody OpenApiDTO dto) {
        return ApiResponse.success(openApiService.createAccount(dto));
    }

    // ==================== 凭证管理 ====================

    /**
     * 颁发凭证 (API_KEY 或 SM2_CERT)
     */
    @PostMapping("/credential/issue")
    public ApiResponse<?> issueCredential(@RequestBody Map<String, Object> params) {
        String memberId = (String) params.get("memberId");
        String connectorNumber = (String) params.get("connectorNumber");
        String credentialType = (String) params.getOrDefault("credentialType", "API_KEY");
        Integer expireDays = params.get("expireDays") != null ?
                Integer.parseInt(params.get("expireDays").toString()) : 365;

        Map<String, String> result = credentialService.issueCredential(
                memberId, connectorNumber, credentialType, expireDays);
        return ApiResponse.success(result);
    }

    /**
     * 验证API_KEY凭证
     */
    @PostMapping("/credential/verify/apiKey")
    public ApiResponse<?> verifyApiKey(@RequestBody Map<String, String> params) {
        String appId = params.get("appId");
        String appKey = params.get("appKey");
        boolean valid = credentialService.verifyApiKey(appId, appKey);
        return ApiResponse.success(Map.of("valid", valid));
    }

    /**
     * 验证SM2签名
     * 签名原文 = appId + timestamp + requestBody
     */
    @PostMapping("/credential/verify/sm2")
    public ApiResponse<?> verifySm2Signature(@RequestBody Map<String, String> params) {
        String appId = params.get("appId");
        String timestamp = params.get("timestamp");
        String signData = params.get("signData");
        String signature = params.get("signature");

        boolean valid = credentialService.verifySm2Signature(appId, timestamp, signData, signature);
        return ApiResponse.success(Map.of("valid", valid));
    }

    /**
     * 获取机构的有效凭证列表
     */
    @GetMapping("/credential/list/{memberId}")
    public ApiResponse<?> getCredentials(@PathVariable String memberId) {
        List<TbMemberCredential> credentials = credentialService.getValidCredentials(memberId);
        return ApiResponse.success(credentials);
    }

    /**
     * 作废凭证
     */
    @PostMapping("/credential/revoke")
    public ApiResponse<?> revokeCredential(@RequestParam String appId) {
        credentialService.revokeCredential(appId);
        return ApiResponse.success(null);
    }

    /**
     * 刷新凭证
     */
    @PostMapping("/credential/refresh")
    public ApiResponse<?> refreshCredential(@RequestParam String appId,
                                            @RequestParam(defaultValue = "365") int expireDays) {
        Map<String, String> result = credentialService.refreshCredential(appId, expireDays);
        return ApiResponse.success(result);
    }

    /**
     * 转发API请求
     */
    @RequestMapping("/forward/**")
    public ApiResponse<?> forwardApi(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String method,
            @RequestBody(required = false) String requestBody) {
        String path = "/open/forward";
        return ApiResponse.success(Map.of("forwarded", true, "path", path));
    }
}