<template>
  <span class="eyebrow" :class="toneClass">
    <slot />
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** muted | strong */
  tone: { type: String, default: 'muted' }
})

const toneClass = computed(() => (props.tone === 'strong' ? 'text-foreground' : ''))
</script>
