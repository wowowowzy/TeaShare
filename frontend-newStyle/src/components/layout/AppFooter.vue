<template>
  <footer class="border-t border-[#1C1C1C]/10 bg-[#1C1C1C] px-6 py-16 text-[#F9F8F6] md:px-12 md:py-24">
    <div class="mx-auto max-w-7xl">
      <div class="grid grid-cols-1 gap-12 md:grid-cols-12">
        <div class="md:col-span-5">
          <p class="font-serif text-2xl tracking-[0.3em] uppercase md:text-3xl">茗承古韵</p>
          <p class="mt-6 max-w-xs font-sans text-sm leading-relaxed text-[#F9F8F6]/60 italic">
            一片叶子，承载着千年的文化与辛勤的汗水。
          </p>
        </div>

        <div class="md:col-span-3">
          <p class="eyebrow text-[#F9F8F6]/40">探索</p>
          <ul class="mt-6 space-y-3">
            <li v-for="item in explore" :key="item.to">
              <RouterLink
                :to="item.to"
                class="link-underline font-sans text-xs tracking-[0.15em] text-[#F9F8F6]/70 uppercase transition-colors duration-300 hover:text-[#F9F8F6]"
              >
                {{ item.label }}
              </RouterLink>
            </li>
          </ul>
        </div>

        <div class="md:col-span-4">
          <p class="eyebrow text-[#F9F8F6]/40">商城</p>
          <ul class="mt-6 space-y-3">
            <li v-for="item in shopping" :key="item.to">
              <RouterLink
                :to="item.to"
                class="link-underline font-sans text-xs tracking-[0.15em] text-[#F9F8F6]/70 uppercase transition-colors duration-300 hover:text-[#F9F8F6]"
              >
                {{ item.label }}
              </RouterLink>
            </li>
          </ul>
        </div>
      </div>

      <hr class="mt-16 border-[#F9F8F6]/10" />

      <div
        class="mt-8 flex flex-col gap-3 font-sans text-xs tracking-[0.15em] text-[#F9F8F6]/40 uppercase md:flex-row md:items-center md:justify-between"
      >
        <span>© {{ year }} 茗承古韵 · 岭南茶文化</span>
        <span>AI 内容仅供参考</span>
      </div>
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()

const explore = [
  { label: '特色茶种', to: '/#tea-varieties' },
  { label: '制茶品茗', to: '/#tea-making' },
  { label: '茶源历史', to: '/#tea-history' },
  { label: '3D 茶具', to: '/tradition' },
  { label: '名师讲解', to: '/master' }
]

const shopping = [
  { label: '茶文化商城', to: '/shop' },
  { label: '购物车', to: '/cart' },
  { label: '茶友社', to: '/community' },
  { label: 'AI 小茗', to: '/quiz' }
]
</script>
