<template>
  <div>
    <a-card>
      <a-tabs v-model:activeKey="activeTab">
        <!-- 计费模板 -->
        <a-tab-pane key="template" tab="计费模板">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="showTemplateModal = true">新建模板</a-button>
          </div>
          <a-table
            :columns="templateColumns"
            :data-source="templateList"
            :loading="templateLoading"
            :pagination="templatePagination"
            @change="handleTemplateTableChange"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'billingModel'">
                <a-tag>{{ record.billingModel === 1 ? '按次计费' : record.billingModel === 2 ? '包月计费' : '按量计费' }}</a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="viewTemplate(record)">查看</a-button>
                  <a-button type="link" size="small" @click="editTemplate(record)">编辑</a-button>
                  <a-button type="link" size="small" danger @click="deleteTemplate(record)">删除</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 产品定价 -->
        <a-tab-pane key="pricing" tab="产品定价">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="showPricingModal = true">新建定价</a-button>
          </div>
          <a-table
            :columns="pricingColumns"
            :data-source="pricingList"
            :loading="pricingLoading"
            :pagination="pricingPagination"
            @change="handlePricingTableChange"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'price'">
                <span>¥{{ record.price }}</span>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="viewPricing(record)">查看</a-button>
                  <a-button type="link" size="small" @click="editPricing(record)">编辑</a-button>
                  <a-button type="link" size="small" danger @click="deletePricing(record)">删除</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 用量记录 -->
        <a-tab-pane key="usage" tab="用量记录">
          <a-table
            :columns="usageColumns"
            :data-source="usageList"
            :loading="usageLoading"
            :pagination="usagePagination"
            @change="handleUsageTableChange"
            row-key="id"
          />
        </a-tab-pane>

        <!-- 账单管理 -->
        <a-tab-pane key="bill" tab="账单管理">
          <a-table
            :columns="billColumns"
            :data-source="billList"
            :loading="billLoading"
            :pagination="billPagination"
            @change="handleBillTableChange"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'amount'">
                <span>¥{{ record.amount }}</span>
              </template>
              <template v-else-if="column.key === 'status'">
                <a-tag :color="getBillStatusColor(record.status)">
                  {{ getBillStatusText(record.status) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="viewBill(record)">查看</a-button>
                  <a-button
                    v-if="record.status === 1"
                    type="link"
                    size="small"
                    @click="confirmBill(record)"
                  >
                    确认
                  </a-button>
                  <a-button
                    v-if="record.status === 2"
                    type="link"
                    size="small"
                    @click="payBill(record)"
                  >
                    支付
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 模板详情弹窗 -->
    <a-modal v-model:open="templateDetailVisible" title="模板详情" :footer="null">
      <a-descriptions :column="2" bordered v-if="currentTemplate">
        <a-descriptions-item label="模板名称">{{ currentTemplate.templateName }}</a-descriptions-item>
        <a-descriptions-item label="计费模式">
          <a-tag>{{ currentTemplate.billingModel === 1 ? '按次计费' : currentTemplate.billingModel === 2 ? '包月计费' : '按量计费' }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="单价">¥{{ currentTemplate.unitPrice }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ currentTemplate.fCreateTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 模板表单弹窗 -->
    <a-modal
      v-model:open="showTemplateModal"
      :title="editingTemplate?.id ? '编辑模板' : '新建模板'"
      @ok="handleTemplateSubmit"
      :confirmLoading="templateSubmitLoading"
    >
      <a-form :model="templateForm" :label-col="{ span: 6 }">
        <a-form-item label="模板名称" required>
          <a-input v-model:value="templateForm.templateName" placeholder="请输入模板名称" />
        </a-form-item>
        <a-form-item label="计费模式" required>
          <a-select v-model:value="templateForm.billingModel" placeholder="请选择计费模式">
            <a-select-option :value="1">按次计费</a-select-option>
            <a-select-option :value="2">包月计费</a-select-option>
            <a-select-option :value="3">按量计费</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="单价">
          <a-input-number v-model:value="templateForm.unitPrice" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 定价详情弹窗 -->
    <a-modal v-model:open="pricingDetailVisible" title="定价详情" :footer="null">
      <a-descriptions :column="2" bordered v-if="currentPricing">
        <a-descriptions-item label="产品名称">{{ currentPricing.productName }}</a-descriptions-item>
        <a-descriptions-item label="价格">¥{{ currentPricing.price }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ currentPricing.fCreateTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 定价表单弹窗 -->
    <a-modal
      v-model:open="showPricingModal"
      :title="editingPricing?.id ? '编辑定价' : '新建定价'"
      @ok="handlePricingSubmit"
      :confirmLoading="pricingSubmitLoading"
    >
      <a-form :model="pricingForm" :label-col="{ span: 6 }">
        <a-form-item label="产品ID" required>
          <a-input v-model:value="pricingForm.productId" placeholder="请输入产品ID" />
        </a-form-item>
        <a-form-item label="价格" required>
          <a-input-number v-model:value="pricingForm.price" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 账单详情弹窗 -->
    <a-modal v-model:open="billDetailVisible" title="账单详情" :footer="null">
      <a-descriptions :column="2" bordered v-if="currentBill">
        <a-descriptions-item label="账单编号">{{ currentBill.billNo }}</a-descriptions-item>
        <a-descriptions-item label="租户ID">{{ currentBill.tenantId }}</a-descriptions-item>
        <a-descriptions-item label="账单金额">¥{{ currentBill.amount }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getBillStatusColor(currentBill.status)">
            {{ getBillStatusText(currentBill.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="账单周期">{{ currentBill.billingPeriod }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ currentBill.fCreateTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import billingApi from '@/api/billing'

const activeTab = ref('template')

// ============ 模板 ============
const templateColumns = [
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName' },
  { title: '计费模式', dataIndex: 'billingModel', key: 'billingModel', width: 120 },
  { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 180 }
]

const templateList = ref([])
const templateLoading = ref(false)
const templatePagination = reactive({ current: 1, pageSize: 20, total: 0 })
const templateDetailVisible = ref(false)
const showTemplateModal = ref(false)
const templateSubmitLoading = ref(false)
const currentTemplate = ref(null)
const editingTemplate = ref(null)
const templateForm = reactive({ templateName: '', billingModel: 1, unitPrice: 0 })

// ============ 定价 ============
const pricingColumns = [
  { title: '产品ID', dataIndex: 'productId', key: 'productId' },
  { title: '产品名称', dataIndex: 'productName', key: 'productName' },
  { title: '价格', dataIndex: 'price', key: 'price', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 180 }
]

const pricingList = ref([])
const pricingLoading = ref(false)
const pricingPagination = reactive({ current: 1, pageSize: 20, total: 0 })
const pricingDetailVisible = ref(false)
const showPricingModal = ref(false)
const pricingSubmitLoading = ref(false)
const currentPricing = ref(null)
const editingPricing = ref(null)
const pricingForm = reactive({ productId: '', price: 0 })

// ============ 用量 ============
const usageColumns = [
  { title: '产品ID', dataIndex: 'productId', key: 'productId' },
  { title: '租户ID', dataIndex: 'tenantId', key: 'tenantId' },
  { title: '用量', dataIndex: 'usageCount', key: 'usageCount', width: 100 },
  { title: '计费周期', dataIndex: 'usagePeriod', key: 'usagePeriod', width: 120 },
  { title: '记录时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 }
]

const usageList = ref([])
const usageLoading = ref(false)
const usagePagination = reactive({ current: 1, pageSize: 20, total: 0 })

// ============ 账单 ============
const billColumns = [
  { title: '账单编号', dataIndex: 'billNo', key: 'billNo', width: 180 },
  { title: '租户ID', dataIndex: 'tenantId', key: 'tenantId', width: 120 },
  { title: '账单周期', dataIndex: 'billingPeriod', key: 'billingPeriod', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 150 }
]

const billList = ref([])
const billLoading = ref(false)
const billPagination = reactive({ current: 1, pageSize: 20, total: 0 })
const billDetailVisible = ref(false)
const currentBill = ref(null)

const billStatusMap = { 1: { text: '待确认', color: 'orange' }, 2: { text: '待支付', color: 'blue' }, 3: { text: '已支付', color: 'green' } }
const getBillStatusText = (s) => billStatusMap[s]?.text || '未知'
const getBillStatusColor = (s) => billStatusMap[s]?.color || 'default'

// 模板操作
const fetchTemplates = async () => {
  templateLoading.value = true
  try {
    const res = await billingApi.getTemplatePage({ currentPage: templatePagination.current, pageSize: templatePagination.pageSize })
    if (res.code === 200) { templateList.value = res.data.list; templatePagination.total = res.data.pagination.total }
  } catch (e) { console.error(e) } finally { templateLoading.value = false }
}

const handleTemplateTableChange = (pag) => { templatePagination.current = pag.current; fetchTemplates() }
const viewTemplate = (r) => { currentTemplate.value = r; templateDetailVisible.value = true }
const editTemplate = (r) => { editingTemplate.value = r; Object.assign(templateForm, { templateName: r.templateName, billingModel: r.billingModel, unitPrice: r.unitPrice }); showTemplateModal.value = true }
const deleteTemplate = async (r) => {
  const res = await billingApi.deleteTemplate(r.id)
  if (res.code === 200) { message.success('删除成功'); fetchTemplates() } else message.error(res.msg)
}
const handleTemplateSubmit = async () => {
  templateSubmitLoading.value = true
  try {
    const res = editingTemplate.value
      ? await billingApi.updateTemplate(editingTemplate.value.id, templateForm)
      : await billingApi.createTemplate(templateForm)
    if (res.code === 200) { message.success('成功'); showTemplateModal.value = false; fetchTemplates() } else message.error(res.msg)
  } catch (e) { message.error('失败') } finally { templateSubmitLoading.value = false }
}

// 定价操作
const fetchPricing = async () => {
  pricingLoading.value = true
  try {
    const res = await billingApi.getPricingPage({ currentPage: pricingPagination.current, pageSize: pricingPagination.pageSize })
    if (res.code === 200) { pricingList.value = res.data.list; pricingPagination.total = res.data.pagination.total }
  } catch (e) { console.error(e) } finally { pricingLoading.value = false }
}

const handlePricingTableChange = (pag) => { pricingPagination.current = pag.current; fetchPricing() }
const viewPricing = (r) => { currentPricing.value = r; pricingDetailVisible.value = true }
const editPricing = (r) => { editingPricing.value = r; Object.assign(pricingForm, { productId: r.productId, price: r.price }); showPricingModal.value = true }
const deletePricing = async (r) => {
  const res = await billingApi.deletePricing(r.id)
  if (res.code === 200) { message.success('删除成功'); fetchPricing() } else message.error(res.msg)
}
const handlePricingSubmit = async () => {
  pricingSubmitLoading.value = true
  try {
    const res = editingPricing.value
      ? await billingApi.updatePricing(editingPricing.value.id, pricingForm)
      : await billingApi.createPricing(pricingForm)
    if (res.code === 200) { message.success('成功'); showPricingModal.value = false; fetchPricing() } else message.error(res.msg)
  } catch (e) { message.error('失败') } finally { pricingSubmitLoading.value = false }
}

// 用量操作
const fetchUsage = async () => {
  usageLoading.value = true
  try {
    const res = await billingApi.getUsagePage({ currentPage: usagePagination.current, pageSize: usagePagination.pageSize })
    if (res.code === 200) { usageList.value = res.data.list; usagePagination.total = res.data.pagination.total }
  } catch (e) { console.error(e) } finally { usageLoading.value = false }
}
const handleUsageTableChange = (pag) => { usagePagination.current = pag.current; fetchUsage() }

// 账单操作
const fetchBills = async () => {
  billLoading.value = true
  try {
    const res = await billingApi.getBillPage({ currentPage: billPagination.current, pageSize: billPagination.pageSize })
    if (res.code === 200) { billList.value = res.data.list; billPagination.total = res.data.pagination.total }
  } catch (e) { console.error(e) } finally { billLoading.value = false }
}
const handleBillTableChange = (pag) => { billPagination.current = pag.current; fetchBills() }
const viewBill = (r) => { currentBill.value = r; billDetailVisible.value = true }
const confirmBill = async (r) => {
  const res = await billingApi.confirmBill(r.id)
  if (res.code === 200) { message.success('已确认'); fetchBills() } else message.error(res.msg)
}
const payBill = async (r) => {
  const res = await billingApi.payBill(r.id, 'online', r.amount)
  if (res.code === 200) { message.success('支付成功'); fetchBills() } else message.error(res.msg)
}

onMounted(() => { fetchTemplates(); fetchPricing(); fetchUsage(); fetchBills() })
</script>
