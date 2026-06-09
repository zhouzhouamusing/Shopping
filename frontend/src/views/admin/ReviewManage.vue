<template>
  <div class="review-manage">
    <!-- 筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="statusFilter" @change="fetchReviews">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="PENDING">待审核</el-radio-button>
        <el-radio-button value="APPROVED">已通过</el-radio-button>
        <el-radio-button value="REJECTED">已拒绝</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 评价列表 -->
    <el-table :data="reviews" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="商品" min-width="120">
        <template #default="{ row }">
          <span class="ellipsis">{{ row.productName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="用户" width="100">
        <template #default="{ row }">
          {{ row.username || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="评分" width="140">
        <template #default="{ row }">
          <el-rate :model-value="row.rating" disabled size="small" />
        </template>
      </el-table-column>
      <el-table-column label="内容" min-width="180">
        <template #default="{ row }">
          <span class="ellipsis">{{ row.content || '无文字评价' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="图片" width="70" align="center">
        <template #default="{ row }">
          {{ row.imageList?.length || 0 }}张
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="approve(row)">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="reject(row)">拒绝</el-button>
          <el-button type="primary" size="small" text @click="showReplyDialog(row)">回复</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchReviews"
      />
    </div>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="450px">
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="请输入回复内容" />
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const loading = ref(false)
const reviews = ref([])
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const replyDialogVisible = ref(false)
const replyContent = ref('')
const replyingId = ref(null)
const replying = ref(false)

const fetchReviews = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await api.get('/admin/reviews', { params })
    if (res.code === 200) {
      reviews.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const approve = async (row) => {
  await ElMessageBox.confirm('确定通过该评价？', '审核')
  const res = await api.put(`/admin/reviews/${row.id}/approve`)
  if (res.code === 200) {
    ElMessage.success('已通过')
    fetchReviews()
  }
}

const reject = async (row) => {
  await ElMessageBox.confirm('确定拒绝该评价？', '审核', { type: 'warning' })
  const res = await api.put(`/admin/reviews/${row.id}/reject`)
  if (res.code === 200) {
    ElMessage.success('已拒绝')
    fetchReviews()
  }
}

const showReplyDialog = (row) => {
  replyingId.value = row.id
  replyContent.value = row.adminReply || ''
  replyDialogVisible.value = true
}

const submitReply = async () => {
  replying.value = true
  try {
    const res = await api.put(`/admin/reviews/${replyingId.value}/reply`, { adminReply: replyContent.value })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
      fetchReviews()
    }
  } finally {
    replying.value = false
  }
}

const statusType = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(fetchReviews)
</script>

<style lang="scss" scoped>
.review-manage {
  padding: 20px;
}

.filter-bar {
  margin-bottom: 20px;
}

.ellipsis {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 13px;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
