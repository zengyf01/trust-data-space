import api from './index'

export default {
  // 数据源分页
  getDataSourcePage(params) {
    return api.get('/datasource/page', { params })
  },

  // 获取数据源详情
  getDataSource(id) {
    return api.get(`/datasource/${id}`)
  },

  // 创建数据源
  createDataSource(data) {
    return api.post('/datasource', data)
  },

  // 更新数据源
  updateDataSource(id, data) {
    return api.put(`/datasource/${id}`, data)
  },

  // 删除数据源
  deleteDataSource(id) {
    return api.delete(`/datasource/${id}`)
  },

  // 测试连接
  testConnection(id) {
    return api.post(`/datasource/${id}/test`)
  },

  // 启用数据源
  enableDataSource(id) {
    return api.post(`/datasource/${id}/enable`)
  },

  // 禁用数据源
  disableDataSource(id) {
    return api.post(`/datasource/${id}/disable`)
  },

  // 获取数据源表列表
  getTableList(id) {
    return api.get(`/datasource/${id}/tables`)
  },

  // 获取数据源字段列表
  getColumnList(id, tableName) {
    return api.get(`/datasource/${id}/columns`, { params: { tableName } })
  }
}