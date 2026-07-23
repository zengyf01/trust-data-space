package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 机构实体
 */
@TableName("tb_organization")
public class TbOrganization {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fOrgCode;              // 机构编码
    private String fOrgName;              // 机构名称
    private String fOrgType;             // 机构类型：ENTERPRISE/GOV/INDIVIDUAL
    private String fOrgDesc;             // 机构描述
    private String fLegalPerson;         // 法人代表
    private String fContact;             // 联系人
    private String fContactPhone;         // 联系电话
    private String fContactEmail;         // 联系邮箱
    private String fAddress;             // 地址
    private String fBusinessLicense;     // 营业执照
    private Integer fStatus;               // 状态：0-待审核 1-正常 2-冻结 3-已注销
    private Integer fUserCount;           // 用户数量
    private Integer fConnectorCount;     // 连接器数量
    private String fTenantId;            // 租户ID
    private LocalDateTime fCreateTime;    // 创建时间
    private LocalDateTime fUpdateTime;    // 更新时间
    @TableLogic
    private Integer fDeleteMark;           // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfOrgCode() { return fOrgCode; }
    public void setfOrgCode(String fOrgCode) { this.fOrgCode = fOrgCode; }
    public String getfOrgName() { return fOrgName; }
    public void setfOrgName(String fOrgName) { this.fOrgName = fOrgName; }
    public String getfOrgType() { return fOrgType; }
    public void setfOrgType(String fOrgType) { this.fOrgType = fOrgType; }
    public String getfOrgDesc() { return fOrgDesc; }
    public void setfOrgDesc(String fOrgDesc) { this.fOrgDesc = fOrgDesc; }
    public String getfLegalPerson() { return fLegalPerson; }
    public void setfLegalPerson(String fLegalPerson) { this.fLegalPerson = fLegalPerson; }
    public String getfContact() { return fContact; }
    public void setfContact(String fContact) { this.fContact = fContact; }
    public String getfContactPhone() { return fContactPhone; }
    public void setfContactPhone(String fContactPhone) { this.fContactPhone = fContactPhone; }
    public String getfContactEmail() { return fContactEmail; }
    public void setfContactEmail(String fContactEmail) { this.fContactEmail = fContactEmail; }
    public String getfAddress() { return fAddress; }
    public void setfAddress(String fAddress) { this.fAddress = fAddress; }
    public String getfBusinessLicense() { return fBusinessLicense; }
    public void setfBusinessLicense(String fBusinessLicense) { this.fBusinessLicense = fBusinessLicense; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public Integer getfUserCount() { return fUserCount; }
    public void setfUserCount(Integer fUserCount) { this.fUserCount = fUserCount; }
    public Integer getfConnectorCount() { return fConnectorCount; }
    public void setfConnectorCount(Integer fConnectorCount) { this.fConnectorCount = fConnectorCount; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}