<template>
  <div class="my-posts-page">
    <div class="my-posts-container">
      <div class="page-header">
        <el-button class="back-btn" @click="goBack" :icon="ArrowLeft" circle />
        <h1 class="page-title">我的帖子</h1>
        <el-button type="primary" @click="goToPublish">
          <el-icon><Edit /></el-icon>
          发布新帖
        </el-button>
      </div>

      <el-tabs v-model="activeTab" class="custom-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="我发布的帖子" name="posts">
          <div class="toolbar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索我的帖子..."
              :prefix-icon="Search"
              clearable
              @input="handleSearch"
              class="search-input"
            />
          </div>

          <div v-loading="loading" class="posts-list">
            <div v-if="postList.length === 0 && !loading" class="empty-state">
              <el-empty description="您还没有发布过帖子" />
              <el-button type="primary" @click="goToPublish" class="empty-btn">
                立即发布
              </el-button>
            </div>

            <div
              v-for="post in postList"
              :key="post.id"
              class="post-card"
            >
              <div class="post-content" @click="goToDetail(post.id)">
                <div class="post-header">
                  <el-tag type="success" size="small">已发布</el-tag>
                </div>
                <h3 class="post-title">{{ post.title }}</h3>
                <p class="post-content-text">{{ truncateContent(post.content) }}</p>
                <div v-if="post.image" class="post-images">
                  <div class="post-image-item">
                    <img :src="(post.image || '').trim()" alt="帖子图片" @error="handleImageError($event)" />
                  </div>
                </div>
                <div class="post-stats">
                  <span class="stat-item like-item disabled" :title="'不可操作'"><!-- 移除点击事件 -->
                    <span class="stat-icon" :class="{ 'active': post.isLiked }">
                      <el-icon><Pointer v-if="!post.isLiked" /><Pointer v-else /></el-icon>
                    </span>
                    <span class="stat-count" :class="{ 'active': post.isLiked }">{{ post.likeCount || 0 }}</span>
                  </span>
                  <span class="stat-item favorite-item disabled" :title="'不可操作'"><!-- 移除点击事件 -->
                    <span class="stat-icon" :class="{ 'active': post.isFavorited }">
                      <el-icon><Star v-if="!post.isFavorited" /><StarFilled v-else /></el-icon>
                    </span>
                    <span class="stat-count" :class="{ 'active': post.isFavorited }">{{ post.collectCount || post.favoriteCount || 0 }}</span>
                  </span>
                  <span class="stat-item comment-item" @click.stop="goToDetail(post.id)" title="评论">
                    <span class="stat-icon">
                      <el-icon><ChatDotRound /></el-icon>
                    </span>
                    <span class="stat-count">{{ post.commentCount || 0 }}</span>
                  </span>
                </div>
              </div>
              <div class="post-actions">
                <el-button type="primary" text @click="goToDetail(post.id)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button type="warning" text @click="handleEdit(post)">
                  <el-icon><Edit /></el-icon>
                  修改
                </el-button>
                <el-button type="danger" text @click="handleDelete(post.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>

          <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 30]"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我收藏的帖子" name="favorites">
          <div class="toolbar">
            <el-input
              v-model="favSearchKeyword"
              placeholder="搜索我收藏的帖子..."
              :prefix-icon="Search"
              clearable
              @input="handleFavSearch"
              class="search-input"
            />
          </div>

          <div v-loading="favLoading" class="posts-list">
            <div v-if="favList.length === 0 && !favLoading" class="empty-state">
              <el-empty description="您还没有收藏过帖子" />
            </div>

            <div
              v-for="post in favList"
              :key="post.id"
              class="post-card"
            >
              <div class="post-content" @click="goToDetail(post.id)">
                <div class="post-header">
                  <span class="author-name">作者：{{ post.writerName || '未知' }}</span>
                </div>
                <h3 class="post-title">{{ post.title }}</h3>
                <p class="post-content-text">{{ truncateContent(post.content) }}</p>
                <div v-if="post.image" class="post-images">
                  <div class="post-image-item">
                    <img :src="(post.image || '').trim()" alt="帖子图片" @error="handleImageError($event)" />
                  </div>
                </div>
                <div class="post-stats">
                  <span class="stat-item like-item" :title="post.isLiked ? '已点赞' : '点赞'">
                    <span class="stat-icon" :class="{ 'active': post.isLiked }">
                      <el-icon><Pointer v-if="!post.isLiked" /><Pointer v-else /></el-icon>
                    </span>
                    <span class="stat-count" :class="{ 'active': post.isLiked }">{{ post.likeCount || 0 }}</span>
                  </span>
                  <span class="stat-item favorite-item" :title="post.isFavorited ? '已收藏' : '收藏'">
                    <span class="stat-icon" :class="{ 'active': post.isFavorited }">
                      <el-icon><Star v-if="!post.isFavorited" /><StarFilled v-else /></el-icon>
                    </span>
                    <span class="stat-count" :class="{ 'active': post.isFavorited }">{{ post.collectCount || post.favoriteCount || 0 }}</span>
                  </span>
                  <span class="stat-item comment-item" @click.stop="goToDetail(post.id)" title="评论">
                    <span class="stat-icon">
                      <el-icon><ChatDotRound /></el-icon>
                    </span>
                    <span class="stat-count">{{ post.commentCount || 0 }}</span>
                  </span>
                </div>
              </div>
              <div class="post-actions">
                <el-button type="primary" text @click="goToDetail(post.id)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button 
                  type="danger" 
                  text 
                  @click="handleUnfavorite(post.id)"
                  :loading="unfavoritingIds.has(post.id)"
                  :disabled="unfavoritingIds.has(post.id) || (post.collectCount !== undefined && post.collectCount <= 0)"
                >
                  <el-icon><StarFilled /></el-icon>
                  {{ unfavoritingIds.has(post.id) ? '取消中...' : '取消收藏' }}
                </el-button>
              </div>
            </div>
          </div>

          <div class="pagination-wrapper" v-if="favTotal > 0">
            <el-pagination
              v-model:current-page="favCurrentPage"
              v-model:page-size="favPageSize"
              :page-sizes="[10, 20, 30]"
              :total="favTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="handleFavSizeChange"
              @current-change="handleFavCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog
      v-model="editDialogVisible"
      title="修改帖子"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-position="top"
      >
        <el-form-item label="帖子标题" prop="title">
          <el-input
            v-model="editForm.title"
            placeholder="请输入帖子标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="帖子内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入帖子内容"
            maxlength="10000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="帖子图片（可选）">
          <div class="edit-upload-section">
            <div v-if="!editForm.image && !editPreviewUrl" class="edit-upload-trigger">
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleEditFileChange"
                accept="image/jpeg,image/png,image/webp,image/gif"
              >
                <el-button type="primary" size="small">
                  <el-icon><Plus /></el-icon>
                  上传图片
                </el-button>
              </el-upload>
            </div>
            <div v-else class="edit-image-preview">
              <img :src="(editForm.image || editPreviewUrl || '').trim()" alt="帖子图片" />
              <el-button type="danger" size="small" @click="handleRemoveEditImage" class="remove-btn">
                <el-icon><Delete /></el-icon>
                删除图片
              </el-button>
            </div>
            <div class="upload-tip">支持 JPG、PNG、WEBP、GIF 格式，文件大小不超过 5MB</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEdit" :loading="editLoading">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, Edit, Search, View, ChatDotRound, Star, Delete, StarFilled, Pointer, Plus } from '@element-plus/icons-vue';
