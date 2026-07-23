package com.tds.datar.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("tb_connector")
public class TbConnector {

    @TableId
    private String fId;
    private String fSn;
    private String fName;
    private Integer fType;
    private Integer fStatus;
    private String fVersion;
    private String fIpAddress;
    private Integer fSshPort;
    private String fSshUsername;
    private String fSshPassword;
    private String fSshPrivateKey;
    private String fMacAddress;
    private LocalDateTime fLastHeartbeat;
    private LocalDateTime fRegisteredTime;
    private String fInstitutionId;
    private String fInstitutionName;
    private String fSpaceId;
    private String fRegion;
    private String fDescription;
    private Integer fIsSystem;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfSn() { return fSn; }
    public void setfSn(String fSn) { this.fSn = fSn; }
    public String getfName() { return fName; }
    public void setfName(String fName) { this.fName = fName; }
    public Integer getfType() { return fType; }
    public void setfType(Integer fType) { this.fType = fType; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfVersion() { return fVersion; }
    public void setfVersion(String fVersion) { this.fVersion = fVersion; }
    public String getfIpAddress() { return fIpAddress; }
    public void setfIpAddress(String fIpAddress) { this.fIpAddress = fIpAddress; }
    public Integer getfSshPort() { return fSshPort; }
    public void setfSshPort(Integer fSshPort) { this.fSshPort = fSshPort; }
    public String getfSshUsername() { return fSshUsername; }
    public void setfSshUsername(String fSshUsername) { this.fSshUsername = fSshUsername; }
    public String getfSshPassword() { return fSshPassword; }
    public void setfSshPassword(String fSshPassword) { this.fSshPassword = fSshPassword; }
    public String getfSshPrivateKey() { return fSshPrivateKey; }
    public void setfSshPrivateKey(String fSshPrivateKey) { this.fSshPrivateKey = fSshPrivateKey; }
    public String getfMacAddress() { return fMacAddress; }
    public void setfMacAddress(String fMacAddress) { this.fMacAddress = fMacAddress; }
    public LocalDateTime getfLastHeartbeat() { return fLastHeartbeat; }
    public void setfLastHeartbeat(LocalDateTime fLastHeartbeat) { this.fLastHeartbeat = fLastHeartbeat; }
    public LocalDateTime getfRegisteredTime() { return fRegisteredTime; }
    public void setfRegisteredTime(LocalDateTime fRegisteredTime) { this.fRegisteredTime = fRegisteredTime; }
    public String getfInstitutionId() { return fInstitutionId; }
    public void setfInstitutionId(String fInstitutionId) { this.fInstitutionId = fInstitutionId; }
    public String getfInstitutionName() { return fInstitutionName; }
    public void setfInstitutionName(String fInstitutionName) { this.fInstitutionName = fInstitutionName; }
    public String getfSpaceId() { return fSpaceId; }
    public void setfSpaceId(String fSpaceId) { this.fSpaceId = fSpaceId; }
    public String getfRegion() { return fRegion; }
    public void setfRegion(String fRegion) { this.fRegion = fRegion; }
    public String getfDescription() { return fDescription; }
    public void setfDescription(String fDescription) { this.fDescription = fDescription; }
    public Integer getfIsSystem() { return fIsSystem; }
    public void setfIsSystem(Integer fIsSystem) { this.fIsSystem = fIsSystem; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}