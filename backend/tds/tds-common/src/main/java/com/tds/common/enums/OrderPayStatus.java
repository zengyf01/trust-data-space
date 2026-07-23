package com.tds.common.enums;

/**
 * 订单支付状态枚举
 */
public enum OrderPayStatus {
    UNPAID(1, "未支付"),
    PAID(2, "已支付"),
    REFUNDED(3, "已退款");

    private final int code;
    private final String description;

    OrderPayStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static OrderPayStatus fromCode(int code) {
        for (OrderPayStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}