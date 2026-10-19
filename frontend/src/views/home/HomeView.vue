

<template>
  <div class="home">
    <!-- 侧边导航栏 -->
    <Sidebar />
    
    <!-- 导航栏组件（首页使用透明背景） -->
    <Navbar :transparent="true" />

    <!-- 视频背景 -->
    <VideoBackground
              videoSrc="https://java-web-itheima-zy.oss-cn-beijing.aliyuncs.com/tea-plantation.mp4"
              posterSrc="/assest/tea-poster.jpg"
              overlayColor="rgba(0, 128, 128, 0.3)"
              :overlayOpacity="0.3"
            >
      <!-- 视频上的内容 -->
      <div class="hero-content">
        <h1 class="hero-title">茶韵岭南，品味广东</h1>
        <p class="hero-subtitle">一片叶子，承载着千年的文化与辛勤的汗水</p>
        <div class="nav-modules">
          <div 
            v-for="(item, index) in navItems" 
            :key="item.id"
            class="nav-module"
            @click="scrollToSection(item.id)"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            <div class="nav-icon">{{ item.icon }}</div>
            <div class="nav-content">
              <h3>{{ item.title }}</h3>
              <p>{{ item.subtitle }}</p>
            </div>
            <div class="nav-arrow">→</div>
          </div>
        </div>
      </div>
    </VideoBackground>
    
    <!-- AI助手按钮 - 移到VideoBackground外部，确保固定定位正常工作 -->
    <div class="ai-assistant" @click="goToAI">
      <div class="ai-button">
        <img src="/assest/number1.png" alt="AI Assistant" />
      </div>
      <p class="ai-text">点击图片可以与小茗对话哦</p>
    </div>



    


    <!-- 首页内容 -->
    <div class="home-content">
      <!-- 特色茶种 -->
      <TeaVarieties />

      <!-- 制茶过程 -->
      <TeaMaking />

      <!-- 茶源历史 -->
      <TeaHistory />
    </div>


  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import * as THREE from 'three';
import gsap from 'gsap';
import ScrollReveal from 'scrollreveal';
import Navbar from '../../components/Navbar.vue';
import VideoBackground from '../../components/VideoBackground.vue';
import TeaVarieties from '../../components/home/TeaVarieties.vue';
import TeaMaking from '../../components/home/TeaMaking.vue';
import TeaHistory from '../../components/home/TeaHistory.vue';
import Sidebar from '../../components/Sidebar.vue';

// 路由
const router = useRouter();


// 动态导入Vanta.js
let VANTA = null;
let vantaEffect = null;
import('vanta/dist/vanta.fog.min.js').then(module => {
  VANTA = module.default;
  // 初始化Vanta.js效果
  if (VANTA && document.getElementById('vanta-bg')) {
    vantaEffect = VANTA.FOG({
      el: '#vanta-bg',
      mouseControls: true,
      touchControls: true,
      gyroControls: false,
      minHeight: 200.00,
      minWidth: 200.00,
      highlightColor: 0x8fd1c7,
      midtoneColor: 0x4a7c75,
      lowlightColor: 0x1a3a36,
      baseColor: 0x000000,
      blurFactor: 0.50,
      speed: 1.00,
      zoom: 0.80
    });
  }
});

// 导航数据
const navItems = ref([
  {
    id: 'tea-varieties',
    title: '特色茶种',
    subtitle: '品味岭南茶香',
    icon: '🍃'
  },
  {
    id: 'tea-making',
    title: '制茶品茗',
    subtitle: '传承匠心技艺',
    icon: '👐'
  },
  {
    id: 'tea-history',
    title: '茶源历史',
    subtitle: '追溯千年茶路',
    icon: '📜'
  }
]);

// 滚动到指定区域
const scrollToSection = (sectionId) => {
  const element = document.getElementById(sectionId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

// 跳转到AI对话界面
const goToAI = () => {
  router.push('/quiz');
}

onMounted(() => {
  // 初始化滚动动画
  initScrollReveal();
});

onUnmounted(() => {
  // 清理Vanta.js
  if (vantaEffect) {
    vantaEffect.destroy();
  }
});

// 初始化滚动动画
function initScrollReveal() {
  const sr = ScrollReveal({
    origin: 'bottom',
    distance: '30px',
    duration: 1000,
    delay: 200,
    easing: 'cubic-bezier(0.5, 0, 0, 1)'
  });

  sr.reveal('.section-title', { interval: 100 });
  sr.reveal('.content-section', { interval: 200 });
  sr.reveal('.tea-selector-item', { interval: 100 });
  sr.reveal('.making-detail', { delay: 300 });
  sr.reveal('.history-timeline', { delay: 400 });
}
</script>

<style scoped>
.home {
  position: relative;
  min-height: 100vh;
}

/* 英雄区域样式 - 保持不变 */
.hero-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: white;
  z-index: 10;
  width: 100%;
  max-width: 1200px;
  padding: 0 20px;
}

.hero-title {
  font-size: 48px;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  font-family: "STKaiti", "KaiTi", "楷体", serif;
}

.hero-subtitle {
  font-size: 24px;
  margin-bottom: 40px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  font-weight: 300;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

/* 导航模块 - 保持不变 */
.nav-modules {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  max-width: 900px;
  margin: 40px auto 0;
  padding: 20px;
}

.nav-module {
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 25px 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.6s ease forwards;
}

.nav-module:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.3);
}

