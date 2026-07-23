package com.tds.datar.common.enums;

/**
 * 交付任务状态枚举
 */
public enum DeliveryTaskStatus {

    PENDING(0, "待执行"),
    RUNNING(1, "执行中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败");

    private final Integer code;
    private final String desc;

    DeliveryTaskStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}