<template>
  <div class="master">
    <!-- 导航栏组件 -->
    <Navbar :transparent="false" />

    <!-- 名师讲解内容 -->
    <div class="master-content">
      <div class="master-layout">
        <!-- 左侧：人物介绍 -->
        <div class="master-detail">
          <div class="master-image-container">
            <div class="background-pattern"></div>
            <img :src="selectedMaster.image" :alt="selectedMaster.name" class="master-image" />
          </div>
          <div class="master-info">
            <h2 class="master-name">{{ selectedMaster.name }}</h2>
            <p class="master-title">{{ selectedMaster.title }}</p>
            <div class="master-bio">{{ selectedMaster.bio }}</div>
            <button class="btn btn-primary" @click="openVideoPlayer">查看讲解视频</button>
          </div>
        </div>

        <!-- 右侧：老师选择列表 -->
        <div class="master-list">
          <div class="list-header">
            <div class="decorative-pattern"></div>
            <h3 class="list-title">名师列表</h3>
          </div>
          <div class="list-content">
            <div 
              v-for="(master, index) in masters" 
              :key="index"
              class="master-item"
              :class="{ 'active': selectedMasterId.value === master.id }"
              @click="selectMaster(master.id)"
            >
              <div class="master-item-info">
                <h4>{{ master.name }}</h4>
                <p>{{ master.specialty }}</p>
              </div>
              <div class="master-item-arrow" v-if="selectedMasterId.value === master.id">→</div>
            </div>
          </div>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import gsap from 'gsap';
import Navbar from '../../components/Navbar.vue';

// 生成B站播放器URL
const getBilibiliPlayerUrl = (url) => {
  // 从URL中提取BV号
  const bvMatch = url.match(/BV[0-9A-Za-z]+/);
  if (bvMatch) {
    const bvid = bvMatch[0];
    // 使用更完整的参数，添加high_quality和danmaku参数
    return `https://player.bilibili.com/player.html?bvid=${bvid}&autoplay=1&high_quality=1&danmaku=0`;
  }
  return url;
};

// 名师数据
const masters = [
  {
    id: 1,
    name: "林伟周",
    title: "国家级非遗级制茶大师",
    specialty: "茶艺制作过程讲解",
    bio: "林伟周，潮州凤凰山三代制茶世家，乌龙茶制作技艺（潮州单丛茶制作技艺）省级非遗代表性传承人，国务院特殊津贴专家，南馥茶业创始人，潮安区茶叶协会会长。",
    image:"https://tse4-mm.cn.bing.net/th/id/OIP-C.MxoASlXIs0xI_jJ1fTAcUAHaE8?w=276&h=184&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3",
    videoUrl: "https://tv.cctv.com/2025/06/15/VIDEKMWuaZ9jpUs9aTxAnH54250615.shtml"
  },
  {
    id: 2,
    name: "王岳飞",
    title: "中国科协全国首席科学传播（茶学）专家",
    specialty: "茶史讲解",
    bio: "王岳飞，浙江大学茶学学科带头人，教授、博士生导师，国家一级评茶师，国内茶学界与茶文化传播领域的标志性人物。",
    image: "/teacher/老师2.png",
    videoUrl: "https://www.bilibili.com/video/BV1YJ411x7v9?spm_id_from=333.788.videopod.episodes"
  },
  {
    id: 3,
    name: "王旭烽",
    title: "高级评茶师",
    specialty: "品茶艺术讲解",
    bio: "王旭烽，高级评茶师，擅长茶叶审评和品质鉴定，对广东各茶区的茶叶特性有深入研究。她致力于推广科学的品茶方法，让更多人了解茶的魅力和文化内涵。",
    image: "https://meizi-chao-pub.8531.cn/1749340400344256522_720px.jpeg?width=720&height=540",
    videoUrl: "https://www.bilibili.com/video/BV1rB4y1K71A?t=5.3"
  }
];



// 状态
const selectedMasterId = ref(1);
// 计算属性：当前选中的老师
const selectedMaster = computed(() => {
  return masters.find(master => master.id === selectedMasterId.value) || masters[0];
});

// 选择老师
const selectMaster = (id) => {
  selectedMasterId.value = id;
};

// 打开视频播放器 - 直接跳转到B站视频页面
const openVideoPlayer = () => {
  if (selectedMaster.value) {
    window.open(selectedMaster.value.videoUrl, '_blank');
  }
};



onMounted(() => {
  // 移除动画效果，避免影响间距
});
</script>

<style scoped>
.master {
  min-height: 100vh;
  padding-top: 80px;
  background-color: #f8f9fa;
  background-image: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.master-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* 左右布局 */
.master-layout {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 40px;
  background: white;
  border-radius: 16px;
  padding: 0;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 左侧：人物介绍 */
.master-detail {
  display: flex;
  flex-direction: column;
  padding: 50px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  position: relative;
}

.master-image-container {
  position: relative;
  width: 100%;
  max-width: 400px;
  height: 320px;
  border-radius: 12px;
  overflow: hidden;
  margin: 0 auto 30px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #5c7a6a 0%, #3d5a4a 50%, #5c7a6a 100%);
}

.background-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M30 5C38.2843 5 45 11.7157 45 20C45 28.2843 38.2843 35 30 35C21.7157 35 15 28.2843 15 20C15 11.7157 21.7157 5 30 5ZM30 10C24.4772 10 20 14.4772 20 20C20 25.5228 24.4772 30 30 30C35.5228 30 40 25.5228 40 20C40 14.4772 35.5228 10 30 10Z' fill='%23ffffff' fill-opacity='0.05'/%3E%3C/svg%3E");
  background-repeat: repeat;
}

.master-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center center;
  display: block;
  transition: transform 0.5s ease;
}

