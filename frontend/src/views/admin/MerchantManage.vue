<template>
  <div class="merchant-manage">
    <div class="toolbar">
      <el-select v-model="statusFilter" placeholder="申请状态" clearable @change="fetchApplications" style="width: 150px;">
        <el-option label="全部" value="" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
    </div>

    <el-table :data="applications" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="shopName" label="店铺名称" min-width="150" />
      <el-table-column prop="contactName" label="联系人" width="100" />
      <el-table-column prop="contactPhone" label="联系电话" width="130" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button text type="success" size="small" @click="handleReview(row, true)">通过</el-button>
            <el-button text type="danger" size="small" @click="handleReview(row, false)">拒绝</el-button>
          </template>
          <span v-else style="color: var(--text-light); font-size: 12px;">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchApplications"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const applications = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const statusFilter = ref('')

const statusLabel = (s) => {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[s] || s
}

const statusType = (s) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[s] || 'info'
}

const fetchApplications = async () => {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 10 }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await api.get('/admin/merchant-applications', { params })
    if (res.code === 200) {
      applications.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const handleReview = async (row, approved) => {
  if (approved) {
    ElMessageBox.confirm(`确认通过"${row.shopName}"的入驻申请？`, '审批确认', { type: 'success' })
      .then(async () => {
        const res = await api.put(`/admin/merchant-applications/${row.id}/review`, { approved: true })
        if (res.code === 200) {
          ElMessage.success('已通过')
          fetchApplications()
        } else {
          ElMessage.error(res.message)
        }
      })
      .catch(() => {})
  } else {
    ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
      confirmButtonText: '确认拒绝',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async ({ value }) => {
        const res = await api.put(`/admin/merchant-applications/${row.id}/review`, {
          approved: false,
          rejectReason: value || '不符合入驻条件'
        })
        if (res.code === 200) {
          ElMessage.success('已拒绝')
          fetchApplications()
        } else {
          ElMessage.error(res.message)
        }
      })
      .catch(() => {})
  }
}

onMounted(() => {
  fetchApplications()
})
</script>

<style lang="scss" scoped>
.merchant-manage {
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
