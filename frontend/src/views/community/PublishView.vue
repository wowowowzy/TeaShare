<template>
  <div class="publish-page">
    <div class="publish-container">
      <div class="page-header">
        <el-button class="back-btn" @click="goBack" :icon="ArrowLeft" circle />
        <h1 class="page-title">发布新帖</h1>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="publish-form"
      >
        <el-form-item label="帖子标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入帖子标题"
            maxlength="100"
            show-word-limit
            size="large"
          />
        </el-form-item>

        <el-form-item label="帖子内容" prop="content">
          <div class="editor-toolbar">
            <el-button-group>
              <el-button :type="editorMode === 'simple' ? 'primary' : 'default'" @click="editorMode = 'simple'" size="small">
                简洁模式
              </el-button>
              <el-button :type="editorMode === 'rich' ? 'primary' : 'default'" @click="editorMode = 'rich'" size="small">
                富文本模式
              </el-button>
            </el-button-group>
          </div>

          <div v-if="editorMode === 'rich'" class="rich-editor">
            <div class="format-toolbar">
              <el-button text @click="insertFormat('**', '**')" title="粗体">
                <strong>B</strong>
              </el-button>
              <el-button text @click="insertFormat('*', '*')" title="斜体">
                <em>I</em>
              </el-button>
              <el-button text @click="insertFormat('~~', '~~')" title="删除线">
                <s>S</s>
              </el-button>
              <el-divider direction="vertical" />
              <el-button text @click="insertFormat('# ', '')" title="标题">
                H1
              </el-button>
              <el-button text @click="insertFormat('## ', '')" title="子标题">
                H2
              </el-button>
              <el-button text @click="insertFormat('### ', '')" title="子标题">
                H3
              </el-button>
              <el-divider direction="vertical" />
              <el-button text @click="insertFormat('- ', '')" title="无序列表">
                •
              </el-button>
              <el-button text @click="insertFormat('1. ', '')" title="有序列表">
                1.
              </el-button>
              <el-divider direction="vertical" />
              <el-button text @click="insertFormat('[', '](url)')" title="链接">
                🔗
              </el-button>
              <el-button text @click="insertFormat('`', '`')" title="代码">
                &lt;/&gt;
              </el-button>
              <el-button text @click="insertFormat('\n> ', '')" title="引用">
                ❝
              </el-button>
            </div>
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="15"
              placeholder="请输入帖子内容..."
              maxlength="10000"
              show-word-limit
            />
          </div>

          <el-input
            v-else
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="请输入帖子内容..."
            maxlength="10000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="上传图片（可选）" prop="images">
          <div class="upload-section">
            <div v-if="!previewUrl" class="upload-trigger">
              <el-upload
                v-model:file-list="fileList"
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                accept="image/jpeg,image/png,image/webp,image/gif"
              >
                <el-icon class="upload-icon"><Plus /></el-icon>
              </el-upload>
            </div>
            <div v-else class="uploaded-state">
              <div class="uploaded-preview">
                <img :src="previewUrl" alt="已上传" />
              </div>
              <el-button type="danger" size="small" @click="handleFileRemove" class="remove-btn">
                <el-icon><Delete /></el-icon>
                删除图片
              </el-button>
            </div>
            <div class="upload-tip">支持 JPG、PNG、WEBP、GIF 格式，文件大小不超过 5MB</div>
          </div>
        </el-form-item>

        <div class="form-actions">
          <el-button @click="goBack">取消</el-button>
          <div class="submit-section">
            <el-button type="primary" @click="handleSubmit" :loading="submitting || uploadLoading" class="submit-btn">
              <el-icon v-if="!submitting && !uploadLoading"><Check /></el-icon>
              {{ uploadLoading ? '上传图片中...' : '发布帖子' }}
            </el-button>
            <el-progress v-if="uploadLoading" :percentage="uploadProgress" :format="formatProgress" class="upload-progress" />
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, Plus, Check, Delete } from '@element-plus/icons-vue';
import { createPost, uploadPostImage } from '../../api/community';
import request from '../../api/request';
import { API_BASE } from '../../utils/apiBase';
import { ElMessage } from 'element-plus';

