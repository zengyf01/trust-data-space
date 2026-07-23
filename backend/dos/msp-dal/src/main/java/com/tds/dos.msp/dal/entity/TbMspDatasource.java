package com.tds.dos.msp.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * MSP DataSource entity
 */
@TableName("tb_msp_datasource")
public class TbMspDatasource {
    @TableId
    private String fId;

    private String fDatasourceId;
    private String fNodeId;
    private String fName;
    private Integer fType;
    private String fHost;
    private Integer fPort;
    private String fDatabaseName;
    private String fUsername;
    private String fPassword;
    private String fTableName;
    private String fColumns;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfDatasourceId() { return fDatasourceId; }
    public void setfDatasourceId(String fDatasourceId) { this.fDatasourceId = fDatasourceId; }
    public String getfNodeId() { return fNodeId; }
    public void setfNodeId(String fNodeId) { this.fNodeId = fNodeId; }
    public String getfName() { return fName; }
    public void setfName(String fName) { this.fName = fName; }
    public Integer getfType() { return fType; }
    public void setfType(Integer fType) { this.fType = fType; }
    public String getfHost() { return fHost; }
    public void setfHost(String fHost) { this.fHost = fHost; }
    public Integer getfPort() { return fPort; }
    public void setfPort(Integer fPort) { this.fPort = fPort; }
    public String getfDatabaseName() { return fDatabaseName; }
    public void setfDatabaseName(String fDatabaseName) { this.fDatabaseName = fDatabaseName; }
    public String getfUsername() { return fUsername; }
    public void setfUsername(String fUsername) { this.fUsername = fUsername; }
    public String getfPassword() { return fPassword; }
    public void setfPassword(String fPassword) { this.fPassword = fPassword; }
    public String getfTableName() { return fTableName; }
    public void setfTableName(String fTableName) { this.fTableName = fTableName; }
    public String getfColumns() { return fColumns; }
    public void setfColumns(String fColumns) { this.fColumns = fColumns; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}