package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("tb_data_source")
public class TbDataSource {

    @TableId(value = "f_id")
    private String id;

    @TableField("f_data_source_code")
    private String sourceCode;

    @TableField("f_data_source_name")
    private String sourceName;

    @TableField("f_data_source_type")
    private String sourceType;

    @TableField("f_host")
    private String host;

    @TableField("f_port")
    private Integer port;

    @TableField("f_database_name")
    private String databaseName;

    @TableField("f_username")
    private String username;

    @TableField("f_password")
    private String password;

    @TableField("f_private_key")
    private String privateKey;

    @TableField("f_base_path")
    private String basePath;

    @TableField("f_conn_params")
    private String connParams;

    @TableField("f_status")
    private Integer status;

    @TableField("f_tenant_id")
    private String tenantId;

    @TableField("f_space_id")
    private String spaceId;

    @TableField(value = "f_create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "f_update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("f_delete_mark")
    private Integer deleteMark;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
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
    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }
    public String getConnParams() { return connParams; }
    public void setConnParams(String connParams) { this.connParams = connParams; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDeleteMark() { return deleteMark; }
    public void setDeleteMark(Integer deleteMark) { this.deleteMark = deleteMark; }

    // Aliases for MyBatis-Plus compatibility and service calls
    public void setfTenantId(String tenantId) { this.tenantId = tenantId; }
    public LocalDateTime getfCreateTime() { return createTime; }
    public void setfCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getfUpdateTime() { return updateTime; }
    public void setfUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getfDeleteMark() { return deleteMark; }
    public void setfDeleteMark(Integer deleteMark) { this.deleteMark = deleteMark; }
}