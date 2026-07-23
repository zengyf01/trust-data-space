package com.tds.dos.common.enums;

/**
 * 工单状态枚举
 */
public enum WorkOrderStatus {
    PENDING(1, "待处理"),
    PROCESSING(2, "处理中"),
    COMPLETED(3, "已完成"),
    FAILED(4, "失败"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String description;

    WorkOrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static WorkOrderStatus fromCode(int code) {
        for (WorkOrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}