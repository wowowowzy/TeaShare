<template>
  <div class="community-page">
    <div class="community-container">
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索帖子..."
            :prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </div>
        <div class="sort-box">
          <el-select v-model="sortBy" @change="handleSortChange" placeholder="排序方式" size="small">
            <el-option label="最新发布" value="newest" />
            <el-option label="最多点赞" value="likes" />
            <el-option label="最多收藏" value="favorites" />
            <el-option label="最多评论" value="comments" />
          </el-select>
        </div>
        <el-button type="primary" class="publish-btn" @click="goToPublish">
          <el-icon><Edit /></el-icon>
          发布新帖
        </el-button>
      </div>

      <!-- 切换标签 -->
      <div class="tab-container">
        <div class="tab-header">
          <div 
            class="tab-item" 
            :class="{ active: activeTab === 'default' }"
            @click="switchTab('default')"
          >
            <el-icon><HotWater /></el-icon>
            <span>默认添加</span>
          </div>
          <div 
            class="tab-item" 
            :class="{ active: activeTab === 'recommended' }"
            @click="switchTab('recommended')"
          >
            <el-icon><Star /></el-icon>
            <span>猜你喜欢</span>
          </div>
        </div>

        <!-- 内容区域 -->
        <div class="tab-content">
          <!-- 默认添加视图 -->
          <div v-show="activeTab === 'default'" class="tab-pane">
            <div class="post-list" v-loading="loading">
              <div v-if="postList.length === 0 && !loading" class="empty-state">
                <el-empty description="暂无帖子，快来发布第一篇吧！" />
              </div>

              <div
                v-for="post in postList"
                :key="post.postId || post.id"
                class="post-item"
                @click="goToDetail(post)"
              >
                <div class="post-middle">
                  <div class="post-header-row">
                    <div class="post-author-avatar">
                      <img
                        :src="(post.userImage || post.authorAvatar || defaultAvatar).trim()"
                        alt="头像"
                        @error="handleAvatarError($event, post)"
                      />
                    </div>
                    <div class="author-info">
                      <span class="post-author-name">{{ post.username || post.authorName || '匿名茶友' }}</span>
                      <span class="post-time">{{ formatTime(post.createTime) }}</span>
                    </div>
                  </div>
                  
                  <h3 class="post-title">{{ post.title }}</h3>
                  
                  <div class="post-preview-content" v-if="post.content">
                    {{ truncateContent(post.content) }}
                  </div>
                  
                  <div v-if="getPostImages(post).length > 0" class="post-images">
                    <div 
                      v-for="(img, index) in getPostImages(post).slice(0, 4)" 
                      :key="index" 
                      class="post-image-item"
                      @click.stop="previewImages(getPostImages(post), index)"
                    >
                      <img :src="img" alt="帖子图片" loading="lazy" />
                      <div v-if="index === 3 && getPostImages(post).length > 4" class="image-more">
                        +{{ getPostImages(post).length - 4 }}
                      </div>
                    </div>
                  </div>
                  
                  <div class="post-stats-row">
                    <span class="stat-item like-item" @click.stop="handleLike(post)" :title="post.isLiked ? '取消点赞' : '点赞'">
                      <span class="stat-icon" :class="{ 'active': post.isLiked }">
                        <el-icon><Pointer v-if="!post.isLiked" /><Pointer v-else /></el-icon>
                      </span>
                      <span class="stat-count" :class="{ 'active': post.isLiked }">{{ post.likeCount || 0 }}</span>
                    </span>
                    <span class="stat-item favorite-item" @click.stop="handleFavorite(post)" :title="post.isFavorited ? '取消收藏' : '收藏'">
                      <span class="stat-icon" :class="{ 'active': post.isFavorited }">
                        <el-icon><Star v-if="!post.isFavorited" /><StarFilled v-else /></el-icon>
                      </span>
                      <span class="stat-count" :class="{ 'active': post.isFavorited }">{{ post.collectCount || post.favoriteCount || 0 }}</span>
                    </span>
                    <span class="stat-item comment-item" @click.stop="goToDetail(post)" title="评论">
                      <span class="stat-icon">
                        <el-icon><ChatDotRound /></el-icon>
                      </span>
                      <span class="stat-count">{{ post.commentCount || 0 }}</span>
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div class="pagination-wrapper" v-if="total > 0">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 30, 50]"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>

          <!-- 猜你喜欢视图 -->
          <div v-show="activeTab === 'recommended'" class="tab-pane">
            <div class="guess-list" v-loading="guessYouLikeLoading">
              <div v-if="guessYouLikeList.length === 0 && !guessYouLikeLoading" class="empty-state">
                <el-empty description="暂无推荐内容" />
              </div>
              <div
                v-for="post in guessYouLikeList"
                :key="post.postId"
                class="guess-item"
                @click="showPostDetail(post)"
              >
                <div class="guess-content">
                  <div class="post-id">帖子ID: {{ post.postId }}</div>
                  <h4 class="guess-item-title">{{ post.title }}</h4>
                  <div class="guess-reason">
                    <span class="reason-tag">推荐理由</span>
                    <span class="reason-text">基于您的兴趣偏好推荐</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-image-viewer
      v-if="showViewer"
      :url-list="viewerImages"
      :initial-index="viewerIndex"
      @close="showViewer = false"
    />


  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Edit, Star, StarFilled, Pointer, ChatDotRound, HotWater } from '@element-plus/icons-vue';
