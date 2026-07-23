package com.tds.common.enums;

/**
 * 实例状态枚举
 */
public enum InstanceStatus {

    STOPPED(0, "已停止"),
    RUNNING(1, "运行中"),
    STARTING(2, "启动中"),
    STOPPING(3, "停止中"),
    FAULT(4, "故障");

    private final Integer code;
    private final String desc;

    InstanceStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}