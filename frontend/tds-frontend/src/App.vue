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
          可信数据空间
        </div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          v-model:openKeys="openKeys"
          theme="dark"
          mode="inline"
          @click="handleMenuClick"
          v-if="!menuLoading"
        >
          <a-sub-menu v-for="group in menuGroups" :key="group.key">
            <template #title>
              <span>
                <component :is="getGroupIcon(group.icon)" />
                <span>{{ group.label }}</span>
              </span>
            </template>
            <a-menu-item v-for="item in group.children" :key="item.key">
              {{ item.label }}
            </a-menu-item>
          </a-sub-menu>
        </a-menu>
        <div v-else style="padding: 20px; text-align: center; color: #666">
          加载中...
        </div>
      </a-layout-sider>
      <a-layout style="margin-left: 220px">
        <a-layout-header style="background: #001529; padding: 0 24px; display: flex; align-items: center; justify-content: space-between">
          <div style="color: white; font-size: 16px">平台管理系统</div>
          <div style="display: flex; align-items: center; color: white;">
            <span style="margin-right: 16px">{{ userInfo.realName || userInfo.username }}</span>
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
  ShoppingCartOutlined,
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
  UserOutlined
} from '@ant-design/icons-vue'
import menuApi from '@/api/menu'

const router = useRouter()
const route = useRoute()

const selectedKeys = ref([])
const openKeys = ref([])
const activeTab = ref('')
const visitedTabs = ref([])
const menuLoading = ref(true)
const menuGroups = ref([])
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

const iconMap = {
  'FileTextOutlined': FileTextOutlined,
  'ShoppingCartOutlined': ShoppingCartOutlined,
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
  'UserOutlined': UserOutlined
}

const getIcon = (iconName) => iconMap[iconName] || FileTextOutlined
const getGroupIcon = (iconName) => iconMap[iconName] || ClusterOutlined

// 默认菜单（API失败时的兜底）
const defaultMenuGroups = [
  {
    key: 'g1',
    label: '数据空间',
    icon: 'ClusterOutlined',
    children: [
      { key: '/dataspace', label: '数据空间', icon: 'DatabaseOutlined' },
      { key: '/organization', label: '机构管理', icon: 'BankOutlined' }
    ]
  },
  {
    key: 'g2',
    label: '数据资源',
    icon: 'CloudServerOutlined',
    children: [
      { key: '/datasource', label: '数据源', icon: 'CloudServerOutlined' },
      { key: '/catalog', label: '资源目录', icon: 'FolderOutlined' },
      { key: '/product', label: '数据产品', icon: 'AppstoreOutlined' }
    ]
  },
  {
    key: 'g3',
    label: '交易管理',
    icon: 'ShoppingCartOutlined',
    children: [
      { key: '/contract', label: '数字合约', icon: 'FileTextOutlined' },
      { key: '/order', label: '交易订单', icon: 'ShoppingCartOutlined' }
    ]
  },
  {
    key: 'g4',
    label: '运维管理',
    icon: 'MonitorOutlined',
    children: [
      { key: '/connector', label: '连接器', icon: 'ApiOutlined' },
      { key: '/policy', label: '策略管理', icon: 'SafetyOutlined' },
      { key: '/evidence', label: '审计存证', icon: 'AuditOutlined' }
    ]
  },
  {
    key: 'g5',
    label: '系统管理',
    icon: 'SettingOutlined',
    children: [
      { key: '/billing', label: '计量计费', icon: 'DollarOutlined' },
      { key: '/system', label: '系统配置', icon: 'SettingOutlined' }
    ]
  },
  {
    key: 'g6',
    label: '用户中心',
    icon: 'UserOutlined',
    children: [
      { key: '/user', label: '用户管理', icon: 'UserOutlined' },
      { key: '/role', label: '角色管理', icon: 'SafetyOutlined' }
    ]
  }
]

// 加载菜单
const loadMenus = async () => {
  try {
    const res = await menuApi.getMenuTree()
    console.log('菜单数据:', res)
    if (res.code === 200 && res.data && res.data.length > 0) {
      // 转换为 Ant Design Menu 格式
      menuGroups.value = res.data
        .filter(group => group.fMenuType === 1)
        .map(group => ({
          key: group.fId,
          label: group.fMenuName,
          icon: group.fIcon,
          children: (group.children || [])
            .filter(menu => menu.fMenuType === 2)
            .map(menu => ({
              key: menu.fPath,
              label: menu.fMenuName,
              icon: menu.fIcon,
              fId: menu.fId
            }))
        }))
    } else {
      console.warn('菜单数据为空，使用默认菜单')
      menuGroups.value = defaultMenuGroups
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
    menuGroups.value = defaultMenuGroups
  } finally {
    menuLoading.value = false
  }
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

// 查找菜单项
const findMenuItem = (path) => {
  for (const group of menuGroups.value) {
    const item = group.children.find(child => child.key === path)
    if (item) {
      return { groupKey: group.key, item }
    }
  }
  return null
}

watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]

    // 展开当前菜单所属的分组
    const found = findMenuItem(path)
    if (found && !openKeys.value.includes(found.groupKey)) {
      openKeys.value.push(found.groupKey)
    }

    const existingTab = visitedTabs.value.find(tab => tab.key === path)
    if (!existingTab) {
      if (found) {
        visitedTabs.value.push({
          key: found.item.key,
          label: found.item.label,
          icon: found.item.icon
        })
      }
    }

    activeTab.value = path
  },
  { immediate: true }
)

onMounted(() => {
  loadMenus()
})
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
