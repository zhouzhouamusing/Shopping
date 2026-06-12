import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/home',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: '/products', name: 'Products', component: () => import('@/views/Products.vue') },
      { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue') },
      { path: '/cart', name: 'Cart', component: () => import('@/views/Cart.vue') },
      { path: '/orders', name: 'Orders', component: () => import('@/views/Orders.vue') },
      { path: '/order/:orderNo', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue') },
      { path: '/payment/:orderNo', name: 'Payment', component: () => import('@/views/Payment.vue') },
      { path: '/payment-result/:paymentNo', name: 'PaymentResult', component: () => import('@/views/PaymentResult.vue') },
      { path: '/addresses', name: 'Addresses', component: () => import('@/views/Addresses.vue') },
      { path: '/favorites', name: 'Favorites', component: () => import('@/views/Favorites.vue') },
      { path: '/history', name: 'History', component: () => import('@/views/History.vue') },
      { path: '/comparison', name: 'Comparison', component: () => import('@/views/Comparison.vue') },
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'products', name: 'AdminProducts', component: () => import('@/views/admin/ProductManage.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/OrderManage.vue') },
      { path: 'reviews', name: 'AdminReviews', component: () => import('@/views/admin/ReviewManage.vue') },
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

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 需要认证的页面，未登录则跳转登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理员权限
  if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
