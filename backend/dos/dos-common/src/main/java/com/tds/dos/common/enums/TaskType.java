package com.tds.dos.common.enums;

/**
 * Task type enumeration
 */
public enum TaskType {
    PSI(1, "PSI求交"),
    MPC(2, "MPC安全计算"),
    HORIZONTAL_FL(3, "横向联邦"),
    CUSTOM_CODE(4, "自定义代码"),
    VERTICAL_FL(5, "纵向联邦"),
    COMPOUND_TASK(6, "复合任务"),
    COMPONENT_DAG(7, "组件DAG"),
    PIR(8, "PIR隐匿查询");

    private final int code;
    private final String description;

    TaskType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TaskType fromCode(int code) {
        for (TaskType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
