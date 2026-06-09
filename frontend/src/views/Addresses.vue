<template>
  <div class="addresses-page">
    <div class="page-inner">
      <div class="page-header">
        <h1>收货地址管理</h1>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon> 新增地址
        </el-button>
      </div>

      <div class="address-list" v-loading="loading">
        <div v-if="addresses.length === 0" class="empty-state">
          <el-empty description="暂无收货地址" />
        </div>
        <div v-for="addr in addresses" :key="addr.id" class="address-card" :class="{ 'is-default': addr.isDefault }">
          <div class="address-info">
            <div class="address-top">
              <span class="receiver-name">{{ addr.receiverName }}</span>
              <span class="receiver-phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.isDefault" type="success" size="small">默认</el-tag>
            </div>
            <div class="address-detail">
              {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
            </div>
          </div>
          <div class="address-actions">
            <el-button text type="primary" @click="showEditDialog(addr)">编辑</el-button>
            <el-button text type="primary" @click="setDefault(addr)" v-if="!addr.isDefault">设为默认</el-button>
            <el-button text type="danger" @click="deleteAddress(addr)">删除</el-button>
          </div>
        </div>
      </div>

      <!-- 新增/编辑弹窗 -->
      <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地址' : '新增地址'" width="500px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="省份">
            <el-input v-model="form.province" placeholder="省" />
          </el-form-item>
          <el-form-item label="城市">
            <el-input v-model="form.city" placeholder="市" />
          </el-form-item>
          <el-form-item label="区/县">
            <el-input v-model="form.district" placeholder="区/县" />
          </el-form-item>
          <el-form-item label="详细地址" prop="detailAddress">
            <el-input v-model="form.detailAddress" type="textarea" :rows="2" placeholder="请输入详细地址" />
          </el-form-item>
          <el-form-item label="设为默认">
            <el-switch v-model="form.isDefault" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const addresses = ref([])
const formRef = ref(null)

const form = reactive({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const fetchAddresses = async () => {
  loading.value = true
  try {
    const res = await api.get('/addresses')
    if (res.code === 200) {
      addresses.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { receiverName: '', phone: '', province: '', city: '', district: '', detailAddress: '', isDefault: false })
}

const showAddDialog = () => {
  resetForm()
  isEdit.value = false
  editingId.value = null
  dialogVisible.value = true
}

const showEditDialog = (addr) => {
  Object.assign(form, {
    receiverName: addr.receiverName,
    phone: addr.phone,
    province: addr.province || '',
    city: addr.city || '',
    district: addr.district || '',
    detailAddress: addr.detailAddress,
    isDefault: addr.isDefault
  })
  isEdit.value = true
  editingId.value = addr.id
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form }
    let res
    if (isEdit.value) {
      res = await api.put(`/addresses/${editingId.value}`, data)
    } else {
      res = await api.post('/addresses', data)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchAddresses()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    submitting.value = false
  }
}

const setDefault = async (addr) => {
  const res = await api.put(`/addresses/${addr.id}/default`)
  if (res.code === 200) {
    ElMessage.success('设置成功')
    fetchAddresses()
  }
}

const deleteAddress = async (addr) => {
  await ElMessageBox.confirm('确定删除该地址？', '提示', { type: 'warning' })
  const res = await api.delete(`/addresses/${addr.id}`)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchAddresses()
  }
}

onMounted(fetchAddresses)
</script>

<style lang="scss" scoped>
.addresses-page {
  padding: 32px 0;
  min-height: 60vh;
}

.page-inner {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  h1 {
    font-size: 22px;
    font-weight: 700;
  }
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-card {
  background: white;
  border-radius: var(--radius);
  padding: 20px 24px;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  transition: var(--transition);
  border: 2px solid transparent;

  &.is-default {
    border-color: var(--primary);
    background: linear-gradient(135deg, #f0fdf9, #ffffff);
  }

  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
}

.address-info {
  flex: 1;
}

.address-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  .receiver-name {
    font-weight: 600;
    font-size: 15px;
  }

  .receiver-phone {
    color: var(--text-secondary);
    font-size: 14px;
  }
}

.address-detail {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
</style>