const router = useRouter();
const formRef = ref(null);
const submitting = ref(false);
const showViewer = ref(false);
const viewerImages = ref([]);
const editorMode = ref('simple');
const fileList = ref([]);
const uploadedImages = ref([]);
const pendingFiles = ref([]);

const form = reactive({
  title: '',
  content: ''
});

const rules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 5, message: '内容至少需要5个字符', trigger: 'blur' }
  ]
};

const insertFormat = (before, after) => {
  const textarea = document.querySelector('.rich-editor textarea');
  if (!textarea) return;

  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const text = form.content;
  const selectedText = text.substring(start, end);

  form.content = text.substring(0, start) + before + selectedText + after + text.substring(end);

  setTimeout(() => {
    textarea.focus();
    textarea.setSelectionRange(start + before.length, end + before.length);
  }, 0);
};

const previewUrl = ref('');
const currentFile = ref(null);

const handleFileChange = (uploadFile, uploadFiles) => {
  const file = uploadFile;
  const isImage = file.raw.type.startsWith('image/');
  const isLt5M = file.raw.size / 1024 / 1024 < 5;

  if (!isImage) {
    ElMessage.error('只能上传图片文件！');
    fileList.value = fileList.value.filter(f => f.uid !== file.uid);
    return;
  }

  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！');
    fileList.value = fileList.value.filter(f => f.uid !== file.uid);
    return;
  }

  if (currentFile.value) {
    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value);
    }
  }

  currentFile.value = file.raw;
  previewUrl.value = URL.createObjectURL(file.raw);
  pendingFiles.value = [file.raw];
  fileList.value = [file];
  console.log('图片已添加/替换:', currentFile.value);
};

const handleFileRemove = (file) => {
  if (currentFile.value) {
    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value);
    }
    currentFile.value = null;
    previewUrl.value = '';
    pendingFiles.value = [];
    fileList.value = [];
  }
};

const previewImage = (img) => {
  viewerImages.value = [img];
  showViewer.value = true;
};

const handleUploadedImageError = (event, index) => {
  event.target.src = '';
  event.target.style.display = 'none';
};

const goBack = () => {
  router.push('/community');
};

const uploadProgress = ref(0);
const uploadLoading = ref(false);

const formatProgress = (percentage) => {
  return `${percentage}%`;
};

// 图片压缩函数
const compressImage = (file, maxWidth = 1280, maxHeight = 1280, quality = 0.8) => {
  return new Promise((resolve) => {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const img = new Image();
    
    img.onload = () => {
      // 计算压缩后的尺寸
      let width = img.width;
      let height = img.height;
      
      if (width > maxWidth || height > maxHeight) {
        const ratio = Math.min(maxWidth / width, maxHeight / height);
        width *= ratio;
        height *= ratio;
      }
      
      canvas.width = width;
      canvas.height = height;
      
      // 绘制压缩后的图片
      ctx.drawImage(img, 0, 0, width, height);
      
      // 将 canvas 转换为 Blob
      canvas.toBlob((blob) => {
        // 创建新的文件对象
        const compressedFile = new File([blob], file.name, {
          type: file.type,
          lastModified: Date.now()
        });
        resolve(compressedFile);
      }, file.type, quality);
    };
    
    img.src = URL.createObjectURL(file);
  });
};

// 分块上传函数
const uploadFileInChunks = (file, postId, chunkSize = 1024 * 1024) => {
  return new Promise(async (resolve, reject) => {
    const totalChunks = Math.ceil(file.size / chunkSize);
    let uploadedChunks = 0;
    let totalUploaded = 0;
    
    for (let i = 0; i < totalChunks; i++) {
      const start = i * chunkSize;
      const end = Math.min(start + chunkSize, file.size);
      const chunk = file.slice(start, end);
      
      try {
        await uploadChunk(chunk, i, totalChunks, file.name, postId);
        uploadedChunks++;
        totalUploaded += chunk.size;
        uploadProgress.value = Math.round((totalUploaded / file.size) * 100);
        console.log(`上传进度: ${uploadedChunks}/${totalChunks} (${uploadProgress.value}%)`);
      } catch (error) {
        console.error(`上传第 ${i} 块失败:`, error);
        reject(error);
        return;
      }
    }
    
    // 所有块上传完成，通知服务器合并
    try {
      await mergeChunks(file.name, postId);
      resolve();
    } catch (error) {
      console.error('合并文件失败:', error);
      reject(error);
    }
  });
};

