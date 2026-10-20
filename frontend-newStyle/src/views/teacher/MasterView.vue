<template>
  <div class="pt-20 md:pt-28">
    <div class="page-zoom container-editorial py-16 md:py-24">
      <header class="mb-16 md:mb-24">
        <SectionLabel class="mb-6 block">名师讲解</SectionLabel>
        <h1 class="font-serif text-5xl leading-[0.95] tracking-tighter md:text-7xl">茶界名师</h1>
        <p class="mt-6 max-w-md font-sans text-sm leading-relaxed text-[#1C1C1C]/60 italic md:text-base">
          制茶、茶史、品鉴 —— 从三位老师的不同视角，重新认识一片叶子。
        </p>
      </header>

      <div class="grid grid-cols-1 gap-16 md:grid-cols-12 md:gap-24">
        <!-- 左：当前名师 -->
        <div class="md:col-span-7">
          <Transition
            mode="out-in"
            enter-active-class="transition duration-500 ease-out"
            enter-from-class="opacity-0 translate-y-2"
            leave-active-class="transition duration-200 ease-in"
            leave-to-class="opacity-0"
          >
            <div :key="selected.id">
              <SmartImage
                :src="selected.image"
                :alt="selected.name"
                :fallback="'/assest/default.jpg'"
                class="aspect-[4/3] w-full border border-[#1C1C1C]/10"
              />
              <h2 class="mt-10 font-serif text-4xl tracking-tighter md:text-5xl">
                {{ selected.name }}
              </h2>
              <p class="mt-3 font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/40 uppercase">
                {{ selected.title }}
              </p>
              <p class="mt-8 max-w-xl font-sans text-sm leading-relaxed text-[#1C1C1C]/70">
                {{ selected.bio }}
              </p>

              <div class="mt-10 flex flex-wrap items-center gap-6">
                <button type="button" class="btn-editorial" @click="openVideo">
                  {{ mastersData.videoButtonText }}
                  <span aria-hidden="true">↗</span>
                </button>
                <span class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
                  专长 · {{ selected.specialty }}
                </span>
              </div>
            </div>
          </Transition>
        </div>

        <!-- 右：名师列表 -->
        <aside class="md:col-span-5">
          <SectionLabel class="mb-8 block">{{ mastersData.sectionTitle }}</SectionLabel>
          <ul class="border-t border-[#1C1C1C]/10">
            <li v-for="(m, i) in mastersData.masters" :key="m.id">
              <button
                type="button"
                class="group flex w-full items-center gap-6 border-b border-[#1C1C1C]/10 py-6 text-left transition-colors duration-300"
                :class="m.id === selectedId ? 'bg-[#1C1C1C]/[0.03]' : 'hover:bg-[#1C1C1C]/[0.02]'"
                @click="selectMaster(m.id)"
              >
                <span class="font-sans text-xs text-[#1C1C1C]/40">
                  {{ String(i + 1).padStart(2, '0') }}
                </span>
                <span class="flex-1">
                  <span class="block font-serif text-xl tracking-tighter md:text-2xl">
                    {{ m.name }}
                  </span>
                  <span class="mt-1 block font-sans text-xs text-[#1C1C1C]/50">
                    {{ m.specialty }}
                  </span>
                </span>
                <span
                  class="font-sans text-[#1C1C1C]/30 transition-all duration-300 group-hover:translate-x-2 group-hover:text-[#1C1C1C]"
                  aria-hidden="true"
                >
                  →
                </span>
              </button>
            </li>
          </ul>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import { masters as mastersData } from '@/content'

const selectedId = ref(mastersData.masters[0].id)
const selected = computed(
  () => mastersData.masters.find((m) => m.id === selectedId.value) || mastersData.masters[0]
)

function selectMaster(id) {
  selectedId.value = id
}

function openVideo() {
  const url = selected.value.videoUrl
  if (url) window.open(url, '_blank', 'noopener')
}
</script>
