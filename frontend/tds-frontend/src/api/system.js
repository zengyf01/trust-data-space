import api from './index'

export default {
  // ============ 系统配置 ============
  // 配置分页
  getConfigPage(params) {
    return api.get('/system/config/page', { params })
  },

  // 获取配置详情
  getConfig(id) {
    return api.get(`/system/config/${id}`)
  },

  // 通过Key获取
  getConfigByKey(configKey, tenantId) {
    return api.get('/system/config/key/{configKey}', { params: { configKey, tenantId } })
  },

  // 获取配置值
  getConfigValue(configKey, tenantId) {
    return api.get('/system/config/value/{configKey}', { params: { configKey, tenantId } })
  },

  // 获取分组配置
  getConfigByGroup(configGroup, tenantId) {
    return api.get('/system/config/group/{configGroup}', { params: { configGroup, tenantId } })
  },

  // 创建配置
  createConfig(data) {
    return api.post('/system/config', data)
  },

  // 更新配置
  updateConfig(id, data) {
    return api.put(`/system/config/${id}`, data)
  },

  // 删除配置
  deleteConfig(id) {
    return api.delete(`/system/config/${id}`)
  },

  // 批量更新配置
  batchUpdateConfig(data) {
    return api.post('/system/config/batch', data)
  },

  // ============ 通知配置 ============
  // 通知配置分页
  getNotificationConfigPage(params) {
    return api.get('/system/notification/config/page', { params })
  },

  // 获取通知配置详情
  getNotificationConfig(id) {
    return api.get(`/system/notification/config/${id}`)
  },

  // 获取启用配置
  getEnabledNotificationConfig(notificationType) {
    return api.get(`/system/notification/config/enabled/{notificationType}`, {
      params: { notificationType }
    })
  },

  // 创建通知配置
  createNotificationConfig(data) {
    return api.post('/system/notification/config', data)
  },

  // 更新通知配置
  updateNotificationConfig(id, data) {
    return api.put(`/system/notification/config/${id}`, data)
  },

  // 删除通知配置
  deleteNotificationConfig(id) {
    return api.delete(`/system/notification/config/${id}`)
  },

  // 切换通知配置
  toggleNotificationConfig(id, isEnabled) {
    return api.put(`/system/notification/config/{id}/toggle`, null, { params: { id, isEnabled } })
  },

  // ============ 通知发送 ============
  // 发送通知
  sendNotification(data) {
    return api.post('/system/notification/send', data)
  },

  // 通知日志分页
  getNotificationLogPage(params) {
    return api.get('/system/notification/log/page', { params })
  },

  // 重试发送
  retryNotification(logId) {
    return api.post(`/system/notification/log/${logId}/retry`, null, { params: { logId } })
  }
}
