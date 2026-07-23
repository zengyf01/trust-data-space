import api from './index'

export default {
  // 存证分页
  getEvidencePage(params) {
    return api.get('/evidence/page', { params })
  },

  // 获取存证详情
  getEvidence(id) {
    return api.get(`/evidence/${id}`)
  },

  // 创建存证
  createEvidence(data) {
    return api.post('/evidence', data)
  },

  // 验证存证
  verifyEvidence(txHash) {
    return api.get('/evidence/verify', { params: { txHash } })
  },

  // 数据消费分页
  getConsumePage(params) {
    return api.get('/evidence/consume/page', { params })
  },

  // 记录数据消费
  createConsume(data) {
    return api.post('/evidence/consume', data)
  },

  // 操作日志分页
  getOperationLogPage(params) {
    return api.get('/evidence/operationLog/page', { params })
  }
}
