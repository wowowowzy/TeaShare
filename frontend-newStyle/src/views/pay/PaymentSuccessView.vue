<template>
  <div class="pt-20 md:pt-28">
    <div class="container-editorial py-24 md:py-40">
      <div class="mx-auto max-w-2xl text-center">
        <SectionLabel class="mb-8 block">支付结果</SectionLabel>
        <h1 class="font-serif text-5xl leading-[0.95] tracking-tighter md:text-7xl">支付成功</h1>
        <p class="mt-8 font-sans text-sm leading-relaxed text-[#1C1C1C]/60 italic">
          您的订单已支付成功，感谢您的购买。
        </p>

        <dl class="mt-16 border-y border-[#1C1C1C]/10 py-8 text-left">
          <div class="flex justify-between border-b border-[#1C1C1C]/10 pb-4">
            <dt class="eyebrow">支付金额</dt>
            <dd class="font-serif text-2xl tracking-tight">¥{{ amount }}</dd>
          </div>
          <div class="flex justify-between pt-4">
            <dt class="eyebrow">支付时间</dt>
            <dd class="font-sans text-sm text-[#1C1C1C]/70">{{ paymentTime }}</dd>
          </div>
        </dl>

        <p class="mt-8 font-sans text-xs leading-relaxed tracking-[0.1em] text-[#1C1C1C]/40">
          请前往个人中心查看订单号。
        </p>

        <div class="mt-12 flex flex-wrap justify-center gap-4">
          <RouterLink to="/shop" class="btn-editorial">去商城逛逛</RouterLink>
          <RouterLink to="/profile" class="btn-editorial-ghost">查看订单</RouterLink>
        </div>

        <p v-if="countdown > 0" class="mt-10 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
          页面将在 {{ countdown }} 秒后自动跳转到商城
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SectionLabel from '@/components/ui/SectionLabel.vue'

const route = useRoute()
const router = useRouter()

const amount = ref(route.query.amount || '0.00')
const paymentTime = ref(new Date().toLocaleString('zh-CN'))
const countdown = ref(5)
let timer = null

onMounted(() => {
  // 仅在支付成功路径且金额有效时启动倒计时
  if (route.path !== '/payment/success' || Number(amount.value) <= 0) return
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      router.push('/shop')
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>
