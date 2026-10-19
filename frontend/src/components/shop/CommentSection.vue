<template>
  <div class="comment-section">
    <!-- 评论提交表单 -->
    <div class="comment-form">
      <h3>发表评论</h3>
      
      <!-- 评分组件 -->
      <div class="rating-section">
        <span class="label">评分：</span>
        <div class="stars">
          <i 
            v-for="star in 5" 
            :key="star" 
            class="star"
            :class="{ active: star <= rating }"
            @click="setRating(star)"
          >★</i>
        </div>
      </div>
      
      <!-- 文本输入 -->
      <div class="input-section">
        <textarea 
          v-model="commentContent" 
          placeholder="请输入您的评价..." 
          maxlength="500"
          @input="validateForm"
        ></textarea>
        <span class="char-count">{{ commentContent.length }}/500</span>
      </div>
      
      <!-- 图片上传区域 -->
      <div class="image-upload">
        <span class="label">上传图片（可选，限一张）：</span>
        <div class="upload-area">
          <div 
            v-for="(image, index) in uploadedImages" 
            :key="index" 
            class="uploaded-image"
          >
            <img :src="image" alt="评论图片" />
            <span class="remove-btn" @click="removeImage(index)">×</span>
          </div>
          <div 
            v-if="uploadedImages.length === 0" 
            class="upload-btn"
            @click="triggerUpload"
          >
            <input 
              type="file" 
              ref="fileInput" 
              style="display:none" 
              accept="image/*" 
              @change="handleImageUpload"
            />
            <i>+</i>
          </div>
        </div>
      </div>
      
      <!-- 提交按钮 -->
      <button 
        class="submit-btn" 
        :disabled="!isFormValid"
        @click="submitComment"
        :class="{ disabled: !isFormValid }"
      >
        提交评论
      </button>
    </div>
    
    <!-- 评论列表 -->
    <div class="comment-list">
      <h3>评论列表 <span class="count">({{ totalCount }})</span></h3>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="comments.length === 0" class="empty-container">
        <el-empty description="暂无评论" />
      </div>
      
      <!-- 评论列表内容 -->
      <div v-else class="comments">
        <div 
          v-for="comment in comments" 
          :key="comment.reviewId" 
          class="comment-item"
        >
          <div class="comment-header">
            <img 
              :src="comment.userImage" 
              :alt="comment.username" 
              class="avatar"
              @error="handleAvatarError($event, comment.userId)"
            />
            <div class="user-info">
              <span class="username">{{ comment.username }}</span>
              <div class="rating">
                <i v-for="star in 5" :key="star" class="star" :class="{ active: star <= comment.level }">★</i>
              </div>
            </div>
            <span class="time">{{ formatTime(comment.createTime) }}</span>
          </div>
          
          <p class="comment-content">{{ comment.content }}</p>
          
          <!-- 评论图片 -->
          <div v-if="comment.image" class="comment-images">
            <img 
              :src="comment.image" 
              alt="评论图片" 
              class="comment-image"
              @click="previewImage(comment.image)"
              loading="lazy"
            />
          </div>
          
          <!-- 互动区域 -->
          <div class="comment-actions">
            <button 
              class="like-btn"
              :class="{ liked: comment.isCancel === 1 }"
              @click="toggleLike(comment)"
            >
              <i>♥</i>
              <span>{{ comment.likeCount }}</span>
            </button>
          </div>
        </div>
      </div>
      
      <!-- 下拉加载更多 -->
      <div v-if="hasMore && !loading" class="load-more">
        <button class="load-btn" @click="loadMore">
          加载更多
        </button>
      </div>
      
      <!-- 已加载全部 -->
      <div v-if="!hasMore && comments.length > 0" class="load-all">
        已加载全部评论
      </div>
    </div>
    
    <!-- 图片预览遮罩 -->
    <div 
      v-if="previewImageUrl" 
      class="image-preview-mask"
      @click="closePreview"
    >
      <img :src="previewImageUrl" alt="预览图片" class="preview-image" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { ElMessage, ElEmpty, ElSkeleton } from 'element-plus';
import { getCommentByGoodsId, commentGoods, likeComment } from '../../api/goods';

const props = defineProps({
  goodsId: {
    type: Number,
    required: true
  }
});

// 表单数据
const rating = ref(5);
const commentContent = ref('');
const uploadedImages = ref([]);
const fileInput = ref(null);

