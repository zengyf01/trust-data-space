import api from './index'

export default {
  getPage(params) {
    return api.get('/connector/page', { params })
  },
  getById(id) {
    return api.get(`/connector/${id}`)
  },
  getBySn(sn) {
    return api.get(`/connector/sn/${sn}`)
  },
  create(data) {
    return api.post('/connector', data)
  },
  update(id, data) {
    return api.put(`/connector/${id}`, data)
  },
  delete(id) {
    return api.delete(`/connector/${id}`)
  },
  heartbeat(sn) {
    return api.post(`/connector/heartbeat/${sn}`)
  },
  checkStatus() {
    return api.post('/connector/checkStatus')
  },
  getVersions(connectorId) {
    return api.get(`/connector/${connectorId}/versions`)
  },
  uploadVersion(data) {
    return api.post('/connector/version', data)
  },
  activateVersion(versionId) {
    return api.post(`/connector/version/${versionId}/activate`)
  },
  getLogs(connectorId) {
    return api.get(`/connector/${connectorId}/logs`)
  },
  operate(data) {
    return api.post('/connector/operate', data)
  },
  getAll() {
    return api.get('/connector/all')
  }
}
