package com.tds.common.enums;

/**
 * 连接器版本状态枚举
 */
public enum ConnectorVersionStatus {
    INACTIVE(0, "未激活"),
    ACTIVATING(1, "激活中"),
    ACTIVATED(2, "已激活");

    private final int code;
    private final String description;

    ConnectorVersionStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static ConnectorVersionStatus fromCode(int code) {
        for (ConnectorVersionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}