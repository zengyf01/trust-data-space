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
              <a-button size="small" danger @click="handleDeleteTask(record)" v-if="[5, 6].includes(record.status)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 任务详情弹窗 -->
    <a-modal v-model:open="taskDetailVisible" title="任务详情" :footer="null" width="1000px">
      <a-tabs v-if="currentTask" v-model:activeKey="detailActiveTab">
        <!-- 基本信息页签（已合并任务参数） -->
        <a-tab-pane key="basic" tab="基本信息">
          <!-- 公共字段区 -->
          <a-descriptions :column="2" size="small" bordered title="基本信息">
            <a-descriptions-item label="任务ID" :span="2">
              <a-typography-paragraph copyable style="margin: 0">{{ currentTask.taskId }}</a-typography-paragraph>
            </a-descriptions-item>
            <a-descriptions-item label="任务编号">{{ currentTask.taskCode || '-' }}</a-descriptions-item>
            <a-descriptions-item label="任务名称">{{ currentTask.name || '-' }}</a-descriptions-item>
            <a-descriptions-item label="任务类型">
              <a-tag>{{ getTaskTypeText(currentTask.type) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="getTaskStatusColor(currentTask.status)">{{ getTaskStatusText(currentTask.status) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="创建人">{{ currentTask.creator || '-' }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ currentTask.createTime || '-' }}</a-descriptions-item>
            <a-descriptions-item label="算法">{{ currentTask.algorithm || '-' }}</a-descriptions-item>
            <a-descriptions-item label="节点模式">{{ currentTask.nodeMode || 'RAY' }}</a-descriptions-item>
            <a-descriptions-item label="描述" :span="2">{{ currentTask.description || '-' }}</a-descriptions-item>
          </a-descriptions>

          <!-- 参与方字段区（A方/B方分开彩色 Card） -->
          <div class="party-section" style="margin-top: 16px">
            <h4 class="section-subtitle">👥 参与方</h4>
            <div class="party-row">
              <!-- A 方 -->
              <a-card size="small" class="party-card party-card-a">
                <template #title>
                  <span class="party-icon">🅰️</span>
                  <span>A 方</span>
                </template>
                <a-descriptions :column="1" size="small" :colon="false">
                  <a-descriptions-item label="节点">
                    <a-typography-paragraph v-if="currentTask.partyANodeId" copyable style="margin: 0">{{ currentTask.partyANodeId }}</a-typography-paragraph>
                    <span v-else>-</span>
                  </a-descriptions-item>
                  <a-descriptions-item label="数据路径" v-if="currentTask.partyADataPath">
                    <code>{{ currentTask.partyADataPath }}</code>
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>
              <!-- B 方 -->
              <a-card size="small" class="party-card party-card-b">
                <template #title>
                  <span class="party-icon">🅱️</span>
                  <span>B 方</span>
                </template>
                <a-descriptions :column="1" size="small" :colon="false">
                  <a-descriptions-item label="节点">
                    <a-typography-paragraph v-if="currentTask.partyBNodeId" copyable style="margin: 0">{{ currentTask.partyBNodeId }}</a-typography-paragraph>
                    <span v-else>-</span>
                  </a-descriptions-item>
                  <a-descriptions-item label="数据路径" v-if="currentTask.partyBDataPath">
                    <code>{{ currentTask.partyBDataPath }}</code>
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>
            </div>
            <!-- 参与方列表（多参与方时） -->
            <div v-if="(currentTask.participants || '').split(',').filter(x => x.trim()).length > 0" class="participants-tags" style="margin-top: 8px">
              <span style="color: #595959; margin-right: 8px">参与方列表：</span>
              <a-tag v-for="p in (currentTask.participants || '').split(',').filter(x => x.trim())" :key="p" color="blue" style="margin: 2px">{{ p.trim() }}</a-tag>
            </div>
          </div>

          <!-- 任务参数区（按参与方分组） -->
          <div v-if="taskParams && Object.keys(taskParams).length > 0" class="task-params-section" style="margin-top: 16px">
            <h4 class="section-subtitle">📋 任务参数</h4>

            <!-- 基础参数（公共字段） -->
            <a-descriptions
              v-if="getParamGroup('basic').length > 0"
              :column="2"
              size="small"
              bordered
              title="基础参数"
              style="margin-top: 8px"
            >
              <a-descriptions-item v-for="item in getParamGroup('basic')" :key="item.key" :label="item.label">
                {{ item.value || '-' }}
              </a-descriptions-item>
            </a-descriptions>

            <!-- A 方参数 -->
            <a-card
              v-if="getParamGroup('partyA').length > 0"
              size="small"
              class="party-card party-card-a"
              style="margin-top: 16px"
            >
              <template #title>
                <span class="party-icon">🅰️</span>
                <span>A 方参数</span>
              </template>
              <a-descriptions :column="1" size="small" :colon="false">
                <a-descriptions-item v-for="item in getParamGroup('partyA')" :key="item.key" :label="item.label">
                  <code v-if="isCodeLike(item.value)">{{ item.value || '-' }}</code>
                  <span v-else>{{ item.value || '-' }}</span>
                </a-descriptions-item>
              </a-descriptions>
            </a-card>

            <!-- B 方参数 -->
            <a-card
              v-if="getParamGroup('partyB').length > 0"
              size="small"
              class="party-card party-card-b"
              style="margin-top: 16px"
            >
              <template #title>
                <span class="party-icon">🅱️</span>
                <span>B 方参数</span>
              </template>
              <a-descriptions :column="1" size="small" :colon="false">
                <a-descriptions-item v-for="item in getParamGroup('partyB')" :key="item.key" :label="item.label">
                  <code v-if="isCodeLike(item.value)">{{ item.value || '-' }}</code>
                  <span v-else>{{ item.value || '-' }}</span>
                </a-descriptions-item>
              </a-descriptions>
            </a-card>

            <!-- 服务端参数（PIR） -->
            <a-card
              v-if="getParamGroup('server').length > 0"
              size="small"
              class="party-card party-card-server"
              style="margin-top: 16px"
            >
              <template #title>
                <span class="party-icon">🗄️</span>
                <span>服务端参数</span>
              </template>
              <a-descriptions :column="1" size="small" :colon="false">
                <a-descriptions-item v-for="item in getParamGroup('server')" :key="item.key" :label="item.label">
                  <code v-if="isCodeLike(item.value)">{{ item.value || '-' }}</code>
                  <span v-else>{{ item.value || '-' }}</span>
                </a-descriptions-item>
              </a-descriptions>
            </a-card>

            <!-- 客户端参数（PIR） -->
            <a-card
              v-if="getParamGroup('client').length > 0"
              size="small"
              class="party-card party-card-client"
              style="margin-top: 16px"
            >
              <template #title>
                <span class="party-icon">🔍</span>
                <span>客户端参数</span>
              </template>
              <a-descriptions :column="1" size="small" :colon="false">
                <a-descriptions-item v-for="item in getParamGroup('client')" :key="item.key" :label="item.label">
                  <code v-if="isCodeLike(item.value)">{{ item.value || '-' }}</code>
                  <span v-else>{{ item.value || '-' }}</span>
                </a-descriptions-item>
              </a-descriptions>
            </a-card>
          </div>
        </a-tab-pane>

        <!-- 执行脚本页签（按任务类型动态命名） -->
        <a-tab-pane key="code" :tab="getCodeTabName(currentTask.type)">
          <a-spin v-if="loadingCode" tip="加载代码中..." />
          <template v-else>
            <a-empty v-if="!taskCode" description="暂无生成的代码，任务执行后生成" />
            <div v-else>
              <a-space style="margin-bottom: 12px">
                <a-tag color="processing">{{ getCodeTabName(currentTask.type) }} · {{ taskCode.split('\n').length }} 行 · {{ taskCode.length }} 字符</a-tag>
                <a-button type="primary" @click="handleCopyCode">
                  <template #icon><CopyOutlined /></template>
                  复制代码
                </a-button>
                <a-button @click="handleDownloadCode">
                  <template #icon><DownloadOutlined /></template>
                  下载脚本
                </a-button>
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
            <template v-else-if="isPsiResult">
              <!-- PSI 结果展示 -->
              <a-descriptions :column="2" size="small" bordered>
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
                <a-descriptions-item label="结果下载" :span="2">
                  <a-button type="primary" @click="handleDownloadResult('alice')" v-if="parsedResult.output_path?.alice">
                    <template #icon><DownloadOutlined /></template>
                    下载结果
                  </a-button>
                  <a-tag v-else color="warning">无可用结果</a-tag>
                </a-descriptions-item>
              </a-descriptions>
            </template>
            <template v-else-if="isFlResult">
              <!-- FL/VFL 结果展示 -->
              <a-descriptions :column="2" size="small" bordered>
                <a-descriptions-item label="Ray Head">{{ parsedResult.ray_head_url || '-' }}</a-descriptions-item>
                <a-descriptions-item label="交付模式">{{ parsedResult.deliveryMode || '-' }}</a-descriptions-item>
                <a-descriptions-item label="执行消息" :span="2">{{ parsedResult.message || '-' }}</a-descriptions-item>
              </a-descriptions>
              <a-divider>各方结果</a-divider>
              <a-row :gutter="16">
                <a-col :span="12" v-if="parsedResult.party_alice">
                  <a-card title="Alice 方" size="small">
                    <a-descriptions :column="1" size="small">
                      <a-descriptions-item label="状态">
                        <a-tag :color="parsedResult.party_alice.status === 'SUCCEEDED' ? 'green' : 'red'">
                          {{ parsedResult.party_alice.status }}
                        </a-tag>
                      </a-descriptions-item>
                      <a-descriptions-item label="训练准确率">
                        {{ parsedResult.party_alice.trainAccuracy != null ? parsedResult.party_alice.trainAccuracy.toFixed(4) : '-' }}
                      </a-descriptions-item>
                      <a-descriptions-item label="模型路径">{{ parsedResult.party_alice.modelPath || '-' }}</a-descriptions-item>
                      <a-descriptions-item label="模型下载">
                        <a-button type="primary" size="small" @click="handleDownloadModel('alice')" v-if="parsedResult.party_alice.modelPath">
                          <template #icon><DownloadOutlined /></template>
                          下载模型
                        </a-button>
                        <a-tag v-else color="warning">无模型</a-tag>
                      </a-descriptions-item>
                    </a-descriptions>
                  </a-card>
                </a-col>
                <a-col :span="12" v-if="parsedResult.party_bob && parsedResult.deliveryMode === 'ALL_PARTIES'">
                  <a-card title="Bob 方" size="small">
                    <a-descriptions :column="1" size="small">
                      <a-descriptions-item label="状态">
                        <a-tag :color="parsedResult.party_bob.status === 'SUCCEEDED' ? 'green' : 'red'">
                          {{ parsedResult.party_bob.status }}
                        </a-tag>
                      </a-descriptions-item>
                      <a-descriptions-item label="训练准确率">
                        {{ parsedResult.party_bob.trainAccuracy != null ? parsedResult.party_bob.trainAccuracy.toFixed(4) : '-' }}
                      </a-descriptions-item>
                      <a-descriptions-item label="模型路径">{{ parsedResult.party_bob.modelPath || '-' }}</a-descriptions-item>
                      <a-descriptions-item label="模型下载">
                        <a-button type="primary" size="small" @click="handleDownloadModel('bob')" v-if="parsedResult.party_bob.modelPath">
                          <template #icon><DownloadOutlined /></template>
                          下载模型
                        </a-button>
                        <a-tag v-else color="warning">无模型</a-tag>
                      </a-descriptions-item>
                    </a-descriptions>
                  </a-card>
                </a-col>
              </a-row>
              <a-alert v-if="parsedResult.deliveryMode === 'AGGREGATOR_ONLY'" message="聚合模式：仅 Alice 方保存聚合模型" type="info" show-icon style="margin-top: 16px" />
            </template>
            <a-descriptions v-else :column="2" size="small" bordered>
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
import { CopyOutlined, DownloadOutlined } from '@ant-design/icons-vue'
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
    // currentTaskResult 可能是 API 返回的 {taskId, result} 或直接的 result 字符串
    let data = currentTaskResult.value
    if (typeof data === 'string') {
      data = JSON.parse(data)
    }
    // 如果是 {taskId, result} 结构，需要取 result 字段
    if (data.result) {
      if (typeof data.result === 'string') {
        return JSON.parse(data.result)
      }
      return data.result
    }
    return data
  } catch {
    return null
  }
})

