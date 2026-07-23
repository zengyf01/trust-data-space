package com.tds.common.enums;

/**
 * 连接器状态枚举
 */
public enum ConnectorStatus {
    ONLINE(1, "在线"),
    OFFLINE(2, "离线"),
    PENDING_REGISTRATION(3, "离线待注册");

    private final int code;
    private final String description;

    ConnectorStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static ConnectorStatus fromCode(int code) {
        for (ConnectorStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}