import { getMyPost, getCollect, updateMyPost, deleteMyPost, unfavoritePost, likePost, unlikePost, favoritePost, uploadPostImage } from '../../api/community';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();

const activeTab = ref('posts');
const searchKeyword = ref('');
const favSearchKeyword = ref('');

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const postList = ref([]);

const favCurrentPage = ref(1);
const favPageSize = ref(10);
const favTotal = ref(0);
const favLoading = ref(false);
const favList = ref([]);
const unfavoritingIds = ref(new Set()); // 用于跟踪正在取消收藏的帖子ID，防止重复点击

const editDialogVisible = ref(false);
const editFormRef = ref(null);
const editLoading = ref(false);
const editPreviewUrl = ref('');
const editFile = ref(null);
const editForm = reactive({
  postId: null,
  title: '',
  content: '',
  image: ''
});

const editRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' }
  ]
};

const postCache = ref({});
const collectCache = ref({});

let searchTimer = null;
let favSearchTimer = null;

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
  return content.length > 100 ? content.substring(0, 100) + '...' : content;
};

const handleImageError = (event) => {
  event.target.style.display = 'none';
};

const handleTabChange = (tabName) => {
  if (tabName === 'posts') {
    if (postList.value.length === 0) {
      fetchMyPosts();
    }
  } else {
    if (favList.value.length === 0) {
      fetchCollect();
    }
  }
};

