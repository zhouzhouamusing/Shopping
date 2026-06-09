<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="page-inner" v-if="order">
      <h1 class="page-title">
        <el-button text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        订单详情
      </h1>

      <!-- 订单状态 -->
      <div class="status-card">
        <div class="status-icon" :class="'status-' + order.status">
          <el-icon :size="32">
            <component :is="statusIcons[order.status]" />
          </el-icon>
        </div>
        <div class="status-info">
          <h3>{{ getStatusText(order.status) }}</h3>
          <p>订单号：{{ order.orderNo }}</p>
          <p>创建时间：{{ formatTime(order.createdAt) }}</p>
        </div>
        <div class="status-actions">
          <el-button v-if="order.status === 0" type="primary" @click="payOrder">立即付款</el-button>
          <el-button v-if="order.status === 0" @click="cancelOrder">取消订单</el-button>
        </div>
      </div>

      <!-- 收货信息 -->
      <div class="info-card">
        <h3>收货信息</h3>
        <div class="info-grid">
          <div><span class="label">收货人：</span>{{ order.receiverName }}</div>
          <div><span class="label">联系电话：</span>{{ order.receiverPhone }}</div>
          <div><span class="label">收货地址：</span>{{ order.receiverAddress }}</div>
          <div v-if="order.remark"><span class="label">备注：</span>{{ order.remark }}</div>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="info-card">
        <h3>商品信息</h3>
        <div class="items-list">
          <div v-for="item in order.orderItems" :key="item.id" class="item-row">
            <img :src="item.productImage" :alt="item.productName" />
            <div class="item-info">
              <h4>{{ item.productName }}</h4>
              <p>¥{{ item.price }} × {{ item.quantity }}</p>
            </div>
            <div class="item-total">¥{{ item.totalPrice }}</div>
          </div>
        </div>
        <div class="total-row">
          <span>订单总额：</span>
          <span class="total-amount">¥{{ order.totalAmount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(true)

const statusIcons = {
  0: 'Clock',
  1: 'CreditCard',
  2: 'Van',
  3: 'CircleCheck',
  4: 'CircleClose'
}

const getStatusText = (status) => {
  const map = { 0: '等待付款', 1: '已付款，待发货', 2: '已发货', 3: '交易完成', 4: '已取消' }
  return map[status] || '未知状态'
}

const formatTime = (time) => time ? new Date(time).toLocaleString('zh-CN') : ''

const fetchOrder = async () => {
  loading.value = true
  try {
    const res = await api.get(`/orders/${route.params.orderNo}`)
    if (res.code === 200) {
      order.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const payOrder = async () => {
  const res = await api.put(`/orders/${order.value.orderNo}/pay`)
  if (res.code === 200) {
    ElMessage.success('支付成功！')
    fetchOrder()
  }
}

const cancelOrder = () => {
  ElMessageBox.confirm('确定取消？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await api.put(`/orders/${order.value.orderNo}/cancel`)
      if (res.code === 200) {
        ElMessage.success('已取消')
        fetchOrder()
      }
    })
    .catch(() => {})
}

onMounted(fetchOrder)
</script>

<style lang="scss" scoped>
.order-detail-page {
  padding: 32px 0;
}

.page-inner {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.status-card {
  background: white;
  border-radius: var(--radius);
  padding: 32px;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 16px;

  @media (max-width: 768px) {
    flex-direction: column;
    text-align: center;
  }
}

.status-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;

  &.status-0 { background: #faad14; }
  &.status-1 { background: #1890ff; }
  &.status-2 { background: #722ed1; }
  &.status-3 { background: #52c41a; }
  &.status-4 { background: #999; }
}

.status-info {
  flex: 1;

  h3 { font-size: 18px; margin-bottom: 4px; }
  p { font-size: 13px; color: var(--text-light); }
}

.info-card {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
  margin-bottom: 16px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border);
  }
}

.info-grid {
  display: grid;
  gap: 10px;
  font-size: 14px;

  .label { color: var(--text-light); }
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);

  &:last-child { border-bottom: none; }

  img {
    width: 64px;
    height: 64px;
    border-radius: var(--radius-sm);
    object-fit: cover;
  }

  .item-info {
    flex: 1;

    h4 { font-size: 14px; margin-bottom: 4px; }
    p { font-size: 13px; color: var(--text-light); }
  }

  .item-total {
    font-weight: 600;
    color: var(--primary);
  }
}

.total-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  padding-top: 16px;
  margin-top: 12px;
  border-top: 1px solid var(--border);
  font-size: 14px;
}

.total-amount {
  font-size: 22px;
  font-weight: 800;
  color: var(--primary);
}
</style>