import { getPostList, getGuessYouLike, getPostDetail, likePost, unlikePost, favoritePost, unfavoritePost } from '../../api/community';
import { ElMessage } from 'element-plus';

const router = useRouter();

const searchKeyword = ref('');
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const loading = ref(false);
const postList = ref([]);
const defaultAvatar = '/assest/default.jpg';
const sortBy = ref('newest');

const showViewer = ref(false);
const viewerImages = ref([]);
const viewerIndex = ref(0);

const guessYouLikeList = ref([]);
const guessYouLikeLoading = ref(false);

// 标签切换
const activeTab = ref('default');



let searchTimer = null;

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  
  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60));
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60));
      return minutes <= 1 ? '刚刚' : `${minutes}分钟前`;
    }
    return `${hours}小时前`;
  } else if (days === 1) return '昨天';
  else if (days < 7) return `${days}天前`;
  else return time.split('T')[0];
};

const truncateContent = (content) => {
  if (!content) return '';
  return content.length > 150 ? content.substring(0, 150) + '...' : content;
};

const getPostImages = (post) => {
  if (post.images && Array.isArray(post.images) && post.images.length > 0) {
    return post.images.map(img => img.trim()).filter(img => img);
  }
  if (post.image && typeof post.image === 'string') {
    const trimmed = post.image.trim();
    return trimmed ? [trimmed] : [];
  }
  return [];
};

const handleAvatarError = (event, post) => {
  event.target.src = defaultAvatar;
  event.target.onerror = null;
};

const fetchPosts = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    };
    if (searchKeyword.value) params.name = searchKeyword.value;
    
    const data = await getPostList(params);
    let posts = data.list || data.records || [];
    
    // 处理点赞和收藏状态
    posts = posts.map(post => {
      // 处理点赞状态：根据后端返回的 likeCancel 字段判断
      // likeCancel === 1 表示已点赞，显示高亮；其他值表示未点赞
      const isLiked = post.likeCancel === 1;
      
      // 处理收藏状态：根据后端返回的 collectCancel 字段判断
      // collectCancel === 1 表示已收藏，显示高亮；其他值表示未收藏
      const isFavorited = post.collectCancel === 1;
      
      return {
        ...post,
        isLiked,
        isFavorited
      };
    });
    
    // 根据排序方式对帖子进行排序
    posts = sortPosts(posts);
    
    postList.value = posts;
    total.value = data.total || 0;
    
    console.log('帖子列表数据:', postList.value);
  } catch (error) {
    console.warn('获取帖子列表失败:', error);
    postList.value = [];
  } finally {
    loading.value = false;
  }
};

const sortPosts = (posts) => {
  const sorted = [...posts];
  
  switch (sortBy.value) {
    case 'likes':
      return sorted.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0));
    case 'favorites':
      return sorted.sort((a, b) => (b.collectCount || b.favoriteCount || 0) - (a.collectCount || a.favoriteCount || 0));
    case 'comments':
      return sorted.sort((a, b) => (b.commentCount || 0) - (a.commentCount || 0));
    case 'newest':
    default:
      return sorted.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
  }
};

const handleSortChange = () => {
  currentPage.value = 1;
  fetchPosts();
};

const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    currentPage.value = 1;
    fetchPosts();
  }, 300);
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchPosts();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchPosts();
};

const goToPublish = () => {
  router.push('/community/publish');
};

const goToDetail = (post) => {
  const postId = post.postId || post.id;
  router.push(`/community/post/${postId}`);
};

const previewImages = (images, index) => {
  viewerImages.value = images;
  viewerIndex.value = index;
  showViewer.value = true;
};

