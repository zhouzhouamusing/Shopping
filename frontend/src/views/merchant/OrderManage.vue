<template>
  <div class="order-manage">
    <div class="toolbar">
      <el-select v-model="statusFilter" placeholder="订单状态" clearable @change="fetchOrders" style="width: 150px;">
        <el-option label="全部" :value="null" />
        <el-option label="待发货" :value="1" />
        <el-option label="待收货" :value="2" />
        <el-option label="已完成" :value="4" />
      </el-select>
    </div>

    <el-table :data="orders" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="orderNo" label="订单编号" width="200" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">
          <span style="font-weight: 600;">¥{{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="createdAt" label="下单时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1"
            text type="primary" size="small"
            @click="handleDeliver(row)"
          >发货</el-button>
        </template>
      </el-table-column>
    </el-table>

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

const statusLabel = (s) => {
  const map = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '待评价', 4: '已完成', 5: '已取消', 6: '已退款' }
  return map[s] || '未知'
}

const statusType = (s) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'success', 5: 'info', 6: 'danger' }
  return map[s] || 'info'
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 10 }
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await api.get('/merchant/orders', { params })
    if (res.code === 200) {
      orders.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const handleDeliver = (row) => {
  ElMessageBox.confirm(`确认对订单 ${row.orderNo} 进行发货？`, '确认发货', { type: 'info' })
    .then(async () => {
      const res = await api.put(`/merchant/orders/${row.orderNo}/deliver`)
      if (res.code === 200) {
        ElMessage.success('发货成功')
        fetchOrders()
      } else {
        ElMessage.error(res.message)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchOrders()
})
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
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
