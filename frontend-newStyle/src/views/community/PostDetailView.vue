<template>
  <div class="pt-20 md:pt-24">
    <div class="mx-auto w-full max-w-[900px] px-5 py-10">
      <nav class="mb-6 font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase">
        <RouterLink to="/community" class="link-underline">茶友社</RouterLink>
        <span class="mx-3">/</span>
        <span>帖子详情</span>
      </nav>

      <SkeletonBlock v-if="loading" :rows="10" />

      <EmptyState v-else-if="!post" title="帖子不存在" description="该帖子可能已被删除。">
        <RouterLink to="/community" class="btn-editorial-ghost !px-4 !py-2 text-xs">返回茶友社</RouterLink>
      </EmptyState>

      <template v-else>
        <!-- 帖子主体 -->
        <article class="border-b border-[#1C1C1C]/10 pb-8">
          <div class="flex items-center gap-3">
            <SmartImage
              :src="post.userImage"
              :alt="post.username"
              class="h-10 w-10 shrink-0 border border-[#1C1C1C]/10"
            />
            <div class="flex-1">
              <p class="font-sans text-sm">{{ post.username || '匿名茶友' }}</p>
              <p class="font-sans text-xs text-[#1C1C1C]/40">{{ formatDate(post.createTime) }}</p>
            </div>
            <button
              v-if="isAuthor"
              type="button"
              class="font-sans text-xs tracking-[0.1em] text-[#1C1C1C]/40 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
              @click="handleDeletePost"
            >
              删除帖子
            </button>
          </div>

          <h1 class="mt-5 font-serif text-2xl leading-snug tracking-tight md:text-3xl">
            {{ post.title }}
          </h1>

          <div class="prose-editorial mt-5 max-w-2xl" v-html="formatContent(post.content)" />

          <!-- 配图：限定最大宽高，避免大图撑破版面 -->
          <img
            v-if="post.image"
            :src="asset(post.image)"
            alt="帖子图片"
            class="mt-5 max-h-80 max-w-md border border-[#1C1C1C]/10 object-cover"
          />

          <div class="mt-6 flex flex-wrap items-center gap-2">
            <button
              type="button"
              class="border px-3 py-1 font-sans text-xs transition-colors duration-300"
              :class="
                post.isLiked
                  ? 'border-[#1C1C1C] text-[#1C1C1C]'
                  : 'border-[#1C1C1C]/10 text-[#1C1C1C]/50 hover:border-[#1C1C1C]/40 hover:text-[#1C1C1C]'
              "
              @click="handleLikePost"
            >
              ♥ 点赞 {{ post.likeCount || 0 }}
            </button>
            <button
              type="button"
              class="border px-3 py-1 font-sans text-xs transition-colors duration-300"
              :class="
                post.isCollected
                  ? 'border-[#1C1C1C] text-[#1C1C1C]'
                  : 'border-[#1C1C1C]/10 text-[#1C1C1C]/50 hover:border-[#1C1C1C]/40 hover:text-[#1C1C1C]'
              "
              @click="handleCollect"
            >
              ★ 收藏 {{ post.collectCount || 0 }}
            </button>
            <span class="border border-[#1C1C1C]/10 px-3 py-1 font-sans text-xs text-[#1C1C1C]/50">
              评论 {{ post.commentCount || 0 }}
            </span>
          </div>
        </article>

        <!-- 评论区 -->
        <section class="mt-16">
          <SectionLabel>评论（{{ post.commentCount || 0 }}）</SectionLabel>

          <!-- 发表评论 -->
          <div v-if="userStore.isLoggedIn" class="mt-6">
            <textarea
              v-model.trim="newComment"
              rows="3"
              maxlength="500"
              class="field-input resize-none"
              placeholder="写下你的评论…"
            />
            <div class="mt-4 flex items-center justify-between">
              <span class="font-sans text-xs text-[#1C1C1C]/40">{{ newComment.length }} / 500</span>
              <button
                type="button"
                class="btn-editorial !px-4 !py-2 text-xs"
                :disabled="commentLoading || !newComment"
                @click="handleAddComment"
              >
                {{ commentLoading ? '发表中…' : '发表评论' }}
              </button>
            </div>
          </div>
          <div v-else class="mt-6 border border-[#1C1C1C]/10 p-6">
            <p class="font-sans text-sm text-[#1C1C1C]/60">登录后即可参与评论。</p>
            <RouterLink to="/login" class="btn-editorial-ghost mt-4 !px-4 !py-2 text-xs">去登录</RouterLink>
          </div>

          <!-- 评论列表 -->
          <ul class="mt-6">
            <li v-for="c in comments" :key="c.id" class="border-b border-[#1C1C1C]/10 py-4">
              <div class="flex items-start gap-3">
                <SmartImage
                  :src="c.userImage"
                  :alt="c.username"
                  class="h-9 w-9 shrink-0 border border-[#1C1C1C]/10"
                />
                <div class="flex-1">
                  <div class="flex items-center gap-3">
                    <span class="font-sans text-sm">{{ c.username || '匿名茶友' }}</span>
                    <span class="font-sans text-xs text-[#1C1C1C]/40">
                      {{ formatDate(c.createTime) }}
                    </span>
                  </div>
                  <p class="mt-1.5 font-sans text-sm leading-relaxed text-[#1C1C1C]/80">
                    {{ c.content }}
                  </p>

                  <div class="mt-2 flex gap-5">
                    <button
                      type="button"
                      class="font-sans text-xs tracking-[0.15em] uppercase transition-colors duration-300"
                      :class="c.isCancel === 1 ? 'text-[#1C1C1C]' : 'text-[#1C1C1C]/40 hover:text-[#1C1C1C]'"
                      @click="handleLikeComment(c)"
                    >
                      ♥ {{ c.likeCount || 0 }}
                    </button>
                    <button
                      type="button"
                      class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                      @click="startReply(c)"
                    >
                      回复
                    </button>
                    <button
                      v-if="String(c.userId) === String(userStore.userId)"
                      type="button"
                      class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                      @click="handleDeleteComment(c)"
                    >
                      删除
                    </button>
                  </div>

                  <!-- 回复输入 -->
                  <div v-if="replyingTo === c.id" class="mt-4">
                    <textarea
                      v-model.trim="replyContent"
                      rows="2"
                      maxlength="500"
                      class="field-input resize-none"
                      :placeholder="`回复 @${c.username}`"
                    />
                    <div class="mt-3 flex gap-3">
                      <button
                        type="button"
                        class="btn-editorial !px-4 !py-1.5 text-xs"
                        :disabled="!replyContent"
                        @click="submitReply(c)"
                      >
                        回复
                      </button>
                      <button
                        type="button"
                        class="btn-editorial-ghost !px-4 !py-1.5 text-xs"
                        @click="cancelReply"
                      >
                        取消
                      </button>
                    </div>
                  </div>

                  <!-- 子回复（两级） -->
                  <ul v-if="c.children?.length" class="mt-4 space-y-4 border-l border-[#1C1C1C]/10 pl-6">
                    <li v-for="child in c.children" :key="child.id">
                      <div class="flex items-center gap-3">
                        <span class="font-sans text-sm">{{ child.username || '匿名茶友' }}</span>
                        <span class="font-sans text-xs text-[#1C1C1C]/40">
                          {{ formatDate(child.createTime) }}
                        </span>
                      </div>
                      <p class="mt-1.5 font-sans text-sm leading-relaxed text-[#1C1C1C]/70">
                        <span v-if="child.toUsername" class="text-[#1C1C1C]/40">
                          回复 @{{ child.toUsername }}：
                        </span>
                        {{ child.content }}
                      </p>
                      <div class="mt-2 flex gap-6">
                        <button
                          type="button"
                          class="font-sans text-xs tracking-[0.15em] uppercase transition-colors duration-300"
                          :class="child.isCancel === 1 ? 'text-[#1C1C1C]' : 'text-[#1C1C1C]/40 hover:text-[#1C1C1C]'"
                          @click="handleLikeComment(child)"
                        >
                          ♥ {{ child.likeCount || 0 }}
                        </button>
                        <button
                          type="button"
                          class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                          @click="startReply(child, c)"
                        >
                          回复
                        </button>
                        <button
                          v-if="String(child.userId) === String(userStore.userId)"
                          type="button"
                          class="font-sans text-xs tracking-[0.15em] text-[#1C1C1C]/40 uppercase transition-colors duration-300 hover:text-[#1C1C1C]"
                          @click="handleDeleteComment(child)"
                        >
                          删除
                        </button>
                      </div>
                    </li>
                  </ul>
                </div>
              </div>
            </li>
          </ul>

          <p v-if="!comments.length" class="mt-8 font-sans text-sm text-[#1C1C1C]/40">
            还没有评论，来说两句吧。
          </p>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import {
  comment as commentApi,
  deleteComment,
  deleteMyPost,
  getPostDetail,
  switchCollect,
  switchLikeComment,
  switchLikePost
} from '@/api/community'
import { useUserStore } from '@/stores/user'
import { asset } from '@/utils/apiBase'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const comments = ref([])
const loading = ref(true)

