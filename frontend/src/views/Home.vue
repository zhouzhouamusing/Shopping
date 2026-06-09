<template>
  <div class="home-page">
    <!-- Hero Banner区域 -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-gradient"></div>
        <div class="hero-particles">
          <div v-for="i in 12" :key="i" class="h-particle" :style="getHeroParticle(i)"></div>
        </div>
      </div>
      <div class="hero-content animate-fadeInUp">
        <h1 class="hero-title">
          <span class="title-line">品质生活</span>
          <span class="title-highlight">从潮购开始</span>
        </h1>
        <p class="hero-desc">精选全球好物，为您提供极致购物体验</p>
        <div class="hero-search">
          <el-input
            v-model="keyword"
            size="large"
            placeholder="搜索你想要的商品..."
            @keyup.enter="goSearch"
          >
            <template #append>
              <el-button type="primary" @click="goSearch">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
            </template>
          </el-input>
        </div>
        <div class="hero-tags">
          <span class="hot-tag" v-for="tag in hotTags" :key="tag" @click="searchTag(tag)">{{ tag }}</span>
        </div>
      </div>
    </section>

    <!-- 分类导航 -->
    <section class="category-section">
      <div class="section-inner">
        <div class="category-grid">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="category-item"
            @click="goCategory(cat.id)"
          >
            <div class="cat-icon">
              <el-icon :size="28"><component :is="getCatIcon(cat.icon)" /></el-icon>
            </div>
            <span class="cat-name">{{ cat.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门推荐 -->
    <section class="product-section">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><TrendCharts /></el-icon>
            热门推荐
          </h2>
          <router-link to="/products?sort=sales" class="view-more">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        <div class="product-grid">
          <div
            v-for="(product, index) in hotProducts"
            :key="product.id"
            class="product-card animate-fadeInUp"
            :style="{ animationDelay: `${index * 0.08}s` }"
            @click="goDetail(product.id)"
          >
            <div class="product-img-wrapper">
              <img :src="product.mainImage" :alt="product.name" loading="lazy" />
              <div class="product-badge tag-hot" v-if="index < 3">TOP{{ index + 1 }}</div>
              <div class="product-overlay">
                <el-button circle size="small" @click.stop="addCart(product)">
                  <el-icon><ShoppingCart /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-meta">
                <span class="price"><span class="symbol">¥</span>{{ product.price }}</span>
                <span class="price-original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
              </div>
              <div class="product-stats">
                <span>已售 {{ product.sales }}+</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新上架 -->
    <section class="product-section new-section">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><Clock /></el-icon>
            最新上架
          </h2>
          <router-link to="/products?sort=newest" class="view-more">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        <div class="product-grid">
          <div
            v-for="(product, index) in newProducts"
            :key="product.id"
            class="product-card animate-fadeInUp"
            :style="{ animationDelay: `${index * 0.08}s` }"
            @click="goDetail(product.id)"
          >
            <div class="product-img-wrapper">
              <img :src="product.mainImage" :alt="product.name" loading="lazy" />
              <div class="product-badge tag-new">NEW</div>
              <div class="product-overlay">
                <el-button circle size="small" @click.stop="addCart(product)">
                  <el-icon><ShoppingCart /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-meta">
                <span class="price"><span class="symbol">¥</span>{{ product.price }}</span>
                <span class="price-original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
              </div>
              <div class="product-stats">
                <span>已售 {{ product.sales }}+</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const keyword = ref('')
const categories = ref([])
const hotProducts = ref([])
const newProducts = ref([])

const hotTags = ['iPhone 15', '小米14', 'MacBook', '耳机', '运动鞋', '护肤品']

const goSearch = () => {
  if (keyword.value.trim()) {
    router.push({ name: 'Products', query: { keyword: keyword.value.trim() } })
  }
}

const searchTag = (tag) => {
  router.push({ name: 'Products', query: { keyword: tag } })
}

const goCategory = (id) => {
  router.push({ name: 'Products', query: { categoryId: id } })
}

const goDetail = (id) => {
  router.push({ name: 'ProductDetail', params: { id } })
}

const addCart = async (product) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const res = await cartStore.addToCart(product.id)
  if (res.code === 200) {
    ElMessage.success('已加入购物车')
  }
}

const getCatIcon = (icon) => {
  const map = {
    'Smartphone': 'Iphone',
    'Computer': 'Monitor',
    'HomeAppliance': 'HomeFilled',
    'Clothing': 'Goods',
    'Beauty': 'MagicStick',
    'Food': 'Coffee',
    'Book': 'Notebook',
    'Sports': 'Trophy'
  }
  return map[icon] || 'Goods'
}

const getHeroParticle = (i) => ({
  left: `${10 + Math.random() * 80}%`,
  top: `${10 + Math.random() * 80}%`,
  animationDelay: `${Math.random() * 3}s`,
  width: `${4 + Math.random() * 8}px`,
  height: `${4 + Math.random() * 8}px`,
  opacity: 0.3 + Math.random() * 0.4
})

onMounted(async () => {
  try {
    const [catRes, hotRes, newRes] = await Promise.all([
      api.get('/categories'),
      api.get('/products/hot?size=8'),
      api.get('/products/new?size=8')
    ])
    if (catRes.code === 200) categories.value = catRes.data
    if (hotRes.code === 200) hotProducts.value = hotRes.data.content
    if (newRes.code === 200) newProducts.value = newRes.data.content
  } catch (e) {
    console.error('Failed to load home data', e)
  }
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
}

/* Hero区域 */
.hero-section {
  position: relative;
  padding: 80px 24px 60px;
  overflow: hidden;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 0%, rgba(255, 77, 79, 0.15) 0%, transparent 60%),
              radial-gradient(ellipse at 80% 80%, rgba(108, 99, 255, 0.1) 0%, transparent 40%);
}

.hero-particles {
  position: absolute;
  inset: 0;
}

.h-particle {
  position: absolute;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  animation: float 6s infinite ease-in-out;
}

.hero-content {
  position: relative;
  z-index: 10;
  max-width: 700px;
  margin: 0 auto;
  text-align: center;
}

.hero-title {
  margin-bottom: 16px;

  .title-line {
    display: block;
    font-size: 48px;
    font-weight: 800;
    color: white;
    letter-spacing: 2px;

    @media (max-width: 768px) {
      font-size: 32px;
    }
  }

  .title-highlight {
    display: block;
    font-size: 48px;
    font-weight: 800;
    background: linear-gradient(135deg, #ff4d4f, #ff7875, #ffa940);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-size: 200% 200%;
    animation: gradient-shift 4s ease infinite;

    @media (max-width: 768px) {
      font-size: 32px;
    }
  }
}

.hero-desc {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  margin-bottom: 32px;
}

.hero-search {
  max-width: 520px;
  margin: 0 auto 20px;

  :deep(.el-input__wrapper) {
    border-radius: 28px 0 0 28px;
    padding: 6px 20px;
    background: rgba(255, 255, 255, 0.95);
  }

  :deep(.el-input-group__append) {
    border-radius: 0 28px 28px 0;
    background: linear-gradient(135deg, #6c63ff, #8b83ff);
    border-color: #6c63ff;
    padding: 0 24px;
  }
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;

  .hot-tag {
    padding: 5px 16px;
    background: rgba(108, 99, 255, 0.15);
    border: 1px solid rgba(108, 99, 255, 0.3);
    border-radius: 20px;
    color: rgba(255, 255, 255, 0.9);
    font-size: 13px;
    cursor: pointer;
    transition: var(--transition);
    backdrop-filter: blur(4px);

    &:hover {
      background: linear-gradient(135deg, #6c63ff, #8b83ff);
      border-color: #6c63ff;
      color: white;
      transform: translateY(-2px);
      box-shadow: 0 4px 15px rgba(108, 99, 255, 0.4);
    }
  }
}

/* 分类导航 */
.category-section {
  padding: 32px 0;
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.section-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 16px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(4, 1fr);
  }
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: var(--radius);
  cursor: pointer;
  transition: var(--transition);

  &:hover {
    background: linear-gradient(135deg, #f0f0ff, #e8f4ff);
    transform: translateY(-4px);

    .cat-icon {
      background: linear-gradient(135deg, #6c63ff, #8b83ff);
      color: white;
      box-shadow: 0 6px 20px rgba(108, 99, 255, 0.3);
    }
  }

  .cat-icon {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    background: linear-gradient(135deg, #eef0ff, #e8ecff);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #6c63ff;
    transition: var(--transition);
  }

  .cat-name {
    font-size: 13px;
    color: var(--text-secondary);
    font-weight: 500;
  }
}

/* 商品区域 */
.product-section {
  padding: 48px 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);

  .el-icon {
    color: var(--primary);
  }
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-secondary);
  transition: var(--transition);

  &:hover {
    color: var(--primary);
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

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
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);

    .product-img-wrapper img {
      transform: scale(1.05);
    }

    .product-overlay {
      opacity: 1;
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

.product-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
}

.product-overlay {
  position: absolute;
  bottom: 12px;
  right: 12px;
  opacity: 0;
  transition: var(--transition);

  .el-button {
    background: white;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.product-meta {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 6px;
}

.product-stats {
  font-size: 12px;
  color: var(--text-light);
}
</style>
