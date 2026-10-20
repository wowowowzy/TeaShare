<template>
  <div class="pt-20 md:pt-28">
    <div class="container-editorial py-16 md:py-24">
      <nav class="mb-12 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
        <RouterLink to="/community" class="link-underline">茶友社</RouterLink>
        <span class="mx-3">/</span>
        <span>发布新帖</span>
      </nav>

      <header class="mb-12">
        <h1 class="font-serif text-5xl leading-[0.95] tracking-tighter md:text-7xl">发布新帖</h1>
      </header>

      <div class="grid grid-cols-1 gap-16 md:grid-cols-12">
        <form class="md:col-span-8" @submit.prevent="handleSubmit">
          <!-- 标题 -->
          <div>
            <label for="title" class="eyebrow mb-3 block">帖子标题</label>
            <input
              id="title"
              v-model.trim="form.title"
              type="text"
              maxlength="100"
              class="field-input font-serif text-xl"
              placeholder="给这篇帖子起个标题"
            />
            <p class="mt-2 text-right font-sans text-xs text-[#1C1C1C]/40">
              {{ form.title.length }} / 100
            </p>
          </div>

          <!-- 内容 -->
          <div class="mt-10">
            <div class="flex items-center justify-between">
              <label for="content" class="eyebrow">帖子内容</label>
              <div class="flex gap-4">
                <button
                  v-for="m in modes"
                  :key="m.value"
                  type="button"
                  class="font-sans text-xs tracking-[0.15em] uppercase transition-colors duration-300"
                  :class="editorMode === m.value ? 'text-[#1C1C1C]' : 'text-[#1C1C1C]/40 hover:text-[#1C1C1C]'"
                  @click="editorMode = m.value"
                >
                  {{ m.label }}
                </button>
              </div>
            </div>

            <!-- 富文本工具栏 -->
            <div v-if="editorMode === 'rich'" class="mt-4 flex flex-wrap gap-px border border-[#1C1C1C]/10">
              <button
                v-for="t in toolbar"
                :key="t.label"
                type="button"
                class="border-r border-[#1C1C1C]/10 px-4 py-2 font-sans text-xs transition-colors duration-300 hover:bg-[#1C1C1C]/[0.04] last:border-r-0"
                :title="t.title"
                @click="insertFormat(t.before, t.after)"
              >
                {{ t.label }}
              </button>
            </div>

            <textarea
              ref="contentEl"
              v-model="form.content"
              rows="15"
              maxlength="10000"
              class="field-input mt-4 resize-none leading-relaxed"
              placeholder="分享你的茶事、茶知识或品饮感受…"
            />
            <p class="mt-2 text-right font-sans text-xs text-[#1C1C1C]/40">
              {{ form.content.length }} / 10000
            </p>
          </div>

          <!-- 图片 -->
          <div class="mt-10">
            <p class="eyebrow mb-3">上传图片（可选）</p>
            <div class="flex items-center gap-4">
              <button
                type="button"
                class="flex h-24 w-24 items-center justify-center border border-[#1C1C1C]/20 font-sans text-2xl text-[#1C1C1C]/40 transition-colors duration-300 hover:border-[#1C1C1C]"
                @click="fileInput?.click()"
              >
                +
              </button>
              <input
                ref="fileInput"
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif"
                class="hidden"
                @change="onFileChange"
              />
              <div v-if="previewUrl" class="relative h-24 w-24 border border-[#1C1C1C]/10">
                <img :src="previewUrl" alt="预览" class="h-full w-full object-cover" />
                <button
                  type="button"
                  class="absolute -top-2 -right-2 flex h-5 w-5 items-center justify-center border border-[#1C1C1C] bg-[#F9F8F6] font-sans text-xs"
                  aria-label="移除图片"
                  @click="clearImage"
                >
                  ✕
                </button>
              </div>
            </div>
            <p class="mt-3 font-sans text-xs text-[#1C1C1C]/40">
              支持 JPG、PNG、WEBP、GIF 格式，文件大小不超过 5MB
            </p>
          </div>

          <!-- 操作 -->
          <div class="mt-12 flex items-center gap-4">
            <RouterLink to="/community" class="btn-editorial-ghost">取消</RouterLink>
            <button type="submit" class="btn-editorial" :disabled="submitting">
              {{ submitting ? '上传图片中…' : '发布帖子' }}
            </button>
          </div>
        </form>

        <aside class="md:col-span-4">
          <div class="border border-[#1C1C1C]/10 p-6">
            <p class="eyebrow">发布须知</p>
            <ul class="mt-4 space-y-3 font-sans text-sm leading-relaxed text-[#1C1C1C]/60">
              <li>标题长度在 1 到 100 个字符之间</li>
              <li>内容至少需要 5 个字符</li>
              <li>图片可选，限一张，建议压缩后上传</li>
              <li>请勿发布违规内容，平台保留审核权</li>
            </ul>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPost, uploadPostImage } from '@/api/community'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const modes = [
  { label: '简洁模式', value: 'simple' },
  { label: '富文本模式', value: 'rich' }
]

