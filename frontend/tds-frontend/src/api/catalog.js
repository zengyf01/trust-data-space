import api from './index'

export default {
  // 目录分页
  getCatalogPage(params) {
    return api.post('/catalog/page', null, { params })
  },

  // 获取目录详情
  getCatalog(id) {
    return api.get(`/catalog/${id}`)
  },

  // 获取目录字段
  getCatalogFields(id) {
    return api.get(`/catalog/${id}/fields`)
  },

  // 创建目录
  createCatalog(data) {
    return api.post('/catalog', data)
  },

  // 更新目录
  updateCatalog(id, data) {
    return api.put(`/catalog/${id}`, data)
  },

  // 发布目录
  publishCatalog(id) {
    return api.post(`/catalog/${id}/publish`, null, { params: { id } })
  },

  // 下线目录
  offlineCatalog(id) {
    return api.post(`/catalog/${id}/offline`, null, { params: { id } })
  },

  // 删除目录
  deleteCatalog(id) {
    return api.delete(`/catalog/${id}`)
  }
}
