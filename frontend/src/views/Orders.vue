<template>
  <div class="orders-page">
    <div class="page-inner">
      <h1 class="page-title">
        <el-icon><List /></el-icon>
        我的订单
      </h1>

      <div v-loading="loading">
        <div v-if="orders.length > 0" class="orders-list">
          <div
            v-for="order in orders"
            :key="order.id"
            class="order-card"
            @click="goDetail(order.orderNo)"
          >
            <div class="order-header">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <el-tag :type="getStatusType(order.status)" size="small">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
            <div class="order-body">
              <div class="order-items">
                <div
                  v-for="item in order.orderItems?.slice(0, 3)"
                  :key="item.id"
                  class="order-product"
                >
                  <img :src="item.productImage" :alt="item.productName" />
                </div>
                <div class="more-items" v-if="order.orderItems?.length > 3">
                  +{{ order.orderItems.length - 3 }}
                </div>
              </div>
              <div class="order-summary">
                <div class="order-total">
                  <span>¥{{ order.totalAmount }}</span>
                </div>
                <div class="order-time">{{ formatTime(order.createdAt) }}</div>
              </div>
            </div>
            <div class="order-actions">
              <el-button
                v-if="order.status === 0"
                type="primary"
                size="small"
                @click.stop="payOrder(order.orderNo)"
              >立即付款</el-button>
              <el-button
                v-if="order.status === 0"
                size="small"
                @click.stop="cancelOrder(order.orderNo)"
              >取消订单</el-button>
              <el-button
                size="small"
                @click.stop="goDetail(order.orderNo)"
              >查看详情</el-button>
            </div>
          </div>
        </div>

        <div class="empty-state" v-else-if="!loading">
          <el-icon class="empty-icon" :size="64"><Document /></el-icon>
          <p>暂无订单</p>
          <router-link to="/products">
            <el-button type="primary">去购物</el-button>
          </router-link>
        </div>
      </div>

      <div class="pagination" v-if="total > 10">
        <el-pagination
          v-model:current-page="page"
          :page-size="10"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchOrders"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const orders = ref([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '已付款', type: 'primary' },
  2: { text: '已发货', type: '' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || ''

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await api.get('/orders', { params: { page: page.value - 1, size: 10 } })
    if (res.code === 200) {
      orders.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const goDetail = (orderNo) => {
  router.push({ name: 'OrderDetail', params: { orderNo } })
}

const payOrder = async (orderNo) => {
  const res = await api.put(`/orders/${orderNo}/pay`)
  if (res.code === 200) {
    ElMessage.success('支付成功！')
    fetchOrders()
  }
}

const cancelOrder = (orderNo) => {
  ElMessageBox.confirm('确定要取消订单吗？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await api.put(`/orders/${orderNo}/cancel`)
      if (res.code === 200) {
        ElMessage.success('订单已取消')
        fetchOrders()
      }
    })
    .catch(() => {})
}

onMounted(fetchOrders)
</script>

<style lang="scss" scoped>
.orders-page {
  padding: 32px 0;
}

.page-inner {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: white;
  border-radius: var(--radius);
  padding: 20px 24px;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    box-shadow: var(--shadow-hover);
    transform: translateY(-2px);
  }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .order-no {
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.order-items {
  display: flex;
  gap: 8px;
  align-items: center;

  img {
    width: 60px;
    height: 60px;
    border-radius: var(--radius-sm);
    object-fit: cover;
  }

  .more-items {
    width: 60px;
    height: 60px;
    border-radius: var(--radius-sm);
    background: var(--bg-gray);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    color: var(--text-light);
  }
}

.order-summary {
  text-align: right;
}

.order-total {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary);
}

.order-time {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 4px;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
