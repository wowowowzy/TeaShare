<template>
  <div class="flex flex-col items-center justify-center gap-6 py-24 text-center">
    <p class="font-serif text-2xl tracking-tighter text-[#1C1C1C]/60 md:text-3xl">
      {{ title }}
    </p>
    <p v-if="description" class="max-w-xs font-sans text-sm leading-relaxed text-[#1C1C1C]/40">
      {{ description }}
    </p>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '暂无内容' },
  description: { type: String, default: '' }
})
</script>
