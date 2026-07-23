package com.tds.service.open;

import java.time.LocalDateTime;

/**
 * 开放接口凭证DTO
 */
public class OpenApiDTO {

    private String appId;
    private String appKey;
    private String organizationName;
    private String organizationCode;
    private String accountName;
    private String accountPhone;
    private String accountEmail;
    private String tenantId;
    private String address;
    private String apiPath;
    private String method;

    // Getters and Setters
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getOrganizationCode() { return organizationCode; }
    public void setOrganizationCode(String organizationCode) { this.organizationCode = organizationCode; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getAccountPhone() { return accountPhone; }
    public void setAccountPhone(String accountPhone) { this.accountPhone = accountPhone; }
    public String getAccountEmail() { return accountEmail; }
    public void setAccountEmail(String accountEmail) { this.accountEmail = accountEmail; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getApiPath() { return apiPath; }
    public void setApiPath(String apiPath) { this.apiPath = apiPath; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}