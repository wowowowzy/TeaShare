<template>
  <div class="fixed inset-0 z-[70] flex items-center justify-center bg-[#1C1C1C]/50 p-4 md:p-6">
    <div class="flex max-h-[92vh] w-full max-w-5xl flex-col border border-[#1C1C1C]/15 bg-[#F9F8F6]">
      <!-- 头部 -->
      <header class="flex items-center justify-between border-b border-[#1C1C1C]/10 px-8 py-6">
        <div>
          <p class="font-serif text-2xl tracking-tighter">趣味问答</p>
          <p class="mt-1 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
            共 {{ questions.length }} 题 · 答对 3 道以上可获得优惠券
          </p>
        </div>
        <button
          type="button"
          class="font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/50 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
          @click="$emit('close')"
        >
          退出
        </button>
      </header>

      <!-- 主体 -->
      <div class="scroll-thin flex-1 overflow-y-auto px-8 py-8">
        <SkeletonBlock v-if="loading" :rows="8" />

        <EmptyState
          v-else-if="error"
          title="题目加载失败"
          :description="error"
        >
          <button type="button" class="btn-editorial-ghost" @click="load">重新加载</button>
        </EmptyState>

        <EmptyState
          v-else-if="!questions.length"
          title="暂无题目"
          description="题库为空，请稍后再试。"
        />

        <!-- 答题中 -->
        <div v-else-if="!submitted" class="grid grid-cols-1 gap-10 md:grid-cols-12">
          <!-- 题目导航 -->
          <aside class="md:col-span-3">
            <SectionLabel class="mb-4 block">题目导航</SectionLabel>
            <div class="grid grid-cols-5 gap-px border border-[#1C1C1C]/10 md:grid-cols-4">
              <button
                v-for="(q, i) in questions"
                :key="q.questionId"
                type="button"
                class="aspect-square border border-[#1C1C1C]/10 font-sans text-xs transition-colors duration-300"
                :class="navClass(i)"
                @click="current = i"
              >
                {{ i + 1 }}
              </button>
            </div>
          </aside>

          <!-- 当前题目 -->
          <div class="md:col-span-9">
            <p class="font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/40 uppercase">
              第 {{ current + 1 }} / {{ questions.length }} 题
            </p>
            <h3 class="mt-4 font-serif text-2xl leading-snug tracking-tight md:text-3xl">
              {{ currentQuestion.questionContent }}
            </h3>

            <ul class="mt-8">
              <li v-for="(opt, i) in currentQuestion.options" :key="i">
                <button
                  type="button"
                  class="group flex w-full items-baseline gap-4 border-b border-[#1C1C1C]/10 py-4 text-left transition-colors duration-300 hover:border-[#1C1C1C]/40 disabled:cursor-default"
                  :disabled="answers[currentQuestion.questionId] != null"
                  @click="selectOption(i)"
                >
                  <span
                    class="font-sans text-xs tracking-[0.15em] uppercase"
                    :class="
                      answers[currentQuestion.questionId] === labelOf(i)
                        ? 'text-[#1C1C1C]'
                        : 'text-[#1C1C1C]/40'
                    "
                  >
                    {{ labelOf(i) }}
                  </span>
                  <span
                    class="font-sans text-sm leading-relaxed transition-all duration-500 group-hover:italic md:text-base"
                    :class="
                      answers[currentQuestion.questionId] === labelOf(i)
                        ? 'text-[#1C1C1C]'
                        : 'text-[#1C1C1C]/70'
                    "
                  >
                    {{ stripLabel(opt) }}
                  </span>
                  <span
                    v-if="answers[currentQuestion.questionId] === labelOf(i)"
                    class="ml-auto font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/60 uppercase"
                  >
                    已选
                  </span>
                </button>
              </li>
            </ul>

            <div class="mt-10 flex items-center justify-between">
              <button
                type="button"
                class="btn-editorial-ghost !px-4 !py-2 text-xs"
                :disabled="current === 0"
                @click="current--"
              >
                上一题
              </button>
              <button
                type="button"
                class="btn-editorial-ghost !px-4 !py-2 text-xs"
                :disabled="current === questions.length - 1"
                @click="current++"
              >
                下一题
              </button>
            </div>
          </div>
        </div>

        <!-- 答题结果 -->
        <div v-else>
          <div class="border border-[#1C1C1C]/10 p-10 text-center">
            <p class="eyebrow">答题结果</p>
            <p class="mt-6 font-serif text-6xl tracking-tighter md:text-7xl">
              {{ correctCount }} <span class="text-[#1C1C1C]/30">/ {{ questions.length }}</span>
            </p>
            <p v-if="resultMsg" class="mt-6 font-sans text-sm leading-relaxed text-[#1C1C1C]/60">
              {{ resultMsg }}
            </p>
          </div>

          <!-- 逐题解析 -->
          <div class="mt-10">
            <SectionLabel class="mb-6 block">逐题解析</SectionLabel>
            <div
              v-for="(q, i) in questions"
              :key="q.questionId"
              class="border-b border-[#1C1C1C]/10 py-6"
            >
              <div class="flex items-baseline gap-4">
                <span class="font-sans text-xs text-[#1C1C1C]/40">
                  {{ String(i + 1).padStart(2, '0') }}
                </span>
                <p class="font-serif text-lg tracking-tight md:text-xl">{{ q.questionContent }}</p>
              </div>
              <p class="mt-3 pl-8 font-sans text-xs tracking-[0.1em] text-[#1C1C1C]/60">
                正确答案 ·
                <span class="text-[#1C1C1C]">{{ correctLabel(q.questionId) }}</span>
                <span v-if="answers[q.questionId]" class="ml-4">
                  你的答案 ·
                  <span
                    :class="
                      answers[q.questionId] === correctLabel(q.questionId)
                        ? 'text-[#1C1C1C]'
                        : 'text-[#1C1C1C]/60 line-through'
                    "
                  >
                    {{ answers[q.questionId] }}
                  </span>
                </span>
              </p>
              <p
                v-if="explanations[q.questionId]"
                class="mt-3 pl-8 font-sans text-sm leading-relaxed text-[#1C1C1C]/70"
              >
                解析：{{ explanations[q.questionId] }}
              </p>
              <p
                v-else-if="loadingExplanation[q.questionId]"
                class="mt-3 pl-8 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase"
              >
                正在获取 AI 解析…
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部 -->
      <footer class="flex items-center justify-end gap-4 border-t border-[#1C1C1C]/10 px-8 py-6">
        <button
          type="button"
          class="btn-editorial-ghost !px-4 !py-2 text-xs"
          @click="$emit('close')"
        >
          退出
        </button>
        <button
          v-if="!submitted"
          type="button"
          class="btn-editorial"
          :disabled="!allAnswered || submitting"
          @click="submitQuiz"
        >
          {{ submitting ? '提交中…' : '提交答案' }}
        </button>
        <button v-else type="button" class="btn-editorial" @click="$emit('close')">完成</button>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { getQuizQuestions, submitQuizAnswers } from '@/api/quiz'
