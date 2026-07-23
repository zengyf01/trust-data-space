package com.tds.common.enums;

/**
 * 用户类型枚举
 */
public enum UserType {

    SUPER_ADMIN("SUPER_ADMIN", "超级管理员"),
    ORG_ADMIN("ORG_ADMIN", "机构管理员"),
    DEPT_ADMIN("DEPT_ADMIN", "部门管理员"),
    NORMAL_USER("NORMAL_USER", "普通用户"),
    GUEST("GUEST", "访客");

    private final String code;
    private final String desc;

    UserType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}