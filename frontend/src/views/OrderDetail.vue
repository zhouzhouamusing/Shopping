<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="page-inner" v-if="order">
      <h1 class="page-title">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        订单详情
      </h1>

      <!-- 订单状态 -->
      <div class="status-card">
        <div class="status-icon" :class="'status-' + order.status">
          <el-icon :size="32">
            <component :is="statusIcons[order.status]" />
          </el-icon>
        </div>
        <div class="status-info">
          <h3>{{ getStatusText(order.status) }}</h3>
          <p>订单号：{{ order.orderNo }}</p>
          <p>创建时间：{{ formatTime(order.createdAt) }}</p>
          <p v-if="order.paymentTime">支付时间：{{ formatTime(order.paymentTime) }}</p>
          <p v-if="order.deliveryTime">发货时间：{{ formatTime(order.deliveryTime) }}</p>
          <p v-if="order.finishTime">完成时间：{{ formatTime(order.finishTime) }}</p>
        </div>
        <div class="status-actions">
          <el-button v-if="order.status === 0" type="primary" @click="payOrder">立即付款</el-button>
          <el-button v-if="order.status === 0" @click="cancelOrder">取消订单</el-button>
          <el-button v-if="order.status === 2" type="primary" @click="confirmReceive">确认收货</el-button>
          <el-button v-if="order.status === 1 || order.status === 2" type="warning" @click="refundOrder">申请退款</el-button>
        </div>
      </div>

      <!-- 订单进度 -->
      <div class="progress-card" v-if="order.status <= 4 && order.status !== 0">
        <el-steps :active="getProgressStep(order.status)" finish-status="success" align-center>
          <el-step title="已付款" />
          <el-step title="已发货" />
          <el-step title="已收货" />
          <el-step title="已完成" />
        </el-steps>
      </div>

      <!-- 收货信息 -->
      <div class="info-card">
        <h3>收货信息</h3>
        <div class="info-grid">
          <div><span class="label">收货人：</span>{{ order.receiverName }}</div>
          <div><span class="label">联系电话：</span>{{ order.receiverPhone }}</div>
          <div><span class="label">收货地址：</span>{{ order.receiverAddress }}</div>
          <div v-if="order.paymentMethod"><span class="label">支付方式：</span>{{ getMethodName(order.paymentMethod) }}</div>
          <div v-if="order.remark"><span class="label">备注：</span>{{ order.remark }}</div>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="info-card">
        <h3>商品信息</h3>
        <div class="items-list">
          <div v-for="item in order.orderItems" :key="item.id" class="item-row">
            <img :src="item.productImage" :alt="item.productName" />
            <div class="item-info">
              <h4>{{ item.productName }}</h4>
              <p>¥{{ item.price }} × {{ item.quantity }}</p>
            </div>
            <div class="item-total">¥{{ item.totalPrice }}</div>
          </div>
        </div>
        <div class="total-row">
          <span>订单总额：</span>
          <span class="total-amount">¥{{ order.totalAmount }}</span>
        </div>
      </div>

      <!-- 评价区域 -->
      <div class="info-card review-section" v-if="order.status === 3">
        <h3>发表评价</h3>
        <div v-for="item in order.orderItems" :key="item.id" class="review-form-item">
          <div class="review-product-header">
            <img :src="item.productImage" :alt="item.productName" />
            <span>{{ item.productName }}</span>
          </div>
          <el-form label-width="60px" class="review-form">
            <el-form-item label="评分">
              <el-rate v-model="reviewForms[item.id].rating" :colors="['#faad14', '#faad14', '#00d4aa']" />
            </el-form-item>
            <el-form-item label="评价">
              <el-input
                v-model="reviewForms[item.id].content"
                type="textarea"
                :rows="3"
                placeholder="分享你的使用体验..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="图片">
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :on-success="(res) => onUploadSuccess(res, item.id)"
                :on-remove="(file) => onUploadRemove(file, item.id)"
                :limit="5"
                list-type="picture-card"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </el-form>
        </div>
        <div class="review-submit-row">
          <el-button type="primary" size="large" :loading="submittingReview" @click="submitReviews">
            提交评价
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const order = ref(null)
const loading = ref(true)
const submittingReview = ref(false)
const reviewForms = reactive({})

const uploadUrl = '/api/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const statusIcons = {
  0: 'Clock',
  1: 'CreditCard',
  2: 'Van',
  3: 'Star',
  4: 'CircleCheck',
  5: 'CircleClose',
  6: 'RefreshLeft',
  7: 'Timer'
}

const getStatusText = (status) => {
  const map = {
    0: '待付款', 1: '待发货', 2: '待收货', 3: '待评价',
    4: '已完成', 5: '已取消', 6: '已退款', 7: '已过期'
  }
  return map[status] || '未知状态'
}

const getMethodName = (method) => {
  const map = { ALIPAY: '支付宝', WECHAT: '微信支付', BANK_CARD: '银行卡', COD: '货到付款' }
  return map[method] || method
}

const getProgressStep = (status) => {
  if (status === 1) return 1
  if (status === 2) return 2
  if (status === 3) return 3
  if (status === 4) return 4
  return 0
}

const formatTime = (time) => time ? new Date(time).toLocaleString('zh-CN') : ''

const goBack = () => {
  router.push({ name: 'Orders' })
}

