package com.tds.dos.msp.common.enums;

/**
 * Node status enumeration
 */
public enum NodeStatus {
    ONLINE(1, "在线"),
    OFFLINE(2, "离线"),
    BUSY(3, "忙碌"),
    MAINTAIN(4, "维护中");

    private final int code;
    private final String description;

    NodeStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NodeStatus fromCode(int code) {
        for (NodeStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}