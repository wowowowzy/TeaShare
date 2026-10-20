<template>
  <div class="pt-20 md:pt-24">
    <div class="mx-auto w-full max-w-[900px] px-5 py-10">
      <!-- 工具栏：搜索 + 排序 + 发布 -->
      <div class="flex flex-wrap items-center gap-4">
        <input
          v-model.trim="searchKeyword"
          type="search"
          class="field-input min-w-48 max-w-96 flex-1"
          placeholder="搜索帖子..."
        />

        <div class="flex flex-wrap gap-2">
          <button
            v-for="s in sortOptions"
            :key="s.value"
            type="button"
            class="border px-3 py-1.5 font-sans text-xs tracking-[0.1em] transition-colors duration-300"
            :class="
              sortBy === s.value
                ? 'border-[#1C1C1C] bg-[#1C1C1C] text-[#F9F8F6]'
                : 'border-[#1C1C1C]/20 text-[#1C1C1C]/60 hover:border-[#1C1C1C]'
            "
            @click="sortBy = s.value"
          >
            {{ s.label }}
          </button>
        </div>

        <RouterLink to="/community/publish" class="btn-editorial ml-auto !px-5 !py-2 text-xs">
          发布新帖
        </RouterLink>
      </div>

      <!-- Tabs 卡片 -->
      <div class="mt-6 border border-[#1C1C1C]/10">
        <div class="flex border-b border-[#1C1C1C]/10">
          <button
            v-for="t in tabs"
            :key="t.value"
            type="button"
            class="relative flex flex-1 items-center justify-center gap-2 py-4 font-sans text-sm transition-colors duration-300"
            :class="activeTab === t.value ? 'text-[#1C1C1C]' : 'text-[#1C1C1C]/40 hover:text-[#1C1C1C]/70'"
            @click="switchTab(t.value)"
          >
            <span>{{ t.label }}</span>
            <span
              v-if="activeTab === t.value"
              class="absolute bottom-0 left-0 h-px w-full bg-[#1C1C1C]"
            />
          </button>
        </div>

        <div class="p-6">
          <!-- 默认添加 -->
          <div v-if="activeTab === 'default'">
            <SkeletonBlock v-if="loading" :rows="6" />

            <EmptyState
              v-else-if="!sortedPosts.length"
              class="!py-16"
              title="暂无帖子，快来发布第一篇吧！"
            >
              <RouterLink to="/community/publish" class="btn-editorial !px-4 !py-2 text-xs">
                发布新帖
              </RouterLink>
            </EmptyState>

            <ul v-else class="space-y-3">
              <li
                v-for="p in sortedPosts"
                :key="p.postId"
                class="cursor-pointer rounded-lg border border-[#E8E8E8] bg-white p-5 transition-colors duration-300 hover:border-[#C9C9C9]"
                @click="goPost(p)"
              >
                <!-- 作者：仅头像 + 名字 -->
                <div class="flex items-center gap-2.5">
                  <SmartImage
                    :src="p.userImage"
                    :alt="p.username"
                    class="h-8 w-8 shrink-0 overflow-hidden rounded-full"
                  />
                  <span class="truncate font-sans text-[13px] text-[#1C1C1C]">
                    {{ p.username || '匿名茶友' }}
                  </span>
                </div>

                <!-- 标题 -->
                <h3 class="mt-3.5 font-sans text-[17px] leading-snug font-semibold text-[#1C1C1C]">
                  {{ p.title }}
                </h3>

                <!-- 正文摘要 -->
                <p
                  v-if="p.content"
                  class="mt-2 line-clamp-2 font-sans text-sm leading-relaxed text-[#8A8A8A]"
                >
                  {{ truncate(p.content, 150) }}
                </p>

                <!-- 配图：固定 120×90 缩略图，不撑开卡片 -->
                <div v-if="postImages(p).length" class="mt-3 flex flex-wrap gap-2">
                  <button
                    v-for="(img, i) in postImages(p).slice(0, 4)"
                    :key="i"
                    type="button"
                    class="relative h-[90px] w-[120px] shrink-0 overflow-hidden rounded-md border border-[#E8E8E8]"
                    @click.stop="previewImage(postImages(p), i)"
                  >
                    <img
                      :src="asset(img)"
                      alt="帖子图片"
                      loading="lazy"
                      class="h-full w-full object-cover transition-transform duration-500 hover:scale-105"
                    />
                    <span
                      v-if="i === 3 && postImages(p).length > 4"
                      class="absolute right-0 bottom-0 bg-black/60 px-2 py-0.5 font-sans text-xs text-white"
                    >
                      +{{ postImages(p).length - 4 }}
                    </span>
                  </button>
                </div>

                <!-- 统计：点赞 / 收藏 / 评论 -->
                <div class="mt-3.5 flex flex-wrap gap-2.5">
                  <button
                    type="button"
                    class="inline-flex items-center gap-1.5 rounded-full border px-3.5 py-1.5 font-sans text-[13px] transition-colors duration-300"
                    :class="
                      p.isLiked
                        ? 'border-[#1C1C1C] text-[#1C1C1C]'
                        : 'border-[#E8E8E8] text-[#8A8A8A] hover:border-[#C9C9C9] hover:text-[#1C1C1C]'
                    "
                    :title="p.isLiked ? '取消点赞' : '点赞'"
                    @click.stop="handleLike(p)"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" class="h-4 w-4">
                      <path d="M7 22V11l5-9a2 2 0 0 1 2 2v5h5a2 2 0 0 1 2 2.3l-1.5 8A2 2 0 0 1 17.5 22H7z" />
                      <path d="M2 11h3v11H2z" />
                    </svg>
                    {{ p.likeCount || 0 }}
                  </button>

                  <button
                    type="button"
                    class="inline-flex items-center gap-1.5 rounded-full border px-3.5 py-1.5 font-sans text-[13px] transition-colors duration-300"
                    :class="
                      p.isCollected
                        ? 'border-[#1C1C1C] text-[#1C1C1C]'
                        : 'border-[#E8E8E8] text-[#8A8A8A] hover:border-[#C9C9C9] hover:text-[#1C1C1C]'
                    "
                    :title="p.isCollected ? '取消收藏' : '收藏'"
                    @click.stop="handleCollect(p)"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" class="h-4 w-4">
                      <path d="M12 3.5l2.7 5.6 6.1.9-4.4 4.3 1 6.1-5.4-2.9-5.4 2.9 1-6.1L3.2 10l6.1-.9z" />
                    </svg>
                    {{ p.collectCount || 0 }}
                  </button>

                  <button
                    type="button"
                    class="inline-flex items-center gap-1.5 rounded-full border border-[#E8E8E8] px-3.5 py-1.5 font-sans text-[13px] text-[#8A8A8A] transition-colors duration-300 hover:border-[#C9C9C9] hover:text-[#1C1C1C]"
                    title="评论"
                    @click.stop="goPost(p)"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" class="h-4 w-4">
                      <path d="M21 11.5a8.4 8.4 0 0 1-9 8.4 9 9 0 0 1-3.8-.8L3 20.5l1.4-4.2A8.4 8.4 0 0 1 12 3a8.4 8.4 0 0 1 9 8.5z" />
                    </svg>
                    {{ p.commentCount || 0 }}
                  </button>
                </div>
              </li>
            </ul>

            <div v-if="total > 0" class="mt-8 flex justify-center">
              <el-pagination
                layout="total, sizes, prev, pager, next"
                :total="total"
                :page-sizes="[10, 20, 30, 50]"
                :page-size="pageSize"
                :current-page="currentPage"
                background
                @size-change="onSizeChange"
                @current-change="onPageChange"
              />
            </div>
          </div>

          <!-- 猜你喜欢 -->
          <div v-else>
            <SkeletonBlock v-if="loadingGuess" :rows="4" />

            <EmptyState v-else-if="!guessList.length" class="!py-16" title="暂无推荐内容" />

            <div v-else class="flex flex-wrap gap-3">
              <button
                v-for="g in guessList"
                :key="g.postId"
                type="button"
                class="min-w-[200px] flex-1 border border-[#1C1C1C]/10 p-3 text-left transition-colors duration-300 hover:border-[#1C1C1C]"
                @click="goPost({ postId: g.postId })"
              >
                <p class="font-sans text-xs text-[#1C1C1C]/40">帖子 ID：{{ g.postId }}</p>
                <p class="mt-1 font-serif text-base leading-snug tracking-tight">{{ g.title }}</p>
                <p class="mt-2 flex items-center gap-2">
                  <span
                    class="border border-[#1C1C1C]/20 px-2 py-0.5 font-sans text-xs tracking-[0.1em] text-[#1C1C1C]/50 uppercase"
                  >
                    推荐理由
                  </span>
                  <span class="font-sans text-xs text-[#1C1C1C]/40">基于您的兴趣偏好推荐</span>
                </p>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-image-viewer
      v-if="showViewer"
      :url-list="viewerImages.map(asset)"
      :initial-index="viewerIndex"
      @close="showViewer = false"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SmartImage from '@/components/ui/SmartImage.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { getGuessYouLike, getPostList, switchCollect, switchLikePost } from '@/api/community'
