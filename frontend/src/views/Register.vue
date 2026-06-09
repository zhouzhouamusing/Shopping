<template>
  <div class="auth-page register-page">
    <!-- 动态背景 -->
    <div class="bg-particles">
      <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>
    <div class="bg-gradient-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <!-- 注册卡片 -->
    <div class="auth-container animate-scaleIn">
      <div class="auth-card">
        <!-- 左侧品牌 -->
        <div class="auth-brand">
          <div class="brand-content">
            <div class="brand-icon animate-fadeInDown">
              <el-icon :size="48"><UserFilled /></el-icon>
            </div>
            <h1 class="brand-title">加入我们</h1>
            <p class="brand-subtitle">创建账户，开启品质购物之旅</p>
            <div class="brand-features">
              <div class="feature-item">
                <el-icon><CircleCheck /></el-icon>
                <span>注册即享新人礼包</span>
              </div>
              <div class="feature-item">
                <el-icon><CircleCheck /></el-icon>
                <span>专属会员折扣优惠</span>
              </div>
              <div class="feature-item">
                <el-icon><CircleCheck /></el-icon>
                <span>积分兑换丰富好礼</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧表单 -->
        <div class="auth-form-section">
          <div class="form-header">
            <h2>创建账号</h2>
            <p>填写信息完成注册</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="auth-form"
            @submit.prevent="handleRegister"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                size="large"
                placeholder="设置用户名（3-20个字符）"
                prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                size="large"
                type="password"
                placeholder="设置密码（6位以上）"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                size="large"
                type="password"
                placeholder="确认密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="email">
              <el-input
                v-model="form.email"
                size="large"
                placeholder="邮箱地址（可选）"
                prefix-icon="Message"
              />
            </el-form-item>

            <el-form-item prop="nickname">
              <el-input
                v-model="form.nickname"
                size="large"
                placeholder="昵称（可选）"
                prefix-icon="Star"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="submit-btn"
                @click="handleRegister"
              >
                {{ loading ? '注册中...' : '立即注册' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="auth-footer">
            <p>已有账号？<router-link to="/login" class="link">立即登录</router-link></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  nickname: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userStore.register({
      username: form.username,
      password: form.password,
      email: form.email || undefined,
      nickname: form.nickname || undefined
    })
    if (res.code === 200) {
      ElMessage.success('注册成功！')
      router.push('/')
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('注册失败，请稍后重试')
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

.bg-particles {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.particle {
  position: absolute;
  background: rgba(108, 99, 255, 0.4);
  border-radius: 50%;
  animation: float infinite ease-in-out;
}

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
  width: 350px;
  height: 350px;
  background: #6c63ff;
  top: -80px;
  left: -80px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: #ff4d4f;
  bottom: -80px;
  right: -80px;
  animation-delay: 2s;
}

.orb-3 {
  width: 200px;
  height: 200px;
  background: #4ecdc4;
  top: 40%;
  right: 30%;
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

  @media (max-width: 768px) {
    flex-direction: column;
  }
}

.auth-brand {
  width: 360px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #16213e 0%, #0f3460 50%, #1a1a2e 100%);
  padding: 48px 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 50% 80%, rgba(108, 99, 255, 0.2) 0%, transparent 60%);
  }

  @media (max-width: 768px) {
    width: 100%;
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
  background: var(--gradient-accent);
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

.auth-form-section {
  flex: 1;
  padding: 40px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  @media (max-width: 768px) {
    padding: 32px 24px;
  }
}

.form-header {
  margin-bottom: 28px;

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
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding: 4px 16px;
    box-shadow: 0 0 0 1px var(--border);
    transition: var(--transition);

    &:focus-within {
      box-shadow: 0 0 0 2px var(--accent);
    }
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: var(--gradient-accent);
  border: none;
  transition: var(--transition);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(108, 99, 255, 0.4);
  }
}

.auth-footer {
  text-align: center;
  margin-top: 20px;
  color: var(--text-secondary);
  font-size: 14px;

  .link {
    color: var(--accent);
    font-weight: 600;
    transition: var(--transition);

    &:hover {
      color: var(--primary-dark);
    }
  }
}
</style>
