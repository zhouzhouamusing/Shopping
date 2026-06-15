<template>
  <div class="user-manage">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名/昵称/手机号"
        style="width: 280px"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch" style="margin-left: 12px">搜索</el-button>
    </div>

    <el-table :data="users" v-loading="loading" stripe style="width: 100%">
      <el-table-column label="头像" width="70">
        <template #default="{ row }">
          <el-avatar :size="36" :src="row.avatar" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="170">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="viewDetail(row)">详情</el-button>
          <el-button
            v-if="row.role !== 'ADMIN'"
            text
            :type="row.status === 1 ? 'danger' : 'success'"
            size="small"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '恢复' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchUsers"
      />
    </div>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="650px">
      <div v-loading="detailLoading" class="user-detail">
        <div class="detail-header">
          <el-avatar :size="64" :src="detailData.user?.avatar" />
          <div class="detail-header-info">
            <h3>{{ detailData.user?.nickname || detailData.user?.username }}</h3>
            <el-tag :type="detailData.user?.status === 1 ? 'success' : 'danger'" size="small">
              {{ detailData.user?.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </div>
        </div>
        <el-divider />
        <h4 class="section-title">基本信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ detailData.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailData.user?.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailData.user?.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailData.user?.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="detailData.user?.role === 'ADMIN' ? 'danger' : ''" size="small">
              {{ detailData.user?.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(detailData.user?.createdAt) }}</el-descriptions-item>
        </el-descriptions>
        <h4 class="section-title" style="margin-top: 20px;">会员信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="会员等级">
            <el-tag type="warning" size="small">{{ detailData.level?.name || '未开通' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前积分">
            <span class="points-value">{{ detailData.membership?.totalPoints || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="累计积分">{{ detailData.membership?.totalEarnedPoints || 0 }}</el-descriptions-item>
          <el-descriptions-item label="累计消费">¥{{ detailData.membership?.totalSpending || '0.00' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const users = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = reactive({ user: null, membership: null, level: null })

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 10 }
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/admin/users', { params })
    if (res.code === 200) {
      users.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchUsers()
}

const viewDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await api.get(`/admin/users/${row.id}`)
    if (res.code === 200) {
      detailData.user = res.data.user
      detailData.membership = res.data.membership
      detailData.level = res.data.level
    }
  } finally {
    detailLoading.value = false
  }
}

const toggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '恢复'
  const newStatus = row.status === 1 ? 0 : 1
  ElMessageBox.confirm(`确定${action}用户"${row.username}"？`, '提示', { type: 'warning' })
    .then(async () => {
      const res = await api.put(`/admin/users/${row.id}/status?status=${newStatus}`)
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        fetchUsers()
      } else {
        ElMessage.error(res.message)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchUsers()
})
</script>

<style lang="scss" scoped>
.user-manage {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.user-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 16px;

    .detail-header-info {
      h3 {
        margin: 0 0 8px;
        font-size: 18px;
      }
    }
  }

  .section-title {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 12px;
    color: var(--text-primary);
  }

  .points-value {
    color: var(--primary);
    font-weight: 600;
  }
}
</style>