// 判断是否为 PSI 结果
const isPsiResult = computed(() => {
  return parsedResult.value && parsedResult.value.intersection_count !== undefined
})

// 判断是否为 FL/VFL 结果
const isFlResult = computed(() => {
  return parsedResult.value && (parsedResult.value.party_alice || parsedResult.value.party_bob)
})

// 格式化参数key为中文（覆盖 PSI / FL / VFL / PIR 全部用户输入字段）
const formatParamKey = (key) => {
  const keyMap = {
    // 通用
    'partyANodeId': 'A方节点ID',
    'partyBNodeId': 'B方节点ID',
    'partyADataPath': 'A方数据路径',
    'partyBDataPath': 'B方数据路径',
    'nodeMode': '节点模式',
    'computeType': '计算类型',
    'algorithm': '算法',
    'selfParty': '本方角色',
    'partyACrossSiloAddress': 'A方跨域通信地址',
    'partyBCrossSiloAddress': 'B方跨域通信地址',
    'partyASpuAddress': 'A方SPU地址',
    'partyBSpuAddress': 'B方SPU地址',
    // PSI
    'keyColumn': '关联键',
    'protocol': '协议',
    'resultType': '结果类型',
    'receiver': '接收方',
    // 横向联邦 (HFL)
    'labelColumn': '标签列',
    'featureColumns': '特征列',
    'modelType': '模型类型',
    'deliveryMode': '交付模式',
    'epochs': '训练轮数',
    'batchSize': '批大小',
    'learningRate': '学习率',
    'modelPath': '模型保存路径',
    // 纵向联邦 (VFL)
    'idColumn': '样本ID列',
    'labelOwner': '标签拥有方',
    'partyAFeatureColumns': 'A方特征列',
    'partyBFeatureColumns': 'B方特征列',
    // PIR
    'queryColumn': '查询列',
    'databasePath': '数据库路径',
    'queryValue': '查询值'
  }
  return keyMap[key] || key
}

