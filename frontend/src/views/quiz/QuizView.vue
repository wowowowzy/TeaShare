<template>
  <Navbar />
  <!-- 导航栏组件 -->
  <div class="quiz">
    <!-- 智能对话模块 -->
    <div class="chat-container">
      <div class="chat-header">
        <div class="ai-info">
          <img src="/assest/number1.png" alt="AI头像" class="ai-avatar">
          <div class="ai-text">
            <h2 class="chat-title">小茗</h2>
            <p class="ai-subtitle">您的茶文化智能顾问</p>
          </div>
        </div>
        <div v-if="userStore.token" class="action-buttons">
          <button @click="createNewChat" class="new-chat-header-btn" :disabled="creatingSession">
            <span v-if="creatingSession" class="loading-spinner-small"></span>
            <span v-else class="new-chat-icon-header"></span>
            {{ creatingSession ? '创建中' : '新建对话' }}
          </button>
          <button @click="showHistory" class="history-btn">
            历史查询
          </button>
        </div>
      </div>

      <div class="chat-messages" ref="chatMessages">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-content">
            <img src="/assest/number1.png" alt="AI头像" class="welcome-avatar">
            <div class="welcome-text">
              <p>您好！我是您的茶文化智能顾问，可解答关于茶叶历史、品类、冲泡、贸易等相关问题。</p>
              <p class="examples">例如："唐代茶文化特点"、"六大茶类的区别"、"明清茶叶贸易史"等。</p>
            </div>
          </div>
        </div>

        <!-- 聊天消息 -->
        <div v-for="(message, index) in messages" :key="index" class="message-row" :class="message.type">
          <img v-if="message.type === 'system-message'" src="/assest/number1.png"
            alt="AI头像" class="message-avatar ai-avatar-img">
          <img v-else :src="userAvatar || '/assest/default.jpg'" alt="用户头像" class="message-avatar user-avatar-img"
            @error="handleUserAvatarError">
          <div class="message-content-wrapper">
            <div class="message-content">
              <div v-html="formatMessageContent(message.content)"></div>
              <div v-if="message.image" class="message-image">
                <img :src="message.image" alt="图片" class="user-uploaded-image" @error="handleImageError" />
              </div>
            </div>
          </div>
        </div>

        <!-- 加载中消息 -->
        <div v-if="loading && !hasStreamingMessage" class="message-row system-message">
          <img src="/assest/number1.png" alt="AI头像"
            class="message-avatar ai-avatar-img">
          <div class="message-content-wrapper">
            <div class="message-content">
              <div class="thinking">
                <span class="thinking-text">正在思考...</span>
                <span class="thinking-dot"></span>
                <span class="thinking-dot"></span>
                <span class="thinking-dot"></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 未登录提示 -->
      <div v-if="!userStore.token" class="login-prompt">
        <div class="prompt-content">
          <h3>请先登录</h3>
          <p>智能问答功能需要登录后才能使用</p>
          <router-link to="/login" class="login-button">去登录</router-link>
        </div>
      </div>

      <!-- 消息输入框 -->
      <div v-else class="chat-input-container">
        <!-- 图片上传区域 -->
        <div class="image-upload-section">
          <div class="upload-options">
            <el-upload class="image-upload" :auto-upload="false" :show-file-list="false" :on-change="handleImageUpload"
              accept="image/jpeg,image/png,image/webp,image/gif">
              <el-button type="primary" size="small" class="upload-button">
                <el-icon>
                  <Plus />
                </el-icon>
                上传图片
              </el-button>
            </el-upload>
            <div class="image-url-input">
            </div>
          </div>
          <div v-if="selectedImage" class="image-preview">
            <img :src="selectedImage.preview" alt="预览" class="preview-image" />
            <el-button type="danger" size="small" class="remove-button" @click="removeImage">
              移除
            </el-button>
          </div>
          <div class="upload-tip">支持上传 JPG、PNG、WebP、GIF 格式图片，大小不超过 5MB，或直接输入图片URL</div>
        </div>
        <div class="input-wrapper">
          <textarea v-model="inputMessage" placeholder="请输入您想咨询的茶文化问题..." class="chat-input"
            @keydown.enter.prevent="sendMessage" rows="1" ref="inputRef"></textarea>
          <button class="send-button" @click="sendMessage"
            :disabled="!inputMessage.trim() && !selectedImage && !imageUrl.trim()">
            <span class="send-icon">➤</span>
          </button>
        </div>
      </div>

      <!-- 历史记录弹窗 -->
      <div v-if="showHistoryDialog" class="history-dialog">
        <div class="history-dialog-content">
          <div class="history-dialog-header">
            <h3>历史查询记录</h3>
            <button @click="showHistoryDialog = false" class="close-button">×</button>
          </div>
          <div class="history-dialog-body">
            <div v-if="loadingHistory" class="loading-history">
              <div class="loading-spinner"></div>
              <div class="loading-text">加载历史记录中...</div>
            </div>
            <div v-else-if="histories.length === 0" class="no-history">
              <div class="no-history-icon">📝</div>
              <div class="no-history-text">暂无历史记录</div>
              <div class="no-history-subtext">开始与小茗对话，历史记录将在这里显示</div>
            </div>
            <div v-else class="history-list">
              <div v-for="(history, index) in histories" :key="history.sessionId" class="history-item"
                @click="goToHistory(history.sessionId, history.messages)">
                <div class="history-header">
                  <div class="history-title">{{ history.title }}</div>
                </div>
                <div class="history-meta">
                  <div class="history-message-count">{{ history.messageCount }} 条消息</div>
                  <div class="history-time">{{ history.time }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue';
import Navbar from '../../components/Navbar.vue';
import { useUserStore } from '../../stores/user';
import { generateStream, getHistories, createNewSession } from '../../api/ai';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

const userStore = useUserStore();
const router = useRouter();

// 获取用户头像
const userAvatar = computed(() => {
  return userStore.userInfo?.avatar || '';
});

// 智能对话相关状态
const messages = ref([]);
const inputMessage = ref('');
const loading = ref(false);
const chatMessages = ref(null);

// 计算属性：检查是否存在正在流式传输的消息
const hasStreamingMessage = computed(() => {
  return messages.value.some(message => message.type === 'system-message' && message.isStreaming);
});

// 图片上传相关状态
const selectedImage = ref(null);
const imageUrl = ref('');

// 历史记录相关状态
const showHistoryDialog = ref(false);
const histories = ref([]);
const loadingHistory = ref(false);
const creatingSession = ref(false);

// 发送消息
async function sendMessage() {
  // 允许只发送图片或图片URL
  if (!inputMessage.value.trim() && !selectedImage.value && !imageUrl.value.trim()) return;

  // 检查是否登录
  if (!userStore.token) {
    router.push('/login');
    return;
  }

  // 添加用户消息
  const userMessage = {
    type: 'user-message',
    content: inputMessage.value
  };

  // 如果有图片，添加图片信息
  if (selectedImage.value) {
    userMessage.image = selectedImage.value.preview;
  } else if (imageUrl.value.trim()) {
    // 如果有图片URL，添加图片信息
    userMessage.image = imageUrl.value.trim();
  }

  messages.value.push(userMessage);

  // 清空输入框和图片
  const message = inputMessage.value;
  inputMessage.value = '';

  // 保存图片文件引用或URL（用于API调用）
  let imageData = null;
  let previewUrl = '';

  // 检测并过滤AI头像图片
  const aiAvatarUrl = '/assest/number1.png';

  if (selectedImage.value) {
    // 检查选中的图片是否为AI头像
    const preview = selectedImage.value.preview;
    if (preview !== aiAvatarUrl) {
      imageData = selectedImage.value.file;
      previewUrl = preview;
    } else {
      ElMessage.warning('检测到AI头像图片，已自动过滤');
    }
  } else if (imageUrl.value.trim()) {
    // 检查图片URL是否为AI头像
    const url = imageUrl.value.trim();
    if (url !== aiAvatarUrl && !url.includes('bottts') && !url.includes('seed=tea-ai') && !url.includes('number1.png')) {
      imageData = url;
      previewUrl = url;
    } else {
      ElMessage.warning('检测到AI头像图片，已自动过滤');
    }
  }

  // 清除选中的图片和图片URL
  if (selectedImage.value) {
    selectedImage.value = null;
    // 不要立即释放URL，因为用户消息中还在使用
    // URL.revokeObjectURL(previewUrl);
  }
  if (imageUrl.value.trim()) {
    imageUrl.value = '';
  }

  // 滚动到底部
  await nextTick();
  scrollToBottom();

  // 显示加载状态
  loading.value = true;

  // 检查sessionId是否存在
  let sessionId = localStorage.getItem('sessionId') || userStore.sessionId;

  // 如果没有sessionId，先创建新会话
  if (!sessionId) {
    console.log('暂无sessionId，正在创建新会话...');
    try {
      const sessionResponse = await createNewSession();
      // 后端 newSession 返回 Result.success(snowflakeId)，经响应拦截器后
      // sessionResponse 就是裸的 sessionId（数字/字符串），而不是 { sessionId } 对象。
      // 这里做兼容处理：对象取 .sessionId，否则直接用返回值本身。
      const newSessionId = (sessionResponse && typeof sessionResponse === 'object')
        ? sessionResponse.sessionId
        : sessionResponse;
      if (newSessionId) {
        sessionId = String(newSessionId);
        userStore.sessionId = sessionId;
        localStorage.setItem('sessionId', sessionId);
        console.log('自动创建会话成功，sessionId:', sessionId);
      } else {
        throw new Error('未能获取到sessionId');
      }
    } catch (sessionError) {
      console.error('创建会话失败:', sessionError);
      loading.value = false;
      messages.value.push({
        type: 'system-message',
        content: `抱歉，无法创建会话：${sessionError.message}`
      });
      return;
    }
  }

  try {
    // 调用API获取回复（流式）

    await generateStream(
      message,
      sessionId,
      imageData,
      (chunk) => {
        // 处理接收到的流式数据（实时）

        // 检查是否已经有系统消息
        let systemMessage = messages.value.find(msg => msg.type === 'system-message' && msg.isStreaming);

        // 如果没有系统消息，创建一个新的
        if (!systemMessage) {
          systemMessage = {
            type: 'system-message',
            content: '',
            // 添加一个标记，用于跟踪是否是流式消息
            isStreaming: true
          };
          messages.value.push(systemMessage);
        }

        // 立即更新系统消息内容
        systemMessage.content += chunk;

        // 强制Vue响应式系统更新
        // 使用Vue的响应式API确保数据变化被捕获
        messages.value = [...messages.value];

        // 立即触发UI更新并滚动到底部
        nextTick(() => {
          scrollToBottom();
        });
      },
      () => {
        // 流式响应完成
        // 找到系统消息并标记为已完成
        const systemMessage = messages.value.find(msg => msg.type === 'system-message' && msg.isStreaming);
        if (systemMessage) {
          systemMessage.isStreaming = false;
          // 强制Vue响应式系统更新
          messages.value = [...messages.value];
        }
      },
      (error) => {
        // 流式响应错误
        console.error('流式响应错误:', error);
      }
    );

  } catch (error) {
    console.error('API调用失败:', error);

    // 检查是否是sessionId无效的错误
    const errorMsg = error.message || '';
    if (errorMsg.includes('未授权') || errorMsg.includes('Unauthorized') || errorMsg.includes('session')) {
      // sessionId无效，清除并提示用户重新创建
      console.log('sessionId无效，正在重新创建...');
      localStorage.removeItem('sessionId');
      userStore.sessionId = '';

      // 替换最后一条消息为错误消息
      if (messages.value.length > 0 && messages.value[messages.value.length - 1].type === 'system-message') {
        messages.value[messages.value.length - 1].content = '会话已过期，请重新创建对话';
        messages.value[messages.value.length - 1].isStreaming = false;
      } else {
        messages.value.push({
          type: 'system-message',
          content: '会话已过期，请重新创建对话',
          isStreaming: false
        });
      }
    } else {
      // 其他错误
      // 替换最后一条消息为错误消息
      if (messages.value.length > 0 && messages.value[messages.value.length - 1].type === 'system-message') {
        messages.value[messages.value.length - 1].content = `抱歉，服务器暂时无法响应：${error.message}`;
        messages.value[messages.value.length - 1].isStreaming = false;
      } else {
        messages.value.push({
          type: 'system-message',
          content: `抱歉，服务器暂时无法响应：${error.message}`,
          isStreaming: false
        });
      }
    }
  } finally {
    // 隐藏加载状态
    loading.value = false;
    // 滚动到底部
    await nextTick();
    scrollToBottom();
  }
}

// 清除图片URL输入
function clearImageUrl() {
  imageUrl.value = '';
}

// 处理图片上传
function handleImageUpload(file) {
  const uploadedFile = file.raw || file;
  const isImage = uploadedFile.type.startsWith('image/');
  const isLt5M = uploadedFile.size / 1024 / 1024 < 5;

  if (!isImage) {
    ElMessage.error('只能上传图片文件！');
    return;
  }

  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！');
    return;
  }

  // 检测是否为AI头像图片
  const aiAvatarUrl = '/assest/number1.png';

  // 创建图片对象进行检测
  const img = new Image();
  img.onload = function () {
    // 生成预览URL
    const preview = URL.createObjectURL(uploadedFile);

    // 检测预览URL是否与AI头像URL相同
    if (preview === aiAvatarUrl) {
      ElMessage.warning('检测到AI头像图片，已自动过滤');
      return;
    }

    // 检查图片内容是否为AI头像（通过绘制到 canvas 并比较）
    const canvas = document.createElement('canvas');
    canvas.width = img.width;
    canvas.height = img.height;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(img, 0, 0);

    // 简单检测：如果是SVG格式且包含AI头像特征
    if (uploadedFile.type === 'image/svg+xml') {
      const reader = new FileReader();
      reader.onload = function (e) {
        const content = e.target.result;
        if (content.includes('bottts') && content.includes('seed=tea-ai') || content.includes('number1')) {
          ElMessage.warning('检测到AI头像图片，已自动过滤');
          return;
        }

        // 不是AI头像，继续上传流程
        selectedImage.value = {
          file: uploadedFile,
          preview
        };
        ElMessage.success('图片已选择');
      };
      reader.readAsText(uploadedFile);
    } else {
      // 不是SVG，直接处理
      selectedImage.value = {
        file: uploadedFile,
        preview
      };
      ElMessage.success('图片已选择');
    }
  };

  img.onerror = function () {
    // 图片加载失败，正常处理
    const preview = URL.createObjectURL(uploadedFile);
    selectedImage.value = {
      file: uploadedFile,
      preview
    };
    ElMessage.success('图片已选择');
  };

  // 加载图片进行检测
  img.src = URL.createObjectURL(uploadedFile);
}

