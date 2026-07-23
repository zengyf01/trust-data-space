package com.tds.common.enums;

/**
 * 数据源类型枚举
 */
public enum DataSourceType {
    MYSQL(1, "MySQL"),
    POSTGRESQL(2, "PostgreSQL"),
    SFTP(3, "SFTP"),
    HTTP(4, "HTTP"),
    OSS(5, "对象存储OSS");

    private final int code;
    private final String description;

    DataSourceType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static DataSourceType fromCode(int code) {
        for (DataSourceType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}