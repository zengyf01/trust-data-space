package com.tds.common.enums;

/**
 * 通知状态枚举
 */
public enum NotificationStatus {

    PENDING(0, "待发送"),
    SENDING(1, "发送中"),
    SUCCESS(2, "发送成功"),
    FAILED(3, "发送失败");

    private final Integer code;
    private final String desc;

    NotificationStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}