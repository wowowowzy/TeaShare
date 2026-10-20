<template>
  <div class="pt-8 md:pt-12">
    <div class="container-editorial py-16 md:py-24">
      <nav class="mb-12 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
        <RouterLink to="/profile" class="link-underline">个人中心</RouterLink>
        <span class="mx-3">/</span>
        <span>订单详情</span>
      </nav>

      <SkeletonBlock v-if="loading" :rows="10" />

      <EmptyState v-else-if="error" title="订单不存在" :description="error">
        <RouterLink to="/profile" class="btn-editorial-ghost">返回订单列表</RouterLink>
      </EmptyState>

      <template v-else-if="order">
        <!-- 状态 -->
        <section class="border border-[#1C1C1C]/10 p-10">
          <p class="eyebrow">订单状态</p>
          <h1 class="mt-6 font-serif text-4xl leading-tight tracking-tighter md:text-5xl">
            {{ orderStatusText(order.status) }}
          </h1>
          <p class="mt-4 font-sans text-sm leading-relaxed text-[#1C1C1C]/60">
            {{ orderStatusDesc(order.status) }}
          </p>
        </section>

        <!-- 订单信息 -->
        <section class="mt-12">
          <SectionLabel>订单信息</SectionLabel>
          <dl class="mt-6 grid grid-cols-1 gap-px border border-[#1C1C1C]/10 md:grid-cols-2">
            <div class="border-b border-[#1C1C1C]/10 p-6 md:border-r">
              <dt class="eyebrow">订单编号</dt>
              <dd class="mt-3 font-sans text-sm break-all">{{ order.orderId }}</dd>
            </div>
            <div class="border-b border-[#1C1C1C]/10 p-6">
              <dt class="eyebrow">下单时间</dt>
              <dd class="mt-3 font-sans text-sm">{{ formatDate(order.createTime) }}</dd>
            </div>
            <div class="border-b border-[#1C1C1C]/10 p-6 md:border-r md:border-b-0">
              <dt class="eyebrow">订单总价</dt>
              <dd class="mt-3 font-serif text-lg tracking-tight">¥{{ formatPrice(order.totalPrice) }}</dd>
            </div>
            <div class="p-6">
              <dt class="eyebrow">优惠券</dt>
              <dd class="mt-3 font-sans text-sm">
                {{ order.couponId ? `已使用（ID ${order.couponId}）` : '未使用' }}
              </dd>
            </div>
          </dl>
        </section>

        <!-- 商品 -->
        <section class="mt-12">
          <SectionLabel>商品清单</SectionLabel>
          <ul class="mt-6 border-t border-[#1C1C1C]/10">
            <li
              v-for="(d, i) in order.orderDTOList || []"
              :key="i"
              class="grid grid-cols-1 items-center gap-4 border-b border-[#1C1C1C]/10 py-6 md:grid-cols-12"
            >
              <div class="md:col-span-2">
                <button type="button" @click="preview(d.goodsImage)">
                  <SmartImage
                    :src="d.goodsImage"
                    :alt="d.goodsName"
                    class="aspect-square w-20 border border-[#1C1C1C]/10"
                  />
                </button>
              </div>
              <div class="md:col-span-6">
                <p class="font-serif text-lg tracking-tight">{{ d.goodsName }}</p>
                <p class="mt-1 font-sans text-xs leading-relaxed text-[#1C1C1C]/50">
                  {{ d.goodsIntro || getDefaultSpecs(d.goodsName) }}
                </p>
              </div>
              <div class="md:col-span-2">
                <p class="font-sans text-sm text-[#1C1C1C]/60">
                  ¥{{ formatPrice(d.goodsPrice) }} × {{ d.quantity }}
                </p>
              </div>
              <div class="text-right md:col-span-2">
                <p class="font-serif text-lg tracking-tight">
                  ¥{{ formatPrice(Number(d.goodsPrice) * Number(d.quantity)) }}
                </p>
              </div>
            </li>
          </ul>
        </section>

        <!-- 金额 -->
        <section class="mt-12 flex justify-end">
          <dl class="w-full max-w-xs space-y-3 border-t border-[#1C1C1C]/10 pt-6">
            <div class="flex justify-between font-sans text-sm text-[#1C1C1C]/60">
              <dt>商品总价</dt>
              <dd>¥{{ formatPrice(order.totalPrice) }}</dd>
            </div>
            <div class="flex items-baseline justify-between border-t border-[#1C1C1C]/10 pt-4">
              <dt class="eyebrow">实付金额</dt>
              <dd class="font-serif text-3xl tracking-tighter">¥{{ formatPrice(order.totalPrice) }}</dd>
            </div>
          </dl>
        </section>

        <div class="mt-16">
          <RouterLink to="/profile" class="btn-editorial-ghost">返回订单列表</RouterLink>
        </div>
      </template>
    </div>

    <el-image-viewer
      v-if="showViewer"
      :url-list="[asset(currentImage)]"
      @close="showViewer = false"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { getOrderById } from '@/api/user'
import { getDefaultSpecs, orderStatusDesc, orderStatusText } from '@/content'
import { asset } from '@/utils/apiBase'

const route = useRoute()

const order = ref(null)
const loading = ref(true)
const error = ref('')
const showViewer = ref(false)
const currentImage = ref('')

const orderId = computed(() => route.query.orderId)

function formatPrice(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

function formatDate(t) {
  if (!t) return '—'
  const d = new Date(t)
  return Number.isNaN(d.getTime()) ? String(t) : d.toLocaleString('zh-CN')
}

function preview(url) {
  if (!url) return
  currentImage.value = url
  showViewer.value = true
}

async function load() {
  if (!orderId.value) {
    error.value = '缺少订单号参数'
    loading.value = false
    return
  }
  loading.value = true
  try {
    order.value = await getOrderById(orderId.value)
  } catch (e) {
    error.value = e?.message || '订单加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
