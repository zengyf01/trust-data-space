import api from './index'

export default {
  // 订单分页
  getOrderPage(params) {
    return api.post('/order/page', null, { params })
  },

  // 获取订单详情
  getOrder(id) {
    return api.get(`/order/${id}`)
  },

  // 通过订单号获取
  getOrderByCode(orderCode) {
    return api.get(`/order/code/${orderCode}`)
  },

  // 创建订单
  createOrder(data) {
    return api.post('/order', data)
  },

  // 审批通过
  approveOrder(id, approver, remark) {
    return api.post(`/order/${id}/approve`, null, {
      params: { approver, remark }
    })
  },

  // 审批拒绝
  rejectOrder(id, reason) {
    return api.post(`/order/${id}/reject`, null, {
      params: { reason }
    })
  },

  // 取消订单
  cancelOrder(id) {
    return api.post(`/order/${id}/cancel`, null, {
      params: { id }
    })
  },

  // 获取订单历史
  getOrderHistory(id) {
    return api.get(`/order/${id}/history`)
  },

  // 更新交付API信息
  updateDeliveryApi(id, deliveryApiInfo) {
    return api.put(`/order/${id}/deliveryApi`, deliveryApiInfo)
  }
}
