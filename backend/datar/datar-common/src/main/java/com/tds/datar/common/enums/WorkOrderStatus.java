package com.tds.datar.common.enums;

/**
 * 工单状态
 */
public enum WorkOrderStatus {

    PENDING(1, "待处理"),
    PROCESSING(2, "处理中"),
    COMPLETED(3, "已完成"),
    FAILED(4, "失败"),
    CANCELLED(5, "已取消");

    private final Integer code;
    private final String desc;

    WorkOrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}