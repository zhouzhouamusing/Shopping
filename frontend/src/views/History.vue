<template>
  <div class="history-page">
    <div class="page-inner">
      <div class="page-header">
        <div class="header-left">
          <el-button class="back-btn" text @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <h2 class="page-title">浏览历史</h2>
          <span class="count-badge" v-if="total > 0">{{ total }} 条记录</span>
        </div>
        <el-button
          v-if="histories.length > 0"
          type="danger"
          plain
          size="small"
          @click="clearAll"
        >
          清空历史
        </el-button>
      </div>

      <div class="history-list" v-loading="loading">
        <div
          v-for="(item, index) in histories"
          :key="item.id"
          class="history-item animate-fadeInUp"
          :style="{ animationDelay: `${index * 0.03}s` }"
        >
          <div class="item-image" @click="goDetail(item.product?.id)">
            <img :src="item.product?.mainImage" :alt="item.product?.name" />
          </div>
          <div class="item-info" @click="goDetail(item.product?.id)">
            <h3 class="item-name">{{ item.product?.name }}</h3>
            <p class="item-desc">{{ item.product?.description }}</p>
            <div class="item-price">
              <span class="price"><span class="symbol">¥</span>{{ item.product?.price }}</span>
            </div>
          </div>
          <div class="item-meta">
            <span class="browse-time">{{ formatTime(item.browsedAt) }}</span>
            <el-button
              type="danger"
              size="small"
              text
              @click="deleteHistory(item.productId)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && histories.length === 0">
        <el-icon class="empty-icon"><Clock /></el-icon>
        <p>暂无浏览记录</p>
        <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchHistory"
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

const histories = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await api.get('/history', { params: { page: page.value - 1, size: pageSize } })
    if (res.code === 200) {
      histories.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const deleteHistory = async (productId) => {
  try {
    const res = await api.delete(`/history/${productId}`)
    if (res.code === 200) {
      ElMessage.success('已删除')
      fetchHistory()
    }
  } catch (e) {
    // handled by interceptor
  }
}

const clearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有浏览历史吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.delete('/history')
    if (res.code === 200) {
      ElMessage.success('已清空浏览历史')
      histories.value = []
      total.value = 0
    }
  } catch (e) {
    // user cancelled or error handled by interceptor
  }
}

const goDetail = (id) => {
  if (id) router.push({ name: 'ProductDetail', params: { id } })
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchHistory()
})
</script>

<style lang="scss" scoped>
.history-page {
  padding: 24px 0;
}

.page-inner {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .back-btn {
    font-size: 14px;
    color: var(--text-secondary);
    &:hover { color: var(--primary); }
  }

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }

  .count-badge {
    font-size: 13px;
    color: var(--text-light);
    background: var(--bg-gray);
    padding: 2px 10px;
    border-radius: 12px;
  }
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: var(--radius);
  padding: 16px;
  box-shadow: var(--shadow);
  transition: var(--transition);

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

.item-image {
  flex-shrink: 0;
  cursor: pointer;

  img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 8px;
  }
}

.item-info {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.item-name {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &:hover { color: var(--primary); }
}

.item-desc {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-price {
  .price {
    color: var(--primary);
    font-size: 16px;
    font-weight: 700;
  }
  .symbol {
    font-size: 12px;
  }
}

.item-meta {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.browse-time {
  font-size: 12px;
  color: var(--text-light);
}

.empty-state {
  text-align: center;
  padding: 80px 0;

  .empty-icon {
    font-size: 64px;
    color: var(--text-light);
  }

  p {
    margin: 16px 0;
    color: var(--text-secondary);
  }
}

.pagination {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}
</style>
