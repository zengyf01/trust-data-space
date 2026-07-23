package com.tds.datar.common.enums;

/**
 * 工单类型
 */
public enum WorkOrderType {

    DATA_SERVICE("DATA_SERVICE", "数据服务"),
    SANDBOX("SANDBOX", "安全沙盒"),
    PRIVACY_COMPUTE("PRIVACY_COMPUTE", "隐私计算");

    private final String code;
    private final String desc;

    WorkOrderType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}