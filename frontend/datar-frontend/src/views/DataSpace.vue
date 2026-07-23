<template>
  <a-card title="我的数据空间">
    <a-table :columns="columns" :data-source="data" :loading="loading">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'fStatus'">
          <a-tag :color="record.fStatus === 1 ? 'green' : 'red'">
            {{ record.fStatus === 1 ? '正常' : '已退出' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click.stop="handleView(record)">详情</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="detailVisible" title="数据空间详情" :footer="null">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="空间名称">{{ currentRecord?.fName }}</a-descriptions-item>
        <a-descriptions-item label="空间代码">{{ currentRecord?.fCode }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentRecord?.fStatus === 1 ? 'green' : 'red'">
            {{ currentRecord?.fStatus === 1 ? '正常' : '已退出' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="加入时间">{{ currentRecord?.fJoinTime }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ currentRecord?.fDescription }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </a-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const columns = [
  { title: '空间名称', dataIndex: 'fName', key: 'fName' },
  { title: '空间代码', dataIndex: 'fCode', key: 'fCode' },
  { title: '状态', dataIndex: 'fStatus', key: 'fStatus', width: 100 },
  { title: '加入时间', dataIndex: 'fJoinTime', key: 'fJoinTime', width: 180 },
  { title: '操作', key: 'action', width: 100 }
]

const loading = ref(false)
const data = ref([])
const detailVisible = ref(false)
const currentRecord = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取当前连接器加入的数据空间列表
    // 暂时使用模拟数据
    data.value = [
      { fId: '1', fName: '测试数据空间', fCode: 'DS_TEST', fStatus: 1, fJoinTime: '2024-01-15 10:30:00', fDescription: '用于测试的数据空间' }
    ]
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleView = (record) => {
  currentRecord.value = record
  detailVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>