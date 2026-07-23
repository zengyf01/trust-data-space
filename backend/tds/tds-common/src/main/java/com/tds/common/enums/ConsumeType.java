package com.tds.common.enums;

/**
 * 数据消费类型
 */
public enum ConsumeType {

    API_CALL("API_CALL", "API调用"),
    SFTP_DOWNLOAD("SFTP_DOWNLOAD", "SFTP下载"),
    DATA_SERVICE("DATA_SERVICE", "数据服务"),
    SANDBOX_ACCESS("SANDBOX_ACCESS", "沙盒访问"),
    PRIVACY_COMPUTE("PRIVACY_COMPUTE", "隐私计算");

    private final String code;
    private final String desc;

    ConsumeType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }
}