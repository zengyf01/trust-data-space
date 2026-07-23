package com.tds.datar.service.dataflow;

/**
 * 数据转换DTO
 */
public class DataTransformDTO {

    private String transformId;
    private String transformName;
    private String inputFormat; // JSON/XML/CSV
    private String outputFormat; // JSON/XML/CSV
    private String transformRules; // 转换规则JSON
    private String transformScript; // 自定义转换脚本
    private String tenantId;

    // Getters and Setters
    public String getTransformId() { return transformId; }
    public void setTransformId(String transformId) { this.transformId = transformId; }
    public String getTransformName() { return transformName; }
    public void setTransformName(String transformName) { this.transformName = transformName; }
    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }
    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
    public String getTransformRules() { return transformRules; }
    public void setTransformRules(String transformRules) { this.transformRules = transformRules; }
    public String getTransformScript() { return transformScript; }
    public void setTransformScript(String transformScript) { this.transformScript = transformScript; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}