// 带重试机制的网络请求函数
const fetchWithRetry = (url, options, maxRetries = 3, retryDelay = 1000) => {
  return new Promise(async (resolve, reject) => {
    let retries = 0;
    
    const attempt = async () => {
      try {
        const response = await fetch(url, options);
        if (response.ok) {
          resolve(response);
        } else {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
      } catch (error) {
        retries++;
        if (retries <= maxRetries) {
          console.log(`请求失败，${retryDelay}ms 后重试 (${retries}/${maxRetries})`);
          await new Promise(resolve => setTimeout(resolve, retryDelay));
          attempt();
        } else {
          reject(error);
        }
      }
    };
    
    attempt();
  });
};

// 上传单个块
const uploadChunk = (chunk, chunkIndex, totalChunks, fileName, postId) => {
  return new Promise(async (resolve, reject) => {
    const formData = new FormData();
    formData.append('file', chunk);
    formData.append('chunkIndex', chunkIndex);
    formData.append('totalChunks', totalChunks);
    formData.append('fileName', fileName);
    
    const token = localStorage.getItem('token');
    const headers = {};
    if (token) {
      headers['token'] = token;
    }
    
    try {
      await fetchWithRetry(
        `${API_BASE}/api/user/uploadPostImageChunk?postId=${postId}`,
        {
          method: 'POST',
          headers,
          body: formData,
          timeout: 30000 // 30秒超时
        }
      );
      resolve();
    } catch (error) {
      reject(error);
    }
  });
};

// 合并块
const mergeChunks = (fileName, postId) => {
  return new Promise(async (resolve, reject) => {
    const token = localStorage.getItem('token');
    const headers = {};
    if (token) {
      headers['token'] = token;
    }
    
    try {
      await fetchWithRetry(
        `${API_BASE}/api/user/mergePostImageChunks?postId=${postId}&fileName=${encodeURIComponent(fileName)}`,
        {
          method: 'POST',
          headers,
          timeout: 30000 // 30秒超时
        }
      );
      resolve();
    } catch (error) {
      reject(error);
    }
  });
};

const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    submitting.value = true;
    let createdPostId = null;
    let compressedFile = null;
    
    try {
      // 并行处理：创建帖子和压缩图片
      const postData = {
        title: form.title,
        content: form.content
      };
      
      // 开始压缩图片（如果有）
      const compressPromise = pendingFiles.value.length > 0 && pendingFiles.value[0] 
        ? compressImage(pendingFiles.value[0]) 
        : Promise.resolve(null);
      
      // 开始创建帖子
      const createPostPromise = createPost(postData);
      
      // 等待两个操作完成
      const [response, compressed] = await Promise.all([createPostPromise, compressPromise]);
      compressedFile = compressed;
      
      console.log('发布帖子响应:', response);
      if (compressedFile) {
        console.log('图片压缩完成，原始大小:', pendingFiles.value[0].size, '压缩后大小:', compressedFile.size);
      }

      if (!response) {
        ElMessage.error('帖子发布失败');
        return;
      }

      const responseStr = typeof response === 'string' ? response : String(response);
      const postIdMatch = responseStr.match(/postId=(\d+)/);
      if (!postIdMatch) {
        ElMessage.error('获取帖子ID失败');
        return;
      }
      createdPostId = postIdMatch[1];
      console.log('提取的postId:', createdPostId);

      if (pendingFiles.value.length > 0 && pendingFiles.value[0]) {
        console.log('准备上传图片，postId:', createdPostId);
        console.log('待上传文件:', pendingFiles.value[0]);
        
        // 显示上传进度
        uploadLoading.value = true;
        uploadProgress.value = 0;
        
        try {
          // 使用压缩后的图片或原始图片
          const fileToUpload = compressedFile || pendingFiles.value[0];
          
          // 根据文件大小决定是否使用分块上传
          if (fileToUpload.size > 5 * 1024 * 1024) { // 大于5MB使用分块上传
            console.log('文件较大，使用分块上传...');
            await uploadFileInChunks(fileToUpload, createdPostId);
          } else { // 小于等于5MB使用普通上传
            console.log('文件较小，使用普通上传...');
            // 异步上传图片
            const formData = new FormData();
            formData.append('file', fileToUpload);
            
            // 创建带进度的上传请求
            await new Promise((resolve, reject) => {
              const xhr = new XMLHttpRequest();
              let retries = 0;
              const maxRetries = 3;
              const retryDelay = 1000;
              
              const attemptUpload = () => {
                xhr.upload.addEventListener('progress', (event) => {
                  if (event.lengthComputable) {
                    uploadProgress.value = Math.round((event.loaded / event.total) * 100);
                  }
                });
                
                xhr.addEventListener('load', () => {
                  if (xhr.status >= 200 && xhr.status < 300) {
                    resolve(xhr.response);
                  } else {
                    handleError();
                  }
                });
                
                xhr.addEventListener('error', handleError);
                xhr.addEventListener('timeout', handleError);
                
                xhr.open('POST', `${API_BASE}/api/user/uploadPostImage?postId=` + createdPostId);
                xhr.timeout = 30000; // 30秒超时
                
                // 添加token
                const token = localStorage.getItem('token');
                if (token) {
                  xhr.setRequestHeader('token', token);
                }
                
                xhr.send(formData);
              };
              
              const handleError = () => {
                retries++;
                if (retries <= maxRetries) {
                  console.log(`上传失败，${retryDelay}ms 后重试 (${retries}/${maxRetries})`);
                  setTimeout(attemptUpload, retryDelay);
                } else {
                  reject(new Error('上传失败'));
                }
              };
              
              attemptUpload();
            });
          }
          
          console.log('图片上传成功');
          ElMessage.success('帖子及图片发布成功！');
        } catch (uploadError) {
          console.error('图片上传失败:', uploadError);
          // 图片上传失败但帖子已创建，仍然显示成功消息
          ElMessage.success('帖子发布成功，但图片上传失败，请稍后重试！');
        }
      } else {
        ElMessage.success('帖子发布成功！');
      }
      
      router.push('/community');
    } catch (error) {
      console.error('发布帖子失败:', error);
      if (createdPostId) {
        console.log('帖子已创建但图片上传失败，postId:', createdPostId);
      }
      const errorMsg = error.message || '';
      if (errorMsg.includes('违禁词')) {
        ElMessage.error('发布内容包含敏感词，请修改后重试');
      } else {
        ElMessage.error(errorMsg || '发布失败，请重试');
      }
    } finally {
      submitting.value = false;
      uploadLoading.value = false;
      uploadProgress.value = 0;
    }
  });
};
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4f3 100%);
  padding: 80px 20px 40px;
}

