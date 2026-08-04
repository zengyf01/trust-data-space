<template>
  <div class="create-container">
    <a-layout-content class="content">
        <a-card title="任务配置">
          <a-form :model="form" layout="vertical">
            <a-form-item label="任务名称" required>
              <a-input v-model:value="form.name" placeholder="请输入任务名称" />
            </a-form-item>
            <a-form-item label="任务类型" required>
              <a-select v-model:value="form.type" placeholder="请选择任务类型">
                <a-select-option value="1">PSI求交</a-select-option>
                <a-select-option value="2">MPC安全计算</a-select-option>
                <a-select-option value="fl">联邦学习</a-select-option>
                <a-select-option value="4">自定义代码</a-select-option>
                <a-select-option value="6">复合任务</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item v-if="form.type === 'fl'" label="联邦类型" required>
              <a-select v-model:value="form.flMode" placeholder="请选择联邦类型">
                <a-select-option value="horizontal">横向联邦</a-select-option>
                <a-select-option value="vertical">纵向联邦</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="描述">
              <a-textarea v-model:value="form.description" :rows="3" placeholder="任务描述" />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleCreate">创建</a-button>
                <a-button @click="$router.push('/tasks')">取消</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-card>
      </a-layout-content>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { taskAPI } from '../api'

const router = useRouter()
const form = ref({ name: '', type: '', flMode: 'horizontal', description: '' })

const handleCreate = async () => {
  if (!form.value.name) {
    message.warning('请输入任务名称')
    return
  }
  // 联邦学习的二级选项映射为后端 f_type: 横向=3, 纵向=5
  const payload = { ...form.value }
  if (payload.type === 'fl') {
    payload.type = payload.flMode === 'vertical' ? 5 : 3
  }
  delete payload.flMode
  try {
    await taskAPI.create(payload)
    message.success('任务创建成功')
    router.push('/tasks')
  } catch (e) {
    message.error('创建失败')
  }
}
</script>

<style scoped>
.create-container { height: 100vh; }
.header { background: #001529; padding: 0 24px; }
.header h1 { color: white; margin: 0; line-height: 64px; }
.content { padding: 24px; background: #f0f2f5; }
</style>