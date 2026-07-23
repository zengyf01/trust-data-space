<template>
  <div>
    <a-card title="用户列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.keyword" placeholder="请输入用户名/姓名" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 100px">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
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
        :data-source="userList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="fId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fStatus'">
            <a-tag :color="record.fStatus === 1 ? 'green' : 'red'">
              {{ record.fStatus === 1 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleAssignRole(record)">分配角色</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="用户详情"
      width="600px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentUser">
        <a-descriptions-item label="用户名">{{ currentUser.fUsername }}</a-descriptions-item>
        <a-descriptions-item label="真实姓名">{{ currentUser.fRealName }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentUser.fStatus === 1 ? 'green' : 'red'">
            {{ currentUser.fStatus === 1 ? '正常' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="手机号">{{ currentUser.fPhone }}</a-descriptions-item>
        <a-descriptions-item label="邮箱">{{ currentUser.fEmail }}</a-descriptions-item>
        <a-descriptions-item label="用户类型">{{ currentUser.fUserType }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ currentUser.fCreateTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="isEdit ? '编辑用户' : '新建用户'"
      width="500px"
      @ok="handleFormSubmit"
      :confirmLoading="formLoading"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" ref="formRef">
        <a-form-item label="用户名" name="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <a-input v-model:value="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </a-form-item>
        <a-form-item v-if="!isEdit" label="密码" name="password" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model:value="formData.password" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item label="真实姓名" name="realName">
          <a-input v-model:value="formData.realName" placeholder="请输入真实姓名" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formData.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="formData.status" placeholder="请选择状态">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 分配角色弹窗 -->
    <a-modal
      v-model:open="roleVisible"
      title="分配角色"
      @ok="handleRoleSubmit"
      :confirmLoading="roleLoading"
    >
      <a-form :model="roleForm" :label-col="{ span: 6 }">
        <a-form-item label="用户名">{{ currentUser?.fUsername }}</a-form-item>
        <a-form-item label="选择角色">
          <a-select v-model:value="roleForm.roleIds" mode="multiple" placeholder="请选择角色">
            <a-select-option v-for="role in roleList" :key="role.fId" :value="role.fId">
              {{ role.fRoleName }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import organizationApi from '@/api/organization'

const columns = [
  { title: '用户名', dataIndex: 'fUsername', key: 'fUsername', width: 150 },
  { title: '真实姓名', dataIndex: 'fRealName', key: 'fRealName', width: 120 },
  { title: '手机号', dataIndex: 'fPhone', key: 'fPhone', width: 130 },
  { title: '邮箱', dataIndex: 'fEmail', key: 'fEmail' },
  { title: '状态', dataIndex: 'fStatus', key: 'fStatus', width: 80 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 250 }
]

const userList = ref([])
const roleList = ref([])
const loading = ref(false)
const searchForm = reactive({
  keyword: '',
  status: null
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentUser = ref(null)

const formVisible = ref(false)
const isEdit = ref(false)
const formLoading = ref(false)
const formData = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1
})
const formRef = ref(null)

const roleVisible = ref(false)
const roleLoading = ref(false)
const roleForm = reactive({
  roleIds: []
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: pagination.current,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status !== null && searchForm.status !== undefined ? searchForm.status : undefined
    }
    const res = await organizationApi.getUserPage(params)
    if (res.code === 200) {
      userList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  try {
    const res = await organizationApi.getRolePage({ currentPage: 1, pageSize: 100 })
    if (res.code === 200) {
      roleList.value = res.data.list
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchUsers()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchUsers()
}

const handleView = (record) => {
  currentUser.value = record
  detailVisible.value = true
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    status: 1
  })
  formVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  currentUser.value = record
  Object.assign(formData, {
    id: record.fId,
    username: record.fUsername,
    password: '',
    realName: record.fRealName,
    phone: record.fPhone,
    email: record.fEmail,
    status: record.fStatus
  })
  formVisible.value = true
}

const handleFormSubmit = async () => {
  formLoading.value = true
  try {
    const res = isEdit.value
      ? await organizationApi.updateUser(formData.id, formData)
      : await organizationApi.createUser(formData)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      formVisible.value = false
      fetchUsers()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleAssignRole = async (record) => {
  currentUser.value = record
  roleForm.roleIds = []
  roleVisible.value = true
  fetchRoles()
  // 获取用户当前角色
  try {
    const res = await organizationApi.getUserRoles(record.fId)
    if (res.code === 200 && res.data) {
      roleForm.roleIds = res.data.map(r => r.fRoleId || r.fId)
    }
  } catch (error) {
    console.error('获取用户角色失败:', error)
  }
}

const handleRoleSubmit = async () => {
  if (roleForm.roleIds.length === 0) {
    message.warning('请至少选择一个角色')
    return
  }
  roleLoading.value = true
  try {
    const res = await organizationApi.assignUserRoles(currentUser.value.fId, roleForm.roleIds.join(','))
    if (res.code === 200) {
      message.success('分配成功')
      roleVisible.value = false
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    roleLoading.value = false
  }
}

const handleDelete = async (record) => {
  try {
    const res = await organizationApi.deleteUser(record.fId)
    if (res.code === 200) {
      message.success('删除成功')
      fetchUsers()
    } else {
      message.error(res.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>
