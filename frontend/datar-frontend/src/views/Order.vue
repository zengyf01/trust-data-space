<template>
  <a-card title="交易订单">
    <template #extra>
      <a-button type="primary" @click="showCreateModal">创建订单</a-button>
    </template>

    <a-form layout="inline" style="margin-bottom: 16px">
      <a-form-item label="订单编号">
        <a-input v-model:value="searchCode" placeholder="订单编号" style="width: 200px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="searchStatus" placeholder="选择状态" style="width: 120px" allow-clear>
          <a-select-option :value="1">待审核</a-select-option>
          <a-select-option :value="2">已通过</a-select-option>
          <a-select-option :value="3">签署中</a-select-option>
          <a-select-option :value="4">执行中</a-select-option>
          <a-select-option :value="5">已完成</a-select-option>
          <a-select-option :value="6">已拒绝</a-select-option>
          <a-select-option :value="7">已取消</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
      </a-form-item>
    </a-form>

    <a-table :columns="columns" :data-source="data" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'fStatus'">
          <a-tag :color="getStatusColor(record.fStatus)">
            {{ getStatusName(record.fStatus) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click.stop="handleView(record)">详情</a-button>
            <a-button size="small" @click.stop="handleDelete(record)" v-if="record.fStatus === 1 || record.fStatus === 6 || record.fStatus === 7">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </a-card>

  <a-modal v-model:open="modalVisible" title="创建订单" @ok="handleSubmit" @cancel="handleCancel" :confirmLoading="submitLoading">
    <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="订单编码" name="fOrderCode">
        <a-input v-model:value="formState.fOrderCode" placeholder="请输入订单编码" />
      </a-form-item>
      <a-form-item label="产品ID" name="fProductId">
        <a-input v-model:value="formState.fProductId" placeholder="请输入产品ID" />
      </a-form-item>
      <a-form-item label="买方租户ID" name="fBuyerTenantId">
        <a-input v-model:value="formState.fBuyerTenantId" placeholder="请输入买方租户ID" />
      </a-form-item>
      <a-form-item label="卖方租户ID" name="fSellerTenantId">
        <a-input v-model:value="formState.fSellerTenantId" placeholder="请输入卖方租户ID" />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal v-model:open="detailVisible" title="订单详情" :footer="null">
    <a-descriptions :column="2" bordered>
      <a-descriptions-item label="订单编号">{{ currentRecord?.fOrderCode }}</a-descriptions-item>
      <a-descriptions-item label="状态">
        <a-tag :color="getStatusColor(currentRecord?.fStatus)">{{ getStatusName(currentRecord?.fStatus) }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="产品ID">{{ currentRecord?.fProductId }}</a-descriptions-item>
      <a-descriptions-item label="合约ID">{{ currentRecord?.fContractId || '-' }}</a-descriptions-item>
      <a-descriptions-item label="买方">{{ currentRecord?.fBuyerTenantId }}</a-descriptions-item>
      <a-descriptions-item label="卖方">{{ currentRecord?.fSellerTenantId }}</a-descriptions-item>
    </a-descriptions>
  </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import orderApi from '@/api/order'

const columns = [
  { title: '订单编号', dataIndex: 'fOrderCode', key: 'fOrderCode' },
  { title: '产品ID', dataIndex: 'fProductId', key: 'fProductId' },
  { title: '买方', dataIndex: 'fBuyerTenantId', key: 'fBuyerTenantId' },
  { title: '卖方', dataIndex: 'fSellerTenantId', key: 'fSellerTenantId' },
  { title: '状态', dataIndex: 'fStatus', key: 'fStatus', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const loading = ref(false)
const data = ref([])
const pagination = reactive({ current: 1, pageSize: 20, total: 0 })
const searchCode = ref('')
const searchStatus = ref(null)
const modalVisible = ref(false)
const detailVisible = ref(false)
const submitLoading = ref(false)
const currentRecord = ref(null)

const formState = reactive({
  fOrderCode: '',
  fProductId: '',
  fBuyerTenantId: '',
  fSellerTenantId: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await orderApi.getPage({
      currentPage: pagination.current,
      pageSize: pagination.pageSize,
      orderCode: searchCode.value || undefined,
      status: searchStatus.value || undefined
    })
    if (res.code === 200) {
      data.value = res.data.list || []
      pagination.total = res.data.pagination?.total || 0
    }
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchCode.value = ''; searchStatus.value = null; pagination.current = 1; loadData() }
const handleTableChange = (pag) => { pagination.current = pag.current; pagination.pageSize = pag.pageSize; loadData() }

const showCreateModal = () => {
  Object.assign(formState, { fOrderCode: '', fProductId: '', fBuyerTenantId: '', fSellerTenantId: '' })
  modalVisible.value = true
}

const handleView = (record) => {
  currentRecord.value = record
  detailVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    const res = await orderApi.create(formState)
    if (res.code === 200) { message.success('创建成功'); modalVisible.value = false; loadData() }
    else message.error(res.msg || '创建失败')
  } catch (error) { message.error('操作失败') } finally { submitLoading.value = false }
}

const handleCancel = () => { modalVisible.value = false }

const handleDelete = async (record) => {
  try {
    const res = await orderApi.delete(record.fId)
    if (res.code === 200) { message.success('删除成功'); loadData() }
    else message.error(res.msg || '删除失败')
  } catch (error) { message.error('删除失败') }
}

const getStatusName = (status) => ({ 1: '待审核', 2: '已通过', 3: '签署中', 4: '执行中', 5: '已完成', 6: '已拒绝', 7: '已取消' })[status] || '-'
const getStatusColor = (status) => ({ 1: 'blue', 2: 'green', 3: 'orange', 4: 'purple', 5: 'cyan', 6: 'red', 7: 'gray' })[status] || 'default'

onMounted(() => { loadData() })
</script>