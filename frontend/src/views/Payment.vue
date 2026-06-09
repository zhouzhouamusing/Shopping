<template>
  <div class="payment-page">
    <div class="page-inner">
      <h1 class="page-title">
        <el-button text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        订单支付
      </h1>

      <!-- 订单信息 -->
      <div class="order-summary" v-if="orderInfo">
        <div class="summary-row">
          <span class="label">订单编号</span>
          <span class="value">{{ orderInfo.orderNo }}</span>
        </div>
        <div class="summary-row">
          <span class="label">应付金额</span>
          <span class="value amount">¥{{ orderInfo.totalAmount }}</span>
        </div>
      </div>

      <!-- 支付倒计时 -->
      <div class="countdown-bar" v-if="payment && payment.paymentStatus === 'PENDING'">
        <el-icon><Clock /></el-icon>
        <span>请在 <b>{{ countdownText }}</b> 内完成支付，超时订单将自动取消</span>
      </div>

      <!-- 支付方式选择 -->
      <div class="payment-methods" v-if="!payment || payment.paymentStatus === 'PENDING'">
        <h3>选择支付方式</h3>
        <div class="method-grid">
          <div
            v-for="method in paymentMethods"
            :key="method.value"
            class="method-card"
            :class="{ active: selectedMethod === method.value }"
            @click="selectedMethod = method.value"
          >
            <div class="method-icon">
              <el-icon :size="28"><component :is="method.icon" /></el-icon>
            </div>
            <div class="method-info">
              <h4>{{ method.label }}</h4>
              <p>{{ method.desc }}</p>
            </div>
            <div class="method-check" v-if="selectedMethod === method.value">
              <el-icon><CircleCheck /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 支付操作区 -->
      <div class="payment-action" v-if="!payment">
        <el-button type="primary" size="large" class="pay-btn" :loading="creating" @click="handleCreatePayment">
          确认支付方式
        </el-button>
      </div>

      <!-- 模拟支付区 -->
      <div class="simulate-area" v-if="payment && payment.paymentStatus === 'PENDING'">
        <div class="simulate-card" v-if="selectedMethod === 'ALIPAY' || selectedMethod === 'WECHAT'">
          <div class="qr-mockup">
            <div class="qr-box">
              <el-icon :size="48"><Cellphone /></el-icon>
              <p>{{ selectedMethod === 'ALIPAY' ? '支付宝' : '微信' }}扫码区域</p>
              <span class="qr-hint">（模拟支付环境）</span>
            </div>
          </div>
          <p class="simulate-tip">点击下方按钮模拟扫码支付成功</p>
        </div>

        <div class="simulate-card" v-else-if="selectedMethod === 'BANK_CARD'">
          <div class="bank-mockup">
            <el-icon :size="48"><CreditCard /></el-icon>
            <p>银行卡快捷支付</p>
            <span class="bank-hint">将从您的银行卡中扣除 ¥{{ payment.paymentAmount }}</span>
          </div>
        </div>

        <el-button type="primary" size="large" class="pay-btn" :loading="paying" @click="handlePay">
          {{ getPayBtnText() }}
        </el-button>
      </div>

      <!-- 支付超时 -->
      <div class="expired-area" v-if="payment && payment.paymentStatus === 'EXPIRED'">
        <el-icon :size="64" class="expired-icon"><WarningFilled /></el-icon>
        <h3>支付已超时</h3>
        <p>订单已自动取消，请重新下单</p>
        <el-button type="primary" @click="$router.push('/products')">继续购物</el-button>
      </div>

      <!-- 支付成功 -->
      <div class="success-area" v-if="payment && payment.paymentStatus === 'SUCCESS'">
        <el-icon :size="64" class="success-icon"><CircleCheckFilled /></el-icon>
        <h3>支付成功</h3>
        <p>订单金额：¥{{ payment.paymentAmount }}</p>
        <div class="success-actions">
          <el-button type="primary" @click="$router.push({ name: 'OrderDetail', params: { orderNo: orderNo } })">查看订单</el-button>
          <el-button @click="$router.push('/products')">继续购物</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()
const orderNo = route.params.orderNo

const orderInfo = ref(null)
const payment = ref(null)
const selectedMethod = ref('ALIPAY')
const creating = ref(false)
const paying = ref(false)
const remainingSeconds = ref(0)
let countdownTimer = null

const paymentMethods = [
  { value: 'ALIPAY', label: '支付宝', desc: '推荐使用支付宝支付', icon: 'Wallet' },
  { value: 'WECHAT', label: '微信支付', desc: '使用微信扫码支付', icon: 'ChatDotRound' },
  { value: 'BANK_CARD', label: '银行卡支付', desc: '支持主流银行借记卡/信用卡', icon: 'CreditCard' },
  { value: 'COD', label: '货到付款', desc: '收到商品后再付款', icon: 'Box' }
]

const countdownText = computed(() => {
  const mins = Math.floor(remainingSeconds.value / 60)
  const secs = remainingSeconds.value % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
})

const getPayBtnText = () => {
  if (selectedMethod.value === 'BANK_CARD') return '确认支付'
  return '模拟支付成功'
}

