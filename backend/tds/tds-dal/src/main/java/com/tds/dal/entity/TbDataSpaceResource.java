package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 数据空间资源实体
 */
@TableName("tb_data_space_resource")
public class TbDataSpaceResource {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fSpaceId;             // 数据空间ID
    private String fResourceType;        // 资源类型：CATALOG/PRODUCT/DATASOURCE
    private String fResourceId;          // 资源ID
    private String fResourceName;        // 资源名称
    private String fResourceDesc;        // 资源描述
    private Integer fAccessLevel;         // 访问级别：1-只读 2-可写 3-可管理
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
    public String getfResourceType() { return fResourceType; }
    public void setfResourceType(String fResourceType) { this.fResourceType = fResourceType; }
    public String getfResourceId() { return fResourceId; }
    public void setfResourceId(String fResourceId) { this.fResourceId = fResourceId; }
    public String getfResourceName() { return fResourceName; }
    public void setfResourceName(String fResourceName) { this.fResourceName = fResourceName; }
    public String getfResourceDesc() { return fResourceDesc; }
    public void setfResourceDesc(String fResourceDesc) { this.fResourceDesc = fResourceDesc; }
    public Integer getfAccessLevel() { return fAccessLevel; }
    public void setfAccessLevel(Integer fAccessLevel) { this.fAccessLevel = fAccessLevel; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
    public Integer getfDeleteMark() { return fDeleteMark; }
    public void setfDeleteMark(Integer fDeleteMark) { this.fDeleteMark = fDeleteMark; }
}