const newComment = ref('')
const commentLoading = ref(false)

const replyingTo = ref(null)
const replyingRootId = ref(null)
const replyContent = ref('')

const isAuthor = computed(
  () => post.value && String(post.value.userId) === String(userStore.userId)
)

function formatDate(t) {
  if (!t) return ''
  const d = new Date(t)
  return Number.isNaN(d.getTime()) ? String(t) : d.toLocaleString('zh-CN')
}

/** 自实现 Markdown→HTML，先转义防 XSS */
function formatContent(raw) {
  if (!raw) return ''
  const escaped = String(raw)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

  return escaped
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/~~(.+?)~~/g, '<del>$1</del>')
    .replace(/`(.+?)`/g, '<code>$1</code>')
    .split('\n')
    .map((line) => {
      const t = line.trim()
      if (/^###\s+/.test(t)) return `<h3>${t.replace(/^###\s+/, '')}</h3>`
      if (/^##\s+/.test(t)) return `<h2>${t.replace(/^##\s+/, '')}</h2>`
      if (/^#\s+/.test(t)) return `<h1>${t.replace(/^#\s+/, '')}</h1>`
      if (/^>\s?/.test(t)) return `<blockquote>${t.replace(/^>\s?/, '')}</blockquote>`
      if (/^\d+[.、]\s*/.test(t)) return `<p class="ml-4">${t}</p>`
      if (/^[-•]\s*/.test(t)) return `<p class="ml-4">· ${t.replace(/^[-•]\s*/, '')}</p>`
      return t ? `<p>${t}</p>` : '<p class="h-2"></p>'
    })
    .join('')
}

/** 依据 parentId 组装两级评论树 */
function buildCommentTree(list) {
  const map = {}
  const roots = []
  list.forEach((c) => {
    map[c.id] = { ...c, children: [] }
  })
  list.forEach((c) => {
    const node = map[c.id]
    if (c.parentId && c.parentId !== 0 && map[c.parentId]) {
      node.toUsername = map[c.parentId]?.username
      map[c.parentId].children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

async function load() {
  loading.value = true
  try {
    const res = await getPostDetail(route.params.id)
    if (!res) {
      post.value = null
      return
    }
    post.value = {
      ...res,
      isLiked: res.likeCancel === 1,
      isCollected: res.collectCancel === 1
    }
    const list = Array.isArray(res.commentDTOS) ? res.commentDTOS : []
    comments.value = buildCommentTree(list)
  } catch {
    post.value = null
  } finally {
    loading.value = false
  }
}

function requireLogin() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

async function handleLikePost() {
  if (!requireLogin()) return
  const liked = post.value.isLiked
  post.value.isLiked = !liked
  post.value.likeCount = Math.max(0, (post.value.likeCount || 0) + (liked ? -1 : 1))
  try {
    await switchLikePost(post.value.postId, liked ? -1 : 1)
  } catch {
    post.value.isLiked = liked
    post.value.likeCount = Math.max(0, (post.value.likeCount || 0) + (liked ? 1 : -1))
  }
}

async function handleCollect() {
  if (!requireLogin()) return
  const collected = post.value.isCollected
  post.value.isCollected = !collected
  post.value.collectCount = Math.max(0, (post.value.collectCount || 0) + (collected ? -1 : 1))
  try {
    await switchCollect(post.value.postId, collected ? -1 : 1)
  } catch {
    post.value.isCollected = collected
    post.value.collectCount = Math.max(0, (post.value.collectCount || 0) + (collected ? 1 : -1))
  }
}

async function handleAddComment() {
  if (!requireLogin()) return
  commentLoading.value = true
  try {
    await commentApi({ postId: post.value.postId, content: newComment.value, parentId: 0, toUserId: 0 })
    newComment.value = ''
    ElMessage.success('评论成功')
    await load()
  } catch {
    /* 拦截器已提示 */
  } finally {
    commentLoading.value = false
  }
}

function startReply(c, root) {
  replyingTo.value = c.id
  replyingRootId.value = root ? root.id : c.id
  replyContent.value = ''
}

function cancelReply() {
  replyingTo.value = null
  replyingRootId.value = null
  replyContent.value = ''
}

async function submitReply(c) {
  if (!requireLogin()) return
  try {
    await commentApi({
      postId: post.value.postId,
      content: replyContent.value,
      parentId: replyingRootId.value,
      toUserId: c.userId
    })
    cancelReply()
    ElMessage.success('回复成功')
    await load()
  } catch {
    /* 拦截器已提示 */
  }
}

async function handleLikeComment(c) {
  if (!requireLogin()) return
  const liked = c.isCancel === 1
  c.isCancel = liked ? -1 : 1
  c.likeCount = Math.max(0, (c.likeCount || 0) + (liked ? -1 : 1))
  try {
    await switchLikeComment(c.id, liked ? -1 : 1)
  } catch {
    c.isCancel = liked ? 1 : -1
    c.likeCount = Math.max(0, (c.likeCount || 0) + (liked ? 1 : -1))
  }
}

async function handleDeleteComment(c) {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteComment(c.id)
    ElMessage.success('已删除')
    await load()
  } catch {
    /* 拦截器已提示 */
  }
}

async function handleDeletePost() {
  try {
    await ElMessageBox.confirm('确定删除这篇帖子吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteMyPost(post.value.postId)
    ElMessage.success('已删除')
    router.push('/community')
  } catch {
    /* 拦截器已提示 */
  }
}

onMounted(load)
</script>

<style scoped>
.prose-editorial :deep(p) {
  margin: 0 0 1em;
  font-family: var(--font-sans);
  font-size: 0.9375rem;
  line-height: 1.75;
  color: rgb(28 28 28 / 0.8);
}
.prose-editorial :deep(h1),
.prose-editorial :deep(h2),
.prose-editorial :deep(h3) {
  font-family: var(--font-serif);
  font-weight: 400;
  letter-spacing: -0.02em;
  margin: 1.5em 0 0.5em;
}
.prose-editorial :deep(h1) {
  font-size: 1.75rem;
}
.prose-editorial :deep(h2) {
  font-size: 1.5rem;
}
.prose-editorial :deep(h3) {
  font-size: 1.25rem;
}
.prose-editorial :deep(blockquote) {
  border-left: 1px solid rgb(28 28 28 / 0.15);
  padding-left: 1.25rem;
  font-style: italic;
  color: rgb(28 28 28 / 0.6);
  margin: 1em 0;
}
.prose-editorial :deep(code) {
  font-family: var(--font-mono, monospace);
  font-size: 0.875em;
  background: rgb(28 28 28 / 0.05);
  padding: 0.1em 0.35em;
}
.prose-editorial :deep(strong) {
  font-weight: 500;
}
</style>
