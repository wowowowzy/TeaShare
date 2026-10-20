import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus, { ElMessage } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import './styles/main.css'
import { onUnauthorized } from './utils/authBus'
import { useUserStore } from './stores/user'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 注册 401 处理器：清除登录态并跳转登录页（避免 request.js 与 store/router 循环依赖）
onUnauthorized(() => {
  const userStore = useUserStore()
  userStore.clearUserInfo()
  ElMessage.error('用户登录已过期')
  router.push('/login')
})

app.mount('#app')
