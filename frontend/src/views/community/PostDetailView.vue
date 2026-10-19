<template>
  <div class="post-detail-page">
    <div class="detail-container">
      <div class="page-header">
        <el-button class="back-btn" @click="goBack" :icon="ArrowLeft" circle />
        <h1 class="page-title">帖子详情</h1>
      </div>

      <div v-loading="loading" class="post-content">
        <div v-if="!loading && !post" class="empty-state">
          <el-empty description="帖子不存在或已被删除" />
        </div>

        <template v-else>
          <div class="post-main">
            <div class="post-author-section">
              <img
                :src="post.authorAvatar || defaultAvatar"
                alt="头像"
                class="author-avatar"
                @error="handleAvatarError"
              />
              <div class="author-info">
                <span class="author-name">{{ post.authorName || '匿名茶友' }}</span>
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
              </div>
              <div v-if="isAuthor" class="post-actions">
                <el-button type="danger" text @click="handleDeletePost">
                  <el-icon><Delete /></el-icon>
                  删除帖子
                </el-button>
              </div>
            </div>

            <h1 class="post-title">{{ post.title }}</h1>

            <div class="post-body" v-html="formatContent(post.content)"></div>

            <div v-if="post.image" class="post-images">
              <el-image
                :src="(post.image || '').trim()"
                :preview-src-list="[(post.image || '').trim()]"
                fit="cover"
                class="post-image"
              />
            </div>

            <div class="post-stats">
              <span class="stat-item like-item" @click="handleLike(post)" :title="post.isLiked ? '取消点赞' : '点赞'">
                <span class="stat-icon" :class="{ 'active': post.isLiked }">
                  <el-icon><Pointer /></el-icon>
                </span>
                <span class="stat-count" :class="{ 'active': post.isLiked }">{{ post.likeCount || 0 }} 点赞</span>
              </span>
              <span class="stat-item favorite-item" @click="handleFavorite(post)" :title="post.isFavorited ? '取消收藏' : '收藏'">
                <span class="stat-icon" :class="{ 'active': post.isFavorited }">
                  <el-icon><Star v-if="!post.isFavorited" /><StarFilled v-else /></el-icon>
                </span>
                <span class="stat-count" :class="{ 'active': post.isFavorited }">{{ post.collectCount || 0 }} 收藏</span>
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ comments.length }} 评论
              </span>
            </div>
          </div>

          <div class="comments-section">
            <h2 class="section-title">
              <el-icon><ChatDotSquare /></el-icon>
              评论 ({{ comments.length }})
            </h2>

            <div class="comment-form">
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="3"
                placeholder="写下你的评论..."
                maxlength="500"
                show-word-limit
              />
              <el-button
                type="primary"
                @click="handleAddComment"
                :loading="commentLoading"
                class="comment-submit-btn"
              >
                发表评论
              </el-button>
            </div>

            <div v-if="comments.length === 0" class="no-comments">
              <el-empty description="暂无评论，快来抢沙发吧！" />
            </div>

            <div class="comments-list">
              <template v-for="comment in comments" :key="comment.id">
                <div class="comment-item" :class="{ 'is-reply': false }">
                  <img
                    :src="comment.authorAvatar || defaultAvatar"
                    alt="头像"
                    class="comment-avatar"
                    @error="handleCommentAvatarError($event, comment)"
                  />
                  <div class="comment-body">
                    <div class="comment-header">
                      <span class="comment-author">{{ comment.authorName || '匿名茶友' }}</span>
                      <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                    </div>
                    <div class="comment-content">{{ comment.content }}</div>
                    <div class="comment-footer">
                      <el-button type="primary" text size="small" @click="handleReply(comment, $event)">
                        回复
                      </el-button>
                      <div class="comment-actions-right">
                        <el-button type="primary" text size="small" @click="handleLikeComment(comment, $event)" :class="{ 'active': comment.isLiked }">
                          <el-icon><Pointer /></el-icon>
                          {{ comment.likeCount || 0 }}
                        </el-button>
                      </div>
                    </div>
                    <div v-if="comment.authorId === currentUserId" class="comment-actions">
                      <el-button type="danger" text size="small" @click="handleDeleteComment(comment.id)">
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>

                <div v-if="replyingTo && replyingTo.targetCommentId === comment.id" class="reply-input-box">
                  <div class="reply-tip">
                    回复 @{{ replyingTo.username }}
                    <el-button type="danger" text size="small" @click="cancelReply">取消</el-button>
                  </div>
                  <el-input
                    v-model="newComment"
                    type="textarea"
                    :rows="2"
                    placeholder="写下你的回复..."
                    maxlength="500"
                  />
                  <div class="reply-actions">
                    <el-button size="small" @click="cancelReply">取消</el-button>
                    <el-button type="primary" size="small" @click="submitReply" :loading="commentLoading">
                      发表回复
                    </el-button>
                  </div>
                </div>

                <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                  <div
                    v-for="reply in comment.replies"
                    :key="reply.id"
                    class="comment-item is-reply"
                  >
                    <img
                      :src="reply.authorAvatar || defaultAvatar"
                      alt="头像"
                      class="comment-avatar reply-avatar"
                      @error="handleCommentAvatarError($event, reply)"
                    />
                    <div class="comment-body">
                      <div class="comment-header">
                        <span class="comment-author">{{ reply.authorName || '匿名茶友' }}</span>
                        <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
                        <span v-if="getToUsername(reply)" class="reply-to">
                          回复 @{{ getToUsername(reply) }}
                        </span>
                      </div>
                      <div class="comment-content">{{ reply.content }}</div>
                      <div class="comment-footer">
                        <el-button type="primary" text size="small" @click="handleReply(reply, $event)">
                          回复
                        </el-button>
                        <div class="comment-actions-right">
                          <el-button type="primary" text size="small" @click="handleLikeComment(reply, $event)" :class="{ 'active': reply.isLiked }">
                            <el-icon><Pointer /></el-icon>
                            {{ reply.likeCount || 0 }}
                          </el-button>
                        </div>
                      </div>
                      <div v-if="reply.authorId === currentUserId" class="comment-actions">
                        <el-button type="danger" text size="small" @click="handleDeleteComment(reply.id)">
                          <el-icon><Delete /></el-icon>
                          删除
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ArrowLeft, Delete, View, ChatDotRound, ChatDotSquare, Loading, PictureFilled, Pointer, Star, StarFilled } from '@element-plus/icons-vue';
import { getPostDetail, deleteMyPost, getComments, comment, deleteComment, likeComment, unlikeComment, likePost, unlikePost, favoritePost, unfavoritePost } from '../../api/community';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useUserStore } from '../../stores/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const loading = ref(false);
const post = ref(null);
const comments = ref([]);
const newComment = ref('');
const commentLoading = ref(false);
const replyingTo = ref(null);
const defaultAvatar = '/assest/default.jpg';