const handleLike = async (post) => {
  const postId = post.postId || post.id;
  try {
    if (post.isLiked) {
      await unlikePost(postId);
      post.likeCount = Math.max(0, (post.likeCount || 1) - 1);
      post.isLiked = false;
      post.likeCancel = -1; // 更新状态标识为未点赞
      ElMessage.success('已取消点赞');
    } else {
      await likePost(postId);
      post.likeCount = (post.likeCount || 0) + 1;
      post.isLiked = true;
      post.likeCancel = 1; // 更新状态标识为已点赞
      ElMessage.success('点赞成功');
    }
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

const handleFavorite = async (post) => {
  const postId = post.postId || post.id;
  try {
    if (post.isFavorited) {
      await unfavoritePost(postId);
      // 实时更新收藏数量
      post.collectCount = Math.max(0, (post.collectCount || 1) - 1);
      post.favoriteCount = Math.max(0, (post.favoriteCount || 1) - 1);
      post.isFavorited = false;
      post.collectCancel = -1; // 更新状态标识为未收藏
      ElMessage.success('已取消收藏');
    } else {
      await favoritePost(postId);
      // 实时更新收藏数量
      post.collectCount = (post.collectCount || 0) + 1;
      post.favoriteCount = (post.favoriteCount || 0) + 1;
      post.isFavorited = true;
      post.collectCancel = 1; // 更新状态标识为已收藏
      ElMessage.success('收藏成功');
    }
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

const fetchGuessYouLike = async () => {
  console.log('开始获取猜你喜欢数据');
  guessYouLikeLoading.value = true;
  try {
    const data = await getGuessYouLike();
    console.log('获取猜你喜欢数据响应:', data);
    // 响应拦截器已经处理了code检查，直接返回data
    if (Array.isArray(data)) {
      // 解析返回的data数组，提取postId和title信息
      guessYouLikeList.value = data.map(item => ({
        postId: item.postId,
        title: item.title
      }));
      console.log('猜你喜欢数据:', guessYouLikeList.value);
    } else {
      guessYouLikeList.value = [];
      console.warn('获取猜你喜欢数据格式异常:', data);
      ElMessage.warning('获取猜你喜欢数据格式异常');
    }
  } catch (error) {
    console.warn('获取猜你喜欢失败:', error);
    guessYouLikeList.value = [];
  } finally {
    guessYouLikeLoading.value = false;
    console.log('获取猜你喜欢数据完成');
  }
};

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab;
  // 切换到猜你喜欢时，总是重新获取数据
  if (tab === 'recommended') {
    console.log('切换到猜你喜欢标签，获取数据');
    fetchGuessYouLike();
  }
};

// 跳转到帖子详情页
const showPostDetail = (post) => {
  const postId = post.postId;
  router.push(`/community/post/${postId}`);
};

onMounted(() => {
  fetchPosts();
  fetchGuessYouLike();
});
</script>

<style scoped>
.community-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4f3 100%);
  padding: 80px 20px 40px;
}

.community-container {
  max-width: 900px;
  margin: 0 auto;
}

/* 切换标签样式 */
.tab-container {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e6e8;
  overflow: hidden;
  margin-bottom: 20px;
}

.tab-header {
  display: flex;
  border-bottom: 1px solid #f0f2f5;
  background: linear-gradient(to right, #f9f9f9, #ffffff);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  user-select: none;
}

.tab-item:hover {
  background: rgba(76, 175, 80, 0.05);
}

.tab-item.active {
  color: #4caf50;
  font-weight: 600;
  background: #ffffff;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #2e7d32, #4caf50);
}

.tab-item .el-icon {
  font-size: 16px;
  transition: all 0.3s ease;
}

.tab-item.active .el-icon {
  transform: scale(1.1);
}

.tab-content {
  padding: 24px;
}

.tab-pane {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  gap: 20px;
}

.search-box {
  flex: 1;
  max-width: 400px;
}

.sort-box {
  min-width: 120px;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 25px;
  padding: 5px 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.search-box :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 16px rgba(0, 128, 128, 0.2);
}

.publish-btn {
  background: linear-gradient(135deg, #008080 0%, #2a9d8f 100%);
  border: none;
  padding: 12px 28px;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 128, 128, 0.4);
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid transparent;
}

.post-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 128, 128, 0.15);
  border-color: rgba(0, 128, 128, 0.2);
}

.post-middle {
  width: 100%;
}

.post-header-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: #f0f0f0;
  flex-shrink: 0;
}

.post-author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-info {
  flex: 1;
  min-width: 0;
}

.post-author-name {
  font-weight: 600;
  color: #333;
  font-size: 14px;
  display: block;
  margin-bottom: 2px;
}

.post-time {
  color: #999;
  font-size: 12px;
  display: block;
}

.post-title {
  font-size: 18px;
  color: #1a1a1a;
  margin: 0 0 10px;
  font-weight: 600;
  line-height: 1.4;
}

