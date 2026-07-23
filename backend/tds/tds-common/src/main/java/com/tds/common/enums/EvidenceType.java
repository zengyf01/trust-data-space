package com.tds.common.enums;

/**
 * 存证类型
 */
public enum EvidenceType {

    CONTRACT_SIGN(1, "合约签署"),
    CONTRACT_EXECUTE(2, "合约执行"),
    DATA_DELIVERY(3, "数据交付"),
    DATA_CONSUME(4, "数据消费"),
    WORK_ORDER_COMPLETE(5, "工单完成"),
    POLICY_UPDATE(6, "策略更新");

    private final Integer code;
    private final String desc;

    EvidenceType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}