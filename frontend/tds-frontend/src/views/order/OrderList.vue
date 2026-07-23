<template>
  <div>
    <a-card title="交易订单列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="订单编号">
          <a-input v-model:value="searchForm.orderCode" placeholder="请输入订单编号" />
        </a-form-item>
        <a-form-item label="订单状态">
          <a-select v-model:value="searchForm.orderStatus" placeholder="请选择状态" allowClear style="width: 120px">
            <a-select-option :value="1">待审核</a-select-option>
            <a-select-option :value="2">审核通过</a-select-option>
            <a-select-option :value="3">审核拒绝</a-select-option>
            <a-select-option :value="4">已取消</a-select-option>
            <a-select-option :value="5">进行中</a-select-option>
            <a-select-option :value="6">已完成</a-select-option>
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
        :data-source="orderList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'orderStatus'">
            <a-tag :color="getStatusColor(record.orderStatus)">
              {{ getStatusText(record.orderStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button
                v-if="record.orderStatus === 1"
                type="link"
                size="small"
                @click="handleApprove(record)"
              >
                审核通过
              </a-button>
              <a-button
                v-if="record.orderStatus === 1"
                type="link"
                size="small"
                danger
                @click="handleReject(record)"
              >
                审核拒绝
              </a-button>
              <a-button
                v-if="record.orderStatus === 2 || record.orderStatus === 5"
                type="link"
                size="small"
                danger
                @click="handleCancel(record)"
              >
                取消
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 订单详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="订单详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentOrder">
        <a-descriptions-item label="订单编号">{{ currentOrder.orderCode }}</a-descriptions-item>
        <a-descriptions-item label="订单状态">
          <a-tag :color="getStatusColor(currentOrder.orderStatus)">
            {{ getStatusText(currentOrder.orderStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="产品名称">{{ currentOrder.productName }}</a-descriptions-item>
        <a-descriptions-item label="购买数量">{{ currentOrder.quantity }}</a-descriptions-item>
        <a-descriptions-item label="订单金额">{{ currentOrder.orderAmount }}</a-descriptions-item>
        <a-descriptions-item label="申请时间">{{ currentOrder.fCreateTime }}</a-descriptions-item>
        <a-descriptions-item label="供方机构">{{ currentOrder.providerOrgName }}</a-descriptions-item>
        <a-descriptions-item label="需方机构">{{ currentOrder.consumerOrgName }}</a-descriptions-item>
        <a-descriptions-item label="开始时间">{{ currentOrder.startTime }}</a-descriptions-item>
        <a-descriptions-item label="结束时间">{{ currentOrder.endTime }}</a-descriptions-item>
        <a-descriptions-item label="交付API" :span="2">{{ currentOrder.deliveryApiInfo || '暂无' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 拒绝弹窗 -->
    <a-modal
      v-model:open="rejectVisible"
      title="审核拒绝"
      @ok="handleRejectConfirm"
      :confirmLoading="actionLoading"
    >
      <a-form>
        <a-form-item label="拒绝原因" required>
          <a-input v-model:value="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import orderApi from '@/api/order'

const columns = [
  { title: '订单编号', dataIndex: 'orderCode', key: 'orderCode', width: 150 },
  { title: '产品名称', dataIndex: 'productName', key: 'productName' },
  { title: '供方机构', dataIndex: 'providerOrgName', key: 'providerOrgName' },
  { title: '需方机构', dataIndex: 'consumerOrgName', key: 'consumerOrgName' },
  { title: '订单金额', dataIndex: 'orderAmount', key: 'orderAmount', width: 100 },
  { title: '状态', dataIndex: 'orderStatus', key: 'orderStatus', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 250 }
]

const orderList = ref([])
const loading = ref(false)
const searchForm = reactive({
  orderCode: '',
  orderStatus: null
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentOrder = ref(null)

const rejectVisible = ref(false)
const rejectReason = ref('')
const actionLoading = ref(false)
const currentActionOrder = ref(null)

const statusMap = {
  1: { text: '待审核', color: 'orange' },
  2: { text: '审核通过', color: 'blue' },
  3: { text: '审核拒绝', color: 'red' },
  4: { text: '已取消', color: 'gray' },
  5: { text: '进行中', color: 'processing' },
  6: { text: '已完成', color: 'green' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusColor = (status) => statusMap[status]?.color || 'default'

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNumber: pagination.current,
      pageSize: pagination.pageSize,
      orderCode: searchForm.orderCode || undefined,
      orderStatus: searchForm.orderStatus || undefined
    }
    const res = await orderApi.getOrderPage(params)
    if (res.code === 200) {
      orderList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchOrders()
}

const handleReset = () => {
  searchForm.orderCode = ''
  searchForm.orderStatus = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchOrders()
}

const handleView = (record) => {
  currentOrder.value = record
  detailVisible.value = true
}

const handleApprove = async (record) => {
  try {
    const res = await orderApi.approveOrder(record.id)
    if (res.code === 200) {
      message.success('审核通过')
      fetchOrders()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleReject = (record) => {
  currentActionOrder.value = record
  rejectReason.value = ''
  rejectVisible.value = true
}

const handleRejectConfirm = async () => {
  if (!rejectReason.value) {
    message.warning('请输入拒绝原因')
    return
  }
  actionLoading.value = true
  try {
    const res = await orderApi.rejectOrder(currentActionOrder.value.id, rejectReason.value)
    if (res.code === 200) {
      message.success('已拒绝')
      rejectVisible.value = false
      fetchOrders()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

const handleCancel = async (record) => {
  try {
    const res = await orderApi.cancelOrder(record.id)
    if (res.code === 200) {
      message.success('已取消')
      fetchOrders()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

onMounted(() => {
  fetchOrders()
})
</script>
