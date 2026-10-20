<template>
  <div class="border-t border-[#1C1C1C]/10 pt-10">
    <div class="grid grid-cols-1 gap-10 md:grid-cols-12">
      <!-- 发表评论 -->
      <div class="md:col-span-5">
        <SectionLabel class="mb-4 block">发表评论</SectionLabel>

        <div v-if="!userStore.isLoggedIn" class="border border-[#1C1C1C]/10 p-5">
          <p class="font-sans text-sm text-[#1C1C1C]/60">登录后即可发表评论。</p>
          <RouterLink to="/login" class="btn-editorial-ghost mt-4 !px-4 !py-2 text-xs">
            去登录
          </RouterLink>
        </div>

        <form v-else class="space-y-6" @submit.prevent="submitComment">
          <div>
            <p class="eyebrow mb-2">评分</p>
            <StarRating v-model="rating" />
          </div>

          <div>
            <label for="comment" class="eyebrow mb-2 block">评价内容</label>
            <textarea
              id="comment"
              v-model.trim="content"
              rows="4"
              maxlength="500"
              class="field-input resize-none"
              placeholder="请输入您的评价…"
            />
            <p class="mt-1 text-right font-sans text-xs text-[#1C1C1C]/40">
              {{ content.length }} / 500
            </p>
          </div>

          <div>
            <p class="eyebrow mb-2">上传图片（可选，限一张）</p>
            <div class="flex items-center gap-4">
              <button
                type="button"
                class="flex h-20 w-20 items-center justify-center border border-[#1C1C1C]/20 font-sans text-2xl text-[#1C1C1C]/40 transition-colors duration-300 hover:border-[#1C1C1C]"
                @click="fileInput?.click()"
              >
                +
              </button>
              <input
                ref="fileInput"
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                class="hidden"
                @change="onFileChange"
              />
              <div v-if="previewUrl" class="relative h-20 w-20 border border-[#1C1C1C]/10">
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
          </div>

          <button type="submit" class="btn-editorial" :disabled="submitting">
            {{ submitting ? '提交中…' : '提交评论' }}
          </button>
        </form>
      </div>

      <!-- 评论列表 -->
      <div class="md:col-span-7">
        <div class="flex items-baseline justify-between">
          <SectionLabel>评论列表</SectionLabel>
          <span class="font-sans text-xs text-[#1C1C1C]/40">{{ totalCount }} 条</span>
        </div>

        <SkeletonBlock v-if="loading" class="mt-6" :rows="4" />

        <EmptyState v-else-if="!comments.length" class="!py-12" title="暂无评论" description="成为第一个分享感受的人。" />

        <ul v-else class="mt-6">
          <li v-for="c in comments" :key="c.reviewId" class="border-b border-[#1C1C1C]/10 py-4">
            <div class="flex items-center gap-3">
              <SmartImage
                :src="c.userImage || c.avatar"
                :alt="c.username"
                class="h-9 w-9 shrink-0 border border-[#1C1C1C]/10"
              />
              <div class="flex-1">
                <p class="font-sans text-sm">{{ c.username || '匿名茶友' }}</p>
                <p class="font-sans text-xs text-[#1C1C1C]/40">
                  {{ formatTime(c.createTime) }}
                </p>
              </div>
              <StarRating :model-value="Number(c.level) || 0" readonly />
            </div>

            <p class="mt-3 font-sans text-sm leading-relaxed text-[#1C1C1C]/80">
              {{ c.content }}
            </p>

        <img
              v-if="c.image"
              :src="asset(c.image)"
              alt="评论图片"
              class="mt-3 max-h-48 max-w-xs border border-[#1C1C1C]/10 object-cover"
            />

            <button
              type="button"
              class="mt-3 border border-[#1C1C1C]/10 px-3 py-1 font-sans text-xs transition-colors duration-300"
              :class="
                isLiked(c)
                  ? 'border-[#1C1C1C] text-[#1C1C1C]'
                  : 'text-[#1C1C1C]/50 hover:border-[#1C1C1C]/40 hover:text-[#1C1C1C]'
              "
              @click="toggleLike(c)"
            >
              ♥ 有用 {{ c.likeCount || 0 }}
            </button>
          </li>
        </ul>

        <div v-if="comments.length && hasMore" class="mt-6">
          <button type="button" class="btn-editorial-ghost !px-4 !py-2 text-xs" @click="loadMore">
            加载更多
          </button>
        </div>
        <p v-else-if="comments.length" class="mt-6 font-sans text-xs text-[#1C1C1C]/40">
          已加载全部评论
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import SectionLabel from '@/components/ui/SectionLabel.vue'
import SmartImage from '@/components/ui/SmartImage.vue'
import SkeletonBlock from '@/components/ui/SkeletonBlock.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StarRating from '@/components/ui/StarRating.vue'
import { commentGoods, getCommentByGoodsId, likeComment } from '@/api/goods'
import { useUserStore } from '@/stores/user'
import { asset } from '@/utils/apiBase'
import { STORAGE_KEYS, readJSON, writeJSON } from '@/utils/storage'

const props = defineProps({
  goodsId: { type: [Number, String], required: true }
})

const userStore = useUserStore()

const rating = ref(5)
const content = ref('')
const fileInput = ref(null)
const pendingFile = ref(null)
const previewUrl = ref('')
const submitting = ref(false)

const comments = ref([])
const loading = ref(true)
const totalCount = ref(0)
const hasMore = ref(false)

/** 点赞本地缓存 { reviewId: true } */
const likedMap = ref(readJSON(STORAGE_KEYS.likedComments, {}) || {})

function isLiked(c) {
  if (c.isCancel === 1) return true
  return !!likedMap.value[c.reviewId]
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

async function submitComment() {
  if (!content.value) {
    ElMessage.warning('请输入评价内容')
    return
  }
  submitting.value = true
  try {
    await commentGoods(props.goodsId, content.value, rating.value, pendingFile.value)
    ElMessage.success('评价成功')
    content.value = ''
    rating.value = 5
    clearImage()
    await loadComments()
  } catch {
    /* 拦截器已提示 */
  } finally {
    submitting.value = false
  }
}

async function loadComments() {
  loading.value = true
  try {
    const res = await getCommentByGoodsId(props.goodsId)
    const list = Array.isArray(res) ? res : []
    comments.value = list
    totalCount.value = list.length
    hasMore.value = false
  } catch {
    comments.value = []
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}

function loadMore() {
  // 后端一次性返回全量，此处直接置为已全部加载
  hasMore.value = false
}

let likeLock = false
async function toggleLike(c) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (likeLock) return
  likeLock = true
  const liked = isLiked(c)
  try {
    await likeComment(c.reviewId, liked ? -1 : 1)
    if (liked) {
      delete likedMap.value[c.reviewId]
      c.likeCount = Math.max(0, (c.likeCount || 0) - 1)
      c.isCancel = -1
    } else {
      likedMap.value[c.reviewId] = true
      c.likeCount = (c.likeCount || 0) + 1
      c.isCancel = 1
    }
    writeJSON(STORAGE_KEYS.likedComments, likedMap.value)
  } catch {
    /* 拦截器已提示 */
  } finally {
    setTimeout(() => {
      likeLock = false
    }, 1500)
  }
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  const diff = Date.now() - d.getTime()
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min} 分钟前`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour} 小时前`
  return `${d.getMonth() + 1}/${d.getDate()}`
}

watch(
  () => props.goodsId,
  () => loadComments()
)

onMounted(loadComments)
</script>
