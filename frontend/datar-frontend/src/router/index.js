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
    redirect: '/datasource'
  },
  {
    path: '/datasource',
    name: 'DataSource',
    component: () => import('@/views/DataSource.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/catalog',
    name: 'Catalog',
    component: () => import('@/views/Catalog.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/product',
    name: 'Product',
    component: () => import('@/views/Product.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/order',
    name: 'Order',
    component: () => import('@/views/Order.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/connector',
    name: 'Connector',
    component: () => import('@/views/Connector.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/user/UserList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/role',
    name: 'Role',
    component: () => import('@/views/role/RoleList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/dataspace',
    name: 'DataSpace',
    component: () => import('@/views/DataSpace.vue'),
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
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
