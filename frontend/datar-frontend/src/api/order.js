import api from './index'

export default {
  getPage(params) {
    return api.get('/order/page', { params })
  },
  getById(id) {
    return api.get(`/order/${id}`)
  },
  create(data) {
    return api.post('/order', data)
  },
  updateStatus(id, status) {
    return api.put(`/order/${id}/status`, null, { params: { status } })
  },
  sign(id, contractId) {
    return api.post(`/order/${id}/sign`, null, { params: { contractId } })
  },
  delete(id) {
    return api.delete(`/order/${id}`)
  }
}