const fetchOrder = async () => {
  loading.value = true
  try {
    const res = await api.get(`/orders/${route.params.orderNo}`)
    if (res.code === 200) {
      order.value = res.data
      initReviewForms()
    }
  } finally {
    loading.value = false
  }
}

const initReviewForms = () => {
  if (order.value?.orderItems) {
    order.value.orderItems.forEach(item => {
      if (!reviewForms[item.id]) {
        reviewForms[item.id] = { rating: 5, content: '', images: [] }
      }
    })
  }
}

const onUploadSuccess = (response, itemId) => {
  if (response.code === 200) {
    reviewForms[itemId].images.push(response.data)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const onUploadRemove = (file, itemId) => {
  const url = file.response?.data
  if (url) {
    const idx = reviewForms[itemId].images.indexOf(url)
    if (idx > -1) reviewForms[itemId].images.splice(idx, 1)
  }
}

const submitReviews = async () => {
  if (!order.value?.orderItems?.length) return

  for (const item of order.value.orderItems) {
    if (!reviewForms[item.id]?.rating) {
      ElMessage.warning(`请对 ${item.productName} 进行评分`)
      return
    }
  }

  submittingReview.value = true
  try {
    for (const item of order.value.orderItems) {
      const form = reviewForms[item.id]
      const res = await api.post('/reviews', {
        orderItemId: item.id,
        rating: form.rating,
        content: form.content,
        images: form.images
      })
      if (res.code !== 200) {
        ElMessage.error(res.message || '评价提交失败')
        return
      }
    }

    await api.put(`/orders/${order.value.orderNo}/complete`)
    ElMessage.success('评价提交成功')
    fetchOrder()
  } finally {
    submittingReview.value = false
  }
}

const payOrder = () => {
  router.push({ name: 'Payment', params: { orderNo: order.value.orderNo } })
}

const cancelOrder = () => {
  ElMessageBox.confirm('确定要取消订单吗？', '取消订单', { type: 'warning' })
    .then(async () => {
      const res = await api.put(`/orders/${order.value.orderNo}/cancel`)
      if (res.code === 200) {
        ElMessage.success('订单已取消')
        fetchOrder()
      }
    })
    .catch(() => {})
}

const confirmReceive = () => {
  ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'info' })
    .then(async () => {
      const res = await api.put(`/orders/${order.value.orderNo}/confirm`)
      if (res.code === 200) {
        ElMessage.success('已确认收货')
        fetchOrder()
      }
    })
    .catch(() => {})
}

const refundOrder = () => {
  ElMessageBox.confirm('确定要申请退款吗？退款后库存将恢复。', '申请退款', { type: 'warning' })
    .then(async () => {
      const res = await api.put(`/orders/${order.value.orderNo}/refund`)
      if (res.code === 200) {
        ElMessage.success('退款成功')
        fetchOrder()
      }
    })
    .catch(() => {})
}

onMounted(fetchOrder)
</script>

<style lang="scss" scoped>
.order-detail-page {
  padding: 32px 0;
}

.page-inner {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.status-card {
  background: white;
  border-radius: var(--radius);
  padding: 32px;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 16px;

  @media (max-width: 768px) {
    flex-direction: column;
    text-align: center;
  }
}

.status-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;

  &.status-0 { background: #faad14; }
  &.status-1 { background: var(--accent); }
  &.status-2 { background: var(--primary); }
  &.status-3 { background: #f59e0b; }
  &.status-4 { background: #10b981; }
  &.status-5 { background: #9ca3af; }
  &.status-6 { background: #ef4444; }
  &.status-7 { background: #6b7280; }
}

.status-info {
  flex: 1;

  h3 { font-size: 18px; margin-bottom: 4px; }
  p { font-size: 13px; color: var(--text-light); margin-top: 2px; }
}

.status-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;

  @media (max-width: 768px) {
    justify-content: center;
  }
}

.progress-card {
  background: white;
  border-radius: var(--radius);
  padding: 24px 32px;
  box-shadow: var(--shadow);
  margin-bottom: 16px;
}

.info-card {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
  margin-bottom: 16px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border);
  }
}

.info-grid {
  display: grid;
  gap: 10px;
  font-size: 14px;

  .label { color: var(--text-light); }
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);

  &:last-child { border-bottom: none; }

  img {
    width: 64px;
    height: 64px;
    border-radius: var(--radius-sm);
    object-fit: cover;
  }

  .item-info {
    flex: 1;

    h4 { font-size: 14px; margin-bottom: 4px; }
    p { font-size: 13px; color: var(--text-light); }
  }

  .item-total {
    font-weight: 600;
    color: var(--primary);
  }
}

.total-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  padding-top: 16px;
  margin-top: 12px;
  border-top: 1px solid var(--border);
  font-size: 14px;
}

.total-amount {
  font-size: 22px;
  font-weight: 800;
  color: var(--primary);
}

/* 评价表单 */
.review-section {
  h3 {
    color: var(--primary);
  }
}

.review-form-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--border);

  &:last-child {
    border-bottom: none;
  }
}

.review-product-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  img {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    object-fit: cover;
  }

  span {
    font-size: 14px;
    font-weight: 500;
  }
}

.review-form {
  padding-left: 60px;

  @media (max-width: 768px) {
    padding-left: 0;
  }
}

.review-submit-row {
  display: flex;
  justify-content: center;
  padding-top: 16px;
}
</style>
