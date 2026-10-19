import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

// 直接导入路由组件
import Layout from '../components/Layout.vue'
import Login from '../views/login/index.vue'
import Dashboard from '../views/dashboard/index.vue'
import Users from '../views/users/index.vue'
import Products from '../views/products/index.vue'
import Orders from '../views/orders/index.vue'
import FlashSales from '../views/seckill/index.vue'
import ActivityLog from '../views/log/index.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/',
      name: 'Layout',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: Dashboard
        },
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: Dashboard
        },
        {
          path: 'users',
          name: 'Users',
          component: Users
        },
        {
          path: 'products',
          name: 'Products',
          component: Products
        },
        {
          path: 'orders',
          name: 'Orders',
          component: Orders
        },
        {
          path: 'seckill',
          name: 'Seckill',
          component: FlashSales
        },
        {
          path: 'log',
          name: 'Log',
          component: ActivityLog
        },
      ]
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 初始化用户状态（从本地存储加载）
  if (!userStore.token) {
    userStore.initUserState()
  }
  
  // 检查路由是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 检查用户是否已登录
    if (!userStore.isLoggedIn()) {
      // 未登录，重定向到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else {
      // 已登录，继续访问
      next()
    }
  } else {
    // 不需要认证的路由，直接访问
    next()
  }
})

export default router
