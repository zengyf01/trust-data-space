<template>
  <div class="dag-designer-container">
    <a-layout>
      <a-layout-header class="header">
        <h1>DAG 工作流设计器</h1>
        <div class="header-actions">
          <a-button @click="handleClear">清空画布</a-button>
          <a-button @click="handlePreview">预览 DAG</a-button>
          <a-button type="primary" @click="handleSave" :loading="saving">保存 DAG</a-button>
        </div>
      </a-layout-header>
      <a-layout-content class="content">
        <a-row :gutter="16">
          <!-- 左侧组件面板 -->
          <a-col :span="5" class="component-panel">
            <a-card title="组件库" size="small">
              <a-collapse v-model:activeKey="activeComponentKey">
                <a-collapse-panel key="data" header="数据输入">
                  <div
                    v-for="comp in componentList.data"
                    :key="comp.id"
                    class="component-item"
                    draggable="true"
                    @dragstart="onDragStart($event, comp)"
                    @click="addComponentToCanvas(comp)"
                  >
                    <database-outlined />
                    {{ comp.label }}
                  </div>
                </a-collapse-panel>
                <a-collapse-panel key="alignment" header="数据对齐">
                  <div
                    v-for="comp in componentList.alignment"
                    :key="comp.id"
                    class="component-item"
                    draggable="true"
                    @dragstart="onDragStart($event, comp)"
                    @click="addComponentToCanvas(comp)"
                  >
                    <share-alt-outlined />
                    {{ comp.label }}
                  </div>
                </a-collapse-panel>
                <a-collapse-panel key="filter" header="数据过滤">
                  <div
                    v-for="comp in componentList.filter"
                    :key="comp.id"
                    class="component-item"
                    draggable="true"
                    @dragstart="onDragStart($event, comp)"
                    @click="addComponentToCanvas(comp)"
                  >
                    <filter-outlined />
                    {{ comp.label }}
                  </div>
                </a-collapse-panel>
                <a-collapse-panel key="preprocessing" header="预处理">
                  <div
                    v-for="comp in componentList.preprocessing"
                    :key="comp.id"
                    class="component-item"
                    draggable="true"
                    @dragstart="onDragStart($event, comp)"
                    @click="addComponentToCanvas(comp)"
                  >
                    <bar-chart-outlined />
                    {{ comp.label }}
                  </div>
                </a-collapse-panel>
                <a-collapse-panel key="model" header="模型">
                  <div
                    v-for="comp in componentList.model"
                    :key="comp.id"
                    class="component-item"
                    draggable="true"
                    @dragstart="onDragStart($event, comp)"
                    @click="addComponentToCanvas(comp)"
                  >
                    <ai-icon-outlined />
                    {{ comp.label }}
                  </div>
                </a-collapse-panel>
                <a-collapse-panel key="output" header="数据输出">
                  <div
                    v-for="comp in componentList.output"
                    :key="comp.id"
                    class="component-item"
                    draggable="true"
                    @dragstart="onDragStart($event, comp)"
                    @click="addComponentToCanvas(comp)"
                  >
                    <export-outlined />
                    {{ comp.label }}
                  </div>
                </a-collapse-panel>
              </a-collapse>
            </a-card>
          </a-col>

          <!-- 中间画布 -->
          <a-col :span="14" class="canvas-panel">
            <a-card title="画布" size="small" class="canvas-card">
              <div
                class="dag-canvas"
                ref="canvasRef"
                @drop="onDrop"
                @dragover="onDragOver"
                @click="onCanvasClick"
              >
                <div v-if="dagNodes.length === 0" class="canvas-empty">
                  <inbox-outlined style="font-size: 48px; color: #999" />
                  <p>从左侧拖拽组件到此处构建 DAG 工作流</p>
                </div>
                <div v-else class="nodes-container">
                  <div
                    v-for="node in dagNodes"
                    :key="node.nodeId"
                    class="dag-node"
                    :class="{ selected: selectedNodeId === node.nodeId, [getNodeCategory(node.compId)]: true }"
                    :style="{ left: node.x + 'px', top: node.y + 'px' }"
                    @click.stop="selectNode(node)"
                  >
                    <div class="node-header">
                      <component-icon :compId="node.compId" />
                      <span class="node-label">{{ node.label }}</span>
                      <close-outlined class="node-delete" @click.stop="deleteNode(node.nodeId)" />
                    </div>
                    <div class="node-ports">
                      <div class="port port-input" @click.stop="startEdge($event, node, 'input')">
                        <div class="port-dot"></div>
                        <span>输入</span>
                      </div>
                      <div class="port port-output" @click.stop="startEdge($event, node, 'output')">
                        <span>输出</span>
                        <div class="port-dot"></div>
                      </div>
                    </div>
                  </div>
                  <!-- 边线 SVG 层 -->
                  <svg class="edges-svg" ref="edgesSvgRef">
                    <defs>
                      <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
                        <polygon points="0 0, 10 3.5, 0 7" fill="#1890ff" />
                      </marker>
                    </defs>
                    <path
                      v-for="(edge, idx) in dagEdges"
                      :key="idx"
                      :d="getEdgePath(edge)"
                      stroke="#1890ff"
                      stroke-width="2"
                      fill="none"
                      marker-end="url(#arrowhead)"
                      class="dag-edge"
                      @click.stop="selectEdge(edge)"
                    />
                    <path
                      v-if="tempEdge"
                      :d="getTempEdgePath()"
                      stroke="#1890ff"
                      stroke-width="2"
                      stroke-dasharray="5,5"
                      fill="none"
                    />
                  </svg>
                </div>
              </div>
            </a-card>
          </a-col>

          <!-- 右侧配置面板 -->
          <a-col :span="5" class="config-panel">
            <a-card title="节点配置" size="small" v-if="selectedNode">
              <a-form :model="selectedNodeConfig" layout="vertical">
                <a-form-item label="节点 ID">
                  <a-input v-model:value="selectedNode.nodeId" disabled />
                </a-form-item>
                <a-form-item label="组件类型">
                  <a-input :value="selectedNode.compId" disabled />
                </a-form-item>
                <a-form-item label="显示名称">
                  <a-input v-model:value="selectedNode.label" placeholder="请输入显示名称" />
                </a-form-item>
                <template v-if="getComponentConfig(selectedNode.compId)">
                  <a-divider>组件参数</a-divider>
                  <a-form-item
                    v-for="param in getComponentConfig(selectedNode.compId)"
                    :key="param.name"
                    :label="param.label"
                  >
                    <a-select
                      v-if="param.type === 'select'"
                      v-model:value="selectedNode.attrs[param.name]"
                      :placeholder="param.placeholder || '请选择'"
                    >
                      <a-select-option v-for="opt in param.options" :key="opt.value" :value="opt.value">
                        {{ opt.label }}
                      </a-select-option>
                    </a-select>
                    <a-input
                      v-else
                      v-model:value="selectedNode.attrs[param.name]"
                      :placeholder="param.placeholder || '请输入'"
                    />
                  </a-form-item>
                </template>
              </a-form>
              <a-button type="link" @click="selectedNode = null">取消选择</a-button>
            </a-card>
            <a-card title="节点配置" size="small" v-else>
              <a-empty description="请在画布中选择一个节点" />
            </a-card>

            <a-card title="执行选项" size="small" class="execution-options" style="margin-top: 16px">
              <a-form layout="vertical">
                <a-form-item label="任务名称">
                  <a-input v-model:value="dagForm.name" placeholder="请输入任务名称" />
                </a-form-item>
                <a-form-item label="参与节点">
                  <a-select mode="tags" v-model:value="dagForm.participants" placeholder="输入参与方ID，按回车确认">
                  </a-select>
                </a-form-item>
                <a-form-item label="节点模式">
                  <a-select v-model:value="dagForm.nodeMode">
                    <a-select-option value="RAY">RAY</a-select-option>
                    <a-select-option value="KUSCIA">KUSCIA</a-select-option>
                  </a-select>
                </a-form-item>
              </a-form>
            </a-card>
          </a-col>
        </a-row>
      </a-layout-content>
    </a-layout>

    <!-- 预览对话框 -->
    <a-modal v-model:open="previewVisible" title="DAG 执行预览" width="600px" @ok="previewVisible = false">
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="节点数量">{{ dagNodes.length }}</a-descriptions-item>
        <a-descriptions-item label="边数量">{{ dagEdges.length }}</a-descriptions-item>
        <a-descriptions-item label="参与节点" :span="2">{{ dagForm.participants.join(', ') || '未指定' }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>执行顺序</a-divider>
      <a-tag v-for="(node, idx) in executionPlan" :key="node.nodeId" class="execution-order">
        {{ idx + 1 }}. {{ node.label || node.compId }}
      </a-tag>
      <a-divider>DAG JSON</a-divider>
      <pre class="dag-json-preview">{{ dagDefinitionJson }}</pre>
    </a-modal>

    <!-- 保存对话框 -->
    <a-modal v-model:open="saveDialogVisible" title="保存 DAG" @ok="confirmSave" :confirmLoading="saving">
      <a-form :model="dagForm" layout="vertical">
        <a-form-item label="任务名称" required>
          <a-input v-model:value="dagForm.name" placeholder="请输入任务名称" />
        </a-form-item>
        <a-form-item label="参与节点">
          <a-select mode="tags" v-model:value="dagForm.participants" placeholder="输入参与方ID">
          </a-select>
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="dagForm.description" :rows="3" placeholder="请输入任务描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'
import {
  DatabaseOutlined,
  ShareAltOutlined,
  FilterOutlined,
  BarChartOutlined,
  RobotOutlined,
  ExportOutlined,
  InboxOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'

const canvasRef = ref(null)
const edgesSvgRef = ref(null)

// 组件列表
const componentList = {
  data: [
    { id: 'read_table', label: '读取数据表' },
    { id: 'read_csv', label: '读取CSV文件' }
  ],
  alignment: [
    { id: 'psi', label: 'PSI 求交' },
    { id: 'psi_tp', label: '三方PSI' },
    { id: 'unbalance_psi', label: '不平衡PSI' }
  ],
  filter: [
    { id: 'filter_column', label: '列过滤' },
    { id: 'filter_rows', label: '行过滤' },
    { id: 'filter_null', label: '空值处理' },
    { id: 'filter_duplicate', label: '去重' }
  ],
  preprocessing: [
    { id: 'binning', label: '等频分箱' },
    { id: 'vert_binning', label: '纵向分箱' },
    { id: 'woe_binning', label: 'WOE分箱' },
    { id: 'sample', label: '数据采样' }
  ],
  model: [
    { id: 'ss_glm_train', label: 'SS-GLM训练' },
    { id: 'ss_glm_predict', label: 'SS-GLM预测' },
    { id: 'sgb_train', label: 'SGB训练' },
    { id: 'sgb_predict', label: 'SGB预测' }
  ],
  output: [
    { id: 'write_table', label: '写入数据表' },
    { id: 'write_csv', label: '写入CSV文件' }
  ]
}

// 组件配置参数
const componentConfigs = {
  read_table: [
    { name: 'datasource_id', label: '数据源ID', type: 'input', placeholder: '如: ds-hosp' },
    { name: 'table_name', label: '表名', type: 'input', placeholder: '如: patients' },
    { name: 'columns', label: '列(逗号分隔)', type: 'input', placeholder: '可选，如: id,name,age' }
  ],
  read_csv: [
    { name: 'file_path', label: '文件路径', type: 'input', placeholder: '如: /data/party_a.csv' }
  ],
  psi: [
    { name: 'key_column', label: '关联键列', type: 'input', placeholder: '如: id' },
    { name: 'psi_type', label: 'PSI类型', type: 'select', options: [
      { value: 'ecdh', label: 'ECDH' },
      { value: 'kkrt', label: 'KKRT' },
      { value: 'bc22', label: 'BC22' }
    ]}
  ],
  write_table: [
    { name: 'output_datasource_id', label: '输出数据源ID', type: 'input', placeholder: '如: ds-hosp' },
    { name: 'output_table', label: '输出表名', type: 'input', placeholder: '如: result' }
  ],
  write_csv: [
    { name: 'file_path', label: '输出文件路径', type: 'input', placeholder: '如: /tmp/result.csv' }
  ],
  binning: [
    { name: 'num_bins', label: '分箱数量', type: 'input', placeholder: '如: 10' },
    { name: 'feature_columns', label: '特征列', type: 'input', placeholder: '逗号分隔' }
  ],
  sample: [
    { name: 'sample_type', label: '采样类型', type: 'select', options: [
      { value: 'random', label: '随机采样' },
      { value: 'stratified', label: '分层采样' }
    ]},
    { name: 'sample_rate', label: '采样比例', type: 'input', placeholder: '如: 0.8' }
  ]
}

const activeComponentKey = ref(['data', 'alignment', 'filter', 'preprocessing', 'model', 'output'])

// DAG 数据
const dagNodes = ref([])
const dagEdges = ref([])
const selectedNodeId = ref(null)
const selectedNode = ref(null)
const selectedNodeConfig = reactive({})

// DAG 表单
const dagForm = reactive({
  name: '',
  participants: [],
  nodeMode: 'RAY',
  description: ''
})

// 临时边（拖拽过程中）
const tempEdge = ref(null)
const edgeStartNode = ref(null)
const edgeStartPort = ref(null)

// 对话框状态
const previewVisible = ref(false)
const saveDialogVisible = ref(false)
const saving = ref(false)

// 计算执行顺序（拓扑排序）
const executionPlan = computed(() => {
  const nodes = [...dagNodes.value]
  const edges = [...dagEdges.value]
  const inDegree = {}
  const adjacency = {}

  nodes.forEach(n => {
    inDegree[n.nodeId] = 0
    adjacency[n.nodeId] = []
  })

  edges.forEach(e => {
    if (inDegree[e.to] !== undefined) {
      inDegree[e.to]++
      adjacency[e.from].push(e.to)
    }
  })

  const queue = nodes.filter(n => inDegree[n.nodeId] === 0).map(n => n.nodeId)
  const result = []

  while (queue.length > 0) {
    const current = queue.shift()
    const node = nodes.find(n => n.nodeId === current)
    if (node) result.push(node)
    adjacency[current].forEach(neighbor => {
      inDegree[neighbor]--
      if (inDegree[neighbor] === 0) {
        queue.push(neighbor)
      }
    })
  }

  return result
})

// DAG JSON 定义
const dagDefinitionJson = computed(() => {
  const dagDef = {
    name: dagForm.name || 'DAG Task',
    nodes: dagNodes.value.map(n => ({
      nodeId: n.nodeId,
      compId: n.compId,
      label: n.label,
      x: n.x,
      y: n.y,
      attrs: n.attrs || {}
    })),
    edges: dagEdges.value.map(e => ({
      from: e.from,
      to: e.to
    })),
    description: dagForm.description,
    participants: dagForm.participants
  }
  return JSON.stringify(dagDef, null, 2)
})

// 获取组件配置
function getComponentConfig(compId) {
  return componentConfigs[compId] || null
}

// 获取节点分类
function getNodeCategory(compId) {
  if (['read_table', 'read_csv'].includes(compId)) return 'category-data'
  if (['psi', 'psi_tp', 'unbalance_psi'].includes(compId)) return 'category-alignment'
  if (['filter_column', 'filter_rows', 'filter_null', 'filter_duplicate'].includes(compId)) return 'category-filter'
  if (['binning', 'vert_binning', 'woe_binning', 'sample'].includes(compId)) return 'category-preprocessing'
  if (['ss_glm_train', 'ss_glm_predict', 'sgb_train', 'sgb_predict'].includes(compId)) return 'category-model'
  if (['write_table', 'write_csv'].includes(compId)) return 'category-output'
  return ''
}

// 添加组件到画布
function addComponentToCanvas(comp) {
  const newNode = {
    nodeId: 'node_' + Date.now(),
    compId: comp.id,
    label: comp.label,
    x: 100 + Math.random() * 200,
    y: 100 + Math.random() * 200,
    attrs: {}
  }
  dagNodes.value.push(newNode)
}

// 拖拽开始
function onDragStart(event, comp) {
  event.dataTransfer.setData('component', JSON.stringify(comp))
  event.dataTransfer.effectAllowed = 'copy'
}

// 拖拽经过
function onDragOver(event) {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'copy'
}

// 放置到画布
function onDrop(event) {
  event.preventDefault()
  const data = event.dataTransfer.getData('component')
  if (data) {
    const comp = JSON.parse(data)
    const rect = canvasRef.value.getBoundingClientRect()
    const newNode = {
      nodeId: 'node_' + Date.now(),
      compId: comp.id,
      label: comp.label,
      x: event.clientX - rect.left - 50,
      y: event.clientY - rect.top - 30,
      attrs: {}
    }
    dagNodes.value.push(newNode)
  }
}

// 画布点击
function onCanvasClick() {
  selectedNodeId.value = null
  selectedNode.value = null
}

// 选择节点
function selectNode(node) {
  selectedNodeId.value = node.nodeId
  selectedNode.value = node
  Object.assign(selectedNodeConfig, node.attrs || {})
}

// 删除节点
function deleteNode(nodeId) {
  dagNodes.value = dagNodes.value.filter(n => n.nodeId !== nodeId)
  dagEdges.value = dagEdges.value.filter(e => e.from !== nodeId && e.to !== nodeId)
  if (selectedNodeId.value === nodeId) {
    selectedNodeId.value = null
    selectedNode.value = null
  }
}

// 开始连线
function startEdge(event, node, port) {
  edgeStartNode.value = node
  edgeStartPort.value = port
  tempEdge.value = {
    startX: event.clientX,
    startY: event.clientY,
    endX: event.clientX,
    endY: event.clientY
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 鼠标移动
function onMouseMove(event) {
  if (tempEdge.value) {
    const rect = canvasRef.value.getBoundingClientRect()
    tempEdge.value.endX = event.clientX - rect.left
    tempEdge.value.endY = event.clientY - rect.top
  }
}

// 鼠标释放
function onMouseUp(event) {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)

  // 检查是否在某个节点上释放
  const targetElement = document.elementFromPoint(event.clientX, event.clientY)
  const nodeElement = targetElement?.closest('.dag-node')

  if (nodeElement && edgeStartNode.value && edgeStartPort.value === 'output') {
    const targetNodeId = nodeElement.dataset.nodeId
    if (targetNodeId && targetNodeId !== edgeStartNode.value.nodeId) {
      // 检查边是否已存在
      const exists = dagEdges.value.some(
        e => e.from === edgeStartNode.value.nodeId && e.to === targetNodeId
      )
      if (!exists) {
        dagEdges.value.push({
          from: edgeStartNode.value.nodeId,
          to: targetNodeId
        })
      }
    }
  }

  tempEdge.value = null
  edgeStartNode.value = null
  edgeStartPort.value = null
}

// 获取边路径
function getEdgePath(edge) {
  const fromNode = dagNodes.value.find(n => n.nodeId === edge.from)
  const toNode = dagNodes.value.find(n => n.nodeId === edge.to)
  if (!fromNode || !toNode) return ''

  const fromX = fromNode.x + 100
  const fromY = fromNode.y + 60
  const toX = toNode.x
  const toY = toNode.y + 30

  const midX = (fromX + toX) / 2
  return `M ${fromX} ${fromY} C ${midX} ${fromY}, ${midX} ${toY}, ${toX} ${toY}`
}

// 获取临时边路径
function getTempEdgePath() {
  if (!tempEdge.value) return ''
  const fromX = edgeStartNode.value ? edgeStartNode.value.x + 100 : tempEdge.value.startX
  const fromY = edgeStartNode.value ? edgeStartNode.value.y + 60 : tempEdge.value.startY
  const midX = (fromX + tempEdge.value.endX) / 2
  return `M ${fromX} ${fromY} C ${midX} ${fromY}, ${midX} ${tempEdge.value.endY}, ${tempEdge.value.endX} ${tempEdge.value.endY}`
}

// 选择边
function selectEdge(edge) {
  // 可扩展：边的选中高亮和删除
}

// 清空画布
function handleClear() {
  dagNodes.value = []
  dagEdges.value = []
  selectedNodeId.value = null
  selectedNode.value = null
}

// 预览 DAG
function handlePreview() {
  if (dagNodes.value.length === 0) {
    message.warning('请先添加节点')
    return
  }
  previewVisible.value = true
}

// 保存 DAG
function handleSave() {
  if (!dagForm.name) {
    message.warning('请输入任务名称')
    return
  }
  if (dagNodes.value.length === 0) {
    message.warning('请先添加节点')
    return
  }
  saveDialogVisible.value = true
}

// 确认保存
async function confirmSave() {
  if (!dagForm.name) {
    message.warning('请输入任务名称')
    return
  }
  saving.value = true
  try {
    const dagDefinition = JSON.stringify({
      name: dagForm.name,
      nodes: dagNodes.value.map(n => ({
        nodeId: n.nodeId,
        compId: n.compId,
        label: n.label,
        x: n.x,
        y: n.y,
        attrs: n.attrs || {}
      })),
      edges: dagEdges.value.map(e => ({
        from: e.from,
        to: e.to
      })),
      description: dagForm.description,
      participants: dagForm.participants
    })

    const response = await axios.post('/api/dos/privacy/dag/submit', {
      dagName: dagForm.name,
      dagDefinition: dagDefinition,
      participants: dagForm.participants,
      nodeMode: dagForm.nodeMode,
      description: dagForm.description
    })

    message.success('DAG 任务已创建: ' + response.data.data.taskId)
    saveDialogVisible.value = false
  } catch (error) {
    message.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 组件图标
const ComponentIcon = {
  props: ['compId'],
  render() {
    const iconMap = {
      read_table: h => h(DatabaseOutlined),
      read_csv: h => h(DatabaseOutlined),
      psi: h => h(ShareAltOutlined),
      psi_tp: h => h(ShareAltOutlined),
      unbalance_psi: h => h(ShareAltOutlined),
      filter_column: h => h(FilterOutlined),
      filter_rows: h => h(FilterOutlined),
      filter_null: h => h(FilterOutlined),
      filter_duplicate: h => h(FilterOutlined),
      binning: h => h(BarChartOutlined),
      vert_binning: h => h(BarChartOutlined),
      woe_binning: h => h(BarChartOutlined),
      sample: h => h(BarChartOutlined),
      ss_glm_train: h => h(RobotOutlined),
      ss_glm_predict: h => h(RobotOutlined),
      sgb_train: h => h(RobotOutlined),
      sgb_predict: h => h(RobotOutlined),
      write_table: h => h(ExportOutlined),
      write_csv: h => h(ExportOutlined)
    }
    return iconMap[this.compId] ? iconMap[this.compId]() : h('span')
  }
}
</script>

<style scoped>
.dag-designer-container {
  height: 100vh;
  overflow: hidden;
}
.header {
  background: #001529;
  color: white;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header h1 {
  color: white;
  margin: 0;
  font-size: 20px;
}
.header-actions {
  display: flex;
  gap: 8px;
}
.content {
  padding: 16px;
  height: calc(100vh - 64px);
  overflow: hidden;
}
.component-panel {
  height: 100%;
  overflow-y: auto;
}
.canvas-panel {
  height: 100%;
}
.canvas-card {
  height: 100%;
}
.canvas-card :deep(.ant-card-body) {
  padding: 0;
  height: calc(100% - 57px);
}
.canvas-card .ant-card-head {
  min-height: 40px;
}
.dag-canvas {
  width: 100%;
  height: 100%;
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
  min-width: 800px;
  min-height: 600px;
}
.edges-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
.dag-edge {
  pointer-events: stroke;
  cursor: pointer;
}
.dag-edge:hover {
  stroke-width: 3;
}
.dag-node {
  position: absolute;
  width: 120px;
  min-height: 80px;
  background: white;
  border: 2px solid #d9d9d9;
  border-radius: 4px;
  cursor: move;
  user-select: none;
  transition: box-shadow 0.2s, border-color 0.2s;
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
  padding: 8px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.node-label {
  flex: 1;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.node-delete {
  font-size: 12px;
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
  font-size: 11px;
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
.component-item {
  padding: 8px 12px;
  margin: 4px 0;
  border-radius: 4px;
  cursor: grab;
  transition: background 0.2s;
}
.component-item:hover {
  background: #f0f0f0;
}
.config-panel {
  height: 100%;
  overflow-y: auto;
}
.execution-order {
  margin: 4px;
}
.dag-json-preview {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  max-height: 200px;
  overflow: auto;
  font-size: 12px;
}
</style>
