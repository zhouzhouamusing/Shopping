<template>
  <div class="order-manage">
    <!-- 筛选 -->
    <div class="toolbar">
      <el-radio-group v-model="statusFilter" @change="fetchOrders">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="0">待付款</el-radio-button>
        <el-radio-button :value="1">已付款</el-radio-button>
        <el-radio-button :value="2">已发货</el-radio-button>
        <el-radio-button :value="3">已完成</el-radio-button>
        <el-radio-button :value="4">已取消</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 订单列表 -->
    <el-table :data="orders" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">
          <span style="color: #ff4d4f; font-weight: 600;">¥{{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="receiverPhone" label="电话" width="120" />
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1"
            text
            type="primary"
            size="small"
            @click="handleDeliver(row)"
          >发货</el-button>
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
  1: { text: '已付款', type: 'primary' },
  2: { text: '已发货', type: '' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' }
}

const getStatusText = (s) => statusMap[s]?.text || '未知'
const getStatusType = (s) => statusMap[s]?.type || ''
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''

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
