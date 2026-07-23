<template>
  <div class="workorder-container">
    <a-layout-content class="content">
        <a-card title="工单列表">
          <template #extra>
            <a-button type="primary" @click="showCreateModal">创建工单</a-button>
          </template>
          <a-table :columns="columns" :data-source="data" :loading="loading" :pagination="pagination" @change="handleTableChange">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="getStatusColor(record.workOrderStatus)">
                  {{ getStatusText(record.workOrderStatus) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button size="small" @click="handleDetail(record)">详情</a-button>
                  <a-button size="small" type="primary" @click="handleExecute(record)" v-if="record.workOrderStatus === 1">执行</a-button>
                  <a-popconfirm title="确定删除此工单?" @confirm="handleDelete(record)" ok-text="确定" cancel-text="取消">
                    <a-button size="small" danger v-if="record.workOrderStatus !== 2">删除</a-button>
                  </a-popconfirm>
                  <a-button size="small" @click="handleCancel(record)" v-if="record.workOrderStatus !== 5 && record.workOrderStatus !== 2">取消</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-layout-content>

    <!-- 创建工单弹窗 -->
    <a-modal
      v-model:open="createModalVisible"
      title="创建工单"
      @ok="handleCreate"
      @cancel="createModalVisible = false"
      :confirmLoading="createLoading"
      width="600px"
    >
      <a-form :model="createForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="订单编号" required>
          <a-input v-model:value="createForm.orderCode" placeholder="请输入订单编号" />
        </a-form-item>
        <a-form-item label="工单类型" required>
          <a-select v-model:value="createForm.workOrderType" placeholder="请选择工单类型">
            <a-select-option :value="1">数据服务</a-select-option>
            <a-select-option :value="2">安全沙盒</a-select-option>
            <a-select-option :value="3">隐私计算</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="配置参数">
          <a-textarea v-model:value="createForm.configJson" placeholder="请输入JSON格式的配置参数" :rows="4" />
        </a-form-item>
        <a-form-item label="创建人">
          <a-input v-model:value="createForm.creator" placeholder="请输入创建人" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 工单详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="工单详情"
      :footer="null"
      width="700px"
    >
      <a-descriptions bordered :column="2" v-if="currentRecord">
        <a-descriptions-item label="工单编号">{{ currentRecord.workOrderCode }}</a-descriptions-item>
        <a-descriptions-item label="订单编号">{{ currentRecord.orderCode }}</a-descriptions-item>
        <a-descriptions-item label="工单类型">{{ getWorkOrderTypeText(currentRecord.workOrderType) }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentRecord.workOrderStatus)">
            {{ getStatusText(currentRecord.workOrderStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建人">{{ currentRecord.creator }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ currentRecord.fCreateTime }}</a-descriptions-item>
        <a-descriptions-item label="处理结果" :span="2">{{ currentRecord.resultMessage || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import axios from 'axios'

const loading = ref(false)
const data = ref([])
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const createModalVisible = ref(false)
const createLoading = ref(false)
const createForm = ref({
  orderCode: '',
  workOrderType: 1,
  configJson: '{}',
  creator: ''
})

const detailModalVisible = ref(false)
const currentRecord = ref(null)

const columns = [
  { title: '工单编号', dataIndex: 'workOrderCode', key: 'workOrderCode' },
  { title: '订单编号', dataIndex: 'orderCode', key: 'orderCode' },
  { title: '工单类型', dataIndex: 'workOrderType', key: 'workOrderType' },
  { title: '状态', dataIndex: 'workOrderStatus', key: 'status' },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 280 }
]

const workOrderTypes = { 1: '数据服务', 2: '安全沙盒', 3: '隐私计算' }

const getStatusColor = (status) => {
  const colors = { 1: 'blue', 2: 'orange', 3: 'green', 4: 'red', 5: 'gray' }
  return colors[status] || 'default'
}

const getStatusText = (status) => {
  const texts = { 1: '待处理', 2: '处理中', 3: '已完成', 4: '失败', 5: '已取消' }
  return texts[status] || '未知'
}

const getWorkOrderTypeText = (type) => {
  return workOrderTypes[type] || '未知'
}

const showCreateModal = () => {
  createForm.value = {
    orderCode: '',
    workOrderType: 1,
    configJson: '{}',
    creator: ''
  }
  createModalVisible.value = true
}

const handleCreate = async () => {
  if (!createForm.value.orderCode) {
    message.error('请输入订单编号')
    return
  }

  try {
    createLoading.value = true
    const response = await axios.post('/api/dos/workOrder', {
      orderCode: createForm.value.orderCode,
      workOrderType: createForm.value.workOrderType,
      configJson: createForm.value.configJson,
      creator: createForm.value.creator
    })

    if (response.data.code === 200) {
      message.success('工单创建成功')
      createModalVisible.value = false
      loadData()
    } else {
      message.error(response.data.msg || '创建失败')
    }
  } catch (error) {
    console.error('创建工单失败:', error)
    message.error(error.response?.data?.msg || '创建失败，请检查网络')
  } finally {
    createLoading.value = false
  }
}

const handleDetail = (record) => {
  currentRecord.value = record
  detailModalVisible.value = true
}

const handleExecute = async (record) => {
  try {
    const response = await axios.post(`/api/dos/workOrder/${record.id}/start`)
    if (response.data.code === 200) {
      message.success('工单执行成功')
      loadData()
    } else {
      message.error(response.data.msg || '执行失败')
    }
  } catch (error) {
    message.error(error.response?.data?.msg || '执行失败')
  }
}

const handleCancel = async (record) => {
  try {
    const response = await axios.post(`/api/dos/workOrder/${record.id}/cancel`)
    if (response.data.code === 200) {
      message.success('工单已取消')
      loadData()
    } else {
      message.error(response.data.msg || '取消失败')
    }
  } catch (error) {
    message.error(error.response?.data?.msg || '取消失败')
  }
}

const handleDelete = async (record) => {
  try {
    console.log('Deleting workOrder:', record.id)
    const response = await axios.delete(`/api/dos/workOrder/${record.id}`)
    console.log('Delete response:', response.data)
    if (response.data.code === 200) {
      message.success('工单已删除')
      loadData()
    } else {
      message.error(response.data.msg || '删除失败')
    }
  } catch (error) {
    console.error('Delete error:', error)
    console.error('Error response:', error.response?.data)
    message.error(error.response?.data?.msg || error.message || '删除失败')
  }
}

const handleTableChange = (pag) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

const loadData = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/dos/workOrder/page', {
      params: {
        pageNumber: pagination.value.current,
        pageSize: pagination.value.pageSize
      }
    })

    if (response.data.code === 200) {
      data.value = response.data.data.list || []
      pagination.value.total = response.data.data.pagination?.total || 0
    }
  } catch (error) {
    console.error('加载工单列表失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.workorder-container {
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
</style>
