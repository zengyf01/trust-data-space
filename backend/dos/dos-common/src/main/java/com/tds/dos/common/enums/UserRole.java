package com.tds.dos.common.enums;

/**
 * User role enumeration
 */
public enum UserRole {
    ADMIN(1, "管理员"),
    NODE_ADMIN(2, "节点管理员"),
    USER(3, "普通用户"),
    VIEWER(4, "访客");

    private final int code;
    private final String description;

    UserRole(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromCode(int code) {
        for (UserRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        return null;
    }
}
