package com.tds.datar.common.enums;

/**
 * 订单状态
 */
public enum OrderStatus {

    PENDING(1, "待审核"),
    APPROVED(2, "已通过"),
    SIGNING(3, "签署中"),
    EXECUTING(4, "执行中"),
    COMPLETED(5, "已完成"),
    REJECTED(6, "已拒绝"),
    CANCELLED(7, "已取消");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}