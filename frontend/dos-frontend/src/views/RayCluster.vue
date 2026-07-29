<template>
  <div>
    <a-card title="Ray集群管理" style="width: 100%">
      <template #extra>
        <a-button type="primary" @click="loadClusters">
          <ReloadOutlined /> 刷新
        </a-button>
      </template>

      <a-table
        :columns="columns"
        :data-source="clusters"
        :loading="loading"
        row-key="clusterId"
        :pagination="{ pageSize: 10 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'participants'">
            <span>{{ formatParticipants(record.participants) }}</span>
          </template>
          <template v-else-if="column.key === 'createTime'">
            <span>{{ formatTime(record.createTime) }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="showDetail(record)">
              详情
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 集群详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="集群详情"
      :footer="null"
      width="600px"
    >
      <a-descriptions :column="2" bordered v-if="currentCluster">
        <a-descriptions-item label="集群ID" :span="2">
          {{ currentCluster.clusterId }}
        </a-descriptions-item>
        <a-descriptions-item label="集群名称">
          {{ currentCluster.clusterName }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentCluster.status)">
            {{ getStatusText(currentCluster.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="Head节点ID">
          {{ currentCluster.headNodeId }}
        </a-descriptions-item>
        <a-descriptions-item label="Head地址" :span="2">
          {{ currentCluster.headAddress }}
        </a-descriptions-item>
        <a-descriptions-item label="参与节点" :span="2">
          {{ formatParticipants(currentCluster.participants) }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatTime(currentCluster.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ formatTime(currentCluster.updateTime) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import api from '../api'

const clusters = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentCluster = ref(null)

const columns = [
  {
    title: '集群类型',
    dataIndex: 'clusterName',
    key: 'clusterName',
    width: 150
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: 'Head地址',
    dataIndex: 'headAddress',
    key: 'headAddress',
    width: 180,
    ellipsis: true
  },
  {
    title: '参与节点',
    dataIndex: 'participants',
    key: 'participants',
    width: 150
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 160
  },
  {
    title: '操作',
    key: 'action',
    width: 80,
    fixed: 'right'
  }
]

const getStatusColor = (status) => {
  const colors = {
    'CREATING': 'processing',
    'RUNNING': 'success',
    'STOPPING': 'warning',
    'STOPPED': 'default',
    'FAILED': 'error'
  }
  return colors[status] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    'CREATING': '创建中',
    'RUNNING': '运行中',
    'STOPPING': '停止中',
    'STOPPED': '已停止',
    'FAILED': '失败'
  }
  return texts[status] || status
}

const formatParticipants = (participants) => {
  if (!participants) return '-'
  try {
    const list = JSON.parse(participants)
    return list.length + '个节点'
  } catch {
    return '-'
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const loadClusters = async () => {
  loading.value = true
  try {
    const res = await api.get('/ray/cluster/list')
    if (res.data.code === 200) {
      clusters.value = res.data.data || []
    } else {
      message.error(res.data.msg || '获取集群列表失败')
    }
  } catch (err) {
    message.error('获取集群列表失败: ' + err.message)
  } finally {
    loading.value = false
  }
}

const showDetail = (record) => {
  currentCluster.value = record
  detailVisible.value = true
}

const handleRelease = async (record) => {
  try {
    const res = await api.post(`/ray/cluster/release/${record.clusterId}`)
    if (res.data.code === 200) {
      message.success('集群已释放')
      loadClusters()
    } else {
      message.error(res.data.msg || '释放集群失败')
    }
  } catch (err) {
    message.error('释放集群失败: ' + err.message)
  }
}

const handleDestroy = async (record) => {
  try {
    const res = await api.post(`/ray/cluster/destroy/${record.clusterId}`)
    if (res.data.code === 200) {
      message.success('集群已销毁')
      loadClusters()
    } else {
      message.error(res.data.msg || '销毁集群失败')
    }
  } catch (err) {
    message.error('销毁集群失败: ' + err.message)
  }
}

onMounted(() => {
  loadClusters()
})
</script>
