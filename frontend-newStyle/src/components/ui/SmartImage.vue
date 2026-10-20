<template>
  <!--
    尺寸由调用方通过 class 控制（如 h-10 w-10 / aspect-square w-full）。
    根节点不预设 h-full/w-full，否则会与调用方的尺寸类冲突
    （同属 utilities 层，胜负取决于 CSS 源码顺序而非 class 书写顺序）。
  -->
  <div class="zoom-frame relative overflow-hidden bg-[#1C1C1C]/[0.04]">
    <img
      v-if="!failed && resolvedSrc"
      :src="resolvedSrc"
      :alt="alt"
      :loading="loading"
      class="h-full w-full object-cover"
      :class="imgClass"
      @error="onError"
    />
    <!--
      兜底占位：只画一个中性方块，不输出 alt 文案。
      头像等场景若把 alt 设为用户昵称，直接渲染会变成一条写着用户名的灰条。
    -->
    <div v-else class="flex h-full w-full items-center justify-center" aria-hidden="true">
      <span class="block h-1/4 w-1/4 border border-[#1C1C1C]/15" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { asset } from '@/utils/apiBase'

const props = defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '' },
  /** 加载失败时回退的图片；传空字符串则不回退，直接显示占位方块 */
  fallback: { type: String, default: '' },
  loading: { type: String, default: 'lazy' },
  imgClass: { type: String, default: '' }
})

const failed = ref(false)
const useFallback = ref(false)

/** 把站点根路径拼上 base；外链原样返回 */
const currentSrc = computed(() => (useFallback.value ? props.fallback : props.src))
const resolvedSrc = computed(() => asset(currentSrc.value))

function onError() {
  // 首次失败 → 尝试回退图；回退也失败（或未配置）→ 显示占位方块
  if (props.fallback && !useFallback.value) {
    useFallback.value = true
  } else {
    failed.value = true
  }
}

watch(
  () => props.src,
  () => {
    failed.value = false
    useFallback.value = false
  }
)
</script>