// 移除选中的图片
function removeImage() {
  if (selectedImage.value) {
    URL.revokeObjectURL(selectedImage.value.preview);
    selectedImage.value = null;
    ElMessage.success('图片已移除');
  }
}

// 处理图片加载错误
function handleImageError(event) {
  const img = event.target;
  img.src = 'https://api.dicebear.com/7.x/bottts/svg?seed=error';
  img.alt = '图片加载失败';
  img.style.opacity = '0.6';
}

// 处理用户头像加载失败
function handleUserAvatarError(event) {
  const img = event.target;
  img.src = '/assest/default.jpg';
  img.alt = '用户头像';
  console.log('用户头像加载失败，使用默认头像');
}

// 格式化消息内容（优化版本）
function formatMessageContent(content) {
  if (!content) return '';

  // 转义HTML特殊字符，防止XSS攻击
  let formatted = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');

  // 处理粗体文本 (**text**)
  formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

  // 处理数字列表的换行
  // 匹配数字+点+空格的模式，如"1. "、"2. "等
  // 确保在数字前添加换行
  formatted = formatted.replace(/(\d+\.)/g, '<br>$1');

  // 处理换行符
  formatted = formatted.replace(/\n/g, '<br>');

  // 处理列表格式
  const lines = formatted.split('<br>');
  let inList = false;
  let result = '';

  // 优化循环处理
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const trimmedLine = line.trim();

    if (trimmedLine.startsWith('- ')) {
      if (!inList) {
        result += '<ul>';
        inList = true;
      }
      result += `<li>${trimmedLine.substring(2)}</li>`;
    } else {
      if (inList) {
        result += '</ul>';
        inList = false;
      }
      if (trimmedLine) {
        result += `<p>${line}</p>`;
      } else {
        result += '<br>';
      }
    }
  }

  if (inList) {
    result += '</ul>';
  }

  return result;
}