const currentUserId = computed(() => userStore.userInfo?.id);
const isAuthor = computed(() => post.value?.authorId === currentUserId.value);

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

const buildCommentTree = (comments) => {
  console.log('buildCommentTree input:', JSON.stringify(comments.map(c => ({ id: c.id, parentId: c.parentId, authorName: c.authorName }))));
  const commentMap = {};
  const rootComments = [];
  
  comments.forEach(comment => {
    comment.replies = [];
    commentMap[comment.id] = comment;
  });
  
  comments.forEach(comment => {
    const parentId = comment.parentId;
    const isRootComment = !parentId || parentId === 0 || parentId === '0' || parentId === comment.id;
    
    if (isRootComment) {
      rootComments.push(comment);
    } else {
      const parentIdNum = Number(parentId);
      const parent = commentMap[parentId] || commentMap[parentIdNum];
      if (parent && parent.id !== comment.id) {
        parent.replies.push(comment);
      } else {
        console.warn('找不到父评论或父评论指向自己:', parentId, 'for comment:', comment.id, 'available ids:', Object.keys(commentMap));
        rootComments.push(comment);
      }
    }
  });
  
  const sortComments = (commentList) => {
    commentList.sort((a, b) => new Date(a.createTime) - new Date(b.createTime));
    commentList.forEach(comment => {
      if (comment.replies && comment.replies.length > 0) {
        sortComments(comment.replies);
      }
    });
  };
  
  sortComments(rootComments);
  
  console.log('buildCommentTree output:', JSON.stringify(rootComments.map(c => ({ id: c.id, authorName: c.authorName, repliesCount: c.replies.length }))));
  return rootComments;
};

const getUsernameById = (userId) => {
  const allComments = flattenComments(comments.value);
  const comment = allComments.find(c => c.authorId === userId);
  return comment ? comment.authorName : '未知用户';
};

const flattenComments = (commentList) => {
  const result = [];
  const flatten = (list) => {
    list.forEach(comment => {
      result.push(comment);
      if (comment.replies && comment.replies.length > 0) {
        flatten(comment.replies);
      }
    });
  };
  flatten(commentList);
  return result;
};

