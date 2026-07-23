package com.tds.common.enums;

/**
 * 数据空间状态枚举
 */
public enum DataSpaceStatus {

    PENDING(0, "待审核"),
    ACTIVE(1, "正常"),
    FROZEN(2, "冻结"),
    CANCELLED(3, "已注销");

    private final Integer code;
    private final String desc;

    DataSpaceStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}