package com.tds.common.enums;

/**
 * 计费模型类型枚举
 */
public enum BillingModelType {

    FIXED("FIXED", "固定计费"),
    API_CALL("API_CALL", "按调用次数计费"),
    VOLUME("VOLUME", "按数据量计费"),
    SUBSCRIPTION("SUBSCRIPTION", "订阅计费"),
    CUSTOM("CUSTOM", "自定义计费");

    private final String code;
    private final String desc;

    BillingModelType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}