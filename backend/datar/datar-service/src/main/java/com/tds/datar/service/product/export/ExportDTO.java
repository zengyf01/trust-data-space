package com.tds.datar.service.product.export;

/**
 * 数据导出DTO
 */
public class ExportDTO {

    private String productId;
    private String catalogId;
    private String exportFormat; // EXCEL/JSON/CSV
    private Integer sampleCount;
    private String[] fields;
    private String condition;
    private String fileName;

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getCatalogId() { return catalogId; }
    public void setCatalogId(String catalogId) { this.catalogId = catalogId; }
    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }
    public Integer getSampleCount() { return sampleCount; }
    public void setSampleCount(Integer sampleCount) { this.sampleCount = sampleCount; }
    public String[] getFields() { return fields; }
    public void setFields(String[] fields) { this.fields = fields; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}