// 参数分组规则：key 前缀匹配对应分组
const PARAM_GROUP_RULES = [
  { group: 'partyA', prefix: 'partyA' },
  { group: 'partyB', prefix: 'partyB' },
  { group: 'server', prefix: 'server' },
  { group: 'client', prefix: 'client' }
]
// 明确归入"基础参数"的 key
const BASIC_PARAM_KEYS = new Set([
  'computeType', 'algorithm', 'selfParty', 'nodeMode',
  'keyColumn', 'protocol', 'resultType', 'receiver',
  'labelColumn', 'featureColumns', 'modelType', 'deliveryMode',
  'epochs', 'batchSize', 'learningRate', 'modelPath',
  'idColumn', 'labelOwner',
  'queryColumn', 'databasePath', 'queryValue'
])

// 按分组获取参数项
const getParamGroup = (group) => {
  if (!taskParams.value) return []
  const items = []
  for (const [key, value] of Object.entries(taskParams.value)) {
    let matchedGroup = null
    // 优先按前缀匹配
    for (const rule of PARAM_GROUP_RULES) {
      if (key === rule.prefix || key.startsWith(rule.prefix)) {
        matchedGroup = rule.group
        break
      }
    }
    // 未匹配前缀的，检查是否在基础参数白名单
    if (!matchedGroup) {
      if (BASIC_PARAM_KEYS.has(key)) {
        matchedGroup = 'basic'
      } else {
        continue // 跳过无法识别的字段
      }
    }
    if (matchedGroup === group) {
      items.push({ key, label: formatParamKey(key), value })
    }
  }
  return items
}

