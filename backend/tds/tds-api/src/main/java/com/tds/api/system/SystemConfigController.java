package com.tds.api.system;

import com.tds.common.core.ApiResponse;
import com.tds.service.system.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统管理
 */
@RestController
@RequestMapping("/system")
public class SystemConfigController {

    @Autowired
    private ISystemConfigService systemConfigService;

    // ==================== 系统参数配置 ====================

    @GetMapping("/config/page")
    public ApiResponse<?> getConfigPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String configGroup,
            @RequestParam(required = false) String configName) {
        return ApiResponse.success(systemConfigService.getConfigPage(currentPage, pageSize, configGroup, configName));
    }

    @GetMapping("/config/{id}")
    public ApiResponse<?> getConfigById(@PathVariable String id) {
        return ApiResponse.success(systemConfigService.getConfigById(id));
    }

    @GetMapping("/config/key/{configKey}")
    public ApiResponse<?> getConfigByKey(
            @PathVariable String configKey,
            @RequestParam(required = false) String tenantId) {
        return ApiResponse.success(systemConfigService.getConfigByKey(configKey, tenantId));
    }

    @GetMapping("/config/value/{configKey}")
    public ApiResponse<?> getConfigValue(
            @PathVariable String configKey,
            @RequestParam(required = false) String tenantId) {
        return ApiResponse.success(systemConfigService.getConfigValue(configKey, tenantId));
    }

    @GetMapping("/config/group/{configGroup}")
    public ApiResponse<?> getConfigsByGroup(
            @PathVariable String configGroup,
            @RequestParam(required = false) String tenantId) {
        return ApiResponse.success(systemConfigService.getConfigsByGroup(configGroup, tenantId));
    }

    @PostMapping("/config")
    public ApiResponse<?> createConfig(@RequestBody SystemConfigDTO dto) {
        return ApiResponse.success(systemConfigService.createConfig(dto));
    }

    @PutMapping("/config/{id}")
    public ApiResponse<?> updateConfig(@PathVariable String id, @RequestBody SystemConfigDTO dto) {
        return ApiResponse.success(systemConfigService.updateConfig(id, dto));
    }

    @DeleteMapping("/config/{id}")
    public ApiResponse<?> deleteConfig(@PathVariable String id) {
        systemConfigService.deleteConfig(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/config/batch")
    public ApiResponse<?> batchUpdateConfigs(@RequestBody List<SystemConfigDTO> configs) {
        systemConfigService.batchUpdateConfigs(configs);
        return ApiResponse.success(null);
    }

    // ==================== 通知配置 ====================

    @GetMapping("/notification/config/page")
    public ApiResponse<?> getNotificationConfigPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String notificationType) {
        return ApiResponse.success(systemConfigService.getNotificationConfigPage(currentPage, pageSize, notificationType));
    }

    @GetMapping("/notification/config/{id}")
    public ApiResponse<?> getNotificationConfigById(@PathVariable String id) {
        return ApiResponse.success(systemConfigService.getNotificationConfigById(id));
    }

    @GetMapping("/notification/config/enabled/{notificationType}")
    public ApiResponse<?> getEnabledConfig(@PathVariable String notificationType) {
        return ApiResponse.success(systemConfigService.getEnabledConfig(notificationType));
    }

    @PostMapping("/notification/config")
    public ApiResponse<?> createNotificationConfig(@RequestBody NotificationConfigDTO dto) {
        return ApiResponse.success(systemConfigService.createNotificationConfig(dto));
    }

    @PutMapping("/notification/config/{id}")
    public ApiResponse<?> updateNotificationConfig(@PathVariable String id, @RequestBody NotificationConfigDTO dto) {
        return ApiResponse.success(systemConfigService.updateNotificationConfig(id, dto));
    }

    @DeleteMapping("/notification/config/{id}")
    public ApiResponse<?> deleteNotificationConfig(@PathVariable String id) {
        systemConfigService.deleteNotificationConfig(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/notification/config/{id}/toggle")
    public ApiResponse<?> toggleNotificationConfig(
            @PathVariable String id,
            @RequestParam Integer isEnabled) {
        return ApiResponse.success(systemConfigService.toggleNotificationConfig(id, isEnabled));
    }

    // ==================== 通知发送 ====================

    @PostMapping("/notification/send")
    public ApiResponse<?> sendNotification(@RequestBody NotificationDTO dto) {
        return ApiResponse.success(systemConfigService.sendNotification(dto));
    }

    @GetMapping("/notification/log/page")
    public ApiResponse<?> getNotificationLogPage(
            @RequestParam int currentPage,
            @RequestParam int pageSize,
            @RequestParam(required = false) String notificationType,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(systemConfigService.getNotificationLogPage(currentPage, pageSize, notificationType, status));
    }

    @PostMapping("/notification/log/{logId}/retry")
    public ApiResponse<?> retrySend(@PathVariable String logId) {
        return ApiResponse.success(systemConfigService.retrySend(logId));
    }
}