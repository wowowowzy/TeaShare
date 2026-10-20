<template>
  <div class="pt-20 md:pt-28">
    <div class="container-editorial py-16 md:py-24">
      <header class="mb-12">
        <SectionLabel class="mb-6 block">个人中心</SectionLabel>
        <h1 class="font-serif text-5xl leading-[0.95] tracking-tighter md:text-7xl">
          {{ userInfo.username || '茶友' }}
        </h1>
      </header>

      <!-- 用户信息 -->
      <section class="grid grid-cols-1 gap-12 border-y border-[#1C1C1C]/10 py-12 md:grid-cols-12">
        <div class="md:col-span-4">
          <button
            type="button"
            class="group relative block"
            aria-label="更换头像"
            @click="avatarInput?.click()"
          >
            <SmartImage
              :src="avatarUrl"
              :alt="userInfo.username"
              loading="eager"
              class="h-40 w-40 border border-[#1C1C1C]/10"
            />
            <span
              class="absolute inset-0 flex items-center justify-center bg-[#1C1C1C]/0 font-sans text-xs tracking-[0.2em] text-[#F9F8F6] uppercase opacity-0 transition-all duration-500 group-hover:bg-[#1C1C1C]/50 group-hover:opacity-100"
            >
              更换头像
            </span>
          </button>
          <input
            ref="avatarInput"
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            class="hidden"
            @change="onAvatarChange"
          />
        </div>

        <dl class="grid grid-cols-1 gap-px self-start border border-[#1C1C1C]/10 md:col-span-8 md:grid-cols-2">
          <div class="border-b border-[#1C1C1C]/10 p-6 md:border-r">
            <dt class="eyebrow">用户 ID</dt>
            <dd class="mt-3 font-sans text-sm">{{ userInfo.userId || '—' }}</dd>
          </div>
          <div class="border-b border-[#1C1C1C]/10 p-6">
            <dt class="eyebrow">用户名</dt>
            <dd class="mt-3 font-sans text-sm">{{ userInfo.username || '—' }}</dd>
          </div>
          <div class="border-b border-[#1C1C1C]/10 p-6 md:border-r md:border-b-0">
            <dt class="eyebrow">手机号</dt>
            <dd class="mt-3 font-sans text-sm">{{ userInfo.phone || '—' }}</dd>
          </div>
          <div class="p-6">
            <dt class="eyebrow">注册时间</dt>
            <dd class="mt-3 font-sans text-sm">{{ formatDate(userInfo.createTime) }}</dd>
          </div>
        </dl>
      </section>

      <!-- 地址 + 优惠券 -->
      <div class="mt-16 grid grid-cols-1 gap-12 md:grid-cols-12">
        <!-- 地址 -->
        <section class="md:col-span-6">
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

          <div class="mt-6 border-t border-[#1C1C1C]/10">
            <p v-if="!addresses.length" class="py-8 font-sans text-sm text-[#1C1C1C]/50">
              暂无收货地址。
            </p>
            <div
              v-for="a in visibleAddresses"
              :key="a.id"
              class="border-b border-[#1C1C1C]/10 py-6"
            >
              <div class="flex items-start justify-between gap-4">
                <div>
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
                    @click="openAddressDialog(a)"
                  >
                    编辑
                  </button>
                  <button
                    type="button"
                    class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                    @click="removeAddress(a)"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>

            <button
              v-if="addresses.length > 2"
              type="button"
              class="mt-6 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/60 uppercase"
              @click="addressExpanded = !addressExpanded"
            >
              {{ addressExpanded ? '收起' : `展开全部 ${addresses.length} 个地址` }}
            </button>
          </div>
        </section>

        <!-- 优惠券 -->
        <section class="md:col-span-6">
          <SectionLabel>优惠券</SectionLabel>
          <div class="mt-6 border-t border-[#1C1C1C]/10">
            <p v-if="!coupons.length" class="py-8 font-sans text-sm text-[#1C1C1C]/50">
              暂无优惠券。
            </p>
            <div v-for="c in visibleCoupons" :key="c.id" class="border-b border-[#1C1C1C]/10 py-6">
              <div class="flex items-baseline justify-between gap-4">
                <span class="font-serif text-lg tracking-tight">{{ c.intro }}</span>
                <span class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
                  {{ couponStatusText(c.status) }}
                </span>
              </div>
              <p class="mt-2 font-sans text-sm text-[#1C1C1C]/60">
                满 ¥{{ formatPrice(c.minAmount) }} 减 ¥{{ formatPrice(c.reduceAmount) }}
              </p>
              <p class="mt-1 font-sans text-xs text-[#1C1C1C]/40">
                {{ formatDate(c.startTime) }} — {{ formatDate(c.endTime) }}
              </p>
            </div>

            <button
              v-if="coupons.length > 2"
              type="button"
              class="mt-6 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/60 uppercase"
              @click="couponExpanded = !couponExpanded"
            >
              {{ couponExpanded ? '收起' : `展开查看全部 ${coupons.length} 张` }}
            </button>
          </div>
        </section>
      </div>

      <!-- 订单 -->
      <section class="mt-24">
        <SectionLabel>我的订单</SectionLabel>

        <SkeletonBlock v-if="loadingOrders" class="mt-6" :rows="4" />

        <EmptyState
          v-else-if="!orders.length"
          class="!py-16"
          title="暂无订单"
          description="去商城挑几款好茶吧。"
        />

        <ul v-else class="mt-6 space-y-px">
          <li
            v-for="o in orders"
            :key="o.orderId"
            class="cursor-pointer border border-[#1C1C1C]/10 transition-colors duration-300 hover:border-[#1C1C1C]"
            @click="goOrder(o)"
          >
            <div class="flex flex-wrap items-center justify-between gap-4 border-b border-[#1C1C1C]/10 px-6 py-4">
              <div class="flex items-center gap-6">
                <span class="font-sans text-xs tracking-[0.1em] text-[#1C1C1C]/40">
                  订单号 {{ o.orderId }}
                </span>
                <span class="border border-[#1C1C1C]/20 px-2 py-0.5 font-sans text-xs tracking-[0.15em] uppercase">
                  {{ orderStatusText(o.status) }}
                </span>
              </div>
              <span
                class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase transition-transform duration-300"
              >
                查看详情 →
              </span>
            </div>

            <ul class="px-6 py-4">
              <li
                v-for="(d, i) in o.orderDTOList || []"
                :key="i"
                class="flex items-center gap-4 border-b border-[#1C1C1C]/5 py-3 last:border-b-0"
              >
                <SmartImage
                  :src="d.goodsImage"
                  :alt="d.goodsName"
                  class="h-14 w-14 shrink-0 border border-[#1C1C1C]/10"
                />
                <span class="flex-1 font-sans text-sm">{{ d.goodsName }}</span>
                <span class="font-sans text-xs text-[#1C1C1C]/50">× {{ d.quantity }}</span>
                <span class="font-sans text-sm">¥{{ formatPrice(d.goodsPrice) }}</span>
              </li>
            </ul>

            <div
              class="flex flex-wrap items-center justify-between gap-4 border-t border-[#1C1C1C]/10 px-6 py-4 font-sans text-xs text-[#1C1C1C]/50"
            >
              <span>{{ formatDate(o.createTime) }}</span>
              <span>
                共 {{ (o.orderDTOList || []).length }} 件 · 总价
                <span class="ml-2 font-serif text-lg tracking-tight text-[#1C1C1C]">
                  ¥{{ formatPrice(o.totalPrice) }}
                </span>
              </span>
            </div>
          </li>
        </ul>
      </section>
    </div>

    <AddressFormDialog v-model="dialogVisible" :address="editingAddress" @saved="saveAddress" />
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
import AddressFormDialog from '@/components/shop/AddressFormDialog.vue'
import { addAddress, deleteAddress, getAddressList, getCoupons, getOrders, updateAddress, uploadAvatar } from '@/api/user'
import { COUPON_STATUS_MAP, orderStatusText } from '@/content'
import { useUserStore } from '@/stores/user'
import { asset } from '@/utils/apiBase'
import { STORAGE_KEYS, writeString } from '@/utils/storage'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo || {})
const avatarInput = ref(null)

