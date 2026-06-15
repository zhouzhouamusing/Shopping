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
      { path: '/merchant-apply', name: 'MerchantApply', component: () => import('@/views/MerchantApply.vue') },
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
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'merchants', name: 'AdminMerchants', component: () => import('@/views/admin/MerchantManage.vue') },
      { path: 'member-levels', name: 'AdminMemberLevels', component: () => import('@/views/admin/MemberLevelManage.vue') },
      { path: 'points-rules', name: 'AdminPointsRules', component: () => import('@/views/admin/PointsRuleManage.vue') },
      { path: 'promotions', name: 'AdminPromotions', component: () => import('@/views/admin/PromotionManage.vue') },
    ]
  },
  {
    path: '/merchant',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresMerchant: true },
    children: [
      { path: '', name: 'MerchantDashboard', component: () => import('@/views/merchant/Dashboard.vue') },
      { path: 'products', name: 'MerchantProducts', component: () => import('@/views/merchant/ProductManage.vue') },
      { path: 'orders', name: 'MerchantOrders', component: () => import('@/views/merchant/OrderManage.vue') },
      { path: 'reviews', name: 'MerchantReviews', component: () => import('@/views/merchant/ReviewManage.vue') },
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

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
    next({ name: 'Home' })
    return
  }

  if (to.meta.requiresMerchant && userStore.user?.role !== 'MERCHANT') {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
