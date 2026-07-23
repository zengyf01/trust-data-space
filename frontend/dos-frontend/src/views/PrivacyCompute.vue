<template>
  <div>
    <a-card title="隐私计算任务列表">
      <template #extra>
        <a-space>
          <a-input-search v-model:value="searchKey" placeholder="搜索任务ID" style="width: 200px" @search="handleSearchTask" />
          <a-button type="primary" @click="handleCreate">新增</a-button>
          <a-button @click="handleRefreshTasks">刷新</a-button>
        </a-space>
      </template>
      <a-table :columns="taskColumns" :data-source="tasks" :loading="tasksLoading" :pagination="taskPagination">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getTaskStatusColor(record.status)">
              {{ getTaskStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'type'">
            <a-tag>{{ getTaskTypeText(record.type) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="handleTaskDetail(record)">详情</a-button>
              <a-button size="small" type="primary" @click="handleExecuteTask(record)" v-if="record.status === 1">执行</a-button>
              <a-button size="small" @click="handleQueryTaskStatus(record)" v-if="[2, 3].includes(record.status)">查询状态</a-button>
              <a-button size="small" danger @click="handleCancelTask(record)" v-if="![4, 5, 6].includes(record.status)">取消</a-button>
              <a-button size="small" @click="handleGetTaskResult(record)" v-if="record.status === 4">获取结果</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 任务详情弹窗 -->
    <a-modal v-model:open="taskDetailVisible" title="任务详情" :footer="null" width="600px">
      <a-descriptions :column="2" v-if="currentTask">
        <a-descriptions-item label="任务ID">{{ currentTask.taskId }}</a-descriptions-item>
        <a-descriptions-item label="任务名称">{{ currentTask.name }}</a-descriptions-item>
        <a-descriptions-item label="任务类型">{{ getTaskTypeText(currentTask.type) }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ getTaskStatusText(currentTask.status) }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ currentTask.createTime }}</a-descriptions-item>
        <a-descriptions-item label="节点模式">{{ currentTask.nodeMode }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>执行结果</a-divider>
      <pre v-if="currentTaskResult">{{ currentTaskResult }}</pre>
      <a-empty v-else description="暂无结果" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import axios from 'axios'

const router = useRouter()

// 任务列表相关
const tasksLoading = ref(false)
const tasks = ref([])
const searchKey = ref('')
const taskDetailVisible = ref(false)
const currentTask = ref(null)
const currentTaskResult = ref('')

const taskPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const taskColumns = [
  { title: '任务ID', dataIndex: 'taskId', key: 'taskId', ellipsis: true },
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '任务类型', key: 'type' },
  { title: '状态', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 280 }
]

const getTaskStatusColor = (status) => {
  const colors = { 1: 'blue', 2: 'orange', 3: 'processing', 4: 'green', 5: 'red', 6: 'gray' }
  return colors[status] || 'default'
}

const getTaskStatusText = (status) => {
  const texts = { 1: '已创建', 2: '等待中', 3: '运行中', 4: '已完成', 5: '失败', 6: '已取消' }
  return texts[status] || '未知'
}

const getTaskTypeText = (type) => {
  const texts = { 1: 'PSI', 2: 'MPC', 3: '联邦学习', 5: '纵向联邦学习', 6: 'DAG任务' }
  return texts[type] || '未知'
}

const handleCreate = () => {
  router.push('/privacy/create')
}

const handleSearchTask = () => {
  message.info('搜索: ' + searchKey.value)
}

const handleRefreshTasks = () => {
  loadTasks()
}

const loadTasks = () => {
  tasksLoading.value = true
  // 模拟数据
  tasks.value = [
    { taskId: 'task-001', name: 'PSI-客户匹配', type: 1, status: 4, createTime: '2024-06-15 10:00:00', nodeMode: 'RAY' },
    { taskId: 'task-002', name: 'MPC-安全求和', type: 2, status: 3, createTime: '2024-06-15 11:00:00', nodeMode: 'RAY' },
    { taskId: 'task-003', name: 'FL-模型训练', type: 3, status: 2, createTime: '2024-06-15 12:00:00', nodeMode: 'KUSCIA' },
    { taskId: 'task-004', name: 'VFL-纵向学习', type: 5, status: 1, createTime: '2024-06-15 13:00:00', nodeMode: 'RAY' },
    { taskId: 'task-005', name: 'DAG-数据处理', type: 6, status: 4, createTime: '2024-06-15 14:00:00', nodeMode: 'RAY' }
  ]
  taskPagination.value.total = 5
  tasksLoading.value = false
}

const handleTaskDetail = (record) => {
  currentTask.value = record
  taskDetailVisible.value = true
}

const handleExecuteTask = async (record) => {
  try {
    await axios.post(`/api/dos/privacy/task/${record.taskId}/execute`)
    message.success('任务已开始执行')
    loadTasks()
  } catch (error) {
    message.error('执行失败: ' + (error.message || '未知错误'))
  }
}

const handleQueryTaskStatus = async (record) => {
  try {
    const response = await axios.get(`/api/dos/privacy/task/${record.taskId}/status`)
    const status = response.data.data.status
    message.info('当前状态: ' + getTaskStatusText(status))
    loadTasks()
  } catch (error) {
    message.error('查询失败: ' + (error.message || '未知错误'))
  }
}

const handleCancelTask = async (record) => {
  try {
    await axios.post(`/api/dos/privacy/task/${record.taskId}/cancel`)
    message.success('任务已取消')
    loadTasks()
  } catch (error) {
    message.error('取消失败: ' + (error.message || '未知错误'))
  }
}

const handleGetTaskResult = async (record) => {
  try {
    const response = await axios.get(`/api/dos/privacy/task/${record.taskId}/result`)
    currentTaskResult.value = response.data.data
    taskDetailVisible.value = true
  } catch (error) {
    message.error('获取结果失败: ' + (error.message || '未知错误'))
  }
}

// 初始化加载任务列表
loadTasks()
</script>

<style scoped>
/* DAG 样式 */
.component-item {
  padding: 8px 12px;
  margin: 4px 0;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}
.component-item:hover {
  background: #f0f0f0;
}
.canvas-card :deep(.ant-card-body) {
  padding: 12px;
}
.dag-canvas {
  width: 100%;
  height: 400px;
  position: relative;
  background: #fafafa;
  background-image: radial-gradient(circle, #ddd 1px, transparent 1px);
  background-size: 20px 20px;
  overflow: auto;
}
.canvas-empty {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #999;
}
.nodes-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-width: 600px;
  min-height: 400px;
}
.edges-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.dag-node {
  position: absolute;
  width: 100px;
  min-height: 60px;
  background: white;
  border: 2px solid #d9d9d9;
  border-radius: 4px;
  cursor: move;
  user-select: none;
}
.dag-node:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.dag-node.selected {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24,144,255,0.2);
}
.dag-node.category-data { border-left: 3px solid #1890ff; }
.dag-node.category-alignment { border-left: 3px solid #722ed1; }
.dag-node.category-filter { border-left: 3px solid #fa8c16; }
.dag-node.category-preprocessing { border-left: 3px solid #52c41a; }
.dag-node.category-model { border-left: 3px solid #f5222d; }
.dag-node.category-output { border-left: 3px solid #13c2c2; }
.node-header {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.node-label {
  flex: 1;
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.node-delete {
  font-size: 10px;
  color: #999;
  cursor: pointer;
}
.node-delete:hover {
  color: #f5222d;
}
.node-ports {
  display: flex;
  justify-content: space-between;
  padding: 4px 8px;
}
.port {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  color: #666;
  cursor: crosshair;
}
.port-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d9d9d9;
  border: 1px solid #999;
}
.port:hover .port-dot {
  background: #1890ff;
  border-color: #1890ff;
}
.canvas-toolbar {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
}
</style>