// 滚动到底部（优化版本）
function scrollToBottom() {
  if (chatMessages.value) {
    // 使用requestAnimationFrame优化滚动性能
    requestAnimationFrame(() => {
      chatMessages.value.scrollTop = chatMessages.value.scrollHeight;
    });
  }
}

// 显示历史记录（优化版本）
async function showHistory() {
  showHistoryDialog.value = true;
  loadingHistory.value = true;

  try {
    // 检查用户登录状态
    if (!userStore.token) {
      histories.value = [];
      ElMessage.warning('请先登录后再查看历史记录');
      return;
    }

    // 从store获取用户id，优先使用登录接口返回的userId
    const userId = userStore.userInfo?.id || userStore.userInfo?.userId;

    if (!userId) {
      histories.value = [];
      ElMessage.warning('请先登录后再查看历史记录');
      return;
    }

    // 使用API函数获取历史记录（需要传userId参数）
    const response = await getHistories(userId);

    // 处理响应数据，按sessionId分组
    if (response && typeof response === 'object') {
      const groupedHistories = [];

      // 检查response是否为空对象
      const responseKeys = Object.keys(response);

      if (responseKeys.length > 0) {
        // 遍历data对象，每个key是一个sessionId
        for (const sessionId of responseKeys) {
          const sessionMessages = response[sessionId];

          if (Array.isArray(sessionMessages) && sessionMessages.length > 0) {
            // 找到用户的第一条非空消息（role为'user'且content不为空）
            const firstUserMessage = sessionMessages.find(msg => msg.role === 'user' && msg.content && msg.content.trim() !== '');

            // 取第一条消息的时间作为对话时间
            const firstMessage = sessionMessages[0];

            if (firstMessage) {
              // 优化字符串处理
              let title = '无标题对话';
              if (firstUserMessage && firstUserMessage.content) {
                title = firstUserMessage.content.length > 50
                  ? firstUserMessage.content.substring(0, 50) + '...'
                  : firstUserMessage.content;
              } else {
                // 找到第一条非空消息作为标题
                const firstNonEmptyMessage = sessionMessages.find(msg => msg.content && msg.content.trim() !== '');
                if (firstNonEmptyMessage && firstNonEmptyMessage.content) {
                  title = firstNonEmptyMessage.content.length > 50
                    ? firstNonEmptyMessage.content.substring(0, 50) + '...'
                    : firstNonEmptyMessage.content;
                }
              }

              groupedHistories.push({
                sessionId,
                title,
                time: firstMessage.datetime,
                messages: sessionMessages,
                messageCount: sessionMessages.length
              });
            }
          }
        }

        if (groupedHistories.length > 0) {
          // 按时间倒序排列
          histories.value = groupedHistories.sort((a, b) => {
            return new Date(b.time) - new Date(a.time);
          });
        } else {
          // 没有有效的历史记录
          histories.value = [];
          ElMessage.warning('暂无历史记录');
        }
      } else {
        // 响应为空对象，设置为空数组
        histories.value = [];
        ElMessage.warning('暂无历史记录');
      }
    } else {
      histories.value = [];
      ElMessage.warning('暂无历史记录');
    }
  } catch (error) {
    console.error('获取历史记录失败:', error);
    histories.value = [];
    ElMessage.error('获取历史记录失败，请稍后重试');
  } finally {
    loadingHistory.value = false;
  }
}

