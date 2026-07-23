package com.tds.datar.common.enums;

/**
 * 产品状态枚举
 */
public enum ProductStatus {
    DRAFT(1, "草稿"),
    PUBLISHED(2, "已发布"),
    OFFLINE(3, "已下架");

    private final int code;
    private final String description;

    ProductStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static ProductStatus fromCode(int code) {
        for (ProductStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}