.master-image-container::before,
.master-image-container::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  width: 30px;
  z-index: 1;
  pointer-events: none;
}

.master-image-container::before {
  left: 0;
  background: linear-gradient(to right, rgba(92, 122, 106, 0.4), transparent);
}

.master-image-container::after {
  right: 0;
  background: linear-gradient(to left, rgba(92, 122, 106, 0.4), transparent);
}

.master-image-container:hover .master-image {
  transform: scale(1.05);
}

.master-info {
  flex: 1;
}

.master-name {
  font-size: 24px;
  color: #2c3e50;
  margin-bottom: 10px;
  font-family: "STKaiti", "KaiTi", "楷体", serif;
  font-weight: 600;
}

.master-title {
  font-size: 16px;
  color: #4db6ac;
  margin-bottom: 20px;
  font-weight: 500;
}

.master-bio {
  color: #555;
  line-height: 1.7;
  margin-bottom: 30px;
  font-size: 14px;
  text-align: justify;
}

/* 右侧：老师选择列表 */
.master-list {
  background: #f8f9fa;
  border-left: 1px solid #e9ecef;
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list-header {
  text-align: center;
  margin-bottom: 10px;
}

.decorative-pattern {
  width: 80px;
  height: 80px;
  margin: 0 auto 10px;
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M50 0C77.6142 0 100 22.3858 100 50C100 77.6142 77.6142 100 50 100C22.3858 100 0 77.6142 0 50C0 22.3858 22.3858 0 50 0ZM50 10C27.9086 10 10 27.9086 10 50C10 72.0914 27.9086 90 50 90C72.0914 90 90 72.0914 90 50C90 27.9086 72.0914 10 50 10Z' fill='%234db6ac' fill-opacity='0.1'/%3E%3Cpath d='M50 20C66.5685 20 80 33.4315 80 50C80 66.5685 66.5685 80 50 80C33.4315 80 20 66.5685 20 50C20 33.4315 33.4315 20 50 20ZM50 30C38.9543 30 30 38.9543 30 50C30 61.0457 38.9543 70 50 70C61.0457 70 70 61.0457 70 50C70 38.9543 61.0457 30 50 30Z' fill='%234db6ac' fill-opacity='0.2'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
}

.list-title {
  font-size: 18px;
  color: #2c3e50;
  font-family: "STKaiti", "KaiTi", "楷体", serif;
  margin: 0;
  position: relative;
}

.list-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 2px;
  background: #4db6ac;
  margin: 10px auto 0;
}

.list-content {
  display: flex;
  flex-direction: column;
}

.master-item {
  margin-bottom: 10px;
}

.master-item {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  opacity: 1;
}

/* 未选中的老师项目样式 */
.master-item:not(.active) {
  opacity: 1;
  background: white;
}

/* 未选中的老师项目文字颜色 */
.master-item:not(.active) .master-item-info h4 {
  color: #2c3e50;
  font-weight: 600;
}

.master-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: transparent;
  border-radius: 8px 0 0 8px;
  transition: all 0.3s ease;
}

.master-item:hover {
  background: #f8f9fa;
  border-color: #4db6ac;
  transform: translateX(5px);
}

.master-item.active {
  background: #f0f9f9;
  border-color: #4db6ac;
  box-shadow: 0 2px 8px rgba(77, 182, 172, 0.15);
}

.master-item.active::before {
  background: #4db6ac;
}

.master-item-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.master-item-info p {
  margin: 0;
  font-size: 13px;
  color: #6c757d;
}

.master-item.active .master-item-info h4 {
  color: #4db6ac;
}

.master-item-arrow {
  font-size: 18px;
  font-weight: bold;
  color: #4db6ac;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.master-item.active .master-item-arrow {
  opacity: 1;
}



/* 响应式设计 */
@media (max-width: 1024px) {
  .master-layout {
    grid-template-columns: 1fr;
    gap: 0;
  }
  
  .master-list {
    border-left: none;
    border-top: 1px solid #e9ecef;
  }
  
  .master-detail {
    padding: 40px;
  }
  
  .master-image-container {
    max-width: 350px;
    height: 280px;
  }
}

@media (max-width: 768px) {
  .master-content {
    padding: 20px 15px;
  }
  
  .master-detail {
    padding: 30px 20px;
  }
  
  .master-image-container {
    max-width: 100%;
    height: 260px;
  }
  
  .video-player {
    height: 300px;
  }
}

@media (max-width: 480px) {
  .master-detail {
    padding: 20px 15px;
  }
  
  .master-image-container {
    height: 220px;
  }
  
  .master-name {
    font-size: 20px;
  }
  
  .master-title {
    font-size: 14px;
  }
  
  .master-bio {
    font-size: 13px;
  }
}
</style>