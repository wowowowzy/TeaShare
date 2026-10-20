<template>
  <div class="min-h-screen bg-background text-foreground">
    <AppNavbar v-if="showNavbar" :transparent="isHomePage" />
    <main>
      <RouterView v-slot="{ Component }">
        <component :is="Component" />
      </RouterView>
    </main>
    <AppFooter v-if="showFooter" />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppNavbar from './components/layout/AppNavbar.vue'
import AppFooter from './components/layout/AppFooter.vue'
import { useUserStore } from './stores/user'

const route = useRoute()
const userStore = useUserStore()

/** 自带独立布局的页面：订单详情（无导航，见规格 8.21） */
const FULLSCREEN_ROUTES = ['/order/detail']

/**
 * 不显示页脚的页面。
 * /quiz 是对话界面，内容高度随消息增减而变化；底部挂深色页脚会在内容较短时
 * 直接露在输入框下方，观感突兀（尤其页面缩放后内容变矮）。对话页属于「应用型」
 * 页面，去掉页脚更合适，导航栏仍保留。
 * /tradition 是 fixed 全屏画布，页脚会叠在画布下方，同样去掉。
 */
const NO_FOOTER_ROUTES = ['/quiz', '/tradition']

const isFullscreen = computed(() => FULLSCREEN_ROUTES.includes(route.path))
const showNavbar = computed(() => !isFullscreen.value)
const showFooter = computed(
  () => !isFullscreen.value && !NO_FOOTER_ROUTES.includes(route.path)
)

/** 仅首页导航透明（视频 Hero 之上） */
const isHomePage = computed(() => route.path === '/')

onMounted(async () => {
  // 已登录时拉取最新用户信息；失败静默保留本地（拦截器已处理 401 不强制登出）
  if (userStore.token) {
    try {
      await userStore.getUserShow()
    } catch {
      /* 保留本地缓存 */
    }
  }
})
</script>
