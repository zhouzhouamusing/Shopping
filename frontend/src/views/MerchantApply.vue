<template>
  <div class="merchant-apply">
    <div class="apply-card">
      <h2>商家入驻申请</h2>

      <!-- 已有申请 -->
      <div v-if="application" class="application-status">
        <el-result
          v-if="application.status === 'PENDING'"
          icon="info"
          title="申请已提交"
          sub-title="您的入驻申请正在审核中，请耐心等待管理员审批。"
        />
        <el-result
          v-else-if="application.status === 'APPROVED'"
          icon="success"
          title="申请已通过"
          sub-title="恭喜！您的入驻申请已审批通过，请重新登录以进入商家中心。"
        >
          <template #extra>
            <el-button type="primary" @click="reLogin">重新登录</el-button>
          </template>
        </el-result>
        <el-result
          v-else-if="application.status === 'REJECTED'"
          icon="error"
          title="申请未通过"
          :sub-title="'拒绝原因：' + (application.rejectReason || '未说明')"
        >
          <template #extra>
            <el-button type="primary" @click="application = null">重新申请</el-button>
          </template>
        </el-result>
      </div>

      <!-- 申请表单 -->
      <el-form
        v-else
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="apply-form"
      >
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="form.shopName" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业执照">
          <el-input v-model="form.businessLicense" placeholder="营业执照编号（选填）" />
        </el-form-item>
        <el-form-item label="店铺描述">
          <el-input v-model="form.description" type="textarea" rows="4" placeholder="简要描述您的店铺和经营范围" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const application = ref(null)

const form = reactive({
  shopName: '',
  contactName: '',
  contactPhone: '',
  businessLicense: '',
  description: ''
})

const rules = {
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const fetchApplication = async () => {
  const res = await api.get('/merchant-application/my')
  if (res.code === 200 && res.data) {
    application.value = res.data
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await api.post('/merchant-application', form)
    if (res.code === 200) {
      ElMessage.success('申请已提交，请等待审核')
      application.value = res.data
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    submitting.value = false
  }
}

const reLogin = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchApplication()
})
</script>

<style lang="scss" scoped>
.merchant-apply {
  max-width: 700px;
  margin: 40px auto;
  padding: 0 20px;
}

.apply-card {
  background: white;
  border-radius: var(--radius);
  padding: 40px;
  box-shadow: var(--shadow);

  h2 {
    font-size: 22px;
    font-weight: 700;
    margin-bottom: 32px;
    text-align: center;
  }
}

.apply-form {
  max-width: 500px;
  margin: 0 auto;
}

.application-status {
  text-align: center;
}
</style>
