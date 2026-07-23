<template>
  <div class="strategy-container">
    <a-layout-content class="content">
        <a-row :gutter="16">
          <a-col :span="8" v-for="strategy in strategies" :key="strategy.type">
            <a-card :title="strategy.name" class="strategy-card">
              <p>{{ strategy.desc }}</p>
              <a-space direction="vertical" style="margin-top: 12px;">
                <a-tag :color="strategy.color">{{ strategy.type }}</a-tag>
                <a-button v-if="strategy.type === 'PRIVACY_COMPUTE'" type="primary" size="small" @click="$router.push('/privacy')">
                  进入隐私计算
                </a-button>
                <a-button v-if="strategy.type === 'PRIVACY_COMPUTE'" type="default" size="small" @click="$router.push('/task')">
                  任务管理
                </a-button>
                <a-button v-if="strategy.type === 'PRIVACY_COMPUTE'" type="default" size="small" @click="$router.push('/node')">
                  节点管理
                </a-button>
              </a-space>
            </a-card>
          </a-col>
        </a-row>
      </a-layout-content>
  </div>
</template>

<script setup>
const strategies = [
  { type: 'DATA_SERVICE', name: '数据服务策略', desc: 'SFTP交付、HTTP推送、数据库同步', color: 'blue' },
  { type: 'SANDBOX', name: '安全沙盒策略', desc: 'Kata容器、TEE运行时、JupyterLab', color: 'green' },
  { type: 'PRIVACY_COMPUTE', name: '隐私计算策略', desc: 'SecretFlow PSI求交、联邦学习', color: 'purple' }
]
</script>

<style scoped>
.strategy-container {
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
.strategy-card {
  margin-bottom: 16px;
}
</style>
