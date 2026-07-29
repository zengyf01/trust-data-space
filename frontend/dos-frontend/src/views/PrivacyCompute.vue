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
              <a-button size="small" type="primary" @click="handleExecuteTask(record)" v-if="record.status === 1" :loading="executingTaskId === record.taskId">
                {{ executingTaskId === record.taskId ? '执行中...' : '执行' }}
              </a-button>
              <a-button size="small" @click="handleQueryTaskStatus(record)" v-if="[2, 3].includes(record.status)">查询状态</a-button>
              <a-button size="small" danger @click="handleCancelTask(record)" v-if="![4, 5, 6].includes(record.status)">取消</a-button>
              <a-button size="small" type="primary" @click="handleGetTaskResult(record)" v-if="record.status === 4">查看结果</a-button>
              <a-button size="small" danger @click="handleDeleteTask(record)" v-if="record.status === 6">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 任务详情弹窗 -->
    <a-modal v-model:open="taskDetailVisible" title="任务详情" :footer="null" width="1000px">
      <a-tabs v-if="currentTask" v-model:activeKey="detailActiveTab">
        <!-- 基本信息页签 -->
        <a-tab-pane key="basic" tab="基本信息">
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="任务ID" :span="2">
              <a-typography-paragraph copyable style="margin: 0">{{ currentTask.taskId }}</a-typography-paragraph>
            </a-descriptions-item>
            <a-descriptions-item label="任务名称">{{ currentTask.name || '-' }}</a-descriptions-item>
            <a-descriptions-item label="任务类型">
              <a-tag>{{ getTaskTypeText(currentTask.type) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="getTaskStatusColor(currentTask.status)">{{ getTaskStatusText(currentTask.status) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ currentTask.createTime || '-' }}</a-descriptions-item>
            <a-descriptions-item label="算法">{{ currentTask.algorithm || '-' }}</a-descriptions-item>
            <a-descriptions-item label="节点模式">{{ currentTask.nodeMode || 'RAY' }}</a-descriptions-item>
            <a-descriptions-item label="描述" :span="2">{{ currentTask.description || '-' }}</a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <!-- 任务参数页签 -->
        <a-tab-pane key="params" tab="任务参数">
          <a-empty v-if="!taskParams || Object.keys(taskParams).length === 0" description="暂无任务参数" />
          <a-descriptions v-else :column="2" size="small" bordered>
            <a-descriptions-item v-for="(value, key) in taskParams" :key="key" :label="formatParamKey(key)">
              {{ value || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>

        <!-- PSI脚本页签 -->
        <a-tab-pane key="code" tab="PSI脚本">
          <a-spin v-if="loadingCode" tip="加载代码中..." />
          <template v-else>
            <a-empty v-if="!taskCode" description="暂无生成的代码，任务创建时生成" />
            <div v-else>
              <a-space style="margin-bottom: 12px">
                <a-button type="primary" @click="handleCopyCode">
                  <template #icon><CopyOutlined /></template>
                  复制代码
                </a-button>
                <a-tag color="processing">代码长度: {{ taskCode.length }} 字符</a-tag>
              </a-space>
              <a-typography-text code style="display: block; max-height: 500px; overflow-y: auto; white-space: pre-wrap; word-break: break-all;">
                {{ taskCode }}
              </a-typography-text>
            </div>
          </template>
        </a-tab-pane>

        <!-- 执行结果页签 -->
        <a-tab-pane key="result" tab="执行结果">
          <template v-if="currentTask.status === 4">
            <a-empty v-if="!parsedResult" description="暂无执行结果" />
            <a-descriptions v-else :column="2" size="small" bordered>
              <a-descriptions-item label="协议">{{ parsedResult.protocol || 'ECPSI' }}</a-descriptions-item>
              <a-descriptions-item label="Ray Head">{{ parsedResult.ray_head_url || '-' }}</a-descriptions-item>
              <a-descriptions-item label="Job ID" :span="2">
                <a-typography-paragraph copyable style="margin: 0">{{ parsedResult.job_id || '-' }}</a-typography-paragraph>
              </a-descriptions-item>
              <a-descriptions-item label="A方数据量">{{ parsedResult.party_a_count || 0 }}</a-descriptions-item>
              <a-descriptions-item label="B方数据量">{{ parsedResult.party_b_count || 0 }}</a-descriptions-item>
              <a-descriptions-item label="交集数量" :span="2">
                <a-badge :count="parsedResult.intersection_count || 0" :number-style="{backgroundColor: '#52c41a'}" />
              </a-descriptions-item>
              <a-descriptions-item label="交集样例" :span="2">
                <a-tag v-for="id in (parsedResult.sample_result || [])" :key="id" style="margin: 2px">{{ id }}</a-tag>
                <span v-if="!parsedResult.sample_result || parsedResult.sample_result.length === 0">无</span>
              </a-descriptions-item>
              <a-descriptions-item label="执行消息" :span="2">{{ parsedResult.message || '-' }}</a-descriptions-item>
            </a-descriptions>
          </template>
          <a-result v-else-if="currentTask.status === 5" status="error" title="任务执行失败" :subTitle="parsedResult?.message || '任务执行失败'" />
          <a-result v-else-if="currentTask.status === 6" status="warning" title="任务已取消" />
          <a-result v-else status="info" title="任务尚未执行" subTitle="请先执行任务后查看结果" />
        </a-tab-pane>

        <!-- 执行日志页签 -->
        <a-tab-pane key="logs" tab="执行日志">
          <template v-if="currentTask.status === 4 || currentTask.status === 5">
            <a-spin v-if="loadingLogs" tip="加载日志中..." />
            <template v-else>
              <a-empty v-if="!taskLogs" description="暂无执行日志" />
              <div v-else>
                <a-space style="margin-bottom: 12px">
                  <a-button type="primary" @click="handleCopyLogs">
                    <template #icon><CopyOutlined /></template>
                    复制日志
                  </a-button>
                </a-space>
                <pre style="max-height: 500px; overflow-y: auto; background: #1e1e1e; color: #d4d4d4; padding: 12px; border-radius: 4px; white-space: pre-wrap; word-break: break-all;">{{ taskLogs }}</pre>
              </div>
            </template>
          </template>
          <a-result v-else status="info" title="暂无日志" subTitle="任务执行完成后可查看日志" />
        </a-tab-pane>
      </a-tabs>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { CopyOutlined } from '@ant-design/icons-vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

// 任务列表相关
const tasksLoading = ref(false)
const tasks = ref([])
const searchKey = ref('')
const taskDetailVisible = ref(false)
const currentTask = ref(null)
const currentTaskResult = ref('')
const executingTaskId = ref(null)
const detailActiveTab = ref('basic')
const taskParams = ref({})
const taskCode = ref('')
const taskLogs = ref('')
const loadingCode = ref(false)
const loadingLogs = ref(false)

const taskPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

// 解析执行结果
const parsedResult = computed(() => {
  if (!currentTaskResult.value) return null
  try {
    let result
    if (typeof currentTaskResult.value === 'string') {
      result = JSON.parse(currentTaskResult.value)
    } else {
      result = currentTaskResult.value
    }
    return result
  } catch {
    return null
  }
})

// 格式化参数key为中文
const formatParamKey = (key) => {
  const keyMap = {
    'partyADataPath': 'A方数据路径',
    'partyBDataPath': 'B方数据路径',
    'keyColumn': '关联键',
    'protocol': '协议',
    'resultType': '结果类型',
    'nodeMode': '节点模式',
    'partyANodeId': 'A方节点ID',
    'partyBNodeId': 'B方节点ID',
    'computeType': '计算类型'
  }
  return keyMap[key] || key
}

// 复制代码
const handleCopyCode = async () => {
  try {
    await navigator.clipboard.writeText(taskCode.value)
    message.success('代码已复制到剪贴板')
  } catch {
    message.error('复制失败')
  }
}

// 复制日志
const handleCopyLogs = async () => {
  try {
    await navigator.clipboard.writeText(taskLogs.value)
    message.success('日志已复制到剪贴板')
  } catch {
    message.error('复制失败')
  }
}

// 从创建页返回时刷新列表
watch(() => route.query.refresh, (val) => {
  if (val === '1') {
    loadTasks()
  }
})

const taskColumns = [
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
  const texts = { 1: '待执行', 2: '等待中', 3: '运行中', 4: '已完成', 5: '失败', 6: '已取消' }
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

const loadTasks = async () => {
  tasksLoading.value = true
  try {
    const response = await axios.get('/api/dos/privacy/task/list', {
      params: { page: taskPagination.value.current, size: taskPagination.value.pageSize }
    })
    if (response.data.code === 200) {
      tasks.value = response.data.data?.list || []
      taskPagination.value.total = response.data.data?.pagination?.total || 0
    } else {
      message.error('加载任务列表失败: ' + (response.data.msg || '未知错误'))
    }
  } catch (error) {
    message.error('加载任务列表失败: ' + (error.message || '未知错误'))
  } finally {
    tasksLoading.value = false
  }
}

const handleTaskDetail = async (record) => {
  currentTask.value = record
  currentTaskResult.value = ''
  taskParams.value = {}
  taskCode.value = ''
  taskLogs.value = ''
  detailActiveTab.value = 'basic'

  // 如果有parameters参数，解析显示
  if (record.parameters) {
    try {
      if (typeof record.parameters === 'string') {
        taskParams.value = JSON.parse(record.parameters)
      } else {
        taskParams.value = record.parameters
      }
    } catch {}
  }

  // 如果列表已返回code，直接显示
  if (record.code) {
    taskCode.value = record.code
  }

  taskDetailVisible.value = true
}

const handleExecuteTask = async (record) => {
  executingTaskId.value = record.taskId
  try {
    // 执行任务
    await axios.post(`/api/dos/privacy/task/${record.taskId}/execute`)
    message.loading({ content: '任务执行中...', key: 'execute' })

    // 轮询状态变化
    let status = 2 // PENDING
    let maxRetries = 60 // 最多等待60次（约60秒）
    while ([2, 3].includes(status) && maxRetries > 0) {
      await new Promise(resolve => setTimeout(resolve, 1000))
      const resp = await axios.get(`/api/dos/privacy/task/${record.taskId}/status`)
      status = resp.data.data.status
      maxRetries--

      // 更新消息显示当前状态
      if (status === 2) {
        message.loading({ content: '等待调度中...', key: 'execute' })
      } else if (status === 3) {
        message.loading({ content: '运行中...', key: 'execute' })
      }
    }

    // 重新加载任务列表
    await loadTasks()

    // 根据最终状态显示结果
    const updatedTask = tasks.value.find(t => t.taskId === record.taskId)
    if (updatedTask) {
      currentTask.value = updatedTask
      if (status === 4) {
        // 执行成功，获取结果
        const resultResp = await axios.get(`/api/dos/privacy/task/${record.taskId}/result`)
        currentTaskResult.value = resultResp.data.data
        detailActiveTab.value = 'result'
        taskDetailVisible.value = true
        message.success({ content: '任务执行成功', key: 'execute' })
      } else if (status === 5) {
        message.error({ content: '任务执行失败', key: 'execute' })
      } else if (status === 6) {
        message.warning({ content: '任务已取消', key: 'execute' })
      }
    }
  } catch (error) {
    message.error({ content: '执行失败: ' + (error.message || '未知错误'), key: 'execute' })
  } finally {
    executingTaskId.value = null
  }
}

const handleQueryTaskStatus = async (record) => {
  try {
    const response = await axios.get(`/api/dos/privacy/task/${record.taskId}/status`)
    const status = response.data.data.status
    message.info('当前状态: ' + getTaskStatusText(status))
    await loadTasks()
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
    detailActiveTab.value = 'result'
    taskDetailVisible.value = true
  } catch (error) {
    message.error('获取结果失败: ' + (error.message || '未知错误'))
  }
}

// 监听页签变化，加载对应数据
watch(detailActiveTab, async (newTab) => {
  if (!currentTask.value) return

  if (newTab === 'code' && !taskCode.value) {
    // 加载生成代码
    loadingCode.value = true
    try {
      const resp = await axios.get(`/api/dos/privacy/task/${currentTask.value.taskId}/code`)
      taskCode.value = resp.data.data || ''
    } catch (error) {
      console.error('加载代码失败:', error)
      taskCode.value = ''
    } finally {
      loadingCode.value = false
    }
  } else if (newTab === 'logs' && !taskLogs.value && [4, 5].includes(currentTask.value.status)) {
    // 加载执行日志
    loadingLogs.value = true
    try {
      // 调用 detail 接口获取 executionLog 字段
      const resp = await axios.get(`/api/dos/privacy/task/${currentTask.value.taskId}/detail`)
      if (resp.data.data) {
        const detail = resp.data.data
        // 优先使用 executionLog 字段，其次使用 result 字段
        taskLogs.value = detail.executionLog || detail.result || ''
      }
    } catch (error) {
      console.error('加载日志失败:', error)
      taskLogs.value = ''
    } finally {
      loadingLogs.value = false
    }
  }
})

const handleDeleteTask = async (record) => {
  try {
    await axios.delete(`/api/dos/privacy/task/${record.taskId}`)
    message.success('任务已删除')
    loadTasks()
  } catch (error) {
    message.error('删除失败: ' + (error.message || '未知错误'))
  }
}

// 初始化加载任务列表
onMounted(() => {
  loadTasks()
})
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
