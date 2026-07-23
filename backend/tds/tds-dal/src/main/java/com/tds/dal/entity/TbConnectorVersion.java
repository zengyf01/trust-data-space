package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 连接器版本实体
 */
@TableName("tb_connector_version")
public class TbConnectorVersion {

    @TableId
    private String id;

    /** 连接器ID */
    private String connectorId;

    /** 版本号 */
    private String version;

    /** 文件路径 */
    private String filePath;

    /** 文件大小 */
    private Long fileSize;

    /** 文件MD5 */
    private String fileMd5;

    /** 变更日志 */
    private String changeLog;

    /** 状态(0未激活,1激活中,2已激活) */
    private Integer status;

    /** 租户ID */
    private String fTenantId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    /** 删除标志(0未删,1已删) */
    @TableLogic
    private Integer fDeleteMark;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnectorId() { return connectorId; }
    public void setConnectorId(String connectorId) { this.connectorId = connectorId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFileMd5() { return fileMd5; }
    public void setFileMd5(String fileMd5) { this.fileMd5 = fileMd5; }
    public String getChangeLog() { return changeLog; }
    public void setChangeLog(String changeLog) { this.changeLog = changeLog; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}