// 判断 value 是否像代码/路径（用 <code> 标签）
const isCodeLike = (value) => {
  if (!value || typeof value !== 'string') return false
  // 文件路径、节点 ID 等
  return /^[\/]?[a-zA-Z0-9_\-\/\.]+$/.test(value) && (value.includes('/') || value.includes('-'))
}

// 根据任务类型返回脚本 Tab 名
const getCodeTabName = (type) => {
  const names = {
    1: 'PSI脚本',
    2: 'MPC脚本',
    3: '横向联邦脚本',
    4: '自定义代码',
    5: '纵向联邦脚本',
    6: '复合任务脚本',
    7: '组件DAG脚本',
    8: 'PIR脚本'
  }
  return names[type] || '执行脚本'
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

// 下载脚本（按任务类型生成对应文件名）
const handleDownloadCode = () => {
  if (!taskCode.value || !currentTask.value) return
  const typeMap = {
    1: 'psi', 2: 'mpc', 3: 'hfl', 4: 'custom',
    5: 'vfl', 6: 'compound', 7: 'dag', 8: 'pir'
  }
  const prefix = typeMap[currentTask.value.type] || 'task'
  const filename = `${prefix}_${currentTask.value.taskId || 'script'}.py`
  const blob = new Blob([taskCode.value], { type: 'text/x-python;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
  message.success(`已下载: ${filename}`)
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
  const texts = { 1: 'PSI', 2: 'MPC', 3: '横向联邦', 5: '纵向联邦', 6: 'DAG任务' }
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
    // 先获取任务详情，确保 currentTask 是最新数据
    const detailResp = await axios.get(`/api/dos/privacy/task/${record.taskId}/detail`)
    if (detailResp.data.code === 200 && detailResp.data.data) {
      currentTask.value = detailResp.data.data
      // 解析 parameters
      if (detailResp.data.data.parameters) {
        try {
          taskParams.value = typeof detailResp.data.data.parameters === 'string'
            ? JSON.parse(detailResp.data.data.parameters)
            : detailResp.data.data.parameters
        } catch {}
      }
      // 解析 code
      if (detailResp.data.data.code) {
        taskCode.value = detailResp.data.data.code
      }
    }

    // 获取执行结果
    const response = await axios.get(`/api/dos/privacy/task/${record.taskId}/result`)
    currentTaskResult.value = response.data.data

    detailActiveTab.value = 'result'
    taskDetailVisible.value = true
  } catch (error) {
    message.error('获取结果失败: ' + (error.message || '未知错误'))
  }
}

// 下载PSI结果文件
const handleDownloadResult = async (party) => {
  if (!currentTask.value) return
  try {
    const response = await axios.get(`/api/dos/privacy/psi/${currentTask.value.taskId}/result`, {
      params: { party },
      responseType: 'blob'
    })
    const blob = new Blob([response.data], { type: 'text/csv' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `psi_result_${currentTask.value.taskId}_${party}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    message.success('下载成功')
  } catch (error) {
    message.error('下载失败: ' + (error.message || '未知错误'))
  }
}

// 下载FL/VFL模型文件
const handleDownloadModel = async (party) => {
  if (!currentTask.value) return
  try {
    const response = await axios.get(`/api/dos/privacy/model/${currentTask.value.taskId}/download`, {
      params: { party },
      responseType: 'blob'
    })
    const blob = new Blob([response.data], { type: 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `model_${currentTask.value.taskId}_${party}.pkl`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    message.success('模型下载成功')
  } catch (error) {
    message.error('模型下载失败: ' + (error.message || '未知错误'))
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
  const isFailed = record.status === 5
  const tip = isFailed
    ? `确定要删除失败任务「${record.name || record.taskId}」吗？删除后不可恢复。`
    : `确定要删除已取消任务「${record.name || record.taskId}」吗？`
  if (!window.confirm(tip)) return
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
/* 区域标题 */
.section-subtitle {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 12px 0;
  padding: 6px 12px;
  background: #f5f7fa;
  border-left: 3px solid #1890ff;
  border-radius: 3px;
}

/* 参与方卡片 */
.party-section {
  margin-bottom: 16px;
}
.party-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
  gap: 16px;
}
.party-card {
  height: 100%;
  border-radius: 6px;
  overflow: hidden;
}
.party-card :deep(.ant-card-head) {
  min-height: 44px;
  padding: 0 14px;
  border-bottom: 2px solid transparent;
}
.party-card :deep(.ant-card-head-title) {
  padding: 10px 0;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}
.party-icon {
  font-size: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  color: white;
}
/* A 方：蓝色 */
.party-card-a :deep(.ant-card-head) {
  background: #e6f4ff;
  border-bottom-color: #91caff;
}
.party-card-a .party-icon {
  background: #1677ff;
}
/* B 方：紫色 */
.party-card-b :deep(.ant-card-head) {
  background: #f9f0ff;
  border-bottom-color: #d3adf7;
}
.party-card-b .party-icon {
  background: #722ed1;
}
/* 服务端：青色 */
.party-card-server :deep(.ant-card-head) {
  background: #e6fffb;
  border-bottom-color: #87e8de;
}
.party-card-server .party-icon {
  background: #13c2c2;
}
/* 客户端：橙色 */
.party-card-client :deep(.ant-card-head) {
  background: #fff7e6;
  border-bottom-color: #ffd591;
}
.party-card-client .party-icon {
  background: #fa8c16;
}
.party-card :deep(.ant-descriptions-item-label) {
  width: 80px;
  color: #595959;
  font-size: 12px;
}
.party-card :deep(.ant-descriptions-item-content) {
  font-size: 13px;
}
.participants-tags {
  color: #595959;
}

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
