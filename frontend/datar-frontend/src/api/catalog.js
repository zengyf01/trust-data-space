import api from './index'

export default {
  getPage(params) {
    return api.get('/catalog/page', { params })
  },
  getById(id) {
    return api.get(`/catalog/${id}`)
  },
  create(data) {
    return api.post('/catalog', data)
  },
  update(id, data) {
    return api.put(`/catalog/${id}`, data)
  },
  delete(id) {
    return api.delete(`/catalog/${id}`)
  },
  publish(id) {
    return api.post(`/catalog/${id}/publish`)
  },
  offline(id) {
    return api.post(`/catalog/${id}/offline`)
  }
}
