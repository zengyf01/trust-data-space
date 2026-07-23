package com.tds.dos.api.auth;

import com.tds.dos.common.core.ApiResponse;
import com.tds.dos.service.auth.AuthDTO;
import com.tds.dos.service.auth.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    /**
     * MaxKey登录
     */
    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        return ApiResponse.success(authService.login(username, password));
    }

    /**
     * SSO认证回调
     */
    @GetMapping("/sso/callback")
    public ApiResponse<?> ssoCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        return ApiResponse.success(authService.ssoCallback(code, state));
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ApiResponse<?> refreshToken(@RequestParam String refreshToken) {
        return ApiResponse.success(authService.refreshToken(refreshToken));
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestHeader(value = "X-Token", required = false) String token) {
        authService.logout(token);
        return ApiResponse.success(null);
    }

    /**
     * 验证Token
     */
    @GetMapping("/verify")
    public ApiResponse<?> verifyToken(@RequestHeader(value = "X-Token", required = false) String token) {
        boolean valid = authService.verifyToken(token);
        return ApiResponse.success(Map.of("valid", valid));
    }
}