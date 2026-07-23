package com.tds.common.enums;

/**
 * 通知类型枚举
 */
public enum NotificationType {

    EMAIL("EMAIL", "邮件通知"),
    SMS("SMS", "短信通知"),
    WECHAT("WECHAT", "微信通知"),
    WEBHOOK("WEBHOOK", "Webhook通知");

    private final String code;
    private final String desc;

    NotificationType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}