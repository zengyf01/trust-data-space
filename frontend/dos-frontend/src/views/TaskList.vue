<template>
  <div class="task-container">
    <a-layout-content class="content">
        <a-card title="任务列表">
          <template #extra>
            <a-button type="primary" @click="$router.push('/tasks/create')">创建任务</a-button>
          </template>
          <a-table :columns="columns" :data-source="tasks" :loading="loading" :pagination="pagination" @change="handleTableChange">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="getStatusColor(record.fStatus)">{{ getStatusText(record.fStatus) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'type'">
                <a-tag>{{ getTypeText(record.fType) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button size="small" type="primary" @click="handleExecute(record)">执行</a-button>
                  <a-button size="small" @click="handleDelete(record)" danger>删除</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-layout-content>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { taskAPI } from '../api'

const router = useRouter()
const selectedKeys = ref(['tasks'])
const menuItems = [
  { key: 'dashboard', label: '首页' },
  { key: 'tasks', label: '任务管理' },
  { key: 'nodes', label: '节点管理' },
  { key: 'datasources', label: '数据源' }
]

const loading = ref(false)
const tasks = ref([])
const pagination = ref({ current: 1, pageSize: 10, total: 0 })

const columns = [
  { title: '任务名称', dataIndex: 'fName', key: 'fName' },
  { title: '类型', key: 'type' },
  { title: '状态', key: 'status' },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime' },
  { title: '操作', key: 'action' }
]

const getStatusColor = (status) => {
  const colors = { 1: 'blue', 2: 'orange', 3: 'processing', 4: 'green', 5: 'red', 6: 'gray' }
  return colors[status] || 'default'
}
const getStatusText = (status) => {
  const texts = { 1: '已创建', 2: '待执行', 3: '执行中', 4: '已完成', 5: '失败', 6: '已取消' }
  return texts[status] || '未知'
}
const getTypeText = (type) => {
  const texts = { 1: 'PSI', 2: 'MPC', 3: '横向联邦', 4: '自定义', 5: '纵向联邦', 6: '复合任务' }
  return texts[type] || '未知'
}

const handleMenuClick = ({ key }) => router.push('/' + key)
const handleTableChange = (pag) => { pagination.value.current = pag.current; loadTasks() }

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await taskAPI.list({ page: pagination.value.current, size: pagination.value.pageSize })
    tasks.value = res.data.data?.list || []
    pagination.value.total = res.data.data?.pagination?.total || 0
  } catch (e) {
    message.error('加载任务失败')
  } finally {
    loading.value = false
  }
}

const handleExecute = async (record) => {
  try {
    await taskAPI.execute(record.fId)
    message.success('任务已启动')
    loadTasks()
  } catch (e) {
    message.error('执行失败')
  }
}

const handleDelete = async (record) => {
  try {
    await taskAPI.delete(record.fId)
    message.success('删除成功')
    loadTasks()
  } catch (e) {
    message.error('删除失败')
  }
}

onMounted(loadTasks)
</script>

<style scoped>
.task-container { height: 100vh; }
.header { background: #001529; padding: 0 24px; display: flex; align-items: center; gap: 24px; }
.header h1 { color: white; margin: 0; font-size: 18px; }
.content { padding: 24px; background: #f0f2f5; overflow-y: auto; }
</style>