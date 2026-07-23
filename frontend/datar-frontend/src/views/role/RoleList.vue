<template>
  <div>
    <a-card title="角色列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item>
          <a-button type="primary" @click="handleCreate">新建</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card>
      <a-table
        :columns="columns"
        :data-source="roleList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="fId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fIsSystem'">
            <a-tag :color="record.fIsSystem === 1 ? 'blue' : 'default'">
              {{ record.fIsSystem === 1 ? '系统角色' : '自定义' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button v-if="record.fIsSystem !== 1" type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button v-if="record.fIsSystem !== 1" type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="角色详情"
      width="500px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentRole">
        <a-descriptions-item label="角色编码">{{ currentRole.fRoleCode }}</a-descriptions-item>
        <a-descriptions-item label="角色名称">{{ currentRole.fRoleName }}</a-descriptions-item>
        <a-descriptions-item label="角色类型">{{ currentRole.fRoleType }}</a-descriptions-item>
        <a-descriptions-item label="角色性质">
          <a-tag :color="currentRole.fIsSystem === 1 ? 'blue' : 'default'">
            {{ currentRole.fIsSystem === 1 ? '系统角色' : '自定义' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ currentRole.fCreateTime }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ currentRole.fRoleDesc }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="isEdit ? '编辑角色' : '新建角色'"
      width="500px"
      @ok="handleFormSubmit"
      :confirmLoading="formLoading"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" ref="formRef">
        <a-form-item label="角色名称" name="roleName" :rules="[{ required: true, message: '请输入角色名称' }]">
          <a-input v-model:value="formData.roleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item label="角色类型" name="roleType">
          <a-select v-model:value="formData.roleType" placeholder="请选择角色类型">
            <a-select-option value="SYSTEM">系统角色</a-select-option>
            <a-select-option value="BUSINESS">业务角色</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述" name="roleDesc">
          <a-textarea v-model:value="formData.roleDesc" :rows="3" placeholder="请输入描述" />
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
  { title: '角色编码', dataIndex: 'fRoleCode', key: 'fRoleCode', width: 150 },
  { title: '角色名称', dataIndex: 'fRoleName', key: 'fRoleName', width: 150 },
  { title: '角色类型', dataIndex: 'fRoleType', key: 'fRoleType', width: 100 },
  { title: '角色性质', dataIndex: 'fIsSystem', key: 'fIsSystem', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 180 }
]

const roleList = ref([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentRole = ref(null)

const formVisible = ref(false)
const isEdit = ref(false)
const formLoading = ref(false)
const formData = reactive({
  roleName: '',
  roleType: 'BUSINESS',
  roleDesc: ''
})
const formRef = ref(null)

const fetchRoles = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await organizationApi.getRolePage(params)
    if (res.code === 200) {
      roleList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchRoles()
}

const handleView = (record) => {
  currentRole.value = record
  detailVisible.value = true
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    roleName: '',
    roleType: 'BUSINESS',
    roleDesc: ''
  })
  formVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  currentRole.value = record
  Object.assign(formData, {
    id: record.fId,
    roleName: record.fRoleName,
    roleType: record.fRoleType,
    roleDesc: record.fRoleDesc
  })
  formVisible.value = true
}

const handleFormSubmit = async () => {
  formLoading.value = true
  try {
    const res = isEdit.value
      ? await organizationApi.updateRole(formData.id, formData)
      : await organizationApi.createRole(formData)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      formVisible.value = false
      fetchRoles()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (record) => {
  try {
    const res = await organizationApi.deleteRole(record.fId)
    if (res.code === 200) {
      message.success('删除成功')
      fetchRoles()
    } else {
      message.error(res.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchRoles()
})
</script>