const fetchOrder = async () => {
  try {
    const res = await api.get(`/orders/${orderNo}`)
    if (res.code === 200) {
      orderInfo.value = res.data
      if (res.data.status !== 0) {
        router.push({ name: 'OrderDetail', params: { orderNo } })
        return
      }
    }
  } catch (e) {
    ElMessage.error('获取订单信息失败')
  }
}

const fetchExistingPayment = async () => {
  try {
    const res = await api.get(`/payments/order/${orderNo}`)
    if (res.code === 200 && res.data) {
      payment.value = res.data
      selectedMethod.value = res.data.paymentMethod
      if (res.data.paymentStatus === 'PENDING') {
        remainingSeconds.value = res.data.remainingSeconds
        startCountdown()
      }
    }
  } catch (e) {
    // no existing payment
  }
}

const handleCreatePayment = async () => {
  creating.value = true
  try {
    const res = await api.post('/payments', {
      orderNo: orderNo,
      paymentMethod: selectedMethod.value
    })
    if (res.code === 200) {
      payment.value = res.data
      if (res.data.paymentStatus === 'SUCCESS') {
        ElMessage.success('支付成功！')
      } else if (res.data.paymentStatus === 'PENDING') {
        remainingSeconds.value = res.data.remainingSeconds
        startCountdown()
      }
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    creating.value = false
  }
}

const handlePay = async () => {
  paying.value = true
  try {
    const res = await api.put(`/payments/${payment.value.paymentNo}/pay`)
    if (res.code === 200) {
      payment.value = res.data
      ElMessage.success('支付成功！')
      stopCountdown()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    paying.value = false
  }
}

const startCountdown = () => {
  stopCountdown()
  countdownTimer = setInterval(() => {
    if (remainingSeconds.value <= 0) {
      stopCountdown()
      payment.value.paymentStatus = 'EXPIRED'
      ElMessage.warning('支付已超时')
      return
    }
    remainingSeconds.value--
  }, 1000)
}

const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

onMounted(async () => {
  await fetchOrder()
  await fetchExistingPayment()
})

onUnmounted(() => {
  stopCountdown()
})
</script>

<style lang="scss" scoped>
.payment-page {
  padding: 32px 0;
  min-height: 80vh;
}

.page-inner {
  max-width: 800px;
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

.order-summary {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
  margin-bottom: 16px;

  .summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;

    &:not(:last-child) {
      border-bottom: 1px solid var(--border);
    }
  }

  .label {
    color: var(--text-secondary);
    font-size: 14px;
  }

  .value {
    font-weight: 500;
  }

  .amount {
    font-size: 24px;
    font-weight: 800;
    color: var(--primary);
  }
}

.countdown-bar {
  background: linear-gradient(135deg, #f0fdf9, #ecfdf5);
  border: 1px solid var(--primary-light);
  border-radius: var(--radius-sm);
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  color: var(--primary-dark);
  font-size: 14px;

  b {
    font-size: 18px;
    color: var(--primary);
  }
}

.payment-methods {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
  margin-bottom: 20px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
  }
}

.method-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.method-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border: 2px solid var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: var(--transition);
  position: relative;

  &:hover {
    border-color: var(--primary-light);
    background: #f0fdf9;
  }

  &.active {
    border-color: var(--primary);
    background: #f0fdf9;
    box-shadow: 0 0 0 3px rgba(0, 212, 170, 0.1);
  }

  .method-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: var(--gradient-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;
  }

  .method-info {
    flex: 1;

    h4 {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 2px;
    }

    p {
      font-size: 12px;
      color: var(--text-light);
    }
  }

  .method-check {
    color: var(--primary);
    font-size: 22px;
  }
}

.payment-action {
  text-align: center;
  padding: 20px 0;
}

.pay-btn {
  width: 100%;
  max-width: 400px;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius);
  background: var(--gradient-primary);
  border: none;
}

.simulate-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.simulate-card {
  background: white;
  border-radius: var(--radius);
  padding: 32px;
  box-shadow: var(--shadow);
  width: 100%;
  text-align: center;
}

.qr-mockup {
  .qr-box {
    width: 200px;
    height: 200px;
    border: 2px dashed var(--primary);
    border-radius: var(--radius);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    color: var(--primary);
    gap: 8px;

    p {
      font-size: 14px;
      font-weight: 500;
    }

    .qr-hint {
      font-size: 12px;
      color: var(--text-light);
    }
  }
}

.bank-mockup {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: var(--primary);
  padding: 20px;

  p {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .bank-hint {
    font-size: 14px;
    color: var(--text-secondary);
  }
}

.simulate-tip {
  font-size: 13px;
  color: var(--text-light);
}

.expired-area, .success-area {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: var(--radius);
  box-shadow: var(--shadow);

  h3 {
    font-size: 20px;
    font-weight: 700;
    margin: 16px 0 8px;
  }

  p {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 24px;
  }
}

.expired-icon {
  color: #faad14;
}

.success-icon {
  color: var(--primary);
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
