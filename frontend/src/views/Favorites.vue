<template>
  <div class="favorites-page">
    <div class="page-inner">
      <div class="page-header">
        <el-button class="back-btn" text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2 class="page-title">我的收藏</h2>
        <span class="count-badge" v-if="total > 0">{{ total }} 件</span>
      </div>

      <div class="product-grid" v-loading="loading">
        <div
          v-for="(item, index) in favorites"
          :key="item.id"
          class="product-card animate-fadeInUp"
          :style="{ animationDelay: `${index * 0.05}s` }"
        >
          <div class="product-img-wrapper" @click="goDetail(item.product?.id)">
            <img :src="item.product?.mainImage" :alt="item.product?.name" loading="lazy" />
          </div>
          <div class="product-info">
            <h3 class="product-name" @click="goDetail(item.product?.id)">{{ item.product?.name }}</h3>
            <div class="product-bottom">
              <div class="product-price">
                <span class="price"><span class="symbol">¥</span>{{ item.product?.price }}</span>
              </div>
              <el-button
                type="danger"
                size="small"
                text
                @click="removeFavorite(item.productId)"
              >
                取消收藏
              </el-button>
            </div>
            <div class="fav-time">收藏于 {{ formatTime(item.createdAt) }}</div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && favorites.length === 0">
        <el-icon class="empty-icon"><Star /></el-icon>
        <p>暂无收藏商品</p>
        <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchFavorites"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()

const favorites = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 12
const total = ref(0)

const fetchFavorites = async () => {
  loading.value = true
  try {
    const res = await api.get('/favorites', { params: { page: page.value - 1, size: pageSize } })
    if (res.code === 200) {
      favorites.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (productId) => {
  try {
    const res = await api.delete(`/favorites/${productId}`)
    if (res.code === 200) {
      ElMessage.success('已取消收藏')
      fetchFavorites()
    }
  } catch (e) {
    // handled by interceptor
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
  fetchFavorites()
})
</script>

<style lang="scss" scoped>
.favorites-page {
  padding: 24px 0;
}

.page-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

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

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.product-card {
  background: white;
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  transition: var(--transition);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  }
}

.product-img-wrapper {
  position: relative;
  cursor: pointer;

  img {
    width: 100%;
    aspect-ratio: 1;
    object-fit: cover;
  }
}

.product-info {
  padding: 14px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 10px;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;

  &:hover { color: var(--primary); }
}

.product-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-price {
  .price {
    color: var(--primary);
    font-size: 18px;
    font-weight: 700;
  }
  .symbol {
    font-size: 13px;
  }
}

.fav-time {
  margin-top: 8px;
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
