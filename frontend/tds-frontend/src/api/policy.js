import api from './index'

export default {
  // 策略分页
  getPolicyPage(params) {
    return api.get('/policy/page', { params })
  },

  // 获取策略详情
  getPolicy(id) {
    return api.get(`/policy/${id}`)
  },

  // 创建策略
  createPolicy(data) {
    return api.post('/policy', data)
  },

  // 更新策略
  updatePolicy(id, data) {
    return api.put(`/policy/${id}`, data)
  },

  // 删除策略
  deletePolicy(id) {
    return api.delete(`/policy/${id}`)
  },

  // 切换策略状态
  togglePolicy(id, status) {
    return api.post(`/policy/${id}/toggle`, null, { params: { status } })
  },

  // 绑定策略
  bindPolicy(data) {
    return api.post('/policy/bind', data)
  },

  // 解绑策略
  unbindPolicy(bindingId) {
    return api.delete(`/policy/bind/${bindingId}`)
  },

  // 获取资源绑定
  getResourceBinding(resourceType, resourceId) {
    return api.get('/policy/bind/resource', { params: { resourceType, resourceId } })
  },

  // 检查访问权限
  checkAccess(data) {
    return api.post('/policy/check', data)
  },

  // 访问日志分页
  getAccessLogPage(params) {
    return api.get('/policy/accessLog/page', { params })
  },

  // 执行日志分页
  getExecLogPage(params) {
    return api.get('/policy/execLog/page', { params })
  }
}
