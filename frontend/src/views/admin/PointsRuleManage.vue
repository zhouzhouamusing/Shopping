<template>
  <div class="points-rule-manage">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 分类积分规则 -->
      <el-tab-pane label="分类积分规则" name="category">
        <div class="toolbar">
          <el-button type="primary" @click="openRuleDialog(null)">
            <el-icon><Plus /></el-icon> 添加规则
          </el-button>
        </div>
        <el-table :data="rules" v-loading="rulesLoading" stripe style="width: 100%">
          <el-table-column label="分类" width="150">
            <template #default="{ row }">
              {{ getCategoryName(row.categoryId) }}
            </template>
          </el-table-column>
          <el-table-column label="积分比率" width="150">
            <template #default="{ row }">
              <el-tag type="success" size="small">{{ (row.pointsRate * 100).toFixed(2) }}%</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="生效时间" min-width="180">
            <template #default="{ row }">
              {{ formatTime(row.startTime) || '不限' }} ~ {{ formatTime(row.endTime) || '不限' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="isActive(row) ? 'success' : 'info'" size="small">
                {{ isActive(row) ? '生效中' : '未生效' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" size="small" @click="openRuleDialog(row)">编辑</el-button>
              <el-button text type="danger" size="small" @click="handleDeleteRule(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 积分基础设置 -->
      <el-tab-pane label="积分基础设置" name="settings">
        <div class="settings-panel">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="积分价值">1积分 = 0.01元</el-descriptions-item>
            <el-descriptions-item label="积分有效期">365天（自获得之日起计算）</el-descriptions-item>
            <el-descriptions-item label="默认积分比率">消费金额 × 1% = 获得积分</el-descriptions-item>
            <el-descriptions-item label="积分获取场景">
              <el-tag size="small" style="margin-right: 8px;">订单完成</el-tag>
              <el-tag size="small" style="margin-right: 8px;">管理员手动调整</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="积分消耗方式">
              <el-tag size="small" type="warning" style="margin-right: 8px;">订单抵扣</el-tag>
              <el-tag size="small" type="warning" style="margin-right: 8px;">兑换优惠券</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="会员等级加成">
              高等级会员享受积分倍率加成（如黄金会员1.5倍积分）
            </el-descriptions-item>
          </el-descriptions>
          <el-alert
            title="提示：分类积分规则优先于默认规则。可为不同商品分类设置不同的积分获取比率。"
            type="info"
            style="margin-top: 16px;"
            :closable="false"
            show-icon
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 规则编辑对话框 -->
    <el-dialog
      v-model="ruleDialogVisible"
      :title="isEditRule ? '编辑积分规则' : '添加积分规则'"
      width="500px"
    >
      <el-form :model="ruleForm" :rules="ruleFormRules" ref="ruleFormRef" label-width="100px">
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="ruleForm.categoryId" placeholder="选择分类" style="width: 100%;" :disabled="isEditRule">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分比率" prop="pointsRate">
          <el-input-number v-model="ruleForm.pointsRate" :min="0.0001" :max="1" :step="0.01" :precision="4" style="width: 100%;" />
          <div class="form-tip">每消费1元获得的积分比率，如 0.01 表示消费100元得1积分</div>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="ruleForm.startTime" type="datetime" placeholder="不选则立即生效" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="ruleForm.endTime" type="datetime" placeholder="不选则永久有效" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSubmitting" @click="handleSubmitRule">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const activeTab = ref('category')
const rules = ref([])
const categories = ref([])
const rulesLoading = ref(false)
const ruleDialogVisible = ref(false)
const isEditRule = ref(false)
const ruleSubmitting = ref(false)
const ruleFormRef = ref(null)

const ruleForm = reactive({
  categoryId: null,
  pointsRate: 0.01,
  startTime: null,
  endTime: null
})

const ruleFormRules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  pointsRate: [{ required: true, message: '请输入积分比率', trigger: 'blur' }]
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const isActive = (rule) => {
  const now = new Date()
  if (rule.startTime && new Date(rule.startTime) > now) return false
  if (rule.endTime && new Date(rule.endTime) < now) return false
  return true
}

const getCategoryName = (categoryId) => {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.name : `分类#${categoryId}`
}

const fetchRules = async () => {
  rulesLoading.value = true
  try {
    const res = await api.get('/admin/points-rules')
    if (res.code === 200) {
      rules.value = res.data
    }
  } finally {
    rulesLoading.value = false
  }
}

const fetchCategories = async () => {
  const res = await api.get('/categories')
  if (res.code === 200) categories.value = res.data
}

const openRuleDialog = (row) => {
  if (row) {
    isEditRule.value = true
    Object.assign(ruleForm, {
      categoryId: row.categoryId,
      pointsRate: row.pointsRate,
      startTime: row.startTime,
      endTime: row.endTime
    })
  } else {
    isEditRule.value = false
    Object.assign(ruleForm, {
      categoryId: null,
      pointsRate: 0.01,
      startTime: null,
      endTime: null
    })
  }
  ruleDialogVisible.value = true
}

const handleSubmitRule = async () => {
  const valid = await ruleFormRef.value?.validate().catch(() => false)
  if (!valid) return

  ruleSubmitting.value = true
  try {
    const res = await api.post('/admin/points-rules', ruleForm)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      ruleDialogVisible.value = false
      fetchRules()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    ruleSubmitting.value = false
  }
}

const handleDeleteRule = (row) => {
  ElMessageBox.confirm('确定删除该积分规则？', '提示', { type: 'warning' })
    .then(async () => {
      const res = await api.delete(`/admin/points-rules/${row.id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchRules()
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchRules()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.points-rule-manage {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
}

.toolbar {
  margin-bottom: 16px;
}

.settings-panel {
  padding: 8px 0;
}

.form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>
