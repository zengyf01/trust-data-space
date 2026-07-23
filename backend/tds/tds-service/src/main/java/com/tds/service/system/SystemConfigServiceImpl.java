package com.tds.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tds.common.enums.NotificationStatus;
import com.tds.common.exception.BusinessException;
import com.tds.dal.entity.TbNotificationConfig;
import com.tds.dal.entity.TbNotificationLog;
import com.tds.dal.entity.TbSystemConfig;
import com.tds.dal.mapper.TbNotificationConfigMapper;
import com.tds.dal.mapper.TbNotificationLogMapper;
import com.tds.dal.mapper.TbSystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统管理服务实现
 */
@Service
public class SystemConfigServiceImpl implements ISystemConfigService {

    @Autowired
    private TbSystemConfigMapper configMapper;

    @Autowired
    private TbNotificationConfigMapper notificationConfigMapper;

    @Autowired
    private TbNotificationLogMapper notificationLogMapper;

    // 本地缓存配置
    private static final Map<String, String> CONFIG_CACHE = new ConcurrentHashMap<>();

    // ==================== 系统参数配置 ====================

    @Override
    public IPage<TbSystemConfig> getConfigPage(int currentPage, int pageSize,
            String configGroup, String configName) {
        Page<TbSystemConfig> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbSystemConfig> wrapper = new LambdaQueryWrapper<>();
        if (configGroup != null && !configGroup.isEmpty()) {
            wrapper.eq(TbSystemConfig::getfConfigGroup, configGroup);
        }
        if (configName != null && !configName.isEmpty()) {
            wrapper.like(TbSystemConfig::getfConfigName, configName);
        }
        wrapper.orderByAsc(TbSystemConfig::getfSortOrder);
        return configMapper.selectPage(page, wrapper);
    }

    @Override
    public TbSystemConfig getConfigById(String id) {
        return configMapper.selectById(id);
    }

