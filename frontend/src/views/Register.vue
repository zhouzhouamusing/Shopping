<template>
  <div class="auth-page register-page">
    <!-- 动态背景 -->
    <div class="bg-particles">
      <div v-for="i in 30" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>
    <div class="bg-gradient-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <!-- 电路板网格 -->
    <div class="circuit-grid"></div>

    <!-- 扫描线 -->
    <div class="scan-line"></div>

    <!-- 科技感几何图形 -->
    <div class="tech-shapes">
      <div class="hex hex-1"></div>
      <div class="hex hex-2"></div>
      <div class="diamond diamond-1"></div>
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
      ElMessage.success('注册成功，请登录！')
      userStore.logout()
      router.push('/login')
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
  background: #0a192f;
}

/* 电路板网格 */
.circuit-grid {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-image:
    linear-gradient(rgba(14, 165, 233, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(14, 165, 233, 0.06) 1px, transparent 1px),
    linear-gradient(rgba(0, 212, 170, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 170, 0.04) 1px, transparent 1px);
  background-size: 80px 80px, 80px 80px, 20px 20px, 20px 20px;
  animation: grid-pulse 8s ease-in-out infinite;
}

@keyframes grid-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

/* 扫描线 */
.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.6), transparent);
  z-index: 1;
  animation: scan-down 7s linear infinite;
  box-shadow: 0 0 20px rgba(14, 165, 233, 0.3), 0 0 60px rgba(14, 165, 233, 0.1);
}

@keyframes scan-down {
  0% { top: -5%; }
  100% { top: 105%; }
}

/* 科技感几何 */
.tech-shapes {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.hex {
  position: absolute;
  width: 90px;
  height: 90px;
  border: 1px solid rgba(14, 165, 233, 0.2);
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  animation: hex-float 12s ease-in-out infinite;
}

.hex-1 {
  top: 12%;
  left: 8%;
  animation-delay: 0s;
}

.hex-2 {
  bottom: 10%;
  right: 12%;
  width: 70px;
  height: 70px;
  animation-delay: 4s;
  border-color: rgba(0, 212, 170, 0.2);
}

.diamond-1 {
  position: absolute;
  top: 40%;
  right: 8%;
  width: 50px;
  height: 50px;
  border: 1px solid rgba(14, 165, 233, 0.15);
  transform: rotate(45deg);
  animation: diamond-spin 15s linear infinite;
}

@keyframes hex-float {
  0%, 100% { transform: rotate(0deg) translateY(0); opacity: 0.3; }
  25% { transform: rotate(30deg) translateY(-15px); opacity: 0.7; }
  50% { transform: rotate(60deg) translateY(0); opacity: 0.4; }
  75% { transform: rotate(90deg) translateY(10px); opacity: 0.6; }
}

@keyframes diamond-spin {
  0% { transform: rotate(45deg) scale(1); opacity: 0.3; }
  50% { transform: rotate(225deg) scale(1.2); opacity: 0.6; }
  100% { transform: rotate(405deg) scale(1); opacity: 0.3; }
}

.bg-particles {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.particle {
  position: absolute;
  background: rgba(14, 165, 233, 0.4);
  border-radius: 50%;
  animation: float infinite ease-in-out;

  &:nth-child(odd) {
    background: rgba(0, 212, 170, 0.4);
    animation-name: particle-drift;
  }

  &:nth-child(3n) {
    box-shadow: 0 0 6px rgba(14, 165, 233, 0.6);
  }
}

@keyframes particle-drift {
  0%, 100% { transform: translate(0, 0) scale(1); opacity: 0.4; }
  33% { transform: translate(20px, -30px) scale(1.3); opacity: 0.8; }
  66% { transform: translate(-10px, 20px) scale(0.8); opacity: 0.5; }
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
  animation: morph-blob 8s infinite ease-in-out;
}

.orb-1 {
  width: 350px;
  height: 350px;
  background: #0ea5e9;
  top: -80px;
  left: -80px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: #00d4aa;
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

@keyframes morph-blob {
  0%, 100% {
    border-radius: 40% 60% 60% 40% / 60% 30% 70% 40%;
    transform: translate(0, 0) scale(1);
  }
  25% {
    border-radius: 60% 40% 30% 70% / 40% 60% 70% 30%;
    transform: translate(10px, -20px) scale(1.05);
  }
  50% {
    border-radius: 30% 60% 70% 40% / 50% 60% 30% 60%;
    transform: translate(-10px, 10px) scale(0.95);
  }
  75% {
    border-radius: 50% 40% 60% 50% / 30% 70% 40% 60%;
    transform: translate(15px, 5px) scale(1.02);
  }
}

.auth-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 900px;
  margin: 20px;
  perspective: 1200px;
}

.auth-card {
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  overflow: hidden;
  box-shadow:
    0 25px 60px rgba(0, 0, 0, 0.3),
    0 0 40px rgba(14, 165, 233, 0.1),
    inset 0 0 0 1px rgba(255, 255, 255, 0.1);
  transition: transform 0.6s cubic-bezier(0.23, 1, 0.32, 1);

  &:hover {
    transform: rotateY(1deg) rotateX(-1deg) translateZ(10px);
    box-shadow:
      0 30px 70px rgba(0, 0, 0, 0.35),
      0 0 60px rgba(14, 165, 233, 0.15);
  }

  @media (max-width: 768px) {
    flex-direction: column;
    &:hover { transform: none; }
  }
}

.auth-brand {
  width: 360px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #0f2744 0%, #132f4c 50%, #0a192f 100%);
  padding: 48px 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 50% 80%, rgba(14, 165, 233, 0.2) 0%, transparent 60%);
  }

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(14, 165, 233, 0.05) 1px, transparent 1px),
      linear-gradient(90deg, rgba(14, 165, 233, 0.05) 1px, transparent 1px);
    background-size: 30px 30px;
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
  box-shadow: 0 0 30px rgba(14, 165, 233, 0.3);
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
  text-shadow: 0 0 20px rgba(14, 165, 233, 0.3);
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
      filter: drop-shadow(0 0 4px rgba(78, 205, 196, 0.5));
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
      box-shadow: 0 0 0 2px var(--accent), 0 0 15px rgba(14, 165, 233, 0.15);
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
    box-shadow: 0 8px 25px rgba(14, 165, 233, 0.4);
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
      text-shadow: 0 0 8px rgba(14, 165, 233, 0.3);
    }
  }
}
</style>
