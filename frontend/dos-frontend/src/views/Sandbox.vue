<template>
  <div class="sandbox-container">
    <a-layout-content class="content">
        <a-card title="沙盒列表">
          <template #extra>
            <a-button type="primary" @click="showCreateModal">创建沙盒</a-button>
          </template>
          <a-table :columns="columns" :data-source="sandboxes" :loading="loading" :pagination="false">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="getStatusColor(record.status)">
                  {{ record.status }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'jupyterUrl'">
                <a :href="record.jupyterUrl" target="_blank" v-if="record.jupyterUrl">
                  打开JupyterLab
                </a>
                <span v-else>-</span>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button size="small" @click="handleDetail(record)">详情</a-button>
                  <a-button size="small" type="primary" @click="handleLogs(record)">日志</a-button>
                  <a-button size="small" danger @click="handleDestroy(record)">销毁</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>

        <!-- 创建沙盒弹窗 -->
        <a-modal
          v-model:open="createModalVisible"
          title="创建安全沙盒"
          @ok="handleCreate"
          @cancel="createModalVisible = false"
          :confirmLoading="createLoading"
          width="600px"
        >
          <a-form :model="createForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
            <a-form-item label="关联工单" required>
              <a-input v-model:value="createForm.workOrderId" placeholder="请输入工单ID" />
            </a-form-item>
            <a-form-item label="容器镜像" required>
              <a-input v-model:value="createForm.image" placeholder="请输入Docker镜像地址" />
            </a-form-item>
            <a-form-item label="CPU核心数">
              <a-input-number v-model:value="createForm.cpu" :min="1" :max="16" style="width: 100%" />
            </a-form-item>
            <a-form-item label="内存(MB)">
              <a-input-number v-model:value="createForm.memoryMB" :min="1024" :max="32768" :step="1024" style="width: 100%" />
            </a-form-item>
            <a-form-item label="工作目录">
              <a-input v-model:value="createForm.workDir" placeholder="/workspace" />
            </a-form-item>
            <a-form-item label="镜像仓库密钥">
              <a-input v-model:value="createForm.sourceUrl" placeholder="镜像仓库密钥名称（可选）" />
            </a-form-item>
          </a-form>
        </a-modal>

        <!-- 详情弹窗 -->
        <a-modal
          v-model:open="detailModalVisible"
          title="沙盒详情"
          :footer="null"
          width="700px"
        >
          <a-descriptions bordered :column="2" v-if="currentSandbox">
            <a-descriptions-item label="Pod名称">{{ currentSandbox.name }}</a-descriptions-item>
            <a-descriptions-item label="命名空间">{{ currentSandbox.namespace }}</a-descriptions-item>
            <a-descriptions-item label="状态">{{ currentSandbox.status }}</a-descriptions-item>
            <a-descriptions-item label="Pod IP">{{ currentSandbox.podIP || '-' }}</a-descriptions-item>
            <a-descriptions-item label="宿主机IP">{{ currentSandbox.hostIP || '-' }}</a-descriptions-item>
            <a-descriptions-item label="JupyterLab">
              <a :href="currentSandbox.jupyterUrl" target="_blank" v-if="currentSandbox.jupyterUrl">
                打开链接
              </a>
              <span v-else>-</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-modal>

        <!-- 日志弹窗 -->
        <a-modal
          v-model:open="logsModalVisible"
          title="沙盒日志"
          :footer="null"
          width="800px"
        >
          <pre class="log-content" v-if="logs">{{ logs }}</pre>
          <a-spin v-else />
        </a-modal>
      </a-layout-content>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'

const loading = ref(false)
const sandboxes = ref([])
const selectedKeys = ref([])

const createModalVisible = ref(false)
const createLoading = ref(false)
const createForm = ref({
  workOrderId: '',
  image: 'jupyter/scipy-notebook:latest',
  cpu: 2,
  memoryMB: 4096,
  workDir: '/workspace',
  sourceUrl: ''
})

const detailModalVisible = ref(false)
const currentSandbox = ref(null)

const logsModalVisible = ref(false)
const logs = ref('')

const columns = [
  { title: 'Pod名称', dataIndex: 'name', key: 'name' },
  { title: '命名空间', dataIndex: 'namespace', key: 'namespace' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: 'Pod IP', dataIndex: 'podIP', key: 'podIP' },
  { title: 'JupyterLab', key: 'jupyterUrl' },
  { title: '操作', key: 'action', width: 200 }
]

const getStatusColor = (status) => {
  const colors = {
    'Running': 'green',
    'Pending': 'blue',
    'Succeeded': 'green',
    'Failed': 'red',
    'Unknown': 'gray'
  }
  return colors[status] || 'default'
}

const showCreateModal = () => {
  createForm.value = {
    workOrderId: '',
    image: 'jupyter/scipy-notebook:latest',
    cpu: 2,
    memoryMB: 4096,
    workDir: '/workspace',
    sourceUrl: ''
  }
  createModalVisible.value = true
}

const handleCreate = async () => {
  if (!createForm.value.workOrderId) {
    message.error('请输入关联工单ID')
    return
  }
  if (!createForm.value.image) {
    message.error('请输入容器镜像')
    return
  }

  try {
    createLoading.value = true
    const response = await axios.post('/api/dos/sandbox/create', {
      workOrderId: createForm.value.workOrderId,
      image: createForm.value.image,
      cpu: createForm.value.cpu,
      memoryMB: createForm.value.memoryMB,
      workDir: createForm.value.workDir,
      sourceUrl: createForm.value.sourceUrl
    })

    if (response.data.code === 200) {
      message.success('沙盒创建成功')
      createModalVisible.value = false
      loadData()
    } else {
      message.error(response.data.msg || '创建失败')
    }
  } catch (error) {
    console.error('创建沙盒失败:', error)
    message.error(error.response?.data?.msg || '创建失败')
  } finally {
    createLoading.value = false
  }
}

const handleDetail = async (record) => {
  try {
    const response = await axios.get('/api/dos/sandbox/detail', {
      params: { podName: record.name }
    })
    if (response.data.code === 200) {
      currentSandbox.value = response.data.data
      detailModalVisible.value = true
    } else {
      message.error(response.data.msg || '获取详情失败')
    }
  } catch (error) {
    message.error('获取详情失败')
  }
}

const handleLogs = async (record) => {
  try {
    const response = await axios.get('/api/dos/sandbox/logs', {
      params: { podName: record.name, tail: true }
    })
    if (response.data.code === 200) {
      logs.value = response.data.data.logs || '无日志'
      logsModalVisible.value = true
    } else {
      message.error(response.data.msg || '获取日志失败')
    }
  } catch (error) {
    message.error('获取日志失败')
  }
}

const handleDestroy = async (record) => {
  try {
    const response = await axios.post('/api/dos/sandbox/destroy', null, {
      params: { podName: record.name }
    })
    if (response.data.code === 200) {
      message.success('沙盒已销毁')
      loadData()
    } else {
      message.error(response.data.msg || '销毁失败')
    }
  } catch (error) {
    message.error('销毁失败')
  }
}

const loadData = async () => {
  try {
    loading.value = true
    // 使用固定用户ID，后续可从登录状态获取
    const response = await axios.get('/api/dos/sandbox/list', {
      params: { userId: 'default' }
    })

    if (response.data.code === 200) {
      sandboxes.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载沙盒列表失败:', error)
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
.sandbox-container {
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
.log-content {
  max-height: 400px;
  overflow-y: auto;
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
