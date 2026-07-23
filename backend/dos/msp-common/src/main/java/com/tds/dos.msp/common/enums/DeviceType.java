package com.tds.dos.msp.common.enums;

/**
 * Device type enumeration
 */
public enum DeviceType {
    CPU(1, "CPU"),
    GPU(2, "GPU"),
    TEE(3, "TEE安全单元"),
    PYU(4, "保护计算单元"),
    SPU(5, "安全处理单元"),
    HEU(6, "同态加密单元");

    private final int code;
    private final String description;

    DeviceType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DeviceType fromCode(int code) {
        for (DeviceType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}