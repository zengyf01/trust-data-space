<template>
  <div class="task-manage-container">
    <a-layout-content class="content">
        <a-card title="任务列表">
          <template #extra>
            <a-space>
              <a-input-search v-model:value="searchKey" placeholder="搜索任务ID" style="width: 200px" @search="handleSearch" />
              <a-button type="primary" @click="handleRefresh">刷新</a-button>
            </a-space>
          </template>
          <a-table :columns="columns" :data-source="tasks" :loading="loading" :pagination="pagination">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="getStatusColor(record.status)">
                  {{ getStatusText(record.status) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'type'">
                <a-tag>{{ getTypeText(record.type) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button size="small" @click="handleDetail(record)">详情</a-button>
                  <a-button size="small" type="primary" @click="handleExecute(record)" v-if="record.status === 1">执行</a-button>
                  <a-button size="small" @click="handleQueryStatus(record)" v-if="[2, 3].includes(record.status)">查询状态</a-button>
                  <a-button size="small" danger @click="handleCancel(record)" v-if="![4, 5, 6].includes(record.status)">取消</a-button>
                  <a-button size="small" @click="handleGetResult(record)" v-if="record.status === 4">获取结果</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>

        <!-- 任务详情弹窗 -->
        <a-modal v-model:open="detailVisible" title="任务详情" :footer="null" width="600px">
          <a-descriptions :column="2" v-if="currentTask">
            <a-descriptions-item label="任务ID">{{ currentTask.taskId }}</a-descriptions-item>
            <a-descriptions-item label="任务名称">{{ currentTask.name }}</a-descriptions-item>
            <a-descriptions-item label="任务类型">{{ getTypeText(currentTask.type) }}</a-descriptions-item>
            <a-descriptions-item label="状态">{{ getStatusText(currentTask.status) }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ currentTask.createTime }}</a-descriptions-item>
            <a-descriptions-item label="节点模式">{{ currentTask.nodeMode }}</a-descriptions-item>
          </a-descriptions>
          <a-divider>执行结果</a-divider>
          <pre v-if="currentTaskResult">{{ currentTaskResult }}</pre>
          <a-empty v-else description="暂无结果" />
        </a-modal>
      </a-layout-content>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'

const loading = ref(false)
const tasks = ref([])
const searchKey = ref('')
const detailVisible = ref(false)
const currentTask = ref(null)
const currentTaskResult = ref('')

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '任务ID', dataIndex: 'taskId', key: 'taskId', ellipsis: true },
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '任务类型', key: 'type' },
  { title: '状态', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 280 }
]

const getStatusColor = (status) => {
  const colors = { 1: 'blue', 2: 'orange', 3: 'processing', 4: 'green', 5: 'red', 6: 'gray' }
  return colors[status] || 'default'
}

const getStatusText = (status) => {
  const texts = { 1: '已创建', 2: '等待中', 3: '运行中', 4: '已完成', 5: '失败', 6: '已取消' }
  return texts[status] || '未知'
}

const getTypeText = (type) => {
  const texts = { 1: 'PSI', 2: 'MPC', 3: '联邦学习', 5: '纵向联邦学习', 6: '复合任务' }
  return texts[type] || '未知'
}

const handleSearch = () => {
  message.info('搜索: ' + searchKey.value)
}

const handleRefresh = () => {
  loadTasks()
}

const loadTasks = () => {
  // 模拟数据
  tasks.value = [
    { taskId: 'task-001', name: 'PSI-客户匹配', type: 1, status: 4, createTime: '2024-06-15 10:00:00', nodeMode: 'RAY' },
    { taskId: 'task-002', name: 'MPC-安全求和', type: 2, status: 3, createTime: '2024-06-15 11:00:00', nodeMode: 'RAY' },
    { taskId: 'task-003', name: 'FL-模型训练', type: 3, status: 2, createTime: '2024-06-15 12:00:00', nodeMode: 'KUSCIA' },
    { taskId: 'task-004', name: 'VFL-纵向学习', type: 5, status: 1, createTime: '2024-06-15 13:00:00', nodeMode: 'RAY' }
  ]
  pagination.value.total = 4
}

const handleDetail = (record) => {
  currentTask.value = record
  detailVisible.value = true
}

const handleExecute = async (record) => {
  try {
    await axios.post(`/api/dos/privacy/task/${record.taskId}/execute`)
    message.success('任务已开始执行')
    loadTasks()
  } catch (error) {
    message.error('执行失败: ' + (error.message || '未知错误'))
  }
}

const handleQueryStatus = async (record) => {
  try {
    const response = await axios.get(`/api/dos/privacy/task/${record.taskId}/status`)
    const status = response.data.data.status
    message.info('当前状态: ' + getStatusText(status))
    loadTasks()
  } catch (error) {
    message.error('查询失败: ' + (error.message || '未知错误'))
  }
}

const handleCancel = async (record) => {
  try {
    await axios.post(`/api/dos/privacy/task/${record.taskId}/cancel`)
    message.success('任务已取消')
    loadTasks()
  } catch (error) {
    message.error('取消失败: ' + (error.message || '未知错误'))
  }
}

const handleGetResult = async (record) => {
  try {
    const response = await axios.get(`/api/dos/privacy/task/${record.taskId}/result`)
    currentTaskResult.value = response.data.data.result
    currentTask.value = record
    detailVisible.value = true
  } catch (error) {
    message.error('获取结果失败: ' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.task-manage-container {
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