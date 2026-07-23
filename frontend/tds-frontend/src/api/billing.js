import api from './index'

export default {
  // ============ 计费模板 ============
  // 模板分页
  getTemplatePage(params) {
    return api.get('/billing/template/page', { params })
  },

  // 获取模板详情
  getTemplate(id) {
    return api.get(`/billing/template/${id}`)
  },

  // 创建模板
  createTemplate(data) {
    return api.post('/billing/template', data)
  },

  // 更新模板
  updateTemplate(id, data) {
    return api.put(`/billing/template/${id}`, data)
  },

  // 删除模板
  deleteTemplate(id) {
    return api.delete(`/billing/template/${id}`)
  },

  // ============ 产品定价 ============
  // 定价分页
  getPricingPage(params) {
    return api.get('/billing/pricing/page', { params })
  },

  // 获取定价详情
  getPricing(id) {
    return api.get(`/billing/pricing/${id}`)
  },

  // 获取产品当前定价
  getProductPricing(productId) {
    return api.get(`/billing/pricing/product/${productId}`)
  },

  // 创建定价
  createPricing(data) {
    return api.post('/billing/pricing', data)
  },

  // 更新定价
  updatePricing(id, data) {
    return api.put(`/billing/pricing/${id}`, data)
  },

  // 删除定价
  deletePricing(id) {
    return api.delete(`/billing/pricing/${id}`)
  },

  // ============ 用量记录 ============
  // 用量分页
  getUsagePage(params) {
    return api.get('/billing/usage/page', { params })
  },

  // 记录用量
  createUsage(data) {
    return api.post('/billing/usage', data)
  },

  // 用量汇总
  getUsageSummary(tenantId, billingPeriod) {
    return api.get('/billing/usage/summary', { params: { tenantId, billingPeriod } })
  },

  // ============ 账单 ============
  // 账单分页
  getBillPage(params) {
    return api.get('/billing/bill/page', { params })
  },

  // 获取账单详情
  getBill(id) {
    return api.get(`/billing/bill/${id}`)
  },

  // 生成账单
  generateBill(tenantId, billingPeriod) {
    return api.post('/billing/bill/generate', null, { params: { tenantId, billingPeriod } })
  },

  // 确认账单
  confirmBill(id) {
    return api.post(`/billing/bill/${id}/confirm`, null, { params: { id } })
  },

  // 支付账单
  payBill(id, paymentMethod, amount) {
    return api.post(`/billing/bill/${id}/pay`, null, { params: { id, paymentMethod, amount } })
  },

  // 删除账单
  deleteBill(id) {
    return api.delete(`/billing/bill/${id}`)
  },

  // ============ 费用计算 ============
  // 费用计算
  calculate(productId, usageCount, billingModel, unitPrice) {
    return api.get('/billing/calculate', {
      params: { productId, usageCount, billingModel, unitPrice }
    })
  }
}
