<template>
  <div class="auth-container">
    <a-layout-content class="content">
        <a-card title="MaxKey SSO 认证">
          <a-form :model="form" layout="vertical">
            <a-form-item label="用户名">
              <a-input v-model:value="form.username" placeholder="请输入用户名" />
            </a-form-item>
            <a-form-item label="密码">
              <a-input-password v-model:value="form.password" placeholder="请输入密码" />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleLogin">登录</a-button>
                <a-button @click="handleSSOLogin">SSO登录</a-button>
                <a-button @click="handleLogout">登出</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-card>
      </a-layout-content>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'

const form = ref({
  username: '',
  password: ''
})

const handleLogin = () => {
  message.success('登录成功')
}

const handleSSOLogin = () => {
  message.info('SSO登录跳转中...')
}

const handleLogout = () => {
  message.warning('已登出')
}
</script>

<style scoped>
.auth-container {
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
  max-width: 500px;
}
</style>
