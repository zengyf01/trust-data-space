import api from './index'

export default {
  // ============ 认证 ============
  // 登录 - 调用TDS用户中心
  login(username, password) {
    // 通过Nginx代理到TDS用户中心
    return axios.post('/api/tds/organization/uc/login?username=' + username
      + '&password=' + password + '&appId=DATAR').then(res => res.data)
  },

  // 登出
  logout(userId) {
    return api.post('/auth/logout', null, { params: { userId } })
  },

  // ============ 角色管理 ============
  // 角色分页
  getRolePage(params) {
    return api.get('/role/page', { params })
  },

  // 获取角色详情
  getRole(id) {
    return api.get(`/role/${id}`)
  },

  // 创建角色
  createRole(data) {
    return api.post('/role', data)
  },

  // 更新角色
  updateRole(id, data) {
    return api.put(`/role/${id}`, data)
  },

  // 删除角色
  deleteRole(id) {
    return api.delete(`/role/${id}`)
  },

  // 获取所有角色
  getAllRoles() {
    return api.get('/role/all')
  },

  // ============ 用户管理 ============
  // 用户分页
  getUserPage(params) {
    return api.get('/user/page', { params })
  },

  // 获取用户详情
  getUser(id) {
    return api.get(`/user/${id}`)
  },

  // 创建用户
  createUser(data) {
    return api.post('/user', data)
  },

  // 更新用户
  updateUser(id, data) {
    return api.put(`/user/${id}`, data)
  },

  // 删除用户
  deleteUser(id) {
    return api.delete(`/user/${id}`)
  },

  // 分配用户角色
  assignUserRoles(userId, roleIds) {
    return api.post(`/user/${userId}/role`, null, { params: { roleIds } })
  },

  // 获取用户角色
  getUserRoles(userId) {
    return api.get(`/user/${userId}/roles`)
  }
}
