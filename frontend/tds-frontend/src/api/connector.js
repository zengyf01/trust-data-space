import api from './index'

export default {
  // 连接器分页
  getConnectorPage(params) {
    return api.post('/connector/page', null, { params })
  },

  // 获取连接器详情
  getConnector(id) {
    return api.get(`/connector/${id}`)
  },

  // 通过SN获取连接器
  getConnectorBySn(sn) {
    return api.get(`/connector/sn/${sn}`)
  },

  // 创建连接器
  createConnector(data) {
    return api.post('/connector', data)
  },

  // 更新连接器
  updateConnector(id, data) {
    return api.put(`/connector/${id}`, data)
  },

  // 删除连接器
  deleteConnector(id) {
    return api.delete(`/connector/${id}`)
  },

  // 心跳检测
  heartbeat(sn) {
    return api.post(`/connector/heartbeat/${sn}`, null, { params: { sn } })
  },

  // 检查连接器状态
  checkStatus() {
    return api.post('/connector/checkStatus')
  },

  // 获取版本列表
  getVersions(id) {
    return api.get(`/connector/${id}/versions`)
  },

  // 上传版本
  uploadVersion(data) {
    return api.post('/connector/version', data)
  },

  // 激活版本
  activateVersion(id) {
    return api.post(`/connector/version/${id}/activate`, null, { params: { id } })
  },

  // 获取操作日志
  getLogs(id) {
    return api.get(`/connector/${id}/logs`)
  },

  // 执行操作
  operate(data) {
    return api.post('/connector/operate', data)
  }
}
