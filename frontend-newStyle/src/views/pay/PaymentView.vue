<template>
  <div class="pt-20 md:pt-28">
    <div class="container-editorial py-16 md:py-24">
      <header class="mb-12">
        <SectionLabel class="mb-6 block">结算</SectionLabel>
        <h1 class="font-serif text-5xl leading-[0.95] tracking-tighter md:text-7xl">支付</h1>
      </header>

      <SkeletonBlock v-if="loading" :rows="10" />

      <EmptyState
        v-else-if="!orderItems.length"
        title="没有待支付的商品"
        description="请先选购商品。"
      >
        <RouterLink to="/shop" class="btn-editorial">去商城</RouterLink>
      </EmptyState>

      <div v-else class="grid grid-cols-1 gap-16 md:grid-cols-12 md:gap-16">
        <!-- 左栏：地址 + 商品 -->
        <div class="md:col-span-8">
          <!-- 收货地址 -->
          <section>
            <div class="flex items-center justify-between">
              <SectionLabel>收货地址</SectionLabel>
              <button
                type="button"
                class="link-underline font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/60 uppercase"
                @click="openAddressDialog(null)"
              >
                添加地址
              </button>
            </div>

            <div class="mt-6 max-h-80 overflow-y-auto scroll-thin border border-[#1C1C1C]/10">
              <p v-if="!addresses.length" class="p-6 font-sans text-sm text-[#1C1C1C]/50">
                暂无收货地址，请先添加。
              </p>
              <label
                v-for="a in addresses"
                :key="a.id"
                class="block cursor-pointer border-b border-[#1C1C1C]/10 p-6 transition-colors duration-300 last:border-b-0"
                :class="String(selectedAddressId) === String(a.id) ? 'bg-[#1C1C1C]/[0.03]' : 'hover:bg-[#1C1C1C]/[0.02]'"
              >
                <div class="flex items-start gap-4">
                  <input
                    v-model="selectedAddressId"
                    type="radio"
                    :value="a.id"
                    class="mt-1.5 accent-[#1C1C1C]"
                  />
                  <div class="flex-1">
                    <div class="flex items-center gap-3">
                      <span class="font-serif text-lg tracking-tight">{{ a.name }}</span>
                      <span class="font-sans text-sm text-[#1C1C1C]/60">{{ a.phone }}</span>
                      <span
                        v-if="a.isDefault === 1"
                        class="border border-[#1C1C1C]/20 px-2 py-0.5 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/60 uppercase"
                      >
                        默认
                      </span>
                    </div>
                    <p class="mt-2 font-sans text-sm leading-relaxed text-[#1C1C1C]/60">
                      {{ a.province }} {{ a.city }} {{ a.district }} {{ a.detail }}
                      <span v-if="a.postalCode">（{{ a.postalCode }}）</span>
                    </p>
                  </div>
                  <div class="flex shrink-0 gap-4">
                    <button
                      type="button"
                      class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                      @click.prevent="openAddressDialog(a)"
                    >
                      编辑
                    </button>
                    <button
                      type="button"
                      class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                      @click.prevent="removeAddress(a)"
                    >
                      删除
                    </button>
                  </div>
                </div>
              </label>
            </div>
          </section>

          <!-- 商品信息 -->
          <section class="mt-16">
            <SectionLabel>商品信息</SectionLabel>
            <ul class="mt-6 border-t border-[#1C1C1C]/10">
              <li
                v-for="item in orderItems"
                :key="item.goodsId"
                class="grid grid-cols-1 items-center gap-4 border-b border-[#1C1C1C]/10 py-6 md:grid-cols-12"
              >
                <div class="md:col-span-2">
                  <SmartImage
                    :src="item.goodsImage"
                    :alt="item.goodsName"
                    class="aspect-square w-20 border border-[#1C1C1C]/10"
                  />
                </div>
                <div class="md:col-span-5">
                  <p class="font-serif text-lg tracking-tight">{{ item.goodsName }}</p>
                  <p class="mt-1 font-sans text-xs text-[#1C1C1C]/50">
                    {{ getDefaultSpecs(item.goodsName) }}
                  </p>
                </div>
                <div class="md:col-span-2">
                  <p class="font-sans text-sm text-[#1C1C1C]/60">
                    ¥{{ formatPrice(item.goodsPrice) }} × {{ item.quantity }}
                  </p>
                </div>
                <div class="text-right md:col-span-3">
                  <p class="font-serif text-lg tracking-tight">
                    ¥{{ formatPrice(Number(item.goodsPrice) * Number(item.quantity)) }}
                  </p>
                </div>
              </li>
            </ul>
          </section>
        </div>

        <!-- 右栏：优惠券 + 汇总 -->
        <aside class="md:col-span-4">
          <div class="border border-[#1C1C1C]/10 p-6 md:sticky md:top-28">
            <!-- 优惠券 -->
            <div class="flex items-center justify-between">
              <SectionLabel>优惠券</SectionLabel>
              <button
                v-if="canUseCoupon && availableCoupons.length"
                type="button"
                class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/60 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                @click="showCouponList = !showCouponList"
              >
                {{ showCouponList ? '收起' : '查看可用优惠券' }}
              </button>
            </div>

            <p
              v-if="!canUseCoupon"
              class="mt-4 font-sans text-xs leading-relaxed text-[#1C1C1C]/50"
            >
              {{ isSeckillGoods ? '秒杀商品不支持使用优惠券' : '商品总价满 ¥200 才可使用优惠券' }}
            </p>

            <template v-else>
              <p v-if="!availableCoupons.length" class="mt-4 font-sans text-xs text-[#1C1C1C]/50">
                暂无可用优惠券
              </p>
              <ul v-else-if="showCouponList" class="mt-4 space-y-px">
                <li v-for="c in availableCoupons" :key="c.id">
                  <label
                    class="flex cursor-pointer items-center gap-3 border border-[#1C1C1C]/10 p-4 transition-colors duration-300 hover:border-[#1C1C1C]"
                    :class="selectedCouponId === c.id ? 'border-[#1C1C1C] bg-[#1C1C1C]/[0.03]' : ''"
                  >
                    <input
                      v-model="selectedCouponId"
                      type="radio"
                      :value="c.id"
                      class="accent-[#1C1C1C]"
                      @click="selectedCouponId === c.id ? (selectedCouponId = null) : null"
                    />
                    <span class="flex-1">
                      <span class="block font-sans text-sm">{{ c.intro }}</span>
                      <span class="mt-1 block font-sans text-xs text-[#1C1C1C]/50">
                        满 ¥{{ formatPrice(c.minAmount) }} 减 ¥{{ formatPrice(c.reduceAmount) }}
                      </span>
                    </span>
                  </label>
                </li>
              </ul>
            </template>

            <hr class="my-6 border-[#1C1C1C]/10" />

            <!-- 价格汇总 -->
            <dl class="space-y-3">
              <div class="flex justify-between font-sans text-sm text-[#1C1C1C]/60">
                <dt>商品总价</dt>
                <dd>¥{{ totalPrice.toFixed(2) }}</dd>
              </div>
              <div class="flex justify-between font-sans text-sm text-[#1C1C1C]/60">
                <dt>运费</dt>
                <dd>¥{{ shippingFee.toFixed(2) }}</dd>
              </div>
              <div
                v-if="couponDiscount > 0"
                class="flex justify-between font-sans text-sm text-[#1C1C1C]/60"
              >
                <dt>优惠券</dt>
                <dd>− ¥{{ couponDiscount.toFixed(2) }}</dd>
              </div>
              <div class="flex items-baseline justify-between border-t border-[#1C1C1C]/10 pt-4">
                <dt class="eyebrow">实付金额</dt>
                <dd class="font-serif text-3xl tracking-tighter">¥{{ finalPrice.toFixed(2) }}</dd>
              </div>
            </dl>

            <button
              type="button"
              class="btn-editorial mt-8 w-full"
              :disabled="!canPay || paying"
              @click="processPayment"
            >
              {{ paying ? '支付中…' : '确认支付' }}
            </button>

            <RouterLink
              to="/quiz"
              class="mt-6 flex items-center justify-center gap-2 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
            >
              <img :src="aiAvatar" alt="AI 小茗" class="h-6 w-6 border border-[#1C1C1C]/10 object-cover" />
              问问小茗
            </RouterLink>
          </div>
        </aside>
      </div>
    </div>

    <AddressFormDialog v-model="dialogVisible" :address="editingAddress" @saved="saveAddress" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import AddressFormDialog from '@/components/shop/AddressFormDialog.vue'
