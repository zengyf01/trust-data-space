<template>
  <div>
    <a-card title="机构管理列表" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="机构名称">
          <a-input v-model:value="searchForm.orgName" placeholder="请输入机构名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allowClear style="width: 120px">
            <a-select-option :value="0">待审核</a-select-option>
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="2">冻结</a-select-option>
            <a-select-option :value="3">已注销</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
          <a-button type="primary" style="margin-left: 8px" @click="handleCreate">新建</a-button>
          <a-button
            v-if="selectedRowKeys.length > 0"
            type="primary"
            danger
            style="margin-left: 8px"
            @click="handleBatchDelete"
          >
            批量删除({{ selectedRowKeys.length }})
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card>
      <a-table
        :columns="columns"
        :data-source="orgList"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
        row-key="fId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fStatus'">
            <a-tag :color="getStatusColor(record.fStatus)">
              {{ getStatusText(record.fStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button
                v-if="record.fStatus === 0"
                type="link"
                size="small"
                @click="handleApprove(record)"
              >
                审核
              </a-button>
              <a-button
                v-if="record.fStatus === 1"
                type="link"
                size="small"
                danger
                @click="handleFreeze(record)"
              >
                冻结
              </a-button>
              <a-button
                v-if="record.fStatus === 2"
                type="link"
                size="small"
                @click="handleUnfreeze(record)"
              >
                解冻
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="机构详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentOrg">
        <a-descriptions-item label="机构编码">{{ currentOrg.fOrgCode }}</a-descriptions-item>
        <a-descriptions-item label="机构名称">{{ currentOrg.fOrgName }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentOrg.fStatus)">
            {{ getStatusText(currentOrg.fStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="联系人">{{ currentOrg.fContact }}</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ currentOrg.fContactPhone }}</a-descriptions-item>
        <a-descriptions-item label="联系地址">{{ currentOrg.fAddress }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ currentOrg.fCreateTime }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ currentOrg.fOrgDesc }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="isEdit ? '编辑机构' : '新建机构'"
      @ok="handleFormSubmit"
      :confirmLoading="formLoading"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" ref="formRef">
        <a-form-item label="机构名称" name="orgName" :rules="[{ required: true, message: '请输入机构名称' }]">
          <a-input v-model:value="formData.orgName" placeholder="请输入机构名称" />
        </a-form-item>
        <a-form-item label="机构编码" name="orgCode" :rules="[{ required: true, message: '请输入机构编码' }]">
          <a-input v-model:value="formData.orgCode" placeholder="请输入机构编码" :disabled="isEdit" />
        </a-form-item>
        <a-form-item label="联系人" name="contact">
          <a-input v-model:value="formData.contact" placeholder="请输入联系人" />
        </a-form-item>
        <a-form-item label="联系电话" name="contactPhone">
          <a-input v-model:value="formData.contactPhone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="地址" name="address">
          <a-input v-model:value="formData.address" placeholder="请输入地址" />
        </a-form-item>
        <a-form-item label="描述" name="orgDesc">
          <a-textarea v-model:value="formData.orgDesc" :rows="3" placeholder="请输入描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import organizationApi from '@/api/organization'

const columns = [
  { title: '机构编码', dataIndex: 'fOrgCode', key: 'fOrgCode', width: 150 },
  { title: '机构名称', dataIndex: 'fOrgName', key: 'fOrgName' },
  { title: '联系人', dataIndex: 'fContact', key: 'fContact', width: 120 },
  { title: '联系电话', dataIndex: 'fContactPhone', key: 'fContactPhone', width: 130 },
  { title: '状态', dataIndex: 'fStatus', key: 'fStatus', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 280 }
]

const orgList = ref([])
const loading = ref(false)
const selectedRowKeys = ref([])
const searchForm = reactive({
  orgName: '',
  status: null
})
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const detailVisible = ref(false)
const currentOrg = ref(null)

const formVisible = ref(false)
const isEdit = ref(false)
const formLoading = ref(false)
const formData = reactive({
  orgName: '',
  orgCode: '',
  orgDesc: '',
  contact: '',
  contactPhone: '',
  address: ''
})
const formRef = ref(null)

const statusMap = {
  0: { text: '待审核', color: 'orange' },
  1: { text: '正常', color: 'green' },
  2: { text: '冻结', color: 'red' },
  3: { text: '已注销', color: 'default' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusColor = (status) => statusMap[status]?.color || 'default'

const fetchOrgs = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: pagination.current,
      pageSize: pagination.pageSize,
      orgName: searchForm.orgName || undefined,
      status: searchForm.status || undefined
    }
    const res = await organizationApi.getOrgPage(params)
    if (res.code === 200) {
      orgList.value = res.data.list
      pagination.total = res.data.pagination.total
    }
  } catch (error) {
    console.error('获取机构列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchOrgs()
}

const handleReset = () => {
  searchForm.orgName = ''
  searchForm.status = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchOrgs()
}

const onSelectChange = (keys) => {
  selectedRowKeys.value = keys
}

const handleView = (record) => {
  currentOrg.value = record
  detailVisible.value = true
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    orgName: '',
    orgCode: '',
    orgDesc: '',
    contact: '',
    contactPhone: '',
    address: ''
  })
  formVisible.value = true
}

const handleEdit = (record) => {
  isEdit.value = true
  Object.assign(formData, {
    id: record.fId,
    orgName: record.fOrgName,
    orgCode: record.fOrgCode,
    orgDesc: record.fOrgDesc,
    contact: record.fContact,
    contactPhone: record.fContactPhone,
    address: record.fAddress
  })
  formVisible.value = true
}

const handleFormSubmit = async () => {
  formLoading.value = true
  try {
    const res = isEdit.value
      ? await organizationApi.updateOrg(formData.id, formData)
      : await organizationApi.createOrg(formData)
    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功')
      formVisible.value = false
      fetchOrgs()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleApprove = async (record) => {
  try {
    const res = await organizationApi.approveOrg(record.fId, 1)
    if (res.code === 200) {
      message.success('审核通过')
      fetchOrgs()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleFreeze = async (record) => {
  try {
    const res = await organizationApi.freezeOrg(record.fId, 2)
    if (res.code === 200) {
      message.success('已冻结')
      fetchOrgs()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleUnfreeze = async (record) => {
  try {
    const res = await organizationApi.freezeOrg(record.fId, 1)
    if (res.code === 200) {
      message.success('已解冻')
      fetchOrgs()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleDelete = async (record) => {
  try {
    const res = await organizationApi.deleteOrg(record.fId)
    if (res.code === 200) {
      message.success('删除成功')
      fetchOrgs()
    } else {
      message.error(res.msg || '操作失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchOrgs()
})
</script>