const fetchMyPosts = async () => {
  const cacheKey = `${currentPage.value}-${pageSize.value}-${searchKeyword.value}`;
  if (postCache.value[cacheKey]) {
    postList.value = postCache.value[cacheKey];
    total.value = postCache.value[cacheKey].length;
    return;
  }

  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      name: searchKeyword.value
    };
    console.log('请求我的帖子参数:', params);
    const data = await getMyPost(params);
    console.log('我的帖子返回数据:', data);
    let list = Array.isArray(data) ? data : [];
    console.log('解析后的列表:', list);
    
    // 处理每个帖子的点赞和收藏状态
    list = list.map(post => ({
      ...post,
      isLiked: post.likeCancel === 1,
      isFavorited: post.collectCancel === 1
    }));
    
    postList.value = list;
    total.value = list.length;
    postCache.value[cacheKey] = list;
  } catch (error) {
    console.error('获取我的帖子失败:', error);
    ElMessage.error('获取帖子列表失败');
  } finally {
    loading.value = false;
  }
};

const fetchCollect = async () => {
  const cacheKey = `${favCurrentPage.value}-${favPageSize.value}-${favSearchKeyword.value}`;
  if (collectCache.value[cacheKey]) {
    favList.value = collectCache.value[cacheKey];
    favTotal.value = collectCache.value[cacheKey].length;
    return;
  }

  favLoading.value = true;
  try {
    const params = {
      page: favCurrentPage.value,
      pageSize: favPageSize.value,
      name: favSearchKeyword.value
    };
    console.log('请求收藏列表参数:', params);
    const data = await getCollect(params);
    console.log('收藏列表返回数据:', data);
    let list = Array.isArray(data) ? data : [];
    console.log('解析后的列表:', list);
    
    // 处理每个帖子的点赞和收藏状态
    list = list.map(post => ({
      ...post,
      isLiked: post.likeCancel === 1,
      isFavorited: post.collectCancel === 1
    }));
    
    favList.value = list;
    favTotal.value = list.length;
    collectCache.value[cacheKey] = list;
  } catch (error) {
    console.error('获取收藏列表失败:', error);
    //ElMessage.error('获取收藏列表失败');
  } finally {
    favLoading.value = false;
  }
};

const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    currentPage.value = 1;
    postCache.value = {};
    fetchMyPosts();
  }, 300);
};

const handleFavSearch = () => {
  if (favSearchTimer) clearTimeout(favSearchTimer);
  favSearchTimer = setTimeout(() => {
    favCurrentPage.value = 1;
    collectCache.value = {};
    fetchCollect();
  }, 300);
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  postCache.value = {};
  fetchMyPosts();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchMyPosts();
};

const handleFavSizeChange = (val) => {
  favPageSize.value = val;
  favCurrentPage.value = 1;
  collectCache.value = {};
  fetchCollect();
};