const getToUsername = (comment) => {
  if (comment.toUserId === 0 || comment.toUserId === post.value?.authorId) {
    return null;
  }
  return getUsernameById(comment.toUserId);
};

const formatContent = (content) => {
  if (!content) return '';
  let html = content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/~~(.*?)~~/g, '<del>$1</del>')
    .replace(/^### (.*)$/gm, '<h3>$1</h3>')
    .replace(/^## (.*)$/gm, '<h2>$1</h2>')
    .replace(/^# (.*)$/gm, '<h1>$1</h1>')
    .replace(/^- (.*)$/gm, '<li>$1</li>')
    .replace(/^\d+\. (.*)$/gm, '<li>$1</li>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" target="_blank">$1</a>')
    .replace(/^> (.*)$/gm, '<blockquote>$1</blockquote>')
    .replace(/\n/g, '<br>');
  return html;
};

const handleAvatarError = (event) => {
  event.target.src = defaultAvatar;
  event.target.onerror = null;
};

const handleCommentAvatarError = (event, comment) => {
  event.target.src = defaultAvatar;
  event.target.onerror = null;
};

const fetchPostDetail = async () => {
  const postId = route.params.id;
  
  if (!postId) {
    ElMessage.error('帖子ID无效，无法加载帖子详情');
    loading.value = false;
    return;
  }
  
  console.log('获取帖子详情，postId:', postId);
  
  loading.value = true;
  try {
    const data = await getPostDetail(postId);
    post.value = {
      id: data.postId,
      authorId: data.userId,
      authorName: data.username,
      authorAvatar: (data.userImage || '').trim(),
      title: data.title,
      content: data.content,
      likeCount: data.likeCount,
      collectCount: data.collectCount,
      commentCount: data.commentCount,
      createTime: data.createTime,
      image: data.image,
      isLiked: data.likeCancel === 1,
      isFavorited: data.collectCancel === 1
    };
    comments.value = (data.commentDTOS || []).map(item => ({
        id: item.id,
        parentId: item.parentId,
        toUserId: item.toUserId,
        authorId: item.userId,
        authorName: item.username,
        authorAvatar: (item.userImage || '').trim(),
        content: item.content,
        createTime: item.createTime,
        likeCount: item.likeCount,
        likeCancel: item.likeCancel,
        isLiked: item.likeCancel === 1,
        replies: []
      }));
    
    comments.value = buildCommentTree(comments.value);
    console.log('帖子详情数据:', post.value);
    console.log('评论数据:', comments.value);
  } catch (error) {
    console.error('获取帖子详情失败:', error);
    ElMessage.error('帖子不存在或已被删除');
    const mockPosts = {
      1: {
        id: 1,
        title: '分享一款好茶：武夷山正山小种',
        content: '今天给大家分享一款我最近在喝的红茶——武夷山正山小种。\n\n这款茶产自福建武夷山保护区，海拔在1200米以上，常年云雾缭绕，气候温和湿润。茶树生长在原始森林中，与其他植物共生，形成了独特的生态环境。\n\n## 口感特点\n\n- **桂圆香**：带有独特的桂圆甜香\n- **醇厚回甘**：茶汤醇厚，入口甘甜\n- **汤色红亮**：呈琥珀色，清澈透亮\n\n冲泡时建议使用95℃的水温，第一泡快速洗茶，第二泡开始正式冲泡，每次浸泡10-15秒即可。',
        authorName: '茶友小明',
        authorAvatar: '',
        createTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
        viewCount: 156,
        commentCount: 12,
        images: []
      },
      2: {
        id: 2,
        title: '新手入门口茶具选择指南',
        content: '很多刚开始喝茶的朋友不知道该如何选择茶具，我总结了一些经验分享给大家。\n\n## 入门级茶具推荐\n\n1. **盖碗**：性价比高，适合冲泡各种茶类\n2. **紫砂壶**：适合冲泡普洱茶和乌龙茶\n3. **玻璃杯**：适合冲泡绿茶和白茶\n\n选择时主要考虑材质、容量和个人的使用习惯。',
        authorName: '茶艺师王老师',
        authorAvatar: '',
        createTime: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
        viewCount: 289,
        commentCount: 24,
        images: []
      }
    };
    const id = parseInt(postId);
    post.value = mockPosts[id] || mockPosts[1];
    comments.value = mockComments;
  } finally {
    loading.value = false;
  }
};

const mockComments = [
  {
    id: 1,
    postId: 1,
    authorName: '茶友甲',
    authorAvatar: '',
    content: '正山小种确实很好喝，我也经常喝！',
    createTime: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
    authorId: 2
  },
  {
    id: 2,
    postId: 1,
    authorName: '茶友乙',
    authorAvatar: '',
    content: '谢谢分享，准备入手一些试试',
    createTime: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
    authorId: 3
  }
];

const fetchComments = async () => {
  try {
    const postId = route.params.id;
    const data = await getPostDetail(postId);
    const commentList = (data.commentDTOS || []).map(item => ({
    id: item.id,
    parentId: item.parentId,
    toUserId: item.toUserId,
    authorId: item.userId,
    authorName: item.username,
    authorAvatar: (item.userImage || '').trim(),
    content: item.content,
    createTime: item.createTime,
    likeCount: item.likeCount,
    likeCancel: item.likeCancel,
    isLiked: item.likeCancel === 1,
    replies: []
  }));
    comments.value = buildCommentTree(commentList);
  } catch (error) {
    console.error('获取评论失败:', error);
  }
};

const handleDeletePost = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await deleteMyPost(post.value.id);
    ElMessage.success('帖子删除成功');
    router.push('/community');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error);
      ElMessage.error('删除失败，请重试');
    }
  }
};

