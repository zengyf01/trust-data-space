<template>
  <div class="node-container">
    <a-layout-content class="content">
        <a-card title="节点列表">
          <template #extra>
            <a-button type="primary" @click="showRegisterModal">注册节点</a-button>
          </template>
          <a-table :columns="columns" :data-source="nodes" :loading="loading" :pagination="pagination" @change="handleTableChange">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.fStatus === 1 ? 'green' : 'red'">
                  {{ record.fStatus === 1 ? '在线' : '离线' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button size="small" type="primary" @click="handleHeartbeat(record)">心跳</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-layout-content>

    <a-modal v-model:open="registerModalVisible" title="注册节点" @ok="handleRegister">
      <a-form :model="nodeForm" layout="vertical">
        <a-form-item label="节点ID" required>
          <a-input v-model:value="nodeForm.nodeId" placeholder="请输入节点ID" />
        </a-form-item>
        <a-form-item label="节点名称" required>
          <a-input v-model:value="nodeForm.nodeName" placeholder="请输入节点名称" />
        </a-form-item>
        <a-form-item label="节点模式">
          <a-select v-model:value="nodeForm.nodeMode">
            <a-select-option value="RAY">RAY</a-select-option>
            <a-select-option value="KUSCIA">KUSCIA</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="端点">
          <a-input v-model:value="nodeForm.endpoint" placeholder="请输入端点地址" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { nodeAPI } from '../api'

const router = useRouter()
const selectedKeys = ref(['nodes'])
const menuItems = [
  { key: 'dashboard', label: '首页' },
  { key: 'tasks', label: '任务管理' },
  { key: 'nodes', label: '节点管理' },
  { key: 'datasources', label: '数据源' }
]

const loading = ref(false)
const nodes = ref([])
const registerModalVisible = ref(false)
const pagination = ref({ current: 1, pageSize: 10, total: 0 })
const nodeForm = ref({ nodeId: '', nodeName: '', nodeMode: 'RAY', endpoint: '' })

const columns = [
  { title: '节点ID', dataIndex: 'fNodeId', key: 'fNodeId' },
  { title: '节点名称', dataIndex: 'fNodeName', key: 'fNodeName' },
  { title: '模式', dataIndex: 'fNodeMode', key: 'fNodeMode' },
  { title: '状态', key: 'status' },
  { title: '最后心跳', dataIndex: 'fLastHeartbeat', key: 'fLastHeartbeat' },
  { title: '操作', key: 'action' }
]

const handleMenuClick = ({ key }) => router.push('/' + key)
const handleTableChange = (pag) => { pagination.value.current = pag.current; loadNodes() }

const loadNodes = async () => {
  loading.value = true
  try {
    const res = await nodeAPI.list({ page: pagination.value.current, size: pagination.value.pageSize })
    nodes.value = res.data.data?.list || []
    pagination.value.total = res.data.data?.pagination?.total || 0
  } catch (e) {
    message.error('加载节点失败')
  } finally {
    loading.value = false
  }
}

const showRegisterModal = () => { registerModalVisible.value = true }
const handleRegister = async () => {
  try {
    await nodeAPI.register(nodeForm.value)
    message.success('节点注册成功')
    registerModalVisible.value = false
    loadNodes()
  } catch (e) {
    message.error('注册失败')
  }
}

const handleHeartbeat = async (record) => {
  try {
    await nodeAPI.heartbeat(record.fNodeId)
    message.success('心跳发送成功')
    loadNodes()
  } catch (e) {
    message.error('心跳失败')
  }
}


onMounted(loadNodes)
</script>

<style scoped>
.node-container { height: 100vh; }
.header { background: #001529; padding: 0 24px; display: flex; align-items: center; gap: 24px; }
.header h1 { color: white; margin: 0; font-size: 18px; }
.content { padding: 24px; background: #f0f2f5; overflow-y: auto; }
</style>