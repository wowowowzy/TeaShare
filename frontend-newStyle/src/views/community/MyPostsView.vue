<template>
  <div class="pt-20 md:pt-24">
    <div class="mx-auto w-full max-w-[900px] px-5 py-10">
      <nav class="mb-6 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
        <RouterLink to="/community" class="link-underline">茶友社</RouterLink>
        <span class="mx-3">/</span>
        <span>我的帖子</span>
      </nav>

      <header class="mb-6 flex flex-wrap items-end justify-between gap-4">
        <h1 class="font-serif text-3xl leading-tight tracking-tighter md:text-4xl">我的帖子</h1>
        <RouterLink to="/community/publish" class="btn-editorial !px-5 !py-2 text-xs">发布新帖</RouterLink>
      </header>

      <!-- Tabs -->
      <div class="flex gap-6 border-b border-[#1C1C1C]/10">
        <button
          v-for="t in tabs"
          :key="t.value"
          type="button"
          class="pb-3 font-sans text-sm transition-colors duration-300"
          :class="
            activeTab === t.value
              ? 'border-b border-[#1C1C1C] text-[#1C1C1C]'
              : 'text-[#1C1C1C]/40 hover:text-[#1C1C1C]/70'
          "
          @click="handleTabChange(t.value)"
        >
          {{ t.label }}
        </button>
      </div>

      <!-- 搜索 -->
      <div class="mt-6 max-w-sm">
        <input
          v-model.trim="searchKeyword"
          type="search"
          class="field-input"
          :placeholder="activeTab === 'posts' ? '搜索我的帖子…' : '搜索我收藏的帖子…'"
        />
      </div>

      <SkeletonBlock v-if="loading" class="mt-8" :rows="6" />

      <!-- 我发布的 -->
      <template v-else-if="activeTab === 'posts'">
        <EmptyState v-if="!pagedPosts.length" class="!py-16" title="您还没有发布过帖子">
          <RouterLink to="/community/publish" class="btn-editorial !px-4 !py-2 text-xs">发布新帖</RouterLink>
        </EmptyState>

        <ul v-else class="mt-6 space-y-4">
          <li
            v-for="p in pagedPosts"
            :key="p.id"
            class="border border-[#1C1C1C]/10 p-4 transition-colors duration-300 hover:border-[#1C1C1C]"
          >
            <span
              class="inline-block border border-[#1C1C1C]/20 px-2 py-0.5 font-sans text-xs tracking-[0.1em] text-[#1C1C1C]/60 uppercase"
            >
              已发布
            </span>
            <h3 class="mt-3 font-serif text-lg leading-snug tracking-tight">
              {{ p.title }}
            </h3>
            <p class="mt-2 line-clamp-2 font-sans text-sm leading-relaxed text-[#1C1C1C]/60">
              {{ truncate(p.content, 100) }}
            </p>
            <img
              v-if="p.image"
              :src="asset(String(p.image).split(',')[0])"
              alt="帖子图片"
              class="mt-3 h-[90px] w-[120px] border border-[#1C1C1C]/10 object-cover"
            />
            <div class="mt-3 flex flex-wrap items-center gap-6 font-sans text-xs text-[#1C1C1C]/40">
              <span>♥ {{ p.likeCount || 0 }}</span>
              <span>★ {{ p.collectCount || 0 }}</span>
              <span>评论 {{ p.commentCount || 0 }}</span>
            </div>
            <div class="mt-3 flex gap-5">
              <RouterLink
                :to="{ name: 'communityPost', params: { id: p.id } }"
                class="font-sans text-xs tracking-[0.1em] text-[#1C1C1C]/60 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
              >
                查看
              </RouterLink>
              <button
                type="button"
                class="font-sans text-xs tracking-[0.1em] text-[#1C1C1C]/60 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                @click="openEdit(p)"
              >
                修改
              </button>
              <button
                type="button"
                class="font-sans text-xs tracking-[0.1em] text-[#1C1C1C]/60 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                @click="handleDelete(p)"
              >
                删除
              </button>
            </div>
          </li>
        </ul>

        <div v-if="filteredPosts.length > pageSize" class="mt-8 flex justify-center">
          <el-pagination
            layout="prev, pager, next"
            :total="filteredPosts.length"
            :page-size="pageSize"
            :current-page="page"
            background
            @current-change="(p) => (page = p)"
          />
        </div>
      </template>

      <!-- 我收藏的 -->
      <template v-else>
        <EmptyState v-if="!pagedCollects.length" class="!py-16" title="您还没有收藏过帖子" />

        <ul v-else class="mt-6 space-y-4">
          <li
            v-for="c in pagedCollects"
            :key="c.id"
            class="border border-[#1C1C1C]/10 p-4 transition-colors duration-300 hover:border-[#1C1C1C]"
          >
            <p class="font-sans text-xs text-[#1C1C1C]/40">
              作者 · {{ c.writerName || '匿名茶友' }}
            </p>
            <h3 class="mt-2 font-serif text-lg leading-snug tracking-tight">
              {{ c.title }}
            </h3>
            <p class="mt-2 line-clamp-2 font-sans text-sm leading-relaxed text-[#1C1C1C]/60">
              {{ truncate(c.content, 100) }}
            </p>
            <img
              v-if="c.image"
              :src="asset(String(c.image).split(',')[0])"
              alt="帖子图片"
              class="mt-3 h-[90px] w-[120px] border border-[#1C1C1C]/10 object-cover"
            />
            <div class="mt-3 flex flex-wrap items-center gap-6 font-sans text-xs text-[#1C1C1C]/40">
              <span>♥ {{ c.likeCount || 0 }}</span>
              <span>★ {{ c.collectCount || 0 }}</span>
              <span>评论 {{ c.commentCount || 0 }}</span>
            </div>
            <div class="mt-3 flex gap-5">
              <RouterLink
                :to="{ name: 'communityPost', params: { id: c.id } }"
                class="font-sans text-xs tracking-[0.1em] text-[#1C1C1C]/60 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
              >
                查看
              </RouterLink>
              <button
                type="button"
                class="font-sans text-xs tracking-[0.1em] text-[#1C1C1C]/60 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                :disabled="unfavoritingIds.has(c.id)"
                @click="handleUnfavorite(c)"
              >
                {{ unfavoritingIds.has(c.id) ? '处理中…' : '取消收藏' }}
              </button>
            </div>
          </li>
        </ul>

        <div v-if="filteredCollects.length > pageSize" class="mt-8 flex justify-center">
          <el-pagination
            layout="prev, pager, next"
            :total="filteredCollects.length"
            :page-size="pageSize"
            :current-page="collectPage"
            background
            @current-change="(p) => (collectPage = p)"
          />
        </div>
      </template>
    </div>

    <!-- 修改帖子弹窗 -->
    <el-dialog v-model="editDialogVisible" title="修改帖子" width="90%" style="max-width: 640px">
      <div class="space-y-6">
        <div>
          <label for="edit-title" class="eyebrow mb-2 block">标题</label>
          <input id="edit-title" v-model.trim="editForm.title" type="text" maxlength="100" class="field-input" />
        </div>
        <div>
          <label for="edit-content" class="eyebrow mb-2 block">内容</label>
          <textarea
            id="edit-content"
            v-model="editForm.content"
            rows="8"
            maxlength="10000"
            class="field-input resize-none"
          />
        </div>
        <div>
          <p class="eyebrow mb-2">图片</p>
          <div class="flex items-center gap-4">
            <div v-if="editPreview || editForm.image" class="relative h-24 w-24 border border-[#1C1C1C]/10">
              <img
                :src="editPreview || asset(String(editForm.image).split(',')[0])"
                alt="帖子图片"
                class="h-full w-full object-cover"
              />
              <button
                type="button"
                class="absolute -top-2 -right-2 flex h-5 w-5 items-center justify-center border border-[#1C1C1C] bg-[#F9F8F6] font-sans text-xs"
                aria-label="移除图片"
                @click="removeEditImage"
              >
                ✕
              </button>
            </div>
            <button
              type="button"
              class="flex h-24 w-24 items-center justify-center border border-[#1C1C1C]/20 font-sans text-2xl text-[#1C1C1C]/40 transition-colors duration-300 hover:border-[#1C1C1C]"
              @click="editFileInput?.click()"
            >
              +
            </button>
            <input
              ref="editFileInput"
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              class="hidden"
              @change="onEditFileChange"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3">
          <button type="button" class="btn-editorial-ghost !px-4 !py-2 text-xs" @click="editDialogVisible = false">
            取消
          </button>
          <button type="button" class="btn-editorial !px-4 !py-2 text-xs" :disabled="savingEdit" @click="handleSubmitEdit">
            {{ savingEdit ? '保存中…' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import {
  deleteMyPost,
  getCollect,
  getMyPost,
  switchCollect,
  updateMyPost,
  uploadPostImage
} from '@/api/community'
import { asset } from '@/utils/apiBase'

const router = useRouter()

const tabs = [
  { label: '我发布的帖子', value: 'posts' },
  { label: '我收藏的帖子', value: 'collects' }
]

const activeTab = ref('posts')
const loading = ref(false)

const myPosts = ref([])
const collects = ref([])
const searchKeyword = ref('')
const page = ref(1)
const collectPage = ref(1)
const pageSize = ref(5)

/** 内存缓存：同一会话内避免重复请求 */
const postCache = ref(null)
const collectCache = ref(null)

const unfavoritingIds = ref(new Set())

const editDialogVisible = ref(false)
const editForm = reactive({ postId: null, title: '', content: '', image: '' })
const editFileInput = ref(null)
const editNewFile = ref(null)
const editPreview = ref('')
const savingEdit = ref(false)

const filteredPosts = computed(() => {
  const kw = searchKeyword.value.toLowerCase()
  if (!kw) return myPosts.value
  return myPosts.value.filter(
    (p) => p.title?.toLowerCase().includes(kw) || p.content?.toLowerCase().includes(kw)
  )
})

const pagedPosts = computed(() =>
  filteredPosts.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
)

const filteredCollects = computed(() => {
  const kw = searchKeyword.value.toLowerCase()
  if (!kw) return collects.value
  return collects.value.filter(
    (c) => c.title?.toLowerCase().includes(kw) || c.content?.toLowerCase().includes(kw)
  )
})

const pagedCollects = computed(() =>
  filteredCollects.value.slice((collectPage.value - 1) * pageSize.value, collectPage.value * pageSize.value)
)

function truncate(s, n) {
  if (!s) return ''
  return s.length > n ? `${s.slice(0, n)}…` : s
}

async function fetchMyPosts(force = false) {
  if (postCache.value && !force) {
    myPosts.value = postCache.value
    return
  }
  const res = await getMyPost()
  myPosts.value = Array.isArray(res) ? res : []
  postCache.value = myPosts.value
}

async function fetchCollect(force = false) {
  if (collectCache.value && !force) {
    collects.value = collectCache.value
    return
  }
  const res = await getCollect()
  collects.value = Array.isArray(res) ? res : []
  collectCache.value = collects.value
}

async function loadTab(tab, force = false) {
  loading.value = true
  try {
    if (tab === 'posts') await fetchMyPosts(force)
    else await fetchCollect(force)
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function handleTabChange(tab) {
  activeTab.value = tab
  searchKeyword.value = ''
  loadTab(tab)
}

function openEdit(p) {
  editForm.postId = p.id
  editForm.title = p.title
  editForm.content = p.content
  editForm.image = p.image || ''
  editNewFile.value = null
  if (editPreview.value) URL.revokeObjectURL(editPreview.value)
  editPreview.value = ''
  editDialogVisible.value = true
}

function onEditFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  editNewFile.value = file
  if (editPreview.value) URL.revokeObjectURL(editPreview.value)
  editPreview.value = URL.createObjectURL(file)
}

function removeEditImage() {
  editNewFile.value = null
  editForm.image = ''
  if (editPreview.value) URL.revokeObjectURL(editPreview.value)
  editPreview.value = ''
  if (editFileInput.value) editFileInput.value.value = ''
}

async function handleSubmitEdit() {
  if (!editForm.title || !editForm.content) {
    ElMessage.warning('标题和内容不能为空')
    return
  }
  savingEdit.value = true
  try {
    await updateMyPost({
      postId: editForm.postId,
      title: editForm.title,
      content: editForm.content
    })
    if (editNewFile.value) {
      await uploadPostImage(editForm.postId, editNewFile.value)
    }
    editDialogVisible.value = false
    ElMessage.success('保存成功')
    await loadTab('posts', true)
  } catch {
    /* 拦截器已提示 */
  } finally {
    savingEdit.value = false
  }
}

async function handleDelete(p) {
  try {
    await ElMessageBox.confirm('确定删除这篇帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteMyPost(p.id)
    ElMessage.success('已删除')
    await loadTab('posts', true)
  } catch {
    /* 拦截器已提示 */
  }
}

async function handleUnfavorite(c) {
  if (unfavoritingIds.value.has(c.id)) return
  unfavoritingIds.value = new Set(unfavoritingIds.value).add(c.id)
  const prev = [...collects.value]
  collects.value = collects.value.filter((x) => x.id !== c.id)
  try {
    await switchCollect(c.id, -1)
    collectCache.value = collects.value
  } catch {
    collects.value = prev
  } finally {
    const s = new Set(unfavoritingIds.value)
    s.delete(c.id)
    unfavoritingIds.value = s
  }
}

onMounted(() => loadTab('posts'))
</script>
