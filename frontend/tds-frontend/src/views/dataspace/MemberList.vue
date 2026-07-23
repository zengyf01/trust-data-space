<template>
  <div>
    <a-card title="空间成员管理" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="空间名称">
          <a-input v-model:value="spaceName" disabled style="width: 200px" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="searchForm.role" placeholder="请选择角色" allowClear style="width: 120px">
            <a-select-option :value="0">所有者</a-select-option>
            <a-select-option :value="1">管理员</a-select-option>
            <a-select-option :value="2">成员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 120px">
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">正常</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
          <a-button type="primary" style="margin-left: 8px" @click="handleAddMember">添加成员</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card>
      <a-table
        :columns="columns"
        :data-source="memberList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="fId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'role'">
            <a-tag :color="getRoleColor(record.fRole)">
              {{ getRoleText(record.fRole) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.fStatus === 1 ? 'green' : 'orange'">
              {{ record.fStatus === 1 ? '正常' : '待审核' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button
                v-if="record.fStatus === 0"
                type="link"
                size="small"
                @click="handleApprove(record)"
              >
                审核
              </a-button>
              <a-button
                v-if="record.fStatus === 1 && record.fRole !== 1"
                type="link"
                size="small"
                @click="handleChangeRole(record)"
              >
                改角色
              </a-button>
              <a-button
                v-if="record.fRole !== 1"
                type="link"
                size="small"
                danger
                @click="handleRemove(record)"
              >
                移除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="成员详情"
      width="500px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentMember">
        <a-descriptions-item label="成员ID">{{ currentMember.fId }}</a-descriptions-item>
        <a-descriptions-item label="角色">
          <a-tag :color="getRoleColor(currentMember.fRole)">
            {{ getRoleText(currentMember.fRole) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentMember.fStatus === 1 ? 'green' : 'orange'">
            {{ currentMember.fStatus === 1 ? '正常' : '待审核' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="机构名称">{{ currentMember.fOrganizationName }}</a-descriptions-item>
        <a-descriptions-item label="加入时间">{{ currentMember.fJoinTime }}</a-descriptions-item>
        <a-descriptions-item label="申请理由" :span="2">{{ currentMember.fApplyReason || '无' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 添加成员弹窗 -->
    <a-modal
      v-model:open="addVisible"
      title="添加成员"
      @ok="handleAddSubmit"
      :confirmLoading="addLoading"
    >
      <a-form :model="addForm" :label-col="{ span: 6 }" ref="addFormRef">
        <a-form-item label="机构名称" name="organizationId" :rules="[{ required: true, message: '请选择机构' }]">
          <a-select v-model:value="addForm.organizationId" placeholder="请选择机构" @change="onAddOrgChange">
            <a-select-option v-for="org in orgList" :key="org.fId" :value="org.fId">
              {{ org.fOrgName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="角色" name="role">
          <a-select v-model:value="addForm.role" placeholder="请选择角色">
            <a-select-option :value="2">管理员</a-select-option>
            <a-select-option :value="3">成员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="申请理由" name="applyReason">
          <a-textarea v-model:value="addForm.applyReason" :rows="3" placeholder="请输入申请理由" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 修改角色弹窗 -->
    <a-modal
      v-model:open="roleVisible"
      title="修改成员角色"
      @ok="handleRoleSubmit"
      :confirmLoading="roleLoading"
    >
      <a-form :model="roleForm" :label-col="{ span: 6 }" ref="roleFormRef">
        <a-form-item label="机构名称">
          <a-input v-model:value="roleForm.organizationName" disabled />
        </a-form-item>
        <a-form-item label="新角色" name="role" :rules="[{ required: true, message: '请选择角色' }]">
          <a-select v-model:value="roleForm.role" placeholder="请选择角色">
            <a-select-option :value="2">管理员</a-select-option>
            <a-select-option :value="3">成员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import dataspaceApi from '@/api/dataspace'
import organizationApi from '@/api/organization'

const route = useRoute()
const spaceId = ref(route.query.spaceId || '')
const spaceName = ref(route.query.spaceName || '')

const columns = [
  { title: '机构名称', dataIndex: 'fOrganizationName', key: 'fOrganizationName', width: 150 },
  { title: '角色', dataIndex: 'fRole', key: 'role', width: 100 },
  { title: '状态', dataIndex: 'fStatus', key: 'status', width: 100 },
  { title: '加入时间', dataIndex: 'fJoinTime', key: 'fJoinTime', width: 180 },
  { title: '操作', key: 'action', width: 220 }
]

const memberList = ref([])
const orgList = ref([])
const loading = ref(false)
const searchForm = reactive({
  role: null,
  status: null
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentMember = ref(null)

const addVisible = ref(false)
const addLoading = ref(false)
const addForm = reactive({
  organizationId: '',
  organizationName: '',
  role: 2,
  applyReason: ''
})
const addFormRef = ref(null)

const roleVisible = ref(false)
const roleLoading = ref(false)
const roleForm = reactive({
  id: '',
  organizationName: '',
  role: null
})
const roleFormRef = ref(null)

const roleMap = {
  1: { text: '所有者', color: 'blue' },
  2: { text: '管理员', color: 'purple' },
  3: { text: '成员', color: 'green' }
}

const getRoleText = (role) => roleMap[role]?.text || '未知'
const getRoleColor = (role) => roleMap[role]?.color || 'default'

const fetchMembers = async () => {
  if (!spaceId.value) return
  loading.value = true
  try {
    const params = {
      currentPage: pagination.current,
      pageSize: pagination.pageSize,
      spaceId: spaceId.value,
      role: searchForm.role || undefined,
      status: searchForm.status || undefined
    }
    const res = await dataspaceApi.getMemberPage(params)
    if (res.code === 200) {
      memberList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取成员列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchMembers()
}

const handleReset = () => {
  searchForm.role = null
  searchForm.status = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchMembers()
}

const handleView = (record) => {
  currentMember.value = record
  detailVisible.value = true
}

const handleAddMember = () => {
  Object.assign(addForm, {
    organizationId: '',
    organizationName: '',
    role: 2,
    applyReason: ''
  })
  fetchOrganizations()
  addVisible.value = true
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

const onAddOrgChange = (orgId) => {
  const org = orgList.value.find(o => o.fId === orgId)
  if (org) {
    addForm.organizationName = org.fOrgName
  }
}

const handleAddSubmit = async () => {
  addLoading.value = true
  try {
    const res = await dataspaceApi.addMember({
      spaceId: spaceId.value,
      ...addForm
    })
    if (res.code === 200) {
      message.success('添加成功')
      addVisible.value = false
      fetchMembers()
    } else {
      message.error(res.msg || '添加失败')
    }
  } catch (error) {
    message.error('添加失败')
  } finally {
    addLoading.value = false
  }
}

const handleApprove = async (record) => {
  try {
    const res = await dataspaceApi.approveMember(record.fId, 1)
    if (res.code === 200) {
      message.success('审核通过')
      fetchMembers()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleChangeRole = (record) => {
  Object.assign(roleForm, {
    id: record.fId,
    organizationName: record.fOrganizationName,
    role: record.fRole
  })
  roleVisible.value = true
}

const handleRoleSubmit = async () => {
  if (!roleForm.role) {
    message.warning('请选择角色')
    return
  }
  roleLoading.value = true
  try {
    const res = await dataspaceApi.updateMemberRole(roleForm.id, roleForm.role)
    if (res.code === 200) {
      message.success('修改成功')
      roleVisible.value = false
      fetchMembers()
    } else {
      message.error(res.msg || '修改失败')
    }
  } catch (error) {
    message.error('修改失败')
  } finally {
    roleLoading.value = false
  }
}

const handleRemove = async (record) => {
  try {
    const res = await dataspaceApi.removeMember(record.fId)
    if (res.code === 200) {
      message.success('移除成功')
      fetchMembers()
    } else {
      message.error(res.msg || '移除失败')
    }
  } catch (error) {
    message.error('移除失败')
  }
}

onMounted(() => {
  fetchMembers()
})
</script>