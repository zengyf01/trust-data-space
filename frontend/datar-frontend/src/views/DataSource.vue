<template>
  <a-card title="数据源列表">
    <template #extra>
      <a-button type="primary" @click="showCreateModal">添加数据源</a-button>
    </template>

    <a-form layout="inline" style="margin-bottom: 16px">
      <a-form-item label="数据源名称">
        <a-input v-model:value="searchName" placeholder="数据源名称" style="width: 200px" />
      </a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="searchType" placeholder="选择类型" style="width: 120px" allow-clear>
          <a-select-option value="MYSQL">MySQL</a-select-option>
          <a-select-option value="POSTGRESQL">PostgreSQL</a-select-option>
          <a-select-option value="SFTP">SFTP</a-select-option>
          <a-select-option value="HTTP">HTTP</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="searchStatus" placeholder="选择状态" style="width: 100px" allow-clear>
          <a-select-option :value="1">启用</a-select-option>
          <a-select-option :value="2">禁用</a-select-option>
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
          <a-tag :color="record.fStatus === 1 ? 'green' : 'red'">
            {{ record.fStatus === 1 ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" type="primary" @click.stop="handleTest(record)">测试</a-button>
            <a-button size="small" @click.stop="handleEdit(record)">编辑</a-button>
            <a-button size="small" danger @click.stop="handleDelete(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>

  <!-- 添加数据源模态框 -->
  <a-modal
    v-model:open="modalVisible"
    title="添加数据源"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirmLoading="submitLoading"
    ok-text="提交"
    cancel-text="取消"
  >
    <a-form ref="formRef" :model="formState" :rules="rules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="数据源编码" name="fDataSourceCode">
        <a-input v-model:value="formState.fDataSourceCode" placeholder="请输入数据源编码" />
      </a-form-item>
      <a-form-item label="数据源名称" name="fDataSourceName">
        <a-input v-model:value="formState.fDataSourceName" placeholder="请输入数据源名称" />
      </a-form-item>
      <a-form-item label="数据源类型" name="fDataSourceType">
        <a-select v-model:value="formState.fDataSourceType" placeholder="请选择数据源类型">
          <a-select-option value="MYSQL">MySQL</a-select-option>
          <a-select-option value="POSTGRESQL">PostgreSQL</a-select-option>
          <a-select-option value="SFTP">SFTP</a-select-option>
          <a-select-option value="HTTP">HTTP</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="主机地址" name="fHost">
        <a-input v-model:value="formState.fHost" placeholder="如：192.168.1.100:3306" />
      </a-form-item>
      <a-form-item label="用户名" name="fUsername">
        <a-input v-model:value="formState.fUsername" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item label="密码" name="fPassword">
        <a-input-password v-model:value="formState.fPassword" placeholder="请输入密码" />
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

  <!-- 编辑数据源模态框 -->
  <a-modal
    v-model:open="editModalVisible"
    title="编辑数据源"
    @ok="handleEditSubmit"
    @cancel="handleEditCancel"
    :confirmLoading="submitLoading"
    ok-text="提交"
    cancel-text="取消"
  >
    <a-form ref="editFormRef" :model="editFormState" :rules="rules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="数据源编码" name="fDataSourceCode">
        <a-input v-model:value="editFormState.fDataSourceCode" disabled />
      </a-form-item>
      <a-form-item label="数据源名称" name="fDataSourceName">
        <a-input v-model:value="editFormState.fDataSourceName" placeholder="请输入数据源名称" />
      </a-form-item>
      <a-form-item label="数据源类型" name="fDataSourceType">
        <a-select v-model:value="editFormState.fDataSourceType" disabled>
          <a-select-option value="MYSQL">MySQL</a-select-option>
          <a-select-option value="POSTGRESQL">PostgreSQL</a-select-option>
          <a-select-option value="SFTP">SFTP</a-select-option>
          <a-select-option value="HTTP">HTTP</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="主机地址" name="fHost">
        <a-input v-model:value="editFormState.fHost" placeholder="如：192.168.1.100:3306" />
      </a-form-item>
      <a-form-item label="用户名" name="fUsername">
        <a-input v-model:value="editFormState.fUsername" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item label="密码" name="fPassword">
        <a-input-password v-model:value="editFormState.fPassword" placeholder="请输入密码（不修改请留空）" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import datasourceApi from '@/api/datasource'
import dataspaceApi from '@/api/dataspace'

const loading = ref(false)
const submitLoading = ref(false)
const modalVisible = ref(false)
const editModalVisible = ref(false)
const formRef = ref(null)
const editFormRef = ref(null)

const data = ref([])
const pagination = reactive({ current: 1, pageSize: 20, total: 0 })
const searchName = ref('')
const searchType = ref(null)
const searchStatus = ref(null)
const searchSpaceId = ref(null)
const spaceList = ref([])

const formState = reactive({
  fDataSourceCode: '',
  fDataSourceName: '',
  fDataSourceType: undefined,
  fHost: '',
  fUsername: '',
  fPassword: '',
  fSpaceId: ''
})

const editFormState = reactive({
  fId: '',
  fDataSourceCode: '',
  fDataSourceName: '',
  fDataSourceType: '',
  fHost: '',
  fUsername: '',
  fPassword: ''
})

const rules = {
  fDataSourceCode: [{ required: true, message: '请输入数据源编码', trigger: 'blur' }],
  fDataSourceName: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  fDataSourceType: [{ required: true, message: '请选择数据源类型', trigger: 'change' }],
  fHost: [{ required: true, message: '请输入主机地址', trigger: 'blur' }]
}

const columns = [
  { title: '编码', dataIndex: 'fDataSourceCode', key: 'fDataSourceCode' },
  { title: '名称', dataIndex: 'fDataSourceName', key: 'fDataSourceName' },
  { title: '类型', dataIndex: 'fDataSourceType', key: 'fDataSourceType' },
  { title: '主机', dataIndex: 'fHost', key: 'fHost' },
  { title: '状态', dataIndex: 'fStatus', key: 'fStatus', width: 80 },
  { title: '操作', key: 'action', width: 200 }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await datasourceApi.getPage({
      currentPage: pagination.current,
      pageSize: pagination.pageSize,
      dataSourceName: searchName.value || undefined,
      sourceType: searchType.value || undefined,
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

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchName.value = ''; searchType.value = null; searchStatus.value = null; searchSpaceId.value = null; pagination.current = 1; loadData() }
const handleTableChange = (pag) => { pagination.current = pag.current; pagination.pageSize = pag.pageSize; loadData() }

const showCreateModal = () => {
  Object.assign(formState, { fDataSourceCode: '', fDataSourceName: '', fDataSourceType: undefined, fHost: '', fUsername: '', fPassword: '', fSpaceId: '' })
  modalVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    const res = await datasourceApi.create(formState)
    if (res.code === 200) {
      message.success('添加成功')
      modalVisible.value = false
      loadData()
    } else {
      message.error(res.msg || '添加失败')
    }
  } catch (error) {
    console.error('操作失败', error)
  } finally {
    submitLoading.value = false
  }
}

const handleCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

const handleTest = async (record) => {
  try {
    const res = await datasourceApi.test(record.fId)
    if (res.code === 200) {
      message.success('连接测试成功')
    } else {
      message.error(res.msg || '连接测试失败')
    }
  } catch (error) {
    message.error('连接测试失败')
  }
}

const handleEdit = (record) => {
  Object.assign(editFormState, {
    fId: record.fId,
    fDataSourceCode: record.fDataSourceCode,
    fDataSourceName: record.fDataSourceName,
    fDataSourceType: record.fDataSourceType,
    fHost: record.fHost,
    fUsername: record.fUsername || '',
    fPassword: ''
  })
  editModalVisible.value = true
}

const handleEditSubmit = async () => {
  try {
    await editFormRef.value.validate()
    submitLoading.value = true
    const updateData = {
      fDataSourceName: editFormState.fDataSourceName,
      fHost: editFormState.fHost,
      fUsername: editFormState.fUsername
    }
    if (editFormState.fPassword) {
      updateData.fPassword = editFormState.fPassword
    }
    const res = await datasourceApi.update(editFormState.fId, updateData)
    if (res.code === 200) {
      message.success('更新成功')
      editModalVisible.value = false
      loadData()
    } else {
      message.error(res.msg || '更新失败')
    }
  } catch (error) {
    console.error('操作失败', error)
  } finally {
    submitLoading.value = false
  }
}

const handleEditCancel = () => {
  editModalVisible.value = false
  editFormRef.value?.resetFields()
}

const handleDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除数据源"${record.fDataSourceName}"吗？`,
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        const res = await datasourceApi.delete(record.fId)
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
  })
}

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