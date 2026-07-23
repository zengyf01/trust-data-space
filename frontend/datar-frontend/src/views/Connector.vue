<template>
  <a-card title="连接器列表">
    <template #extra>
      <a-button type="primary" @click="showCreateModal">添加连接器</a-button>
    </template>

    <!-- 搜索表单 -->
    <a-form layout="inline" style="margin-bottom: 16px">
      <a-form-item label="关键词">
        <a-input v-model:value="searchKeyword" placeholder="名称/SN" style="width: 200px" />
      </a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="searchType" placeholder="选择类型" style="width: 150px" allow-clear>
          <a-select-option :value="1">数据连接器</a-select-option>
          <a-select-option :value="2">沙盒连接器</a-select-option>
          <a-select-option :value="3">隐私计算连接器</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="searchStatus" placeholder="选择状态" style="width: 120px" allow-clear>
          <a-select-option :value="1">在线</a-select-option>
          <a-select-option :value="2">离线</a-select-option>
          <a-select-option :value="3">待注册</a-select-option>
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
        <template v-if="column.key === 'fType'">
          <a-tag :color="getTypeColor(record.fType)">
            {{ getTypeName(record.fType) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'fStatus'">
          <a-tag :color="getStatusColor(record.fStatus)">
            {{ getStatusName(record.fStatus) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'fLastHeartbeat'">
          {{ formatDateTime(record.fLastHeartbeat) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" type="primary" @click.stop="handleView(record)">详情</a-button>
            <a-button size="small" @click.stop="handleEdit(record)">编辑</a-button>
            <a-button size="small" @click.stop="handleVersion(record)">版本</a-button>
            <a-button size="small" @click.stop="handleLog(record)">日志</a-button>
            <a-button size="small" danger @click.stop="handleDelete(record)" v-if="record.fIsSystem !== 1">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>

  <!-- 添加/编辑连接器模态框 -->
  <a-modal
    v-model:open="modalVisible"
    :title="isEdit ? '编辑连接器' : '添加连接器'"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirmLoading="submitLoading"
    ok-text="提交"
    cancel-text="取消"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="连接器名称" name="fName">
        <a-input v-model:value="formState.fName" placeholder="请输入连接器名称" />
      </a-form-item>
      <a-form-item label="序列号(SN)" name="fSn" v-if="!isEdit">
        <a-input v-model:value="formState.fSn" placeholder="请输入序列号" />
      </a-form-item>
      <a-form-item label="类型" name="fType">
        <a-select v-model:value="formState.fType" placeholder="请选择类型">
          <a-select-option :value="1">数据连接器</a-select-option>
          <a-select-option :value="2">沙盒连接器</a-select-option>
          <a-select-option :value="3">隐私计算连接器</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="IP地址" name="fIpAddress">
        <a-input v-model:value="formState.fIpAddress" placeholder="请输入IP地址" />
      </a-form-item>
      <a-form-item label="SSH端口" name="fSshPort">
        <a-input-number v-model:value="formState.fSshPort" placeholder="22" style="width: 100%" />
      </a-form-item>
      <a-form-item label="SSH用户名" name="fSshUsername">
        <a-input v-model:value="formState.fSshUsername" placeholder="请输入SSH用户名" />
      </a-form-item>
      <a-form-item label="SSH密码" name="fSshPassword">
        <a-input-password v-model:value="formState.fSshPassword" placeholder="请输入SSH密码" />
      </a-form-item>
      <a-form-item label="机构名称" name="fInstitutionId">
        <a-select v-model:value="formState.fInstitutionId" placeholder="请选择机构" @change="onOrgChange" allow-clear>
          <a-select-option v-for="org in orgList" :key="org.fId" :value="org.fId">
            {{ org.fOrgName }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="数据空间" name="fSpaceId">
        <a-select v-model:value="formState.fSpaceId" placeholder="请选择数据空间" allow-clear>
          <a-select-option v-for="space in spaceList" :key="space.fId" :value="space.fId">
            {{ space.fSpaceName }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="所属区域" name="fRegion">
        <a-input v-model:value="formState.fRegion" placeholder="请输入所属区域" />
      </a-form-item>
      <a-form-item label="描述" name="fDescription">
        <a-textarea v-model:value="formState.fDescription" placeholder="请输入描述" :rows="3" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- 详情模态框 -->
  <a-modal
    v-model:open="detailModalVisible"
    title="连接器详情"
    :footer="null"
  >
    <a-descriptions :column="2" bordered>
      <a-descriptions-item label="名称">{{ currentRecord?.fName }}</a-descriptions-item>
      <a-descriptions-item label="序列号">{{ currentRecord?.fSn }}</a-descriptions-item>
      <a-descriptions-item label="类型">{{ getTypeName(currentRecord?.fType) }}</a-descriptions-item>
      <a-descriptions-item label="状态">
        <a-tag :color="getStatusColor(currentRecord?.fStatus)">
          {{ getStatusName(currentRecord?.fStatus) }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="IP地址">{{ currentRecord?.fIpAddress }}</a-descriptions-item>
      <a-descriptions-item label="SSH端口">{{ currentRecord?.fSshPort }}</a-descriptions-item>
      <a-descriptions-item label="机构名称">{{ currentRecord?.fInstitutionName }}</a-descriptions-item>
      <a-descriptions-item label="所属区域">{{ currentRecord?.fRegion }}</a-descriptions-item>
      <a-descriptions-item label="当前版本">{{ currentRecord?.fVersion || '-' }}</a-descriptions-item>
      <a-descriptions-item label="最后心跳">{{ formatDateTime(currentRecord?.fLastHeartbeat) }}</a-descriptions-item>
      <a-descriptions-item label="描述" :span="2">{{ currentRecord?.fDescription }}</a-descriptions-item>
    </a-descriptions>
  </a-modal>

  <!-- 版本管理模态框 -->
  <a-modal
    v-model:open="versionModalVisible"
    title="版本管理"
    width="700px"
    :footer="null"
  >
    <a-button type="primary" style="margin-bottom: 16px" @click="showUploadVersion">上传新版本</a-button>
    <a-table :columns="versionColumns" :data-source="versionData" :loading="versionLoading" size="small">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'fStatus'">
          <a-tag v-if="record.fStatus === 2" color="green">已激活</a-tag>
          <a-tag v-else-if="record.fStatus === 1" color="blue">激活中</a-tag>
          <a-tag v-else>未激活</a-tag>
        </template>
        <template v-else-if="column.key === 'fCreateTime'">
          {{ formatDateTime(record.fCreateTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button size="small" @click.stop="handleActivate(record)" v-if="record.fStatus !== 2">激活</a-button>
        </template>
      </template>
    </a-table>
  </a-modal>

  <!-- 上传版本模态框 -->
  <a-modal
    v-model:open="uploadVersionModalVisible"
    title="上传新版本"
    @ok="handleUploadVersion"
    @cancel="uploadVersionModalVisible = false"
    :confirmLoading="submitLoading"
  >
    <a-form :model="versionFormState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="版本号" name="fVersion">
        <a-input v-model:value="versionFormState.fVersion" placeholder="如：v1.0.0" />
      </a-form-item>
      <a-form-item label="变更日志" name="fChangeLog">
        <a-textarea v-model:value="versionFormState.fChangeLog" placeholder="请输入变更日志" :rows="4" />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- 日志模态框 -->
  <a-modal
    v-model:open="logModalVisible"
    title="操作日志"
    width="800px"
    :footer="null"
  >
    <a-table :columns="logColumns" :data-source="logData" :loading="logLoading" size="small">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'fOperateType'">
          <a-tag>{{ record.fOperateType }}</a-tag>
        </template>
        <template v-else-if="column.key === 'fOperateResult'">
          <a-tag :color="record.fOperateResult === 'SUCCESS' ? 'green' : 'red'">
            {{ record.fOperateResult === 'SUCCESS' ? '成功' : '失败' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'fCreateTime'">
          {{ formatDateTime(record.fCreateTime) }}
        </template>
      </template>
    </a-table>
  </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import connectorApi from '@/api/connector'
import dataspaceApi from '@/api/dataspace'
import tdsOrganizationApi from '@/api/tds-organization'

const columns = [
  { title: '名称', dataIndex: 'fName', key: 'fName' },
  { title: '序列号', dataIndex: 'fSn', key: 'fSn', width: 180 },
  { title: '类型', dataIndex: 'fType', key: 'fType', width: 120 },
  { title: '状态', dataIndex: 'fStatus', key: 'fStatus', width: 100 },
  { title: 'IP地址', dataIndex: 'fIpAddress', key: 'fIpAddress', width: 130 },
  { title: '版本', dataIndex: 'fVersion', key: 'fVersion', width: 100 },
  { title: '最后心跳', dataIndex: 'fLastHeartbeat', key: 'fLastHeartbeat', width: 160 },
  { title: '操作', key: 'action', width: 280, fixed: 'right' }
]

const versionColumns = [
  { title: '版本号', dataIndex: 'fVersion', key: 'fVersion' },
  { title: '变更日志', dataIndex: 'fChangeLog', key: 'fChangeLog', ellipsis: true },
  { title: '状态', dataIndex: 'fStatus', key: 'fStatus', width: 80 },
  { title: '上传时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 160 },
  { title: '操作', key: 'action', width: 80 }
]

const logColumns = [
  { title: '操作类型', dataIndex: 'fOperateType', key: 'fOperateType', width: 100 },
  { title: '操作内容', dataIndex: 'fOperateContent', key: 'fOperateContent', ellipsis: true },
  { title: '结果', dataIndex: 'fOperateResult', key: 'fOperateResult', width: 80 },
  { title: '耗时', dataIndex: 'fDuration', key: 'fDuration', width: 80 },
  { title: '操作时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 160 }
]

const loading = ref(false)
const data = ref([])
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const searchKeyword = ref('')
const searchType = ref(null)
const searchStatus = ref(null)
const searchSpaceId = ref(null)
const spaceList = ref([])
const orgList = ref([])

const modalVisible = ref(false)
const detailModalVisible = ref(false)
const versionModalVisible = ref(false)
const uploadVersionModalVisible = ref(false)
const logModalVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const currentRecord = ref(null)
const currentConnectorId = ref('')

const formState = reactive({
  fName: '',
  fSn: '',
  fType: 1,
  fIpAddress: '',
  fSshPort: 22,
  fSshUsername: '',
  fSshPassword: '',
  fInstitutionId: '',
  fInstitutionName: '',
  fSpaceId: '',
  fRegion: '',
  fDescription: ''
})

const versionFormState = reactive({
  fVersion: '',
  fChangeLog: ''
})

const versionLoading = ref(false)
const versionData = ref([])
const logLoading = ref(false)
const logData = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await connectorApi.getPage({
      currentPage: pagination.current,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value || undefined,
      type: searchType.value || undefined,
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
  searchKeyword.value = ''
  searchType.value = null
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
  Object.assign(formState, {
    fName: '',
    fSn: '',
    fType: 1,
    fIpAddress: '',
    fSshPort: 22,
    fSshUsername: '',
    fSshPassword: '',
    fInstitutionId: '',
    fInstitutionName: '',
    fSpaceId: '',
    fRegion: '',
    fDescription: ''
  })
  modalVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  Object.assign(formState, {
    fName: record.fName,
    fSn: record.fSn,
    fType: record.fType,
    fIpAddress: record.fIpAddress,
    fSshPort: record.fSshPort,
    fSshUsername: record.fSshUsername,
    fSshPassword: '',
    fInstitutionId: record.fInstitutionId || '',
    fInstitutionName: record.fInstitutionName,
    fSpaceId: record.fSpaceId || '',
    fRegion: record.fRegion,
    fDescription: record.fDescription
  })
  modalVisible.value = true
}

const handleView = (record) => {
  currentRecord.value = record
  detailModalVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    // Transform frontend field names to backend expected names
    const submitData = {
      name: formState.fName,
      type: formState.fType,
      version: formState.fVersion || null,
      ipAddress: formState.fIpAddress,
      sshPort: formState.fSshPort,
      sshUsername: formState.fSshUsername,
      sshPassword: formState.fSshPassword || null,
      sshPrivateKey: formState.fSshPrivateKey || null,
      macAddress: formState.fMacAddress || null,
      institutionId: formState.fInstitutionId,
      institutionName: formState.fInstitutionName,
      spaceId: formState.fSpaceId,
      region: formState.fRegion,
      description: formState.fDescription,
      tenantId: formState.fTenantId || null
    }
    if (isEdit.value) {
      const res = await connectorApi.update(currentRecord.value.fId, submitData)
      if (res.code === 200) {
        message.success('更新成功')
        modalVisible.value = false
        loadData()
      } else {
        message.error(res.msg || '更新失败')
      }
    } else {
      const res = await connectorApi.create(submitData)
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
    const res = await connectorApi.delete(record.fId)
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

const handleVersion = async (record) => {
  currentConnectorId.value = record.fId
  versionModalVisible.value = true
  loadVersions()
}

const loadVersions = async () => {
  versionLoading.value = true
  try {
    const res = await connectorApi.getVersions(currentConnectorId.value)
    if (res.code === 200) {
      versionData.value = res.data || []
    }
  } catch (error) {
    console.error('加载版本失败', error)
  } finally {
    versionLoading.value = false
  }
}

const showUploadVersion = () => {
  versionFormState.fVersion = ''
  versionFormState.fChangeLog = ''
  uploadVersionModalVisible.value = true
}

const handleUploadVersion = async () => {
  if (!versionFormState.fVersion) {
    message.warning('请输入版本号')
    return
  }
  submitLoading.value = true
  try {
    const res = await connectorApi.uploadVersion({
      connectorId: currentConnectorId.value,
      version: versionFormState.fVersion,
      changeLog: versionFormState.fChangeLog
    })
    if (res.code === 200) {
      message.success('上传成功')
      uploadVersionModalVisible.value = false
      loadVersions()
    } else {
      message.error(res.msg || '上传失败')
    }
  } catch (error) {
    message.error('上传失败')
  } finally {
    submitLoading.value = false
  }
}

const handleActivate = async (record) => {
  try {
    const res = await connectorApi.activateVersion(record.fId)
    if (res.code === 200) {
      message.success('激活成功')
      loadVersions()
    } else {
      message.error(res.msg || '激活失败')
    }
  } catch (error) {
    message.error('激活失败')
  }
}

const handleLog = async (record) => {
  currentConnectorId.value = record.fId
  logModalVisible.value = true
  loadLogs()
}

const loadLogs = async () => {
  logLoading.value = true
  try {
    const res = await connectorApi.getLogs(currentConnectorId.value)
    if (res.code === 200) {
      logData.value = res.data || []
    }
  } catch (error) {
    console.error('加载日志失败', error)
  } finally {
    logLoading.value = false
  }
}

const getTypeName = (type) => {
  const map = { 1: '数据连接器', 2: '沙盒连接器', 3: '隐私计算连接器' }
  return map[type] || '-'
}

const getTypeColor = (type) => {
  const map = { 1: 'blue', 2: 'purple', 3: 'orange' }
  return map[type] || 'default'
}

const getStatusName = (status) => {
  const map = { 1: '在线', 2: '离线', 3: '待注册' }
  return map[status] || '-'
}

const getStatusColor = (status) => {
  const map = { 1: 'green', 2: 'red', 3: 'orange' }
  return map[status] || 'default'
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
  fetchSpaceList()
  fetchOrgList()
})

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

const fetchOrgList = async () => {
  try {
    const res = await tdsOrganizationApi.getOrgPage({ currentPage: 1, pageSize: 100 })
    if (res.code === 200) {
      orgList.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取机构列表失败', error)
  }
}

const onOrgChange = (orgId) => {
  const org = orgList.value.find(o => o.fId === orgId)
  if (org) {
    formState.fInstitutionName = org.fOrgName
  }
}
</script>