import { useUserStore } from '@/stores/user'
import { asset } from '@/utils/apiBase'

const router = useRouter()
const userStore = useUserStore()

const tabs = [
  { label: '默认添加', value: 'default' },
  { label: '猜你喜欢', value: 'recommended' }
]

const sortOptions = [
  { label: '最新发布', value: 'newest' },
  { label: '最多点赞', value: 'likes' },
  { label: '最多收藏', value: 'collects' },
  { label: '最多评论', value: 'comments' }
]

const searchKeyword = ref('')
const sortBy = ref('newest')
const activeTab = ref('default')

const postList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(true)

const guessList = ref([])
const loadingGuess = ref(false)

const showViewer = ref(false)
const viewerImages = ref([])
const viewerIndex = ref(0)

const sortedPosts = computed(() => {
  const list = [...postList.value]
  switch (sortBy.value) {
    case 'likes':
      return list.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
    case 'collects':
      return list.sort((a, b) => (b.collectCount || 0) - (a.collectCount || 0))
    case 'comments':
      return list.sort((a, b) => (b.commentCount || 0) - (a.commentCount || 0))
    default:
      return list.sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))
  }
})

/** 相对时间：刚刚 / N分钟前 / N小时前 / 昨天 / N天前 / 日期 */
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return String(time)
  const diff = Date.now() - date.getTime()
  const days = Math.floor(diff / 86400000)
  if (days === 0) {
    const hours = Math.floor(diff / 3600000)
    if (hours === 0) {
      const minutes = Math.floor(diff / 60000)
      return minutes <= 1 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  }
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return String(time).split('T')[0]
}