import { createNewSession, generateStream } from '@/api/ai'
import { useUserStore } from '@/stores/user'

const emit = defineEmits(['close'])
const userStore = useUserStore()

const questions = ref([])
const answers = reactive({})
const loading = ref(true)
const error = ref('')
const submitting = ref(false)
const submitted = ref(false)
const current = ref(0)

const correctMap = ref({}) // questionId -> optionLabel
const resultMsg = ref('')
const explanations = reactive({})
const loadingExplanation = reactive({})

const currentQuestion = computed(() => questions.value[current.value] || {})
const allAnswered = computed(() =>
  questions.value.every((q) => answers[q.questionId] != null)
)
const correctCount = computed(
  () =>
    questions.value.filter((q) => answers[q.questionId] === correctMap.value[q.questionId]).length
)

function labelOf(i) {
  return String.fromCharCode(65 + i)
}
function stripLabel(opt) {
  return String(opt).replace(/^[A-Za-z][.、:：]\s*/, '')
}

function navClass(i) {
  const q = questions.value[i]
  const picked = answers[q.questionId]
  if (i === current.value) return 'bg-[#1C1C1C] text-[#F9F8F6]'
  if (submitted.value && picked) {
    return picked === correctMap.value[q.questionId]
      ? 'text-[#1C1C1C] font-medium'
      : 'text-[#1C1C1C]/40 line-through'
  }
  return picked ? 'bg-[#1C1C1C]/[0.06] text-[#1C1C1C]' : 'text-[#1C1C1C]/40 hover:text-[#1C1C1C]'
}

function selectOption(i) {
  if (answers[currentQuestion.value.questionId] != null) return
  answers[currentQuestion.value.questionId] = labelOf(i)
}

function correctLabel(questionId) {
  return correctMap.value[questionId] || '—'
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await getQuizQuestions()
    questions.value = Array.isArray(res) ? res : []
  } catch (e) {
    error.value = e?.message || '请检查网络连接'
  } finally {
    loading.value = false
  }
}

async function submitQuiz() {
  submitting.value = true
  try {
    const ids = questions.value.map((q) => q.questionId)
    const labels = ids.map((id) => answers[id])
    const res = await submitQuizAnswers(ids, labels)

    // 容错解析正确答案
    const list = res?.answerList || []
    list.forEach((item) => {
      if (item?.questionId != null) correctMap.value[item.questionId] = item.optionLabel
    })
    resultMsg.value = res?.msg || ''
    submitted.value = true

    // 串行请求 AI 解析，避免并发压垮后端
    for (const q of questions.value) {
      await loadExplanation(q)
    }
  } catch {
    /* 拦截器已提示 */
  } finally {
    submitting.value = false
  }
}

async function loadExplanation(q) {
  const correct = correctMap.value[q.questionId]
  const correctOption = q.options.find((o) => String(o).startsWith(`${correct}.`)) || ''
  const prompt = `请简要解析这道茶文化题目：\n题目：${q.questionContent}\n正确答案：${correct}. ${stripLabel(correctOption)}\n请用50字以内的中文给出解析。`

  loadingExplanation[q.questionId] = true
  let buffer = ''
  try {
    let sessionId = userStore.getSessionId
    if (!sessionId) {
      sessionId = await createNewSession()
      userStore.setSessionId(sessionId)
    }
    await generateStream(prompt, sessionId, null, (chunk) => {
      buffer += chunk
      explanations[q.questionId] = buffer
    })
  } catch {
    explanations[q.questionId] = ''
  } finally {
    loadingExplanation[q.questionId] = false
  }
}

onMounted(load)
</script>
