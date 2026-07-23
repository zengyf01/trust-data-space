<template>
  <div class="node-manage-container">
    <a-layout-content class="content">
        <a-row :gutter="16">
          <!-- 节点列表 -->
          <a-col :span="24">
            <a-card title="计算节点列表">
              <template #extra>
                <a-space>
                  <a-button type="primary" @click="showRegisterModal">注册节点</a-button>
                  <a-button @click="loadNodes">刷新</a-button>
                </a-space>
              </template>
              <a-table :columns="columns" :data-source="nodes" :loading="loading" :pagination="false" :row-key="record => record.nodeId">
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'name'">
                    <a-input
                      v-if="editingNodeId === record.nodeId"
                      v-model:value="editingName"
                      size="small"
                      style="width: 150px"
                      @pressEnter="handleSaveName(record)"
                      @blur="handleSaveName(record)"
                    />
                    <span v-else>{{ record.nodeName }}</span>
                  </template>
                  <template v-else-if="column.key === 'status'">
                    <a-badge :status="record.status === 'ONLINE' ? 'success' : 'error'" :text="record.status === 'ONLINE' ? '在线' : '离线'" />
                  </template>
                  <template v-else-if="column.key === 'mode'">
                    <a-tag>{{ record.nodeMode }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button size="small" @click="startEditName(record)">编辑</a-button>
                      <a-button size="small" danger @click="handleUnregister(record)">注销</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
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
import { message, Modal } from 'ant-design-vue'
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
  { title: '节点名称', key: 'name' },
  { title: '端点', dataIndex: 'endpoint', key: 'endpoint' },
  { title: '模式', key: 'mode' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 120 }
]

const editingNodeId = ref(null)
const editingName = ref('')

const startEditName = (record) => {
  editingNodeId.value = record.nodeId
  editingName.value = record.nodeName
}

const handleSaveName = async (record) => {
  if (!editingName.value.trim()) {
    message.warning('节点名称不能为空')
    editingNodeId.value = null
    return
  }
  try {
    await axios.put(`/api/dos/privacy/node/${record.nodeId}/name`, {
      nodeName: editingName.value.trim()
    })
    record.nodeName = editingName.value.trim()
    message.success('节点名称修改成功')
  } catch (error) {
    message.error('修改失败: ' + (error.message || '未知错误'))
  }
  editingNodeId.value = null
}

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

const handleUnregister = (record) => {
  Modal.confirm({
    title: '确认注销节点',
    content: `确定要注销节点「${record.nodeName}」吗？注销后该节点将不再参与计算任务。`,
    okText: '确认注销',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      try {
        await axios.delete(`/api/dos/privacy/node/${record.nodeId}`)
        message.success('节点注销成功')
        loadNodes()
      } catch (error) {
        message.error('注销失败: ' + (error.message || '未知错误'))
      }
    }
  })
}

const loadNodes = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/dos/privacy/node/list', {
      params: { page: 1, size: 100 }
    })
    if (res.data.code === 200) {
      nodes.value = res.data.data?.list || []
    } else {
      message.error('加载节点失败: ' + (res.data.msg || '未知错误'))
    }
  } catch (error) {
    message.error('加载节点失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
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