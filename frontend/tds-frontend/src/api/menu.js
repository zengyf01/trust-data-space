import api from './index'

export default {
  // 获取菜单树
  getMenuTree() {
    return api.get('/menu/tree')
  },

  // 获取菜单详情
  getMenu(id) {
    return api.get(`/menu/${id}`)
  },

  // 创建菜单
  createMenu(data) {
    return api.post('/menu', data)
  },

  // 更新菜单
  updateMenu(id, data) {
    return api.put(`/menu/${id}`, data)
  },

  // 删除菜单
  deleteMenu(id) {
    return api.delete(`/menu/${id}`)
  }
}
