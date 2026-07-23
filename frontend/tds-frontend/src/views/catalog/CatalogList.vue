<template>
  <div>
    <a-card title="资源目录列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="目录名称">
          <a-input v-model:value="searchForm.catalogName" placeholder="请输入目录名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 120px">
            <a-select-option :value="1">草稿</a-select-option>
            <a-select-option :value="2">已发布</a-select-option>
            <a-select-option :value="3">已下线</a-select-option>
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
        :data-source="catalogList"
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
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                @click="handlePublish(record)"
              >
                发布
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
      title="目录详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentCatalog">
        <a-descriptions-item label="目录编码">{{ currentCatalog.catalogCode }}</a-descriptions-item>
        <a-descriptions-item label="目录名称">{{ currentCatalog.catalogName }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentCatalog.status)">
            {{ getStatusText(currentCatalog.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="数据源">{{ currentCatalog.sourceName }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ currentCatalog.fCreateTime }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ currentCatalog.description }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="isEdit ? '编辑目录' : '新建目录'"
      width="600px"
      @ok="handleFormSubmit"
      :confirmLoading="formLoading"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" ref="formRef">
        <a-form-item label="目录名称" name="catalogName" :rules="[{ required: true, message: '请输入目录名称' }]">
          <a-input v-model:value="formData.catalogName" placeholder="请输入目录名称" />
        </a-form-item>
        <a-form-item label="目录编码" name="catalogCode" :rules="[{ required: true, message: '请输入目录编码' }]">
          <a-input v-model:value="formData.catalogCode" placeholder="请输入目录编码" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="数据源" name="sourceId">
          <a-select v-model:value="formData.sourceId" placeholder="请选择数据源">
            <a-select-option v-for="item in dataSourceList" :key="item.id" :value="item.id">
              {{ item.sourceName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import catalogApi from '@/api/catalog'
import datasourceApi from '@/api/datasource'

const columns = [
  { title: '目录编码', dataIndex: 'catalogCode', key: 'catalogCode', width: 150 },
  { title: '目录名称', dataIndex: 'catalogName', key: 'catalogName' },
  { title: '数据源', dataIndex: 'sourceName', key: 'sourceName', width: 150 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 250 }
]

const catalogList = ref([])
const dataSourceList = ref([])
const loading = ref(false)
const searchForm = reactive({
  catalogName: '',
  status: null
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentCatalog = ref(null)

const formVisible = ref(false)
const isEdit = ref(false)
const formLoading = ref(false)
const formData = reactive({
  catalogName: '',
  catalogCode: '',
  sourceId: null,
  description: ''
})
const formRef = ref(null)

const statusMap = {
  1: { text: '草稿', color: 'default' },
  2: { text: '已发布', color: 'green' },
  3: { text: '已下线', color: 'red' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusColor = (status) => statusMap[status]?.color || 'default'

const fetchCatalogs = async () => {
  loading.value = true
  try {
    const params = {
      pageNumber: pagination.current,
      pageSize: pagination.pageSize,
      catalogName: searchForm.catalogName || undefined,
      status: searchForm.status || undefined
    }
    const res = await catalogApi.getCatalogPage(params)
    if (res.code === 200) {
      catalogList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取目录列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchDataSources = async () => {
  try {
    const res = await datasourceApi.getDataSourcePage({ pageNumber: 1, pageSize: 100 })
    if (res.code === 200) {
      dataSourceList.value = res.data.list
    }
  } catch (error) {
    console.error('获取数据源列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchCatalogs()
}

const handleReset = () => {
  searchForm.catalogName = ''
  searchForm.status = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchCatalogs()
}

const handleView = async (record) => {
  currentCatalog.value = record
  detailVisible.value = true
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    catalogName: '',
    catalogCode: '',
    sourceId: null,
    description: ''
  })
  formVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  Object.assign(formData, {
    id: record.id,
    catalogName: record.catalogName,
    catalogCode: record.catalogCode,
    sourceId: record.sourceId,
    description: record.description
  })
  formVisible.value = true
}

const handleFormSubmit = async () => {
  formLoading.value = true
  try {
    const res = isEdit.value
      ? await catalogApi.updateCatalog(formData.id, formData)
      : await catalogApi.createCatalog(formData)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      formVisible.value = false
      fetchCatalogs()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const handlePublish = async (record) => {
  try {
    const res = await catalogApi.publishCatalog(record.id)
    if (res.code === 200) {
      message.success('发布成功')
      fetchCatalogs()
    } else {
      message.error(res.msg || '发布失败')
    }
  } catch (error) {
    message.error('发布失败')
  }
}

const handleOffline = async (record) => {
  try {
    const res = await catalogApi.offlineCatalog(record.id)
    if (res.code === 200) {
      message.success('下线成功')
      fetchCatalogs()
    } else {
      message.error(res.msg || '下线失败')
    }
  } catch (error) {
    message.error('下线失败')
  }
}

const handleDelete = async (record) => {
  try {
    const res = await catalogApi.deleteCatalog(record.id)
    if (res.code === 200) {
      message.success('删除成功')
      fetchCatalogs()
    } else {
      message.error(res.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchCatalogs()
  fetchDataSources()
})
</script>
