<template>
  <div class="product-detail-page" v-loading="loading">
    <div class="detail-inner" v-if="product">
      <!-- 返回导航 -->
      <div class="breadcrumb-nav">
        <el-button class="back-btn" text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回上一页
        </el-button>
        <span class="nav-sep">/</span>
        <span class="nav-current">{{ product.name }}</span>
      </div>

      <!-- 商品主信息 -->
      <div class="product-main">
        <!-- 图片区 -->
        <div class="product-gallery animate-fadeInUp">
          <div class="main-image">
            <img :src="product.mainImage" :alt="product.name" />
          </div>
        </div>

        <!-- 信息区 -->
        <div class="product-info animate-fadeInUp stagger-2">
          <h1 class="product-title">{{ product.name }}</h1>

          <div class="price-block">
            <div class="current-price">
              <span class="symbol">¥</span>
              <span class="amount">{{ product.price }}</span>
            </div>
            <div class="original-price" v-if="product.originalPrice">
              <span>原价: ¥{{ product.originalPrice }}</span>
              <span class="discount-badge">
                省{{ (product.originalPrice - product.price).toFixed(0) }}元
              </span>
            </div>
          </div>

          <div class="info-items">
            <div class="info-row">
              <span class="info-label">销量</span>
              <span class="info-value">{{ product.sales }}+ 件</span>
            </div>
            <div class="info-row">
              <span class="info-label">库存</span>
              <span class="info-value" :class="{ 'low-stock': product.stock < 10 }">
                {{ product.stock > 0 ? product.stock + ' 件' : '暂时缺货' }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">运费</span>
              <span class="info-value free-shipping">免运费</span>
            </div>
          </div>

          <!-- 数量选择 -->
          <div class="quantity-section">
            <span class="info-label">数量</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock"
              size="large"
            />
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              class="btn-buy"
              :disabled="product.stock === 0"
              @click="buyNow"
            >
              <el-icon><ShoppingBag /></el-icon>
              立即购买
            </el-button>
            <el-button
              size="large"
              class="btn-cart"
              :disabled="product.stock === 0"
              @click="addToCart"
            >
              <el-icon><ShoppingCart /></el-icon>
              加入购物车
            </el-button>
          </div>

          <!-- 保障 -->
          <div class="guarantees">
            <div class="guarantee-item">
              <el-icon><CircleCheck /></el-icon>
              <span>正品保障</span>
            </div>
            <div class="guarantee-item">
              <el-icon><CircleCheck /></el-icon>
              <span>7天无理由退换</span>
            </div>
            <div class="guarantee-item">
              <el-icon><CircleCheck /></el-icon>
              <span>极速发货</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品详情描述 -->
      <div class="product-description animate-fadeInUp stagger-3">
        <h2 class="desc-title">商品详情</h2>
        <div class="desc-content">
          <p>{{ product.description }}</p>
        </div>
      </div>

      <!-- 商品评价 -->
      <div class="product-reviews animate-fadeInUp stagger-4">
        <div class="reviews-header">
          <h2 class="desc-title">商品评价</h2>
          <div class="rating-summary" v-if="product.reviewCount">
            <span class="avg-score">{{ product.averageRating || '-' }}</span>
            <el-rate :model-value="Number(product.averageRating) || 0" disabled />
            <span class="review-count">{{ product.reviewCount }} 条评价</span>
          </div>
          <span v-else class="no-review-hint">暂无评价</span>
        </div>

        <!-- 评分筛选 -->
        <div class="rating-filter" v-if="product.reviewCount">
          <el-radio-group v-model="ratingFilter" size="small" @change="fetchReviews">
            <el-radio-button :value="0">全部</el-radio-button>
            <el-radio-button :value="5">5星</el-radio-button>
            <el-radio-button :value="4">4星</el-radio-button>
            <el-radio-button :value="3">3星</el-radio-button>
            <el-radio-button :value="2">2星</el-radio-button>
            <el-radio-button :value="1">1星</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 评价列表 -->
        <div class="reviews-list" v-if="reviews.length > 0">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-user-row">
              <el-avatar :size="36" :src="review.userAvatar">
                {{ review.username?.charAt(0) }}
              </el-avatar>
              <div class="review-user-info">
                <span class="review-username">{{ review.username }}</span>
                <div class="review-meta">
                  <el-rate :model-value="review.rating" disabled size="small" />
                  <span class="review-time">{{ formatTime(review.createdAt) }}</span>
                </div>
              </div>
            </div>
            <p class="review-content" v-if="review.content">{{ review.content }}</p>
            <div class="review-images" v-if="review.imageList && review.imageList.length">
              <el-image
                v-for="(img, idx) in review.imageList"
                :key="idx"
                :src="img"
                :preview-src-list="review.imageList"
                fit="cover"
                class="review-img"
              />
            </div>
            <div class="admin-reply-box" v-if="review.adminReply">
              <span class="reply-label">商家回复：</span>
              <span>{{ review.adminReply }}</span>
            </div>
          </div>
        </div>
        <div v-else-if="product.reviewCount" class="no-reviews-filter">
          <el-empty description="该筛选条件下暂无评价" :image-size="60" />
        </div>

        <!-- 分页 -->
        <div class="reviews-pagination" v-if="reviewTotal > reviewPageSize">
          <el-pagination
            v-model:current-page="reviewPage"
            :page-size="reviewPageSize"
            :total="reviewTotal"
            layout="prev, pager, next"
            @current-change="fetchReviews"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const quantity = ref(1)

// 评价相关
const reviews = ref([])
const reviewPage = ref(1)
const reviewPageSize = ref(10)
const reviewTotal = ref(0)
const ratingFilter = ref(0)

const fetchProduct = async () => {
  loading.value = true
  try {
    const res = await api.get(`/products/${route.params.id}`)
    if (res.code === 200) {
      product.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const fetchReviews = async () => {
  const params = { page: reviewPage.value - 1, size: reviewPageSize.value }
  if (ratingFilter.value > 0) params.rating = ratingFilter.value
  try {
    const res = await api.get(`/products/${route.params.id}/reviews`, { params })
    if (res.code === 200) {
      reviews.value = res.data.content
      reviewTotal.value = res.data.totalElements
    }
  } catch (e) {
    // silently fail
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const addToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const res = await cartStore.addToCart(product.value.id, quantity.value)
  if (res.code === 200) {
    ElMessage.success('已加入购物车')
  } else {
    ElMessage.error(res.message)
  }
}

const buyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await addToCart()
  router.push('/cart')
}

onMounted(() => {
  fetchProduct()
  fetchReviews()
})
</script>

<style lang="scss" scoped>
.product-detail-page {
  padding: 32px 0;
  min-height: 60vh;
}

.detail-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 12px 20px;
  background: white;
  border-radius: var(--radius);
  box-shadow: var(--shadow);

  .back-btn {
    font-size: 14px;
    color: var(--accent);
    font-weight: 500;

    &:hover {
      color: var(--primary);
    }
  }

  .nav-sep {
    color: var(--text-light);
    font-size: 12px;
  }

  .nav-current {
    font-size: 13px;
    color: var(--text-secondary);
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  background: white;
  border-radius: var(--radius-lg);
  padding: 40px;
  box-shadow: var(--shadow);

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: 24px;
    padding: 20px;
  }
}

.product-gallery {
  .main-image {
    border-radius: var(--radius);
    overflow: hidden;
    background: #f8f9fa;

    img {
      width: 100%;
      aspect-ratio: 1;
      object-fit: cover;
      transition: transform 0.5s;

      &:hover {
        transform: scale(1.03);
      }
    }
  }
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--text-primary);
}

.price-block {
  background: linear-gradient(135deg, #f0fdf9, #ecfdf5);
  padding: 20px 24px;
  border-radius: var(--radius);
}

.current-price {
  color: var(--primary);

  .symbol {
    font-size: 18px;
    font-weight: 600;
  }

  .amount {
    font-size: 36px;
    font-weight: 800;
  }
}

.original-price {
  margin-top: 8px;
  font-size: 14px;
  color: var(--text-light);
  display: flex;
  align-items: center;
  gap: 10px;

  span:first-child {
    text-decoration: line-through;
  }
}

.discount-badge {
  background: var(--primary);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  text-decoration: none !important;
}

.info-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-label {
  color: var(--text-light);
  font-size: 14px;
  min-width: 40px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
}

.low-stock {
  color: var(--primary);
}

.free-shipping {
  color: #52c41a;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 8px;

  .btn-buy {
    flex: 1;
    height: 50px;
    font-size: 16px;
    font-weight: 600;
    border-radius: var(--radius);
    background: var(--gradient-primary);
    border: none;
  }

  .btn-cart {
    flex: 1;
    height: 50px;
    font-size: 16px;
    font-weight: 600;
    border-radius: var(--radius);
    color: var(--primary);
    border-color: var(--primary);

    &:hover {
      background: #f0fdf9;
    }
  }
}

.guarantees {
  display: flex;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border);

  .guarantee-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: var(--text-secondary);

    .el-icon {
      color: #52c41a;
    }
  }
}

.product-description {
  margin-top: 32px;
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px 40px;
  box-shadow: var(--shadow);
}

.desc-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--primary);
  display: inline-block;
}

.desc-content {
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 15px;
}

/* 评价区域 */
.product-reviews {
  margin-top: 32px;
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px 40px;
  box-shadow: var(--shadow);
}

.reviews-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.rating-summary {
  display: flex;
  align-items: center;
  gap: 8px;

  .avg-score {
    font-size: 28px;
    font-weight: 800;
    color: var(--primary);
  }

  .review-count {
    font-size: 13px;
    color: var(--text-light);
  }
}

.no-review-hint {
  font-size: 14px;
  color: var(--text-light);
}

.rating-filter {
  margin-bottom: 20px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--border);

  &:last-child {
    border-bottom: none;
  }
}

.review-user-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.review-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.review-username {
  font-size: 14px;
  font-weight: 500;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 10px;

  .review-time {
    font-size: 12px;
    color: var(--text-light);
  }
}

.review-content {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 10px;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;

  .review-img {
    width: 80px;
    height: 80px;
    border-radius: 8px;
    cursor: pointer;
  }
}

.admin-reply-box {
  background: #f8f9fa;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-secondary);

  .reply-label {
    color: var(--primary);
    font-weight: 600;
  }
}

.no-reviews-filter {
  padding: 20px 0;
}

.reviews-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
