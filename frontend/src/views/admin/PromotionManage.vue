<template>
  <div class="promotion-manage">
    <div class="toolbar">
      <el-button type="primary" @click="openDialog(null)">
        <el-icon><Plus /></el-icon> 创建活动
      </el-button>
    </div>

    <el-table :data="promotions" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="name" label="活动名称" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.type === 'FLASH_SALE' ? 'danger' : 'warning'" size="small">
            {{ row.type === 'FLASH_SALE' ? '限时折扣' : '满减优惠' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="优惠值" width="120">
        <template #default="{ row }">
          <span style="color: var(--primary); font-weight: 600;">
            {{ row.type === 'FLASH_SALE' ? (row.discountValue * 10).toFixed(1) + '折' : '减¥' + row.discountValue }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="最低消费" width="100">
        <template #default="{ row }">
          ¥{{ row.minOrderAmount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="活动时间" min-width="200">
        <template #default="{ row }">
          {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row)" size="small">{{ getStatusText(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button
            text
            :type="row.status === 1 ? 'warning' : 'success'"
            size="small"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
          <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchPromotions"
      />
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑活动' : '创建活动'"
      width="600px"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="form.type" placeholder="选择活动类型" style="width: 100%;">
            <el-option label="限时折扣" value="FLASH_SALE" />
            <el-option label="满减优惠" value="FULL_REDUCTION" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="优惠值" prop="discountValue">
              <el-input-number
                v-model="form.discountValue"
                :min="form.type === 'FLASH_SALE' ? 0.1 : 1"
                :max="form.type === 'FLASH_SALE' ? 0.99 : 99999"
                :step="form.type === 'FLASH_SALE' ? 0.05 : 10"
                :precision="2"
                style="width: 100%;"
              />
              <div class="form-tip">
                {{ form.type === 'FLASH_SALE' ? '折扣率，如0.8表示8折' : '满减金额（元）' }}
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低消费">
              <el-input-number v-model="form.minOrderAmount" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="描述活动规则" />
        </el-form-item>
        <el-form-item label="适用商品">
          <el-input v-model="form.productIds" placeholder="留空表示全部商品，多个商品ID用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const promotions = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  type: 'FLASH_SALE',
  discountValue: 0.8,
  minOrderAmount: 0,
  startTime: null,
  endTime: null,
  description: '',
  productIds: ''
})

const formRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

const getStatusText = (row) => {
  if (row.status === 0) return '已停用'
  const now = new Date()
  const start = new Date(row.startTime)
  const end = new Date(row.endTime)
  if (now < start) return '未开始'
  if (now > end) return '已结束'
  return '进行中'
}

const getStatusType = (row) => {
  if (row.status === 0) return 'info'
  const now = new Date()
  const start = new Date(row.startTime)
  const end = new Date(row.endTime)
  if (now < start) return 'warning'
  if (now > end) return 'info'
  return 'success'
}

const fetchPromotions = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/promotions', { params: { page: page.value - 1, size: 10 } })
    if (res.code === 200) {
      promotions.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    editingId.value = row.id
    Object.assign(form, {
      name: row.name,
      type: row.type,
      discountValue: row.discountValue,
      minOrderAmount: row.minOrderAmount || 0,
      startTime: row.startTime,
      endTime: row.endTime,
      description: row.description || '',
      productIds: row.productIds || ''
    })
  } else {
    isEdit.value = false
    editingId.value = null
    Object.assign(form, {
      name: '', type: 'FLASH_SALE', discountValue: 0.8, minOrderAmount: 0,
      startTime: null, endTime: null, description: '', productIds: ''
    })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await api.put(`/admin/promotions/${editingId.value}`, form)
    } else {
      res = await api.post('/admin/promotions', form)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
      dialogVisible.value = false
      fetchPromotions()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    submitting.value = false
  }
}

const toggleStatus = (row) => {
  const action = row.status === 1 ? '停用' : '启用'
  const newStatus = row.status === 1 ? 0 : 1
  ElMessageBox.confirm(`确定${action}活动"${row.name}"？`, '提示', { type: 'warning' })
    .then(async () => {
      const res = await api.put(`/admin/promotions/${row.id}`, { status: newStatus })
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        fetchPromotions()
      }
    })
    .catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除活动"${row.name}"？`, '提示', { type: 'warning' })
    .then(async () => {
      const res = await api.delete(`/admin/promotions/${row.id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchPromotions()
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchPromotions()
})
</script>

<style lang="scss" scoped>
.promotion-manage {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
}

.toolbar {
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>