.publish-container {
  max-width: 800px;
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

.publish-form {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.publish-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.publish-form :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.publish-form :deep(.el-textarea__inner) {
  border-radius: 10px;
}

.editor-toolbar {
  margin-bottom: 15px;
}

.rich-editor {
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  overflow: hidden;
}

.format-toolbar {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 10px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  flex-wrap: wrap;
}

.format-toolbar :deep(.el-button) {
  padding: 6px 10px;
  min-width: 32px;
}

.format-toolbar :deep(.el-divider--vertical) {
  height: 20px;
}

.upload-section {
  width: 100%;
}

.upload-trigger {
  display: inline-block;
}

.uploaded-state {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.uploaded-preview {
  width: 100px;
  height: 100px;
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid #67c23a;
}

.uploaded-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  margin-top: 30px;
}

.upload-section :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
  border-radius: 10px;
}

.upload-section :deep(.el-upload-list__item) {
  width: 100px;
  height: 100px;
  border-radius: 10px;
}

.upload-tip {
  color: #999;
  font-size: 13px;
  margin-top: 10px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 15px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.submit-section {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.upload-progress {
  width: 200px;
  margin-top: 5px;
}

.submit-btn {
  background: linear-gradient(135deg, #008080 0%, #2a9d8f 100%);
  border: none;
  padding: 12px 40px;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
}

.submit-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .publish-form {
    padding: 20px;
  }

  .page-title {
    font-size: 22px;
  }

  .format-toolbar {
    gap: 2px;
  }

  .format-toolbar :deep(.el-button) {
    padding: 4px 6px;
    font-size: 12px;
  }
}
</style>
