<template>
  <div class="order-manage">
    <!-- 筛选 -->
    <div class="toolbar">
      <el-radio-group v-model="statusFilter" @change="fetchOrders">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待付款</el-radio-button>
        <el-radio-button :value="1">待发货</el-radio-button>
        <el-radio-button :value="2">待收货</el-radio-button>
        <el-radio-button :value="3">待评价</el-radio-button>
        <el-radio-button :value="4">已完成</el-radio-button>
        <el-radio-button :value="5">已取消</el-radio-button>
        <el-radio-button :value="6">已退款</el-radio-button>
        <el-radio-button :value="7">已过期</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 订单列表 -->
    <el-table :data="orders" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">
          <span style="color: var(--primary); font-weight: 600;">¥{{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="100">
        <template #default="{ row }">
          {{ getMethodName(row.paymentMethod) }}
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="receiverPhone" label="电话" width="120" />
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1"
            text
            type="primary"
            size="small"
            @click="handleDeliver(row)"
          >发货</el-button>
          <el-button
            v-if="row.status === 1 || row.status === 2"
            text
            type="warning"
            size="small"
            @click="handleRefund(row)"
          >退款</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const orders = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const statusFilter = ref(null)

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '待发货', type: 'primary' },
  2: { text: '待收货', type: '' },
  3: { text: '待评价', type: 'warning' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已取消', type: 'info' },
  6: { text: '已退款', type: 'danger' },
  7: { text: '已过期', type: 'info' }
}

const getStatusText = (s) => statusMap[s]?.text || '未知'
const getStatusType = (s) => statusMap[s]?.type || ''
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''
const getMethodName = (method) => {
  if (!method) return '-'
  const map = { ALIPAY: '支付宝', WECHAT: '微信支付', BANK_CARD: '银行卡', COD: '货到付款' }
  return map[method] || method
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 10 }
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await api.get('/admin/orders', { params })
    if (res.code === 200) {
      orders.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const handleDeliver = (row) => {
  ElMessageBox.confirm(`确认对订单 ${row.orderNo} 发货？`, '发货确认', { type: 'info' })
    .then(async () => {
      const res = await api.put(`/admin/orders/${row.orderNo}/deliver`)
      if (res.code === 200) {
        ElMessage.success('发货成功')
        fetchOrders()
      }
    })
    .catch(() => {})
}

const handleRefund = (row) => {
  ElMessageBox.confirm(`确认对订单 ${row.orderNo} 退款？库存将恢复。`, '退款确认', { type: 'warning' })
    .then(async () => {
      const res = await api.put(`/admin/orders/${row.orderNo}/refund`)
      if (res.code === 200) {
        ElMessage.success('退款成功')
        fetchOrders()
      }
    })
    .catch(() => {})
}

onMounted(fetchOrders)
</script>

<style lang="scss" scoped>
.order-manage {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
}

.toolbar {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
