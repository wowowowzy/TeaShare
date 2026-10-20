<template>
  <!--
    对话页：撑满视口剩余高度，避免内容较短时下方留白/露出页脚。
    高度用 calc(100vh - 导航高度) 而非固定值；zoom 作用于子元素，
    因此 100vh 以未缩放的布局坐标计算，结果稳定。
  -->
  <!--
    对话页：外层占满 100vh（含顶部 5rem 导航留白），内层再 flex 撑满剩余空间。
    这样输入区贴在视口底部，内容再少也不会露出下方页脚或大片空白。
  -->
  <div class="flex flex-col pt-20 md:pt-28" :style="{ height: '100vh' }">
    <div class="page-zoom container-editorial flex w-full flex-1 flex-col py-8 md:py-10">
      <!-- 头部 -->
      <header class="flex flex-wrap items-center justify-between gap-6 border-b border-[#1C1C1C]/10 pb-8">
        <div class="flex items-center gap-5">
          <img
            :src="aiAvatar"
            alt="小茗"
            class="h-14 w-14 border border-[#1C1C1C]/10 object-cover"
          />
          <div>
            <p class="font-serif text-2xl tracking-tighter md:text-3xl">小茗</p>
            <p class="mt-1 font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/40 uppercase">
              您的茶文化智能顾问
            </p>
          </div>
        </div>

        <div v-if="userStore.isLoggedIn" class="flex gap-6">
          <button
            type="button"
            class="link-underline font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/60 uppercase"
            :disabled="creatingSession"
            @click="newSession"
          >
            {{ creatingSession ? '创建中…' : '新建对话' }}
          </button>
          <button
            type="button"
            class="link-underline font-sans text-xs tracking-[0.2em] text-[#1C1C1C]/60 uppercase"
            @click="openHistory"
          >
            历史查询
          </button>
        </div>
      </header>

      <!-- 未登录 -->
      <div v-if="!userStore.isLoggedIn" class="py-24 text-center md:py-40">
        <h2 class="font-serif text-4xl tracking-tighter md:text-5xl">请先登录</h2>
        <p class="mt-6 font-sans text-sm leading-relaxed text-[#1C1C1C]/60 italic">
          智能问答功能需要登录后才能使用。
        </p>
        <RouterLink to="/login" class="btn-editorial mt-10">去登录</RouterLink>
      </div>

      <template v-else>
        <!-- 消息区：flex-1 自动撑满，内容少时不塌陷 -->
        <div
          ref="messagesEl"
          class="scroll-thin mt-8 min-h-96 flex-1 overflow-y-auto pr-2"
          aria-live="polite"
        >
          <!-- 空态欢迎 -->
          <div v-if="!messages.length" class="py-12">
            <p class="max-w-md font-sans text-sm leading-relaxed text-[#1C1C1C]/70">
              您好！我是您的茶文化智能顾问，可解答关于茶叶历史、品类、冲泡、贸易等相关问题。
            </p>
            <div class="mt-10">
              <SectionLabel class="mb-4 block">试试这样问</SectionLabel>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="q in sampleQuestions"
                  :key="q"
                  type="button"
                  class="border border-[#1C1C1C]/20 px-4 py-2 font-sans text-xs text-[#1C1C1C]/70 transition-colors duration-300 hover:border-[#1C1C1C] hover:text-[#1C1C1C]"
                  @click="askSample(q)"
                >
                  {{ q }}
                </button>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <ul v-else class="space-y-10">
            <li
              v-for="(m, i) in messages"
              :key="i"
              :class="m.type === 'user' ? 'flex justify-end' : 'flex justify-start'"
            >
              <div :class="m.type === 'user' ? 'max-w-xl text-right' : 'max-w-2xl'">
                <p class="eyebrow mb-3">
                  {{ m.type === 'user' ? '我' : '小茗' }}
                </p>
                <div
                  class="inline-block border p-5 text-left"
                  :class="
                    m.type === 'user'
                      ? 'border-[#1C1C1C] bg-[#1C1C1C] text-[#F9F8F6]'
                      : 'border-[#1C1C1C]/10 bg-transparent'
                  "
                >
                  <img
                    v-if="m.imageUrl"
                    :src="m.imageUrl"
                    alt="上传的图片"
                    class="mb-4 max-h-64 border border-current/20 object-cover"
                  />
                  <div
                    class="prose-editorial font-sans text-sm leading-relaxed"
                    v-html="formatMessageContent(m.content)"
                  />
                </div>
              </div>
            </li>

            <!-- 思考中 -->
            <li v-if="loading" class="flex justify-start">
              <div>
                <p class="eyebrow mb-3">小茗</p>
                <p class="font-sans text-sm tracking-[0.1em] text-[#1C1C1C]/40">
                  正在思考<span class="animate-pulse">…</span>
                </p>
              </div>
            </li>
          </ul>
        </div>

        <!-- 输入区 -->
        <div class="mt-8 shrink-0 border-t border-[#1C1C1C]/10 pt-8">
          <!-- 图片预览 -->
          <div v-if="selectedImage" class="mb-6 flex items-center gap-4">
            <div class="relative h-20 w-20 border border-[#1C1C1C]/10">
              <img :src="imagePreviewUrl" alt="待发送图片" class="h-full w-full object-cover" />
              <button
                type="button"
                class="absolute -top-2 -right-2 flex h-5 w-5 items-center justify-center border border-[#1C1C1C] bg-[#F9F8F6] font-sans text-xs"
                aria-label="移除图片"
                @click="clearImage"
              >
                ✕
              </button>
            </div>
            <span class="font-sans text-xs text-[#1C1C1C]/40">图片将在发送时一并上传</span>
          </div>

          <div class="flex items-end gap-6">
            <button
              type="button"
              class="flex h-11 w-11 shrink-0 items-center justify-center border border-[#1C1C1C]/20 font-sans text-lg text-[#1C1C1C]/50 transition-colors duration-300 hover:border-[#1C1C1C] hover:text-[#1C1C1C]"
              aria-label="上传图片"
              @click="fileInput?.click()"
            >
              ＋
            </button>
            <input
              ref="fileInput"
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              class="hidden"
              @change="onFileChange"
            />

            <div class="flex-1">
              <textarea
                v-model="inputMessage"
                rows="1"
                class="field-input resize-none"
                placeholder="请输入您想咨询的茶文化问题…"
                @keydown.enter.exact.prevent="sendMessage"
              />
              <p class="mt-2 font-sans text-xs text-[#1C1C1C]/30">
                支持 JPG、PNG、WebP、GIF 格式图片，大小不超过 5MB。Enter 发送，Shift+Enter 换行。
              </p>
            </div>

            <button
              type="button"
              class="btn-editorial shrink-0"
              :disabled="loading || (!inputMessage.trim() && !selectedImage)"
              @click="sendMessage"
            >
              发送
            </button>
          </div>
        </div>
      </template>
    </div>

    <!-- 历史记录弹窗 -->
    <el-dialog
      v-model="showHistoryDialog"
      title="历史查询记录"
      width="90%"
      style="max-width: 560px"
    >
      <SkeletonBlock v-if="loadingHistory" :rows="4" />
      <p v-else-if="!histories.length" class="py-8 text-center font-sans text-sm text-[#1C1C1C]/50">
        暂无历史记录，开始与小茗对话，历史记录将在这里显示。
      </p>
      <ul v-else class="max-h-[60vh] space-y-px overflow-y-auto scroll-thin">
        <li v-for="h in histories" :key="h.sessionId">
          <button
            type="button"
            class="w-full border border-[#1C1C1C]/10 p-4 text-left transition-colors duration-300 hover:border-[#1C1C1C]"
            @click="loadHistorySession(h)"
          >
            <p class="font-serif text-lg tracking-tight">{{ h.title }}</p>
            <p class="mt-1 font-sans text-xs text-[#1C1C1C]/40">
              {{ h.time }} · {{ h.count }} 条消息
            </p>
          </button>
        </li>
      </ul>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import { createNewSession, generateStream, getHistories } from '@/api/ai'
