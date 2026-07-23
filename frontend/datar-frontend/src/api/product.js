import api from './index'

export default {
  getPage(params) {
    return api.get('/product/page', { params })
  },
  getById(id) {
    return api.get(`/product/${id}`)
  },
  create(data) {
    return api.post('/product', data)
  },
  update(id, data) {
    return api.put(`/product/${id}`, data)
  },
  delete(id) {
    return api.delete(`/product/${id}`)
  },
  publish(id) {
    return api.post(`/product/${id}/publish`)
  },
  offline(id) {
    return api.post(`/product/${id}/offline`)
  }
}
