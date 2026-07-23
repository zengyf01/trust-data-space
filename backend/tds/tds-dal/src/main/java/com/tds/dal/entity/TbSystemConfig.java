package com.tds.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 系统参数配置实体
 */
@TableName("tb_system_config")
public class TbSystemConfig {

    @TableId(type = IdType.INPUT)
    private String fId;                    // 主键ID
    private String fConfigKey;           // 配置键
    private String fConfigValue;         // 配置值
    private String fValueType;           // 值类型：STRING/NUMBER/BOOLEAN/JSON/TEXT
    private String fConfigName;          // 配置名称
    private String fConfigGroup;         // 配置分组
    private String fDescription;         // 描述
    private Integer fSortOrder;           // 排序
    private Integer fIsVisible;           // 是否可见：0-隐藏 1-可见
    private Integer fIsEditable;          // 是否可编辑：0-不可编辑 1-可编辑
    private String fTenantId;            // 租户ID（空表示全局配置）
    private LocalDateTime fCreateTime;    // 创建时间
    private LocalDateTime fUpdateTime;    // 更新时间

    // Getters and Setters
    public String getfId() { return fId; }
    public void setfId(String fId) { this.fId = fId; }
    public String getfConfigKey() { return fConfigKey; }
    public void setfConfigKey(String fConfigKey) { this.fConfigKey = fConfigKey; }
    public String getfConfigValue() { return fConfigValue; }
    public void setfConfigValue(String fConfigValue) { this.fConfigValue = fConfigValue; }
    public String getfValueType() { return fValueType; }
    public void setfValueType(String fValueType) { this.fValueType = fValueType; }
    public String getfConfigName() { return fConfigName; }
    public void setfConfigName(String fConfigName) { this.fConfigName = fConfigName; }
    public String getfConfigGroup() { return fConfigGroup; }
    public void setfConfigGroup(String fConfigGroup) { this.fConfigGroup = fConfigGroup; }
    public String getfDescription() { return fDescription; }
    public void setfDescription(String fDescription) { this.fDescription = fDescription; }
    public Integer getfSortOrder() { return fSortOrder; }
    public void setfSortOrder(Integer fSortOrder) { this.fSortOrder = fSortOrder; }
    public Integer getfIsVisible() { return fIsVisible; }
    public void setfIsVisible(Integer fIsVisible) { this.fIsVisible = fIsVisible; }
    public Integer getfIsEditable() { return fIsEditable; }
    public void setfIsEditable(Integer fIsEditable) { this.fIsEditable = fIsEditable; }
    public String getfTenantId() { return fTenantId; }
    public void setfTenantId(String fTenantId) { this.fTenantId = fTenantId; }
    public LocalDateTime getfCreateTime() { return fCreateTime; }
    public void setfCreateTime(LocalDateTime fCreateTime) { this.fCreateTime = fCreateTime; }
    public LocalDateTime getfUpdateTime() { return fUpdateTime; }
    public void setfUpdateTime(LocalDateTime fUpdateTime) { this.fUpdateTime = fUpdateTime; }
}