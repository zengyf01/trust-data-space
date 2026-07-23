package com.tds.dos.msp.common.enums;

/**
 * Task status enumeration
 */
public enum TaskStatus {
    CREATED(1, "已创建"),
    PENDING(2, "待执行"),
    RUNNING(3, "执行中"),
    COMPLETED(4, "已完成"),
    FAILED(5, "失败"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String description;

    TaskStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TaskStatus fromCode(int code) {
        for (TaskStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}