// 跳转到历史对话（优化版本）
function goToHistory(sessionId, historyMessages) {
  if (!sessionId || !historyMessages) {
    console.error('历史记录数据不完整');
    ElMessage.error('历史记录数据不完整');
    return;
  }

  showHistoryDialog.value = false;

  // 显示加载状态
  loading.value = true;

  try {
    // 清空当前消息
    messages.value = [];

    // 优化消息转换，减少不必要的计算
    const convertedMessages = [];
    for (let i = 0; i < historyMessages.length; i++) {
      const msg = historyMessages[i];
      const convertedMessage = {
        type: msg.role === 'user' ? 'user-message' : 'system-message',
        content: msg.content
      };

      // 检查是否有图片信息
      if (msg.image) {
        convertedMessage.image = msg.image;
      } else if (msg.imageUrl) {
        // 兼容其他可能的图片字段名
        convertedMessage.image = msg.imageUrl;
      }

      convertedMessages.push(convertedMessage);
    }

    // 批量更新消息
    messages.value = convertedMessages;

    // 更新sessionId
    userStore.sessionId = sessionId;
    localStorage.setItem('sessionId', sessionId);

    // 滚动到底部
    nextTick(() => {
      scrollToBottom();
      loading.value = false;
    });
  } catch (error) {
    console.error('加载历史对话失败:', error);
    ElMessage.error('加载历史对话失败');
    loading.value = false;
  }
}

