<template>
  <section id="tea-varieties" class="section-editorial">
    <div class="container-editorial">
      <!-- 章节头 -->
      <header class="mb-16 md:mb-24">
        <SectionLabel class="mb-6 block">01 — 特色茶种</SectionLabel>
        <h2 class="font-serif text-4xl leading-[1.05] tracking-tighter md:text-6xl">
          品味岭南茶香
        </h2>
        <p class="mt-6 max-w-md font-sans text-sm leading-relaxed text-[#1C1C1C]/60 italic md:text-base">
          广东三大茶类，十一个品种。从蕉岭到凤凰，每一片叶子都有自己的山场与故事。
        </p>
      </header>

      <!-- 茶类选择 -->
      <div class="grid grid-cols-1 gap-px border border-[#1C1C1C]/10 md:grid-cols-3">
        <button
          v-for="(tea, i) in teas"
          :key="tea.id"
          type="button"
          class="group relative border-b border-[#1C1C1C]/10 p-8 text-left transition-colors duration-500 last:border-b-0 md:border-r md:border-b-0 md:last:border-r-0"
          :class="tea.id === selectedId ? 'bg-[#1C1C1C]/[0.03]' : 'hover:bg-[#1C1C1C]/[0.02]'"
          @click="selectTea(tea.id)"
        >
          <span class="eyebrow">{{ String(i + 1).padStart(2, '0') }}</span>
          <span class="mt-4 block font-serif text-3xl tracking-tighter md:text-4xl">
            {{ tea.name }}
          </span>
          <span
            class="mt-2 block font-sans text-xs leading-relaxed text-[#1C1C1C]/60 transition-all duration-500 group-hover:italic"
          >
            {{ tea.feature }}
          </span>
          <span
            class="mt-6 block h-px w-full origin-left bg-[#1C1C1C] transition-transform duration-500"
            :class="tea.id === selectedId ? 'scale-x-100' : 'scale-x-0'"
          />
        </button>
      </div>

      <!-- 当前茶种详情 -->
      <div v-if="currentTea" class="mt-16 grid grid-cols-1 gap-12 md:mt-24 md:grid-cols-12 md:gap-16">
        <!-- 左：品种列表 -->
        <div class="md:col-span-4">
          <SectionLabel class="mb-8 block">品种细分</SectionLabel>
          <ul>
            <li v-for="(v, i) in currentTea.varieties" :key="v.name">
              <button
                type="button"
                class="group flex w-full items-baseline gap-4 border-b border-[#1C1C1C]/10 py-4 text-left transition-colors duration-300 hover:border-[#1C1C1C]/40"
                @click="setVariety(i)"
              >
                <span class="font-sans text-xs text-[#1C1C1C]/40">
                  {{ String(i + 1).padStart(2, '0') }}
                </span>
                <span
                  class="font-serif text-xl tracking-tighter transition-all duration-500 group-hover:italic md:text-2xl"
                  :class="i === varietyIndex ? 'text-[#1C1C1C]' : 'text-[#1C1C1C]/60'"
                >
                  {{ v.name }}
                </span>
              </button>
            </li>
          </ul>

          <!-- 香气强度 -->
          <div class="mt-12">
            <SectionLabel class="mb-6 block">香气结构</SectionLabel>
            <div v-for="a in currentTea.aromas" :key="a.name" class="mb-4">
              <div class="flex items-baseline justify-between font-sans text-xs text-[#1C1C1C]/60">
                <span>{{ a.name }}</span>
                <span>{{ a.intensity }}</span>
              </div>
              <div class="mt-2 h-px w-full bg-[#1C1C1C]/10">
                <div
                  class="h-px bg-[#1C1C1C] transition-[width] duration-1000"
                  :style="{ width: `${a.intensity}%` }"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 右：品种详情 -->
        <div class="md:col-span-8">
          <Transition
            mode="out-in"
            enter-active-class="transition duration-500 ease-out"
            enter-from-class="opacity-0 translate-y-2"
            leave-active-class="transition duration-200 ease-in"
            leave-to-class="opacity-0"
          >
            <div :key="currentVariety.name">
              <div class="group">
                <SmartImage
                  :src="currentVariety.image"
                  :alt="currentVariety.name"
                  class="aspect-[16/10] w-full border border-[#1C1C1C]/10"
                />
              </div>

              <div class="mt-10 flex flex-wrap items-baseline justify-between gap-4">
                <h3 class="font-serif text-3xl tracking-tighter md:text-4xl">
                  {{ currentVariety.name }}
                </h3>
                <span class="font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/40 uppercase">
                  {{ currentTea.name }}
                </span>
              </div>

              <dl class="mt-10 grid grid-cols-1 gap-px border border-[#1C1C1C]/10 md:grid-cols-2">
                <div class="border-b border-[#1C1C1C]/10 p-6 md:border-r md:border-b-0">
                  <dt class="eyebrow">产地</dt>
                  <dd class="mt-3 font-sans text-sm leading-relaxed">{{ currentVariety.origin }}</dd>
                </div>
                <div class="p-6">
                  <dt class="eyebrow">冲泡</dt>
                  <dd class="mt-3 font-sans text-sm leading-relaxed">{{ currentVariety.brewing }}</dd>
                </div>
              </dl>

              <div class="mt-10">
                <SectionLabel class="mb-4 block">口感特点</SectionLabel>
                <p class="max-w-xl font-sans text-sm leading-relaxed text-[#1C1C1C]/80">
                  {{ currentVariety.taste }}
                </p>
              </div>

              <div class="mt-10">
                <SectionLabel class="mb-4 block">历史文化</SectionLabel>
                <p class="max-w-xl font-sans text-sm leading-relaxed text-[#1C1C1C]/60">
                  {{ currentVariety.history }}
                </p>
              </div>

              <div class="mt-10 border-l border-[#1C1C1C]/10 pl-6">
                <SectionLabel class="mb-3 block">温馨提示</SectionLabel>
                <p class="max-w-md font-sans text-sm leading-relaxed text-[#1C1C1C]/60 italic">
                  {{ tip }}
                </p>
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import { teaVarieties } from '@/content'

