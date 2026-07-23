package com.tds.common.enums;

/**
 * 成员角色枚举
 */
public enum MemberRole {

    OWNER(1, "所有者"),
    ADMIN(2, "管理员"),
    MEMBER(3, "成员"),
    GUEST(4, "访客");

    private final Integer code;
    private final String desc;

    MemberRole(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}