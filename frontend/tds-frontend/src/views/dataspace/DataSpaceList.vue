<template>
  <div>
    <a-card title="数据空间列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="空间名称">
          <a-input v-model:value="searchForm.spaceName" placeholder="请输入空间名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 120px">
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="2">冻结</a-select-option>
            <a-select-option :value="3">已注销</a-select-option>
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
        :data-source="dataSpaceList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="fId"
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
                v-if="record.fStatus === 0"
                type="link"
                size="small"
                @click="handleApprove(record)"
              >
                审核
              </a-button>
              <a-button
                v-if="record.fStatus === 1"
                type="link"
                size="small"
                danger
                @click="handleFreeze(record)"
              >
                冻结
              </a-button>
              <a-button
                v-if="record.fStatus === 2"
                type="link"
                size="small"
                @click="handleThaw(record)"
              >
                解冻
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
              <a-button type="link" size="small" @click="handleMemberManage(record)">成员</a-button>
              <a-button
                v-if="record.fStatus === 1"
                type="link"
                size="small"
                @click="handleJoinApply(record)"
              >
                申请加入
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="数据空间详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentDataSpace">
        <a-descriptions-item label="空间编码">{{ currentDataSpace.fSpaceCode }}</a-descriptions-item>
        <a-descriptions-item label="空间名称">{{ currentDataSpace.fSpaceName }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentDataSpace.fStatus)">
            {{ getStatusText(currentDataSpace.fStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="负责人">{{ currentDataSpace.fOwnerName }}</a-descriptions-item>
        <a-descriptions-item label="机构名称">{{ currentDataSpace.fOrganizationName }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ currentDataSpace.fCreateTime }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ currentDataSpace.fSpaceDesc }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="isEdit ? '编辑数据空间' : '新建数据空间'"
      @ok="handleFormSubmit"
      :confirmLoading="formLoading"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" ref="formRef">
        <a-form-item label="空间名称" name="spaceName" :rules="[{ required: true, message: '请输入空间名称' }]">
          <a-input v-model:value="formData.spaceName" placeholder="请输入空间名称" />
        </a-form-item>
        <a-form-item label="空间类型" name="spaceType">
          <a-select v-model:value="formData.spaceType" placeholder="请选择空间类型">
            <a-select-option value="PRIVATE">私有</a-select-option>
            <a-select-option value="PUBLIC">公开</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="负责人" name="ownerName">
          <a-input v-model:value="formData.ownerName" placeholder="请输入负责人姓名" />
        </a-form-item>
        <a-form-item label="所属机构" name="organizationId" :rules="[{ required: true, message: '请选择机构' }]">
          <a-select v-model:value="formData.organizationId" placeholder="请选择机构" @change="onOrgChange">
            <a-select-option v-for="org in orgList" :key="org.fId" :value="org.fId">
              {{ org.fOrgName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述" name="spaceDesc">
          <a-textarea v-model:value="formData.spaceDesc" :rows="3" placeholder="请输入描述" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 申请加入弹窗 -->
    <a-modal
      v-model:open="joinVisible"
      title="申请加入数据空间"
      @ok="handleJoinSubmit"
      :confirmLoading="joinLoading"
    >
      <a-form :model="joinForm" :label-col="{ span: 6 }" ref="joinFormRef">
        <a-form-item label="空间名称">
          <a-input v-model:value="joinForm.spaceName" disabled />
        </a-form-item>
        <a-form-item label="所属机构" name="organizationId" :rules="[{ required: true, message: '请选择机构' }]">
          <a-select v-model:value="joinForm.organizationId" placeholder="请选择机构" @change="onJoinOrgChange">
            <a-select-option v-for="org in orgList" :key="org.fId" :value="org.fId">
              {{ org.fOrgName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="申请理由" name="applyReason">
          <a-textarea v-model:value="joinForm.applyReason" :rows="3" placeholder="请输入申请理由" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dataspaceApi from '@/api/dataspace'
import organizationApi from '@/api/organization'

const router = useRouter()

const columns = [
  { title: '空间编码', dataIndex: 'fSpaceCode', key: 'fSpaceCode', width: 150 },
  { title: '空间名称', dataIndex: 'fSpaceName', key: 'fSpaceName' },
  { title: '负责人', dataIndex: 'fOwnerName', key: 'fOwnerName', width: 120 },
  { title: '机构', dataIndex: 'fOrganizationName', key: 'fOrganizationName', width: 120 },
  { title: '状态', dataIndex: 'fStatus', key: 'fStatus', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 280 }
]

const dataSpaceList = ref([])
const orgList = ref([])
const loading = ref(false)
const searchForm = reactive({
  spaceName: '',
  status: null
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentDataSpace = ref(null)

const formVisible = ref(false)
const isEdit = ref(false)
const formLoading = ref(false)
const formData = reactive({
  spaceName: '',
  spaceCode: '',
  ownerId: '',
  ownerName: '',
  spaceDesc: '',
  organizationId: '',
  organizationName: '',
  spaceType: 'PRIVATE'
})
const formRef = ref(null)

// 申请加入相关变量
const joinVisible = ref(false)
const joinLoading = ref(false)
const joinForm = reactive({
  spaceId: '',
  spaceName: '',
  organizationId: '',
  organizationName: '',
  applyReason: ''
})
const joinFormRef = ref(null)

const statusMap = {
  0: { text: '待审核', color: 'orange' },
  1: { text: '正常', color: 'green' },
  2: { text: '冻结', color: 'red' },
  3: { text: '已注销', color: 'default' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusColor = (status) => statusMap[status]?.color || 'default'

const fetchDataSpaces = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: pagination.current,
      pageSize: pagination.pageSize,
      spaceName: searchForm.spaceName || undefined,
      status: searchForm.status || undefined
    }
    const res = await dataspaceApi.getDataSpacePage(params)
    if (res.code === 200) {
      dataSpaceList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取数据空间列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchOrganizations = async () => {
  try {
    const res = await organizationApi.getOrgPage({ currentPage: 1, pageSize: 100 })
    if (res.code === 200) {
      orgList.value = res.data.list
    }
  } catch (error) {
    console.error('获取机构列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchDataSpaces()
}

const handleReset = () => {
  searchForm.spaceName = ''
  searchForm.status = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchDataSpaces()
}

const handleView = (record) => {
  currentDataSpace.value = record
  detailVisible.value = true
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, { spaceName: '', spaceCode: '', ownerId: '', ownerName: '', spaceDesc: '', organizationId: '', organizationName: '', spaceType: 'PRIVATE' })
  fetchOrganizations()
  formVisible.value = true
}

const onOrgChange = (orgId) => {
  const org = orgList.value.find(o => o.fId === orgId)
  if (org) {
    formData.organizationName = org.fOrgName
  }
}

const handleEdit = (record) => {
  isEdit.value = true
  Object.assign(formData, {
    id: record.fId,
    spaceName: record.fSpaceName,
    spaceCode: record.fSpaceCode,
    ownerId: record.fOwnerId,
    ownerName: record.fOwnerName,
    spaceDesc: record.fSpaceDesc,
    organizationId: record.fOrganizationId,
    organizationName: record.fOrganizationName,
    spaceType: record.fSpaceType
  })
  formVisible.value = true
  // 编辑时：先获取机构列表，然后尝试用机构名称匹配正确的机构ID
  fetchOrganizations().then(() => {
    // 如果当前organizationId在列表中不存在，尝试用名称匹配
    if (formData.organizationId && !orgList.value.find(o => o.fId === formData.organizationId)) {
      const matchedOrg = orgList.value.find(o => o.fOrgName === formData.organizationName)
      if (matchedOrg) {
        formData.organizationId = matchedOrg.fId
      }
    }
  })
}

const handleFormSubmit = async () => {
  formLoading.value = true
  try {
    const res = isEdit.value
      ? await dataspaceApi.updateDataSpace(formData.id, formData)
      : await dataspaceApi.createDataSpace(formData)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      formVisible.value = false
      fetchDataSpaces()
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
    const res = await dataspaceApi.approveDataSpace(record.fId, 1)
    if (res.code === 200) {
      message.success('审核通过')
      fetchDataSpaces()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleFreeze = async (record) => {
  try {
    const res = await dataspaceApi.freezeDataSpace(record.fId, 2)
    if (res.code === 200) {
      message.success('已冻结')
      fetchDataSpaces()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleThaw = async (record) => {
  try {
    const res = await dataspaceApi.freezeDataSpace(record.fId, 1)
    if (res.code === 200) {
      message.success('已解冻')
      fetchDataSpaces()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleDelete = async (record) => {
  try {
    const res = await dataspaceApi.deleteDataSpace(record.fId)
    if (res.code === 200) {
      message.success('删除成功')
      fetchDataSpaces()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

const handleMemberManage = (record) => {
  router.push({
    path: '/dataspace/member',
    query: { spaceId: record.fId, spaceName: record.fSpaceName }
  })
}

const handleJoinApply = (record) => {
  Object.assign(joinForm, {
    spaceId: record.fId,
    spaceName: record.fSpaceName,
    organizationId: '',
    organizationName: '',
    applyReason: ''
  })
  fetchOrganizations()
  joinVisible.value = true
}

const onJoinOrgChange = (orgId) => {
  const org = orgList.value.find(o => o.fId === orgId)
  if (org) {
    joinForm.organizationName = org.fOrgName
  }
}

const handleJoinSubmit = async () => {
  if (!joinForm.organizationId) {
    message.warning('请选择机构')
    return
  }
  joinLoading.value = true
  try {
    const res = await dataspaceApi.addMember({
      spaceId: joinForm.spaceId,
      organizationId: joinForm.organizationId,
      organizationName: joinForm.organizationName,
      role: 2,  // 成员角色
      applyReason: joinForm.applyReason
    })
    if (res.code === 200) {
      message.success('申请已提交，请等待空间管理员审批')
      joinVisible.value = false
    } else {
      message.error(res.msg || '申请失败')
    }
  } catch (error) {
    message.error('申请失败')
  } finally {
    joinLoading.value = false
  }
}

onMounted(() => {
  fetchDataSpaces()
})
</script>
