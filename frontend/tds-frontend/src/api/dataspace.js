import api from './index'

export default {
  // 数据空间分页
  getDataSpacePage(params) {
    return api.get('/dataspace/page', { params })
  },

  // 获取数据空间详情
  getDataSpace(id) {
    return api.get(`/dataspace/${id}`)
  },

  // 通过编码获取
  getDataSpaceByCode(spaceCode) {
    return api.get(`/dataspace/code/${spaceCode}`)
  },

  // 创建数据空间
  createDataSpace(data) {
    return api.post('/dataspace', data)
  },

  // 更新数据空间
  updateDataSpace(id, data) {
    return api.put(`/dataspace/${id}`, data)
  },

  // 删除数据空间
  deleteDataSpace(id) {
    return api.delete(`/dataspace/${id}`)
  },

  // 审批数据空间
  approveDataSpace(id, status) {
    return api.post(`/dataspace/${id}/approve`, null, { params: { status } })
  },

  // 冻结数据空间
  freezeDataSpace(id, status) {
    return api.post(`/dataspace/${id}/freeze`, null, { params: { status } })
  },

  // 获取用户的数据空间
  getUserDataSpaces(userId) {
    return api.get(`/dataspace/user/${userId}`)
  },

  // ============ 成员管理 ============
  // 成员分页
  getMemberPage(params) {
    return api.get('/dataspace/member/page', { params })
  },

  // 获取成员详情
  getMember(id) {
    return api.get(`/dataspace/member/${id}`)
  },

  // 添加成员
  addMember(data) {
    return api.post('/dataspace/member', data)
  },

  // 更新成员角色
  updateMemberRole(id, role) {
    return api.put(`/dataspace/member/${id}/role`, null, { params: { role } })
  },

  // 审批成员
  approveMember(id, status) {
    return api.post(`/dataspace/member/${id}/approve`, null, { params: { status } })
  },

  // 移除成员
  removeMember(id) {
    return api.delete(`/dataspace/member/${id}`)
  },

  // 获取成员角色
  getMemberRole(spaceId, userId) {
    return api.get('/dataspace/member/role', { params: { spaceId, userId } })
  },

  // ============ 资源管理 ============
  // 资源分页
  getResourcePage(params) {
    return api.get('/dataspace/resource/page', { params })
  },

  // 添加资源
  addResource(data) {
    return api.post('/dataspace/resource', data)
  },

  // 更新资源权限
  updateResourceAccess(id, accessLevel) {
    return api.put(`/dataspace/resource/${id}/access`, null, { params: { accessLevel } })
  },

  // 移除资源
  removeResource(id) {
    return api.delete(`/dataspace/resource/${id}`)
  },

  // 获取用户可访问资源
  getUserResources(userId, resourceType) {
    return api.get(`/dataspace/resource/user/${userId}`, { params: { userId, resourceType } })
  }
}
