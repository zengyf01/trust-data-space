<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>DOS 交付平台</h1>
        <p>Data Orchestration System</p>
      </div>
      <a-form
        :model="formData"
        :rules="rules"
        ref="formRef"
        @finish="handleLogin"
        class="login-form"
      >
        <a-form-item name="username">
          <a-input
            v-model:value="formData.username"
            placeholder="请输入用户名"
            size="large"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item name="password">
          <a-input-password
            v-model:value="formData.password"
            placeholder="请输入密码"
            size="large"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" block :loading="loading">
            登 录
          </a-button>
        </a-form-item>
      </a-form>
      <div class="login-tip">
        <p>默认账号：admin / admin123 (密码SM3加密)</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import axios from 'axios'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await axios.post('/api/tds/organization/uc/login?username=' + formData.username
      + '&password=' + formData.password + '&appId=DOS')
    if (res.data.code === 200) {
      // 保存登录信息到 localStorage
      localStorage.setItem('dos_token', res.data.data.token)
      localStorage.setItem('dos_user', JSON.stringify({
        userId: res.data.data.userId,
        username: res.data.data.username,
        realName: res.data.data.realName
      }))

      message.success('登录成功')
      router.push('/workorder')
    } else {
      message.error(res.data.msg || '登录失败')
    }
  } catch (error) {
    message.error('登录失败：' + (error.response?.data?.msg || error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #001529 0%, #004a6e 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 24px;
  color: #001529;
  margin: 0 0 8px 0;
}

.login-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-form {
  margin-top: 24px;
}

.login-tip {
  text-align: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.login-tip p {
  font-size: 12px;
  color: #999;
  margin: 0;
}
</style>
