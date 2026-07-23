<template>
  <div class="user-container">
    <a-layout-content class="content">
        <a-card title="用户列表">
          <template #extra>
            <a-button type="primary" @click="showCreateModal">创建用户</a-button>
          </template>
          <a-table :columns="columns" :data-source="users" :loading="loading" :pagination="pagination" @change="handleTableChange">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === '1' || record.status === 1 ? 'green' : 'red'">
                  {{ record.status === '1' || record.status === 1 ? '正常' : '禁用' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'enabled'">
                <a-switch :checked="record.enabled === 1" @change="(checked) => handleToggleEnabled(record, checked)" />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button size="small" @click="handleEdit(record)">编辑</a-button>
                  <a-button size="small" danger @click="handleDelete(record)">删除</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-layout-content>

    <!-- 创建/编辑用户弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑用户' : '创建用户'"
      @ok="handleSubmit"
      @cancel="modalVisible = false"
      :confirmLoading="submitLoading"
      width="500px"
    >
      <a-form :model="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="用户名" required>
          <a-input v-model:value="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="密码" :required="!isEdit">
          <a-input-password v-model:value="form.password" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="form.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="form.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="form.role" placeholder="请选择角色">
            <a-select-option value="ADMIN">管理员</a-select-option>
            <a-select-option value="USER">普通用户</a-select-option>
            <a-select-option value="OPERATOR">操作员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'

const loading = ref(false)
const users = ref([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const modalVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const form = ref({
  userId: null,
  username: '',
  password: '',
  email: '',
  phone: '',
  role: 'USER'
})

const columns = [
  { title: '用户ID', dataIndex: 'userId', key: 'userId' },
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '角色', dataIndex: 'role', key: 'role' },
  { title: '状态', key: 'status' },
  { title: '启用', key: 'enabled' },
  { title: '操作', key: 'action', width: 150 }
]

const showCreateModal = () => {
  form.value = {
    userId: null,
    username: '',
    password: '',
    email: '',
    phone: '',
    role: 'USER'
  }
  isEdit.value = false
  modalVisible.value = true
}

const handleEdit = (record) => {
  form.value = {
    userId: record.userId,
    username: record.username,
    password: '',
    email: record.email || '',
    phone: record.phone || '',
    role: record.role || 'USER'
  }
  isEdit.value = true
  modalVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.username) {
    message.error('请输入用户名')
    return
  }
  if (!isEdit.value && !form.value.password) {
    message.error('请输入密码')
    return
  }

  try {
    submitLoading.value = true
    let response

    if (isEdit.value) {
      response = await axios.put(`/api/user/${form.value.userId}`, {
        email: form.value.email,
        phone: form.value.phone,
        role: form.value.role
      })
    } else {
      response = await axios.post('/api/user', {
        username: form.value.username,
        password: form.value.password,
        email: form.value.email,
        phone: form.value.phone,
        role: form.value.role
      })
    }

    if (response.data.code === 200) {
      message.success(isEdit.value ? '用户更新成功' : '用户创建成功')
      modalVisible.value = false
      loadData()
    } else {
      message.error(response.data.msg || '操作失败')
    }
  } catch (error) {
    message.error(error.response?.data?.msg || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (record) => {
  try {
    const response = await axios.delete(`/api/user/${record.userId}`)
    if (response.data.code === 200) {
      message.success('用户删除成功')
      loadData()
    } else {
      message.error(response.data.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

const handleToggleEnabled = async (record, checked) => {
  try {
    const response = await axios.patch(`/api/user/${record.userId}/enabled?enabled=${checked}`)
    if (response.data.code === 200) {
      message.success(checked ? '用户已启用' : '用户已禁用')
      loadData()
    } else {
      message.error(response.data.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleTableChange = (pag) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

const loadData = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/user/page', {
      params: {
        currentPage: pagination.value.current,
        pageSize: pagination.value.pageSize
      }
    })

    if (response.data.code === 200) {
      users.value = response.data.data.list || []
      pagination.value.total = response.data.data.pagination?.total || 0
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-container {
  height: 100vh;
}
.header {
  background: #001529;
  color: white;
  padding: 0 24px;
}
.header h1 {
  color: white;
  margin: 0;
  line-height: 64px;
}
.content {
  padding: 24px;
}
</style>