// 列表数据
const comments = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const hasMore = ref(true);
const totalCount = ref(0);

// 防重复点击状态
const likedComments = ref(new Set());

// 获取本地存储的点赞状态
const getLikedStatus = () => {
  try {
    const stored = localStorage.getItem('liked_comments');
    return stored ? JSON.parse(stored) : {};
  } catch (e) {
    return {};
  }
};

// 保存点赞状态到本地存储
const saveLikedStatus = (reviewId, isLiked) => {
  try {
    const liked = getLikedStatus();
    liked[reviewId] = isLiked;
    localStorage.setItem('liked_comments', JSON.stringify(liked));
  } catch (e) {
    console.error('保存点赞状态失败:', e);
  }
};

// 图片预览
const previewImageUrl = ref('');

// 表单验证
const isFormValid = computed(() => {
  return rating.value > 0 && commentContent.value.trim().length > 0 && commentContent.value.trim().length <= 500;
});

// 设置评分
const setRating = (star) => {
  rating.value = star;
};

// 触发文件上传
const triggerUpload = () => {
  fileInput.value?.click();
};

// 处理图片上传
const handleImageUpload = (event) => {
  const files = event.target.files;
  if (files) {
    Array.from(files).forEach(file => {
      if (uploadedImages.value.length < 5) {
        const reader = new FileReader();
        reader.onload = (e) => {
          uploadedImages.value.push(e.target.result);
        };
        reader.readAsDataURL(file);
      }
    });
  }
  // 重置input
  event.target.value = '';
};

// 移除图片
const removeImage = (index) => {
  uploadedImages.value.splice(index, 1);
};

// 表单验证提示
const validateForm = () => {
  if (commentContent.value.length > 500) {
    ElMessage.warning('评论内容不能超过500字');
  }
};