    @Override
    public TbSystemConfig getConfigByKey(String configKey, String tenantId) {
        LambdaQueryWrapper<TbSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbSystemConfig::getfConfigKey, configKey)
                .and(w -> w.eq(TbSystemConfig::getfTenantId, tenantId)
                        .or()
                        .isNull(TbSystemConfig::getfTenantId))
                .orderByDesc(TbSystemConfig::getfTenantId)
                .last("LIMIT 1");
        return configMapper.selectOne(wrapper);
    }

    @Override
    public String getConfigValue(String configKey, String tenantId) {
        String cacheKey = configKey + ":" + (tenantId != null ? tenantId : "global");
        if (CONFIG_CACHE.containsKey(cacheKey)) {
            return CONFIG_CACHE.get(cacheKey);
        }

        TbSystemConfig config = getConfigByKey(configKey, tenantId);
        if (config != null) {
            CONFIG_CACHE.put(cacheKey, config.getfConfigValue());
            return config.getfConfigValue();
        }
        return null;
    }

    @Override
    public List<TbSystemConfig> getConfigsByGroup(String configGroup, String tenantId) {
        LambdaQueryWrapper<TbSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbSystemConfig::getfConfigGroup, configGroup)
                .and(w -> w.eq(TbSystemConfig::getfTenantId, tenantId)
                        .or()
                        .isNull(TbSystemConfig::getfTenantId))
                .orderByAsc(TbSystemConfig::getfSortOrder);
        return configMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public TbSystemConfig createConfig(SystemConfigDTO dto) {
        // 检查key唯一性
        LambdaQueryWrapper<TbSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbSystemConfig::getfConfigKey, dto.getConfigKey())
                .eq(dto.getTenantId() != null, TbSystemConfig::getfTenantId, dto.getTenantId());
        if (configMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("配置键已存在");
        }

        TbSystemConfig config = new TbSystemConfig();
        config.setfId(UUID.randomUUID().toString().replace("-", ""));
        config.setfConfigKey(dto.getConfigKey());
        config.setfConfigValue(dto.getConfigValue());
        config.setfValueType(dto.getValueType() != null ? dto.getValueType() : "STRING");
        config.setfConfigName(dto.getConfigName());
        config.setfConfigGroup(dto.getConfigGroup());
        config.setfDescription(dto.getDescription());
        config.setfSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        config.setfIsVisible(dto.getIsVisible() != null ? dto.getIsVisible() : 1);
        config.setfIsEditable(dto.getIsEditable() != null ? dto.getIsEditable() : 1);
        config.setfTenantId(dto.getTenantId());
        config.setfCreateTime(LocalDateTime.now());
        config.setfUpdateTime(LocalDateTime.now());

        configMapper.insert(config);
        return config;
    }

    @Override
    @Transactional
    public TbSystemConfig updateConfig(String id, SystemConfigDTO dto) {
        TbSystemConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("系统参数不存在");
        }
        if (config.getfIsEditable() != null && config.getfIsEditable() == 0) {
            throw new BusinessException("该参数不可编辑");
        }

        config.setfConfigValue(dto.getConfigValue());
        config.setfConfigName(dto.getConfigName());
        config.setfDescription(dto.getDescription());
        if (dto.getSortOrder() != null) {
            config.setfSortOrder(dto.getSortOrder());
        }
        config.setfUpdateTime(LocalDateTime.now());

        configMapper.updateById(config);

        // 清除缓存
        String cacheKey = config.getfConfigKey() + ":" + (config.getfTenantId() != null ? config.getfTenantId() : "global");
        CONFIG_CACHE.remove(cacheKey);

        return config;
    }

    @Override
    @Transactional
    public void deleteConfig(String id) {
        TbSystemConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("系统参数不存在");
        }
        configMapper.deleteById(id);

        // 清除缓存
        String cacheKey = config.getfConfigKey() + ":" + (config.getfTenantId() != null ? config.getfTenantId() : "global");
        CONFIG_CACHE.remove(cacheKey);
    }

    @Override
    @Transactional
    public void batchUpdateConfigs(List<SystemConfigDTO> configs) {
        for (SystemConfigDTO dto : configs) {
            if (dto.getId() != null) {
                updateConfig(dto.getId(), dto);
            }
        }
    }

    // ==================== 通知配置 ====================

    @Override
    public IPage<TbNotificationConfig> getNotificationConfigPage(int currentPage, int pageSize,
            String notificationType) {
        Page<TbNotificationConfig> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbNotificationConfig> wrapper = new LambdaQueryWrapper<>();
        if (notificationType != null && !notificationType.isEmpty()) {
            wrapper.eq(TbNotificationConfig::getfNotificationType, notificationType);
        }
        wrapper.orderByDesc(TbNotificationConfig::getfCreateTime);
        return notificationConfigMapper.selectPage(page, wrapper);
    }

    @Override
    public TbNotificationConfig getNotificationConfigById(String id) {
        return notificationConfigMapper.selectById(id);
    }

    @Override
    public TbNotificationConfig getEnabledConfig(String notificationType) {
        LambdaQueryWrapper<TbNotificationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbNotificationConfig::getfNotificationType, notificationType)
                .eq(TbNotificationConfig::getfIsEnabled, 1)
                .eq(TbNotificationConfig::getfDeleteMark, 0)
                .last("LIMIT 1");
        return notificationConfigMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public TbNotificationConfig createNotificationConfig(NotificationConfigDTO dto) {
        TbNotificationConfig config = new TbNotificationConfig();
        config.setfId(UUID.randomUUID().toString().replace("-", ""));
        config.setfConfigCode("NC" + System.currentTimeMillis());
        config.setfConfigName(dto.getConfigName());
        config.setfNotificationType(dto.getNotificationType());
        config.setfIsEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : 0);
        config.setfHost(dto.getHost());
        config.setfPort(dto.getPort());
        config.setfUsername(dto.getUsername());
        config.setfPassword(dto.getPassword());
        config.setfApiKey(dto.getApiKey());
        config.setfApiSecret(dto.getApiSecret());
        config.setfSignature(dto.getSignature());
        config.setfTemplateCode(dto.getTemplateCode());
        config.setfWebhookUrl(dto.getWebhookUrl());
        config.setfDescription(dto.getDescription());
        config.setfTenantId(dto.getTenantId());
        config.setfCreateTime(LocalDateTime.now());
        config.setfUpdateTime(LocalDateTime.now());
        config.setfDeleteMark(0);

        notificationConfigMapper.insert(config);
        return config;
    }

    @Override
    @Transactional
    public TbNotificationConfig updateNotificationConfig(String id, NotificationConfigDTO dto) {
        TbNotificationConfig config = notificationConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("通知配置不存在");
        }
        config.setfConfigName(dto.getConfigName());
        config.setfHost(dto.getHost());
        config.setfPort(dto.getPort());
        config.setfUsername(dto.getUsername());
        config.setfPassword(dto.getPassword());
        config.setfApiKey(dto.getApiKey());
        config.setfApiSecret(dto.getApiSecret());
        config.setfSignature(dto.getSignature());
        config.setfTemplateCode(dto.getTemplateCode());
        config.setfWebhookUrl(dto.getWebhookUrl());
        config.setfDescription(dto.getDescription());
        config.setfUpdateTime(LocalDateTime.now());
        notificationConfigMapper.updateById(config);
        return config;
    }

    @Override
    @Transactional
    public void deleteNotificationConfig(String id) {
        TbNotificationConfig config = notificationConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("通知配置不存在");
        }
        config.setfDeleteMark(1);
        config.setfUpdateTime(LocalDateTime.now());
        notificationConfigMapper.updateById(config);
    }

    @Override
    @Transactional
    public TbNotificationConfig toggleNotificationConfig(String id, Integer isEnabled) {
        TbNotificationConfig config = notificationConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("通知配置不存在");
        }
        config.setfIsEnabled(isEnabled);
        config.setfUpdateTime(LocalDateTime.now());
        notificationConfigMapper.updateById(config);
        return config;
    }

    // ==================== 通知发送 ====================

    @Override
    @Transactional
    public TbNotificationLog sendNotification(NotificationDTO dto) {
        TbNotificationConfig notificationConfig = getEnabledConfig(dto.getNotificationType());
        if (notificationConfig == null) {
            throw new BusinessException("通知配置不存在或未启用");
        }

        TbNotificationLog log = new TbNotificationLog();
        log.setfId(UUID.randomUUID().toString().replace("-", ""));
        log.setfNotificationType(dto.getNotificationType());
        log.setfRecipient(dto.getRecipient());
        log.setfRecipientName(dto.getRecipientName());
        log.setfSubject(dto.getSubject());
        log.setfContent(dto.getContent());
        log.setfStatus(NotificationStatus.PENDING.getCode());
        log.setfRetryCount(0);
        log.setfTenantId(dto.getTenantId());
        log.setfCreateTime(LocalDateTime.now());
        notificationLogMapper.insert(log);

        // 模拟发送
        try {
            log.setfStatus(NotificationStatus.SUCCESS.getCode());
            log.setfSendTime(LocalDateTime.now());
        } catch (Exception e) {
            log.setfStatus(NotificationStatus.FAILED.getCode());
            log.setfErrorMessage(e.getMessage());
        }

        notificationLogMapper.updateById(log);
        return log;
    }

    @Override
    public IPage<TbNotificationLog> getNotificationLogPage(int currentPage, int pageSize,
            String notificationType, Integer status) {
        Page<TbNotificationLog> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<TbNotificationLog> wrapper = new LambdaQueryWrapper<>();
        if (notificationType != null && !notificationType.isEmpty()) {
            wrapper.eq(TbNotificationLog::getfNotificationType, notificationType);
        }
        if (status != null) {
            wrapper.eq(TbNotificationLog::getfStatus, status);
        }
        wrapper.orderByDesc(TbNotificationLog::getfCreateTime);
        return notificationLogMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public TbNotificationLog retrySend(String logId) {
        TbNotificationLog log = notificationLogMapper.selectById(logId);
        if (log == null) {
            throw new BusinessException("通知日志不存在");
        }

        log.setfStatus(NotificationStatus.SENDING.getCode());
        log.setfRetryCount(log.getfRetryCount() + 1);
        notificationLogMapper.updateById(log);

        try {
            // 模拟重试发送
            log.setfStatus(NotificationStatus.SUCCESS.getCode());
            log.setfSendTime(LocalDateTime.now());
            log.setfErrorMessage(null);
        } catch (Exception e) {
            log.setfStatus(NotificationStatus.FAILED.getCode());
            log.setfErrorMessage(e.getMessage());
        }

        notificationLogMapper.updateById(log);
        return log;
    }
}