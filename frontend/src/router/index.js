import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'products', name: 'Products', component: () => import('@/views/Products.vue') },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('@/views/Cart.vue'), meta: { requiresAuth: true } },
      { path: 'orders', name: 'Orders', component: () => import('@/views/Orders.vue'), meta: { requiresAuth: true } },
      { path: 'order/:orderNo', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue'), meta: { requiresAuth: true } },
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'products', name: 'AdminProducts', component: () => import('@/views/admin/ProductManage.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/OrderManage.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
