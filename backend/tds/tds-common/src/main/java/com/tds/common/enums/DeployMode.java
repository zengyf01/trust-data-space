package com.tds.common.enums;

/**
 * 部署模式枚举
 */
public enum DeployMode {

    CENTRALIZED("CENTRALIZED", "集中式部署"),
    DISTRIBUTED("DISTRIBUTED", "分布式部署"),
    HYBRID("HYBRID", "混合模式");

    private final String code;
    private final String desc;

    DeployMode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}