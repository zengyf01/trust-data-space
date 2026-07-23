package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 数据空间实体
 */
@TableName("tb_data_space")
public class TbDataSpace {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fSpaceCode;            // 空间编码
    private String fSpaceName;            // 空间名称
    private String fSpaceDesc;            // 空间描述
    private String fOwnerId;              // 所有者ID
    private String fOwnerName;             // 所有者名称
    private String fOrganizationId;       // 所属机构ID
    private String fOrganizationName;     // 所属机构名称
    private Integer fStatus;               // 状态：0-待审核 1-正常 2-冻结 3-已注销
    private String fSpaceType;            // 空间类型：PUBLIC/PRIVATE
    private Integer fMemberCount;          // 成员数量
    private Integer fResourceCount;        // 资源数量
    private String fTenantId;             // 租户ID
    private LocalDateTime fCreateTime;     // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    @TableLogic
    private Integer fDeleteMark;           // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfSpaceCode() { return fSpaceCode; }
    public void setfSpaceCode(String fSpaceCode) { this.fSpaceCode = fSpaceCode; }
    public String getfSpaceName() { return fSpaceName; }
    public void setfSpaceName(String fSpaceName) { this.fSpaceName = fSpaceName; }
    public String getfSpaceDesc() { return fSpaceDesc; }
    public void setfSpaceDesc(String fSpaceDesc) { this.fSpaceDesc = fSpaceDesc; }
    public String getfOwnerId() { return fOwnerId; }
    public void setfOwnerId(String fOwnerId) { this.fOwnerId = fOwnerId; }
    public String getfOwnerName() { return fOwnerName; }
    public void setfOwnerName(String fOwnerName) { this.fOwnerName = fOwnerName; }
    public String getfOrganizationId() { return fOrganizationId; }
    public void setfOrganizationId(String fOrganizationId) { this.fOrganizationId = fOrganizationId; }
    public String getfOrganizationName() { return fOrganizationName; }
    public void setfOrganizationName(String fOrganizationName) { this.fOrganizationName = fOrganizationName; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfSpaceType() { return fSpaceType; }
    public void setfSpaceType(String fSpaceType) { this.fSpaceType = fSpaceType; }
    public Integer getfMemberCount() { return fMemberCount; }
    public void setfMemberCount(Integer fMemberCount) { this.fMemberCount = fMemberCount; }
    public Integer getfResourceCount() { return fResourceCount; }
    public void setfResourceCount(Integer fResourceCount) { this.fResourceCount = fResourceCount; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}