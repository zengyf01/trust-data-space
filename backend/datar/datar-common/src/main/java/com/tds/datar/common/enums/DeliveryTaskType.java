package com.tds.datar.common.enums;

/**
 * 交付任务类型枚举
 */
public enum DeliveryTaskType {

    SANDBOX_INIT("SANDBOX_INIT", "沙盒初始化"),
    IMAGE_BUILD("IMAGE_BUILD", "镜像构建"),
    SOURCE_DOWNLOAD("SOURCE_DOWNLOAD", "源码下载");

    private final String code;
    private final String desc;

    DeliveryTaskType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}