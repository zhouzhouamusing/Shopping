<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <router-link to="/home" class="admin-logo">
          <el-icon :size="24"><ShoppingCart /></el-icon>
          <span>潮购后台</span>
        </router-link>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#0f2744"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#00d4aa"
      >
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/reviews">
          <el-icon><ChatDotRound /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/member-levels">
          <el-icon><Medal /></el-icon>
          <span>会员等级</span>
        </el-menu-item>
        <el-menu-item index="/admin/points-rules">
          <el-icon><Coin /></el-icon>
          <span>积分规则</span>
        </el-menu-item>
        <el-menu-item index="/admin/promotions">
          <el-icon><Present /></el-icon>
          <span>促销活动</span>
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

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titles = {
    '/admin': '数据概览',
    '/admin/products': '商品管理',
    '/admin/orders': '订单管理',
    '/admin/reviews': '评价管理',
    '/admin/users': '用户管理',
    '/admin/member-levels': '会员等级管理',
    '/admin/points-rules': '积分规则管理',
    '/admin/promotions': '促销活动管理'
  }
  return titles[route.path] || '后台管理'
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
