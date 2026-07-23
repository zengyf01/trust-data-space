package com.tds.common.enums;

/**
 * 脱敏规则枚举
 */
public enum DesensitizeRule {
    NONE("NONE", "不脱敏"),
    MASK("MASK", "掩码脱敏"),
    PHONE("PHONE", "手机号脱敏"),
    IDCARD("IDCARD", "身份证脱敏"),
    BANK("BANK", "银行卡脱敏"),
    EMAIL("EMAIL", "邮箱脱敏");

    private final String code;
    private final String description;

    DesensitizeRule(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }

    public static DesensitizeRule fromCode(String code) {
        for (DesensitizeRule rule : values()) {
            if (rule.code.equals(code)) {
                return rule;
            }
        }
        return null;
    }
}