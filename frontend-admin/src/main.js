import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs' // 导入中文语言包
import 'element-plus/dist/index.css'

console.log('Main.js loaded')
console.log('Router:', router)

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn, // 设置中文语言包
})

console.log('App created')

app.mount('#app')

console.log('App mounted')