// 新建对话（优化版本）
async function createNewChat() {
  if (creatingSession.value) return;

  creatingSession.value = true;

  try {
    const response = await createNewSession();

    // 后端返回格式: { code: 1, msg: "success", data: 2035330286363082752 }
    // request.js拦截器已处理code=1的情况，返回data部分
    // 所以response就是sessionId
    let newSessionId = response;

    if (newSessionId) {
      newSessionId = String(newSessionId);
      userStore.sessionId = newSessionId;
      localStorage.setItem('sessionId', newSessionId);

      // 清空消息
      messages.value = [];
    } else {
      console.error('响应中没有sessionId字段');
    }
  } catch (error) {
    console.error('创建新会话失败:', error);
    messages.value.push({
      type: 'system-message',
      content: `抱歉，无法创建新会话：${error.message}`
    });
  } finally {
    creatingSession.value = false;
  }
}

</script>

<style scoped>
.quiz {
  min-height: 100vh;
  padding-top: 80px;
  background-color: #f0f8f5;
  font-family: "楷体", "KaiTi", "STKaiti", "楷_GB2312", "Microsoft Yahei", serif;
  color: #2c4a40;
  display: flex;
  flex-direction: column;
}

/* 智能对话模块 */
.chat-container {
  background-color: #f8faf9;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  padding: 30px;
  margin: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e8f4f0;
  max-width: 1200px;
  align-self: center;
  width: 100%;
}

