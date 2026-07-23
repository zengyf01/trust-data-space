package com.tds.dos.service.auth;

/**
 * 认证服务接口
 */
public interface IAuthService {

    /**
     * MaxKey登录
     */
    AuthDTO login(String username, String password);

    /**
     * SSO认证回调
     */
    AuthDTO ssoCallback(String code, String state);

    /**
     * 刷新Token
     */
    AuthDTO refreshToken(String refreshToken);

    /**
     * 登出
     */
    void logout(String token);

    /**
     * 验证Token
     */
    boolean verifyToken(String token);
}