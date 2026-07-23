<template>
  <div>
    <a-card title="创建隐私计算任务">
      <a-tabs v-model:activeKey="createType" tab-position="left" style="min-height: 500px">
        <!-- PSI 求交 -->
        <a-tab-pane key="psi" tab="PSI 求交">
          <a-form :model="psiForm" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="任务名称">
                  <a-input v-model:value="psiForm.taskName" placeholder="请输入任务名称" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="关联键列">
                  <a-input v-model:value="psiForm.keyColumn" placeholder="如: id, user_id" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="A方数据路径">
                  <a-input v-model:value="psiForm.partyADataPath" placeholder="/data/party_a.csv" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="B方数据路径">
                  <a-input v-model:value="psiForm.partyBDataPath" placeholder="/data/party_b.csv" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item label="协议类型">
                  <a-select v-model:value="psiForm.protocol" placeholder="选择协议">
                    <a-select-option value="ECPSI">ECPSI</a-select-option>
                    <a-select-option value="RR22PSI">RR22PSI</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="结果类型">
                  <a-select v-model:value="psiForm.resultType" placeholder="选择结果">
                    <a-select-option value="INTERSECTION">交集</a-select-option>
                    <a-select-option value="UNION">并集</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="节点模式">
                  <a-select v-model:value="psiForm.nodeMode">
                    <a-select-option value="RAY">RAY</a-select-option>
                    <a-select-option value="KUSCIA">KUSCIA</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-tab-pane>

        <!-- MPC 多方计算 -->
        <a-tab-pane key="mpc" tab="MPC 多方计算">
          <a-form :model="mpcForm" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="任务名称">
                  <a-input v-model:value="mpcForm.taskName" placeholder="请输入任务名称" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="算法名称">
                  <a-select v-model:value="mpcForm.algorithm" placeholder="选择算法">
                    <a-select-option value="SecureSum">安全求和</a-select-option>
                    <a-select-option value="SecureComparison">安全比较</a-select-option>
                    <a-select-option value="GarbledCircuit">混淆电路</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item label="参与方列表">
              <a-select mode="tags" v-model:value="mpcForm.participants" placeholder="输入参与方ID，按回车确认">
              </a-select>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- 联邦学习 -->
        <a-tab-pane key="fl" tab="联邦学习">
          <a-form :model="flForm" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="任务名称">
                  <a-input v-model:value="flForm.taskName" placeholder="请输入任务名称" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="模型类型">
                  <a-select v-model:value="flForm.modelType" placeholder="选择模型">
                    <a-select-option value="LR">逻辑回归 (LR)</a-select-option>
                    <a-select-option value="NN">神经网络 (NN)</a-select-option>
                    <a-select-option value="XGB">梯度提升 (XGB)</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="标签列">
                  <a-input v-model:value="flForm.labelColumn" placeholder="如: label, target" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="特征列">
                  <a-select mode="tags" v-model:value="flForm.featureColumns" placeholder="输入特征列，按回车确认">
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :span="8">
                <a-form-item label="参与方">
                  <a-select mode="tags" v-model:value="flForm.participants" placeholder="输入参与方ID">
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="轮次 (Epochs)">
                  <a-input-number v-model:value="flForm.epochs" :min="1" :max="100" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="批量大小">
                  <a-input-number v-model:value="flForm.batchSize" :min="1" :max="1024" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-tab-pane>

        <!-- 纵向联邦学习 -->
        <a-tab-pane key="vfl" tab="纵向联邦学习">
          <a-form :model="vflForm" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="任务名称">
                  <a-input v-model:value="vflForm.taskName" placeholder="请输入任务名称" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="标签列">
                  <a-input v-model:value="vflForm.labelColumn" placeholder="如: label" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item label="参与方特征映射">
              <a-textarea v-model:value="vflForm.featureMapJson" :rows="4" placeholder='{"party1": ["feat1", "feat2"], "party2": ["feat3"]}' />
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- DAG 工作流 -->
        <a-tab-pane key="dag" tab="DAG 工作流">
          <a-row :gutter="16">
            <!-- 左侧组件面板 -->
            <a-col :span="5">
              <a-card title="组件库" size="small">
                <a-collapse v-model:activeKey="activeComponentKey">
                  <a-collapse-panel key="data" header="数据输入" :style="{ padding: '0' }">
                    <div v-for="comp in componentList.data" :key="comp.id" class="component-item" @click="addComponentToCanvas(comp)">
                      <database-outlined /> {{ comp.label }}
                    </div>
                  </a-collapse-panel>
                  <a-collapse-panel key="alignment" header="数据对齐">
                    <div v-for="comp in componentList.alignment" :key="comp.id" class="component-item" @click="addComponentToCanvas(comp)">
                      <share-alt-outlined /> {{ comp.label }}
                    </div>
                  </a-collapse-panel>
                  <a-collapse-panel key="filter" header="数据过滤">
                    <div v-for="comp in componentList.filter" :key="comp.id" class="component-item" @click="addComponentToCanvas(comp)">
                      <filter-outlined /> {{ comp.label }}
                    </div>
                  </a-collapse-panel>
                  <a-collapse-panel key="preprocessing" header="预处理">
                    <div v-for="comp in componentList.preprocessing" :key="comp.id" class="component-item" @click="addComponentToCanvas(comp)">
                      <bar-chart-outlined /> {{ comp.label }}
                    </div>
                  </a-collapse-panel>
                  <a-collapse-panel key="model" header="模型">
                    <div v-for="comp in componentList.model" :key="comp.id" class="component-item" @click="addComponentToCanvas(comp)">
                      <ai-icon-outlined /> {{ comp.label }}
                    </div>
                  </a-collapse-panel>
                  <a-collapse-panel key="output" header="数据输出">
                    <div v-for="comp in componentList.output" :key="comp.id" class="component-item" @click="addComponentToCanvas(comp)">
                      <export-outlined /> {{ comp.label }}
                    </div>
                  </a-collapse-panel>
                </a-collapse>
              </a-card>
            </a-col>

            <!-- 中间画布 -->
            <a-col :span="12">
              <a-card title="画布" size="small" class="canvas-card">
                <div class="dag-canvas" ref="dagCanvasRef" @click="selectedDagNode = null">
                  <div v-if="dagNodes.length === 0" class="canvas-empty">
                    <inbox-outlined style="font-size: 48px; color: #999" />
                    <p>点击左侧组件添加到画布</p>
                  </div>
                  <div v-else class="nodes-container">
                    <div
                      v-for="node in dagNodes"
                      :key="node.nodeId"
                      class="dag-node"
                      :class="{ selected: selectedDagNode?.nodeId === node.nodeId, [getNodeCategory(node.compId)]: true }"
                      :style="{ left: node.x + 'px', top: node.y + 'px' }"
                      @click.stop="selectDagNode(node)"
                    >
                      <div class="node-header">
                        <span class="node-label">{{ node.label }}</span>
                        <close-outlined class="node-delete" @click.stop="deleteDagNode(node.nodeId)" />
                      </div>
                      <div class="node-ports">
                        <div class="port port-output" @click.stop="startEdge(node, 'output')">
                          <span>输出</span><div class="port-dot"></div>
                        </div>
                        <div class="port port-input" @click.stop="startEdge(node, 'input')">
                          <div class="port-dot"></div><span>输入</span>
                        </div>
                      </div>
                    </div>
                    <svg class="edges-svg">
                      <defs><marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto"><polygon points="0 0, 10 3.5, 0 7" fill="#1890ff" /></marker></defs>
                      <path v-for="(edge, idx) in dagEdges" :key="idx" :d="getEdgePath(edge)" stroke="#1890ff" stroke-width="2" fill="none" marker-end="url(#arrowhead)" />
                      <path v-if="tempEdge" :d="getTempEdgePath()" stroke="#1890ff" stroke-width="2" stroke-dasharray="5,5" fill="none" />
                    </svg>
                  </div>
                </div>
                <div class="canvas-toolbar">
                  <a-button size="small" @click="clearDagCanvas">清空</a-button>
                  <a-button size="small" @click="previewDag">预览</a-button>
                </div>
              </a-card>
            </a-col>

            <!-- 右侧配置面板 -->
            <a-col :span="7">
              <a-card title="节点配置" size="small" v-if="selectedDagNode">
                <a-form :model="selectedDagNode" layout="vertical" size="small">
                  <a-form-item label="组件ID">
                    <a-input :value="selectedDagNode.compId" disabled />
                  </a-form-item>
                  <a-form-item label="显示名称">
                    <a-input v-model:value="selectedDagNode.label" />
                  </a-form-item>
                  <template v-if="getComponentConfig(selectedDagNode.compId)">
                    <a-divider>参数配置</a-divider>
                    <a-form-item v-for="param in getComponentConfig(selectedDagNode.compId)" :key="param.name" :label="param.label">
                      <a-select v-if="param.type === 'select'" v-model:value="selectedDagNode.attrs[param.name]" :placeholder="param.placeholder">
                        <a-select-option v-for="opt in param.options" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                      </a-select>
                      <a-input v-else v-model:value="selectedDagNode.attrs[param.name]" :placeholder="param.placeholder" />
                    </a-form-item>
                  </template>
                </a-form>
              </a-card>
              <a-card title="节点配置" size="small" v-else>
                <a-empty description="请选择节点" />
              </a-card>
              <a-card title="执行选项" size="small" style="margin-top: 16px">
                <a-form layout="vertical" size="small">
                  <a-form-item label="任务名称">
                    <a-input v-model:value="dagForm.name" placeholder="请输入任务名称" />
                  </a-form-item>
                  <a-form-item label="参与节点">
                    <a-select mode="tags" v-model:value="dagForm.participants" placeholder="输入参与方ID" />
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
        </a-tab-pane>
      </a-tabs>
      <div class="create-actions">
        <a-space>
          <a-button type="primary" @click="handleCreateTask" :loading="createLoading">创建</a-button>
          <a-button @click="handleCancel">取消</a-button>
        </a-space>
      </div>
    </a-card>

    <!-- 预览对话框 -->
    <a-modal v-model:open="previewVisible" title="DAG 执行预览" width="600px" @ok="previewVisible = false">
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="节点数量">{{ dagNodes.length }}</a-descriptions-item>
        <a-descriptions-item label="边数量">{{ dagEdges.length }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>执行顺序</a-divider>
      <a-tag v-for="(node, idx) in executionPlan" :key="node.nodeId" style="margin: 4px">{{ idx + 1 }}. {{ node.label || node.compId }}</a-tag>
    </a-modal>

    <!-- 保存对话框 -->
    <a-modal v-model:open="showDagSaveModal" title="保存 DAG" @ok="confirmSaveDag" :confirmLoading="dagSaving">
      <a-form :model="dagForm" layout="vertical">
        <a-form-item label="任务名称" required>
          <a-input v-model:value="dagForm.name" placeholder="请输入任务名称" />
        </a-form-item>
        <a-form-item label="参与节点">
          <a-select mode="tags" v-model:value="dagForm.participants" placeholder="输入参与方ID" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="dagForm.description" :rows="3" placeholder="任务描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
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

const router = useRouter()

const createType = ref('psi')
const createLoading = ref(false)
const psiLoading = ref(false)
const mpcLoading = ref(false)
const flLoading = ref(false)
const vflLoading = ref(false)

const psiResult = ref(null)
const mpcResult = ref(null)
const flResult = ref(null)

const handleCancel = () => {
  router.push('/privacy')
}

const handleCreateTask = () => {
  createLoading.value = true
  try {
    if (createType.value === 'psi') {
      handleExecutePsi()
    } else if (createType.value === 'mpc') {
      handleExecuteMpc()
    } else if (createType.value === 'fl') {
      handleExecuteFl()
    } else if (createType.value === 'vfl') {
      handleExecuteVfl()
    } else if (createType.value === 'dag') {
      showDagSaveModal.value = true
      return
    }
    router.push('/privacy')
  } finally {
    createLoading.value = false
  }
}

const handleExecutePsi = async () => {
  if (!psiForm.taskName || !psiForm.partyADataPath || !psiForm.partyBDataPath || !psiForm.keyColumn) {
    message.warning('请填写完整信息')
    return
  }
  psiLoading.value = true
  try {
    const response = await axios.post('/api/dos/privacy/psi/execute', {
      taskName: psiForm.taskName,
      partyADataPath: psiForm.partyADataPath,
      partyBDataPath: psiForm.partyBDataPath,
      keyColumn: psiForm.keyColumn,
      protocol: psiForm.protocol,
      resultType: psiForm.resultType,
      nodeMode: psiForm.nodeMode
    })
    message.success('PSI任务已创建: ' + response.data.data.taskId)
    psiResult.value = response.data.data
  } catch (error) {
    message.error('执行失败: ' + (error.message || '未知错误'))
  } finally {
    psiLoading.value = false
  }
}

const handleExecuteMpc = async () => {
  if (!mpcForm.taskName || !mpcForm.algorithm || mpcForm.participants.length === 0) {
    message.warning('请填写完整信息')
    return
  }
  mpcLoading.value = true
  try {
    const response = await axios.post('/api/dos/privacy/mpc/execute', {
      taskName: mpcForm.taskName,
      algorithm: mpcForm.algorithm,
      participants: mpcForm.participants
    })
    message.success('MPC任务已创建: ' + response.data.data.taskId)
    mpcResult.value = response.data.data
  } catch (error) {
    message.error('执行失败: ' + (error.message || '未知错误'))
  } finally {
    mpcLoading.value = false
  }
}

const handleExecuteFl = async () => {
  if (!flForm.taskName || !flForm.labelColumn || flForm.participants.length === 0) {
    message.warning('请填写完整信息')
    return
  }
  flLoading.value = true
  try {
    const response = await axios.post('/api/dos/privacy/fl/execute', {
      taskName: flForm.taskName,
      labelColumn: flForm.labelColumn,
      featureColumns: flForm.featureColumns,
      participants: flForm.participants,
      modelType: flForm.modelType,
      epochs: flForm.epochs,
      batchSize: flForm.batchSize
    })
    message.success('联邦学习任务已创建: ' + response.data.data.taskId)
    flResult.value = response.data.data
  } catch (error) {
    message.error('执行失败: ' + (error.message || '未知错误'))
  } finally {
    flLoading.value = false
  }
}

const handleExecuteVfl = async () => {
  if (!vflForm.taskName || !vflForm.labelColumn) {
    message.warning('请填写完整信息')
    return
  }
  vflLoading.value = true
  try {
    const featureMap = JSON.parse(vflForm.featureMapJson || '{}')
    const response = await axios.post('/api/dos/privacy/vfl/execute', {
      taskName: vflForm.taskName,
      labelColumn: vflForm.labelColumn,
      featureColumns: featureMap
    })
    message.success('纵向联邦学习任务已创建: ' + response.data.data.taskId)
  } catch (error) {
    message.error('执行失败: ' + (error.message || '未知错误'))
  } finally {
    vflLoading.value = false
  }
}

// ========== DAG 相关 ==========

const dagCanvasRef = ref(null)

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
    { name: 'columns', label: '列(逗号分隔)', type: 'input', placeholder: '可选' }
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

const dagNodes = ref([])
const dagEdges = ref([])
const selectedDagNode = ref(null)
const dagForm = reactive({
  name: '',
  participants: [],
  nodeMode: 'RAY',
  description: ''
})
const tempEdge = ref(null)
const edgeStartNode = ref(null)
const edgeStartPort = ref(null)
const previewVisible = ref(false)
const showDagSaveModal = ref(false)
const dagSaving = ref(false)

function getComponentConfig(compId) {
  return componentConfigs[compId] || null
}

function getNodeCategory(compId) {
  if (['read_table', 'read_csv'].includes(compId)) return 'category-data'
  if (['psi', 'psi_tp', 'unbalance_psi'].includes(compId)) return 'category-alignment'
  if (['filter_column', 'filter_rows', 'filter_null', 'filter_duplicate'].includes(compId)) return 'category-filter'
  if (['binning', 'vert_binning', 'woe_binning', 'sample'].includes(compId)) return 'category-preprocessing'
  if (['ss_glm_train', 'ss_glm_predict', 'sgb_train', 'sgb_predict'].includes(compId)) return 'category-model'
  if (['write_table', 'write_csv'].includes(compId)) return 'category-output'
  return ''
}

function addComponentToCanvas(comp) {
  const newNode = {
    nodeId: 'node_' + Date.now(),
    compId: comp.id,
    label: comp.label,
    x: 50 + Math.random() * 100,
    y: 50 + Math.random() * 100,
    attrs: {}
  }
  dagNodes.value.push(newNode)
}

function selectDagNode(node) {
  selectedDagNode.value = node
}

function deleteDagNode(nodeId) {
  dagNodes.value = dagNodes.value.filter(n => n.nodeId !== nodeId)
  dagEdges.value = dagEdges.value.filter(e => e.from !== nodeId && e.to !== nodeId)
  if (selectedDagNode.value?.nodeId === nodeId) {
    selectedDagNode.value = null
  }
}

function startEdge(node, port) {
  if (!edgeStartNode.value) {
    edgeStartNode.value = node
    edgeStartPort.value = port
    tempEdge.value = { startX: node.x, startY: node.y }
    document.addEventListener('mouseup', onMouseUp)
  } else {
    if (edgeStartNode.value.nodeId !== node.nodeId && edgeStartPort.value === 'output') {
      const exists = dagEdges.value.some(e => e.from === edgeStartNode.value.nodeId && e.to === node.nodeId)
      if (!exists) {
        dagEdges.value.push({ from: edgeStartNode.value.nodeId, to: node.nodeId })
      }
    }
    edgeStartNode.value = null
    edgeStartPort.value = null
    tempEdge.value = null
    document.removeEventListener('mouseup', onMouseUp)
  }
}

function onMouseUp(event) {
  document.removeEventListener('mouseup', onMouseUp)
  edgeStartNode.value = null
  edgeStartPort.value = null
  tempEdge.value = null
}

function getEdgePath(edge) {
  const fromNode = dagNodes.value.find(n => n.nodeId === edge.from)
  const toNode = dagNodes.value.find(n => n.nodeId === edge.to)
  if (!fromNode || !toNode) return ''
  const fromX = fromNode.x + 100
  const fromY = fromNode.y + 50
  const toX = toNode.x
  const toY = toNode.y + 25
  const midX = (fromX + toX) / 2
  return `M ${fromX} ${fromY} C ${midX} ${fromY}, ${midX} ${toY}, ${toX} ${toY}`
}

function getTempEdgePath() {
  if (!tempEdge.value || !edgeStartNode.value) return ''
  return ''
}

const executionPlan = computed(() => {
  const nodes = [...dagNodes.value]
  const edges = [...dagEdges.value]
  const inDegree = {}
  const adjacency = {}
  nodes.forEach(n => { inDegree[n.nodeId] = 0; adjacency[n.nodeId] = [] })
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
      if (inDegree[neighbor] === 0) queue.push(neighbor)
    })
  }
  return result
})

