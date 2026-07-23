package com.tds.dos.common.enums;

/**
 * User status enumeration
 */
public enum UserStatus {
    ACTIVE("ACTIVE", "正常"),
    NORMAL("NORMAL", "正常"),
    LOCKED("LOCKED", "锁定"),
    DISABLED("DISABLED", "禁用");

    private final String code;
    private final String description;

    UserStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserStatus fromCode(String code) {
        if (code == null) return null;
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}