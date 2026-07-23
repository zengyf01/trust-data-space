<template>
  <div>
    <a-card title="数字合约列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="合约编号">
          <a-input v-model:value="searchForm.contractCode" placeholder="请输入合约编号" />
        </a-form-item>
        <a-form-item label="合约状态">
          <a-select v-model:value="searchForm.contractStatus" placeholder="请选择状态" allowClear style="width: 120px">
            <a-select-option :value="1">待签</a-select-option>
            <a-select-option :value="2">签署中</a-select-option>
            <a-select-option :value="3">执行中</a-select-option>
            <a-select-option :value="4">已拒绝</a-select-option>
            <a-select-option :value="5">已终止</a-select-option>
          </a-select>
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
        :data-source="contractList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'contractStatus'">
            <a-tag :color="getStatusColor(record.contractStatus)">
              {{ getStatusText(record.contractStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button
                v-if="record.contractStatus === 1"
                type="link"
                size="small"
                @click="handleSign(record, 'provider')"
              >
                供方签名
              </a-button>
              <a-button
                v-if="record.contractStatus === 2"
                type="link"
                size="small"
                @click="handleSign(record, 'consumer')"
              >
                需方签名
              </a-button>
              <a-button
                v-if="record.contractStatus === 1 || record.contractStatus === 2"
                type="link"
                size="small"
                danger
                @click="handleReject(record)"
              >
                拒绝
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 合约详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="合约详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentContract">
        <a-descriptions-item label="合约编号">{{ currentContract.contractCode }}</a-descriptions-item>
        <a-descriptions-item label="订单编号">{{ currentContract.orderCode }}</a-descriptions-item>
        <a-descriptions-item label="合约状态">
          <a-tag :color="getStatusColor(currentContract.contractStatus)">
            {{ getStatusText(currentContract.contractStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="合约类型">{{ currentContract.contractType }}</a-descriptions-item>
        <a-descriptions-item label="开始时间">{{ currentContract.contractStartTime }}</a-descriptions-item>
        <a-descriptions-item label="结束时间">{{ currentContract.contractEndTime }}</a-descriptions-item>
        <a-descriptions-item label="供应方">{{ currentContract.providerInstitutionName }}</a-descriptions-item>
        <a-descriptions-item label="使用方">{{ currentContract.useInstitutionName }}</a-descriptions-item>
        <a-descriptions-item label="供方签名" :span="2">
          <span v-if="currentContract.providerSignature">{{ currentContract.providerSignature.substring(0, 20) }}...</span>
          <span v-else style="color: #999">未签名</span>
        </a-descriptions-item>
        <a-descriptions-item label="需方签名" :span="2">
          <span v-if="currentContract.useSignature">{{ currentContract.useSignature.substring(0, 20) }}...</span>
          <span v-else style="color: #999">未签名</span>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 签名弹窗 -->
    <a-modal
      v-model:open="signVisible"
      :title="signType === 'provider' ? '供方签名' : '需方签名'"
      @ok="handleSignConfirm"
      :confirmLoading="signLoading"
    >
      <a-input
        v-model:value="signature"
        placeholder="请输入SM2签名"
        :rows="4"
        type="textarea"
      />
      <p style="margin-top: 8px; color: #999; font-size: 12px">
        提示：实际生产环境中，签名由SM2私钥生成
      </p>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import contractApi from '@/api/contract'

const columns = [
  { title: '合约编号', dataIndex: 'contractCode', key: 'contractCode', width: 150 },
  { title: '订单编号', dataIndex: 'orderCode', key: 'orderCode', width: 150 },
  { title: '供应方', dataIndex: 'providerInstitutionName', key: 'providerInstitutionName' },
  { title: '使用方', dataIndex: 'useInstitutionName', key: 'useInstitutionName' },
  { title: '状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 250 }
]

const contractList = ref([])
const loading = ref(false)
const searchForm = reactive({
  contractCode: '',
  contractStatus: null
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentContract = ref(null)

const signVisible = ref(false)
const signType = ref('provider')
const signLoading = ref(false)
const signature = ref('')
const currentSignContract = ref(null)

const statusMap = {
  1: { text: '待签', color: 'orange' },
  2: { text: '签署中', color: 'blue' },
  3: { text: '执行中', color: 'green' },
  4: { text: '已拒绝', color: 'red' },
  5: { text: '已终止', color: 'gray' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusColor = (status) => statusMap[status]?.color || 'default'

const fetchContracts = async () => {
  loading.value = true
  try {
    const params = {
      pageNumber: pagination.current,
      pageSize: pagination.pageSize,
      contractCode: searchForm.contractCode || undefined,
      contractStatus: searchForm.contractStatus || undefined
    }
    const res = await contractApi.getContractPage(params)
    if (res.code === 200) {
      contractList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取合约列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchContracts()
}

const handleReset = () => {
  searchForm.contractCode = ''
  searchForm.contractStatus = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchContracts()
}

const handleView = (record) => {
  currentContract.value = record
  detailVisible.value = true
}

const handleSign = (record, type) => {
  currentSignContract.value = record
  signType.value = type
  signature.value = ''
  signVisible.value = true
}

const handleSignConfirm = async () => {
  if (!signature.value) {
    message.warning('请输入签名')
    return
  }
  signLoading.value = true
  try {
    const apiMethod = signType.value === 'provider' ? contractApi.providerSign : contractApi.consumerSign
    const res = await apiMethod(currentSignContract.value.id, signature.value)
    if (res.code === 200) {
      message.success('签名成功')
      signVisible.value = false
      fetchContracts()
    } else {
      message.error(res.msg || '签名失败')
    }
  } catch (error) {
    message.error('签名失败')
  } finally {
    signLoading.value = false
  }
}

const handleReject = async (record) => {
  try {
    const res = await contractApi.rejectContract(record.id, '用户拒绝')
    if (res.code === 200) {
      message.success('已拒绝')
      fetchContracts()
    }
  } catch (error) {
    message.error('操作失败')
  }
}

onMounted(() => {
  fetchContracts()
})
</script>