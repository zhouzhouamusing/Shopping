<template>
  <div class="cart-page">
    <div class="page-inner">
      <h1 class="page-title">
        <el-icon><ShoppingCart /></el-icon>
        我的购物车
        <span class="count-badge" v-if="cartStore.items.length">{{ cartStore.items.length }}件商品</span>
      </h1>

      <div v-if="cartStore.items.length > 0" class="cart-content">
        <!-- 购物车列表 -->
        <div class="cart-list">
          <div class="cart-header">
            <el-checkbox
              :model-value="isAllSelected"
              @change="handleSelectAll"
            >全选</el-checkbox>
            <span class="col-product">商品信息</span>
            <span class="col-price">单价</span>
            <span class="col-quantity">数量</span>
            <span class="col-total">小计</span>
            <span class="col-action">操作</span>
          </div>

          <transition-group name="list" tag="div">
            <div
              v-for="item in cartStore.items"
              :key="item.id"
              class="cart-item"
            >
              <el-checkbox
                :model-value="item.selected === 1"
                @change="(val) => cartStore.toggleSelect(item.productId, val ? 1 : 0)"
              />
              <div class="item-product" @click="goDetail(item.productId)">
                <img :src="item.product?.mainImage" :alt="item.product?.name" />
                <div class="item-info">
                  <h4>{{ item.product?.name }}</h4>
                </div>
              </div>
              <div class="item-price">
                <span class="price">¥{{ item.product?.price }}</span>
              </div>
              <div class="item-quantity">
                <el-input-number
                  :model-value="item.quantity"
                  :min="1"
                  :max="item.product?.stock || 99"
                  size="small"
                  @change="(val) => cartStore.updateQuantity(item.productId, val)"
                />
              </div>
              <div class="item-total">
                <span class="price">¥{{ (item.product?.price * item.quantity).toFixed(2) }}</span>
              </div>
              <div class="item-action">
                <el-button
                  text
                  type="danger"
                  @click="handleRemove(item)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </transition-group>
        </div>

        <!-- 结算栏 -->
        <div class="cart-footer">
          <div class="footer-left">
            <el-checkbox
              :model-value="isAllSelected"
              @change="handleSelectAll"
            >全选</el-checkbox>
            <span class="selected-info">
              已选 <b>{{ cartStore.selectedItems.length }}</b> 件
            </span>
          </div>
          <div class="footer-right">
            <div class="total-info">
              <span>合计：</span>
              <span class="total-price">
                <span class="symbol">¥</span>{{ cartStore.totalPrice.toFixed(2) }}
              </span>
            </div>
            <el-button
              type="primary"
              size="large"
              class="checkout-btn"
              :disabled="cartStore.selectedItems.length === 0"
              @click="goCheckout"
            >
              去结算({{ cartStore.selectedItems.length }})
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空购物车 -->
      <div class="empty-cart" v-else>
        <el-icon class="empty-icon" :size="80"><ShoppingCart /></el-icon>
        <p class="empty-text">购物车还是空的</p>
        <p class="empty-hint">快去挑选心仪的商品吧～</p>
        <router-link to="/products">
          <el-button type="primary" size="large">去逛逛</el-button>
        </router-link>
      </div>

      <!-- 结算对话框 -->
      <el-dialog v-model="showCheckout" title="确认订单" width="500px">
        <el-form :model="orderForm" :rules="orderRules" ref="orderFormRef" label-width="80px">
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="orderForm.receiverName" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="receiverPhone">
            <el-input v-model="orderForm.receiverPhone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="收货地址" prop="receiverAddress">
            <el-input v-model="orderForm.receiverAddress" type="textarea" rows="2" placeholder="请输入详细收货地址" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="orderForm.remark" placeholder="选填" />
          </el-form-item>
        </el-form>
        <div class="checkout-summary">
          <p>共 {{ cartStore.selectedItems.length }} 件商品</p>
          <p class="checkout-total">应付金额：<span>¥{{ cartStore.totalPrice.toFixed(2) }}</span></p>
        </div>
        <template #footer>
          <el-button @click="showCheckout = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitOrder">提交订单</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const cartStore = useCartStore()