// 提交评论
const submitComment = async () => {
  if (!isFormValid.value) return;
  
  loading.value = true;
  try {
    let file = null;
    if (uploadedImages.value.length > 0) {
      const blob = await fetch(uploadedImages.value[0]).then(res => res.blob());
      file = new File([blob], 'comment.jpg', { type: 'image/jpeg' });
    }
    
    const commentRes = await commentGoods(props.goodsId, commentContent.value.trim(), rating.value, file);
    
    if (commentRes) {
      ElMessage.success('评论提交成功');
      // 重置表单
      rating.value = 5;
      commentContent.value = '';
      uploadedImages.value = [];
      // 刷新评论列表
      refreshComments();
    } else {
      ElMessage.error('评论提交失败');
    }
  } catch (error) {
    console.error('提交评论失败:', error);
    ElMessage.error('评论提交失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 获取评论列表
const fetchComments = async (pageNum = 1, reset = false) => {
  loading.value = true;
  try {
    const data = await getCommentByGoodsId(props.goodsId);
    const storedLiked = getLikedStatus();
    
    if (Array.isArray(data)) {
      const formattedComments = data.map(item => {
        const storedStatus = storedLiked[item.reviewId];
        const isCancel = storedStatus !== undefined ? (storedStatus ? 1 : -1) : (item.isCancel || -1);
        
        return {
          reviewId: item.reviewId,
          userId: item.userId,
          username: item.username,
          content: item.content,
          level: item.level,
          likeCount: Math.max(0, parseInt(item.likeCount) || 0),
          image: item.image,
          userImage: item.userImage,
          isCancel: isCancel,
          createTime: item.createTime || Date.now()
        };
      });
      
      if (reset) {
        comments.value = formattedComments;
      } else {
        comments.value = [...comments.value, ...formattedComments];
      }
      
      totalCount.value = data.length;
      hasMore.value = formattedComments.length >= pageSize.value;
    }
  } catch (error) {
    console.error('获取评论失败:', error);
    ElMessage.error('获取评论失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 头像加载失败处理
const handleAvatarError = (event, userId) => {
  event.target.src = `https://picsum.photos/100/100?random=${userId}`;
};

// 加载更多
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    page.value++;
    fetchComments(page.value);
  }
};

// 刷新评论
const refreshComments = () => {
  page.value = 1;
  fetchComments(1, true);
};

// 点赞/取消点赞
const toggleLike = async (comment) => {
  const reviewId = comment.reviewId;
  
  if (likedComments.value.has(reviewId)) {
    ElMessage.warning('操作过于频繁，请稍后再试');
    return;
  }
  
  try {
    likedComments.value.add(reviewId);
    
    const isLiked = comment.isCancel === 1;
    const cancel = isLiked ? -1 : 1;
    const res = await likeComment(comment.reviewId, cancel);
    
    if (res) {
      if (isLiked) {
        comment.likeCount = Math.max(0, comment.likeCount - 1);
        comment.isCancel = -1;
        saveLikedStatus(reviewId, false);
        ElMessage.success('取消点赞成功');
      } else {
        comment.likeCount = Math.max(0, comment.likeCount + 1);
        comment.isCancel = 1;
        saveLikedStatus(reviewId, true);
        ElMessage.success('点赞成功');
      }
    }
  } catch (error) {
    console.error('点赞失败:', error);
    ElMessage.error('操作失败，请稍后重试');
  } finally {
    setTimeout(() => {
      likedComments.value.delete(reviewId);
    }, 1500);
  }
};

// 图片预览
const previewImage = (url) => {
  previewImageUrl.value = url;
};

// 关闭预览
const closePreview = () => {
  previewImageUrl.value = '';
};

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) {
    return '';
  } else if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前';
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前';
  } else {
    return `${date.getMonth() + 1}/${date.getDate()}`;
  }
};

// 监听商品ID变化
watch(() => props.goodsId, () => {
  refreshComments();
});

// 页面加载时获取评论
onMounted(() => {
  fetchComments(1, true);
});
</script>

<style scoped>
.comment-section {
  padding: 20px;
  background-color: #f5f5f5;
}

/* 评论表单 */
.comment-form {
  background-color: white;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.comment-form h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
}

.rating-section {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
}

.stars {
  display: flex;
}

.star {
  font-size: 24px;
  color: #ddd;
  cursor: pointer;
  transition: color 0.2s;
}

.star.active {
  color: #FFD700;
}

.input-section {
  position: relative;
  margin-bottom: 15px;
}

.input-section textarea {
  width: 100%;
  height: 100px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;
  font-size: 14px;
  box-sizing: border-box;
}

.char-count {
  position: absolute;
  right: 12px;
  bottom: 12px;
  font-size: 12px;
  color: #999;
}

.image-upload {
  margin-bottom: 20px;
}

.upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.uploaded-image {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}

.uploaded-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 24px;
  height: 24px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  text-align: center;
  line-height: 24px;
  cursor: pointer;
  font-size: 18px;
}

.upload-btn {
  width: 80px;
  height: 80px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: #999;
  font-size: 32px;
}

.upload-btn:hover {
  border-color: #8B4513;
  color: #8B4513;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #8B4513;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-btn:hover:not(.disabled) {
  background-color: #A0522D;
}

.submit-btn.disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 评论列表 */
.comment-list {
  background-color: white;
  padding: 20px;
  border-radius: 10px;
}

.comment-list h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.count {
  font-size: 14px;
  color: #999;
  margin-left: 5px;
}

.loading-container,
.empty-container {
  padding: 40px 0;
  text-align: center;
}

.comments {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
}

.user-info {
  flex: 1;
}

.username {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
  display: block;
}

.user-info .rating .star {
  font-size: 14px;
}

.time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-images {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.comment-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
}

.comment-actions {
  display: flex;
  gap: 20px;
}

.like-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 14px;
  color: #999;
  transition: color 0.2s;
}

.like-btn i {
  font-size: 18px;
}

.like-btn:hover,
.like-btn.liked {
  color: #e74c3c;
}

.load-more,
.load-all {
  text-align: center;
  padding: 20px;
  color: #999;
}

.load-btn {
  padding: 10px 30px;
  border: 1px solid #8B4513;
  border-radius: 20px;
  background-color: white;
  color: #8B4513;
  cursor: pointer;
  transition: all 0.3s;
}

.load-btn:hover {
  background-color: #8B4513;
  color: white;
}

/* 图片预览 */
.image-preview-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.preview-image {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .comment-section {
    padding: 10px;
  }
  
  .comment-form,
  .comment-list {
    padding: 15px;
  }
  
  .comment-image {
    width: 80px;
    height: 80px;
  }
  
  .uploaded-image,
  .upload-btn {
    width: 60px;
    height: 60px;
  }
}
</style>