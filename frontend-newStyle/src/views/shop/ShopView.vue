<template>
  <div class="pt-20 md:pt-24">
    <div class="page-zoom mx-auto w-full max-w-[1200px] px-5 py-10">
      <header class="mb-6">
        <SectionLabel class="mb-3 block">茶文化商城</SectionLabel>
        <h1 class="font-serif text-3xl leading-tight tracking-tighter md:text-4xl">选购好茶</h1>
      </header>

      <!-- 轮播 -->
      <div class="relative overflow-hidden border border-[#1C1C1C]/10">
        <div class="relative h-[300px]">
          <TransitionGroup
            enter-active-class="transition-opacity duration-1000"
            enter-from-class="opacity-0"
            leave-active-class="absolute inset-0 transition-opacity duration-1000"
            leave-to-class="opacity-0"
          >
            <div v-for="(b, i) in banners" v-show="i === activeBanner" :key="b.id" class="absolute inset-0">
              <SmartImage :src="b.image" :alt="b.title" class="h-full w-full" loading="eager" />
              <div class="absolute inset-x-0 bottom-0 bg-gradient-to-t from-[#1C1C1C]/70 to-transparent p-6 md:p-8">
                <p class="font-serif text-2xl tracking-tighter text-[#F9F8F6]">{{ b.title }}</p>
                <p class="mt-1 font-sans text-sm text-[#F9F8F6]/80">{{ b.description }}</p>
              </div>
            </div>
          </TransitionGroup>
        </div>
        <div class="absolute right-6 bottom-6 flex gap-2">
          <button
            v-for="(b, i) in banners"
            :key="b.id"
            type="button"
            class="h-px w-8 transition-colors duration-300"
            :class="i === activeBanner ? 'bg-[#F9F8F6]' : 'bg-[#F9F8F6]/40'"
            :aria-label="b.title"
            @click="activeBanner = i"
          />
        </div>
      </div>

      <!-- 筛选区 -->
      <div class="mt-6 flex flex-wrap items-center gap-4 border border-[#1C1C1C]/10 p-5">
        <input
          v-model.trim="searchQuery"
          type="search"
          class="field-input min-w-56 max-w-96 flex-1"
          placeholder="请输入商品名称搜索"
        />

        <div class="flex flex-wrap gap-2">
          <button
            type="button"
            class="border px-3 py-1.5 font-sans text-xs tracking-[0.1em] transition-colors duration-300"
            :class="
              selectedCategory === ''
                ? 'border-[#1C1C1C] bg-[#1C1C1C] text-[#F9F8F6]'
                : 'border-[#1C1C1C]/20 text-[#1C1C1C]/60 hover:border-[#1C1C1C]'
            "
            @click="setCategory('')"
          >
            全部
          </button>
          <button
            v-for="c in categories"
            :key="c.id"
            type="button"
            class="border px-3 py-1.5 font-sans text-xs tracking-[0.1em] transition-colors duration-300"
            :class="
              String(selectedCategory) === String(c.id)
                ? 'border-[#1C1C1C] bg-[#1C1C1C] text-[#F9F8F6]'
                : 'border-[#1C1C1C]/20 text-[#1C1C1C]/60 hover:border-[#1C1C1C]'
            "
            @click="setCategory(c.id)"
          >
            {{ c.name }}
          </button>
        </div>

        <RouterLink
          to="/cart"
          class="btn-editorial-ghost ml-auto !px-4 !py-2 text-xs"
        >
          购物车
        </RouterLink>
      </div>

      <!-- 价格区间 -->
      <div class="mt-4 flex flex-wrap items-center gap-4 border border-[#1C1C1C]/10 p-5">
        <span class="eyebrow shrink-0">价格区间</span>
        <input
          v-model.number="priceRange[0]"
          type="range"
          min="0"
          max="2000"
          step="50"
          class="h-px min-w-40 flex-1 appearance-none bg-[#1C1C1C]/20 accent-[#1C1C1C]"
          aria-label="最低价格"
          @change="applyFilters"
        />
        <span class="shrink-0 font-sans text-xs text-[#1C1C1C]/60">
          ¥{{ priceRange[0] }} — ¥{{ priceRange[1] }}
        </span>
        <input
          v-model.number="priceRange[1]"
          type="range"
          min="0"
          max="2000"
          step="50"
          class="h-px min-w-40 flex-1 appearance-none bg-[#1C1C1C]/20 accent-[#1C1C1C]"
          aria-label="最高价格"
          @change="applyFilters"
        />
        <span class="shrink-0 font-sans text-xs text-[#1C1C1C]/40">共 {{ total }} 件商品</span>
      </div>

      <!-- 商品网格 -->
      <div v-if="loading" class="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <div v-for="i in 6" :key="i">
          <div class="skeleton h-[200px] w-full" />
          <div class="skeleton mt-4 h-4 w-3/4" />
          <div class="skeleton mt-2 h-4 w-1/3" />
        </div>
      </div>

      <EmptyState
        v-else-if="!products.length"
        class="mt-8"
        title="暂无商品数据"
        description="换个关键词或分类试试。"
      />

      <template v-else>
        <div class="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <ProductCard
            v-for="p in products"
            :key="p.goodsId"
            :product="p"
            @view="goProduct"
            @add-to-cart="addToCart"
          />
        </div>

        <div v-if="total > pageSize" class="mt-8 flex justify-center">
          <el-pagination
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="page"
            background
            @current-change="onPageChange"
          />
        </div>
      </template>
    </div>

    <!-- 侧边固定入口 -->
    <button
      type="button"
      class="fixed top-1/3 right-0 z-40 border border-[#1C1C1C]/15 border-r-0 bg-[#F9F8F6] px-2.5 py-4 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/70 transition-colors duration-300 hover:bg-[#1C1C1C] hover:text-[#F9F8F6]"
      @click="showQuiz = true"
    >
      <span class="block [writing-mode:vertical-rl]">🎯 趣味闯关</span>
    </button>
    <button
      type="button"
      class="fixed top-1/2 right-0 z-40 border border-[#1C1C1C]/15 border-r-0 bg-[#F9F8F6] px-2.5 py-4 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/70 transition-colors duration-300 hover:bg-[#1C1C1C] hover:text-[#F9F8F6]"
      @click="showSeckill = true"
    >
      <span class="block [writing-mode:vertical-rl]">🔥 秒杀商品</span>
    </button>

    <QuizChallenge v-if="showQuiz" @close="showQuiz = false" />
    <SeckillModal v-if="showSeckill" @close="showSeckill = false" />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ProductCard from '@/components/shop/ProductCard.vue'