function clearDagCanvas() {
  dagNodes.value = []
  dagEdges.value = []
  selectedDagNode.value = null
}

function previewDag() {
  if (dagNodes.value.length === 0) {
    message.warning('请先添加节点')
    return
  }
  previewVisible.value = true
}

async function confirmSaveDag() {
  if (!dagForm.name) {
    message.warning('请输入任务名称')
    return
  }
  dagSaving.value = true
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
      edges: dagEdges.value.map(e => ({ from: e.from, to: e.to })),
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
    showDagSaveModal.value = false
    router.push('/privacy')
  } catch (error) {
    message.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    dagSaving.value = false
  }
}

// ========== 表单数据 ==========

const psiForm = reactive({
  taskName: '',
  partyADataPath: '',
  partyBDataPath: '',
  keyColumn: '',
  protocol: 'ECPSI',
  resultType: 'INTERSECTION',
  nodeMode: 'RAY'
})

const mpcForm = reactive({
  taskName: '',
  algorithm: '',
  participants: []
})

const flForm = reactive({
  taskName: '',
  modelType: 'LR',
  labelColumn: '',
  featureColumns: [],
  participants: [],
  epochs: 10,
  batchSize: 32
})

const vflForm = reactive({
  taskName: '',
  labelColumn: '',
  featureMapJson: ''
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
  text-over: ellipsis;
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
.create-actions {
  margin-top: 16px;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}
</style>
