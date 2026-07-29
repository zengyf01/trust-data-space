package com.tds.dos.common.enums;

/**
 * Node mode enumeration - execution mode for tasks
 */
public enum NodeMode {
    RAY("RAY", "Ray模式"),
    KUSCIA("KUSCIA", "Kuscia/SecretFlow模式");

    private final String code;
    private final String description;

    NodeMode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NodeMode fromCode(String code) {
        for (NodeMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return null;
    }
}
