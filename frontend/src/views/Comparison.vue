<template>
  <div class="comparison-page">
    <div class="page-inner">
      <div class="page-header">
        <div class="header-left">
          <el-button class="back-btn" text @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <h2 class="page-title">商品对比</h2>
          <span class="count-badge" v-if="products.length > 0">{{ products.length }} 件商品</span>
        </div>
        <el-button
          v-if="products.length > 0"
          type="danger"
          plain
          size="small"
          @click="handleClearAll"
        >
          清空对比
        </el-button>
      </div>

      <!-- 对比表格 -->
      <div class="compare-table-wrapper" v-if="products.length >= 2" v-loading="loading">
        <table class="compare-table">
          <thead>
            <tr>
              <th class="attr-header">属性</th>
              <th v-for="product in products" :key="product.id" class="product-header">
                <div class="product-header-content">
                  <img :src="product.mainImage" :alt="product.name" class="header-img" />
                  <span class="header-name">{{ product.name }}</span>
                  <el-button
                    type="danger"
                    size="small"
                    circle
                    @click="removeProduct(product.id)"
                  >
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td class="attr-label">价格</td>
              <td v-for="product in products" :key="product.id"
                  :class="{ 'highlight-best': isBestPrice(product) }">
                <span class="price-value">¥{{ product.price }}</span>
              </td>
            </tr>
            <tr v-if="products.some(p => p.originalPrice)">
              <td class="attr-label">原价</td>
              <td v-for="product in products" :key="product.id">
                {{ product.originalPrice ? '¥' + product.originalPrice : '-' }}
              </td>
            </tr>
            <tr>
              <td class="attr-label">评分</td>
              <td v-for="product in products" :key="product.id"
                  :class="{ 'highlight-best': isBestRating(product) }">
                <el-rate :model-value="Number(product.averageRating) || 0" disabled size="small" />
                <span class="rating-text">{{ product.averageRating || '-' }}</span>
              </td>
            </tr>
            <tr>
              <td class="attr-label">销量</td>
              <td v-for="product in products" :key="product.id"
                  :class="{ 'highlight-best': isBestSales(product) }">
                {{ product.sales }} 件
              </td>
            </tr>
            <tr>
              <td class="attr-label">库存</td>
              <td v-for="product in products" :key="product.id">
                <span :class="{ 'low-stock': product.stock < 10 }">
                  {{ product.stock > 0 ? product.stock + ' 件' : '缺货' }}
                </span>
              </td>
            </tr>
            <tr>
              <td class="attr-label">评价数</td>
              <td v-for="product in products" :key="product.id">
                {{ product.reviewCount || 0 }} 条
              </td>
            </tr>
            <tr>
              <td class="attr-label">商品描述</td>
              <td v-for="product in products" :key="product.id" class="desc-cell">
                {{ product.description || '-' }}
              </td>
            </tr>
            <tr>
              <td class="attr-label">操作</td>
              <td v-for="product in products" :key="product.id">
                <el-button type="primary" size="small" @click="goDetail(product.id)">
                  查看详情
                </el-button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 不足2个商品提示 -->
      <div class="empty-state" v-if="!loading && products.length < 2">
        <el-icon class="empty-icon"><DataAnalysis /></el-icon>
        <p v-if="products.length === 0">暂无对比商品</p>
        <p v-else>请至少选择2个商品进行对比</p>
        <el-button type="primary" @click="$router.push('/products')">去选择商品</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useComparisonStore } from '@/stores/comparison'

const router = useRouter()
const comparisonStore = useComparisonStore()

const products = ref([])
const loading = ref(false)

const fetchProducts = async () => {
  if (comparisonStore.compareIds.length === 0) {
    products.value = []
    return
  }
  loading.value = true
  try {
    const data = await comparisonStore.fetchCompareProducts()
    products.value = data || []
  } finally {
    loading.value = false
  }
}

const removeProduct = (productId) => {
  comparisonStore.removeFromCompare(productId)
  products.value = products.value.filter(p => p.id !== productId)
}

const handleClearAll = () => {
  comparisonStore.clearAll()
  products.value = []
}

const goDetail = (id) => {
  router.push({ name: 'ProductDetail', params: { id } })
}

const isBestPrice = (product) => {
  if (products.value.length < 2) return false
  const minPrice = Math.min(...products.value.map(p => Number(p.price)))
  return Number(product.price) === minPrice
}

const isBestRating = (product) => {
  if (products.value.length < 2) return false
  const maxRating = Math.max(...products.value.map(p => Number(p.averageRating) || 0))
  return maxRating > 0 && (Number(product.averageRating) || 0) === maxRating
}

const isBestSales = (product) => {
  if (products.value.length < 2) return false
  const maxSales = Math.max(...products.value.map(p => p.sales || 0))
  return maxSales > 0 && (product.sales || 0) === maxSales
}

onMounted(() => {
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.comparison-page {
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
  justify-content: space-between;
  margin-bottom: 24px;

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

.compare-table-wrapper {
  overflow-x: auto;
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
}

.compare-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;

  th, td {
    padding: 16px 20px;
    border-bottom: 1px solid var(--border);
    text-align: center;
    vertical-align: middle;
  }

  th {
    background: #f8f9fc;
    font-weight: 600;
  }
}

.attr-header {
  width: 100px;
  text-align: left !important;
}

.attr-label {
  text-align: left !important;
  font-weight: 500;
  color: var(--text-secondary);
  white-space: nowrap;
}

.product-header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;

  .header-img {
    width: 100px;
    height: 100px;
    object-fit: cover;
    border-radius: 8px;
  }

  .header-name {
    font-size: 13px;
    max-width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.highlight-best {
  background: rgba(0, 212, 170, 0.06);
}

.price-value {
  color: var(--primary);
  font-weight: 700;
  font-size: 16px;
}

.rating-text {
  margin-left: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.low-stock {
  color: #e74c3c;
  font-weight: 500;
}

.desc-cell {
  font-size: 13px;
  color: var(--text-secondary);
  max-width: 200px;
  line-height: 1.5;
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
</style>
