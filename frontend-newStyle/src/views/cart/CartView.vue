<template>
  <div class="pt-20 md:pt-28">
    <div class="container-editorial py-16 md:py-24">
      <header class="mb-12 flex flex-wrap items-end justify-between gap-6">
        <div>
          <SectionLabel class="mb-6 block">购物车</SectionLabel>
          <h1 class="font-serif text-5xl leading-[0.95] tracking-tighter md:text-7xl">我的茶品</h1>
        </div>
        <button
          v-if="cartItems.length"
          type="button"
          class="link-underline font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/60 uppercase"
          @click="clearCart"
        >
          清空购物车
        </button>
      </header>

      <SkeletonBlock v-if="loading" :rows="6" />

      <EmptyState v-else-if="!cartItems.length" title="购物车是空的" description="去商城挑几款好茶吧。">
        <RouterLink to="/shop" class="btn-editorial">去购物</RouterLink>
      </EmptyState>

      <template v-else>
        <ul class="border-t border-[#1C1C1C]/10">
          <li
            v-for="item in cartItems"
            :key="item.goodsId"
            class="grid grid-cols-1 items-center gap-6 border-b border-[#1C1C1C]/10 py-8 md:grid-cols-12"
          >
            <div class="md:col-span-2">
              <SmartImage
                :src="item.goodsImage"
                :alt="item.goodsName"
                class="aspect-square w-24 border border-[#1C1C1C]/10"
              />
            </div>

            <div class="md:col-span-4">
              <p class="font-serif text-xl tracking-tight">{{ item.goodsName }}</p>
              <p class="mt-2 font-sans text-xs leading-relaxed text-[#1C1C1C]/50">
                {{ getDefaultSpecs(item.goodsName) }}
              </p>
            </div>

            <div class="md:col-span-2">
              <p class="font-serif text-lg tracking-tight">¥{{ formatPrice(item.goodsPrice) }}</p>
            </div>

            <div class="md:col-span-2">
              <div class="inline-flex items-center border border-[#1C1C1C]/15">
                <button
                  type="button"
                  class="flex h-9 w-9 items-center justify-center transition-colors duration-300 hover:bg-[#1C1C1C]/[0.04]"
                  aria-label="减少数量"
                  @click="decrease(item)"
                >
                  −
                </button>
                <span class="w-10 text-center font-sans text-sm">{{ item.quantity }}</span>
                <button
                  type="button"
                  class="flex h-9 w-9 items-center justify-center transition-colors duration-300 hover:bg-[#1C1C1C]/[0.04]"
                  aria-label="增加数量"
                  @click="increase(item)"
                >
                  +
                </button>
              </div>
            </div>

            <div class="flex items-center justify-between md:col-span-2 md:justify-end">
              <span class="font-serif text-lg tracking-tight">
                ¥{{ formatPrice(Number(item.goodsPrice) * Number(item.quantity)) }}
              </span>
              <button
                type="button"
                class="ml-6 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                @click="removeItem(item)"
              >
                删除
              </button>
            </div>
          </li>
        </ul>

        <div class="mt-12 flex flex-col items-end gap-6 md:flex-row md:items-center md:justify-end">
          <div class="text-right">
            <p class="eyebrow">合计</p>
            <p class="mt-2 font-serif text-4xl tracking-tighter">¥{{ totalPrice.toFixed(2) }}</p>
          </div>
          <button type="button" class="btn-editorial" @click="checkout">去结算</button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { cartApi } from '@/api/cart'
import { getDefaultSpecs } from '@/content'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const cartItems = ref([])
const loading = ref(true)

const totalPrice = computed(() =>
  cartItems.value.reduce((sum, i) => sum + Number(i.goodsPrice) * Number(i.quantity), 0)
)

function formatPrice(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

/** 字段容错：兼容 goodsId||id、goodsName||name 等命名差异 */
function normalize(raw) {
  return {
    goodsId: raw.goodsId ?? raw.id,
    quantity: Number(raw.quantity ?? 1),
    goodsName: raw.goodsName ?? raw.name ?? '未知商品',
    goodsPrice: Number(raw.goodsPrice ?? raw.price ?? 0),
    goodsImage: raw.goodsImage ?? raw.image ?? ''
  }
}

async function load() {
  loading.value = true
  try {
    const res = await cartApi.getCartList()
    cartItems.value = (Array.isArray(res) ? res : []).map(normalize)
  } catch {
    cartItems.value = []
  } finally {
    loading.value = false
  }
}

/** 乐观更新，失败回滚 */
async function changeQuantity(item, delta) {
  const next = Number(item.quantity) + delta
  if (next < 1) return
  const prev = item.quantity
  item.quantity = next
  try {
    await cartApi.updateCartItem(item.goodsId, next)
  } catch {
    item.quantity = prev
  }
}

const increase = (item) => changeQuantity(item, 1)
const decrease = (item) => changeQuantity(item, -1)

async function removeItem(item) {
  try {
    await cartApi.deleteCartItems([item.goodsId])
    cartItems.value = cartItems.value.filter((i) => String(i.goodsId) !== String(item.goodsId))
    ElMessage.success('已删除')
  } catch {
    /* 拦截器已提示 */
  }
}

async function clearCart() {
  try {
    await ElMessageBox.confirm('确定清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await cartApi.deleteCartItems(cartItems.value.map((i) => i.goodsId))
    cartItems.value = []
    ElMessage.success('已清空')
  } catch {
    /* 拦截器已提示 */
  }
}

function checkout() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/payment')
}

onMounted(load)
</script>
