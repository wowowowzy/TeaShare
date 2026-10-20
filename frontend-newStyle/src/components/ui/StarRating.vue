<template>
  <div class="flex items-center gap-1" :aria-label="`评分 ${modelValue} 分，共 ${max} 分`">
    <button
      v-for="n in max"
      :key="n"
      type="button"
      class="p-0.5 font-sans text-lg leading-none transition-colors duration-200"
      :class="n <= modelValue ? 'text-[#1C1C1C]' : 'text-[#1C1C1C]/20'"
      :disabled="readonly"
      :aria-label="`${n} 分`"
      @click="!readonly && $emit('update:modelValue', n)"
      @mouseenter="!readonly && (hover = n)"
      @mouseleave="hover = 0"
    >
      {{ n <= (hover || modelValue) ? '★' : '☆' }}
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  modelValue: { type: Number, default: 0 },
  max: { type: Number, default: 5 },
  readonly: { type: Boolean, default: false }
})
defineEmits(['update:modelValue'])

const hover = ref(0)
</script>
