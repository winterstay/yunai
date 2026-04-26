<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Menu, Avatar, Button, Space, Dropdown, message } from 'ant-design-vue'
import { LogoutOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController'
import { RouterLink, useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const selectedKeys = ref(['home'])

const menuItems = [
  {
    key: 'home',
    label: '首页',
    path: '/'
  },
  {
    key: 'about',
    label: '关于',
    path: '/about'
  }
]

const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key === 'home') {
    router.push('/')
  } else if (key === 'about') {
    router.push('/about')
  }
}

// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  if (to.path === '/') {
    selectedKeys.value = ['home']
  } else if (to.path === '/about') {
    selectedKeys.value = ['about']
  }
})

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<template>
  <div class="global-header">
    <div class="header-left">
      <img src="@/assets/logo.svg" alt="Logo" class="logo" />
      <h1 class="site-title">YunAI</h1>
      <Menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        class="header-menu"
        @click="handleMenuClick"
      >
        <Menu.Item v-for="item in menuItems" :key="item.key">
          <RouterLink :to="item.path">{{ item.label }}</RouterLink>
        </Menu.Item>
      </Menu>
    </div>
    
    <div class="header-right">
      <div class="user-login-status">
        <div v-if="loginUserStore.loginUser.id">
          <Dropdown>
            <Space>
              <Avatar :src="loginUserStore.loginUser.userAvatar" />
              {{ loginUserStore.loginUser.userName ?? '无名' }}
            </Space>
            <template #overlay>
              <Menu>
                <Menu.Item @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </Menu.Item>
              </Menu>
            </template>
          </Dropdown>
        </div>
        <div v-else>
          <Button type="primary" href="/user/login">登录</Button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.global-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
}

.logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.site-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #1890ff;
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
  line-height: 64px;
}

.header-menu :deep(.ant-menu-item) {
  height: 64px;
  line-height: 64px;
  border-bottom: 2px solid transparent;
}

.header-menu :deep(.ant-menu-item:hover),
.header-menu :deep(.ant-menu-item-selected) {
  border-bottom-color: #1890ff;
}

.header-menu :deep(.ant-menu-item a) {
  color: inherit;
  text-decoration: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  background: #1890ff;
}

.user-name {
  font-size: 14px;
  color: #333;
}

@media (max-width: 768px) {
  .global-header {
    padding: 0 16px;
  }
  
  .header-left {
    gap: 12px;
  }
  
  .site-title {
    font-size: 16px;
  }
  
  .header-menu {
    display: none;
  }
  
  .user-name {
    display: none;
  }
}
</style>
