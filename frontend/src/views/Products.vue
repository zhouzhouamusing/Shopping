<template>
  <div class="products-page">
    <div class="page-inner">
      <!-- 页面顶部 -->
      <div class="page-header">
        <el-button class="back-btn" text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2 class="page-title" v-if="$route.query.keyword">
          搜索"{{ $route.query.keyword }}"的结果
        </h2>
        <h2 class="page-title" v-else>全部商品</h2>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="filter-left">
          <el-button
            v-for="s in sortOptions"
            :key="s.value"
            :type="currentSort === s.value ? 'primary' : ''"
            @click="changeSort(s.value)"
            size="default"
          >
            {{ s.label }}
          </el-button>
        </div>
        <div class="filter-right">
          <span class="result-count">共 {{ total }} 件商品</span>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="product-grid" v-loading="loading">
        <div
          v-for="(product, index) in products"
          :key="product.id"
          class="product-card animate-fadeInUp"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="goDetail(product.id)"
        >
          <div class="product-img-wrapper">
            <img :src="product.mainImage" :alt="product.name" loading="lazy" />
            <div class="discount-tag" v-if="product.originalPrice && product.originalPrice > product.price">
              {{ Math.round((1 - product.price / product.originalPrice) * 100) }}%OFF
            </div>
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-bottom">
              <div class="product-price">
                <span class="price"><span class="symbol">¥</span>{{ product.price }}</span>
                <span class="price-original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
              </div>
              <span class="sales">{{ product.sales }}人付款</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && products.length === 0">
        <el-icon class="empty-icon"><ShoppingBag /></el-icon>
        <p>暂无相关商品</p>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchProducts"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()

const products = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 12
const total = ref(0)
const currentSort = ref('default')

const sortOptions = [
  { label: '综合排序', value: 'default' },
  { label: '销量优先', value: 'sales' },
  { label: '最新上架', value: 'newest' },
  { label: '价格从低到高', value: 'price_asc' },
  { label: '价格从高到低', value: 'price_desc' }
]

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value - 1,
      size: pageSize,
      sort: currentSort.value
    }
    if (route.query.keyword) params.keyword = route.query.keyword
    if (route.query.categoryId) params.categoryId = route.query.categoryId

    const res = await api.get('/products', { params })
    if (res.code === 200) {
      products.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const changeSort = (sort) => {
  currentSort.value = sort
  page.value = 1
  fetchProducts()
}

const goDetail = (id) => {
  router.push({ name: 'ProductDetail', params: { id } })
}

watch(() => route.query, () => {
  page.value = 1
  if (route.query.sort) currentSort.value = route.query.sort
  fetchProducts()
}, { immediate: false })

onMounted(() => {
  if (route.query.sort) currentSort.value = route.query.sort
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.products-page {
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

    &:hover {
      color: var(--primary);
    }
  }

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 16px 24px;
  border-radius: var(--radius);
  margin-bottom: 24px;
  box-shadow: var(--shadow);
}

.filter-left {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.result-count {
  font-size: 14px;
  color: var(--text-light);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 200px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

.product-card {
  background: white;
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  transition: var(--transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  opacity: 0;
  animation-fill-mode: forwards;

  &:hover {
    transform: translateY(-6px);
    box-shadow: var(--shadow-hover);

    img {
      transform: scale(1.05);
    }
  }
}

.product-img-wrapper {
  position: relative;
  padding-top: 100%;
  overflow: hidden;
  background: #f8f9fa;

  img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }
}

.discount-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  background: var(--gradient-primary);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.product-desc {
  font-size: 12px;
  color: var(--text-light);
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-bottom {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.sales {
  font-size: 12px;
  color: var(--text-light);
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>
