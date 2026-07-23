package com.tds.common.enums;

/**
 * 连接器类型枚举
 */
public enum ConnectorType {
    DATA(1, "数据连接器"),
    SANDBOX(2, "沙盒连接器"),
    PRIVACY_COMPUTE(3, "隐私计算连接器");

    private final int code;
    private final String description;

    ConnectorType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static ConnectorType fromCode(int code) {
        for (ConnectorType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}