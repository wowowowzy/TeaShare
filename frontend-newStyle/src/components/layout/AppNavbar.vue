<template>
  <header
    class="fixed top-0 left-0 z-50 w-full transition-colors duration-500"
    :class="
      solid
        ? 'border-b border-[#1C1C1C]/10 bg-[#F9F8F6]/90 backdrop-blur-sm'
        : 'bg-transparent'
    "
  >
    <nav
      class="mx-auto flex h-16 max-w-7xl items-center justify-between px-6 md:h-20 md:px-12"
      aria-label="主导航"
    >
      <!-- Logo -->
      <RouterLink
        to="/"
        class="font-serif text-base tracking-[0.3em] uppercase md:text-lg"
        :class="solid ? 'text-[#1C1C1C]' : 'text-[#F9F8F6]'"
      >
        茗承古韵
      </RouterLink>

      <!-- 桌面菜单 -->
      <ul class="hidden items-center gap-8 lg:flex">
        <li v-for="item in navItems" :key="item.to">
          <RouterLink
            :to="item.to"
            class="link-underline font-sans text-xs tracking-[0.2em] uppercase transition-colors duration-300"
            :class="solid ? 'text-[#1C1C1C]/60 hover:text-[#1C1C1C]' : 'text-[#F9F8F6]/70 hover:text-[#F9F8F6]'"
          >
            {{ item.label }}
          </RouterLink>
        </li>
      </ul>

      <!-- 用户区 -->
      <div class="flex items-center gap-4">
        <template v-if="userStore.isLoggedIn">
          <div ref="dropdownRef" class="relative">
            <button
              type="button"
              class="flex items-center gap-2 font-sans text-xs tracking-[0.15em] uppercase transition-colors duration-300"
              :class="solid ? 'text-[#1C1C1C]/70 hover:text-[#1C1C1C]' : 'text-[#F9F8F6]/80 hover:text-[#F9F8F6]'"
              :aria-expanded="showDropdown"
              @click="showDropdown = !showDropdown"
            >
              <img
                :src="avatarUrl"
                :alt="userStore.userName"
                class="h-7 w-7 border border-current object-cover"
                @error="onAvatarError"
              />
              <span class="hidden md:inline">{{ userStore.userName || '茶友' }}</span>
              <span aria-hidden="true">▾</span>
            </button>

            <Transition
              enter-active-class="transition duration-200 ease-out"
              enter-from-class="opacity-0 -translate-y-1"
              leave-active-class="transition duration-150 ease-in"
              leave-to-class="opacity-0 -translate-y-1"
            >
              <ul
                v-if="showDropdown"
                class="absolute right-0 mt-3 w-44 border border-[#1C1C1C]/10 bg-[#F9F8F6] py-2"
              >
                <li v-for="item in userMenu" :key="item.label">
                  <button
                    type="button"
                    class="block w-full px-4 py-2 text-left font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/60 uppercase transition-colors duration-200 hover:bg-[#1C1C1C]/[0.03] hover:text-[#1C1C1C]"
                    @click="item.action"
                  >
                    {{ item.label }}
                  </button>
                </li>
              </ul>
            </Transition>
          </div>
        </template>

        <RouterLink
          v-else
          to="/login"
          class="link-underline font-sans text-xs tracking-[0.2em] uppercase transition-colors duration-300"
          :class="solid ? 'text-[#1C1C1C]/60 hover:text-[#1C1C1C]' : 'text-[#F9F8F6]/80 hover:text-[#F9F8F6]'"
        >
          登录
        </RouterLink>

        <!-- 移动端汉堡 -->
        <button
          type="button"
          class="flex h-8 w-8 flex-col items-center justify-center gap-1.5 lg:hidden"
          :aria-expanded="showMobile"
          aria-label="打开菜单"
          @click="showMobile = !showMobile"
        >
          <span
            class="block h-px w-6 transition-transform duration-300"
            :class="[
              solid ? 'bg-[#1C1C1C]' : 'bg-[#F9F8F6]',
              showMobile ? 'translate-y-[3.5px] rotate-45' : ''
            ]"
          />
          <span
            class="block h-px w-6 transition-transform duration-300"
            :class="[
              solid ? 'bg-[#1C1C1C]' : 'bg-[#F9F8F6]',
              showMobile ? '-translate-y-[3.5px] -rotate-45' : ''
            ]"
          />
        </button>
      </div>
    </nav>

    <!-- 移动端抽屉 -->
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="opacity-0 -translate-y-2"
      leave-active-class="transition duration-200 ease-in"
      leave-to-class="opacity-0 -translate-y-2"
    >
      <div
        v-if="showMobile"
        class="border-t border-[#1C1C1C]/10 bg-[#F9F8F6] lg:hidden"
      >
        <ul class="container-editorial py-6">
          <li v-for="item in navItems" :key="item.to" class="border-b border-[#1C1C1C]/10 last:border-0">
            <RouterLink
              :to="item.to"
              class="block py-4 font-serif text-xl tracking-tighter text-[#1C1C1C]"
              @click="showMobile = false"
            >
              {{ item.label }}
            </RouterLink>
          </li>
          <template v-if="userStore.isLoggedIn">
            <li v-for="item in userMenu" :key="item.label" class="border-b border-[#1C1C1C]/10 last:border-0">
              <button
                type="button"
                class="block w-full py-4 text-left font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/60 uppercase"
                @click="item.action(); showMobile = false"
              >
                {{ item.label }}
              </button>
            </li>
          </template>
        </ul>
      </div>
    </Transition>
  </header>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { asset } from '@/utils/apiBase'

const props = defineProps({
  transparent: { type: Boolean, default: false }
})

const router = useRouter()
const userStore = useUserStore()

const navItems = [
  { label: '首页', to: '/' },
  { label: '名师讲解', to: '/master' },
  { label: '3D 茶具', to: '/tradition' },
  { label: '茶友社', to: '/community' },
  { label: 'AI 小茗', to: '/quiz' },
  { label: '茶文化商城', to: '/shop' }
]

const showDropdown = ref(false)
const showMobile = ref(false)
const scrolled = ref(false)
const dropdownRef = ref(null)
const avatarError = ref(false)

/** 首页视频 Hero 之上为透明导航，滚动 50px 后转实底 */
const solid = computed(() => !props.transparent || scrolled.value)

const avatarUrl = computed(() => {
  if (avatarError.value || !userStore.avatar) return asset('/assest/default.jpg')
  const ts = localStorage.getItem('avatarTimestamp')
  const sep = userStore.avatar.includes('?') ? '&' : '?'
  return asset(userStore.avatar) + (ts ? `${sep}t=${ts}` : '')
})

const userMenu = [
  { label: '个人中心', action: () => router.push('/profile') },
  { label: '我的帖子', action: () => router.push('/community/my-posts') },
  { label: '购物车', action: () => router.push('/cart') },
  { label: '退出登录', action: handleLogout }
]

function onAvatarError() {
  avatarError.value = true
}

async function handleLogout() {
  showDropdown.value = false
  await userStore.logout()
  router.push('/login')
}

function onScroll() {
  scrolled.value = window.scrollY > 50
}

function onClickOutside(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

onMounted(() => {
  if (props.transparent) {
    window.addEventListener('scroll', onScroll, { passive: true })
    onScroll()
  }
  document.addEventListener('click', onClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  document.removeEventListener('click', onClickOutside)
})
</script>
