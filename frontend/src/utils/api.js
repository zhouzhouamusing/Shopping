import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'

// 熔断器状态
const circuitBreaker = {
  state: 'CLOSED',    // CLOSED / OPEN / HALF_OPEN
  failureCount: 0,
  failureThreshold: 5,
  resetTimeout: 30000,
  lastFailureTime: null,
  halfOpenSuccessCount: 0,
  halfOpenThreshold: 2,

  recordFailure() {
    this.failureCount++
    this.lastFailureTime = Date.now()
    if (this.failureCount >= this.failureThreshold) {
      this.state = 'OPEN'
      ElNotification({
        title: '网络异常',
        message: '服务暂时不可用，系统将在30秒后自动重试',
        type: 'error',
        duration: 5000
      })
    }
  },

  recordSuccess() {
    if (this.state === 'HALF_OPEN') {
      this.halfOpenSuccessCount++
      if (this.halfOpenSuccessCount >= this.halfOpenThreshold) {
        this.reset()
      }
    } else {
      this.failureCount = Math.max(0, this.failureCount - 1)
    }
  },

  canRequest() {
    if (this.state === 'CLOSED') return true
    if (this.state === 'OPEN') {
      if (Date.now() - this.lastFailureTime >= this.resetTimeout) {
        this.state = 'HALF_OPEN'
        this.halfOpenSuccessCount = 0
        return true
      }
      return false
    }
    return true // HALF_OPEN
  },

  reset() {
    this.state = 'CLOSED'
    this.failureCount = 0
    this.halfOpenSuccessCount = 0
    this.lastFailureTime = null
  }
}

// 重试配置
const retryConfig = {
  maxRetries: 2,
  retryDelay: 1000,
  retryableStatuses: [408, 429, 500, 502, 503, 504],
  retryableMethods: ['get', 'put']
}

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms))

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器
api.interceptors.request.use(config => {
  // 熔断器检查
  if (!circuitBreaker.canRequest()) {
    return Promise.reject({
      __circuitOpen: true,
      message: '服务熔断中，请稍后重试'
    })
  }

  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  // 标记重试次数
  config.__retryCount = config.__retryCount || 0

  return config
}, error => Promise.reject(error))

// 响应拦截器
api.interceptors.response.use(
  response => {
    circuitBreaker.recordSuccess()
    return response.data
  },
  async error => {
    // 熔断器已打开导致的拒绝
    if (error.__circuitOpen) {
      ElMessage.warning('系统正在恢复中，请稍后重试')
      return Promise.reject(error)
    }

    const config = error.config

    // 网络超时处理
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      circuitBreaker.recordFailure()
      // 自动重试
      if (config && config.__retryCount < retryConfig.maxRetries &&
          retryConfig.retryableMethods.includes(config.method)) {
        config.__retryCount++
        await sleep(retryConfig.retryDelay * config.__retryCount)
        return api(config)
      }
      ElMessage.error('请求超时，请检查网络后重试')
      return Promise.reject(error)
    }

    // 网络断开
    if (!error.response) {
      circuitBreaker.recordFailure()
      if (config && config.__retryCount < retryConfig.maxRetries &&
          retryConfig.retryableMethods.includes(config.method)) {
        config.__retryCount++
        await sleep(retryConfig.retryDelay * config.__retryCount)
        return api(config)
      }
      ElNotification({
        title: '网络连接失败',
        message: '无法连接到服务器，请检查网络连接是否正常',
        type: 'error',
        duration: 4000
      })
      return Promise.reject(error)
    }

    const { status, data } = error.response

    // 服务端错误自动重试
    if (retryConfig.retryableStatuses.includes(status) && config) {
      circuitBreaker.recordFailure()
      if (config.__retryCount < retryConfig.maxRetries &&
          retryConfig.retryableMethods.includes(config.method)) {
        config.__retryCount++
        await sleep(retryConfig.retryDelay * config.__retryCount)
        return api(config)
      }
    }

    // 按状态码分类处理
    switch (status) {
      case 401:
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
        ElMessage.error('登录已过期，请重新登录')
        break
      case 403:
        ElMessage.error('无权限访问该资源')
        break
      case 404:
        ElMessage.error(data?.message || '请求的资源不存在')
        break
      case 409:
        ElMessage.warning(data?.message || '操作冲突，请刷新后重试')
        break
      case 429:
        ElMessage.warning('请求过于频繁，请稍后再试')
        break
      case 500:
        ElMessage.error('服务器内部错误，请稍后重试')
        break
      case 502:
      case 503:
        ElMessage.error('服务暂时不可用，正在恢复中')
        break
      case 504:
        ElMessage.error('网关超时，请稍后重试')
        break
      default:
        ElMessage.error(data?.message || `请求失败 (${status})`)
    }

    return Promise.reject(error)
  }
)

export default api

export { circuitBreaker }