const orders = ref([])
const loadingOrders = ref(true)

const addresses = ref([])
const addressExpanded = ref(false)
const dialogVisible = ref(false)
const editingAddress = ref(null)

const coupons = ref([])
const couponExpanded = ref(false)

const avatarUrl = computed(() => {
  if (!userInfo.value.avatar) return asset('/assest/default.jpg')
  const ts = localStorage.getItem(STORAGE_KEYS.avatarTimestamp)
  const sep = userInfo.value.avatar.includes('?') ? '&' : '?'
  return asset(userInfo.value.avatar) + (ts ? `${sep}t=${ts}` : '')
})

const visibleAddresses = computed(() =>
  addressExpanded.value ? addresses.value : addresses.value.slice(0, 2)
)
const visibleCoupons = computed(() =>
  couponExpanded.value ? coupons.value : coupons.value.slice(0, 2)
)

function formatPrice(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

function formatDate(t) {
  if (!t) return '—'
  const d = new Date(t)
  return Number.isNaN(d.getTime()) ? String(t) : d.toLocaleString('zh-CN')
}

function couponStatusText(s) {
  return COUPON_STATUS_MAP[s] || '未知'
}

async function loadOrders() {
  loadingOrders.value = true
  try {
    const res = await getOrders()
    orders.value = Array.isArray(res) ? res : []
  } catch {
    orders.value = []
  } finally {
    loadingOrders.value = false
  }
}

async function loadAddresses() {
  try {
    const res = await getAddressList()
    addresses.value = Array.isArray(res) ? res : []
  } catch {
    addresses.value = []
  }
}

async function loadCoupons() {
  try {
    const res = await getCoupons()
    coupons.value = Array.isArray(res) ? res : []
  } catch {
    coupons.value = []
  }
}

async function onAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!/^image\/(jpeg|png|gif|webp)$/.test(file.type)) {
    ElMessage.error('仅支持 jpeg / png / gif / webp 格式')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  try {
    const url = await uploadAvatar(file)
    if (url) {
      userStore.updateAvatar(url)
      writeString(STORAGE_KEYS.avatarTimestamp, String(Date.now()))
      await userStore.getUserShow()
      ElMessage.success('头像更新成功')
    }
  } catch {
    /* 拦截器已提示 */
  } finally {
    if (avatarInput.value) avatarInput.value.value = ''
  }
}

function openAddressDialog(a) {
  editingAddress.value = a
  dialogVisible.value = true
}

async function saveAddress(payload) {
  try {
    if (payload.id) await updateAddress(payload)
    else await addAddress(payload)
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

function goOrder(o) {
  router.push({ path: '/order/detail', query: { orderId: o.orderId } })
}

onMounted(() => {
  loadOrders()
  loadAddresses()
  loadCoupons()
})
</script>