const toolbar = [
  { label: 'B', title: '粗体', before: '**', after: '**' },
  { label: 'I', title: '斜体', before: '*', after: '*' },
  { label: 'S', title: '删除线', before: '~~', after: '~~' },
  { label: 'H1', title: '一级标题', before: '# ', after: '' },
  { label: 'H2', title: '二级标题', before: '## ', after: '' },
  { label: 'H3', title: '三级标题', before: '### ', after: '' },
  { label: '•', title: '无序列表', before: '- ', after: '' },
  { label: '1.', title: '有序列表', before: '1. ', after: '' },
  { label: '🔗', title: '链接', before: '[', after: '](url)' },
  { label: '</>', title: '代码', before: '`', after: '`' },
  { label: '❝', title: '引用', before: '> ', after: '' }
]

const form = reactive({ title: '', content: '' })
const editorMode = ref('simple')
const submitting = ref(false)
const contentEl = ref(null)
const fileInput = ref(null)
const pendingFile = ref(null)
const previewUrl = ref('')

function insertFormat(before, after) {
  const el = contentEl.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const text = form.content
  const selected = text.slice(start, end)
  form.content = text.slice(0, start) + before + selected + after + text.slice(end)
}

function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  pendingFile.value = file
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = URL.createObjectURL(file)
}

function clearImage() {
  pendingFile.value = null
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

/** 压缩图片：canvas 缩放到 1280×1280 内，质量 0.8 */
function compressImage(file) {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => {
      const max = 1280
      let { width, height } = img
      if (width > max || height > max) {
        const ratio = Math.min(max / width, max / height)
        width = Math.round(width * ratio)
        height = Math.round(height * ratio)
      }
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      canvas.getContext('2d').drawImage(img, 0, 0, width, height)
      canvas.toBlob(
        (blob) => resolve(blob ? new File([blob], file.name, { type: 'image/jpeg' }) : file),
        'image/jpeg',
        0.8
      )
    }
    img.onerror = () => resolve(file)
    img.src = URL.createObjectURL(file)
  })
}

async function handleSubmit() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!form.title || form.title.length > 100) {
    ElMessage.warning('标题长度在 1 到 100 个字符之间')
    return
  }
  if (!form.content || form.content.length < 5) {
    ElMessage.warning('内容至少需要 5 个字符')
    return
  }

  submitting.value = true
  try {
    const file = pendingFile.value
    // 并行：创建帖子 + 压缩图片
    const [msg, compressed] = await Promise.all([
      createPost({ title: form.title, content: form.content }),
      file ? compressImage(file) : Promise.resolve(null)
    ])

    if (compressed) {
      const match = /postId=(\d+)/.exec(String(msg || ''))
      const postId = match?.[1]
      if (postId) {
        await uploadPostImage(postId, compressed)
      }
    }

    ElMessage.success('发布成功')
    router.push('/community')
  } catch {
    /* 拦截器已提示 */
  } finally {
    submitting.value = false
  }
}
</script>
