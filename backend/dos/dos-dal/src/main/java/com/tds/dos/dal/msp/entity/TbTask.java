package com.tds.dos.dal.msp.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * MSP Task entity
 */
@TableName("tb_task")
public class TbTask {
    @TableId
    private String fId;

    private String fTaskCode;
    private String fName;
    private Integer fType;
    private Integer fStatus;
    private String fAlgorithm;
    private String fParticipants;
    private String fInputs;
    private String fParameters;
    private String fDescription;
    private String fCode;
    private String fResult;
    private String fExecutionLog;
    private String fNodeMode;
    private String fCreator;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime fCreateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime fUpdateTime;

    @TableLogic
    private Integer fDeleteMark;

    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }

    public String getfTaskCode() { return fTaskCode; }
    public void setfTaskCode(String fTaskCode) { this.fTaskCode = fTaskCode; }

    public String getfName() { return fName; }
    public void setfName(String fName) { this.fName = fName; }

    public Integer getfType() { return fType; }
    public void setfType(Integer fType) { this.fType = fType; }

    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }

    public String getfAlgorithm() { return fAlgorithm; }
    public void setfAlgorithm(String fAlgorithm) { this.fAlgorithm = fAlgorithm; }

    public String getfParticipants() { return fParticipants; }
    public void setfParticipants(String fParticipants) { this.fParticipants = fParticipants; }

    public String getfInputs() { return fInputs; }
    public void setfInputs(String fInputs) { this.fInputs = fInputs; }

    public String getfParameters() { return fParameters; }
    public void setfParameters(String fParameters) { this.fParameters = fParameters; }

    public String getfDescription() { return fDescription; }
    public void setfDescription(String fDescription) { this.fDescription = fDescription; }

    public String getfCode() { return fCode; }
    public void setfCode(String fCode) { this.fCode = fCode; }

    public String getfResult() { return fResult; }
    public void setfResult(String fResult) { this.fResult = fResult; }

    public String getfExecutionLog() { return fExecutionLog; }
    public void setfExecutionLog(String fExecutionLog) { this.fExecutionLog = fExecutionLog; }

    public String getfNodeMode() { return fNodeMode; }
    public void setfNodeMode(String fNodeMode) { this.fNodeMode = fNodeMode; }

    public String getfCreator() { return fCreator; }
    public void setfCreator(String fCreator) { this.fCreator = fCreator; }

    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }

    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }

    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}