.nav-icon {
  font-size: 2.5rem;
  flex-shrink: 0;
}

.nav-content {
  flex: 1;
  text-align: left;
}

.nav-content h3 {
  color: white;
  margin: 0 0 5px 0;
  font-size: 1.3rem;
  font-weight: 600;
}

.nav-content p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-size: 0.9rem;
}

.nav-arrow {
  color: #4db6ac;
  font-size: 1.5rem;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.nav-module:hover .nav-arrow {
  opacity: 1;
  transform: translateX(0);
}

/* AI助手样式 */
.ai-assistant {
  position: fixed;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  z-index: 100;
  box-sizing: border-box;
  padding: 0;
  margin: 0;
}

.ai-button {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ffffff 0%, #ffffff 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  /* box-shadow: 0 4px 15px rgba(255, 255, 255, 0.4); */
  transition: all 0.3s ease;
  padding: 10px;
}

.ai-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(238, 90, 90, 0.6);
}

.ai-button img {
  display: block;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  object-fit: cover;
  border-radius: 50%;
}

.ai-text {
  margin-top: 10px;
  color: #5e5b5b;
  font-size: 12px;
  font-weight: bold;
  background-color: #fff;
  padding: 4px 8px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .ai-assistant {
    right: 20px;
  }
  
  .ai-button {
    width: 70px;
    height: 70px;
  }
}

@media (max-width: 768px) {
  .ai-assistant {
    right: 15px;
  }
  
  .ai-button {
    width: 60px;
    height: 60px;
  }
  
  .ai-text {
    font-size: 10px;
    padding: 3px 6px;
  }
}

@media (max-width: 480px) {
  .ai-assistant {
    right: 10px;
  }
  
  .ai-button {
    width: 50px;
    height: 50px;
  }
  
  .ai-text {
    font-size: 9px;
  }
}

/* 首页内容 */
.home-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  background: #f9f9f9;
}







/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .nav-modules {
    grid-template-columns: 1fr;
    max-width: 500px;
  }
  
  .tea-detail {
    grid-template-columns: 1fr;
  }
  
  .tea-info-left {
    border-right: none;
    border-bottom: 1px solid #eee;
    padding-right: 0;
    padding-bottom: 30px;
  }
  
  .step-content-grid {
    grid-template-columns: 1fr;
  }
  
  .timeline-era.left,
  .timeline-era.right {
    left: 50%;
  }
  
  .timeline-era {
    width: 180px;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-subtitle {
    font-size: 1.1rem;
    padding: 0 20px;
  }
  
  .nav-modules {
    grid-template-columns: 1fr;
    max-width: 90%;
    gap: 15px;
    padding: 10px;
  }
  
  .nav-module {
    padding: 20px;
    flex-direction: column;
    text-align: center;
  }
  
  .nav-content {
    text-align: center;
  }
  
  .nav-content h3 {
    font-size: 1.1rem;
  }
  
  .nav-arrow {
    margin-top: 10px;
  }
  
  .home-content {
    padding: 20px 15px;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .content-section {
    padding: 40px 20px;
  }
  
  .tea-selector {
    gap: 10px;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .tea-selector-item {
    min-width: 100px;
    padding: 15px 10px;
  }
  
  .process-flowchart {
    padding: 15px;
  }
  
  .flow-step {
    width: 80px;
  }
  
  .step-name {
    min-width: 80px;
    font-size: 0.8rem;
  }
  
  .history-timeline {
    height: 800px;
  }
  
  .nodes-grid,
  .figures-grid {
    grid-template-columns: 1fr;
  }
  
  .master-info {
    flex-direction: column;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
    line-height: 1.2;
  }
  
  .hero-subtitle {
    font-size: 1rem;
    line-height: 1.4;
  }
  
  .nav-modules {
    gap: 10px;
  }
  
  .nav-icon {
    font-size: 2rem;
  }
  
  .nav-module {
    padding: 15px;
  }
  
  .nav-content h3 {
    font-size: 1rem;
  }
  
  .nav-content p {
    font-size: 0.8rem;
  }
  
  .section-title {
    font-size: 1.8rem;
  }
  
  .content-section {
    padding: 30px 15px;
  }
  
  .tea-name {
    font-size: 1.5rem;
  }
  
  .tea-selector {
    flex-direction: column;
    align-items: center;
  }
  
  .tea-selector-item {
    width: 100%;
    max-width: 200px;
  }
  
  .process-flowchart-image {
    margin-bottom: 30px;
  }
  
  .flow-image-container {
    height: 300px;
  }
  
  .step-content-grid {
    gap: 20px;
  }
  
  .making-detail {
    padding: 20px;
  }
  
  .master-interview {
    padding: 20px;
  }
  
  .dynasty-images {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .dynasty-card {
    min-height: 300px;
  }
  
  .modal-content {
    padding: 20px;
    width: 95%;
  }
}
</style>