package com.tds.common.enums;

/**
 * 用量类型枚举
 */
public enum UsageType {

    API_CALL("API_CALL", "API调用"),
    DATA_VOLUME("DATA_VOLUME", "数据量"),
    STORAGE("STORAGE", "存储空间"),
    COMPUTE("COMPUTE", "计算资源");

    private final String code;
    private final String desc;

    UsageType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}