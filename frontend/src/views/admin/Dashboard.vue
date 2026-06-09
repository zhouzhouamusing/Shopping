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
      <h2>欢迎使用潮购商城后台管理系统</h2>
      <p>您可以在这里管理商品、处理订单、查看数据统计。</p>
      <div class="quick-actions">
        <router-link to="/admin/products">
          <el-button type="primary">
            <el-icon><Goods /></el-icon> 管理商品
          </el-button>
        </router-link>
        <router-link to="/admin/orders">
          <el-button>
            <el-icon><List /></el-icon> 管理订单
          </el-button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const stats = ref([
  { title: '商品总数', value: '12', icon: 'Goods', gradient: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { title: '今日订单', value: '0', icon: 'ShoppingBag', gradient: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { title: '注册用户', value: '1', icon: 'User', gradient: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
  { title: '总销售额', value: '¥0', icon: 'Wallet', gradient: 'linear-gradient(135deg, #43e97b, #38f9d7)' },
])
</script>

<style lang="scss" scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
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
