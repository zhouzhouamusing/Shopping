<template>
  <div class="dashboard">
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.title">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <el-icon :size="24"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ stat.value }}</h3>
          <p>{{ stat.title }}</p>
        </div>
      </div>
    </div>

    <div class="welcome-card">
      <h2>欢迎来到商家中心</h2>
      <p>您可以在这里管理自己店铺的商品、处理订单、回复评价。</p>
      <div class="quick-actions">
        <router-link to="/merchant/products">
          <el-button type="primary">
            <el-icon><Goods /></el-icon> 管理商品
          </el-button>
        </router-link>
        <router-link to="/merchant/orders">
          <el-button>
            <el-icon><List /></el-icon> 管理订单
          </el-button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/utils/api'

const stats = ref([
  { title: '我的商品', value: '0', icon: 'Goods', gradient: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { title: '总订单数', value: '0', icon: 'ShoppingBag', gradient: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { title: '待发货', value: '0', icon: 'Van', gradient: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
])

const fetchDashboard = async () => {
  const res = await api.get('/merchant/dashboard')
  if (res.code === 200) {
    stats.value[0].value = String(res.data.totalProducts || 0)
    stats.value[1].value = String(res.data.totalOrders || 0)
    stats.value[2].value = String(res.data.pendingOrders || 0)
  }
}

onMounted(() => {
  fetchDashboard()
})
</script>

<style lang="scss" scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow);
  transition: var(--transition);

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-hover);
  }
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  h3 {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
  }

  p {
    font-size: 13px;
    color: var(--text-light);
    margin-top: 4px;
  }
}

.welcome-card {
  background: white;
  border-radius: var(--radius);
  padding: 32px;
  box-shadow: var(--shadow);

  h2 {
    font-size: 20px;
    margin-bottom: 8px;
  }

  p {
    color: var(--text-secondary);
    margin-bottom: 20px;
  }
}

.quick-actions {
  display: flex;
  gap: 12px;
}
</style>
