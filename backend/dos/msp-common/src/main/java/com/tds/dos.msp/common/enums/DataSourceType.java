package com.tds.dos.msp.common.enums;

/**
 * Data source type enumeration
 */
public enum DataSourceType {
    MYSQL(1, "MySQL"),
    POSTGRESQL(2, "PostgreSQL"),
    API(3, "API"),
    FILE(4, "文件");

    private final int code;
    private final String description;

    DataSourceType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DataSourceType fromCode(int code) {
        for (DataSourceType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}