const teas = teaVarieties.teas

const selectedId = ref(teas[0].id)
const varietyIndex = ref(0)

const currentTea = computed(() => teas.find((t) => t.id === selectedId.value) || teas[0])
const currentVariety = computed(() => currentTea.value.varieties[varietyIndex.value])

function selectTea(id) {
  selectedId.value = id
  varietyIndex.value = 0
}

function setVariety(i) {
  varietyIndex.value = i
}

/** 按「茶类 + 品种」返回饮用建议 */
const TIPS = {
  绿茶: {
    蕉岭绿茶: '湿热体质更友好，脾胃虚寒者建议温饮。',
    柏塘山茶: '炒米香明显，适合饭后消食时品饮。',
    仁化白毛尖: '茶性清幽，宜静心独饮，避免空腹。',
    梅县绿茶: '栗香醇厚，陈茶更温和，适合日常口粮。',
    乐昌白毛茶: '兰蜜香高长，耐泡度极佳，宜用山泉水冲泡。'
  },
  红茶: {
    象窝红茶: '蜜香清甜，禅茶一味，适合清晨与睡前温饮。',
    鹤山红茶: '桂圆甜香，搭配广式烧腊、煲仔饭绝配。',
    开平大沙茶: '山野花香，配烘焙点心可解腻。'
  },
  青茶: {
    凤凰单丛: '香型丰富，宜用潮汕功夫茶具，100℃沸水快进快出。',
    岭头单丛: '蜜韵兰香突出，焖泡更能激发甜感。',
    石古坪乌龙: '茉莉花香清高，轻发酵，忌长时间闷泡。'
  }
}

const tip = computed(() => {
  const byTea = TIPS[currentTea.value.name] || {}
  return byTea[currentVariety.value.name] || '适量品饮，切勿浓茶空腹。'
})

/* 轮播：每 15 秒切换一个品种 */
let timer = null
function startCarousel() {
  stopCarousel()
  timer = setInterval(() => {
    varietyIndex.value = (varietyIndex.value + 1) % currentTea.value.varieties.length
  }, 15000)
}
function stopCarousel() {
  if (timer) clearInterval(timer)
  timer = null
}

watch(selectedId, () => startCarousel())

onMounted(() => {
  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (!reduce) startCarousel()
})
onUnmounted(stopCarousel)
</script>
