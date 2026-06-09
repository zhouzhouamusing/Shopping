import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const selectedItems = computed(() => items.value.filter(item => item.selected === 1))
  const totalPrice = computed(() => {
    return selectedItems.value.reduce((sum, item) => {
      return sum + (item.product?.price || 0) * item.quantity
    }, 0)
  })

  // 获取购物车列表
  async function fetchCart() {
    loading.value = true
    try {
      const res = await api.get('/cart')
      if (res.code === 200) {
        items.value = res.data
      }
    } finally {
      loading.value = false
    }
  }

  // 添加到购物车
  async function addToCart(productId, quantity = 1) {
    const res = await api.post('/cart', { productId, quantity })
    if (res.code === 200) {
      await fetchCart()
    }
    return res
  }

  // 更新数量
  async function updateQuantity(productId, quantity) {
    const res = await api.put(`/cart/${productId}?quantity=${quantity}`)
    if (res.code === 200) {
      const item = items.value.find(i => i.productId === productId)
      if (item) item.quantity = quantity
    }
    return res
  }

  // 删除商品
  async function removeItem(productId) {
    const res = await api.delete(`/cart/${productId}`)
    if (res.code === 200) {
      items.value = items.value.filter(i => i.productId !== productId)
    }
    return res
  }

  // 切换选中
  async function toggleSelect(productId, selected) {
    await api.put(`/cart/${productId}/select?selected=${selected}`)
    const item = items.value.find(i => i.productId === productId)
    if (item) item.selected = selected
  }

  // 全选
  async function selectAll(selected) {
    await api.put(`/cart/select-all?selected=${selected}`)
    items.value.forEach(item => item.selected = selected)
  }

  return { items, loading, totalCount, selectedItems, totalPrice, fetchCart, addToCart, updateQuantity, removeItem, toggleSelect, selectAll }
})