.post-images {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.post-image-item {
  position: relative;
  width: 120px;
  height: 90px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.post-image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.post-image-item:hover img {
  transform: scale(1.05);
}

.image-more {
  position: absolute;
  bottom: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 4px 8px;
  font-size: 12px;
  border-radius: 0 0 0 8px;
}

.post-preview-content {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-stats-row {
  display: flex;
  gap: 16px;
  margin-top: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 14px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s ease;
  border: 1px solid #eee;
}

.stat-item:hover {
  background: #f8f8f8;
}

.stat-item .el-icon {
  font-size: 16px;
  transition: all 0.3s ease;
}

.like-item {
  color: #999;
}

.like-item:hover {
  color: #ff6b6b;
  border-color: rgba(255, 107, 107, 0.3);
  background: rgba(255, 107, 107, 0.08);
}

.like-item.active,
.like-item.active:hover {
  color: #ff6b6b;
  border-color: rgba(255, 107, 107, 0.3);
  background: rgba(255, 107, 107, 0.08);
}

.like-item .el-icon {
  font-size: 18px;
  color: #999;
  transition: all 0.3s ease;
}

.like-item:hover .el-icon {
  color: #ff6b6b;
}

.like-item.active .el-icon {
  color: #ff6b6b;
}

.favorite-item {
  color: #999;
}

.favorite-item:hover {
  color: #ffb800;
  border-color: rgba(255, 184, 0, 0.3);
  background: rgba(255, 184, 0, 0.08);
}

.favorite-item.active,
.favorite-item.active:hover {
  color: #ffb800;
  border-color: rgba(255, 184, 0, 0.3);
  background: rgba(255, 184, 0, 0.08);
}

.favorite-item .el-icon {
  font-size: 18px;
  color: #999;
  transition: all 0.3s ease;
}

.favorite-item:hover .el-icon {
  color: #ffb800;
}

.favorite-item.active .el-icon {
  color: #ffb800;
}

.comment-item:hover {
  color: #008080;
  border-color: rgba(0, 128, 128, 0.3);
  background: rgba(0, 128, 128, 0.08);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.active {
  transform: scale(1.15);
}

.stat-count {
  font-weight: 600;
  min-width: 20px;
  text-align: center;
  transition: all 0.3s ease;
}

.stat-count.active {
  color: inherit;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.stat-item:active .el-icon {
  animation: pulse 0.3s ease;
}

.empty-state {
  padding: 60px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.pagination-wrapper :deep(.el-pagination) {
  --el-pagination-button-bg-color: white;
  --el-pagination-hover-color: #008080;
}

.default-recommendation {
  margin-bottom: 30px;
}

.default-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.default-title .el-icon {
  color: #ff6b6b;
  font-size: 20px;
}

.guess-you-like {
  margin-top: 40px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid transparent;
}

.guess-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.guess-title .el-icon {
  color: #ffb800;
  font-size: 20px;
}

.guess-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.guess-item {
  flex: 1;
  min-width: 200px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.guess-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 128, 128, 0.1);
  border-color: rgba(0, 128, 128, 0.2);
  background: #f0f8f7;
}

.post-id {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
}

.guess-item-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.guess-reason {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.reason-tag {
  background: rgba(76, 175, 80, 0.1);
  color: #4caf50;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 600;
}

.reason-text {
  font-size: 12px;
  color: #666;
}

.guess-empty {
  padding: 40px 0;
  text-align: center;
}

.pagination-wrapper :deep(.el-pager li.is-active) {
  /* background: linear-gradient(135deg, #008080 0%, #2a9d8f 100%); */
}



@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: none;
  }

  .sort-box {
    width: 100%;
    margin-top: 10px;
  }

  .publish-btn {
    justify-content: center;
    margin-top: 10px;
  }

  .tab-container {
    border-radius: 12px;
  }

  .tab-header {
    flex-direction: column;
  }

  .tab-item {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f2f5;
  }

  .tab-item.active::after {
    left: 20px;
    right: 20px;
  }

  .tab-content {
    padding: 20px;
  }

  .post-item {
    flex-direction: column;
    padding: 12px;
  }

  .post-left {
    margin-right: 0;
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .post-author-avatar {
    width: 40px;
    height: 40px;
  }

  .post-image-item {
    width: 80px;
    height: 60px;
  }

  .post-title {
    font-size: 16px;
  }

  .guess-item {
    min-width: 100%;
  }

  .guess-reason {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  /* 响应式模态框 */
  .detail-image {
    width: 100%;
    height: auto;
    max-height: 200px;
  }

  .detail-stats {
    flex-wrap: wrap;
    gap: 12px;
  }

  .detail-title {
    font-size: 18px;
  }
}
</style>