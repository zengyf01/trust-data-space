package com.tds.common.enums;

/**
 * 数字合约状态
 */
public enum ContractStatus {
    PENDING(1, "待签"),
    SIGNING(2, "签署中"),
    EXECUTING(3, "执行中"),
    REJECTED(4, "已拒绝"),
    TERMINATED(5, "已终止"),
    COMPLETED(6, "已完成");

    private final int code;
    private final String description;

    ContractStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }

    public static ContractStatus fromCode(int code) {
        for (ContractStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}