package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 数据空间成员实体
 */
@TableName("tb_data_space_member")
public class TbDataSpaceMember {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fSpaceId;             // 数据空间ID
    private String fOrganizationId;       // 机构ID
    private String fOrganizationName;     // 机构名称
    private Integer fRole;               // 角色：1-所有者 2-管理员 3-成员 4-访客
    private Integer fStatus;               // 状态：0-待审核 1-已加入 2-已拒绝
    private String fApplyReason;          // 申请原因
    private LocalDateTime fJoinTime;       // 加入时间
    private LocalDateTime fExpireTime;     // 过期时间
    private String fTenantId;             // 租户ID
    private LocalDateTime fCreateTime;     // 创建时间
    private LocalDateTime fUpdateTime;     // 更新时间
    @TableLogic
    private Integer fDeleteMark;           // 删除标记

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfSpaceId() { return fSpaceId; }
    public void setfSpaceId(String fSpaceId) { this.fSpaceId = fSpaceId; }
    public String getfOrganizationId() { return fOrganizationId; }
    public void setfOrganizationId(String fOrganizationId) { this.fOrganizationId = fOrganizationId; }
    public String getfOrganizationName() { return fOrganizationName; }
    public void setfOrganizationName(String fOrganizationName) { this.fOrganizationName = fOrganizationName; }
    public Integer getfRole() { return fRole; }
    public void setfRole(Integer fRole) { this.fRole = fRole; }
    public Integer getfStatus() { return fStatus; }
    public void setfStatus(Integer fStatus) { this.fStatus = fStatus; }
    public String getfApplyReason() { return fApplyReason; }
    public void setfApplyReason(String fApplyReason) { this.fApplyReason = fApplyReason; }
    public LocalDateTime getfJoinTime() { return fJoinTime; }
    public void setfJoinTime(LocalDateTime fJoinTime) { this.fJoinTime = fJoinTime; }
    public LocalDateTime getfExpireTime() { return fExpireTime; }
    public void setfExpireTime(LocalDateTime fExpireTime) { this.fExpireTime = fExpireTime; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}