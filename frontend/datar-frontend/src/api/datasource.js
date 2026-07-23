import api from './index'

export default {
  getPage(params) {
    return api.get('/datasource/page', { params })
  },
  getById(id) {
    return api.get(`/datasource/${id}`)
  },
  create(data) {
    return api.post('/datasource', data)
  },
  update(id, data) {
    return api.put(`/datasource/${id}`, data)
  },
  delete(id) {
    return api.delete(`/datasource/${id}`)
  },
  test(id) {
    return api.post(`/datasource/${id}/test`)
  },
  enable(id) {
    return api.post(`/datasource/${id}/enable`)
  },
  disable(id) {
    return api.post(`/datasource/${id}/disable`)
  },
  getTables(id) {
    return api.get(`/datasource/${id}/tables`)
  },
  getColumns(id, tableName) {
    return api.get(`/datasource/${id}/columns`, { params: { tableName } })
  }
}
