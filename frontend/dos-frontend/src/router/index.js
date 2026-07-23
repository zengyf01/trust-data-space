import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/workorder'
  },
  // DOS 交付管理
  {
    path: '/workorder',
    name: 'WorkOrder',
    component: () => import('../views/WorkOrder.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/sandbox',
    name: 'Sandbox',
    component: () => import('../views/Sandbox.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/privacy',
    name: 'PrivacyCompute',
    component: () => import('../views/PrivacyCompute.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/privacy/create',
    name: 'PrivacyComputeCreate',
    component: () => import('../views/PrivacyComputeCreate.vue'),
    meta: { requiresAuth: true }
  },
  // 数据服务
  {
    path: '/strategy',
    name: 'Strategy',
    component: () => import('../views/Strategy.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/datasources',
    name: 'DataSourceList',
    component: () => import('../views/DataSourceList.vue'),
    meta: { requiresAuth: true }
  },
  // 系统管理
  {
    path: '/user',
    name: 'User',
    component: () => import('../views/User.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/node',
    name: 'NodeManage',
    component: () => import('../views/NodeManage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/task',
    name: 'TaskManage',
    component: () => import('../views/TaskManage.vue'),
    meta: { requiresAuth: true }
  },
  // MSP 旧路由（保留兼容）
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tasks',
    name: 'TaskList',
    component: () => import('../views/TaskList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tasks/create',
    name: 'TaskCreate',
    component: () => import('../views/TaskCreate.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/nodes',
    name: 'NodeList',
    component: () => import('../views/NodeList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/auth',
    name: 'Auth',
    component: () => import('../views/Auth.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('dos_token')
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    // 需要登录但没有token，重定向到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录访问登录页，跳转到首页
    next('/workorder')
  } else {
    next()
  }
})

export default router
