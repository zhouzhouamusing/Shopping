<template>
  <div class="auth-page login-page">
    <!-- 动态背景粒子效果 -->
    <div class="bg-particles">
      <div v-for="i in 30" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>

    <!-- 渐变背景光效 -->
    <div class="bg-gradient-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <!-- 电路板网格 -->
    <div class="circuit-grid"></div>

    <!-- 扫描线 -->
    <div class="scan-line"></div>

    <!-- 流光线条 -->
    <div class="glow-lines">
      <div class="glow-line glow-h-1"></div>
      <div class="glow-line glow-h-2"></div>
      <div class="glow-line glow-v-1"></div>
    </div>

    <!-- 科技感几何图形 -->
    <div class="tech-shapes">
      <div class="hex hex-1"></div>
      <div class="hex hex-2"></div>
      <div class="hex hex-3"></div>
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
      const redirect = route.query.redirect || '/home'
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
  background: #0a192f;
}

/* 电路板网格 */
.circuit-grid {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-image:
    linear-gradient(rgba(0, 212, 170, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 170, 0.06) 1px, transparent 1px),
    linear-gradient(rgba(14, 165, 233, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(14, 165, 233, 0.04) 1px, transparent 1px);
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
  background: linear-gradient(90deg, transparent, rgba(0, 212, 170, 0.6), transparent);
  z-index: 1;
  animation: scan-down 6s linear infinite;
  box-shadow: 0 0 20px rgba(0, 212, 170, 0.3), 0 0 60px rgba(0, 212, 170, 0.1);
}

@keyframes scan-down {
  0% { top: -5%; }
  100% { top: 105%; }
}

/* 流光线条 */
.glow-lines {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

.glow-line {
  position: absolute;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 170, 0.5), transparent);
  animation: trace-h 8s linear infinite;
}

.glow-h-1 {
  top: 25%;
  left: 0;
  right: 0;
  height: 1px;
  animation-delay: 0s;
}

.glow-h-2 {
  top: 75%;
  left: 0;
  right: 0;
  height: 1px;
  animation-delay: 4s;
}

.glow-v-1 {
  left: 30%;
  top: 0;
  bottom: 0;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(14, 165, 233, 0.4), transparent);
  animation: trace-v 10s linear infinite;
}

@keyframes trace-h {
  0% { opacity: 0; transform: translateX(-100%); }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { opacity: 0; transform: translateX(100%); }
}

@keyframes trace-v {
  0% { opacity: 0; transform: translateY(-100%); }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { opacity: 0; transform: translateY(100%); }
}

/* 科技感六边形 */
.tech-shapes {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.hex {
  position: absolute;
  width: 100px;
  height: 100px;
  border: 1px solid rgba(0, 212, 170, 0.15);
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  animation: hex-float 12s ease-in-out infinite;
}

.hex-1 {
  top: 10%;
  right: 10%;
  animation-delay: 0s;
  border-color: rgba(0, 212, 170, 0.2);
}

.hex-2 {
  bottom: 15%;
  left: 8%;
  width: 80px;
  height: 80px;
  animation-delay: 4s;
  border-color: rgba(14, 165, 233, 0.2);
}

.hex-3 {
  top: 50%;
  left: 20%;
  width: 60px;
  height: 60px;
  animation-delay: 8s;
  border-color: rgba(0, 212, 170, 0.15);
}

@keyframes hex-float {
  0%, 100% { transform: rotate(0deg) translateY(0); opacity: 0.3; }
  25% { transform: rotate(30deg) translateY(-15px); opacity: 0.7; }
  50% { transform: rotate(60deg) translateY(0); opacity: 0.4; }
  75% { transform: rotate(90deg) translateY(10px); opacity: 0.6; }
}

/* 粒子背景 */
.bg-particles {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.particle {
  position: absolute;
  background: rgba(0, 212, 170, 0.4);
  border-radius: 50%;
  animation: float infinite ease-in-out;

  &:nth-child(odd) {
    background: rgba(14, 165, 233, 0.4);
    animation-name: particle-drift;
  }

  &:nth-child(3n) {
    box-shadow: 0 0 6px rgba(0, 212, 170, 0.6);
  }
}

@keyframes particle-drift {
  0%, 100% { transform: translate(0, 0) scale(1); opacity: 0.4; }
  33% { transform: translate(20px, -30px) scale(1.3); opacity: 0.8; }
  66% { transform: translate(-10px, 20px) scale(0.8); opacity: 0.5; }
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
  animation: morph-blob 8s infinite ease-in-out;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: #00d4aa;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: #0ea5e9;
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
    0 0 40px rgba(0, 212, 170, 0.1),
    inset 0 0 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  transition: transform 0.6s cubic-bezier(0.23, 1, 0.32, 1);

  &:hover {
    transform: rotateY(-1deg) rotateX(1deg) translateZ(10px);
    box-shadow:
      0 30px 70px rgba(0, 0, 0, 0.35),
      0 0 60px rgba(0, 212, 170, 0.15);
  }

  @media (max-width: 768px) {
    flex-direction: column;
    &:hover { transform: none; }
  }
}

/* 品牌区域 */
.auth-brand {
  flex: 1;
  background: linear-gradient(135deg, #0a192f 0%, #0f2744 50%, #132f4c 100%);
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
    background: radial-gradient(circle at 30% 70%, rgba(0, 212, 170, 0.15) 0%, transparent 50%),
                radial-gradient(circle at 70% 30%, rgba(14, 165, 233, 0.15) 0%, transparent 50%);
  }

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(0, 212, 170, 0.05) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 212, 170, 0.05) 1px, transparent 1px);
    background-size: 30px 30px;
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
  box-shadow: 0 0 30px rgba(0, 212, 170, 0.3);
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
  text-shadow: 0 0 20px rgba(0, 212, 170, 0.3);
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
      box-shadow: 0 0 0 2px var(--primary), 0 0 15px rgba(0, 212, 170, 0.15);
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
    box-shadow: 0 8px 25px rgba(0, 212, 170, 0.4);
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
      text-shadow: 0 0 8px rgba(0, 212, 170, 0.3);
    }
  }
}
</style>