const handleReply = (comment, event) => {
  if (event) {
    event.stopPropagation();
  }
  const parentId = comment.parentId || comment.id;
  replyingTo.value = {
    targetCommentId: parentId,
    commentId: comment.id,
    toUserId: comment.authorId,
    isAuthor: comment.authorId === post.value.authorId,
    username: comment.authorName
  };
  newComment.value = '';
  
  nextTick(() => {
    const replyBox = document.querySelector('.reply-input-box');
    if (replyBox) {
      replyBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  });
};

const cancelReply = () => {
  replyingTo.value = null;
  newComment.value = '';
};

const submitReply = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入回复内容');
    return;
  }

  commentLoading.value = true;
  try {
    const params = {
      postId: post.value.id,
      content: newComment.value.trim(),
      parentId: replyingTo.value.targetCommentId,
      toUserId: replyingTo.value.toUserId
    };
    
    await comment(params);
    ElMessage.success('回复发表成功');
    newComment.value = '';
    replyingTo.value = null;
    await fetchComments();
  } catch (error) {
    console.error('回复失败:', error);
    ElMessage.error('回复失败，请重试');
  } finally {
    commentLoading.value = false;
  }
};

const handleAddComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }

  commentLoading.value = true;
  try {
    await comment({
      postId: post.value.id,
      content: newComment.value.trim(),
      parentId: 0,
      toUserId: 0
    });
    ElMessage.success('评论发表成功');
    newComment.value = '';
    await fetchComments();
  } catch (error) {
    console.error('发表评论失败:', error);
    ElMessage.error('发表评论失败，请重试');
  } finally {
    commentLoading.value = false;
  }
};

const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await deleteComment(commentId);
    ElMessage.success('评论删除成功');
    await fetchComments();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error);
      ElMessage.error('删除失败，请重试');
    }
  }
};

