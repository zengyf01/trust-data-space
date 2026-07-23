package com.tds.common.enums;

/**
 * 隧道认证类型枚举
 */
public enum TunnelAuthType {
    SM2_SIGNATURE(1, "SM2签名认证"),
    SM2_CERTIFICATE(2, "SM2证书认证");

    private final int code;
    private final String description;

    TunnelAuthType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
