<template>
  <div class="member-level-manage">
    <div class="toolbar">
      <el-button type="primary" @click="openDialog(null)">
        <el-icon><Plus /></el-icon> 添加等级
      </el-button>
    </div>

    <el-table :data="levels" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="levelCode" label="等级编码" width="100" sortable />
      <el-table-column prop="name" label="等级名称" width="120">
        <template #default="{ row }">
          <span style="font-weight: 600;">{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最低消费" width="120">
        <template #default="{ row }">
          ¥{{ row.minSpending }}
        </template>
      </el-table-column>
      <el-table-column prop="minPoints" label="最低积分" width="100" />
      <el-table-column label="折扣率" width="100">
        <template #default="{ row }">
          <el-tag type="warning" size="small">{{ (row.discountRate * 10).toFixed(1) }}折</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="积分倍率" width="100">
        <template #default="{ row }">
          <el-tag type="success" size="small">{{ row.pointsMultiplier }}x</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button text type="danger" size="small" @click="handleDelete(row)" :disabled="row.levelCode === 1">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑会员等级' : '添加会员等级'"
      width="550px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="form.name" placeholder="如：青铜、白银、黄金" />
        </el-form-item>
        <el-form-item label="等级编码" prop="levelCode">
          <el-input-number v-model="form.levelCode" :min="1" :max="99" style="width: 100%;" :disabled="isEdit" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="最低消费" prop="minSpending">
              <el-input-number v-model="form.minSpending" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低积分" prop="minPoints">
              <el-input-number v-model="form.minPoints" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="折扣率" prop="discountRate">
              <el-input-number v-model="form.discountRate" :min="0.01" :max="1" :step="0.01" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="积分倍率" prop="pointsMultiplier">
              <el-input-number v-model="form.pointsMultiplier" :min="1" :max="10" :step="0.1" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="等级描述">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="描述该等级的权益" />
        </el-form-item>
        <el-form-item label="图标URL">
          <el-input v-model="form.icon" placeholder="输入图标URL" />
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

const levels = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  levelCode: 1,
  minSpending: 0,
  minPoints: 0,
  discountRate: 1,
  pointsMultiplier: 1,
  description: '',
  icon: ''
})

const rules = {
  name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  levelCode: [{ required: true, message: '请输入等级编码', trigger: 'blur' }],
  discountRate: [{ required: true, message: '请输入折扣率', trigger: 'blur' }],
  pointsMultiplier: [{ required: true, message: '请输入积分倍率', trigger: 'blur' }]
}

const fetchLevels = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/member-levels')
    if (res.code === 200) {
      levels.value = res.data
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
      levelCode: row.levelCode,
      minSpending: row.minSpending,
      minPoints: row.minPoints,
      discountRate: row.discountRate,
      pointsMultiplier: row.pointsMultiplier,
      description: row.description || '',
      icon: row.icon || ''
    })
  } else {
    isEdit.value = false
    editingId.value = null
    Object.assign(form, {
      name: '', levelCode: 1, minSpending: 0, minPoints: 0,
      discountRate: 1, pointsMultiplier: 1, description: '', icon: ''
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
      res = await api.put(`/admin/member-levels/${editingId.value}`, form)
    } else {
      res = await api.post('/admin/member-levels', form)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchLevels()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除等级"${row.name}"？`, '提示', { type: 'warning' })
    .then(async () => {
      const res = await api.delete(`/admin/member-levels/${row.id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchLevels()
      } else {
        ElMessage.error(res.message)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchLevels()
})
</script>

<style lang="scss" scoped>
.member-level-manage {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
}

.toolbar {
  margin-bottom: 16px;
}
</style>
