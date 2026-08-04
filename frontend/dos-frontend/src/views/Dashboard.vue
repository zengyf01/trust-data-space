<template>
  <div class="dashboard-container">
    <a-layout-content class="content">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-card>
              <a-statistic title="总任务数" :value="stats.totalTasks" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <a-statistic title="运行中" :value="stats.runningTasks" status="processing" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <a-statistic title="在线节点" :value="stats.onlineNodes" status="success" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <a-statistic title="完成任务" :value="stats.completedTasks" status="success" />
            </a-card>
          </a-col>
        </a-row>

        <a-card title="任务状态分布" style="margin-top: 16px">
          <template #extra>
            <a-button type="link" @click="$router.push('/tasks')">查看全部</a-button>
          </template>
          <a-table :columns="taskColumns" :data-source="recentTasks" :pagination="false">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="getStatusColor(record.fStatus)">{{ getStatusText(record.fStatus) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'type'">
                <a-tag>{{ getTypeText(record.fType) }}</a-tag>
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
import { taskAPI, nodeAPI } from '../api'

const router = useRouter()
const selectedKeys = ref(['dashboard'])
const menuItems = [
  { key: 'dashboard', label: '首页' },
  { key: 'tasks', label: '任务管理' },
  { key: 'nodes', label: '节点管理' },
  { key: 'datasources', label: '数据源' }
]

const stats = ref({ totalTasks: 0, runningTasks: 0, onlineNodes: 0, completedTasks: 0 })
const recentTasks = ref([])

const taskColumns = [
  { title: '任务名称', dataIndex: 'fName', key: 'fName' },
  { title: '类型', key: 'type' },
  { title: '状态', key: 'status' },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime' }
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

const handleMenuClick = ({ key }) => {
  router.push('/' + key)
}

onMounted(async () => {
  try {
    const taskRes = await taskAPI.list({ page: 1, size: 100 })
    const tasks = taskRes.data.data?.list || []
    recentTasks.value = tasks.slice(0, 5)
    stats.value.totalTasks = taskRes.data.data?.pagination?.total || tasks.length
    stats.value.runningTasks = tasks.filter(t => t.fStatus === 3).length
    stats.value.completedTasks = tasks.filter(t => t.fStatus === 4).length
  } catch (e) {
    console.error('Failed to load tasks', e)
  }

  try {
    const nodeRes = await nodeAPI.list({ page: 1, size: 100 })
    const nodes = nodeRes.data.data?.list || []
    stats.value.onlineNodes = nodes.filter(n => n.fStatus === 1).length
  } catch (e) {
    console.error('Failed to load nodes', e)
  }
})
</script>

<style scoped>
.dashboard-container { height: 100vh; }
.header { background: #001529; padding: 0 24px; display: flex; align-items: center; gap: 24px; }
.header h1 { color: white; margin: 0; font-size: 18px; }
.content { padding: 24px; background: #f0f2f5; overflow-y: auto; }
</style>