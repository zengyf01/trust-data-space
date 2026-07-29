<template>
  <a-config-provider :locale="zhCN">
    <!-- 登录页面不需要主布局 -->
    <router-view v-if="route.path === '/login'" />
    <!-- 业务页面使用主布局 -->
    <a-layout v-else style="min-height: 100vh">
      <a-layout-sider
        theme="dark"
        width="220"
        style="overflow: auto; height: 100vh; position: fixed; left: 0; top: 0; bottom: 0"
      >
        <div style="height: 64px; line-height: 64px; text-align: center; color: white; font-size: 16px; font-weight: bold; border-bottom: 1px solid #333">
          DOS 交付平台
        </div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          v-model:openKeys="openKeys"
          theme="dark"
          mode="inline"
          @click="handleMenuClick"
        >
          <a-menu-item key="/workorder">
            <FileTextOutlined /><span>工单管理</span>
          </a-menu-item>
          <a-menu-item key="/sandbox">
            <BankOutlined /><span>安全沙盒</span>
          </a-menu-item>
          <a-menu-item key="/privacy">
            <LockOutlined /><span>隐私计算</span>
          </a-menu-item>
          <a-menu-item key="/datasources">
            <DatabaseOutlined /><span>数据服务</span>
          </a-menu-item>
          <a-sub-menu key="system">
            <template #title>
              <span><SettingOutlined /><span>系统管理</span></span>
            </template>
            <a-menu-item key="/user"><UserOutlined /><span>用户管理</span></a-menu-item>
            <a-menu-item key="/node"><ClusterOutlined /><span>节点管理</span></a-menu-item>
            <a-menu-item key="/ray-cluster"><CloudServerOutlined /><span>集群管理</span></a-menu-item>
            <a-menu-item key="/strategy"><SettingOutlined /><span>策略配置</span></a-menu-item>
          </a-sub-menu>
        </a-menu>
      </a-layout-sider>
      <a-layout style="margin-left: 220px">
        <a-layout-header style="background: #001529; padding: 0 24px; display: flex; align-items: center; justify-content: space-between">
          <div style="color: white; font-size: 16px">DOS 交付管理系统</div>
          <div style="display: flex; align-items: center; color: white;">
            <span style="margin-right: 16px">{{ userInfo.username || '用户' }}</span>
            <a-button type="link" style="color: white" @click="handleLogout">退出</a-button>
          </div>
        </a-layout-header>
        <!-- 访问历史 Tab 页签栏 -->
        <div v-if="visitedTabs.length > 0" class="tab-bar">
          <div class="tab-container">
            <div
              v-for="tab in visitedTabs"
              :key="tab.key"
              :class="['tab-item', { active: activeTab === tab.key }]"
              @click="handleTabChange(tab.key)"
            >
              <span class="tab-icon">
                <component :is="getIcon(tab.icon)" />
              </span>
              <span class="tab-label">{{ tab.label }}</span>
              <span class="tab-close" @click.stop="removeTab(tab.key)">
                <close-outlined />
              </span>
            </div>
          </div>
        </div>
        <a-layout-content style="padding: 24px; min-height: calc(100vh - 64px)">
          <router-view />
        </a-layout-content>
      </a-layout>
    </a-layout>
  </a-config-provider>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import {
  FileTextOutlined,
  ShoppingOutlined,
  DatabaseOutlined,
  BankOutlined,
  CloudServerOutlined,
  FolderOutlined,
  AppstoreOutlined,
  ApiOutlined,
  SafetyOutlined,
  AuditOutlined,
  DollarOutlined,
  SettingOutlined,
  CloseOutlined,
  ClusterOutlined,
  MonitorOutlined,
  UserOutlined,
  LockOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()

const selectedKeys = ref([])
const openKeys = ref(['system'])
const activeTab = ref('')
const visitedTabs = ref([])
const userInfo = ref(JSON.parse(localStorage.getItem('dos_user') || '{}'))

const iconMap = {
  'FileTextOutlined': FileTextOutlined,
  'ShoppingOutlined': ShoppingOutlined,
  'DatabaseOutlined': DatabaseOutlined,
  'BankOutlined': BankOutlined,
  'CloudServerOutlined': CloudServerOutlined,
  'FolderOutlined': FolderOutlined,
  'AppstoreOutlined': AppstoreOutlined,
  'ApiOutlined': ApiOutlined,
  'SafetyOutlined': SafetyOutlined,
  'AuditOutlined': AuditOutlined,
  'DollarOutlined': DollarOutlined,
  'SettingOutlined': SettingOutlined,
  'ClusterOutlined': ClusterOutlined,
  'MonitorOutlined': MonitorOutlined,
  'UserOutlined': UserOutlined,
  'LockOutlined': LockOutlined
}

const getIcon = (iconName) => iconMap[iconName] || FileTextOutlined

const menuItems = [
  { key: '/workorder', label: '工单管理', icon: 'FileTextOutlined', group: 'main' },
  { key: '/sandbox', label: '安全沙盒', icon: 'BankOutlined', group: 'main' },
  { key: '/privacy', label: '隐私计算', icon: 'LockOutlined', group: 'main' },
  { key: '/datasources', label: '数据服务', icon: 'DatabaseOutlined', group: 'main' },
  { key: '/user', label: '用户管理', icon: 'UserOutlined', group: 'system' },
  { key: '/node', label: '节点管理', icon: 'ClusterOutlined', group: 'system' },
  { key: '/ray-cluster', label: '集群管理', icon: 'CloudServerOutlined', group: 'system' },
  { key: '/strategy', label: '策略配置', icon: 'SettingOutlined', group: 'system' }
]

const handleLogout = () => {
  localStorage.removeItem('dos_token')
  localStorage.removeItem('dos_user')
  router.push('/login')
}

const handleMenuClick = ({ key }) => {
  router.push(key)
}

const handleTabChange = (key) => {
  selectedKeys.value = [key]
  router.push(key)
}

const removeTab = (targetKey) => {
  const index = visitedTabs.value.findIndex(tab => tab.key === targetKey)
  if (index === -1) return

  visitedTabs.value.splice(index, 1)

  if (activeTab.value === targetKey) {
    if (visitedTabs.value.length > 0) {
      const newIndex = Math.min(index, visitedTabs.value.length - 1)
      const newTab = visitedTabs.value[newIndex]
      activeTab.value = newTab.key
      selectedKeys.value = [newTab.key]
      router.push(newTab.key)
    } else {
      activeTab.value = ''
      selectedKeys.value = []
    }
  }
}

const findMenuItem = (path) => {
  return menuItems.find(item => item.key === path)
}

watch(
  () => route.path,
  (path) => {
    if (path === '/login') return

    selectedKeys.value = [path]

    const found = findMenuItem(path)
    if (found) {
      // 展开所属分组
      if (!openKeys.value.includes(found.group)) {
        openKeys.value.push(found.group)
      }

      // 添加到访问历史
      const existingTab = visitedTabs.value.find(tab => tab.key === path)
      if (!existingTab) {
        visitedTabs.value.push({
          key: found.key,
          label: found.label,
          icon: found.icon
        })
      }
    }

    activeTab.value = path
  },
  { immediate: true }
)
</script>

<style scoped>
.ant-layout-header {
  height: 64px;
  line-height: 64px;
}

.tab-bar {
  background: linear-gradient(to bottom, #fafafa, #f5f5f5);
  border-bottom: 1px solid #e8e8e8;
  padding: 8px 16px 0;
}

.tab-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tab-item {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 6px 6px 0 0;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
  color: #666;
  max-width: 160px;
  position: relative;
}

.tab-item:hover {
  background: #e6f7ff;
  border-color: #40a9ff;
  color: #40a9ff;
}

.tab-item.active {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #1890ff;
}

.tab-icon {
  display: flex;
  align-items: center;
  margin-right: 6px;
  font-size: 12px;
}

.tab-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tab-close {
  display: flex;
  align-items: center;
  margin-left: 6px;
  padding: 2px;
  border-radius: 3px;
  font-size: 10px;
  opacity: 0.6;
  transition: all 0.2s;
}

.tab-close:hover {
  opacity: 1;
  background: rgba(0, 0, 0, 0.1);
}

.tab-item.active .tab-close:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
