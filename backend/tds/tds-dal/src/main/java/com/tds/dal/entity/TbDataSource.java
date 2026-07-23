package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 数据源实体
 */
@TableName("tb_data_source")
public class TbDataSource {

    @TableId
    private String id;

    /** 数据源编号 */
    private String sourceCode;

    /** 数据源名称 */
    private String sourceName;

    /** 数据源类型(1 MySQL, 2 PostgreSQL, 3 SFTP, 4 HTTP, 5 OSS) */
    private Integer sourceType;

    /** 主机地址 */
    private String host;

    /** 端口 */
    private Integer port;

    /** 数据库名 */
    private String databaseName;

    /** 用户名 */
    private String username;

    /** 密码(加密存储) */
    private String password;

    /** 基础路径(SFTP/OSS用) */
    private String basePath;

    /** 连接参数(JSON) */
    private String connParams;

    /** 状态(1启用,2禁用) */
    private Integer status;

    /** 最后测试时间 */
    private LocalDateTime lastTestTime;

    /** 最后测试结果 */
    private String lastTestResult;

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
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }
    public String getConnParams() { return connParams; }
    public void setConnParams(String connParams) { this.connParams = connParams; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getLastTestTime() { return lastTestTime; }
    public void setLastTestTime(LocalDateTime lastTestTime) { this.lastTestTime = lastTestTime; }
    public String getLastTestResult() { return lastTestResult; }
    public void setLastTestResult(String lastTestResult) { this.lastTestResult = lastTestResult; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}