import { useUserStore } from '@/stores/user'
import { asset } from '@/utils/apiBase'

const userStore = useUserStore()
const aiAvatar = computed(() => asset('/assest/number1.png'))

const sampleQuestions = ['唐代茶文化特点', '六大茶类的区别', '明清茶叶贸易史']

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const creatingSession = ref(false)

const messagesEl = ref(null)
const fileInput = ref(null)
const selectedImage = ref(null)
const imagePreviewUrl = ref('')

const showHistoryDialog = ref(false)
const histories = ref([])
const loadingHistory = ref(false)
const rawHistories = ref({})

function scrollToBottom() {
  nextTick(() => {
    if (messagesEl.value) messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  })
}

function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!/^image\/(jpeg|png|webp|gif)$/.test(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、WebP、GIF 格式')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  selectedImage.value = file
  if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value)
  imagePreviewUrl.value = URL.createObjectURL(file)
}

function clearImage() {
  selectedImage.value = null
  if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value)
  imagePreviewUrl.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

async function ensureSession() {
  if (userStore.getSessionId) return userStore.getSessionId
  creatingSession.value = true
  try {
    const id = await createNewSession()
    userStore.setSessionId(id)
    return id
  } finally {
    creatingSession.value = false
  }
}

function askSample(q) {
  inputMessage.value = q
  sendMessage()
}