const handleLikeComment = async (comment, event) => {
  if (event) {
    event.stopPropagation();
  }
  
  try {
    if (comment.isLiked) {
      await unlikeComment(comment.id);
      comment.likeCount = (comment.likeCount || 1) - 1;
      comment.isLiked = false;
      ElMessage.success('已取消点赞');
    } else {
      await likeComment(comment.id);
      comment.likeCount = (comment.likeCount || 0) + 1;
      comment.isLiked = true;
      ElMessage.success('点赞成功');
    }
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

const handleLike = async (postData) => {
  try {
    if (postData.isLiked) {
      await unlikePost(postData.id);
      postData.likeCount = (postData.likeCount || 1) - 1;
      postData.isLiked = false;
      ElMessage.success('已取消点赞');
    } else {
      await likePost(postData.id);
      postData.likeCount = (postData.likeCount || 0) + 1;
      postData.isLiked = true;
      ElMessage.success('点赞成功');
    }
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

const handleFavorite = async (postData) => {
  try {
    if (postData.isFavorited) {
      await unfavoritePost(postData.id);
      postData.collectCount = (postData.collectCount || 1) - 1;
      postData.isFavorited = false;
      ElMessage.success('已取消收藏');
    } else {
      await favoritePost(postData.id);
      postData.collectCount = (postData.collectCount || 0) + 1;
      postData.isFavorited = true;
      ElMessage.success('收藏成功');
    }
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

const goBack = () => {
  router.push('/community');
};

onMounted(() => {
  fetchPostDetail();
});
</script>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4f3 100%);
  padding: 80px 20px 40px;
}

.detail-container {
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
  font-size: 28px;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.post-content {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.post-main {
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #eee;
}

.post-author-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
}

.author-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e0f2f1;
}

.author-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.post-time {
  color: #999;
  font-size: 14px;
}

.post-actions {
  margin-left: auto;
}

.post-title {
  font-size: 28px;
  color: #2c3e50;
  margin: 0 0 25px;
  font-weight: 700;
  line-height: 1.4;
}

.post-body {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  word-wrap: break-word;
}

.post-body :deep(h1),
.post-body :deep(h2),
.post-body :deep(h3) {
  margin: 20px 0 15px;
  color: #2c3e50;
}

.post-body :deep(p) {
  margin: 15px 0;
}

.post-body :deep(li) {
  margin: 8px 0;
  padding-left: 10px;
}

.post-body :deep(code) {
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
  color: #008080;
}

.post-body :deep(blockquote) {
  border-left: 4px solid #008080;
  padding-left: 15px;
  margin: 15px 0;
  color: #666;
  background: #f9f9f9;
  padding: 10px 15px;
}

.post-body :deep(a) {
  color: #008080;
  text-decoration: none;
}

.post-body :deep(a:hover) {
  text-decoration: underline;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 25px;
}

.post-image {
  width: 100%;
  height: 200px;
  border-radius: 12px;
  cursor: pointer;
}

.image-loading,
.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  border-radius: 12px;
}

.loading-icon {
  font-size: 24px;
  color: #008080;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.image-error .el-icon {
  font-size: 40px;
  color: #c0c4cc;
}

.post-stats {
  display: flex;
  gap: 25px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #888;
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
  font-size: 18px;
  transition: all 0.3s ease;
}

.like-item {
  color: #888;
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
  font-size: 20px;
  color: #888;
  transition: all 0.3s ease;
}

.like-item:hover .el-icon {
  color: #ff6b6b;
}

.like-item.active .el-icon {
  color: #ff6b6b;
}

.favorite-item {
  color: #888;
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
  font-size: 20px;
  color: #888;
  transition: all 0.3s ease;
}

.favorite-item:hover .el-icon {
  color: #ffb800;
}

.favorite-item.active .el-icon {
  color: #ffb800;
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

.comments-section {
  margin-top: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  color: #2c3e50;
  margin: 0 0 25px;
  font-weight: 600;
}

.section-title .el-icon {
  color: #008080;
}

.comment-form {
  margin-bottom: 30px;
}

.comment-form :deep(.el-textarea__inner) {
  border-radius: 12px;
}

.comment-submit-btn {
  margin-top: 15px;
  background: linear-gradient(135deg, #008080 0%, #2a9d8f 100%);
  border: none;
  border-radius: 20px;
  padding: 10px 30px;
}

.no-comments {
  padding: 40px 0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.comment-item.is-reply {
  background: #fff;
  border-left: 3px solid #008080;
  margin-left: 20px;
  padding: 15px;
  border-radius: 8px;
}

.comment-item.is-reply:hover {
  background: #f5f5f5;
}

.reply-avatar {
  width: 35px;
  height: 35px;
}

.reply-list {
  margin-top: 10px;
}

.reply-input-box {
  margin-left: 60px;
  margin-top: 10px;
  padding: 15px;
  background: #f0f7f7;
  border-radius: 8px;
  border: 1px solid #e0f2f1;
}

.reply-input-box .reply-tip {
  margin-bottom: 10px;
  color: #008080;
  font-size: 14px;
}

.reply-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.reply-to {
  color: #409eff;
  font-size: 12px;
}

.comment-avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e0f2f1;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.comment-author {
  font-weight: 600;
  color: #333;
}

.comment-time {
  color: #999;
  font-size: 13px;
}

.comment-content {
  color: #444;
  line-height: 1.6;
  font-size: 15px;
}

.comment-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}

.comment-footer {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-actions-right {
  display: flex;
  align-items: center;
}

.comment-footer .el-button.active {
  color: #ff6b6b;
}

.comment-item {
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.empty-state {
  padding: 60px 0;
}

@media (max-width: 768px) {
  .post-content {
    padding: 20px;
  }

  .post-title {
    font-size: 22px;
  }

  .post-author-section {
    flex-wrap: wrap;
  }

  .post-images {
    grid-template-columns: repeat(2, 1fr);
  }

  .post-image {
    height: 150px;
  }

  .comment-item {
    padding: 15px;
  }
}
</style>
