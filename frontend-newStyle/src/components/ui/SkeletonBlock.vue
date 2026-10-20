<template>
  <div class="space-y-6" :style="{ '--rows': rows }">
    <div
      v-for="i in rows"
      :key="i"
      class="skeleton h-4 w-full"
      :style="{ width: `${100 - (i % 3) * 12}%` }"
    />
  </div>
</template>

<script setup>
defineProps({
  rows: { type: Number, default: 3 }
})
</script>
