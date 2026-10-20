<template>
  <div class="pt-20 md:pt-24">
    <div class="mx-auto w-full max-w-[1200px] px-5 py-10">
      <SkeletonBlock v-if="loading" :rows="8" />

      <EmptyState
        v-else-if="!product"
        title="商品不存在"
        description="该商品可能已下架。"
      >
        <RouterLink to="/shop" class="btn-editorial-ghost !px-4 !py-2 text-xs">返回商城</RouterLink>
      </EmptyState>

      <template v-else>
        <nav class="mb-6 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
          <RouterLink to="/shop" class="link-underline">商城</RouterLink>
          <span class="mx-3">/</span>
          <span>{{ product.goodsName }}</span>
        </nav>

        <div class="grid grid-cols-1 gap-10 md:grid-cols-2">
          <!-- 左：图片 -->
          <div>
            <div class="group">
              <SmartImage
                :src="product.goodsImage"
                :alt="product.goodsName"
                loading="eager"
                class="aspect-square w-full border border-[#1C1C1C]/10"
              />
            </div>
          </div>

          <!-- 右：信息 -->
          <div>
            <h1 class="font-serif text-2xl leading-snug tracking-tight md:text-3xl">
              {{ product.goodsName }}
            </h1>

            <p class="mt-5 font-serif text-2xl tracking-tight md:text-3xl">
              ¥{{ formatPrice(product.goodsPrice) }}
            </p>

            <p class="mt-4 max-w-md font-sans text-sm leading-relaxed text-[#1C1C1C]/70">
              {{ product.goodsIntro || '精选原产地茶叶，传统工艺制作，滋味醇厚。' }}
            </p>

            <dl class="mt-6 grid grid-cols-2 gap-px border border-[#1C1C1C]/10">
              <div class="border-r border-[#1C1C1C]/10 p-4">
                <dt class="eyebrow">库存</dt>
                <dd class="mt-1 font-sans text-sm">{{ product.stockNum ?? 0 }}</dd>
              </div>
              <div class="p-4">
                <dt class="eyebrow">已售</dt>
                <dd class="mt-1 font-sans text-sm">{{ product.quantity ?? 0 }}</dd>
              </div>
            </dl>

            <!-- 数量 -->
            <div class="mt-6 flex items-center gap-5">
              <span class="eyebrow">数量</span>
              <div class="flex items-center border border-[#1C1C1C]/15">
                <button
                  type="button"
                  class="flex h-9 w-9 items-center justify-center transition-colors duration-300 hover:bg-[#1C1C1C]/[0.04]"
                  aria-label="减少数量"
                  @click="decrease"
                >
                  −
                </button>
                <span class="w-10 text-center font-sans text-sm">{{ quantity }}</span>
                <button
                  type="button"
                  class="flex h-9 w-9 items-center justify-center transition-colors duration-300 hover:bg-[#1C1C1C]/[0.04]"
                  aria-label="增加数量"
                  @click="increase"
                >
                  +
                </button>
              </div>
            </div>

            <div class="mt-6 flex flex-wrap gap-3">
              <button
                type="button"
                class="btn-editorial !px-5 !py-2 text-xs"
                :disabled="stockLeft <= 0"
                @click="addToCart"
              >
                加入购物车
              </button>
              <button type="button" class="btn-editorial-ghost !px-5 !py-2 text-xs" @click="goCart">
                查看购物车
              </button>
              <button
                type="button"
                class="btn-editorial-ghost !px-5 !py-2 text-xs"
                :disabled="stockLeft <= 0"
                @click="buyNow"
              >
                立即购买
              </button>
            </div>

            <p v-if="stockLeft <= 0" class="mt-4 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/50 uppercase">
              已售罄
            </p>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="mt-12">
          <CommentSection :goods-id="product.goodsId" />
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
import CommentSection from '@/components/shop/CommentSection.vue'
import { findGoodById } from '@/api/goods'
import { cartApi } from '@/api/cart'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const quantity = ref(1)

const stockLeft = computed(() => Number(product.value?.stockNum ?? 0))

function formatPrice(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

function decrease() {
  if (quantity.value > 1) quantity.value--
}
function increase() {
  if (quantity.value < stockLeft.value) quantity.value++
}

async function load() {
  loading.value = true
  try {
    const res = await findGoodById(route.params.id)
    product.value = res || null
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

function requireLogin() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

async function addToCart() {
  if (!requireLogin()) return
  try {
    const cart = await cartApi.getCartList()
    const list = Array.isArray(cart) ? cart : []
    const existing = list.find((c) => String(c.goodsId) === String(product.value.goodsId))
    if (existing) {
      await cartApi.updateCartItem(product.value.goodsId, Number(existing.quantity) + quantity.value)
    } else {
      await cartApi.addToCart(product.value.goodsId, quantity.value)
    }
    ElMessage.success('已加入购物车')
  } catch {
    /* 拦截器已提示 */
  }
}

function goCart() {
  router.push('/cart')
}

function buyNow() {
  if (!requireLogin()) return
  router.push({
    path: '/payment',
    query: {
      buyNow: 'true',
      goodsId: product.value.goodsId,
      goodsName: product.value.goodsName,
      goodsPrice: product.value.goodsPrice,
      goodsImage: product.value.goodsImage,
      quantity: quantity.value
    }
  })
}

onMounted(load)
</script>
