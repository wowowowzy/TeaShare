<template>
  <nav
    class="fixed top-1/2 left-6 z-40 hidden -translate-y-1/2 lg:block"
    aria-label="页面锚点导航"
  >
    <ul class="border border-[#1C1C1C]/10 bg-[#F9F8F6]/80 py-2 backdrop-blur-sm">
      <li v-for="item in items" :key="item.id">
        <button
          type="button"
          class="group flex w-full items-center gap-3 px-4 py-3 text-left transition-colors duration-300"
          @click="go(item.id)"
        >
          <span
            class="block h-px transition-all duration-500"
            :class="
              active === item.id
                ? 'w-6 bg-[#1C1C1C]'
                : 'w-3 bg-[#1C1C1C]/30 group-hover:w-5 group-hover:bg-[#1C1C1C]/60'
            "
          />
          <span
            class="font-sans text-xs tracking-[0.2em] uppercase transition-colors duration-300"
            :class="active === item.id ? 'text-[#1C1C1C]' : 'text-[#1C1C1C]/40 group-hover:text-[#1C1C1C]/70'"
          >
            {{ item.label }}
          </span>
        </button>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

const items = [
  { id: 'top', label: '置顶' },
  { id: 'tea-varieties', label: '特色茶种' },
  { id: 'tea-making', label: '制茶品茗' },
  { id: 'tea-history', label: '茶源历史' }
]

const active = ref('top')
let timer = null

function go(id) {
  if (id === 'top') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } else {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
  }
}

function onScroll() {
  if (timer) return
  timer = setTimeout(() => {
    timer = null
    const mid = window.innerHeight / 2
    let current = 'top'
    items.slice(1).forEach(({ id }) => {
      const el = document.getElementById(id)
      if (el) {
        const rect = el.getBoundingClientRect()
        if (rect.top <= mid && rect.bottom >= mid) current = id
      }
    })
    active.value = current
  }, 150)
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  if (timer) clearTimeout(timer)
})
</script>
