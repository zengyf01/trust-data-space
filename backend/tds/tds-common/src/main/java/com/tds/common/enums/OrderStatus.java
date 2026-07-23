package com.tds.common.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    PENDING(1, "待审批"),
    APPROVED(2, "已审批"),
    SIGNING(3, "签署中"),
    EXECUTING(4, "执行中"),
    COMPLETED(5, "已完成"),
    REJECTED(6, "已驳回"),
    CANCELLED(7, "已取消");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}