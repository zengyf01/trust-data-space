<template>
  <div class="node-manage-container">
    <a-layout-content class="content">
        <a-row :gutter="16">
          <!-- 节点列表 -->
          <a-col :span="16">
            <a-card title="计算节点列表">
              <template #extra>
                <a-button type="primary" @click="showRegisterModal">注册节点</a-button>
              </template>
              <a-table :columns="columns" :data-source="nodes" :loading="loading" :pagination="false">
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-badge :status="record.status === 'ONLINE' ? 'success' : 'error'" :text="record.status === 'ONLINE' ? '在线' : '离线'" />
                  </template>
                  <template v-else-if="column.key === 'mode'">
                    <a-tag>{{ record.nodeMode }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button size="small" @click="handleHeartbeat(record)">发送心跳</a-button>
                      <a-button size="small" danger @click="handleUnregister(record)">注销</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-col>

          <!-- 统计信息 -->
          <a-col :span="8">
            <a-card title="节点统计" class="stat-card">
              <a-statistic title="总节点数" :value="nodes.length" />
              <a-divider />
              <a-statistic title="在线节点" :value="onlineCount" :value-style="{ color: '#52c41a' }" />
              <a-divider />
              <a-statistic title="离线节点" :value="offlineCount" :value-style="{ color: '#ff4d4f' }" />
              <a-divider />
              <a-statistic title="RAY模式" :value="rayCount" />
              <a-divider />
              <a-statistic title="KUSCIA模式" :value="kusciaCount" />
            </a-card>
          </a-col>
        </a-row>

        <!-- 注册节点弹窗 -->
        <a-modal v-model:open="registerVisible" title="注册计算节点" @ok="handleRegister" :confirm-loading="registerLoading">
          <a-form :model="registerForm" layout="vertical">
            <a-form-item label="节点ID" required>
              <a-input v-model:value="registerForm.nodeId" placeholder="请输入节点ID" />
            </a-form-item>
            <a-form-item label="节点名称" required>
              <a-input v-model:value="registerForm.nodeName" placeholder="请输入节点名称" />
            </a-form-item>
            <a-form-item label="节点端点">
              <a-input v-model:value="registerForm.endpoint" placeholder="如: http://node1:8080" />
            </a-form-item>
            <a-form-item label="节点模式">
              <a-select v-model:value="registerForm.nodeMode">
                <a-select-option value="RAY">RAY</a-select-option>
                <a-select-option value="KUSCIA">KUSCIA</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </a-modal>
      </a-layout-content>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'

const loading = ref(false)
const nodes = ref([])
const registerVisible = ref(false)
const registerLoading = ref(false)

const registerForm = ref({
  nodeId: '',
  nodeName: '',
  endpoint: '',
  nodeMode: 'RAY'
})

const columns = [
  { title: '节点ID', dataIndex: 'nodeId', key: 'nodeId' },
  { title: '节点名称', dataIndex: 'nodeName', key: 'nodeName' },
  { title: '端点', dataIndex: 'endpoint', key: 'endpoint' },
  { title: '模式', key: 'mode' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 180 }
]

const onlineCount = computed(() => nodes.value.filter(n => n.status === 'ONLINE').length)
const offlineCount = computed(() => nodes.value.filter(n => n.status === 'OFFLINE').length)
const rayCount = computed(() => nodes.value.filter(n => n.nodeMode === 'RAY').length)
const kusciaCount = computed(() => nodes.value.filter(n => n.nodeMode === 'KUSCIA').length)

const showRegisterModal = () => {
  registerForm.value = { nodeId: '', nodeName: '', endpoint: '', nodeMode: 'RAY' }
  registerVisible.value = true
}

const handleRegister = async () => {
  if (!registerForm.value.nodeId || !registerForm.value.nodeName) {
    message.warning('请填写完整信息')
    return
  }
  registerLoading.value = true
  try {
    await axios.post('/api/dos/privacy/node/register', {
      nodeId: registerForm.value.nodeId,
      nodeName: registerForm.value.nodeName,
      endpoint: registerForm.value.endpoint,
      nodeMode: registerForm.value.nodeMode
    })
    message.success('节点注册成功')
    registerVisible.value = false
    loadNodes()
  } catch (error) {
    message.error('注册失败: ' + (error.message || '未知错误'))
  } finally {
    registerLoading.value = false
  }
}

const handleHeartbeat = async (record) => {
  try {
    await axios.post(`/api/dos/privacy/node/${record.nodeId}/heartbeat`)
    message.success('心跳发送成功')
  } catch (error) {
    message.error('心跳发送失败: ' + (error.message || '未知错误'))
  }
}

const handleUnregister = (record) => {
  message.warning('注销节点: ' + record.nodeId)
}

const loadNodes = () => {
  // 模拟数据
  nodes.value = [
    { nodeId: 'node-001', nodeName: 'RAY计算节点1', endpoint: 'http://node1:8080', nodeMode: 'RAY', status: 'ONLINE' },
    { nodeId: 'node-002', nodeName: 'RAY计算节点2', endpoint: 'http://node2:8080', nodeMode: 'RAY', status: 'ONLINE' },
    { nodeId: 'node-003', nodeName: 'KUSCIA节点1', endpoint: 'http://node3:8080', nodeMode: 'KUSCIA', status: 'ONLINE' },
    { nodeId: 'node-004', nodeName: 'RAY计算节点3', endpoint: 'http://node4:8080', nodeMode: 'RAY', status: 'OFFLINE' }
  ]
}

onMounted(() => {
  loadNodes()
})
</script>

<style scoped>
.node-manage-container {
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
.stat-card {
  text-align: center;
}
</style>