package com.tds.common.enums;

/**
 * 账单状态枚举
 */
public enum BillStatus {

    PENDING(0, "待结算"),
    CONFIRMED(1, "已确认"),
    PAID(2, "已支付"),
    OVERDUE(3, "已逾期");

    private final Integer code;
    private final String desc;

    BillStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}