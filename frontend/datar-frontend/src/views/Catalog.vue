<template>
  <a-card title="资源目录">
    <template #extra>
      <a-button type="primary" @click="showCreateModal">添加目录</a-button>
    </template>

    <a-form layout="inline" style="margin-bottom: 16px">
      <a-form-item label="目录名称">
        <a-input v-model:value="searchName" placeholder="目录名称" style="width: 200px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="searchStatus" placeholder="选择状态" style="width: 120px" allow-clear>
          <a-select-option :value="1">草稿</a-select-option>
          <a-select-option :value="2">已发布</a-select-option>
          <a-select-option :value="3">已下线</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="数据空间">
        <a-select v-model:value="searchSpaceId" placeholder="选择空间" style="width: 180px" allow-clear>
          <a-select-option v-for="space in spaceList" :key="space.fId" :value="space.fId">
            {{ space.fSpaceName }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
      </a-form-item>
    </a-form>

    <a-table :columns="columns" :data-source="data" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'fStatus'">
          <a-tag :color="getStatusColor(record.fStatus)">
            {{ getStatusName(record.fStatus) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click.stop="handleEdit(record)">编辑</a-button>
            <a-button size="small" @click.stop="handlePublish(record)" v-if="record.fStatus === 1">发布</a-button>
            <a-button size="small" @click.stop="handleOffline(record)" v-if="record.fStatus === 2">下线</a-button>
            <a-button size="small" danger @click.stop="handleDelete(record)" v-if="record.fStatus !== 2">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>

  <!-- 添加/编辑模态框 -->
  <a-modal
    v-model:open="modalVisible"
    :title="isEdit ? '编辑目录' : '添加目录'"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirmLoading="submitLoading"
  >
    <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="目录编码" name="fCatalogCode">
        <a-input v-model:value="formState.fCatalogCode" placeholder="请输入目录编码" />
      </a-form-item>
      <a-form-item label="目录名称" name="fCatalogName">
        <a-input v-model:value="formState.fCatalogName" placeholder="请输入目录名称" />
      </a-form-item>
      <a-form-item label="数据源ID" name="fDataSourceId">
        <a-input v-model:value="formState.fDataSourceId" placeholder="请输入数据源ID" />
      </a-form-item>
      <a-form-item label="Schema" name="fSchemaName">
        <a-input v-model:value="formState.fSchemaName" placeholder="请输入Schema名" />
      </a-form-item>
      <a-form-item label="表名" name="fTableName">
        <a-input v-model:value="formState.fTableName" placeholder="请输入表名" />
      </a-form-item>
      <a-form-item label="描述" name="fDescription">
        <a-textarea v-model:value="formState.fDescription" placeholder="请输入描述" :rows="3" />
      </a-form-item>
      <a-form-item label="数据空间" name="fSpaceId">
        <a-select v-model:value="formState.fSpaceId" placeholder="请选择数据空间" allow-clear>
          <a-select-option v-for="space in spaceList" :key="space.fId" :value="space.fId">
            {{ space.fSpaceName }}
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import catalogApi from '@/api/catalog'
import dataspaceApi from '@/api/dataspace'

const columns = [
  { title: '编码', dataIndex: 'fCatalogCode', key: 'fCatalogCode' },
  { title: '名称', dataIndex: 'fCatalogName', key: 'fCatalogName' },
  { title: 'Schema', dataIndex: 'fSchemaName', key: 'fSchemaName' },
  { title: '表名', dataIndex: 'fTableName', key: 'fTableName' },
  { title: '状态', dataIndex: 'fStatus', key: 'fStatus', width: 100 },
  { title: '操作', key: 'action', width: 200 }
]

const loading = ref(false)
const data = ref([])
const pagination = reactive({ current: 1, pageSize: 20, total: 0 })
const searchName = ref('')
const searchStatus = ref(null)
const searchSpaceId = ref(null)
const spaceList = ref([])
const modalVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const currentRecord = ref(null)

const formState = reactive({
  fCatalogCode: '',
  fCatalogName: '',
  fDataSourceId: '',
  fSchemaName: '',
  fTableName: '',
  fDescription: '',
  fSpaceId: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await catalogApi.getPage({
      currentPage: pagination.current,
      pageSize: pagination.pageSize,
      catalogName: searchName.value || undefined,
      status: searchStatus.value || undefined,
      spaceId: searchSpaceId.value || undefined
    })
    if (res.code === 200) {
      data.value = res.data.list || []
      pagination.total = res.data.pagination?.total || 0
    }
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchName.value = ''
  searchStatus.value = null
  searchSpaceId.value = null
  pagination.current = 1
  loadData()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const showCreateModal = () => {
  isEdit.value = false
  Object.assign(formState, { fCatalogCode: '', fCatalogName: '', fDataSourceId: '', fSchemaName: '', fTableName: '', fDescription: '', fSpaceId: '' })
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  currentRecord.value = record
  Object.assign(formState, {
    fCatalogCode: record.fCatalogCode,
    fCatalogName: record.fCatalogName,
    fDataSourceId: record.fDataSourceId,
    fSchemaName: record.fSchemaName,
    fTableName: record.fTableName,
    fDescription: record.fDescription,
    fSpaceId: record.fSpaceId || ''
  })
  modalVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    if (isEdit.value) {
      const res = await catalogApi.update(currentRecord.value.fId, formState)
      if (res.code === 200) {
        message.success('更新成功')
        modalVisible.value = false
        loadData()
      } else {
        message.error(res.msg || '更新失败')
      }
    } else {
      const res = await catalogApi.create(formState)
      if (res.code === 200) {
        message.success('添加成功')
        modalVisible.value = false
        loadData()
      } else {
        message.error(res.msg || '添加失败')
      }
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleCancel = () => {
  modalVisible.value = false
}

const handleDelete = async (record) => {
  try {
    const res = await catalogApi.delete(record.fId)
    if (res.code === 200) {
      message.success('删除成功')
      loadData()
    } else {
      message.error(res.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

const handlePublish = async (record) => {
  try {
    const res = await catalogApi.publish(record.fId)
    if (res.code === 200) {
      message.success('发布成功')
      loadData()
    } else {
      message.error(res.msg || '发布失败')
    }
  } catch (error) {
    message.error('发布失败')
  }
}

const handleOffline = async (record) => {
  try {
    const res = await catalogApi.offline(record.fId)
    if (res.code === 200) {
      message.success('下线成功')
      loadData()
    } else {
      message.error(res.msg || '下线失败')
    }
  } catch (error) {
    message.error('下线失败')
  }
}

const getStatusName = (status) => ({ 1: '草稿', 2: '已发布', 3: '已下线' })[status] || '-'
const getStatusColor = (status) => ({ 1: 'orange', 2: 'green', 3: 'red' })[status] || 'default'

onMounted(() => { loadData(); fetchSpaceList() })

const fetchSpaceList = async () => {
  try {
    const res = await dataspaceApi.getDataSpacePage({ currentPage: 1, pageSize: 100 })
    if (res.code === 200) {
      spaceList.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取数据空间列表失败', error)
  }
}
</script>