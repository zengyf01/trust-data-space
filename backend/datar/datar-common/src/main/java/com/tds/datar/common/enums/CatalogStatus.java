package com.tds.datar.common.enums;

/**
 * 目录状态枚举
 */
public enum CatalogStatus {
    DRAFT(1, "草稿"),
    PUBLISHED(2, "已发布"),
    OFFLINE(3, "已下线");

    private final int code;
    private final String description;

    CatalogStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static CatalogStatus fromCode(int code) {
        for (CatalogStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}