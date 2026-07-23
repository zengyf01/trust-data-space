package com.tds.common.enums;

/**
 * 隧道状态枚举
 */
public enum TunnelStatus {
    PENDING_AUTH(0, "待认证"),
    ACTIVE(1, "活跃"),
    INACTIVE(2, "非活跃"),
    CLOSED(3, "已关闭");

    private final int code;
    private final String description;

    TunnelStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TunnelStatus fromCode(int code) {
        for (TunnelStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
