<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="header" :class="{ 'header-scrolled': isScrolled }">
      <div class="header-inner">
        <!-- Logo -->
        <router-link to="/home" class="logo">
          <div class="logo-icon">
            <el-icon :size="28"><ShoppingCart /></el-icon>
          </div>
          <span class="logo-text">潮购商城</span>
        </router-link>

        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索你想要的好物..."
            size="large"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <!-- 右侧操作区 -->
        <div class="header-actions">
          <!-- 管理员入口 - 醒目按钮 -->
          <router-link to="/admin" class="action-item admin-btn" v-if="userStore.isAdmin">
            <el-icon :size="20"><Setting /></el-icon>
            <span class="action-text">后台管理</span>
          </router-link>

          <router-link to="/cart" class="action-item cart-btn">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0">
              <el-icon :size="22"><ShoppingCart /></el-icon>
            </el-badge>
            <span class="action-text">购物车</span>
          </router-link>

          <router-link to="/orders" class="action-item" v-if="userStore.isLoggedIn">
            <el-icon :size="22"><List /></el-icon>
            <span class="action-text">订单</span>
          </router-link>

          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userStore.user?.avatar">
                  {{ userStore.user?.nickname?.charAt(0) }}
                </el-avatar>
                <span class="user-name">{{ userStore.user?.nickname }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin">
                    <el-icon><Setting /></el-icon>后台管理
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <el-icon><List /></el-icon>我的订单
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <template v-else>
            <router-link to="/login" class="action-item login-btn">
              <el-icon :size="20"><User /></el-icon>
              <span class="action-text">登录</span>
            </router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-grid">
          <div class="footer-col">
            <h4>潮购商城</h4>
            <p>品质生活，从这里开始</p>
            <p>让购物成为一种享受</p>
          </div>
          <div class="footer-col">
            <h4>帮助中心</h4>
            <a href="#">购物指南</a>
            <a href="#">支付方式</a>
            <a href="#">配送服务</a>
          </div>
          <div class="footer-col">
            <h4>售后服务</h4>
            <a href="#">退换货政策</a>
            <a href="#">售后保障</a>
            <a href="#">投诉建议</a>
          </div>
          <div class="footer-col">
            <h4>关于我们</h4>
            <a href="#">公司简介</a>
            <a href="#">联系我们</a>
            <a href="#">加入我们</a>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2024 潮购商城 All Rights Reserved. | 仅供学习交流使用</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const isScrolled = ref(false)

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Products', query: { keyword: searchKeyword.value.trim() } })
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'admin':
      router.push('/admin')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
  }
}

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  if (userStore.isLoggedIn) {
    cartStore.fetchCart()
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid transparent;
  transition: var(--transition);

  &.header-scrolled {
    border-bottom-color: var(--border);
    box-shadow: 0 2px 20px rgba(0, 0, 0, 0.06);
  }
}

.header-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;

  .logo-icon {
    width: 42px;
    height: 42px;
    background: var(--gradient-primary);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    transition: var(--transition);
  }

  .logo-text {
    font-size: 22px;
    font-weight: 700;
    background: var(--gradient-primary);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  &:hover .logo-icon {
    transform: rotate(-5deg) scale(1.05);
  }
}

.search-bar {
  flex: 1;
  max-width: 560px;

  :deep(.el-input-group__append) {
    background: linear-gradient(135deg, #0ea5e9, #38bdf8);
    border-color: #0ea5e9;
    color: white;

    .el-button {
      color: white;
      font-weight: 600;
    }
  }

  :deep(.el-input__wrapper) {
    border-radius: 24px 0 0 24px;
    box-shadow: 0 0 0 1px #e0e0e8;
    background: #f8f9fc;

    &:focus-within {
      box-shadow: 0 0 0 2px rgba(14, 165, 233, 0.4);
      background: white;
    }
  }

  :deep(.el-input-group__append) {
    border-radius: 0 24px 24px 0;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  transition: var(--transition);
  cursor: pointer;
  color: var(--text-secondary);

  &:hover {
    background: var(--bg-gray);
    color: var(--primary);
  }

  .action-text {
    font-size: 13px;
    font-weight: 500;
  }
}

.cart-btn {
  position: relative;
}

.login-btn {
  background: var(--gradient-primary);
  color: white !important;
  padding: 8px 20px;
  border-radius: 20px;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 15px rgba(0, 212, 170, 0.3);
    background: var(--gradient-primary);
  }
}

.admin-btn {
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
  color: white !important;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 500;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 15px rgba(14, 165, 233, 0.35);
    background: linear-gradient(135deg, #0ea5e9, #38bdf8);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: var(--transition);

  &:hover {
    background: var(--bg-gray);
  }

  .user-name {
    font-size: 14px;
    font-weight: 500;
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.main-content {
  flex: 1;
  margin-top: 72px;
  min-height: calc(100vh - 72px - 280px);
}

.footer {
  background: var(--gradient-dark);
  color: rgba(255, 255, 255, 0.8);
  margin-top: 60px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px 24px;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  margin-bottom: 32px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }
}

.footer-col {
  h4 {
    color: white;
    font-size: 16px;
    margin-bottom: 16px;
  }

  a, p {
    display: block;
    color: rgba(255, 255, 255, 0.6);
    font-size: 14px;
    margin-bottom: 8px;
    transition: var(--transition);

    &:hover {
      color: white;
    }
  }
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 20px;
  text-align: center;

  p {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.4);
  }
}

@media (max-width: 768px) {
  .header-inner {
    padding: 10px 12px;
    gap: 12px;
  }

  .search-bar {
    max-width: none;
  }

  .action-text {
    display: none;
  }

  .user-name {
    display: none;
  }
}
</style>
