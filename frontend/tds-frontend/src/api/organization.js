import api from './index'

export default {
  // ============ 机构管理 ============
  // 机构分页
  getOrgPage(params) {
    return api.get('/organization/page', { params })
  },

  // 获取机构详情
  getOrg(id) {
    return api.get(`/organization/${id}`)
  },

  // 通过编码获取
  getOrgByCode(orgCode) {
    return api.get(`/organization/code/${orgCode}`)
  },

  // 创建机构
  createOrg(data) {
    return api.post('/organization', data)
  },

  // 更新机构
  updateOrg(id, data) {
    return api.put(`/organization/${id}`, data)
  },

  // 删除机构
  deleteOrg(id) {
    return api.delete(`/organization/${id}`)
  },

  // 批量删除机构
  batchDeleteOrg(ids) {
    return api.delete('/organization/batch', { data: ids })
  },

  // 审批机构
  approveOrg(id, status) {
    return api.post(`/organization/${id}/approve`, null, { params: { status } })
  },

  // 冻结机构
  freezeOrg(id, status) {
    return api.post(`/organization/${id}/freeze`, null, { params: { status } })
  },

  // ============ 部门管理 ============
  // 部门分页
  getDeptPage(params) {
    return api.get('/organization/dept/page', { params })
  },

  // 获取部门详情
  getDept(id) {
    return api.get(`/organization/dept/${id}`)
  },

  // 获取机构部门
  getOrgDepts(orgId) {
    return api.get(`/organization/dept/org/${orgId}`)
  },

  // 创建部门
  createDept(data) {
    return api.post('/organization/dept', data)
  },

  // 更新部门
  updateDept(id, data) {
    return api.put(`/organization/dept/${id}`, data)
  },

  // 删除部门
  deleteDept(id) {
    return api.delete(`/organization/dept/${id}`)
  },

  // ============ 角色管理 ============
  // 角色分页
  getRolePage(params) {
    return api.get('/organization/role/page', { params })
  },

  // 获取角色详情
  getRole(id) {
    return api.get(`/organization/role/${id}`)
  },

  // 创建角色
  createRole(data) {
    return api.post('/organization/role', data)
  },

  // 更新角色
  updateRole(id, data) {
    return api.put(`/organization/role/${id}`, data)
  },

  // 删除角色
  deleteRole(id) {
    return api.delete(`/organization/role/${id}`)
  },

  // ============ 用户管理 ============
  // 用户分页
  getUserPage(params) {
    return api.get('/organization/user/page', { params })
  },

  // 获取用户详情
  getUser(id) {
    return api.get(`/organization/user/${id}`)
  },

  // 通过用户名获取
  getUserByUsername(username) {
    return api.get(`/organization/user/username/${username}`)
  },

  // 创建用户
  createUser(data) {
    return api.post('/organization/user', data)
  },

  // 更新用户
  updateUser(id, data) {
    return api.put(`/organization/user/${id}`, data)
  },

  // 删除用户
  deleteUser(id) {
    return api.delete(`/organization/user/${id}`)
  },

  // 分配用户角色
  assignUserRoles(userId, roleIds) {
    return api.post(`/organization/user/${userId}/role`, null, { params: { roleIds } })
  },

  // 获取用户角色
  getUserRoles(userId) {
    return api.get(`/organization/user/${userId}/roles`)
  },

  // ============ 认证 ============
  // 登录
  login(username, password) {
    return api.post('/organization/login', null, {
      params: { username, password }
    })
  },

  // SSO登录
  ssoLogin(token) {
    return api.post('/organization/sso/login', null, { params: { token } })
  },

  // SSO回调
  ssoCallback(code, state) {
    return api.get('/organization/sso/callback', { params: { code, state } })
  },

  // 登出
  logout(userId) {
    return api.post('/organization/logout', null, { params: { userId } })
  },

  // 刷新Token
  refresh(refreshToken) {
    return api.post('/organization/refresh', null, { params: { refreshToken } })
  }
}
