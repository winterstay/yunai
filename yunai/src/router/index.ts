import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import UserLoginPage from '../pages/user/UserLoginPage.vue'
import UserRegisterPage from '../pages/user/UserRegisterPage.vue'
import UserManagePage from '../pages/admin/UserManagePage.vue'
import AppManagePage from '../pages/admin/AppManagePage.vue'
import AppChatPage from '../pages/app/AppChatPage.vue'
import AppEditPage from '../pages/app/AppEditPage.vue'
import { useLoginUserStore } from '@/stores/loginUser'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: AppManagePage,
    },
    {
      path: '/app/chat/:id',
      name: '应用对话',
      component: AppChatPage,
    },
    {
      path: '/app/edit/:id',
      name: '应用编辑',
      component: AppEditPage,
    },
  ],

})

router.beforeEach(async (to) => {
  const loginUserStore = useLoginUserStore()
  if (!loginUserStore.loginUser.id && to.path !== '/user/login' && to.path !== '/user/register') {
    await loginUserStore.fetchLoginUser()
  }
  if (to.path.startsWith('/admin') && loginUserStore.loginUser.userRole !== 'admin') {
    return '/'
  }
  if ((to.path.startsWith('/app/chat/') || to.path.startsWith('/app/edit/')) && !loginUserStore.loginUser.id) {
    return `/user/login?redirect=${encodeURIComponent(window.location.href)}`
  }
  return true
})

export default router
