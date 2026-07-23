package com.tds.common.enums;

/**
 * 连接器操作类型枚举
 */
public enum ConnectorOperateType {
    DEPLOY("DEPLOY", "部署"),
    UPGRADE("UPGRADE", "升级"),
    UNINSTALL("UNINSTALL", "卸载"),
    RESTART("RESTART", "重启"),
    STOP("STOP", "停止");

    private final String code;
    private final String description;

    ConnectorOperateType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }

    public static ConnectorOperateType fromCode(String code) {
        for (ConnectorOperateType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}