import QuizChallenge from '@/components/shop/QuizChallenge.vue'
import SeckillModal from '@/components/shop/SeckillModal.vue'
import { findAllGoods } from '@/api/goods'
import { cartApi } from '@/api/cart'
import { shop } from '@/content'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const banners = shop.banners
const categories = shop.categories

const activeBanner = ref(0)
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const loading = ref(true)
const searchQuery = ref('')
const selectedCategory = ref('')
const priceRange = reactive([0, 2000])

const showQuiz = ref(false)
const showSeckill = ref(false)

/** 后端数据（未过滤价格），价格过滤在 computed 中进行，避免破坏源数据 */
const allRecords = ref([])

const products = computed(() => {
  const [min, max] = priceRange
  return allRecords.value.filter((p) => {
    const price = Number(p.goodsPrice)
    return price >= min && price <= max
  })
})

/* 轮播自动播放 */
let bannerTimer = null

async function loadGoods() {
  loading.value = true
  try {
    const res = await findAllGoods({
      page: page.value,
      pageSize: pageSize.value,
      goodsName: searchQuery.value,
      categoryId: selectedCategory.value
    })
    // 后端返回 PageResult{ total, records }
    if (res && Array.isArray(res.records)) {
      allRecords.value = res.records
      total.value = res.total ?? res.records.length
    } else if (Array.isArray(res)) {
      allRecords.value = res
      total.value = res.length
    } else {
      allRecords.value = []
      total.value = 0
    }
  } catch {
    allRecords.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

let debounceTimer = null
function applyFilters() {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    page.value = 1
    loadGoods()
  }, 300)
}

function setCategory(id) {
  selectedCategory.value = id
  applyFilters()
}

function onPageChange(p) {
  page.value = p
  loadGoods()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(searchQuery, applyFilters)

function goProduct(p) {
  const url = router.resolve({ name: 'product', params: { id: p.goodsId } }).href
  window.open(url, '_blank', 'noopener')
}

async function addToCart(product) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    // 先查购物车判断是否已存在，避免重复添加导致数量错误
    const cart = await cartApi.getCartList()
    const list = Array.isArray(cart) ? cart : []
    const existing = list.find((c) => String(c.goodsId) === String(product.goodsId))
    if (existing) {
      await cartApi.updateCartItem(product.goodsId, Number(existing.quantity) + 1)
    } else {
      await cartApi.addToCart(product.goodsId, 1)
    }
    ElMessage.success('已加入购物车')
  } catch {
    /* 拦截器已提示 */
  }
}

onMounted(() => {
  loadGoods()
  bannerTimer = setInterval(() => {
    activeBanner.value = (activeBanner.value + 1) % banners.length
  }, 5000)
})

onUnmounted(() => {
  if (bannerTimer) clearInterval(bannerTimer)
  if (debounceTimer) clearTimeout(debounceTimer)
})
</script>
