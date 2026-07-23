package com.tds.common.enums;

/**
 * 配置值类型枚举
 */
public enum ConfigValueType {

    STRING("STRING", "字符串"),
    NUMBER("NUMBER", "数字"),
    BOOLEAN("BOOLEAN", "布尔值"),
    JSON("JSON", "JSON对象"),
    TEXT("TEXT", "文本");

    private final String code;
    private final String desc;

    ConfigValueType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}