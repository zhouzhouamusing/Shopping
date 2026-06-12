import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

export const useComparisonStore = defineStore('comparison', () => {
  const compareIds = ref(JSON.parse(localStorage.getItem('compareIds') || '[]'))

  const count = computed(() => compareIds.value.length)
  const isFull = computed(() => compareIds.value.length >= 4)

  function addToCompare(productId) {
    if (compareIds.value.includes(productId)) {
      ElMessage.warning('该商品已在对比列表中')
      return
    }
    if (isFull.value) {
      ElMessage.warning('最多只能对比4个商品')
      return
    }
    compareIds.value.push(productId)
    persist()
    ElMessage.success('已加入对比')
  }

  function removeFromCompare(productId) {
    compareIds.value = compareIds.value.filter(id => id !== productId)
    persist()
  }

  function clearAll() {
    compareIds.value = []
    persist()
  }

  function isInCompare(productId) {
    return compareIds.value.includes(productId)
  }

  async function fetchCompareProducts() {
    if (compareIds.value.length === 0) return []
    try {
      const res = await api.get('/products/batch', { params: { ids: compareIds.value.join(',') } })
      if (res.code === 200) {
        return res.data
      }
    } catch (e) {
      // silently fail
    }
    return []
  }

  function persist() {
    localStorage.setItem('compareIds', JSON.stringify(compareIds.value))
  }

  return { compareIds, count, isFull, addToCompare, removeFromCompare, clearAll, isInCompare, fetchCompareProducts }
})
