<template>
  <div class="group border border-[#1C1C1C]/10 transition-colors duration-300 hover:border-[#1C1C1C]">
    <!-- 图片：固定高度 200px（与原版一致），避免方形大图占满视口 -->
    <div class="h-[200px] overflow-hidden border-b border-[#1C1C1C]/10">
      <SmartImage
        :src="product.goodsImage"
        :alt="product.goodsName"
        :fallback="'/assest/default.jpg'"
        img-class="transition-transform duration-500 group-hover:scale-105"
        class="h-full w-full"
      />
    </div>

    <div class="p-4">
      <h3 class="font-serif text-base leading-snug tracking-tight transition-all duration-500 group-hover:italic">
        {{ product.goodsName }}
      </h3>

      <p class="mt-2 font-serif text-lg tracking-tight">¥{{ formatPrice(product.goodsPrice) }}</p>

      <p class="mt-1 font-sans text-xs text-[#1C1C1C]/50">
        {{ categoryName }} · 销售数量：{{ product.quantity || 0 }}
      </p>

      <div class="mt-3 flex gap-2">
        <button
          type="button"
          class="btn-editorial-ghost flex-1 !px-3 !py-1.5 text-xs"
          @click="$emit('view', product)"
        >
          查看详情
        </button>
        <button
          type="button"
          class="btn-editorial flex-1 !px-3 !py-1.5 text-xs"
          @click="$emit('add-to-cart', product)"
        >
          加入购物车
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import { shop } from '@/content'

const props = defineProps({
  product: { type: Object, required: true }
})
defineEmits(['add-to-cart', 'view'])

const categoryName = computed(
  () => shop.categories.find((c) => String(c.id) === String(props.product.categoryId))?.name || '茶叶'
)

function formatPrice(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}
</script>