import { cartApi, payApi } from '@/api/cart'
import { paySeckillGoods } from '@/api/goods'
import { addAddress, deleteAddress, getAddressList, getCoupons, updateAddress } from '@/api/user'
import { getDefaultSpecs } from '@/content'
import { asset } from '@/utils/apiBase'

const route = useRoute()
const router = useRouter()

const aiAvatar = computed(() => asset('/assest/number1.png'))

const orderItems = ref([])
const loading = ref(true)
const paying = ref(false)

const addresses = ref([])
const selectedAddressId = ref(null)
const dialogVisible = ref(false)
const editingAddress = ref(null)

const availableCoupons = ref([])
const selectedCouponId = ref(null)
const showCouponList = ref(false)

const shippingFee = ref(0)

const isSeckillGoods = computed(() => route.query.seckill === 'true')

const totalPrice = computed(() =>
  orderItems.value.reduce((sum, i) => sum + Number(i.goodsPrice) * Number(i.quantity), 0)
)

const canUseCoupon = computed(() => !isSeckillGoods.value && totalPrice.value >= 200)

const couponDiscount = computed(() => {
  if (!canUseCoupon.value || selectedCouponId.value == null) return 0
  const c = availableCoupons.value.find((x) => String(x.id) === String(selectedCouponId.value))
  if (!c) return 0
  if (totalPrice.value < Number(c.minAmount)) return 0
  return Number(c.reduceAmount)
})