function truncate(content, n) {
  if (!content) return ''
  return content.length > n ? `${content.slice(0, n)}...` : content
}

/** image 字段可能是逗号分隔的多图 */
function postImages(p) {
  if (!p.image) return []
  return String(p.image)
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
}

function previewImage(images, index) {
  viewerImages.value = images
  viewerIndex.value = index
  showViewer.value = true
}

async function fetchPosts() {
  loading.value = true
  try {
    const res = await getPostList({
      page: currentPage.value,
      pageSize: pageSize.value,
      name: searchKeyword.value
    })
    const records = Array.isArray(res?.records) ? res.records : Array.isArray(res) ? res : []
    postList.value = records.map((p) => ({
      ...p,
      isLiked: p.likeCancel === 1,
      isCollected: p.collectCancel === 1
    }))
    total.value = res?.total ?? records.length
  } catch {
    postList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function fetchGuess() {
  loadingGuess.value = true
  try {
    const res = await getGuessYouLike()
    guessList.value = Array.isArray(res) ? res.map((i) => ({ postId: i.postId, title: i.title })) : []
  } catch {
    guessList.value = []
  } finally {
    loadingGuess.value = false
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

async function handleLike(p) {
  if (!requireLogin()) return
  const liked = p.isLiked
  p.isLiked = !liked
  p.likeCount = Math.max(0, (p.likeCount || 0) + (liked ? -1 : 1))
  try {
    await switchLikePost(p.postId, liked ? -1 : 1)
  } catch {
    p.isLiked = liked
    p.likeCount = Math.max(0, (p.likeCount || 0) + (liked ? 1 : -1))
  }
}

async function handleCollect(p) {
  if (!requireLogin()) return
  const collected = p.isCollected
  p.isCollected = !collected
  p.collectCount = Math.max(0, (p.collectCount || 0) + (collected ? -1 : 1))
  try {
    await switchCollect(p.postId, collected ? -1 : 1)
  } catch {
    p.isCollected = collected
    p.collectCount = Math.max(0, (p.collectCount || 0) + (collected ? 1 : -1))
  }
}

function goPost(p) {
  router.push({ name: 'communityPost', params: { id: p.postId } })
}

function onPageChange(p) {
  currentPage.value = p
  fetchPosts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function onSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  fetchPosts()
}

function switchTab(v) {
  activeTab.value = v
  // 切到「猜你喜欢」总是重新拉取（推荐结果会随行为变化）
  if (v === 'recommended') fetchGuess()
}

let debounceTimer = null
watch(searchKeyword, () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    currentPage.value = 1
    fetchPosts()
  }, 300)
})

onMounted(() => {
  fetchPosts()
  fetchGuess()
})
</script>
