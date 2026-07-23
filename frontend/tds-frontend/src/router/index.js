import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/contract'
  },
  {
    path: '/contract',
    name: 'ContractList',
    component: () => import('@/views/contract/ContractList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/order',
    name: 'OrderList',
    component: () => import('@/views/order/OrderList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/dataspace',
    name: 'DataSpaceList',
    component: () => import('@/views/dataspace/DataSpaceList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/dataspace/member',
    name: 'MemberList',
    component: () => import('@/views/dataspace/MemberList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/organization',
    name: 'OrganizationList',
    component: () => import('@/views/organization/OrganizationList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user',
    name: 'UserList',
    component: () => import('@/views/user/UserList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/role',
    name: 'RoleList',
    component: () => import('@/views/role/RoleList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/datasource',
    name: 'DataSourceList',
    component: () => import('@/views/datasource/DataSourceList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/catalog',
    name: 'CatalogList',
    component: () => import('@/views/catalog/CatalogList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/product',
    name: 'ProductList',
    component: () => import('@/views/product/ProductList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/connector',
    name: 'ConnectorList',
    component: () => import('@/views/connector/ConnectorList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/policy',
    name: 'PolicyList',
    component: () => import('@/views/policy/PolicyList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/evidence',
    name: 'EvidenceList',
    component: () => import('@/views/evidence/EvidenceList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/billing',
    name: 'BillingList',
    component: () => import('@/views/billing/BillingList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/system',
    name: 'SystemConfigList',
    component: () => import('@/views/system/SystemConfigList.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    // 需要登录但没有token，重定向到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录访问登录页，跳转到首页
    next('/')
  } else {
    next()
  }
})

export default router