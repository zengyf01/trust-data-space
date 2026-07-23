package com.tds.dos.common.enums;

/**
 * 工单类型枚举
 */
public enum WorkOrderType {
    DATA_SERVICE(1, "数据服务"),
    SANDBOX(2, "安全沙盒"),
    PRIVACY_COMPUTE(3, "隐私计算");

    private final int code;
    private final String description;

    WorkOrderType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static WorkOrderType fromCode(int code) {
        for (WorkOrderType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}