const finalPrice = computed(() => Math.max(0, totalPrice.value + shippingFee.value - couponDiscount.value))

const canPay = computed(() => !!selectedAddressId.value && orderItems.value.length > 0)

function formatPrice(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

function normalize(raw) {
  return {
    goodsId: raw.goodsId ?? raw.id,
    quantity: Number(raw.quantity ?? 1),
    goodsName: raw.goodsName ?? raw.name ?? '未知商品',
    goodsPrice: Number(raw.goodsPrice ?? raw.price ?? 0),
    goodsImage: raw.goodsImage ?? raw.image ?? ''
  }
}

/** 三分支：秒杀 / 立即购买 / 购物车 */
async function loadOrderItems() {
  if (isSeckillGoods.value) {
    orderItems.value = [
      {
        goodsId: route.query.goodsId,
        quantity: Number(route.query.quantity || 1),
        goodsName: route.query.goodsName || '秒杀商品',
        goodsPrice: Number(route.query.goodsPrice || 0),
        goodsImage: route.query.goodsImage || ''
      }
    ]
    return
  }
  if (route.query.buyNow === 'true') {
    orderItems.value = [
      {
        goodsId: route.query.goodsId,
        quantity: Number(route.query.quantity || 1),
        goodsName: route.query.goodsName || '商品',
        goodsPrice: Number(route.query.goodsPrice || 0),
        goodsImage: route.query.goodsImage || ''
      }
    ]
    return
  }
  const res = await cartApi.getCartList()
  orderItems.value = (Array.isArray(res) ? res : []).map(normalize)
}

async function loadAddresses() {
  try {
    const res = await getAddressList()
    addresses.value = Array.isArray(res) ? res : []
    const def = addresses.value.find((a) => a.isDefault === 1) || addresses.value[0]
    if (def) selectedAddressId.value = def.id
  } catch {
    addresses.value = []
  }
}

async function loadCoupons() {
  try {
    const res = await getCoupons()
    const list = Array.isArray(res) ? res : []
    availableCoupons.value = list.filter((c) => Number(c.status) === 1)
  } catch {
    availableCoupons.value = []
  }
}

function openAddressDialog(a) {
  editingAddress.value = a
  dialogVisible.value = true
}

async function saveAddress(payload) {
  try {
    if (payload.id) {
      await updateAddress(payload)
    } else {
      await addAddress(payload)
    }
    dialogVisible.value = false
    ElMessage.success('保存成功')
    await loadAddresses()
  } catch {
    /* 拦截器已提示 */
  }
}

async function removeAddress(a) {
  try {
    await ElMessageBox.confirm('确定删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteAddress(a.id)
    ElMessage.success('已删除')
    await loadAddresses()
  } catch {
    /* 拦截器已提示 */
  }
}

async function processPayment() {
  if (!canPay.value) return
  paying.value = true
  try {
    if (isSeckillGoods.value) {
      const msg = await paySeckillGoods(orderItems.value[0].goodsId)
      ElMessage.success(msg || '抢购成功，已排队生成订单')
    } else {
      const list = orderItems.value.map((i) => ({ goodsId: i.goodsId, quantity: i.quantity }))
      await payApi(selectedCouponId.value, list)
      // 购物车下单成功后清空购物车
      if (route.query.buyNow !== 'true') {
        try {
          await cartApi.deleteCartItems(orderItems.value.map((i) => i.goodsId))
        } catch {
          /* 忽略 */
        }
      }
    }
    router.push({ path: '/payment/success', query: { amount: finalPrice.value.toFixed(2), seckill: String(isSeckillGoods.value) } })
  } catch {
    /* 拦截器已提示 */
  } finally {
    paying.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadOrderItems(), loadAddresses(), loadCoupons()])
  } finally {
    loading.value = false
  }
})
</script>
