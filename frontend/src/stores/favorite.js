import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/utils/api'

export const useFavoriteStore = defineStore('favorite', () => {
  const favoriteIds = ref(new Set())
  const loaded = ref(false)

  async function loadFavoriteIds() {
    try {
      const res = await api.get('/favorites/ids')
      if (res.code === 200) {
        favoriteIds.value = new Set(res.data)
        loaded.value = true
      }
    } catch (e) {
      // silently fail
    }
  }

  function isFavorited(productId) {
    return favoriteIds.value.has(productId)
  }

  async function toggleFavorite(productId) {
    try {
      const res = await api.post('/favorites/toggle', null, { params: { productId } })
      if (res.code === 200) {
        if (res.data.favorited) {
          favoriteIds.value.add(productId)
        } else {
          favoriteIds.value.delete(productId)
        }
        return res.data.favorited
      }
    } catch (e) {
      // handled by interceptor
    }
    return null
  }

  function addFavoriteId(productId) {
    favoriteIds.value.add(productId)
  }

  function removeFavoriteId(productId) {
    favoriteIds.value.delete(productId)
  }

  function reset() {
    favoriteIds.value = new Set()
    loaded.value = false
  }

  return { favoriteIds, loaded, loadFavoriteIds, isFavorited, toggleFavorite, addFavoriteId, removeFavoriteId, reset }
})