.chat-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e8f4f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ai-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.history-button {
  margin-left: auto;
}

.action-buttons {
  margin-left: auto;
  display: flex;
  gap: 12px;
}

.new-chat-header-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: #4ba88f;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  /* font-family: "楷体", "KaiTi", "STKaiti", "楷_GB2312", "Microsoft Yahei", serif; */
}

.new-chat-header-btn:hover:not(:disabled) {
  background-color: #3a9078;
}

.new-chat-header-btn:active:not(:disabled) {
  transform: translateY(0);
}

.new-chat-header-btn:disabled {
  background-color: #a8d4c6;
  cursor: not-allowed;
}

.new-chat-icon-header {
  font-size: 14px;
  font-weight: bold;
}

.loading-spinner-small {
  width: 12px;
  height: 12px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.history-btn {
  background-color: #4ba88f;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.history-btn:hover {
  background-color: #3a9078;
}

/* 未登录提示 */
.login-prompt {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fafafa;
  border-radius: 8px;
  padding: 40px;
}

.prompt-content {
  text-align: center;
  background-color: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.prompt-content h3 {
  color: #2c4a40;
  margin-bottom: 10px;
}

.prompt-content p {
  color: #666;
  margin-bottom: 20px;
}

.login-button {
  display: inline-block;
  background-color: #4ba88f;
  color: white;
  padding: 10px 20px;
  border-radius: 20px;
  text-decoration: none;
  transition: background-color 0.3s ease;
}

.login-button:hover {
  background-color: #3a9078;
}

/* 历史记录弹窗 */
.history-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.history-dialog-content {
  background-color: white;
  border-radius: 16px;
  width: 90%;
  max-width: 800px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.history-dialog-header {
  padding: 20px;
  border-bottom: 1px solid #e8f4f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: #4ba88f;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: "楷体", "KaiTi", "STKaiti", "楷_GB2312", "Microsoft Yahei", serif;
}

.new-chat-btn:hover:not(:disabled) {
  background-color: #3a9078;
  transform: translateY(-1px);
}

.new-chat-btn:disabled {
  background-color: #a8d4c6;
  cursor: not-allowed;
}

.new-chat-icon {
  font-size: 16px;
  font-weight: bold;
}

.loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.history-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.history-dialog-content {
  background-color: white;
  border-radius: 16px;
  width: 90%;
  max-width: 700px;
  max-height: 85vh;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: dialogFadeIn 0.3s ease-out;
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }

  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.history-dialog-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fafafa;
}

.history-dialog-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c4a40;
}

.close-button {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.close-button:hover {
  background-color: #f5f5f5;
  color: #666;
}

.history-dialog-body {
  padding: 24px;
  max-height: calc(85vh - 80px);
  overflow-y: auto;
}

.loading-history {
  text-align: center;
  padding: 60px 0;
  color: #666;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #4ba88f;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 16px;
  color: #666;
}

.no-history {
  text-align: center;
  padding: 60px 0;
  color: #999;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.no-history-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.no-history-text {
  font-size: 18px;
  font-weight: 500;
  color: #666;
}

.no-history-subtext {
  font-size: 14px;
  color: #999;
  max-width: 80%;
  line-height: 1.5;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
  position: relative;
  overflow: hidden;
}

.history-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background-color: #4ba88f;
  transform: scaleY(0);
  transition: transform 0.3s;
}

.history-item:hover {
  border-color: #4ba88f;
  box-shadow: 0 4px 16px rgba(75, 168, 143, 0.15);
  transform: translateY(-2px);
}

.history-item:hover::before {
  transform: scaleY(1);
}

.history-header {
  margin-bottom: 12px;
}

.history-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}

