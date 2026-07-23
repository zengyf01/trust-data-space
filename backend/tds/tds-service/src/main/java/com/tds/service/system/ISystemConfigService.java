package com.tds.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tds.dal.entity.TbNotificationConfig;
import com.tds.dal.entity.TbNotificationLog;
import com.tds.dal.entity.TbSystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统管理服务接口
 */
public interface ISystemConfigService {

    // ==================== 系统参数配置 ====================

    /**
     * 分页查询系统参数
     */
    IPage<TbSystemConfig> getConfigPage(int currentPage, int pageSize, String configGroup, String configName);

    /**
     * 获取参数详情
     */
    TbSystemConfig getConfigById(String id);

    /**
     * 根据key获取参数
     */
    TbSystemConfig getConfigByKey(String configKey, String tenantId);

    /**
     * 获取参数值
     */
    String getConfigValue(String configKey, String tenantId);

    /**
     * 根据分组获取参数列表
     */
    List<TbSystemConfig> getConfigsByGroup(String configGroup, String tenantId);

    /**
     * 创建系统参数
     */
    TbSystemConfig createConfig(SystemConfigDTO dto);

    /**
     * 更新系统参数
     */
    TbSystemConfig updateConfig(String id, SystemConfigDTO dto);

    /**
     * 删除系统参数
     */
    void deleteConfig(String id);

    /**
     * 批量更新参数
     */
    void batchUpdateConfigs(List<SystemConfigDTO> configs);

    // ==================== 通知配置 ====================

    /**
     * 分页查询通知配置
     */
    IPage<TbNotificationConfig> getNotificationConfigPage(int currentPage, int pageSize, String notificationType);

    /**
     * 获取通知配置详情
     */
    TbNotificationConfig getNotificationConfigById(String id);

    /**
     * 获取启用的通知配置
     */
    TbNotificationConfig getEnabledConfig(String notificationType);

    /**
     * 创建通知配置
     */
    TbNotificationConfig createNotificationConfig(NotificationConfigDTO dto);

    /**
     * 更新通知配置
     */
    TbNotificationConfig updateNotificationConfig(String id, NotificationConfigDTO dto);

    /**
     * 删除通知配置
     */
    void deleteNotificationConfig(String id);

    /**
     * 启用/禁用通知配置
     */
    TbNotificationConfig toggleNotificationConfig(String id, Integer isEnabled);

    // ==================== 通知发送 ====================

    /**
     * 发送通知
     */
    TbNotificationLog sendNotification(NotificationDTO dto);

    /**
     * 分页查询通知发送记录
     */
    IPage<TbNotificationLog> getNotificationLogPage(int currentPage, int pageSize, String notificationType, Integer status);

    /**
     * 重试发送
     */
    TbNotificationLog retrySend(String logId);
}