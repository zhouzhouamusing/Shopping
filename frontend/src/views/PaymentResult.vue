<template>
  <div class="payment-result-page">
    <div class="page-inner">
      <div class="result-card" v-if="payment">
        <!-- 成功状态 -->
        <template v-if="payment.paymentStatus === 'SUCCESS'">
          <div class="result-icon success">
            <el-icon :size="64"><CircleCheckFilled /></el-icon>
          </div>
          <h2>支付成功</h2>
          <div class="result-info">
            <div class="info-row">
              <span>支付单号</span>
              <span>{{ payment.paymentNo }}</span>
            </div>
            <div class="info-row">
              <span>订单编号</span>
              <span>{{ payment.orderNo }}</span>
            </div>
            <div class="info-row">
              <span>支付金额</span>
              <span class="amount">¥{{ payment.paymentAmount }}</span>
            </div>
            <div class="info-row">
              <span>支付方式</span>
              <span>{{ getMethodName(payment.paymentMethod) }}</span>
            </div>
            <div class="info-row">
              <span>支付时间</span>
              <span>{{ formatTime(payment.payTime) }}</span>
            </div>
          </div>
        </template>

        <!-- 失败/超时状态 -->
        <template v-else>
          <div class="result-icon failed">
            <el-icon :size="64"><CircleCloseFilled /></el-icon>
          </div>
          <h2>支付失败</h2>
          <p class="error-msg">{{ payment.paymentStatus === 'EXPIRED' ? '支付超时，订单已自动取消' : '支付处理失败，请重试' }}</p>
        </template>

        <div class="result-actions">
          <el-button type="primary" @click="$router.push({ name: 'OrderDetail', params: { orderNo: payment.orderNo } })">
            查看订单
          </el-button>
          <el-button @click="$router.push('/products')">继续购物</el-button>
        </div>
      </div>

      <div v-else v-loading="loading" class="loading-area"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/utils/api'

const route = useRoute()
const paymentNo = route.params.paymentNo
const payment = ref(null)
const loading = ref(true)

const getMethodName = (method) => {
  const map = { ALIPAY: '支付宝', WECHAT: '微信支付', BANK_CARD: '银行卡', COD: '货到付款' }
  return map[method] || method
}

const formatTime = (time) => time ? new Date(time).toLocaleString('zh-CN') : ''

const fetchPayment = async () => {
  loading.value = true
  try {
    const res = await api.get(`/payments/${paymentNo}`)
    if (res.code === 200) {
      payment.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchPayment)
</script>

<style lang="scss" scoped>
.payment-result-page {
  padding: 60px 0;
  min-height: 70vh;
}

.page-inner {
  max-width: 560px;
  margin: 0 auto;
  padding: 0 24px;
}

.result-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 48px 40px;
  box-shadow: var(--shadow);
  text-align: center;

  h2 {
    font-size: 22px;
    font-weight: 700;
    margin: 20px 0 8px;
  }

  .error-msg {
    color: var(--text-secondary);
    font-size: 14px;
    margin-bottom: 24px;
  }
}

.result-icon {
  &.success {
    color: var(--primary);
  }

  &.failed {
    color: #f56c6c;
  }
}

.result-info {
  text-align: left;
  background: var(--bg-gray);
  border-radius: var(--radius);
  padding: 20px 24px;
  margin: 24px 0;

  .info-row {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    font-size: 14px;
    border-bottom: 1px solid var(--border);

    &:last-child {
      border-bottom: none;
    }

    span:first-child {
      color: var(--text-secondary);
    }

    .amount {
      font-weight: 700;
      color: var(--primary);
      font-size: 16px;
    }
  }
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.loading-area {
  min-height: 300px;
}
</style>