async function sendMessage() {
  const text = inputMessage.value.trim()
  if (!text && !selectedImage.value) return
  if (loading.value) return

  const image = selectedImage.value
  const imageUrl = imagePreviewUrl.value

  messages.value.push({ type: 'user', content: text, imageUrl: image ? imageUrl : '' })
  inputMessage.value = ''
  selectedImage.value = null
  imagePreviewUrl.value = ''
  if (fileInput.value) fileInput.value.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const sessionId = await ensureSession()

    let aiIndex = -1
    let buffer = ''
    await generateStream(
      text,
      sessionId,
      image,
      (chunk) => {
        buffer += chunk
        if (aiIndex === -1) {
          // 首个 chunk 到达即创建 AI 消息，实现流式渲染
          messages.value.push({ type: 'assistant', content: buffer })
          aiIndex = messages.value.length - 1
        } else {
          messages.value[aiIndex] = { type: 'assistant', content: buffer }
        }
        scrollToBottom()
      },
      () => {
        loading.value = false
        scrollToBottom()
      },
      () => {
        loading.value = false
      }
    )
  } catch (e) {
    // 上游大模型异常（如 Connection reset）时后端会返回错误体，
    // 这里不把裸 JSON 渲染成回复，改为移除空气泡并给用户可读提示
    const last = messages.value[messages.value.length - 1]
    if (last && last.type === 'assistant' && !last.content) {
      messages.value.pop()
    }
    ElMessage.error(e?.message || 'AI 服务暂时不可用，请稍后重试')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

async function newSession() {
  creatingSession.value = true
  try {
    const id = await createNewSession()
    userStore.setSessionId(id)
    messages.value = []
    ElMessage.success('已开始新对话')
  } catch {
    /* 拦截器已提示 */
  } finally {
    creatingSession.value = false
  }
}

async function openHistory() {
  showHistoryDialog.value = true
  loadingHistory.value = true
  try {
    const res = await getHistories()
    rawHistories.value = res && typeof res === 'object' ? res : {}
    // 按 sessionId 分组 → 组装标题/时间/条数，按最新时间倒序
    const list = Object.entries(rawHistories.value).map(([sessionId, items]) => {
      const arr = Array.isArray(items) ? items : []
      const firstUser = arr.find((m) => m.role === 'user')
      const title = firstUser?.content
        ? String(firstUser.content).slice(0, 50)
        : `会话 ${String(sessionId).slice(-6)}`
      const last = arr[arr.length - 1]
      return {
        sessionId,
        title,
        time: last?.datetime ? new Date(last.datetime).toLocaleString('zh-CN') : '',
        count: arr.length,
        messages: arr
      }
    })
    histories.value = list.sort((a, b) => new Date(b.time || 0) - new Date(a.time || 0))
  } catch {
    histories.value = []
  } finally {
    loadingHistory.value = false
  }
}

function loadHistorySession(h) {
  userStore.setSessionId(h.sessionId)
  messages.value = (h.messages || [])
    .filter((m) => m.role === 'user' || m.role === 'assistant')
    .map((m) => ({ type: m.role === 'user' ? 'user' : 'assistant', content: m.content }))
  showHistoryDialog.value = false
  scrollToBottom()
}

/** 先 HTML 转义防 XSS，再处理 **粗体**、换行、列表 */
function formatMessageContent(raw) {
  if (!raw) return ''
  const escaped = String(raw)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

  return escaped
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .split('\n')
    .map((line) => {
      const t = line.trim()
      if (/^\d+[.、]\s*/.test(t)) return `<p class="ml-4">${t}</p>`
      if (/^[-•]\s*/.test(t)) return `<p class="ml-4">· ${t.replace(/^[-•]\s*/, '')}</p>`
      return t ? `<p>${t}</p>` : '<p class="h-2"></p>'
    })
    .join('')
}

onMounted(() => {
  if (userStore.isLoggedIn) ensureSession()
})
</script>

<style scoped>
.prose-editorial :deep(p) {
  margin: 0 0 0.5em;
}
.prose-editorial :deep(p:last-child) {
  margin-bottom: 0;
}
.prose-editorial :deep(strong) {
  font-weight: 500;
}
</style>
