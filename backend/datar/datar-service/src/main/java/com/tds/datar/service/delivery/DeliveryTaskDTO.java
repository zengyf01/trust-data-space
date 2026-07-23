package com.tds.datar.service.delivery;

/**
 * 交付任务DTO
 */
public class DeliveryTaskDTO {

    private String id;
    private String workOrderId;           // 工单ID
    private String taskType;              // 任务类型：SANDBOX_INIT, IMAGE_BUILD, SOURCE_DOWNLOAD
    private Integer status;               // 状态
    private String sandboxId;            // 沙盒ID
    private String workDirectory;         // 工作目录
    private String imageName;            // 镜像名称
    private String imageTag;             // 镜像标签
    private String sourceUrl;            // 源码URL
    private String sourcePath;           // 源码本地路径
    private String buildLog;             // 构建日志
    private String errorMessage;         // 错误信息
    private Long duration;               // 执行时长
    private String tenantId;             // 租户ID

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(String workOrderId) { this.workOrderId = workOrderId; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getSandboxId() { return sandboxId; }
    public void setSandboxId(String sandboxId) { this.sandboxId = sandboxId; }
    public String getWorkDirectory() { return workDirectory; }
    public void setWorkDirectory(String workDirectory) { this.workDirectory = workDirectory; }
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public String getImageTag() { return imageTag; }
    public void setImageTag(String imageTag) { this.imageTag = imageTag; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    public String getBuildLog() { return buildLog; }
    public void setBuildLog(String buildLog) { this.buildLog = buildLog; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}