const handleFavCurrentChange = (val) => {
  favCurrentPage.value = val;
  fetchCollect();
};

const goBack = () => {
  router.push('/community');
};

const goToPublish = () => {
  router.push('/community/publish');
};

const goToDetail = (id) => {
  router.push(`/community/post/${id}`);
};

const handleEdit = (post) => {
  editForm.postId = post.id;
  editForm.title = post.title;
  editForm.content = post.content;
  editForm.image = post.image || '';
  editPreviewUrl.value = '';
  editFile.value = null;
  editDialogVisible.value = true;
};

const handleEditFileChange = (uploadFile) => {
  const file = uploadFile.raw || uploadFile;
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isImage) {
    ElMessage.error('只能上传图片文件！');
    return;
  }

  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！');
    return;
  }

  if (editPreviewUrl.value) {
    URL.revokeObjectURL(editPreviewUrl.value);
  }

  editPreviewUrl.value = URL.createObjectURL(file);
  editFile.value = file;
  // 保持 editForm.image 为字符串类型
};

const handleRemoveEditImage = () => {
  if (editPreviewUrl.value) {
    URL.revokeObjectURL(editPreviewUrl.value);
  }
  editPreviewUrl.value = '';
  editForm.image = '';
  editFile.value = null;
};

const handleSubmitEdit = async () => {
  if (!editFormRef.value) return;
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    editLoading.value = true;
    try {
      const data = {
        postId: editForm.postId,
        title: editForm.title,
        content: editForm.content
      };
      await updateMyPost(data);
      
      if (editFile.value) {
        const formData = new FormData();
        formData.append('file', editFile.value);
        const imageUrl = await uploadPostImage(String(editForm.postId), formData);
        // 更新 editForm.image 为返回的图片URL
        if (imageUrl) {
          editForm.image = imageUrl;
        }
      }
      
      ElMessage.success('帖子修改成功');
      editDialogVisible.value = false;
      postCache.value = {};
      fetchMyPosts();
    } catch (error) {
      console.error('修改帖子失败:', error);
      ElMessage.error('修改失败，请重试');
    } finally {
      editLoading.value = false;
    }
  });
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await deleteMyPost(String(id));
    ElMessage.success('帖子删除成功');
    postCache.value = {};
    fetchMyPosts();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error);
      ElMessage.error('删除失败，请重试');
    }
  }
};

const handleUnfavorite = async (id) => {
  // 防重复点击检查
  if (unfavoritingIds.value.has(id)) {
    console.log('正在取消收藏中，请勿重复点击');
    return;
  }

  // 查找当前帖子
  const post = favList.value.find(p => p.id === id);
  
  // 边界值校验：检查收藏数量
  if (post && post.collectCount !== undefined && post.collectCount <= 0) {
    ElMessage.warning('该帖子收藏数量已为0，无法继续取消收藏');
    return;
  }

  try {
    await ElMessageBox.confirm('确定要取消收藏这篇帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    // 添加到正在取消收藏的集合中，禁用按钮
    unfavoritingIds.value.add(id);

    await unfavoritePost(String(id));
    ElMessage.success('已取消收藏');
    
    // 立即更新本地数据，优化用户体验
    if (post) {
      post.collectCount = Math.max(0, (post.collectCount || 1) - 1);
      post.isFavorited = false;
    }
    
    // 清除缓存并重新加载数据
    collectCache.value = {};
    await fetchCollect();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error);
      ElMessage.error('操作失败，请重试');
      // 恢复本地数据
      if (post) {
        post.collectCount = (post.collectCount || 0) + 1;
        post.isFavorited = true;
      }
    }
  } finally {
    // 从正在取消收藏的集合中移除，恢复按钮状态
    unfavoritingIds.value.delete(id);
  }
};

