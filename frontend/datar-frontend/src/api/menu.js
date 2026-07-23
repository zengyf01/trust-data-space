import api from './index'

export default {
  // 获取菜单树
  getMenuTree() {
    return api.get('/menu/tree')
  }
}
