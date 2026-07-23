<template>
  <div>
    <a-card>
      <a-tabs v-model:activeKey="activeTab">
        <!-- 系统参数 -->
        <a-tab-pane key="config" tab="系统参数">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="showConfigModal = true">新建参数</a-button>
          </div>
          <a-table
            :columns="configColumns"
            :data-source="configList"
            :loading="configLoading"
            :pagination="configPagination"
            @change="handleConfigTableChange"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="viewConfig(record)">查看</a-button>
                  <a-button type="link" size="small" @click="editConfig(record)">编辑</a-button>
                  <a-button type="link" size="small" danger @click="deleteConfig(record)">删除</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 通知配置 -->
        <a-tab-pane key="notification" tab="通知配置">
          <div style="margin-bottom: 16px">
            <a-button type="primary" @click="showNotificationModal = true">新建通知配置</a-button>
          </div>
          <a-table
            :columns="notificationColumns"
            :data-source="notificationList"
            :loading="notificationLoading"
            :pagination="notificationPagination"
            @change="handleNotificationTableChange"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'notificationType'">
                <a-tag>{{ getNotificationTypeText(record.notificationType) }}</a-tag>
              </template>
              <template v-else-if="column.key === 'isEnabled'">
                <a-tag :color="record.isEnabled === 1 ? 'green' : 'red'">
                  {{ record.isEnabled === 1 ? '启用' : '禁用' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="viewNotification(record)">查看</a-button>
                  <a-button type="link" size="small" @click="editNotification(record)">编辑</a-button>
                  <a-button type="link" size="small" @click="toggleNotification(record)">
                    {{ record.isEnabled === 1 ? '禁用' : '启用' }}
                  </a-button>
                  <a-button type="link" size="small" danger @click="deleteNotification(record)">删除</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 发送记录 -->
        <a-tab-pane key="log" tab="发送记录">
          <a-table
            :columns="logColumns"
            :data-source="logList"
            :loading="logLoading"
            :pagination="logPagination"
            @change="handleLogTableChange"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="getLogStatusColor(record.status)">
                  {{ getLogStatusText(record.status) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button
                  v-if="record.status === 0"
                  type="link"
                  size="small"
                  @click="retryLog(record)"
                >
                  重试
                </a-button>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 参数详情 -->
    <a-modal v-model:open="configDetailVisible" title="参数详情" :footer="null">
      <a-descriptions :column="2" bordered v-if="currentConfig">
        <a-descriptions-item label="参数编码">{{ currentConfig.configKey }}</a-descriptions-item>
        <a-descriptions-item label="参数值">{{ currentConfig.configValue }}</a-descriptions-item>
        <a-descriptions-item label="参数分组">{{ currentConfig.configGroup }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ currentConfig.fCreateTime }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ currentConfig.description }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 参数表单 -->
    <a-modal
      v-model:open="showConfigModal"
      :title="editingConfig?.id ? '编辑参数' : '新建参数'"
      @ok="handleConfigSubmit"
      :confirmLoading="configSubmitLoading"
    >
      <a-form :model="configForm" :label-col="{ span: 6 }">
        <a-form-item label="参数编码" required>
          <a-input v-model:value="configForm.configKey" placeholder="请输入参数编码" :disabled="!!editingConfig?.id" />
        </a-form-item>
        <a-form-item label="参数值" required>
          <a-input v-model:value="configForm.configValue" placeholder="请输入参数值" />
        </a-form-item>
        <a-form-item label="参数分组">
          <a-input v-model:value="configForm.configGroup" placeholder="请输入参数分组" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="configForm.description" :rows="2" placeholder="请输入描述" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 通知详情 -->
    <a-modal v-model:open="notificationDetailVisible" title="通知配置详情" :footer="null">
      <a-descriptions :column="2" bordered v-if="currentNotification">
        <a-descriptions-item label="通知类型">
          <a-tag>{{ getNotificationTypeText(currentNotification.notificationType) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="启用状态">
          <a-tag :color="currentNotification.isEnabled === 1 ? 'green' : 'red'">
            {{ currentNotification.isEnabled === 1 ? '启用' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="配置内容" :span="2">{{ currentNotification.configContent }}</a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">{{ currentNotification.fCreateTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 通知表单 -->
    <a-modal
      v-model:open="showNotificationModal"
      :title="editingNotification?.id ? '编辑通知配置' : '新建通知配置'"
      @ok="handleNotificationSubmit"
      :confirmLoading="notificationSubmitLoading"
    >
      <a-form :model="notificationForm" :label-col="{ span: 6 }">
        <a-form-item label="通知类型" required>
          <a-select v-model:value="notificationForm.notificationType" placeholder="请选择通知类型">
            <a-select-option value="EMAIL">邮件</a-select-option>
            <a-select-option value="SMS">短信</a-select-option>
            <a-select-option value="WECHAT">微信</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="配置内容" required>
          <a-textarea v-model:value="notificationForm.configContent" :rows="3" placeholder="请输入配置内容" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import systemApi from '@/api/system'

const activeTab = ref('config')

// ============ 系统参数 ============
const configColumns = [
  { title: '参数编码', dataIndex: 'configKey', key: 'configKey' },
  { title: '参数值', dataIndex: 'configValue', key: 'configValue' },
  { title: '参数分组', dataIndex: 'configGroup', key: 'configGroup', width: 120 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 180 }
]

const configList = ref([])
const configLoading = ref(false)
const configPagination = reactive({ current: 1, pageSize: 20, total: 0 })
const configDetailVisible = ref(false)
const showConfigModal = ref(false)
const configSubmitLoading = ref(false)
const currentConfig = ref(null)
const editingConfig = ref(null)
const configForm = reactive({ configKey: '', configValue: '', configGroup: '', description: '' })

// ============ 通知配置 ============
const notificationColumns = [
  { title: '通知类型', dataIndex: 'notificationType', key: 'notificationType', width: 120 },
  { title: '启用状态', dataIndex: 'isEnabled', key: 'isEnabled', width: 100 },
  { title: '创建时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 220 }
]

const notificationList = ref([])
const notificationLoading = ref(false)
const notificationPagination = reactive({ current: 1, pageSize: 20, total: 0 })
const notificationDetailVisible = ref(false)
const showNotificationModal = ref(false)
const notificationSubmitLoading = ref(false)
const currentNotification = ref(null)
const editingNotification = ref(null)
const notificationForm = reactive({ notificationType: '', configContent: '' })

// ============ 发送记录 ============
const logColumns = [
  { title: '通知类型', dataIndex: 'notificationType', key: 'notificationType', width: 100 },
  { title: '接收人', dataIndex: 'recipient', key: 'recipient' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '发送时间', dataIndex: 'fCreateTime', key: 'fCreateTime', width: 180 },
  { title: '操作', key: 'action', width: 80 }
]

const logList = ref([])
const logLoading = ref(false)
const logPagination = reactive({ current: 1, pageSize: 20, total: 0 })

const notificationTypeMap = { EMAIL: '邮件', SMS: '短信', WECHAT: '微信' }
const getNotificationTypeText = (t) => notificationTypeMap[t] || t

const logStatusMap = { 0: { text: '失败', color: 'red' }, 1: { text: '成功', color: 'green' } }
const getLogStatusText = (s) => logStatusMap[s]?.text || '未知'
const getLogStatusColor = (s) => logStatusMap[s]?.color || 'default'

// 参数操作
const fetchConfigs = async () => {
  configLoading.value = true
  try {
    const res = await systemApi.getConfigPage({ currentPage: configPagination.current, pageSize: configPagination.pageSize })
    if (res.code === 200) { configList.value = res.data.list; configPagination.total = res.data.pagination.total }
  } catch (e) { console.error(e) } finally { configLoading.value = false }
}

const handleConfigTableChange = (pag) => { configPagination.current = pag.current; fetchConfigs() }
const viewConfig = (r) => { currentConfig.value = r; configDetailVisible.value = true }
const editConfig = (r) => { editingConfig.value = r; Object.assign(configForm, { configKey: r.configKey, configValue: r.configValue, configGroup: r.configGroup, description: r.description }); showConfigModal.value = true }
const deleteConfig = async (r) => {
  const res = await systemApi.deleteConfig(r.id)
  if (res.code === 200) { message.success('删除成功'); fetchConfigs() } else message.error(res.msg)
}
const handleConfigSubmit = async () => {
  configSubmitLoading.value = true
  try {
    const res = editingConfig.value
      ? await systemApi.updateConfig(editingConfig.value.id, configForm)
      : await systemApi.createConfig(configForm)
    if (res.code === 200) { message.success('成功'); showConfigModal.value = false; fetchConfigs() } else message.error(res.msg)
  } catch (e) { message.error('失败') } finally { configSubmitLoading.value = false }
}

// 通知配置操作
const fetchNotifications = async () => {
  notificationLoading.value = true
  try {
    const res = await systemApi.getNotificationConfigPage({ currentPage: notificationPagination.current, pageSize: notificationPagination.pageSize })
    if (res.code === 200) { notificationList.value = res.data.list; notificationPagination.total = res.data.pagination.total }
  } catch (e) { console.error(e) } finally { notificationLoading.value = false }
}

const handleNotificationTableChange = (pag) => { notificationPagination.current = pag.current; fetchNotifications() }
const viewNotification = (r) => { currentNotification.value = r; notificationDetailVisible.value = true }
const editNotification = (r) => { editingNotification.value = r; Object.assign(notificationForm, { notificationType: r.notificationType, configContent: r.configContent }); showNotificationModal.value = true }
const toggleNotification = async (r) => {
  const res = await systemApi.toggleNotificationConfig(r.id, r.isEnabled === 1 ? 0 : 1)
  if (res.code === 200) { message.success('更新成功'); fetchNotifications() } else message.error(res.msg)
}
const deleteNotification = async (r) => {
  const res = await systemApi.deleteNotificationConfig(r.id)
  if (res.code === 200) { message.success('删除成功'); fetchNotifications() } else message.error(res.msg)
}
const handleNotificationSubmit = async () => {
  notificationSubmitLoading.value = true
  try {
    const res = editingNotification.value
      ? await systemApi.updateNotificationConfig(editingNotification.value.id, notificationForm)
      : await systemApi.createNotificationConfig(notificationForm)
    if (res.code === 200) { message.success('成功'); showNotificationModal.value = false; fetchNotifications() } else message.error(res.msg)
  } catch (e) { message.error('失败') } finally { notificationSubmitLoading.value = false }
}

// 发送记录操作
const fetchLogs = async () => {
  logLoading.value = true
  try {
    const res = await systemApi.getNotificationLogPage({ currentPage: logPagination.current, pageSize: logPagination.pageSize })
    if (res.code === 200) { logList.value = res.data.list; logPagination.total = res.data.pagination.total }
  } catch (e) { console.error(e) } finally { logLoading.value = false }
}
const handleLogTableChange = (pag) => { logPagination.current = pag.current; fetchLogs() }
const retryLog = async (r) => {
  const res = await systemApi.retryNotification(r.id)
  if (res.code === 200) { message.success('已重试'); fetchLogs() } else message.error(res.msg)
}

onMounted(() => { fetchConfigs(); fetchNotifications(); fetchLogs() })
</script>
