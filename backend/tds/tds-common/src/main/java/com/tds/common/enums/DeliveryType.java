package com.tds.common.enums;

/**
 * 交付类型枚举
 */
public enum DeliveryType {
    DATA_SERVICE(1, "数据服务"),
    SANDBOX(2, "安全沙盒"),
    PRIVACY_COMPUTE(3, "隐私计算");

    private final int code;
    private final String description;

    DeliveryType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static DeliveryType fromCode(int code) {
        for (DeliveryType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}