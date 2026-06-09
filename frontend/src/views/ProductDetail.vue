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

onMounted(fetchProduct)
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
</style>
