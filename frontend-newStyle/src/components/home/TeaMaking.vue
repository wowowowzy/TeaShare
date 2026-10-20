<template>
  <section id="tea-making" class="section-editorial border-t border-[#1C1C1C]/10">
    <div class="container-editorial">
      <header class="mb-16 md:mb-24">
        <SectionLabel class="mb-6 block">02 — 制茶品茗</SectionLabel>
        <h2 class="font-serif text-4xl leading-[1.05] tracking-tighter md:text-6xl">传承匠心技艺</h2>
        <p class="mt-6 max-w-md font-sans text-sm leading-relaxed text-[#1C1C1C]/60 italic md:text-base">
          从鲜叶到干茶，每一步都是时间与温度的博弈。
        </p>

        <!-- 茶种选择 -->
        <div class="mt-10 flex flex-wrap gap-3">
          <button
            v-for="t in teaTypes"
            :key="t.value"
            type="button"
            class="border px-5 py-2 font-sans text-xs tracking-[0.2em] uppercase transition-colors duration-300"
            :class="
              t.value === selectedType
                ? 'border-[#1C1C1C] bg-[#1C1C1C] text-[#F9F8F6]'
                : 'border-[#1C1C1C]/20 text-[#1C1C1C]/60 hover:border-[#1C1C1C] hover:text-[#1C1C1C]'
            "
            @click="selectTeaType(t.value)"
          >
            {{ t.label }}
          </button>
        </div>
      </header>

      <!-- 工艺流程图：底图 + 绝对定位点位 -->
      <div class="relative border border-[#1C1C1C]/10">
        <SmartImage
          :src="flowchartImage"
          alt="制茶工艺流程图"
          loading="eager"
          class="aspect-[16/9] w-full"
        />
        <button
          v-for="(p, i) in stepPoints"
          :key="i"
          type="button"
          class="absolute -translate-x-1/2 -translate-y-1/2"
          :style="{ left: `${p.x}%`, top: `${p.y}%` }"
          :aria-label="steps[i]?.name"
          @click="selectStep(i)"
        >
          <span
            class="flex h-9 w-9 items-center justify-center border font-sans text-xs transition-colors duration-300 md:h-10 md:w-10"
            :class="
              i === currentStep
                ? 'border-[#1C1C1C] bg-[#1C1C1C] text-[#F9F8F6]'
                : 'border-[#1C1C1C]/40 bg-[#F9F8F6]/90 text-[#1C1C1C] hover:border-[#1C1C1C]'
            "
          >
            {{ i + 1 }}
          </span>
          <span
            class="mt-2 hidden whitespace-nowrap font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/70 md:block"
          >
            {{ steps[i]?.name }}
          </span>
        </button>
      </div>

      <!-- 当前步骤详情 -->
      <Transition
        mode="out-in"
        enter-active-class="transition duration-500 ease-out"
        enter-from-class="opacity-0 translate-y-2"
        leave-active-class="transition duration-200 ease-in"
        leave-to-class="opacity-0"
      >
        <div :key="`${selectedType}-${currentStep}`" class="mt-16 grid grid-cols-1 gap-12 md:grid-cols-12 md:gap-16">
          <div class="md:col-span-7">
            <div class="flex items-baseline gap-6">
              <span class="font-sans text-xs text-[#1C1C1C]/40">
                {{ String(currentStep + 1).padStart(2, '0') }} / {{ String(steps.length).padStart(2, '0') }}
              </span>
              <h3 class="font-serif text-3xl tracking-tighter md:text-4xl">{{ step.name }}</h3>
            </div>

            <p class="mt-4 font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/40 uppercase">
              耗时 · {{ step.time }}
            </p>

            <p class="mt-8 max-w-xl font-sans text-sm leading-relaxed text-[#1C1C1C]/80">
              {{ step.purpose }}
            </p>

            <div class="mt-10">
              <SectionLabel class="mb-4 block">工艺要求</SectionLabel>
              <ul>
                <li
                  v-for="(r, i) in step.requirements"
                  :key="i"
                  class="border-b border-[#1C1C1C]/10 py-3 font-sans text-sm leading-relaxed text-[#1C1C1C]/70"
                >
                  {{ r }}
                </li>
              </ul>
            </div>

            <div class="mt-10 border-l border-[#1C1C1C]/10 pl-6">
              <SectionLabel class="mb-3 block">判断标准</SectionLabel>
              <p class="max-w-xl font-sans text-sm leading-relaxed">{{ step.standard }}</p>
            </div>

            <p class="mt-8 max-w-xl font-sans text-sm leading-relaxed text-[#1C1C1C]/60 italic">
              {{ step.tips }}
            </p>
          </div>

          <div class="md:col-span-5">
            <SmartImage
              :src="step.image"
              :alt="step.name"
              class="aspect-[4/3] w-full border border-[#1C1C1C]/10"
            />

            <!-- 发酵对比表（发酵步骤专属） -->
            <div v-if="step.name === '发酵'" class="mt-10">
              <SectionLabel class="mb-4 block">六大茶类发酵对比</SectionLabel>
              <table class="w-full border-collapse text-left">
                <thead>
                  <tr>
                    <th
                      v-for="h in fermentation.headers"
                      :key="h"
                      class="border-b border-[#1C1C1C]/20 pb-3 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase"
                    >
                      {{ h }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(row, i) in fermentation.rows" :key="i">
                    <td
                      v-for="(cell, j) in row"
                      :key="j"
                      class="border-b border-[#1C1C1C]/10 py-3 font-sans text-sm text-[#1C1C1C]/80"
                    >
                      {{ cell }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </Transition>

      <!-- 大师访谈 -->
      <div class="mt-24 border-t border-[#1C1C1C]/10 pt-16 md:mt-40 md:pt-24">
        <div class="grid grid-cols-1 gap-12 md:grid-cols-12 md:gap-16">
          <div class="md:col-span-7">
            <SectionLabel class="mb-6 block">{{ interview.sectionTitle }}</SectionLabel>
            <div
              class="group relative cursor-pointer border border-[#1C1C1C]/10"
              role="button"
              tabindex="0"
              @click="playVideo"
              @keydown.enter="playVideo"
            >
              <template v-if="!playerVisible">
                <SmartImage
                  :src="interview.coverImage"
                  alt="品茶大师访谈视频封面"
                  class="aspect-video w-full"
                />
                <div
                  class="absolute inset-0 flex items-center justify-center bg-[#1C1C1C]/30 transition-colors duration-500 group-hover:bg-[#1C1C1C]/50"
                >
                  <span
                    class="flex h-16 w-16 items-center justify-center border border-[#F9F8F6] font-sans text-xl text-[#F9F8F6] transition-transform duration-500 group-hover:scale-105"
                  >
                    ▶
                  </span>
                </div>
              </template>
              <video
                v-else
                ref="videoEl"
                :src="interview.videoSource"
                class="aspect-video w-full"
                controls
                autoplay
                playsinline
              />
            </div>
            <p class="mt-4 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40">
              {{ interview.sectionHint }}
            </p>
          </div>

          <div class="flex flex-col justify-between md:col-span-5">
            <div>
              <div class="flex items-center gap-6">
                <SmartImage
                  :src="interview.avatar"
                  :alt="interview.masterName"
                  class="h-16 w-16 shrink-0 border border-[#1C1C1C]/10"
                />
                <div>
                  <p class="font-serif text-2xl tracking-tighter md:text-3xl">
                    {{ interview.masterName }}
                  </p>
                  <p class="mt-1 font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/40 uppercase">
                    {{ interview.masterTitle }}
                  </p>
                </div>
              </div>

              <p class="mt-8 max-w-md font-sans text-sm leading-relaxed text-[#1C1C1C]/60">
                {{ interview.masterBio }}
              </p>

              <blockquote class="mt-8 border-l border-[#1C1C1C]/10 pl-6">
                <p class="font-serif text-xl leading-relaxed tracking-tight italic md:text-2xl">
                  「{{ interview.masterQuote }}」
                </p>
              </blockquote>
            </div>

            <RouterLink :to="interview.ctaRoute" class="btn-editorial-ghost mt-10 self-start">
              {{ interview.ctaText }}
              <span aria-hidden="true" class="transition-transform duration-300 group-hover:translate-x-1">→</span>
            </RouterLink>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import { teaMaking } from '@/content'

const teaTypes = teaMaking.teaTypes
const flowchartImage = teaMaking.flowchartImage
const fermentation = teaMaking.fermentationTable
const interview = teaMaking.masterInterview

const selectedType = ref('green')
const currentStep = ref(0)
const playerVisible = ref(false)
const videoEl = ref(null)

const steps = computed(() => teaMaking.steps[selectedType.value] || [])
const stepPoints = computed(() => teaMaking.stepPointsMap[selectedType.value] || [])
const step = computed(() => steps.value[currentStep.value] || steps.value[0])

function selectTeaType(value) {
  selectedType.value = value
  currentStep.value = 0
}

function selectStep(i) {
  currentStep.value = i
}

async function playVideo() {
  playerVisible.value = true
  await nextTick()
  videoEl.value?.play?.().catch(() => {
    /* 浏览器可能拦截自动播放，用户可手动点击播放 */
  })
}
</script>
