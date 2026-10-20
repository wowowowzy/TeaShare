<template>
  <section id="tea-history" class="section-editorial border-t border-[#1C1C1C]/10">
    <div class="container-editorial">
      <header class="mb-16 md:mb-24">
        <SectionLabel class="mb-6 block">03 — 茶源历史</SectionLabel>
        <h2 class="font-serif text-4xl leading-[1.05] tracking-tighter md:text-6xl">
          {{ history.sectionTitle }}
        </h2>
        <p class="mt-6 max-w-md font-sans text-sm leading-relaxed text-[#1C1C1C]/60 italic md:text-base">
          {{ history.sectionSubtitle }}
        </p>
      </header>

      <!-- 四朝网格 -->
      <div
        ref="gridEl"
        class="grid grid-cols-1 gap-px border border-[#1C1C1C]/10 sm:grid-cols-2 lg:grid-cols-4"
      >
        <button
          v-for="(card, i) in history.cards"
          :key="card.id"
          type="button"
          class="group border-b border-[#1C1C1C]/10 p-6 text-left transition-all duration-700 sm:border-r lg:border-b-0"
          :class="[
            revealed ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0',
            'hover:bg-[#1C1C1C]/[0.02]'
          ]"
          :style="{ transitionDelay: `${i * 120}ms` }"
          @click="openDetail(card.id)"
        >
          <span class="font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/40 uppercase">
            {{ String(i + 1).padStart(2, '0') }}
          </span>
          <SmartImage :src="card.image" :alt="card.dynasty" class="mt-6 aspect-[3/4] w-full" />
          <p class="mt-6 font-serif text-3xl tracking-tighter">{{ card.dynasty }}</p>
          <p class="mt-2 font-sans text-sm leading-relaxed text-[#1C1C1C]/60 transition-all duration-500 group-hover:italic">
            {{ card.title }}
          </p>
          <span class="mt-6 inline-block font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/40 uppercase">
            查看详情
            <span aria-hidden="true" class="inline-block transition-transform duration-300 group-hover:translate-x-2">→</span>
          </span>
        </button>
      </div>
    </div>

    <!-- 详情弹窗：结构化渲染（不再 v-html，规避 XSS） -->
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="opacity-0"
      leave-active-class="transition duration-200 ease-in"
      leave-to-class="opacity-0"
    >
      <div
        v-if="selected"
        class="fixed inset-0 z-[60] flex items-center justify-center overflow-y-auto bg-[#1C1C1C]/40 p-6 md:p-12"
        role="dialog"
        aria-modal="true"
        :aria-label="selected.title"
        @click.self="closeDetail"
      >
        <div class="relative w-full max-w-5xl border border-[#1C1C1C]/15 bg-[#F9F8F6]">
          <button
            type="button"
            class="absolute top-4 right-4 z-10 flex h-10 w-10 items-center justify-center font-sans text-lg text-[#1C1C1C]/60 transition-colors duration-300 hover:text-[#1C1C1C]"
            aria-label="关闭"
            @click="closeDetail"
          >
            ✕
          </button>

          <div class="grid grid-cols-1 md:grid-cols-2">
            <div class="border-b border-[#1C1C1C]/10 p-8 md:border-r md:border-b-0 md:p-12">
              <h3 class="font-serif text-3xl tracking-tighter md:text-4xl">{{ selected.title }}</h3>
              <div
                v-for="(s, i) in selected.sections"
                :key="i"
                class="mt-8 border-t border-[#1C1C1C]/10 pt-6"
              >
                <p class="font-serif text-lg tracking-tight md:text-xl">{{ s.subtitle }}</p>
                <p class="mt-3 font-sans text-sm leading-relaxed text-[#1C1C1C]/70">
                  {{ s.text }}
                </p>
              </div>
            </div>

            <figure class="p-8 md:p-12">
              <SmartImage
                :src="selected.image"
                :alt="selected.imgCaption"
                class="aspect-[4/3] w-full border border-[#1C1C1C]/10"
              />
              <figcaption class="mt-4 font-sans text-xs leading-relaxed tracking-[0.1em] text-[#1C1C1C]/40 italic">
                {{ selected.imgCaption }}
              </figcaption>
            </figure>
          </div>
        </div>
      </div>
    </Transition>
  </section>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import { teaHistory } from '@/content'

const history = teaHistory
const selectedId = ref(null)
const revealed = ref(false)
const gridEl = ref(null)

const selected = computed(() => (selectedId.value ? history.details[selectedId.value] : null))

function openDetail(id) {
  selectedId.value = id
  document.body.style.overflow = 'hidden'
}
function closeDetail() {
  selectedId.value = null
  document.body.style.overflow = ''
}

/* 首次进入视口触发卡片依次淡入 */
let observer = null
onMounted(() => {
  if (!gridEl.value) return
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((e) => {
        if (e.isIntersecting) {
          revealed.value = true
          observer?.disconnect()
        }
      })
    },
    { threshold: 0.2, rootMargin: '0px 0px -50px 0px' }
  )
  observer.observe(gridEl.value)
})
onUnmounted(() => {
  observer?.disconnect()
  document.body.style.overflow = ''
})
</script>