// 处理点赞
const handleLike = async (post) => {
  const postId = post.id;
  try {
    if (post.isLiked) {
      await unlikePost(postId);
      post.likeCount = Math.max(0, (post.likeCount || 1) - 1);
      post.isLiked = false;
      ElMessage.success('已取消点赞');
    } else {
      await likePost(postId);
      post.likeCount = (post.likeCount || 0) + 1;
      post.isLiked = true;
      ElMessage.success('点赞成功');
    }
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

// 处理收藏
const handleFavorite = async (post) => {
  const postId = post.id;
  try {
    if (post.isFavorited) {
      await unfavoritePost(postId);
      post.collectCount = Math.max(0, (post.collectCount || 1) - 1);
      post.isFavorited = false;
      ElMessage.success('已取消收藏');
    } else {
      await favoritePost(postId);
      post.collectCount = (post.collectCount || 0) + 1;
      post.isFavorited = true;
      ElMessage.success('收藏成功');
    }
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

onMounted(() => {
  fetchMyPosts();
});
</script>

<style scoped>
.my-posts-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4f3 100%);
  padding: 80px 20px 40px;
}

.my-posts-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.back-btn {
  background: white;
  border: 1px solid #dcdfe6;
}

.page-title {
  flex: 1;
  font-size: 28px;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.page-header .el-button--primary {
  background: linear-gradient(135deg, #008080 0%, #2a9d8f 100%);
  border: none;
  border-radius: 20px;
  padding: 10px 25px;
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #008080;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(135deg, #008080 0%, #2a9d8f 100%);
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #e8e8e8;
}

.toolbar {
  margin-bottom: 25px;
}

.search-input {
  max-width: 350px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 25px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-card {
  background: white;
  border-radius: 16px;
  padding: 20px 25px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.post-card:hover {
  box-shadow: 0 4px 20px rgba(0, 128, 128, 0.15);
}

.post-content {
  cursor: pointer;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-time {
  color: #999;
  font-size: 14px;
}

.author-name {
  color: #008080;
  font-size: 14px;
}

.post-title {
  font-size: 18px;
  color: #2c3e50;
  margin: 0 0 10px;
  font-weight: 600;
  line-height: 1.4;
}

.post-content-text {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 15px;
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

.edit-upload-section {
  width: 100%;
}

.edit-upload-trigger {
  display: inline-block;
}

.edit-image-preview {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.edit-image-preview img {
  width: 150px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #67c23a;
}

.edit-image-preview .remove-btn {
  margin-top: 30px;
}

.post-stats {
  display: flex;
  gap: 20px;
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

/* 收藏页面的点赞和收藏按钮不可点击 */
.el-tab-pane[name="favorites"] .like-item,
.el-tab-pane[name="favorites"] .favorite-item {
  cursor: default;
}

.el-tab-pane[name="favorites"] .like-item:hover,
.el-tab-pane[name="favorites"] .favorite-item:hover {
  background: none;
  color: #999;
  border-color: #eee;
}

.el-tab-pane[name="favorites"] .like-item:hover .el-icon {
  color: #999;
}

.el-tab-pane[name="favorites"] .favorite-item:hover .el-icon {
  color: #999;
}

/* 禁用状态样式 */
.stat-item.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.stat-item.disabled:hover {
  background: none;
  color: #999;
  border-color: #eee;
}

.stat-item.disabled:hover .el-icon {
  color: #999;
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

.post-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.empty-btn {
  margin-top: 20px;
  background: linear-gradient(135deg, #008080 0%, #2a9d8f 100%);
  border: none;
  border-radius: 20px;
  padding: 12px 30px;
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

.pagination-wrapper :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #008080 0%, #2a9d8f 100%);
}

@media (max-width: 768px) {
  .page-header {
    flex-wrap: wrap;
  }

  .page-title {
    font-size: 22px;
  }

  .search-input {
    max-width: none;
  }

  .post-card {
    padding: 15px;
  }

  .post-title {
    font-size: 16px;
  }
}
</style>
