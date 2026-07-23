package com.tds.common.enums;

/**
 * 数据源状态枚举
 */
public enum DataSourceStatus {
    ENABLED(1, "启用"),
    DISABLED(2, "禁用");

    private final int code;
    private final String description;

    DataSourceStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static DataSourceStatus fromCode(int code) {
        for (DataSourceStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}