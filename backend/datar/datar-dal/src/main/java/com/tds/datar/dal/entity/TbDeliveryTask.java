package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 交付任务实体
 */
@TableName("tb_delivery_task")
public class TbDeliveryTask {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fWorkOrderId;           // 工单ID
    private String fTaskType;              // 任务类型：SANDBOX_INIT-沙盒初始化, IMAGE_BUILD-镜像构建, SOURCE_DOWNLOAD-源码下载
    private Integer fStatus;               // 状态：0-待执行, 1-执行中, 2-成功, 3-失败
    private String fSandboxId;             // 沙盒ID
    private String fWorkDirectory;         // 工作目录
    private String fImageName;             // 镜像名称
    private String fImageTag;              // 镜像标签
    private String fSourceUrl;            // 源码URL
    private String fSourcePath;            // 源码本地路径
    private String fBuildLog;              // 构建日志
    private String fErrorMessage;          // 错误信息
    private Long fDuration;                // 执行时长(毫秒)
    private String fTenantId;              // 租户ID
    private LocalDateTime fCreateTime;     // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    private Integer fDeleteMark;           // 删除标记：0-未删, 1-已删

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfWorkOrderId() { return fWorkOrderId; }
    public void setfWorkOrderId(String fWorkOrderId) { this.fWorkOrderId = fWorkOrderId; }
    public String getfTaskType() { return fTaskType; }
    public void setfTaskType(String fTaskType) { this.fTaskType = fTaskType; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfSandboxId() { return fSandboxId; }
    public void setfSandboxId(String fSandboxId) { this.fSandboxId = fSandboxId; }
    public String getfWorkDirectory() { return fWorkDirectory; }
    public void setfWorkDirectory(String fWorkDirectory) { this.fWorkDirectory = fWorkDirectory; }
    public String getfImageName() { return fImageName; }
    public void setfImageName(String fImageName) { this.fImageName = fImageName; }
    public String getfImageTag() { return fImageTag; }
    public void setfImageTag(String fImageTag) { this.fImageTag = fImageTag; }
    public String getfSourceUrl() { return fSourceUrl; }
    public void setfSourceUrl(String fSourceUrl) { this.fSourceUrl = fSourceUrl; }
    public String getfSourcePath() { return fSourcePath; }
    public void setfSourcePath(String fSourcePath) { this.fSourcePath = fSourcePath; }
    public String getfBuildLog() { return fBuildLog; }
    public void setfBuildLog(String fBuildLog) { this.fBuildLog = fBuildLog; }
    public String getfErrorMessage() { return fErrorMessage; }
    public void setfErrorMessage(String fErrorMessage) { this.fErrorMessage = fErrorMessage; }
    public Long getfDuration() { return fDuration; }
    public void setfDuration(Long fDuration) { this.fDuration = fDuration; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}