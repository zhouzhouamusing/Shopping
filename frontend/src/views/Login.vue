<template>
  <div class="auth-page login-page">
    <!-- 动态背景粒子效果 -->
    <div class="bg-particles">
      <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>

    <!-- 渐变背景光效 -->
    <div class="bg-gradient-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="auth-container animate-scaleIn">
      <div class="auth-card">
        <!-- 左侧品牌区域 -->
        <div class="auth-brand">
          <div class="brand-content">
            <div class="brand-icon animate-fadeInDown">
              <el-icon :size="48"><ShoppingCart /></el-icon>
            </div>
            <h1 class="brand-title">欢迎回来</h1>
            <p class="brand-subtitle">登录您的账户，探索无限精彩</p>
            <div class="brand-features">
              <div class="feature-item">
                <el-icon><CircleCheck /></el-icon>
                <span>海量正品好货</span>
              </div>
              <div class="feature-item">
                <el-icon><CircleCheck /></el-icon>
                <span>极速物流配送</span>
              </div>
              <div class="feature-item">
                <el-icon><CircleCheck /></el-icon>
                <span>无忧售后保障</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧表单区域 -->
        <div class="auth-form-section">
          <div class="form-header">
            <h2>账号登录</h2>
            <p>使用您的账号和密码登录</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="auth-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                size="large"
                placeholder="请输入用户名"
                prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                size="large"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="submit-btn"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '立即登录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="auth-footer">
            <p>还没有账号？<router-link to="/register" class="link">立即注册</router-link></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userStore.login(form)
    if (res.code === 200) {
      ElMessage.success('登录成功！')
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const getParticleStyle = (i) => ({
  left: `${Math.random() * 100}%`,
  top: `${Math.random() * 100}%`,
  animationDelay: `${Math.random() * 5}s`,
  animationDuration: `${3 + Math.random() * 4}s`,
  width: `${2 + Math.random() * 4}px`,
  height: `${2 + Math.random() * 4}px`
})
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: #0f0f23;
}

/* 粒子背景 */
.bg-particles {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.particle {
  position: absolute;
  background: rgba(255, 77, 79, 0.4);
  border-radius: 50%;
  animation: float infinite ease-in-out;
}

/* 渐变光球 */
.bg-gradient-orbs {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 8s infinite ease-in-out;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: #ff4d4f;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: #6c63ff;
  bottom: -50px;
  left: -50px;
  animation-delay: 2s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: #00d2ff;
  top: 50%;
  left: 50%;
  animation-delay: 4s;
}

.auth-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 900px;
  margin: 20px;
}

.auth-card {
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);

  @media (max-width: 768px) {
    flex-direction: column;
  }
}

/* 品牌区域 */
.auth-brand {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 48px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 30% 70%, rgba(255, 77, 79, 0.15) 0%, transparent 50%),
                radial-gradient(circle at 70% 30%, rgba(108, 99, 255, 0.15) 0%, transparent 50%);
  }

  @media (max-width: 768px) {
    padding: 32px 24px;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.brand-icon {
  width: 80px;
  height: 80px;
  background: var(--gradient-primary);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  animation: pulse-glow 3s infinite;
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
}

.brand-subtitle {
  font-size: 15px;
  opacity: 0.7;
  margin-bottom: 32px;
}

.brand-features {
  text-align: left;
  display: inline-block;

  .feature-item {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
    font-size: 14px;
    opacity: 0.8;

    .el-icon {
      color: #4ecdc4;
    }
  }
}

/* 表单区域 */
.auth-form-section {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  @media (max-width: 768px) {
    padding: 32px 24px;
  }
}

.form-header {
  margin-bottom: 32px;

  h2 {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8px;
  }

  p {
    color: var(--text-light);
    font-size: 14px;
  }
}

.auth-form {
  .el-form-item {
    margin-bottom: 20px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding: 4px 16px;
    box-shadow: 0 0 0 1px var(--border);
    transition: var(--transition);

    &:focus-within {
      box-shadow: 0 0 0 2px var(--primary);
    }
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: var(--gradient-primary);
  border: none;
  transition: var(--transition);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(255, 77, 79, 0.4);
  }
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
  color: var(--text-secondary);
  font-size: 14px;

  .link {
    color: var(--primary);
    font-weight: 600;
    transition: var(--transition);

    &:hover {
      color: var(--primary-dark);
    }
  }
}
</style>
