package com.tds.common.enums;

/**
 * 数据产品状态枚举
 */
public enum ProductStatus {
    DRAFT(1, "草稿"),
    PENDING_AUDIT(2, "待审核"),
    APPROVED(3, "审核通过"),
    REJECTED(4, "审核拒绝"),
    PUBLISHED(5, "已发布"),
    OFFLINE(6, "已下架");

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