const showCheckout = ref(false)
const submitting = ref(false)
const orderFormRef = ref(null)
const orderForm = ref({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  remark: ''
})

const orderRules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  receiverAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const isAllSelected = computed(() => {
  return cartStore.items.length > 0 && cartStore.items.every(i => i.selected === 1)
})

const handleSelectAll = (val) => {
  cartStore.selectAll(val ? 1 : 0)
}

const handleRemove = (item) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' })
    .then(() => {
      cartStore.removeItem(item.productId)
      ElMessage.success('已删除')
    })
    .catch(() => {})
}

const goDetail = (id) => {
  router.push({ name: 'ProductDetail', params: { id } })
}

const goCheckout = () => {
  showCheckout.value = true
}

const submitOrder = async () => {
  const valid = await orderFormRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await api.post('/orders', orderForm.value)
    if (res.code === 200) {
      ElMessage.success('订单创建成功！')
      showCheckout.value = false
      cartStore.fetchCart()
      router.push({ name: 'OrderDetail', params: { orderNo: res.data.orderNo } })
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  cartStore.fetchCart()
})
</script>

<style lang="scss" scoped>
.cart-page {
  padding: 32px 0;
}

.page-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;

  .count-badge {
    font-size: 14px;
    font-weight: 400;
    color: var(--text-light);
  }
}

.cart-list {
  background: white;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
}

.cart-header {
  display: grid;
  grid-template-columns: 40px 1fr 100px 140px 100px 60px;
  align-items: center;
  padding: 16px 24px;
  background: var(--bg-gray);
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;

  @media (max-width: 768px) {
    display: none;
  }
}

.cart-item {
  display: grid;
  grid-template-columns: 40px 1fr 100px 140px 100px 60px;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
  transition: var(--transition);

  &:hover {
    background: #fafbfc;
  }

  @media (max-width: 768px) {
    grid-template-columns: 30px 1fr auto;
    gap: 8px;
  }
}

.item-product {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;

  img {
    width: 80px;
    height: 80px;
    border-radius: var(--radius-sm);
    object-fit: cover;
  }

  h4 {
    font-size: 14px;
    font-weight: 500;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.item-price, .item-total {
  text-align: center;
}

.item-quantity {
  display: flex;
  justify-content: center;
}

.item-action {
  text-align: center;
}

/* 结算栏 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 20px 24px;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  margin-top: 16px;
  position: sticky;
  bottom: 0;
  z-index: 10;

  @media (max-width: 768px) {
    flex-direction: column;
    gap: 12px;
  }
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.selected-info {
  font-size: 14px;
  color: var(--text-secondary);

  b {
    color: var(--primary);
  }
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-info {
  font-size: 14px;
}

.total-price {
  color: var(--primary);
  font-size: 24px;
  font-weight: 800;

  .symbol {
    font-size: 16px;
  }
}

.checkout-btn {
  height: 48px;
  padding: 0 40px;
  border-radius: var(--radius);
  font-size: 16px;
  font-weight: 600;
  background: var(--gradient-primary);
  border: none;
}

/* 空购物车 */
.empty-cart {
  text-align: center;
  padding: 80px 20px;

  .empty-icon {
    color: #ddd;
    margin-bottom: 20px;
  }

  .empty-text {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 8px;
  }

  .empty-hint {
    color: var(--text-light);
    margin-bottom: 24px;
  }
}

/* 结算弹窗 */
.checkout-summary {
  background: var(--bg-gray);
  padding: 16px;
  border-radius: var(--radius-sm);
  margin-top: 16px;

  p {
    font-size: 14px;
    color: var(--text-secondary);
  }

  .checkout-total {
    margin-top: 8px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);

    span {
      color: var(--primary);
      font-size: 20px;
    }
  }
}

/* 列表动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.4s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
