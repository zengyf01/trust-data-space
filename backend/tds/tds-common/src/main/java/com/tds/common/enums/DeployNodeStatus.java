package com.tds.common.enums;

/**
 * 部署节点状态枚举
 */
public enum DeployNodeStatus {

    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    FAULT(2, "故障"),
    MAINTENANCE(3, "维护中");

    private final Integer code;
    private final String desc;

    DeployNodeStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}