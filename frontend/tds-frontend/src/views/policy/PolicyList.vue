<template>
  <div>
    <a-card title="策略列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="策略名称">
          <a-input v-model:value="searchForm.policyName" placeholder="请输入策略名称" />
        </a-form-item>
        <a-form-item label="策略类型">
          <a-select v-model:value="searchForm.policyType" placeholder="请选择类型" allowClear style="width: 120px">
            <a-select-option value="ACCESS">访问控制</a-select-option>
            <a-select-option value="USAGE">用量限制</a-select-option>
            <a-select-option value="AUDIT">审计策略</a-select-option>
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
        :data-source="policyList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'policyType'">
            <a-tag>{{ getTypeText(record.policyType) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleToggle(record)">
                {{ record.status === 1 ? '禁用' : '启用' }}
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
      title="策略详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentPolicy">
        <a-descriptions-item label="策略编码">{{ currentPolicy.policyCode }}</a-descriptions-item>
        <a-descriptions-item label="策略名称">{{ currentPolicy.policyName }}</a-descriptions-item>
        <a-descriptions-item label="策略类型">
          <a-tag>{{ getTypeText(currentPolicy.policyType) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentPolicy.status === 1 ? 'green' : 'red'">
            {{ currentPolicy.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ currentPolicy.fCreateTime }}</a-descriptions-item>
        <a-descriptions-item label="策略内容" :span="2">
          <pre style="margin: 0; white-space: pre-wrap">{{ currentPolicy.policyContent }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ currentPolicy.description }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="isEdit ? '编辑策略' : '新建策略'"
      width="600px"
      @ok="handleFormSubmit"
      :confirmLoading="formLoading"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" ref="formRef">
        <a-form-item label="策略名称" name="policyName" :rules="[{ required: true, message: '请输入策略名称' }]">
          <a-input v-model:value="formData.policyName" placeholder="请输入策略名称" />
        </a-form-item>
        <a-form-item label="策略编码" name="policyCode" :rules="[{ required: true, message: '请输入策略编码' }]">
          <a-input v-model:value="formData.policyCode" placeholder="请输入策略编码" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="策略类型" name="policyType" :rules="[{ required: true, message: '请选择策略类型' }]">
          <a-select v-model:value="formData.policyType" placeholder="请选择策略类型">
            <a-select-option value="ACCESS">访问控制</a-select-option>
            <a-select-option value="USAGE">用量限制</a-select-option>
            <a-select-option value="AUDIT">审计策略</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="策略内容" name="policyContent">
          <a-textarea v-model:value="formData.policyContent" :rows="4" placeholder="请输入策略内容(JSON格式)" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="2" placeholder="请输入描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import policyApi from '@/api/policy'

const columns = [
  { title: '策略编码', dataIndex: 'policyCode', key: 'policyCode', width: 150 },
  { title: '策略名称', dataIndex: 'policyName', key: 'policyName' },
  { title: '策略类型', dataIndex: 'policyType', key: 'policyType', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 220 }
]

const policyList = ref([])
const loading = ref(false)
const searchForm = reactive({
  policyName: '',
  policyType: null
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentPolicy = ref(null)

const formVisible = ref(false)
const isEdit = ref(false)
const formLoading = ref(false)
const formData = reactive({
  policyName: '',
  policyCode: '',
  policyType: '',
  policyContent: '',
  description: ''
})
const formRef = ref(null)

const typeMap = {
  ACCESS: '访问控制',
  USAGE: '用量限制',
  AUDIT: '审计策略'
}

const getTypeText = (type) => typeMap[type] || type

const fetchPolicies = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: pagination.current,
      pageSize: pagination.pageSize,
      policyName: searchForm.policyName || undefined,
      policyType: searchForm.policyType || undefined
    }
    const res = await policyApi.getPolicyPage(params)
    if (res.code === 200) {
      policyList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取策略列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchPolicies()
}

const handleReset = () => {
  searchForm.policyName = ''
  searchForm.policyType = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchPolicies()
}

const handleView = (record) => {
  currentPolicy.value = record
  detailVisible.value = true
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    policyName: '',
    policyCode: '',
    policyType: '',
    policyContent: '',
    description: ''
  })
  formVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  Object.assign(formData, {
    id: record.id,
    policyName: record.policyName,
    policyCode: record.policyCode,
    policyType: record.policyType,
    policyContent: record.policyContent,
    description: record.description
  })
  formVisible.value = true
}

const handleFormSubmit = async () => {
  formLoading.value = true
  try {
    const res = isEdit.value
      ? await policyApi.updatePolicy(formData.id, formData)
      : await policyApi.createPolicy(formData)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      formVisible.value = false
      fetchPolicies()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleToggle = async (record) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    const res = await policyApi.togglePolicy(record.id, newStatus)
    if (res.code === 200) {
      message.success(newStatus === 1 ? '已启用' : '已禁用')
      fetchPolicies()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleDelete = async (record) => {
  try {
    const res = await policyApi.deletePolicy(record.id)
    if (res.code === 200) {
      message.success('删除成功')
      fetchPolicies()
    } else {
      message.error(res.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchPolicies()
})
</script>

<style scoped>
pre {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
}
</style>
