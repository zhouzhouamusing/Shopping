<template>
  <div class="review-manage">
    <el-table :data="reviews" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="productName" label="商品" min-width="150" show-overflow-tooltip />
      <el-table-column label="评分" width="100">
        <template #default="{ row }">
          <el-rate :model-value="row.rating" disabled size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="createdAt" label="时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openReply(row)">回复</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchReviews"
      />
    </div>

    <el-dialog v-model="replyVisible" title="回复评价" width="500px">
      <el-input v-model="replyContent" type="textarea" rows="4" placeholder="输入回复内容" />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="handleReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const reviews = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const replyVisible = ref(false)
const replyContent = ref('')
const replying = ref(false)
const currentReviewId = ref(null)

const fetchReviews = async () => {
  loading.value = true
  try {
    const res = await api.get('/merchant/reviews', { params: { page: page.value - 1, size: 10 } })
    if (res.code === 200) {
      reviews.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const openReply = (row) => {
  currentReviewId.value = row.id
  replyContent.value = row.adminReply || ''
  replyVisible.value = true
}

const handleReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replying.value = true
  try {
    const res = await api.put(`/merchant/reviews/${currentReviewId.value}/reply`, { adminReply: replyContent.value })
    if (res.code === 200) {
      ElMessage.success('回复成功')
      replyVisible.value = false
      fetchReviews()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    replying.value = false
  }
}

onMounted(() => {
  fetchReviews()
})
</script>

<style lang="scss" scoped>
.review-manage {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
