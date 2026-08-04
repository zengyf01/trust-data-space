<template>
  <div class="task-manage-container">
    <a-layout-content class="content">
      <a-card title="任务管理 [v20260803-merge]">
        <template #extra>
          <a-space>
            <a-input-search v-model:value="searchKey" placeholder="搜索任务ID/名称" style="width: 240px" @search="loadTasks(1)" allow-clear />
            <a-select v-model:value="filterType" placeholder="任务类型" style="width: 140px" allow-clear @change="loadTasks(1)">
              <a-select-option :value="1">PSI 求交</a-select-option>
              <a-select-option :value="2">MPC 多方计算</a-select-option>
              <a-select-option :value="3">横向联邦</a-select-option>
              <a-select-option :value="4">PIR 隐匿查询</a-select-option>
              <a-select-option :value="5">纵向联邦</a-select-option>
              <a-select-option :value="7">复合任务（DAG）</a-select-option>
            </a-select>
            <a-select v-model:value="filterStatus" placeholder="状态" style="width: 130px" allow-clear @change="loadTasks(1)">
              <a-select-option :value="1">已创建</a-select-option>
              <a-select-option :value="2">等待中</a-select-option>
              <a-select-option :value="3">运行中</a-select-option>
              <a-select-option :value="4">已完成</a-select-option>
              <a-select-option :value="5">失败</a-select-option>
              <a-select-option :value="6">已取消</a-select-option>
            </a-select>
            <a-button type="primary" @click="loadTasks()">刷新</a-button>
          </a-space>
        </template>

        <a-table :columns="columns" :data-source="tasks" :loading="loading" :pagination="pagination" row-key="taskId" @change="handleTableChange">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'type'">
              <a-tag :color="getTypeColor(record.type)">
                {{ getTypeText(record.type) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space wrap>
                <a-button size="small" @click="handleDetail(record)">详情</a-button>
                <a-button size="small" type="primary" @click="handleExecute(record)" v-if="record.status === 1" :loading="executingId === record.taskId">执行</a-button>
                <a-button size="small" @click="handleQueryStatus(record)" v-if="[2, 3].includes(record.status)">查询状态</a-button>
                <a-button size="small" danger @click="handleCancel(record)" v-if="![4, 5, 6].includes(record.status)" :disabled="cancelingId === record.taskId">取消</a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>

      <!-- 任务详情弹窗 -->
      <a-modal
        v-model:open="detailVisible"
        :title="`任务详情 - ${currentTask?.name || ''} [v20260803-1855]`"
        :footer="null"
        width="960px"
        :destroy-on-close="true"
        class="task-detail-modal"
      >
        <div v-if="loadingDetail" style="text-align: center; padding: 40px">
          <a-spin />
        </div>
        <div v-else-if="currentDetail" class="detail-scroll">
          <!-- 基本信息 -->
          <section class="detail-section">
            <h3 class="detail-section-title">📌 基本信息</h3>
            <a-descriptions :column="2" bordered size="small">
              <a-descriptions-item label="任务ID">{{ currentDetail.taskId }}</a-descriptions-item>
              <a-descriptions-item label="任务编号">{{ currentDetail.taskCode || '-' }}</a-descriptions-item>
              <a-descriptions-item label="任务名称">{{ currentDetail.name || '-' }}</a-descriptions-item>
              <a-descriptions-item label="任务类型">
                <a-tag :color="getTypeColor(currentDetail.type)">{{ getTypeText(currentDetail.type) }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="任务状态">
                <a-tag :color="getStatusColor(currentDetail.status)">{{ getStatusText(currentDetail.status) }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="算法">{{ currentDetail.algorithm || '-' }}</a-descriptions-item>
              <a-descriptions-item label="节点模式">{{ currentDetail.nodeMode || '-' }}</a-descriptions-item>
              <a-descriptions-item label="创建者">{{ currentDetail.creator || '-' }}</a-descriptions-item>
              <a-descriptions-item label="创建时间">{{ currentDetail.createTime || '-' }}</a-descriptions-item>
              <a-descriptions-item label="更新时间">{{ currentDetail.updateTime || '-' }}</a-descriptions-item>
              <a-descriptions-item label="任务描述" :span="2">
                <div style="white-space: pre-wrap">{{ currentDetail.description || '-' }}</div>
              </a-descriptions-item>
              <a-descriptions-item label="参与方" :span="2">
                <a-tag v-for="p in parseParticipants(currentDetail.participants)" :key="p" color="blue" style="margin: 2px">
                  {{ p }}
                </a-tag>
                <span v-if="!currentDetail.participants">-</span>
              </a-descriptions-item>
            </a-descriptions>
          </section>

          <!-- 任务参数（基础信息字段） -->
          <section v-if="parsedParams && Object.keys(parsedParams).length > 0 && getGroupEntries('basic').length > 0" class="detail-section">
            <h3 class="detail-section-title">📋 任务配置参数</h3>
            <a-descriptions :column="3" bordered size="small">
              <a-descriptions-item v-for="entry in getGroupEntries('basic')" :key="entry.key" :label="entry.label">
                <span v-if="entry.isList" class="param-list">
                  <a-tag v-for="(item, idx) in entry.listValue" :key="idx" color="cyan" style="margin: 2px">
                    {{ item }}
                  </a-tag>
                </span>
                <span v-else style="word-break: break-all">{{ entry.value }}</span>
              </a-descriptions-item>
            </a-descriptions>
          </section>

          <!-- A 方 / B 方 分组 -->
          <section v-if="hasPartyGroup('partyA') || hasPartyGroup('partyB')" class="detail-section">
            <h3 class="detail-section-title">👥 参与方配置</h3>
            <div class="param-party-row">
              <a-card v-if="hasPartyGroup('partyA')" size="small" class="param-party-card param-party-partyA">
                <template #title>
                  <span class="param-group-icon">🅰️</span>
                  <span>A 方（参与方 A）</span>
                </template>
                <a-descriptions :column="1" size="small" :colon="false">
                  <a-descriptions-item v-for="entry in getGroupEntries('partyA')" :key="entry.key" :label="entry.label">
                    <span v-if="entry.isList" class="param-list">
                      <a-tag v-for="(item, idx) in entry.listValue" :key="idx" color="cyan" style="margin: 2px">
                        {{ item }}
                      </a-tag>
                    </span>
                    <span v-else style="word-break: break-all">{{ entry.value }}</span>
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>
              <a-card v-if="hasPartyGroup('partyB')" size="small" class="param-party-card param-party-partyB">
                <template #title>
                  <span class="param-group-icon">🅱️</span>
                  <span>B 方（参与方 B）</span>
                </template>
                <a-descriptions :column="1" size="small" :colon="false">
                  <a-descriptions-item v-for="entry in getGroupEntries('partyB')" :key="entry.key" :label="entry.label">
                    <span v-if="entry.isList" class="param-list">
                      <a-tag v-for="(item, idx) in entry.listValue" :key="idx" color="cyan" style="margin: 2px">
                        {{ item }}
                      </a-tag>
                    </span>
                    <span v-else style="word-break: break-all">{{ entry.value }}</span>
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>
            </div>
          </section>

          <!-- PIR 服务端 / 客户端 分组 -->
          <section v-if="hasPartyGroup('server') || hasPartyGroup('client')" class="detail-section">
            <h3 class="detail-section-title">👥 参与方配置</h3>
            <div class="param-party-row">
              <a-card v-if="hasPartyGroup('server')" size="small" class="param-party-card param-party-server">
                <template #title>
                  <span class="param-group-icon">🗄️</span>
                  <span>服务端（数据提供方）</span>
                </template>
                <a-descriptions :column="1" size="small" :colon="false">
                  <a-descriptions-item v-for="entry in getGroupEntries('server')" :key="entry.key" :label="entry.label">
                    <span v-if="entry.isList" class="param-list">
                      <a-tag v-for="(item, idx) in entry.listValue" :key="idx" color="cyan" style="margin: 2px">
                        {{ item }}
                      </a-tag>
                    </span>
                    <span v-else style="word-break: break-all">{{ entry.value }}</span>
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>
              <a-card v-if="hasPartyGroup('client')" size="small" class="param-party-card param-party-client">
                <template #title>
                  <span class="param-group-icon">🔍</span>
                  <span>客户端（查询方）</span>
                </template>
                <a-descriptions :column="1" size="small" :colon="false">
                  <a-descriptions-item v-for="entry in getGroupEntries('client')" :key="entry.key" :label="entry.label">
                    <span v-if="entry.isList" class="param-list">
                      <a-tag v-for="(item, idx) in entry.listValue" :key="idx" color="cyan" style="margin: 2px">
                        {{ item }}
                      </a-tag>
                    </span>
                    <span v-else style="word-break: break-all">{{ entry.value }}</span>
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>
            </div>
          </section>

          <!-- 执行结果 -->
          <section class="detail-section">
            <h3 class="detail-section-title">🎯 执行结果</h3>
            <div v-if="!currentDetail.result || currentDetail.result === 'null'">
              <a-empty description="任务尚未产生结果，请等待执行完成" />
            </div>
            <div v-else-if="parsedResult && parsedResult.status === 'ok'">
              <a-alert type="success" :message="`执行成功${parsedResult.message ? '：' + parsedResult.message : ''}`" show-icon style="margin-bottom: 16px" />

              <!-- PSI 结果：展示交集/并集行数 -->
              <a-descriptions v-if="parsedResult.jobs && parsedResult.output_path" :column="1" bordered size="small" title="PSI 结果概览">
                <a-descriptions-item v-for="(partyInfo, party) in parsedResult.jobs" :key="party" :label="getPartyLabel(party)">
                  <a-space direction="vertical" style="width: 100%">
                    <div>
                      <a-tag :color="partyInfo.status === 'SUCCEEDED' ? 'green' : 'red'">
                        {{ partyInfo.status }}
                      </a-tag>
                      <span style="margin-left: 8px">作业ID: {{ partyInfo.job_id }}</span>
                    </div>
                    <div v-if="parsedResult.output_path[party]">
                      结果文件：<a-tag color="purple">{{ parsedResult.output_path[party] }}</a-tag>
                      <a-button size="small" type="link" @click="handleDownloadPsiResult(currentDetail.taskId, party)" :loading="downloadingFile === `psi:${party}`">
                        下载结果文件
                      </a-button>
                    </div>
                  </a-space>
                </a-descriptions-item>
              </a-descriptions>

              <!-- FL/VFL 结果：按 party 展示 -->
              <a-descriptions v-else :column="1" bordered size="small" title="各方执行结果">
                <a-descriptions-item v-for="(partyInfo, party) in getPartyResults(parsedResult)" :key="party" :label="getPartyLabel(party)">
                  <a-space direction="vertical" style="width: 100%">
                    <div>
                      <a-tag :color="partyInfo.status === 'SUCCEEDED' ? 'green' : 'red'">
                        {{ partyInfo.status }}
                      </a-tag>
                      <span v-if="parsedResult.jobs && parsedResult.jobs[party]" style="margin-left: 8px">
                        作业ID: {{ parsedResult.jobs[party].job_id }}
                      </span>
                    </div>
                    <div v-if="partyInfo.trainAccuracy !== null && partyInfo.trainAccuracy !== undefined">
                      训练准确率：<a-tag color="geekblue">{{ partyInfo.trainAccuracy }}</a-tag>
                    </div>
                    <div v-if="partyInfo.modelPath">
                      模型文件：<a-tag color="purple">{{ partyInfo.modelPath }}</a-tag>
                      <a-button size="small" type="link" @click="handleDownloadModel(currentDetail.taskId, party)" :loading="downloadingFile === `model:${party}`">
                        下载模型
                      </a-button>
                    </div>
                  </a-space>
                </a-descriptions-item>
              </a-descriptions>

              <!-- 集群信息 -->
              <a-descriptions :column="1" bordered size="small" title="执行环境" style="margin-top: 16px">
                <a-descriptions-item label="Ray 集群ID">{{ parsedResult.ray_cluster_id || '-' }}</a-descriptions-item>
                <a-descriptions-item label="Ray Head 地址">{{ parsedResult.ray_head_url || '-' }}</a-descriptions-item>
              </a-descriptions>
            </div>
            <div v-else-if="parsedResult && parsedResult.status === 'error'">
              <a-alert type="error" :message="`执行失败：${parsedResult.message || '未知错误'}`" show-icon />
            </div>
            <div v-else-if="parsedResult && parsedResult.status === 'cancelled'">
              <a-alert type="warning" message="任务已取消" show-icon />
            </div>
            <div v-else>
              <a-alert type="info" message="无法识别的结果格式" show-icon style="margin-bottom: 8px" />
              <pre class="raw-json">{{ currentDetail.result }}</pre>
            </div>
          </section>

          <!-- 生成的代码 -->
          <section v-if="currentDetail.code" class="detail-section">
            <h3 class="detail-section-title">💻 生成的脚本</h3>
            <a-space style="margin-bottom: 8px">
              <a-button size="small" @click="copyCode" :icon="'copy'">复制代码</a-button>
              <span style="color: #999; font-size: 12px">字符数: {{ currentDetail.code.length }}</span>
            </a-space>
            <pre class="code-block">{{ currentDetail.code }}</pre>
          </section>

          <!-- 执行日志 -->
          <section v-if="currentDetail.executionLog" class="detail-section">
            <h3 class="detail-section-title">📜 执行日志</h3>
            <pre class="code-block">{{ currentDetail.executionLog }}</pre>
          </section>

          <!-- 原始数据 -->
          <section class="detail-section">
            <h3 class="detail-section-title">🔍 原始数据</h3>
            <a-descriptions :column="1" bordered size="small">
              <a-descriptions-item v-for="key in rawFieldOrder" :key="key" :label="rawFieldLabels[key] || key">
                <pre class="raw-json">{{ formatRawValue(key, currentDetail[key]) }}</pre>
              </a-descriptions-item>
            </a-descriptions>
          </section>
        </div>
      </a-modal>
    </a-layout-content>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'

const loading = ref(false)
const tasks = ref([])
const searchKey = ref('')
const filterType = ref(undefined)
const filterStatus = ref(undefined)
const executingId = ref('')
const cancelingId = ref('')
const downloadingFile = ref('')

// 详情弹窗
const detailVisible = ref(false)
const currentTask = ref(null)
const currentDetail = ref(null)
const loadingDetail = ref(false)
const detailTabKey = ref('basic')

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: '任务ID', dataIndex: 'taskId', key: 'taskId', ellipsis: true, width: 200 },
  { title: '任务名称', dataIndex: 'name', key: 'name', ellipsis: true },
  { title: '任务类型', key: 'type', width: 120 },
  { title: '状态', key: 'status', width: 100 },
  { title: '节点模式', dataIndex: 'nodeMode', key: 'nodeMode', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' }
]

// ========== 枚举映射 ==========
const STATUS_MAP = {
  1: { text: '已创建', color: 'blue' },
  2: { text: '等待中', color: 'orange' },
  3: { text: '运行中', color: 'processing' },
  4: { text: '已完成', color: 'green' },
  5: { text: '失败', color: 'red' },
  6: { text: '已取消', color: 'gray' }
}

const TYPE_MAP = {
  1: { text: 'PSI 求交', color: 'blue' },
  2: { text: 'MPC 多方计算', color: 'purple' },
  3: { text: '横向联邦', color: 'cyan' },
  4: { text: 'PIR 隐匿查询', color: 'magenta' },
  5: { text: '纵向联邦', color: 'geekblue' },
  7: { text: '复合任务（DAG）', color: 'volcano' }
}

const PARTY_MAP = {
  alice: 'Alice（A 方）',
  bob: 'Bob（B 方）',
  server: '服务端',
  client: '客户端'
}

const NODE_MODE_MAP = {
  RAY: 'RAY',
  KUSCIA: 'KUSCIA'
}

const PSI_PROTOCOL_MAP = {
  ECPSI: 'ECPSI（椭圆曲线）',
  RR22PSI: 'RR22PSI'
}

const PSI_RESULT_TYPE_MAP = {
  INTERSECTION: '交集',
  UNION: '并集'
}

const FL_DELIVERY_MAP = {
  AGGREGATOR_ONLY: '聚合方保存模型',
  ALL_PARTIES: '各方保存本地模型'
}

const FL_MODEL_MAP = {
  LR: '逻辑回归 (LR)',
  NN: '神经网络 (NN)',
  XGB: '梯度提升 (XGB)'
}

const PIR_TYPE_MAP = {
  SealPIR: 'SealPIR（Label PIR）',
  APSI: 'APSI（Keyword PIR）'
}

const getStatusColor = (status) => STATUS_MAP[status]?.color || 'default'
const getStatusText = (status) => STATUS_MAP[status]?.text || '未知'
const getTypeColor = (type) => TYPE_MAP[type]?.color || 'default'
const getTypeText = (type) => TYPE_MAP[type]?.text || `未知类型(${type})`
const getPartyLabel = (party) => PARTY_MAP[party] || party

const PARTY_GROUP_ICON = {
  partyA: '🅰️',
  partyB: '🅱️',
  server: '🗄️',
  client: '🔍'
}
const getPartyGroupIcon = (groupKey) => PARTY_GROUP_ICON[groupKey] || '👥'

// 从 groupedParamEntries 中获取指定分组的字段列表
const getGroupEntries = (groupKey) => {
  if (!groupedParamEntries.value) return []
  const g = groupedParamEntries.value.find(g => g.key === groupKey)
  return g ? g.entries : []
}

// 判断某个分组是否有内容
const hasPartyGroup = (groupKey) => {
  return getGroupEntries(groupKey).length > 0
}

const parseParticipants = (participants) => {
  if (!participants) return []
  if (Array.isArray(participants)) return participants
  return String(participants).split(',').map(p => p.trim()).filter(Boolean)
}

// ========== 原始字段展示 ==========
const rawFieldOrder = ['taskId', 'taskCode', 'name', 'type', 'status', 'algorithm', 'nodeMode', 'creator', 'description', 'participants', 'parameters', 'code', 'result', 'executionLog', 'createTime', 'updateTime']
const rawFieldLabels = {
  taskId: '任务ID',
  taskCode: '任务编号',
  name: '任务名称',
  type: '任务类型',
  status: '任务状态',
  algorithm: '算法',
  nodeMode: '节点模式',
  creator: '创建者',
  description: '任务描述',
  participants: '参与方',
  parameters: '参数JSON',
  code: '生成的代码',
  result: '执行结果JSON',
  executionLog: '执行日志',
  createTime: '创建时间',
  updateTime: '更新时间'
}

const formatRawValue = (key, value) => {
  if (value === null || value === undefined || value === '') return '（空）'
  if (key === 'type') return getTypeText(value)
  if (key === 'status') return getStatusText(value)
  if (key === 'nodeMode') return NODE_MODE_MAP[value] || value
  if (key === 'participants') return parseParticipants(value).join(', ')
  return String(value)
}

// ========== 参数解析与展示 ==========

// 通用参数映射（key -> {label, hidden, group}）
// group: basic / partyA / partyB / server / client / config
const PARAM_LABELS = {
  // 通用（基础信息）
  computeType: { label: '计算类型', hidden: true },
  name: { label: '任务名称', hidden: true },
  participants: { label: '参与方', group: 'basic' },
  nodeMode: { label: '节点模式', group: 'basic' },
  // PSI / FL / VFL 共享
  keyColumn: { label: '关联键列', group: 'basic' },
  protocol: { label: '协议类型', enum: PSI_PROTOCOL_MAP, group: 'basic' },
  resultType: { label: '结果类型', enum: PSI_RESULT_TYPE_MAP, group: 'basic' },
  // A 方（参与方 A）
  partyANodeId: { label: '节点', group: 'partyA' },
  partyADataPath: { label: '数据路径', group: 'partyA' },
  // B 方（参与方 B）
  partyBNodeId: { label: '节点', group: 'partyB' },
  partyBDataPath: { label: '数据路径', group: 'partyB' },
  // MPC
  algorithm: { label: '算法名称', group: 'basic' },
  // FL（基础）
  modelType: { label: '模型类型', enum: FL_MODEL_MAP, group: 'basic' },
  labelColumn: { label: '标签列', group: 'basic' },
  featureColumns: { label: '特征列', isList: true, separator: ',', group: 'basic' },
  epochs: { label: '训练轮次（Epochs）', group: 'basic' },
  batchSize: { label: '批量大小（Batch Size）', group: 'basic' },
  deliveryMode: { label: '交付模式', enum: FL_DELIVERY_MAP, group: 'basic' },
  learningRate: { label: '学习率', group: 'basic' },
  // VFL（基础）
  idColumn: { label: '样本ID列', group: 'basic' },
  labelOwner: { label: '标签持有方', group: 'basic' },
  partyAFeatureColumns: { label: '特征列', isList: true, separator: ',', group: 'partyA' },
  partyBFeatureColumns: { label: '特征列', isList: true, separator: ',', group: 'partyB' },
  // PIR
  pirType: { label: 'PIR 协议', enum: PIR_TYPE_MAP, group: 'basic' },
  serverNodeId: { label: '节点', group: 'server' },
  inputPath: { label: '数据文件路径', group: 'server' },
  clientNodeId: { label: '节点', group: 'client' },
  queryValue: { label: '查询值', group: 'client' },
  labelColumns: { label: '返回列', group: 'server' }
}

// 参数分组配置：哪些字段属于哪个分组
const PARAM_GROUPS = {
  basic: { title: '基础信息', icon: '📋' },
  partyA: { title: 'A 方（参与方 A）', icon: '🅰️', color: 'blue' },
  partyB: { title: 'B 方（参与方 B）', icon: '🅱️', color: 'purple' },
  server: { title: '服务端（数据提供方）', icon: '🗄️', color: 'cyan' },
  client: { title: '客户端（查询方）', icon: '🔍', color: 'orange' }
}

// 隐藏的字段（参数内部标识）
const HIDDEN_PARAM_KEYS = new Set(['computeType', 'name', 'taskName'])

// 默认分组：未明确分组的字段归入 basic
const DEFAULT_GROUP = 'basic'

// 字段在分组内的展示顺序
const GROUP_FIELD_ORDER = {
  basic: ['algorithm', 'participants', 'nodeMode', 'protocol', 'resultType', 'pirType', 'modelType', 'labelColumn', 'featureColumns', 'epochs', 'batchSize', 'deliveryMode', 'learningRate', 'idColumn', 'labelOwner', 'keyColumn'],
  partyA: ['partyANodeId', 'partyADataPath', 'partyAFeatureColumns'],
  partyB: ['partyBNodeId', 'partyBDataPath', 'partyBFeatureColumns'],
  server: ['serverNodeId', 'inputPath', 'labelColumns'],
  client: ['clientNodeId', 'queryValue']
}

const parsedParams = computed(() => {
  if (!currentDetail.value?.parameters) return null
  try {
    const p = JSON.parse(currentDetail.value.parameters)
    // 把数组形式的字符串（如 features）解析回来
    const out = {}
    for (const [k, v] of Object.entries(p)) {
      out[k] = v
    }
    return out
  } catch (e) {
    return null
  }
})

const getVisibleParamEntries = (params) => {
  const entries = []
  for (const [key, value] of Object.entries(params)) {
    if (HIDDEN_PARAM_KEYS.has(key)) continue
    if (value === null || value === undefined || value === '') continue
    const meta = PARAM_LABELS[key] || { label: key }
    let displayValue = value
    let isList = false
    let listValue = null

    if (meta.isList && typeof value === 'string') {
      isList = true
      listValue = value.split(meta.separator || ',').map(s => s.trim()).filter(Boolean)
      if (listValue.length === 0) continue
    } else if (meta.enum) {
      displayValue = meta.enum[value] || value
    }
    entries.push({
      key,
      label: meta.label,
      value: displayValue,
      isList,
      listValue,
      group: meta.group || DEFAULT_GROUP
    })
  }
  return entries
}

// 按分组组织参数：{ basic: [...], partyA: [...], ... }
const groupedParamEntries = computed(() => {
  if (!parsedParams.value) return null
  const all = getVisibleParamEntries(parsedParams.value)
  const groups = {}
  for (const entry of all) {
    const g = entry.group
    if (!groups[g]) groups[g] = []
    groups[g].push(entry)
  }
  // 按 GROUP_FIELD_ORDER 排序每个分组内的字段
  for (const g of Object.keys(groups)) {
    const order = GROUP_FIELD_ORDER[g] || []
    groups[g].sort((a, b) => {
      const ia = order.indexOf(a.key)
      const ib = order.indexOf(b.key)
      if (ia === -1 && ib === -1) return 0
      if (ia === -1) return 1
      if (ib === -1) return -1
      return ia - ib
    })
  }
  // 返回按显示顺序排列的分组列表
  const orderedGroupKeys = ['basic', 'partyA', 'partyB', 'server', 'client']
  const result = []
  for (const key of orderedGroupKeys) {
    if (groups[key] && groups[key].length > 0) {
      result.push({
        key,
        title: PARAM_GROUPS[key]?.title || key,
        color: PARAM_GROUPS[key]?.color || 'default',
        entries: groups[key]
      })
    }
  }
  // 其它未识别的分组也放出来
  for (const key of Object.keys(groups)) {
    if (!orderedGroupKeys.includes(key)) {
      result.push({
        key,
        title: key,
        color: 'default',
        entries: groups[key]
      })
    }
  }
  return result
})

// ========== 结果解析 ==========
const parsedResult = computed(() => {
  if (!currentDetail.value?.result || currentDetail.value.result === 'null') return null
  try {
    return JSON.parse(currentDetail.value.result)
  } catch (e) {
    return null
  }
})

const getPartyResults = (result) => {
  const out = {}
  if (result.party_alice) out.alice = result.party_alice
  if (result.party_bob) out.bob = result.party_bob
  return out
}

// ========== API 调用 ==========

const loadTasks = async (page) => {
  if (page) pagination.current = page
  loading.value = true
  try {
    const res = await axios.get('/api/dos/privacy/task/list', {
      params: {
        page: pagination.current,
        size: pagination.pageSize
      }
    })
    if (res.data?.code === 200) {
      let list = res.data.data?.list || []
      // 前端过滤
      if (searchKey.value) {
        const key = searchKey.value.toLowerCase()
        list = list.filter(t =>
          (t.taskId && t.taskId.toLowerCase().includes(key)) ||
          (t.name && t.name.toLowerCase().includes(key))
        )
      }
      if (filterType.value !== undefined && filterType.value !== null) {
        list = list.filter(t => t.type === filterType.value)
      }
      if (filterStatus.value !== undefined && filterStatus.value !== null) {
        list = list.filter(t => t.status === filterStatus.value)
      }
      tasks.value = list
      pagination.total = res.data.data?.pagination?.total || list.length
    } else {
      message.error('加载任务列表失败: ' + (res.data?.msg || '未知错误'))
    }
  } catch (error) {
    message.error('加载任务列表失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadTasks()
}

const handleDetail = async (record) => {
  currentTask.value = record
  detailVisible.value = true
  detailTabKey.value = 'basic'
  loadingDetail.value = true
  currentDetail.value = null
  try {
    const res = await axios.get(`/api/dos/privacy/task/${record.taskId}/detail`)
    if (res.data?.code === 200) {
      currentDetail.value = res.data.data
    } else {
      message.error('加载任务详情失败: ' + (res.data?.msg || '未知错误'))
    }
  } catch (error) {
    message.error('加载任务详情失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
  } finally {
    loadingDetail.value = false
  }
}

const handleExecute = async (record) => {
  executingId.value = record.taskId
  try {
    await axios.post(`/api/dos/privacy/task/${record.taskId}/execute`)
    message.success(`任务「${record.name || record.taskId}」已开始执行`)
    loadTasks()
  } catch (error) {
    message.error('执行失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
  } finally {
    executingId.value = ''
  }
}

const handleQueryStatus = async (record) => {
  try {
    const res = await axios.get(`/api/dos/privacy/task/${record.taskId}/status`)
    const status = res.data?.data?.status
    message.info(`当前状态: ${getStatusText(status)}`)
    loadTasks()
  } catch (error) {
    message.error('查询失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
  }
}

const handleCancel = async (record) => {
  cancelingId.value = record.taskId
  try {
    await axios.post(`/api/dos/privacy/task/${record.taskId}/cancel`)
    message.success('任务已取消')
    loadTasks()
  } catch (error) {
    message.error('取消失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
  } finally {
    cancelingId.value = ''
  }
}

const handleDownloadPsiResult = async (taskId, party) => {
  downloadingFile.value = `psi:${party}`
  try {
    const res = await axios.get(`/api/dos/privacy/psi/${taskId}/result`, {
      params: { party },
      responseType: 'blob'
    })
    downloadBlob(res.data, `${taskId}_${party}_psi_result.csv`)
    message.success('下载成功')
  } catch (error) {
    message.error('下载失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
  } finally {
    downloadingFile.value = ''
  }
}

const handleDownloadModel = async (taskId, party) => {
  downloadingFile.value = `model:${party}`
  try {
    const res = await axios.get(`/api/dos/privacy/model/${taskId}/download`, {
      params: { party },
      responseType: 'blob'
    })
    downloadBlob(res.data, `${taskId}_${party}_model.pkl`)
    message.success('下载成功')
  } catch (error) {
    message.error('下载失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
  } finally {
    downloadingFile.value = ''
  }
}

const downloadBlob = (data, filename) => {
  const url = window.URL.createObjectURL(new Blob([data]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

const copyCode = async () => {
  if (!currentDetail.value?.code) return
  try {
    await navigator.clipboard.writeText(currentDetail.value.code)
    message.success('代码已复制')
  } catch (e) {
    message.error('复制失败，请手动选择')
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
.content {
  padding: 24px;
}
/* 任务详情弹窗：滚动区域 + 章节分隔 */
.detail-scroll {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 4px;
}
.detail-section {
  margin-bottom: 24px;
}
.detail-section:last-child {
  margin-bottom: 0;
}
.detail-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 12px 0;
  padding: 8px 12px;
  background: #f5f7fa;
  border-left: 3px solid #1890ff;
  border-radius: 3px;
}
.code-block {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
  line-height: 1.6;
  max-height: 500px;
  overflow: auto;
  white-space: pre;
  margin: 0;
}
.raw-json {
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  padding: 8px 12px;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
  line-height: 1.5;
  max-height: 400px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
.param-list {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 4px;
}
.param-group {
  margin-bottom: 20px;
}
.param-group-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 8px;
  color: #262626;
  display: flex;
  align-items: center;
  gap: 6px;
}
.param-group-icon {
  font-size: 16px;
}
.param-group-text {
  color: #262626;
}
/* 参与方分组行：两个一组（A/B），（server/client），单列时占满 */
.param-party-row {
  margin-bottom: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
  gap: 16px;
}
.param-party-card {
  height: 100%;
}
.param-party-card :deep(.ant-card-head) {
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 12px;
  min-height: 40px;
}
.param-party-card :deep(.ant-card-head-title) {
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}
.param-party-card.param-party-partyA :deep(.ant-card-head) {
  background: #e6f4ff;
  border-bottom-color: #91caff;
}
.param-party-card.param-party-partyB :deep(.ant-card-head) {
  background: #f9f0ff;
  border-bottom-color: #d3adf7;
}
.param-party-card.param-party-server :deep(.ant-card-head) {
  background: #e6fffb;
  border-bottom-color: #87e8de;
}
.param-party-card.param-party-client :deep(.ant-card-head) {
  background: #fff7e6;
  border-bottom-color: #ffd591;
}
.param-party-card :deep(.ant-descriptions-item-label) {
  width: 90px;
  color: #595959;
  font-size: 12px;
}
.param-party-card :deep(.ant-descriptions-item-content) {
  font-size: 13px;
}
:deep(.task-detail-modal .ant-modal-body) {
  max-height: 70vh;
  overflow-y: auto;
}
</style>