<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <router-link to="/home" class="admin-logo">
          <el-icon :size="24"><ShoppingCart /></el-icon>
          <span>{{ headerTitle }}</span>
        </router-link>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#0f2744"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#00d4aa"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <router-link to="/home" class="back-link">
          <el-icon><Back /></el-icon>
          <span>返回前台</span>
        </router-link>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <header class="admin-header">
        <h2>{{ pageTitle }}</h2>
        <div class="admin-user">
          <el-tag v-if="isMerchant" type="warning" size="small" style="margin-right: 8px;">商家</el-tag>
          <el-tag v-if="isAdmin" type="danger" size="small" style="margin-right: 8px;">管理员</el-tag>
          <span>{{ userStore.user?.nickname }}</span>
          <el-button text @click="handleLogout">退出</el-button>
        </div>
      </header>
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() => userStore.user?.role === 'ADMIN')
const isMerchant = computed(() => userStore.user?.role === 'MERCHANT')

const activeMenu = computed(() => route.path)

const menuItems = computed(() => {
  if (isAdmin.value) {
    return [
      { path: '/admin', label: '数据概览', icon: 'DataAnalysis' },
      { path: '/admin/products', label: '商品管理', icon: 'Goods' },
      { path: '/admin/orders', label: '订单管理', icon: 'List' },
      { path: '/admin/reviews', label: '评价管理', icon: 'ChatDotRound' },
      { path: '/admin/users', label: '用户管理', icon: 'User' },
      { path: '/admin/merchants', label: '商家管理', icon: 'Shop' },
      { path: '/admin/member-levels', label: '会员等级', icon: 'Medal' },
      { path: '/admin/points-rules', label: '积分规则', icon: 'Coin' },
      { path: '/admin/promotions', label: '促销活动', icon: 'Present' },
    ]
  } else if (isMerchant.value) {
    return [
      { path: '/merchant', label: '数据概览', icon: 'DataAnalysis' },
      { path: '/merchant/products', label: '我的商品', icon: 'Goods' },
      { path: '/merchant/orders', label: '订单管理', icon: 'List' },
      { path: '/merchant/reviews', label: '评价管理', icon: 'ChatDotRound' },
    ]
  }
  return []
})

const headerTitle = computed(() => {
  if (isAdmin.value) return '潮购后台'
  if (isMerchant.value) return '商家中心'
  return '后台管理'
})

const pageTitle = computed(() => {
  const item = menuItems.value.find(m => m.path === route.path)
  return item?.label || (isAdmin.value ? '后台管理' : '商家中心')
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background: #0f2744;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.admin-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-size: 18px;
  font-weight: 700;
}

.sidebar-footer {
  margin-top: auto;
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);

  .back-link {
    display: flex;
    align-items: center;
    gap: 8px;
    color: rgba(255, 255, 255, 0.5);
    font-size: 14px;
    transition: var(--transition);

    &:hover {
      color: white;
    }
  }
}

.admin-main {
  flex: 1;
  margin-left: 220px;
  background: var(--bg-gray);
}

.admin-header {
  background: white;
  padding: 16px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 50;

  h2 {
    font-size: 18px;
    font-weight: 600;
  }
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--text-secondary);
}

.admin-content {
  padding: 24px 32px;
}
</style>