.history-message-count {
  background-color: #e8f4f0;
  color: #4ba88f;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.history-time {
  color: #999;
  font-size: 12px;
}

.ai-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #e8f4f0;
}

.ai-text {
  display: flex;
  flex-direction: column;
}

.chat-title {
  font-size: 20px;
  color: #2c4a40;
  margin: 0;
}

.ai-subtitle {
  font-size: 14px;
  color: #666;
  margin: 5px 0 0 0;
}

.welcome-message {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 80%;
}

.welcome-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e8f4f0;
  flex-shrink: 0;
}

.welcome-text {
  background-color: white;
  padding: 15px 20px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  font-size: 20px;
  line-height: 1.6;
  color: #2c4a40;
}

.examples {
  margin-top: 10px;
  font-style: italic;
  color: #666;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-height: 454px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 85%;
}

.message-row.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-row.system-message {
  align-self: flex-start;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  background-color: #e8f4f0;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
}

.message-content {
  background-color: white;
  padding: 15px 20px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  font-size: 20px;
  line-height: 1.6;
  color: #2c4a40;
  word-wrap: break-word;
}

.message-content p {
  margin: 0 0 12px 0;
}

.message-content p:last-child {
  margin-bottom: 0;
}

.message-content ul,
.message-content ol {
  margin: 12px 0;
  padding-left: 24px;
}

.message-content li {
  margin: 6px 0;
}

.message-content strong,
.message-content b {
  font-weight: 600;
  color: #4ba88f;
}

.user-message .message-content {
  background-color: #4ba88f;
  color: white;
  border-top-left-radius: 12px;
  border-top-right-radius: 4px;
  /* 新增强制换行属性 */
  word-wrap: break-word;
  /* 兼容旧浏览器 */
  word-break: break-word;
  /* 换行规则：截断长单词/URL */
  white-space: pre-wrap;
  /* 保留换行符，同时自动换行 */
  /* 限制最大宽度（可根据需求调整） */
}

.user-message .message-content strong,
.user-message .message-content b {
  color: #fff;
  font-weight: 600;
}

