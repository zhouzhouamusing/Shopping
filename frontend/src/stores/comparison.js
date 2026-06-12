import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

export const useComparisonStore = defineStore('comparison', () => {
  const compareIds = ref([])
  const compareProducts = ref([])

  const count = computed(() => compareIds.value.length)
  const isFull = computed(() => compareIds.value.length >= 4)

  async function fetchCompareList() {
    try {
      const res = await api.get('/compare')
      if (res.code === 200) {
        compareProducts.value = res.data
        compareIds.value = res.data.map(item => item.productId)
      }
    } catch (e) {
      // silently fail
    }
  }

  async function addToCompare(productId) {
    if (compareIds.value.includes(productId)) {
      ElMessage.warning('该商品已在对比列表中')
      return
    }
    if (isFull.value) {
      ElMessage.warning('最多只能对比4个商品')
      return
    }
    try {
      const res = await api.post('/compare', null, { params: { productId } })
      if (res.code === 200) {
        compareIds.value.push(productId)
        ElMessage.success('已加入对比')
      } else {
        ElMessage.warning(res.message)
      }
    } catch (e) {
      // handled by interceptor
    }
  }

  async function removeFromCompare(productId) {
    try {
      const res = await api.delete(`/compare/${productId}`)
      if (res.code === 200) {
        compareIds.value = compareIds.value.filter(id => id !== productId)
        compareProducts.value = compareProducts.value.filter(item => item.productId !== productId)
      }
    } catch (e) {
      // handled by interceptor
    }
  }

  async function clearAll() {
    try {
      const res = await api.delete('/compare')
      if (res.code === 200) {
        compareIds.value = []
        compareProducts.value = []
      }
    } catch (e) {
      // handled by interceptor
    }
  }

  function isInCompare(productId) {
    return compareIds.value.includes(productId)
  }

  async function fetchCompareProducts() {
    await fetchCompareList()
    return compareProducts.value.map(item => item.product).filter(Boolean)
  }

  return { compareIds, compareProducts, count, isFull, addToCompare, removeFromCompare, clearAll, isInCompare, fetchCompareList, fetchCompareProducts }
})
