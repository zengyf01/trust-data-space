import api from './index'

export default {
  // 产品分页
  getProductPage(params) {
    return api.post('/product/page', null, { params })
  },

  // 获取产品详情
  getProduct(id) {
    return api.get(`/product/${id}`)
  },

  // 创建产品
  createProduct(data) {
    return api.post('/product', data)
  },

  // 更新产品
  updateProduct(id, data) {
    return api.put(`/product/${id}`, data)
  },

  // 删除产品
  deleteProduct(id) {
    return api.delete(`/product/${id}`)
  },

  // 提交审核
  submitAudit(id) {
    return api.post(`/product/${id}/submitAudit`, null, { params: { id } })
  },

  // 审批通过
  approveProduct(id) {
    return api.post(`/product/${id}/approve`, null, { params: { id } })
  },

  // 审批拒绝
  rejectProduct(id, reason) {
    return api.post(`/product/${id}/reject`, null, { params: { reason } })
  },

  // 下线产品
  offlineProduct(id) {
    return api.post(`/product/${id}/offline`, null, { params: { id } })
  }
}