.system-message .message-content {
  background-color: white;
  color: #2c4a40;
  border-top-left-radius: 4px;
  border-top-right-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 思考中动画 */
.thinking {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 25px;
  background-color: #f8faf9;
  border-radius: 16px;
  border: 1px solid #e8f4f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.thinking-text {
  font-size: 20px;
  color: #4ba88f;
  font-weight: 500;
}

.thinking-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #4ba88f;
  animation: thinking 1.2s infinite ease-in-out;
}

.thinking-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-dot:nth-child(3) {
  animation-delay: 0.4s;
}

.thinking-dot:nth-child(4) {
  animation-delay: 0.6s;
}

@keyframes thinking {

  0%,
  100% {
    transform: scale(0.8);
    opacity: 0.5;
  }

  50% {
    transform: scale(1.4);
    opacity: 1;
  }
}

/* 聊天输入框 */
.chat-input-container {
  padding: 15px;
  background-color: white;
  border-radius: 8px;
  border: 1px solid #e8f4f0;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  background-color: #f5f5f5;
  border-radius: 20px;
  padding: 8px 8px 8px 16px;
}

.chat-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 20px;
  line-height: 1.5;
  background: transparent;
  font-family: "楷体", "KaiTi", "STKaiti", "楷_GB2312", "Microsoft Yahei", serif;
  resize: none;
  min-height: 20px;
  max-height: 120px;
  padding: 6px 0;
}

.chat-input::placeholder {
  color: #999;
}

.send-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background-color: #4ba88f;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
  flex-shrink: 0;
}

.send-button:hover:not(:disabled) {
  background-color: #3a9078;
}

.send-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.send-icon {
  font-size: 14px;
}

/* 图片上传区域 */
.image-upload-section {
  margin-bottom: 15px;
}

.upload-options {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.image-upload {
  flex-shrink: 0;
}

.image-url-input {
  flex: 1;
}

.image-url-input .el-input {
  width: 100%;
}

.upload-button {
  background-color: #4ba88f;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.upload-button:hover {
  background-color: #3a9078;
}

.image-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.preview-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e8f4f0;
}

.remove-button {
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 12px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

/* 消息中的图片 */
.message-image {
  margin-top: 10px;
}

.user-uploaded-image {
  max-width: 300px;
  max-height: 200px;
  object-fit: contain;
  border-radius: 4px;
  border: 1px solid #e8f4f0;
  display: block;
  margin-top: 10px;
}

/* 打字机效果 */
.typewriter {
  display: inline-block;
  line-height: 1.6;
}

.typewriter-char {
  opacity: 0;
  animation: typewriter-fade-in 0.3s ease forwards;
  display: inline-block;
}

@keyframes typewriter-fade-in {
  from {
    opacity: 0;
    transform: translateY(5px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    padding: 20px;
    margin: 10px;
  }

  .chat-header {
    margin-bottom: 15px;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .action-buttons {
    margin-left: 0;
    width: 100%;
    justify-content: space-between;
  }

  .chat-title {
    font-size: 18px;
  }

  .ai-subtitle {
    font-size: 12px;
  }

  .welcome-message {
    font-size: 14px;
  }

  .chat-messages {
    padding: 15px;
    gap: 10px;
  }

  .message-row {
    max-width: 90%;
  }

  .message-content {
    font-size: 14px;
    padding: 12px 16px;
  }

  .send-button {
    width: 32px;
    height: 32px;
  }

  .action-buttons {
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 8px;
  }

  .new-chat-header-btn,
  .history-btn {
    font-size: 12px;
    padding: 6px 12px;
  }

  /* 图片上传区域响应式 */
  .upload-options {
    flex-direction: column;
    align-items: stretch;
  }

  .image-upload {
    width: 100%;
  }

  .upload-button {
    width: 100%;
    justify-content: center;
  }

  /* 弹窗响应式布局 */
  .history-dialog-content {
    max-width: 95%;
    max-height: 90vh;
  }
}

@media (max-width: 480px) {
  .chat-container {
    padding: 15px;
    margin: 5px;
  }

  .message-content {
    font-size: 13px;
    padding: 10px 14px;
  }

  .chat-title {
    font-size: 16px;
  }

  .welcome-text {
    font-size: 14px;
    padding: 12px 16px;
  }
}
</style>