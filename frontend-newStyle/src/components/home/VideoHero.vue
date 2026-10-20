<template>
  <section
    id="top"
    class="relative flex min-h-screen items-center overflow-hidden bg-[#1C1C1C]"
  >
    <!-- 背景视频 -->
    <video
      v-if="!videoFailed"
      ref="videoEl"
      class="absolute inset-0 h-full w-full object-cover opacity-70"
      :src="videoSrc"
      :poster="posterResolved"
      autoplay
      loop
      muted
      playsinline
      @error="videoFailed = true"
      @loadeddata="loaded = true"
    />
    <!-- 视频加载失败时的静态封面兜底 -->
    <img
      v-else
      :src="posterResolved"
      alt=""
      class="absolute inset-0 h-full w-full object-cover opacity-70"
      aria-hidden="true"
    />

    <!-- 叠层：保证文字对比度达 WCAG AA -->
    <div class="absolute inset-0 bg-[#1C1C1C]/55" aria-hidden="true" />

    <!-- 内容 -->
    <div
      class="container-editorial relative z-10 py-32 transition-opacity duration-1000"
      :class="loaded || videoFailed ? 'opacity-100' : 'opacity-0'"
    >
      <p class="eyebrow text-[#F9F8F6]/50">广东 · 岭南茶文化</p>

      <h1
        class="mt-8 font-serif text-6xl leading-[0.9] tracking-tighter text-[#F9F8F6] md:text-8xl lg:text-[9rem]"
      >
        茶韵岭南<br />品味广东
      </h1>

      <p class="mt-8 max-w-md font-sans text-sm leading-relaxed text-[#F9F8F6]/70 italic md:text-base">
        一片叶子，承载着千年的文化与辛勤的汗水。
      </p>

      <!-- 三大板块入口 -->
      <div class="mt-16 grid grid-cols-1 gap-px border border-[#F9F8F6]/15 md:mt-24 md:grid-cols-3">
        <button
          v-for="(item, i) in navItems"
          :key="item.id"
          type="button"
          class="group border-b border-[#F9F8F6]/15 p-6 text-left transition-colors duration-500 last:border-b-0 hover:bg-[#F9F8F6]/[0.06] md:border-r md:border-b-0 md:p-8 md:last:border-r-0"
          @click="scrollToSection(item.id)"
        >
          <span class="font-sans text-xs tracking-[0.2em] text-[#F9F8F6]/40 uppercase">
            {{ String(i + 1).padStart(2, '0') }}
          </span>
          <span class="mt-4 block font-serif text-2xl tracking-tighter text-[#F9F8F6] md:text-3xl">
            {{ item.title }}
          </span>
          <span
            class="mt-2 block font-sans text-xs leading-relaxed text-[#F9F8F6]/60 transition-all duration-500 group-hover:italic"
          >
            {{ item.subtitle }}
          </span>
          <span
            class="mt-6 block font-sans text-xs tracking-[0.2em] text-[#F9F8F6]/40 uppercase transition-transform duration-300 group-hover:translate-x-2"
          >
            进入 →
          </span>
        </button>
      </div>
    </div>

    <!-- 播放控制 -->
    <button
      type="button"
      class="absolute right-6 bottom-6 z-10 flex h-10 w-10 items-center justify-center border border-[#F9F8F6]/40 font-sans text-xs text-[#F9F8F6]/70 transition-colors duration-300 hover:border-[#F9F8F6] hover:text-[#F9F8F6] md:right-12"
      :aria-label="muted ? '取消静音' : '静音'"
      @click="toggleMute"
    >
      {{ muted ? '🔇' : '🔊' }}
    </button>

    <!-- AI 小茗入口 -->
    <RouterLink
      to="/quiz"
      class="group absolute right-6 bottom-20 z-10 flex items-center gap-3 md:right-12 md:bottom-24"
    >
      <span class="hidden font-sans text-xs tracking-[0.15em] text-[#F9F8F6]/70 md:block">
        点击图片可以与小茗对话哦
      </span>
      <img
        :src="aiAvatar"
        alt="AI 小茗"
        class="h-12 w-12 border border-[#F9F8F6]/40 object-cover transition-transform duration-500 group-hover:scale-105"
      />
    </RouterLink>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { asset } from '@/utils/apiBase'

const navItems = [
  { id: 'tea-varieties', title: '特色茶种', subtitle: '品味岭南茶香' },
  { id: 'tea-making', title: '制茶品茗', subtitle: '传承匠心技艺' },
  { id: 'tea-history', title: '茶源历史', subtitle: '追溯千年茶路' }
]

/** 优先用 OSS 视频，失败回退本地 public 资源 */
const videoSrc = 'https://java-web-itheima-zy.oss-cn-beijing.aliyuncs.com/tea-plantation.mp4'
const posterSrc = '/assest/tea-poster.jpg'
const posterResolved = computed(() => asset(posterSrc))
const aiAvatar = computed(() => asset('/assest/number1.png'))

const videoEl = ref(null)
const videoFailed = ref(false)
const loaded = ref(false)
const muted = ref(true)

function toggleMute() {
  muted.value = !muted.value
  if (videoEl.value) videoEl.value.muted = muted.value
}

function scrollToSection(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
}
</script>
