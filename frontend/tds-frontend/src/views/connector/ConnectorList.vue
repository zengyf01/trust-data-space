<template>
  <div>
    <a-card title="连接器列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="连接器名称">
          <a-input v-model:value="searchForm.name" placeholder="请输入连接器名称" />
        </a-form-item>
        <a-form-item label="连接器类型">
          <a-select v-model:value="searchForm.type" placeholder="请选择类型" allowClear style="width: 120px">
            <a-select-option value="DATA_SOURCE">数据源连接器</a-select-option>
            <a-select-option value="SANDBOX">沙盒连接器</a-select-option>
            <a-select-option value="PRIVACY_COMPUTE">隐私计算连接器</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 120px">
            <a-select-option :value="1">在线</a-select-option>
            <a-select-option :value="0">离线</a-select-option>
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
        :data-source="connectorList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag>{{ getTypeText(record.connectorType) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '在线' : '离线' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleOperate(record)">操作</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="连接器详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentConnector">
        <a-descriptions-item label="连接器SN">{{ currentConnector.connectorSn }}</a-descriptions-item>
        <a-descriptions-item label="连接器名称">{{ currentConnector.name }}</a-descriptions-item>
        <a-descriptions-item label="连接器类型">
          <a-tag>{{ getTypeText(currentConnector.connectorType) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentConnector.status === 1 ? 'green' : 'red'">
            {{ currentConnector.status === 1 ? '在线' : '离线' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="版本">{{ currentConnector.version }}</a-descriptions-item>
        <a-descriptions-item label="最后心跳">{{ currentConnector.lastHeartbeatTime }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ currentConnector.fCreateTime }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ currentConnector.description }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="isEdit ? '编辑连接器' : '新建连接器'"
      width="600px"
      @ok="handleFormSubmit"
      :confirmLoading="formLoading"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" ref="formRef">
        <a-form-item label="连接器名称" name="name" :rules="[{ required: true, message: '请输入连接器名称' }]">
          <a-input v-model:value="formData.name" placeholder="请输入连接器名称" />
        </a-form-item>
        <a-form-item label="连接器SN" name="connectorSn" :rules="[{ required: true, message: '请输入连接器SN' }]">
          <a-input v-model:value="formData.connectorSn" placeholder="请输入连接器SN" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="连接器类型" name="connectorType" :rules="[{ required: true, message: '请选择连接器类型' }]">
          <a-select v-model:value="formData.connectorType" placeholder="请选择连接器类型">
            <a-select-option value="DATA_SOURCE">数据源连接器</a-select-option>
            <a-select-option value="SANDBOX">沙盒连接器</a-select-option>
            <a-select-option value="PRIVACY_COMPUTE">隐私计算连接器</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="版本" name="version">
          <a-input v-model:value="formData.version" placeholder="请输入版本号" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入描述" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 操作弹窗 -->
    <a-modal
      v-model:open="operateVisible"
      title="连接器操作"
      @ok="handleOperateConfirm"
      :confirmLoading="operateLoading"
    >
      <a-form :model="operateForm" :label-col="{ span: 6 }">
        <a-form-item label="操作类型">
          <a-select v-model:value="operateForm.operation" placeholder="请选择操作">
            <a-select-option value="start">启动</a-select-option>
            <a-select-option value="stop">停止</a-select-option>
            <a-select-option value="restart">重启</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import connectorApi from '@/api/connector'

const columns = [
  { title: '连接器SN', dataIndex: 'connectorSn', key: 'connectorSn', width: 180 },
  { title: '连接器名称', dataIndex: 'name', key: 'name' },
  { title: '类型', dataIndex: 'connectorType', key: 'connectorType', width: 150 },
  { title: '版本', dataIndex: 'version', key: 'version', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '最后心跳', dataIndex: 'lastHeartbeatTime', key: 'lastHeartbeatTime', width: 180 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 220 }
]

const connectorList = ref([])
const loading = ref(false)
const searchForm = reactive({
  name: '',
  type: null,
  status: null
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentConnector = ref(null)

const formVisible = ref(false)
const isEdit = ref(false)
const formLoading = ref(false)
const formData = reactive({
  name: '',
  connectorSn: '',
  connectorType: '',
  version: '',
  description: ''
})
const formRef = ref(null)

const operateVisible = ref(false)
const operateLoading = ref(false)
const operateForm = reactive({
  operation: ''
})
const currentOperateConnector = ref(null)

const typeMap = {
  DATA_SOURCE: '数据源连接器',
  SANDBOX: '沙盒连接器',
  PRIVACY_COMPUTE: '隐私计算连接器'
}

const getTypeText = (type) => typeMap[type] || type

const fetchConnectors = async () => {
  loading.value = true
  try {
    const params = {
      pageNumber: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status || undefined
    }
    const res = await connectorApi.getConnectorPage(params)
    if (res.code === 200) {
      connectorList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取连接器列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchConnectors()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.type = null
  searchForm.status = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchConnectors()
}

const handleView = (record) => {
  currentConnector.value = record
  detailVisible.value = true
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    name: '',
    connectorSn: '',
    connectorType: '',
    version: '',
    description: ''
  })
  formVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  Object.assign(formData, {
    id: record.id,
    name: record.name,
    connectorSn: record.connectorSn,
    connectorType: record.connectorType,
    version: record.version,
    description: record.description
  })
  formVisible.value = true
}

const handleFormSubmit = async () => {
  formLoading.value = true
  try {
    const res = isEdit.value
      ? await connectorApi.updateConnector(formData.id, formData)
      : await connectorApi.createConnector(formData)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      formVisible.value = false
      fetchConnectors()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleOperate = (record) => {
  currentOperateConnector.value = record
  operateForm.operation = ''
  operateVisible.value = true
}

const handleOperateConfirm = async () => {
  if (!operateForm.operation) {
    message.warning('请选择操作类型')
    return
  }
  operateLoading.value = true
  try {
    const res = await connectorApi.operate({
      connectorSn: currentOperateConnector.value.connectorSn,
      operation: operateForm.operation
    })
    if (res.code === 200) {
      message.success('操作已提交')
      operateVisible.value = false
      fetchConnectors()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    operateLoading.value = false
  }
}

const handleDelete = async (record) => {
  try {
    const res = await connectorApi.deleteConnector(record.id)
    if (res.code === 200) {
      message.success('删除成功')
      fetchConnectors()
    } else {
      message.error(res.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchConnectors()
})
</script>
