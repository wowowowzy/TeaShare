<template>
  <div class="video-background">
    <!-- 视频背景容器 -->
    <div class="video-container" ref="videoContainer">
      <!-- 视频元素 -->
      <video
        ref="videoPlayer"
        :src="videoSrc"
        :poster="posterSrc"
        autoplay
        loop
        muted
        playsinline
        class="background-video"
        @loadeddata="onVideoLoaded"
        @error="onVideoError"
      ></video>
      
      <!-- 遮罩层，控制视频透明度 -->
      <div class="video-overlay" :style="overlayStyle"></div>
      
      <!-- 内容插槽 -->
      <div class="content-container" :class="{ 'fade-in': videoReady }">
        <slot></slot>
      </div>
      
      <!-- 加载指示器 -->
      <div v-if="!videoReady" class="loading-indicator">
        <div class="loading-spinner">
          <div class="spinner"></div>
          <p>加载茶叶种植视频中...</p>
        </div>
      </div>
      
      <!-- 播放控制按钮 -->
      <div class="video-controls" v-if="showControls">
        <button @click="toggleMute" class="control-btn">
          <el-icon><Mute v-if="isMuted" /><Microphone v-else /></el-icon>
          {{ isMuted ? '取消静音' : '静音' }}
        </button>
        <button @click="togglePause" class="control-btn">
          <el-icon><VideoPlay v-if="isPaused" /><VideoPause v-else /></el-icon>
          {{ isPaused ? '播放' : '暂停' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Microphone, Mute, VideoPlay, VideoPause } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 视频源（本地或远程）
  videoSrc: {
    type: String,
    default: '/assest/tea-plantation.mp4'
  },
  // 预览图
  posterSrc: {
    type: String,
    default: '/assest/tea-poster.jpg'
  },
  // 遮罩层颜色
  overlayColor: {
    type: String,
    default: 'rgba(0, 0, 0, 0.3)'
  },
  // 遮罩层透明度
  overlayOpacity: {
    type: Number,
    default: 0.3
  },
  // 是否显示控制按钮
  showControls: {
    type: Boolean,
    default: false
  },
  // 播放速度
  playbackRate: {
    type: Number,
    default: 1.0
  }
})

// Refs
const videoPlayer = ref(null)
const videoContainer = ref(null)
const isMuted = ref(true) // 默认静音，避免自动播放限制
const isPaused = ref(false)
const videoReady = ref(false)
const videoError = ref(false)
const isMobile = ref(false)

// 计算遮罩层样式
const overlayStyle = computed(() => ({
  backgroundColor: props.overlayColor,
  opacity: props.overlayOpacity
}))

// 检测是否为移动设备
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 视频加载完成
const onVideoLoaded = () => {
  videoReady.value = true
  console.log('茶叶种植视频加载完成')
}

// 视频加载错误
const onVideoError = (error) => {
  videoError.value = true
  ElMessage.error('视频加载失败，请检查网络连接或视频源')
  console.error('视频加载错误:', error)
}

// 切换静音
const toggleMute = () => {
  if (videoPlayer.value) {
    isMuted.value = !isMuted.value
    videoPlayer.value.muted = isMuted.value
  }
}

// 切换播放/暂停
const togglePause = () => {
  if (videoPlayer.value) {
    if (videoPlayer.value.paused) {
      videoPlayer.value.play()
      isPaused.value = false
    } else {
      videoPlayer.value.pause()
      isPaused.value = true
    }
  }
}

// 自适应视频尺寸
const resizeVideo = () => {
  if (!videoPlayer.value || !videoContainer.value) return
  
  const container = videoContainer.value
  const video = videoPlayer.value
  
  const containerRatio = container.offsetWidth / container.offsetHeight
  const videoRatio = video.videoWidth / video.videoHeight
  
  if (containerRatio > videoRatio) {
    // 容器更宽，视频填满高度
    video.style.width = 'auto'
    video.style.height = '100%'
  } else {
    // 容器更高，视频填满宽度
    video.style.width = '100%'
    video.style.height = 'auto'
  }
}

// 监听窗口大小变化
const onResize = () => {
  checkMobile()
  resizeVideo()
}

// 生命周期
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', onResize)
  
  // 延迟执行，确保DOM已渲染
  setTimeout(() => {
    if (videoPlayer.value) {
      resizeVideo()
    }
  }, 100)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  if (videoPlayer.value) {
    videoPlayer.value.pause()
  }
})

// 监听播放速率变化
watch(() => props.playbackRate, (newRate) => {
  if (videoPlayer.value && videoPlayer.value.playbackRate !== undefined) {
    videoPlayer.value.playbackRate = newRate
  }
})
</script>

<style scoped>
.video-background {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.video-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.background-video {
  position: absolute;
  top: 50%;
  left: 50%;
  min-width: 100%;
  min-height: 100%;
  width: auto;
  height: auto;
  z-index: 0;
  transform: translate(-50%, -50%);
  object-fit: cover;
  filter: brightness(0.9) contrast(1.1);
  transition: filter 0.5s ease;
}

.background-video:hover {
  filter: brightness(1) contrast(1.2);
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  transition: opacity 0.5s ease;
}

.content-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 1200px;
  padding: 0 20px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 1s ease 0.5s;
}

.content-container.fade-in {
  opacity: 1;
  transform: translateY(0);
}

.loading-indicator {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
  background: rgba(0, 0, 0, 0.7);
}

.loading-spinner {
  text-align: center;
  color: #fff;
}

.spinner {
  display: inline-block;
  width: 50px;
  height: 50px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #789F6F;
  animation: spin 1s ease-in-out infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-spinner p {
  margin: 0;
  font-size: 16px;
  color: #789F6F;
  font-family: "STKaiti", "KaiTi", "楷体", serif;
}

.video-controls {
  position: absolute;
  bottom: 20px;
  right: 20px;
  z-index: 3;
  display: flex;
  gap: 10px;
  opacity: 0.7;
  transition: opacity 0.3s ease;
}

.video-controls:hover {
  opacity: 1;
}

.control-btn {
  background: rgba(120, 159, 111, 0.8);
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
}

.control-btn:hover {
  background: rgba(120, 159, 111, 1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(120, 159, 111, 0.3);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .video-background {
    height: 60vh;
  }
  
  .background-video {
    filter: brightness(0.85) contrast(1.2);
  }
  
  .loading-spinner p {
    font-size: 14px;
  }
  
  .video-controls {
    bottom: 10px;
    right: 10px;
  }
  
  .control-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
  
  .content-container {
    padding: 0 15px;
  }
}

/* 性能优化：在低性能设备上降低质量 */
@media (prefers-reduced-motion: reduce) {
  .background-video {
    filter: none;
  }
  
  .content-container {
    transition: none;
  }
}
</style>