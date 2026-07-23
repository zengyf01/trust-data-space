<template>
  <div class="datasource-container">
    <a-layout-content class="content">
        <a-tabs v-model:activeKey="activeTab">
          <!-- 数据源代理 -->
          <a-tab-pane key="proxy" tab="数据源代理">
            <a-card title="已代理数据源">
              <template #extra>
                <a-space>
                  <a-button @click="loadProxiedSources">刷新</a-button>
                  <a-button type="primary" @click="showAddProxyModal">注册数据源</a-button>
                </a-space>
              </template>
              <a-table :columns="proxyColumns" :data-source="proxiedSources" :loading="loading" :pagination="pagination" @change="handleProxyTableChange">
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'type'">
                    <a-tag :color="getTypeColor(record.sourceType)">{{ record.sourceType }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'status'">
                    <a-switch :checked="record.enabled" @change="(v) => toggleSourceStatus(record, v)" checked-children="启用" un-checked-children="禁用" />
                  </template>
                  <template v-else-if="column.key === 'policy'">
                    <a-tag v-if="record.policyCount > 0" color="blue">{{ record.policyCount }}条策略</a-tag>
                    <a-tag v-else color="red">未配置</a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button size="small" type="primary" @click="handleTestSource(record)">测试</a-button>
                      <a-button size="small" @click="handleConfigPolicy(record)">策略</a-button>
                      <a-button size="small" @click="handleViewAccessLog(record)">日志</a-button>
                      <a-button size="small" danger @click="handleDeleteProxy(record)">删除</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
            </a-card>

            <!-- 计量计费信息 -->
            <a-row :gutter="16" style="margin-top: 16px">
              <a-col :span="6">
                <a-card>
                  <a-statistic title="代理数据源数" :value="stats.proxyCount" />
                </a-card>
              </a-col>
              <a-col :span="6">
                <a-card>
                  <a-statistic title="本月访问次数" :value="stats.monthAccessCount" status="processing" />
                </a-card>
              </a-col>
              <a-col :span="6">
                <a-card>
                  <a-statistic title="本月数据流量" :value="stats.monthTraffic" suffix="MB" />
                </a-card>
              </a-col>
              <a-col :span="6">
                <a-card>
                  <a-statistic title="本月计费金额" :value="stats.monthBilling" prefix="¥" status="success" />
                </a-card>
              </a-col>
            </a-row>
          </a-tab-pane>

          <!-- 访问请求 -->
          <a-tab-pane key="request" tab="访问请求">
            <a-card title="数据访问请求">
              <template #extra>
                <a-button type="primary" @click="showAccessRequestModal">创建访问请求</a-button>
              </template>
              <a-table :columns="requestColumns" :data-source="accessRequests" :loading="loading" :pagination="pagination" @change="handleRequestTableChange">
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-tag :color="getRequestStatusColor(record.status)">{{ getRequestStatusText(record.status) }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'type'">
                    <a-tag>{{ record.dataType }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button size="small" type="primary" @click="handleApproveRequest(record)" v-if="record.status === 'PENDING'">审批</a-button>
                      <a-button size="small" @click="handleViewRequestDetail(record)">详情</a-button>
                      <a-button size="small" danger @click="handleCancelRequest(record)" v-if="record.status === 'PENDING'">取消</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-tab-pane>

          <!-- 交付记录 -->
          <a-tab-pane key="delivery" tab="交付记录">
            <a-card title="数据交付记录">
              <template #extra>
                <a-space>
                  <a-range-picker v-model:value="deliveryDateRange" @change="loadDeliveries" style="width: 250px" />
                  <a-button @click="loadDeliveries">刷新</a-button>
                </a-space>
              </template>
              <a-table :columns="deliveryColumns" :data-source="deliveries" :loading="loading" :pagination="pagination" @change="handleDeliveryTableChange">
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'type'">
                    <a-tag :color="getDeliveryTypeColor(record.deliveryType)">{{ record.deliveryType }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'status'">
                    <a-tag :color="getDeliveryStatusColor(record.status)">{{ getDeliveryStatusText(record.status) }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'volume'">
                    <span>{{ formatBytes(record.dataVolume) }}</span>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button size="small" @click="handleViewDeliveryDetail(record)">详情</a-button>
                      <a-button size="small" type="primary" @click="handleRetryDelivery(record)" v-if="record.status === 'FAILED'">重试</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-tab-pane>

          <!-- 策略配置 -->
          <a-tab-pane key="policy" tab="策略配置">
            <a-card title="数据使用策略">
              <template #extra>
                <a-button type="primary" @click="showPolicyModal">创建策略</a-button>
              </template>
              <a-table :columns="policyColumns" :data-source="policies" :loading="loading" :pagination="pagination" @change="handlePolicyTableChange">
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'type'">
                    <a-tag>{{ record.policyType }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'status'">
                    <a-tag :color="record.enabled ? 'green' : 'red'">{{ record.enabled ? '启用' : '禁用' }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space>
                      <a-button size="small" type="primary" @click="handleEditPolicy(record)">编辑</a-button>
                      <a-button size="small" danger @click="handleDeletePolicy(record)">删除</a-button>
                    </a-space>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-tab-pane>

          <!-- 审计日志 -->
          <a-tab-pane key="audit" tab="审计日志">
            <a-card title="操作审计日志">
              <template #extra>
                <a-space>
                  <a-range-picker v-model:value="auditDateRange" @change="loadAuditLogs" style="width: 250px" />
                  <a-button @click="loadAuditLogs">刷新</a-button>
                </a-space>
              </template>
              <a-table :columns="auditColumns" :data-source="auditLogs" :loading="loading" :pagination="pagination" @change="handleAuditTableChange">
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'operation'">
                    <a-tag :color="getOperationColor(record.operationType)">{{ record.operationType }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'result'">
                    <a-tag :color="record.success ? 'green' : 'red'">{{ record.success ? '成功' : '失败' }}</a-tag>
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-button size="small" @click="handleViewAuditDetail(record)">详情</a-button>
                  </template>
                </template>
              </a-table>
            </a-card>
          </a-tab-pane>
        </a-tabs>
      </a-layout-content>

    <!-- 注册数据源弹窗 -->
    <a-modal v-model:open="proxyModalVisible" title="注册数据源" @ok="handleAddProxy" :confirmLoading="modalLoading" width="600px">
      <a-form :model="proxyForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="数据源名称" required>
          <a-input v-model:value="proxyForm.name" placeholder="请输入数据源名称" />
        </a-form-item>
        <a-form-item label="数据源类型" required>
          <a-select v-model:value="proxyForm.sourceType" placeholder="选择数据源类型">
            <a-select-option value="MYSQL">MySQL</a-select-option>
            <a-select-option value="POSTGRESQL">PostgreSQL</a-select-option>
            <a-select-option value="ORACLE">Oracle</a-select-option>
            <a-select-option value="SFTP">SFTP</a-select-option>
            <a-select-option value="HTTP">HTTP API</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="提供方节点" required>
          <a-select v-model:value="proxyForm.providerNodeId" placeholder="选择提供方连接器">
            <a-select-option v-for="node in providerNodes" :key="node.id" :value="node.id">{{ node.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="原始数据源ID" required>
          <a-input v-model:value="proxyForm.originalSourceId" placeholder="提供方数据源ID" />
        </a-form-item>
        <a-form-item label="代理地址">
          <a-input v-model:value="proxyForm.proxyPath" placeholder="/proxy/datasource/xxx" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="proxyForm.description" :rows="3" placeholder="数据源描述" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 创建访问请求弹窗 -->
    <a-modal v-model:open="requestModalVisible" title="创建访问请求" @ok="handleCreateRequest" :confirmLoading="modalLoading" width="600px">
      <a-form :model="requestForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="数据源" required>
          <a-select v-model:value="requestForm.proxySourceId" placeholder="选择数据源">
            <a-select-option v-for="source in proxiedSources" :key="source.id" :value="source.id">{{ source.name }} ({{ source.sourceType }})</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="使用方节点" required>
          <a-select v-model:value="requestForm.consumerNodeId" placeholder="选择使用方连接器">
            <a-select-option v-for="node in consumerNodes" :key="node.id" :value="node.id">{{ node.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="访问类型" required>
          <a-select v-model:value="requestForm.accessType" placeholder="选择访问类型">
            <a-select-option value="QUERY">数据查询</a-select-option>
            <a-select-option value="DOWNLOAD">数据下载</a-select-option>
            <a-select-option value="STREAM">数据流</a-select-option>
            <a-select-option value="SYNC">数据同步</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="访问条件">
          <a-textarea v-model:value="requestForm.accessCondition" :rows="3" placeholder="查询条件或过滤规则" />
        </a-form-item>
        <a-form-item label="有效期">
          <a-date-picker v-model:value="requestForm.expireTime" style="width: 100%" placeholder="选择过期时间" />
        </a-form-item>
        <a-form-item label="用途说明">
          <a-textarea v-model:value="requestForm.purpose" :rows="2" placeholder="数据用途说明" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 策略配置弹窗 -->
    <a-modal v-model:open="policyModalVisible" :title="editingPolicy ? '编辑策略' : '创建策略'" @ok="handleSavePolicy" :confirmLoading="modalLoading" width="600px">
      <a-form :model="policyForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="策略名称" required>
          <a-input v-model:value="policyForm.name" placeholder="请输入策略名称" />
        </a-form-item>
        <a-form-item label="策略类型" required>
          <a-select v-model:value="policyForm.policyType" placeholder="选择策略类型">
            <a-select-option value="RATE_LIMIT">访问频率限制</a-select-option>
            <a-select-option value="DATA_MASK">数据脱敏</a-select-option>
            <a-select-option value="TIME_RANGE">时间范围限制</a-select-option>
            <a-select-option value="IP_WHITELIST">IP白名单</a-select-option>
            <a-select-option value="VOLUME_LIMIT">数据量限制</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关联数据源">
          <a-select v-model:value="policyForm.sourceIds" mode="multiple" placeholder="选择关联数据源（留空则全局生效）">
            <a-select-option v-for="source in proxiedSources" :key="source.id" :value="source.id">{{ source.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="规则配置">
          <a-textarea v-model:value="policyForm.rules" :rows="4" placeholder='{"maxRequests": 100, "period": "day"}' />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="policyForm.description" :rows="2" placeholder="策略描述" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailModalVisible" :title="detailTitle" :footer="null" width="700px">
      <a-descriptions bordered :column="2" v-if="currentDetail">
        <template v-for="(value, key) in currentDetail" :key="key">
          <a-descriptions-item :label="getDetailLabel(key)" :span="isLongValue(value) ? 2 : 1">
            <template v-if="key === 'resultMessage' || key === 'logs'">
              <pre style="white-space: pre-wrap; word-break: break-all;">{{ value }}</pre>
            </template>
            <template v-else>{{ value }}</template>
          </a-descriptions-item>
        </template>
      </a-descriptions>
      <a-spin v-else />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'

const activeTab = ref('proxy')
const loading = ref(false)
const modalLoading = ref(false)

// ========== 统计数据 ==========
const stats = ref({
  proxyCount: 0,
  monthAccessCount: 0,
  monthTraffic: 0,
  monthBilling: 0
})

// ========== 代理数据源 ==========
const proxiedSources = ref([])
const providerNodes = ref([])
const consumerNodes = ref([])
const proxyModalVisible = ref(false)
const proxyForm = reactive({
  name: '',
  sourceType: 'MYSQL',
  providerNodeId: '',
  originalSourceId: '',
  proxyPath: '',
  description: ''
})

const proxyColumns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '类型', key: 'type' },
  { title: '提供方', dataIndex: 'providerNode', key: 'providerNode' },
  { title: '代理路径', dataIndex: 'proxyPath', key: 'proxyPath', ellipsis: true },
  { title: '状态', key: 'status' },
  { title: '策略', key: 'policy' },
  { title: '操作', key: 'action', width: 280 }
]

const getTypeColor = (type) => {
  const colors = { 'MYSQL': 'blue', 'POSTGRESQL': 'green', 'ORACLE': 'orange', 'SFTP': 'purple', 'HTTP': 'cyan' }
  return colors[type] || 'default'
}

const toggleSourceStatus = async (record, enabled) => {
  try {
    await axios.put(`/api/dos/datasource/proxy/${record.id}/status`, null, { params: { enabled } })
    record.enabled = enabled
    message.success(enabled ? '已启用' : '已禁用')
  } catch (e) {
    message.error('操作失败')
  }
}

const showAddProxyModal = () => {
  Object.assign(proxyForm, { name: '', sourceType: 'MYSQL', providerNodeId: '', originalSourceId: '', proxyPath: '', description: '' })
  proxyModalVisible.value = true
}

const handleAddProxy = async () => {
  if (!proxyForm.name || !proxyForm.providerNodeId || !proxyForm.originalSourceId) {
    message.warning('请填写必填项')
    return
  }
  modalLoading.value = true
  try {
    const response = await axios.post('/api/dos/datasource/proxy', proxyForm)
    if (response.data.code === 200) {
      message.success('数据源注册成功')
      proxyModalVisible.value = false
      loadProxiedSources()
    } else {
      message.error(response.data.msg || '注册失败')
    }
  } catch (e) {
    message.error('注册失败')
  } finally {
    modalLoading.value = false
  }
}

const handleTestSource = async (record) => {
  try {
    const response = await axios.get(`/api/dos/datasource/proxy/${record.id}/test`)
    if (response.data.code === 200) {
      message.success('连接测试成功')
    } else {
      message.error('连接测试失败: ' + (response.data.msg || ''))
    }
  } catch (e) {
    message.error('连接测试失败')
  }
}

const handleConfigPolicy = (record) => {
  activeTab.value = 'policy'
  // 可以添加筛选逻辑
}

const handleViewAccessLog = (record) => {
  activeTab.value = 'audit'
}

const handleDeleteProxy = async (record) => {
  try {
    await axios.delete(`/api/dos/datasource/proxy/${record.id}`)
    message.success('删除成功')
    loadProxiedSources()
  } catch (e) {
    message.error('删除失败')
  }
}

const loadProxiedSources = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/dos/datasource/proxy/list')
    if (response.data.code === 200) {
      proxiedSources.value = response.data.data || []
    }
  } catch (e) {
    // 使用模拟数据
    proxiedSources.value = [
      { id: 'ds-001', name: '医院患者数据', sourceType: 'MYSQL', providerNode: '医院连接器A', proxyPath: '/proxy/hospital/patients', enabled: true, policyCount: 2 },
      { id: 'ds-002', name: '银行交易数据', sourceType: 'MYSQL', providerNode: '银行连接器B', proxyPath: '/proxy/bank/transactions', enabled: true, policyCount: 1 },
      { id: 'ds-003', name: '政务文件服务', sourceType: 'SFTP', providerNode: '政务连接器C', proxyPath: '/proxy/gov/files', enabled: false, policyCount: 0 },
      { id: 'ds-004', name: '第三方API数据', sourceType: 'HTTP', providerNode: '第三方连接器D', proxyPath: '/proxy/thirdparty/api', enabled: true, policyCount: 3 }
    ]
  } finally {
    loading.value = false
  }
}

// ========== 访问请求 ==========
const accessRequests = ref([])
const requestModalVisible = ref(false)
const requestForm = reactive({
  proxySourceId: '',
  consumerNodeId: '',
  accessType: 'QUERY',
  accessCondition: '',
  expireTime: null,
  purpose: ''
})

const requestColumns = [
  { title: '请求ID', dataIndex: 'requestId', key: 'requestId', ellipsis: true },
  { title: '数据源', dataIndex: 'sourceName', key: 'sourceName' },
  { title: '使用方', dataIndex: 'consumerNode', key: 'consumerNode' },
  { title: '类型', key: 'type' },
  { title: '状态', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 200 }
]

const getRequestStatusColor = (status) => {
  const colors = { 'PENDING': 'orange', 'APPROVED': 'green', 'REJECTED': 'red', 'EXPIRED': 'gray', 'CANCELLED': 'default' }
  return colors[status] || 'default'
}

const getRequestStatusText = (status) => {
  const texts = { 'PENDING': '待审批', 'APPROVED': '已批准', 'REJECTED': '已拒绝', 'EXPIRED': '已过期', 'CANCELLED': '已取消' }
  return texts[status] || status
}

const showAccessRequestModal = () => {
  Object.assign(requestForm, { proxySourceId: '', consumerNodeId: '', accessType: 'QUERY', accessCondition: '', expireTime: null, purpose: '' })
  requestModalVisible.value = true
}

const handleCreateRequest = async () => {
  if (!requestForm.proxySourceId || !requestForm.consumerNodeId) {
    message.warning('请填写必填项')
    return
  }
  modalLoading.value = true
  try {
    const response = await axios.post('/api/dos/access/request', {
      ...requestForm,
      expireTime: requestForm.expireTime ? requestForm.expireTime.format('YYYY-MM-DD HH:mm:ss') : null
    })
    if (response.data.code === 200) {
      message.success('访问请求已提交')
      requestModalVisible.value = false
      loadAccessRequests()
    } else {
      message.error(response.data.msg || '创建失败')
    }
  } catch (e) {
    message.error('创建失败')
  } finally {
    modalLoading.value = false
  }
}

const handleApproveRequest = async (record) => {
  try {
    const response = await axios.post(`/api/dos/access/request/${record.requestId}/approve`)
    if (response.data.code === 200) {
      message.success('已批准访问请求')
      loadAccessRequests()
    } else {
      message.error('审批失败')
    }
  } catch (e) {
    message.error('审批失败')
  }
}

const handleCancelRequest = async (record) => {
  try {
    await axios.post(`/api/dos/access/request/${record.requestId}/cancel`)
    message.success('已取消')
    loadAccessRequests()
  } catch (e) {
    message.error('取消失败')
  }
}

const handleViewRequestDetail = (record) => {
  currentDetail.value = record
  detailTitle.value = '访问请求详情'
  detailModalVisible.value = true
}

const loadAccessRequests = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/dos/access/request/list')
    if (response.data.code === 200) {
      accessRequests.value = response.data.data || []
    }
  } catch (e) {
    accessRequests.value = [
      { requestId: 'AR-20240101', sourceName: '医院患者数据', consumerNode: '分析平台A', dataType: 'QUERY', status: 'APPROVED', createTime: '2024-06-15 10:00:00' },
      { requestId: 'AR-20240102', sourceName: '银行交易数据', consumerNode: '分析平台B', dataType: 'DOWNLOAD', status: 'PENDING', createTime: '2024-06-15 11:00:00' },
      { requestId: 'AR-20240103', sourceName: '政务文件服务', consumerNode: '分析平台C', dataType: 'SYNC', status: 'REJECTED', createTime: '2024-06-15 12:00:00' }
    ]
  } finally {
    loading.value = false
  }
}

// ========== 交付记录 ==========
const deliveries = ref([])
const deliveryDateRange = ref(null)

const deliveryColumns = [
  { title: '交付ID', dataIndex: 'deliveryId', key: 'deliveryId', ellipsis: true },
  { title: '数据源', dataIndex: 'sourceName', key: 'sourceName' },
  { title: '使用方', dataIndex: 'consumerNode', key: 'consumerNode' },
  { title: '类型', key: 'type' },
  { title: '状态', key: 'status' },
  { title: '数据量', key: 'volume' },
  { title: '耗时', dataIndex: 'duration', key: 'duration' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 120 }
]

const getDeliveryTypeColor = (type) => {
  const colors = { 'SFTP': 'blue', 'HTTP': 'green', 'DATABASE': 'purple', 'FILE': 'orange' }
  return colors[type] || 'default'
}

const getDeliveryTypeText = (type) => {
  const texts = { 'SFTP': 'SFTP传输', 'HTTP': 'HTTP推送', 'DATABASE': '数据库同步', 'FILE': '文件' }
  return texts[type] || type
}

const getDeliveryStatusColor = (status) => {
  const colors = { 'SUCCESS': 'green', 'FAILED': 'red', 'PROCESSING': 'blue', 'PENDING': 'orange' }
  return colors[status] || 'default'
}

const getDeliveryStatusText = (status) => {
  const texts = { 'SUCCESS': '成功', 'FAILED': '失败', 'PROCESSING': '处理中', 'PENDING': '待处理' }
  return texts[status] || status
}

const formatBytes = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const handleViewDeliveryDetail = (record) => {
  currentDetail.value = record
  detailTitle.value = '交付详情'
  detailModalVisible.value = true
}

const handleRetryDelivery = async (record) => {
  try {
    await axios.post(`/api/dos/delivery/${record.deliveryId}/retry`)
    message.success('已重新提交')
    loadDeliveries()
  } catch (e) {
    message.error('重试失败')
  }
}

const loadDeliveries = async () => {
  loading.value = true
  try {
    const params = {}
    if (deliveryDateRange.value) {
      params.startDate = deliveryDateRange.value[0].format('YYYY-MM-DD')
      params.endDate = deliveryDateRange.value[1].format('YYYY-MM-DD')
    }
    const response = await axios.get('/api/dos/delivery/list', { params })
    if (response.data.code === 200) {
      deliveries.value = response.data.data || []
    }
  } catch (e) {
    deliveries.value = [
      { deliveryId: 'DL-20240101', sourceName: '医院患者数据', consumerNode: '分析平台A', deliveryType: 'SFTP', status: 'SUCCESS', dataVolume: 1048576, duration: '5s', createTime: '2024-06-15 10:00:00' },
      { deliveryId: 'DL-20240102', sourceName: '银行交易数据', consumerNode: '分析平台B', deliveryType: 'HTTP', status: 'PROCESSING', dataVolume: 0, duration: '-', createTime: '2024-06-15 11:00:00' },
      { deliveryId: 'DL-20240103', sourceName: '政务文件服务', consumerNode: '分析平台C', deliveryType: 'SFTP', status: 'FAILED', dataVolume: 0, duration: '-', createTime: '2024-06-15 12:00:00' }
    ]
  } finally {
    loading.value = false
  }
}

// ========== 策略配置 ==========
const policies = ref([])
const policyModalVisible = ref(false)
const editingPolicy = ref(null)
const policyForm = reactive({
  name: '',
  policyType: 'RATE_LIMIT',
  sourceIds: [],
  rules: '',
  description: ''
})

const policyColumns = [
  { title: '策略名称', dataIndex: 'name', key: 'name' },
  { title: '类型', key: 'type' },
  { title: '关联数据源', dataIndex: 'sourceNames', key: 'sourceNames' },
  { title: '状态', key: 'status' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 150 }
]

const showPolicyModal = () => {
  editingPolicy.value = null
  Object.assign(policyForm, { name: '', policyType: 'RATE_LIMIT', sourceIds: [], rules: '', description: '' })
  policyModalVisible.value = true
}

const handleEditPolicy = (record) => {
  editingPolicy.value = record
  Object.assign(policyForm, {
    name: record.name,
    policyType: record.policyType,
    sourceIds: record.sourceIds || [],
    rules: typeof record.rules === 'string' ? record.rules : JSON.stringify(record.rules),
    description: record.description || ''
  })
  policyModalVisible.value = true
}

const handleSavePolicy = async () => {
  if (!policyForm.name || !policyForm.policyType) {
    message.warning('请填写必填项')
    return
  }
  modalLoading.value = true
  try {
    const url = editingPolicy.value ? `/api/dos/policy/${editingPolicy.value.id}` : '/api/dos/policy'
    const method = editingPolicy.value ? 'put' : 'post'
    const response = await axios[method](url, policyForm)
    if (response.data.code === 200) {
      message.success('策略保存成功')
      policyModalVisible.value = false
      loadPolicies()
    } else {
      message.error(response.data.msg || '保存失败')
    }
  } catch (e) {
    message.error('保存失败')
  } finally {
    modalLoading.value = false
  }
}

const handleDeletePolicy = async (record) => {
  try {
    await axios.delete(`/api/dos/policy/${record.id}`)
    message.success('删除成功')
    loadPolicies()
  } catch (e) {
    message.error('删除失败')
  }
}

const loadPolicies = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/dos/policy/list')
    if (response.data.code === 200) {
      policies.value = response.data.data || []
    }
  } catch (e) {
    policies.value = [
      { id: 'pol-001', name: '医院数据访问限制', policyType: 'RATE_LIMIT', sourceNames: '医院患者数据', sourceIds: ['ds-001'], enabled: true, rules: '{"maxRequests": 100, "period": "day"}', createTime: '2024-06-01 10:00:00' },
      { id: 'pol-002', name: '银行数据脱敏', policyType: 'DATA_MASK', sourceNames: '银行交易数据', sourceIds: ['ds-002'], enabled: true, rules: '{"maskFields": ["cardNo", "phone"]}', createTime: '2024-06-02 10:00:00' },
      { id: 'pol-003', name: '工作时间限制', policyType: 'TIME_RANGE', sourceNames: '全部', sourceIds: [], enabled: false, rules: '{"startHour": 9, "endHour": 18}', createTime: '2024-06-03 10:00:00' }
    ]
  } finally {
    loading.value = false
  }
}

// ========== 审计日志 ==========
const auditLogs = ref([])
const auditDateRange = ref(null)

const auditColumns = [
  { title: '时间', dataIndex: 'operateTime', key: 'operateTime' },
  { title: '操作类型', key: 'operation' },
  { title: '操作人', dataIndex: 'operator', key: 'operator' },
  { title: '数据源', dataIndex: 'sourceName', key: 'sourceName' },
  { title: '使用方', dataIndex: 'consumerNode', key: 'consumerNode' },
  { title: '结果', key: 'result' },
  { title: '操作', key: 'action', width: 80 }
]

const getOperationColor = (type) => {
  const colors = { 'ACCESS': 'blue', 'PROXY': 'green', 'DELIVERY': 'purple', 'POLICY': 'orange', 'BILLING': 'cyan' }
  return colors[type] || 'default'
}

const handleViewAuditDetail = (record) => {
  currentDetail.value = record
  detailTitle.value = '审计详情'
  detailModalVisible.value = true
}

const loadAuditLogs = async () => {
  loading.value = true
  try {
    const params = {}
    if (auditDateRange.value) {
      params.startDate = auditDateRange.value[0].format('YYYY-MM-DD')
      params.endDate = auditDateRange.value[1].format('YYYY-MM-DD')
    }
    const response = await axios.get('/api/dos/audit/log/list', { params })
    if (response.data.code === 200) {
      auditLogs.value = response.data.data || []
    }
  } catch (e) {
    auditLogs.value = [
      { id: 'log-001', operateTime: '2024-06-15 10:00:00', operationType: 'ACCESS', operator: 'system', sourceName: '医院患者数据', consumerNode: '分析平台A', success: true, resultMessage: '访问成功，数据量: 1024条' },
      { id: 'log-002', operateTime: '2024-06-15 10:05:00', operationType: 'DELIVERY', operator: 'system', sourceName: '银行交易数据', consumerNode: '分析平台B', success: true, resultMessage: 'SFTP传输成功' },
      { id: 'log-003', operateTime: '2024-06-15 10:10:00', operationType: 'POLICY', operator: 'admin', sourceName: '政务文件服务', consumerNode: '-', success: false, resultMessage: '策略拦截: 访问频率超限' },
      { id: 'log-004', operateTime: '2024-06-15 10:15:00', operationType: 'BILLING', operator: 'system', sourceName: '医院患者数据', consumerNode: '分析平台A', success: true, resultMessage: '计费: ¥15.00' }
    ]
  } finally {
    loading.value = false
  }
}

// ========== 详情弹窗 ==========
const detailModalVisible = ref(false)
const detailTitle = ref('')
const currentDetail = ref(null)

const getDetailLabel = (key) => {
  const labels = {
    requestId: '请求ID',
    deliveryId: '交付ID',
    sourceName: '数据源',
    consumerNode: '使用方',
    dataType: '数据类型',
    status: '状态',
    createTime: '创建时间',
    resultMessage: '结果信息',
    logs: '日志详情',
    operateTime: '操作时间',
    operationType: '操作类型',
    operator: '操作人',
    success: '是否成功'
  }
  return labels[key] || key
}

const isLongValue = (value) => {
  return typeof value === 'string' && value.length > 100
}

// ========== 分页处理 ==========
const pagination = ref({ current: 1, pageSize: 10, total: 0 })

const handleProxyTableChange = (pag) => {
  pagination.value.current = pag.current
  loadProxiedSources()
}

const handleRequestTableChange = (pag) => {
  pagination.value.current = pag.current
  loadAccessRequests()
}

const handleDeliveryTableChange = (pag) => {
  pagination.value.current = pag.current
  loadDeliveries()
}

const handlePolicyTableChange = (pag) => {
  pagination.value.current = pag.current
  loadPolicies()
}

const handleAuditTableChange = (pag) => {
  pagination.value.current = pag.current
  loadAuditLogs()
}

// ========== 初始化 ==========
onMounted(() => {
  loadProxiedSources()
  // 加载统计数据
  stats.value = { proxyCount: 4, monthAccessCount: 1256, monthTraffic: 1024, monthBilling: 3500 }
  // 加载节点列表
  providerNodes.value = [
    { id: 'node-001', name: '医院连接器A' },
    { id: 'node-002', name: '银行连接器B' },
    { id: 'node-003', name: '政务连接器C' }
  ]
  consumerNodes.value = [
    { id: 'node-101', name: '分析平台A' },
    { id: 'node-102', name: '分析平台B' },
    { id: 'node-103', name: '分析平台C' }
  ]
})
</script>

<style scoped>
.datasource-container { height: 100vh; }
.content { padding: 24px; background: #f0f2f5; overflow-y: auto; }
</style>
