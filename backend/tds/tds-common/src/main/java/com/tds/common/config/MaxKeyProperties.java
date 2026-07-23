package com.tds.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MaxKey SSO 配置
 */
@Component
@ConfigurationProperties(prefix = "maxkey")
public class MaxKeyProperties {

    private boolean enabled = true;
    private String serverUrl = "http://localhost:8087/maxkey";
    private String clientId = "maxkey_client";
    private String clientSecret = "maxkey_secret";
    private String redirectUri = "http://localhost:8081/organization/sso/callback";
    private String grantType = "authorization_code";
    private int tokenExpireSeconds = 7200;
    private boolean skipSslValidation = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    public String getGrantType() { return grantType; }
    public void setGrantType(String grantType) { this.grantType = grantType; }
    public int getTokenExpireSeconds() { return tokenExpireSeconds; }
    public void setTokenExpireSeconds(int tokenExpireSeconds) { this.tokenExpireSeconds = tokenExpireSeconds; }
    public boolean isSkipSslValidation() { return skipSslValidation; }
    public void setSkipSslValidation(boolean skipSslValidation) { this.skipSslValidation = skipSslValidation; }
}