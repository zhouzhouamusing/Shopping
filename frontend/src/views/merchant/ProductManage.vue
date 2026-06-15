<template>
  <div class="product-manage">
    <div class="toolbar">
      <el-button type="primary" @click="openDialog(null)">
        <el-icon><Plus /></el-icon> 添加商品
      </el-button>
    </div>

    <el-table :data="products" v-loading="loading" stripe style="width: 100%">
      <el-table-column label="商品图片" width="80">
        <template #default="{ row }">
          <img :src="row.mainImage" style="width: 50px; height: 50px; border-radius: 6px; object-fit: cover;" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">
          <span style="color: var(--primary); font-weight: 600;">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openDialog(row)">编辑</el-button>
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
        @current-change="fetchProducts"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '添加商品'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%;">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="主图URL">
          <el-input v-model="form.mainImage" placeholder="输入图片URL" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
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

const products = ref([])
const categories = ref([])
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
  description: '',
  price: 0,
  originalPrice: 0,
  stock: 0,
  mainImage: '',
  categoryId: null,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await api.get('/merchant/products', { params: { page: page.value - 1, size: 10 } })
    if (res.code === 200) {
      products.value = res.data.content
      total.value = res.data.totalElements
    }
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  const res = await api.get('/categories')
  if (res.code === 200) categories.value = res.data
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    editingId.value = row.id
    Object.assign(form, {
      name: row.name,
      description: row.description,
      price: row.price,
      originalPrice: row.originalPrice,
      stock: row.stock,
      mainImage: row.mainImage,
      categoryId: row.categoryId,
      status: row.status
    })
  } else {
    isEdit.value = false
    editingId.value = null
    Object.assign(form, {
      name: '', description: '', price: 0, originalPrice: 0,
      stock: 0, mainImage: '', categoryId: null, status: 1
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
      res = await api.put(`/merchant/products/${editingId.value}`, form)
    } else {
      res = await api.post('/merchant/products', form)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchProducts()
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除商品"${row.name}"？`, '提示', { type: 'warning' })
    .then(async () => {
      const res = await api.delete(`/merchant/products/${row.id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchProducts()
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.product-manage {
  background: white;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow);
}

.toolbar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
