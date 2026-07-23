<template>
  <div>
    <a-card title="数据产品列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="产品名称">
          <a-input v-model:value="searchForm.productName" placeholder="请输入产品名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 120px">
            <a-select-option :value="1">待审核</a-select-option>
            <a-select-option :value="2">已上线</a-select-option>
            <a-select-option :value="3">已下线</a-select-option>
            <a-select-option :value="4">审核拒绝</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
          <a-button type="primary" style="margin-left: 8px" @click="handleCreate">新建</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card>
      <a-table
        :columns="columns"
        :data-source="productList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'price'">
            <span>¥{{ record.price }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                @click="handleApprove(record)"
              >
                审核
              </a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                danger
                @click="handleReject(record)"
              >
                拒绝
              </a-button>
              <a-button
                v-if="record.status === 2"
                type="link"
                size="small"
                @click="handleOffline(record)"
              >
                下线
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="产品详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentProduct">
        <a-descriptions-item label="产品编码">{{ currentProduct.productCode }}</a-descriptions-item>
        <a-descriptions-item label="产品名称">{{ currentProduct.productName }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentProduct.status)">
            {{ getStatusText(currentProduct.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="价格">¥{{ currentProduct.price }}</a-descriptions-item>
        <a-descriptions-item label="目录">{{ currentProduct.catalogName }}</a-descriptions-item>
        <a-descriptions-item label="提供方">{{ currentProduct.providerOrgName }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ currentProduct.fCreateTime }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ currentProduct.description }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="isEdit ? '编辑产品' : '新建产品'"
      width="600px"
      @ok="handleFormSubmit"
      :confirmLoading="formLoading"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" ref="formRef">
        <a-form-item label="产品名称" name="productName" :rules="[{ required: true, message: '请输入产品名称' }]">
          <a-input v-model:value="formData.productName" placeholder="请输入产品名称" />
        </a-form-item>
        <a-form-item label="产品编码" name="productCode" :rules="[{ required: true, message: '请输入产品编码' }]">
          <a-input v-model:value="formData.productCode" placeholder="请输入产品编码" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="目录" name="catalogId">
          <a-select v-model:value="formData.catalogId" placeholder="请选择目录">
            <a-select-option v-for="item in catalogList" :key="item.id" :value="item.id">
              {{ item.catalogName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="价格" name="price">
          <a-input-number v-model:value="formData.price" placeholder="请输入价格" style="width: 100%" :min="0" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入描述" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 拒绝原因弹窗 -->
    <a-modal
      v-model:open="rejectVisible"
      title="审核拒绝"
      @ok="handleRejectConfirm"
      :confirmLoading="actionLoading"
    >
      <a-form>
        <a-form-item label="拒绝原因" required>
          <a-input v-model:value="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import productApi from '@/api/product'
import catalogApi from '@/api/catalog'

const columns = [
  { title: '产品编码', dataIndex: 'productCode', key: 'productCode', width: 150 },
  { title: '产品名称', dataIndex: 'productName', key: 'productName' },
  { title: '目录', dataIndex: 'catalogName', key: 'catalogName', width: 120 },
  { title: '提供方', dataIndex: 'providerOrgName', key: 'providerOrgName', width: 150 },
  { title: '价格', dataIndex: 'price', key: 'price', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 250 }
]

const productList = ref([])
const catalogList = ref([])
const loading = ref(false)
const searchForm = reactive({
  productName: '',
  status: null
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentProduct = ref(null)

const formVisible = ref(false)
const isEdit = ref(false)
const formLoading = ref(false)
const formData = reactive({
  productName: '',
  productCode: '',
  catalogId: null,
  price: 0,
  description: ''
})
const formRef = ref(null)

const rejectVisible = ref(false)
const rejectReason = ref('')
const actionLoading = ref(false)
const currentActionProduct = ref(null)

const statusMap = {
  1: { text: '待审核', color: 'orange' },
  2: { text: '已上线', color: 'green' },
  3: { text: '已下线', color: 'gray' },
  4: { text: '审核拒绝', color: 'red' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusColor = (status) => statusMap[status]?.color || 'default'

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      pageNumber: pagination.current,
      pageSize: pagination.pageSize,
      productName: searchForm.productName || undefined,
      status: searchForm.status || undefined
    }
    const res = await productApi.getProductPage(params)
    if (res.code === 200) {
      productList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取产品列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchCatalogs = async () => {
  try {
    const res = await catalogApi.getCatalogPage({ pageNumber: 1, pageSize: 100, status: 2 })
    if (res.code === 200) {
      catalogList.value = res.data.list
    }
  } catch (error) {
    console.error('获取目录列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchProducts()
}

const handleReset = () => {
  searchForm.productName = ''
  searchForm.status = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchProducts()
}

const handleView = (record) => {
  currentProduct.value = record
  detailVisible.value = true
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    productName: '',
    productCode: '',
    catalogId: null,
    price: 0,
    description: ''
  })
  formVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  Object.assign(formData, {
    id: record.id,
    productName: record.productName,
    productCode: record.productCode,
    catalogId: record.catalogId,
    price: record.price,
    description: record.description
  })
  formVisible.value = true
}

const handleFormSubmit = async () => {
  formLoading.value = true
  try {
    const res = isEdit.value
      ? await productApi.updateProduct(formData.id, formData)
      : await productApi.createProduct(formData)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      formVisible.value = false
      fetchProducts()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleApprove = async (record) => {
  try {
    const res = await productApi.approveProduct(record.id)
    if (res.code === 200) {
      message.success('审核通过')
      fetchProducts()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleReject = (record) => {
  currentActionProduct.value = record
  rejectReason.value = ''
  rejectVisible.value = true
}

const handleRejectConfirm = async () => {
  if (!rejectReason.value) {
    message.warning('请输入拒绝原因')
    return
  }
  actionLoading.value = true
  try {
    const res = await productApi.rejectProduct(currentActionProduct.value.id, rejectReason.value)
    if (res.code === 200) {
      message.success('已拒绝')
      rejectVisible.value = false
      fetchProducts()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

const handleOffline = async (record) => {
  try {
    const res = await productApi.offlineProduct(record.id)
    if (res.code === 200) {
      message.success('已下线')
      fetchProducts()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleDelete = async (record) => {
  try {
    const res = await productApi.deleteProduct(record.id)
    if (res.code === 200) {
      message.success('删除成功')
      fetchProducts()
    } else {
      message.error(res.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchProducts()
  fetchCatalogs()
})
</script>
