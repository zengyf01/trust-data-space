<template>
  <div>
    <a-card title="存证列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="存证类型">
          <a-select v-model:value="searchForm.evidenceType" placeholder="请选择类型" allowClear style="width: 140px">
            <a-select-option value="CONTRACT_SIGN">合约签署</a-select-option>
            <a-select-option value="DATA_CONSUME">数据消费</a-select-option>
            <a-select-option value="POLICY_EXEC">策略执行</a-select-option>
            <a-select-option value="API_INVOKE">API调用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="合约编号">
          <a-input v-model:value="searchForm.contractId" placeholder="请输入合约编号" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card>
      <a-table
        :columns="columns"
        :data-source="evidenceList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'evidenceType'">
            <a-tag>{{ getTypeText(record.evidenceType) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleVerify(record)">验证</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="存证详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentEvidence">
        <a-descriptions-item label="存证编号">{{ currentEvidence.evidenceNo }}</a-descriptions-item>
        <a-descriptions-item label="存证类型">
          <a-tag>{{ getTypeText(currentEvidence.evidenceType) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="关联合约" :span="2">{{ currentEvidence.contractId }}</a-descriptions-item>
        <a-descriptions-item label="交易Hash" :span="2">
          <span style="word-break: break-all">{{ currentEvidence.txHash }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="区块高度">{{ currentEvidence.blockHeight }}</a-descriptions-item>
        <a-descriptions-item label="存证时间">{{ currentEvidence.evidenceTime }}</a-descriptions-item>
        <a-descriptions-item label="存证内容" :span="2">
          <pre style="margin: 0; white-space: pre-wrap">{{ currentEvidence.evidenceContent }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 验证结果弹窗 -->
    <a-modal
      v-model:open="verifyVisible"
      title="存证验证"
      :footer="null"
    >
      <a-result
        v-if="verifyResult !== null"
        :status="verifyResult ? 'success' : 'error'"
        :title="verifyResult ? '验证通过' : '验证失败'"
        :sub-title="verifyResult ? '该存证在区块链上存在，数据未被篡改' : '该存证在区块链上不存在或数据已被篡改'"
      />
      <p v-else style="text-align: center">正在验证...</p>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import evidenceApi from '@/api/evidence'

const columns = [
  { title: '存证编号', dataIndex: 'evidenceNo', key: 'evidenceNo', width: 200 },
  { title: '存证类型', dataIndex: 'evidenceType', key: 'evidenceType', width: 120 },
  { title: '关联合约', dataIndex: 'contractId', key: 'contractId', width: 150 },
  { title: '交易Hash', dataIndex: 'txHash', key: 'txHash', ellipsis: true },
  { title: '存证时间', dataIndex: 'evidenceTime', key: 'evidenceTime', width: 180 },
  { title: '操作', key: 'action', width: 150 }
]

const evidenceList = ref([])
const loading = ref(false)
const searchForm = reactive({
  evidenceType: null,
  contractId: ''
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentEvidence = ref(null)

const verifyVisible = ref(false)
const verifyResult = ref(null)

const typeMap = {
  CONTRACT_SIGN: '合约签署',
  DATA_CONSUME: '数据消费',
  POLICY_EXEC: '策略执行',
  API_INVOKE: 'API调用'
}

const getTypeText = (type) => typeMap[type] || type

const fetchEvidences = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: pagination.current,
      pageSize: pagination.pageSize,
      evidenceType: searchForm.evidenceType || undefined,
      contractId: searchForm.contractId || undefined
    }
    const res = await evidenceApi.getEvidencePage(params)
    if (res.code === 200) {
      evidenceList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取存证列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchEvidences()
}

const handleReset = () => {
  searchForm.evidenceType = null
  searchForm.contractId = ''
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchEvidences()
}

const handleView = (record) => {
  currentEvidence.value = record
  detailVisible.value = true
}

const handleVerify = async (record) => {
  verifyVisible.value = true
  verifyResult.value = null
  try {
    const res = await evidenceApi.verifyEvidence(record.txHash)
    verifyResult.value = res.code === 200 && res.data === true
  } catch (error) {
    verifyResult.value = false
  }
}

onMounted(() => {
  fetchEvidences()
})
</script>

<style scoped>
pre {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
}
</style>
