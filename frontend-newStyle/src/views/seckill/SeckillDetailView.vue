<template>
  <div class="pt-20 md:pt-28">
    <div class="container-editorial py-16 md:py-24">
      <SkeletonBlock v-if="loading" :rows="8" />

      <EmptyState v-else-if="!product" title="秒杀商品不存在" description="该商品可能已结束秒杀。">
        <RouterLink to="/shop" class="btn-editorial-ghost">返回商城</RouterLink>
      </EmptyState>

      <template v-else>
        <nav class="mb-12 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
          <RouterLink to="/shop" class="link-underline">商城</RouterLink>
          <span class="mx-3">/</span>
          <span>秒杀</span>
        </nav>

        <div class="grid grid-cols-1 gap-16 md:grid-cols-12 md:gap-24">
          <div class="md:col-span-6">
            <div class="group relative">
              <SmartImage
                :src="product.goodsImage"
                :alt="product.goodsName"
                loading="eager"
                class="aspect-square w-full border border-[#1C1C1C]/10"
              />
              <span
                class="absolute top-6 left-6 border border-[#1C1C1C] bg-[#F9F8F6] px-4 py-1.5 font-sans text-xs tracking-[0.2em] uppercase"
              >
                秒杀商品
              </span>
            </div>
          </div>

          <div class="md:col-span-6">
            <h1 class="font-serif text-4xl leading-tight tracking-tighter md:text-5xl">
              {{ product.goodsName }}
            </h1>

            <div class="mt-8 flex items-baseline gap-4">
              <p class="font-serif text-4xl tracking-tight md:text-5xl">
                ¥{{ formatPrice(product.goodsPrice) }}
              </p>
              <p class="font-sans text-sm text-[#1C1C1C]/40 line-through">
                ¥{{ (Number(product.goodsPrice) * 1.5).toFixed(2) }}
              </p>
            </div>

            <p class="mt-8 max-w-md font-sans text-sm leading-relaxed text-[#1C1C1C]/70">
              {{ product.goodsIntro || '限时特价，数量有限，售完即止。' }}
            </p>

            <dl class="mt-10 grid grid-cols-2 gap-px border border-[#1C1C1C]/10">
              <div class="border-r border-[#1C1C1C]/10 p-5">
                <dt class="eyebrow">剩余库存量</dt>
                <dd class="mt-2 font-sans text-sm">{{ stock }}</dd>
              </div>
              <div class="p-5">
                <dt class="eyebrow">购买数量</dt>
                <dd class="mt-2 font-sans text-sm">1</dd>
              </div>
            </dl>

            <button
              type="button"
              class="btn-editorial mt-10 w-full md:w-auto"
              :disabled="stock <= 0"
              @click="buyNow"
            >
              {{ stock > 0 ? '立即抢购' : '已售罄' }}
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SmartImage from '@/components/ui/SmartImage.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { findGoodById } from '@/api/goods'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)

const stock = computed(() => Number(product.value?.stockNum ?? 0))

function formatPrice(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

async function load() {
  loading.value = true
  try {
    product.value = (await findGoodById(route.params.id)) || null
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

async function buyNow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  // 下单前再次校验实时库存
  try {
    const fresh = await findGoodById(route.params.id)
    if (!fresh || Number(fresh.stockNum) <= 0) {
      ElMessage.error('商品已售罄')
      product.value = fresh || product.value
      return
    }
    router.push({
      path: '/payment',
      query: {
        seckill: 'true',
        goodsId: fresh.goodsId,
        goodsName: fresh.goodsName,
        goodsPrice: fresh.goodsPrice,
        goodsImage: fresh.goodsImage,
        quantity: 1
      }
    })
  } catch {
    /* 拦截器已提示 */
  }
}

onMounted(load)
</script>
