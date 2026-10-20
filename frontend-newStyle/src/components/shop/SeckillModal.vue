<template>
  <div class="fixed inset-0 z-[70] flex items-center justify-center bg-[#1C1C1C]/50 p-6" @click.self="$emit('close')">
    <div class="flex max-h-[90vh] w-full max-w-3xl flex-col border border-[#1C1C1C]/15 bg-[#F9F8F6]">
      <header class="flex items-center justify-between border-b border-[#1C1C1C]/10 px-8 py-6">
        <div>
          <p class="font-serif text-2xl tracking-tighter">秒杀商品</p>
          <p class="mt-1 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
            特价商品限量购买
          </p>
        </div>
        <button
          type="button"
          class="font-sans text-lg text-[#1C1C1C]/60 transition-colors duration-300 hover:text-[#1C1C1C]"
          aria-label="关闭"
          @click="$emit('close')"
        >
          ✕
        </button>
      </header>

      <div class="scroll-thin flex-1 overflow-y-auto p-8">
        <SkeletonBlock v-if="loading" :rows="6" />

        <EmptyState v-else-if="!goods.length" title="暂无秒杀商品" />

        <div v-else class="grid grid-cols-1 gap-px border border-[#1C1C1C]/10 sm:grid-cols-2">
          <div
            v-for="g in goods"
            :key="g.goodsId"
            class="border-b border-[#1C1C1C]/10 p-6 transition-colors duration-300 hover:bg-[#1C1C1C]/[0.02] sm:border-r"
          >
            <div class="relative">
              <SmartImage
                :src="g.goodsImage"
                :alt="g.goodsName"
                class="aspect-square w-full border border-[#1C1C1C]/10"
              />
              <span
                class="absolute top-3 left-3 border border-[#1C1C1C] bg-[#F9F8F6] px-2 py-0.5 font-sans text-xs tracking-[0.15em] uppercase"
              >
                秒杀
              </span>
            </div>
            <p class="mt-4 font-serif text-lg tracking-tight">{{ g.goodsName }}</p>
            <div class="mt-2 flex items-baseline gap-3">
              <span class="font-serif text-xl tracking-tight">¥{{ Number(g.goodsPrice).toFixed(2) }}</span>
              <span class="font-sans text-xs text-[#1C1C1C]/40 line-through">
                ¥{{ (Number(g.goodsPrice) * 1.5).toFixed(2) }}
              </span>
            </div>
            <p class="mt-2 font-sans text-xs text-[#1C1C1C]/50">
              剩余库存量：{{ g.stockNum ?? 0 }}
            </p>
            <button
              type="button"
              class="btn-editorial mt-4 w-full !py-2 text-xs"
              :disabled="(g.stockNum ?? 0) <= 0"
              @click="buy(g)"
            >
              {{ (g.stockNum ?? 0) > 0 ? '立即抢购' : '已售罄' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { getSeckillGoods } from '@/api/goods'

const emit = defineEmits(['close'])

const goods = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    const res = await getSeckillGoods()
    goods.value = Array.isArray(res) ? res : []
  } catch {
    goods.value = []
  } finally {
    loading.value = false
  }
}

function buy(g) {
  emit('close')
  window.open(
    `${import.meta.env.BASE_URL}seckill/${g.goodsId}`,
    '_blank',
    'noopener'
  )
}

onMounted(load)
</script>
