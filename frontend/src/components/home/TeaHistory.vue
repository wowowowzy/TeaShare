<template>
  <div class="history-scroll" id="tea-history" ref="historySection">
    <!-- 主标题 -->
    <h1 class="main-title">千年传承 · 茶源历史</h1>
    <p class="subtitle">从古至今的茶叶贸易与文化传播</p>

    <!-- 卷轴容器 -->
    <div class="scroll-container" :class="{ 'animate': isVisible }">
      <!-- 卷轴轴头 -->
      <div class="scroll-left-axis"></div>
      <div class="scroll-right-axis"></div>

      <!-- 朝代图片区域 -->
      <div class="dynasty-images">
        <!-- 唐朝卡片 -->
        <div class="dynasty-card tang" @click="showDetail('tang')">
          <div class="dynasty-img-container">
            <img src="/assest/唐2.png" alt="唐朝茶文化" class="dynasty-img">
          </div>
          <div class="card-footer">
            <div class="card-title">唐</div>
            <div class="card-desc">茶道始成，盛世雅事</div>
          </div>
        </div>

        <!-- 宋朝卡片 -->
        <div class="dynasty-card song" @click="showDetail('song')">
          <div class="dynasty-img-container">
            <img src="/assest/宋1.png" alt="宋朝茶文化" class="dynasty-img">
          </div>
          <div class="card-footer">
            <div class="card-title">宋</div>
            <div class="card-desc">极致精雅，点茶艺术</div>
          </div>
        </div>

        <!-- 明朝卡片 -->
        <div class="dynasty-card ming" @click="showDetail('ming')">
          <div class="dynasty-img-container">
            <img src="/assest/明1.png" alt="明朝茶文化" class="dynasty-img">
          </div>
          <div class="card-footer">
            <div class="card-title">明</div>
            <div class="card-desc">返璞归真，散泡风行</div>
          </div>
        </div>

        <!-- 清朝卡片 -->
        <div class="dynasty-card qing" @click="showDetail('qing')">
          <div class="dynasty-img-container">
            <img src="/assest/清1.png" alt="清朝茶文化" class="dynasty-img">
          </div>
          <div class="card-footer">
            <div class="card-title">清</div>
            <div class="card-desc">茶通四海，俗雅共荣</div>
          </div>
        </div>
      </div>
    </div>

    <!-- <div class="accordion-container">
      <div v-for="card in cardList" :key="card.id" class="card"
        :class="[`card-${card.id}`, { expanded: activeId === card.id }]" @click="toggleCard(card.id)">
        <div class="card-content">
          <div class="card-number">{{ card.id }}</div>
          <div class="card-title">{{ card.title }}</div>
          <div class="card-poem">{{ card.poem }}</div>
        </div>
      </div>
    </div> -->

    <!-- 详情弹窗 -->
    <div class="detail-modal" v-if="selectedDynasty" @click="closeDetail">
      <div class="modal-content" @click.stop>
        <div class="close-btn" @click="closeDetail">×</div>
        <!-- 文字内容容器 -->
        <div class="modal-text-container" v-html="dynastyDetails[selectedDynasty].text"></div>
        <!-- 图片容器 -->
        <div class="modal-img-container">
          <img :src="dynastyDetails[selectedDynasty].img" :alt="selectedDynasty + '茶文化'" class="modal-img">
          <p class="modal-img-caption">{{ dynastyDetails[selectedDynasty].imgCaption }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// 选中的朝代
const selectedDynasty = ref(null);

// 历史模块引用
const historySection = ref(null);

// 是否可见（用于触发动画）
const isVisible = ref(false);

// Intersection Observer
let observer = null;

// 初始化Intersection Observer
onMounted(() => {
  observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        isVisible.value = true;
        // 动画触发后停止观察
        observer.unobserve(entry.target);
      }
    });
  }, {
    threshold: 0.2, // 当20%的元素可见时触发
    rootMargin: '0px 0px -50px 0px'
  });

  if (historySection.value) {
    observer.observe(historySection.value);
  }
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});

// 各朝代详细数据（包含配图）
const dynastyDetails = {
  tang: {
    text: `
      <h2 class="detail-title">唐：茶道始成，盛世雅事</h2>
      <div class="detail-section">
        <h3 class="detail-subtitle">划时代著作</h3>
        <p class="detail-text">陆羽著《茶经》，世界第一部茶学专著问世，系统总结了茶之源、具、造、器、煮、饮、事、出、略、图，被誉为“茶叶百科全书”，标志着茶学独立成科，茶文化进入自觉时代。</p>
      </div>
      <div class="detail-section">
        <h3 class="detail-subtitle">饮用方式</h3>
        <p class="detail-text">推崇“煮茶法”（煎茶），茶饼经炙、碾、罗后成末，于沸水中煎煮，分饮。注重水质、火候与品饮仪式。</p>
      </div>
      <div class="detail-section">
        <h3 class="detail-subtitle">文化地位</h3>
        <p class="detail-text">饮茶之风从上至下普及，成为“比屋之饮”。寺庙禅茶兴盛，茶宴、茶会盛行，茶与诗歌、书法深度结合，奠定了茶作为精神文化的基石。</p>
      </div>
    `,
    img: "/assest/历史1.png",
    imgCaption: "唐代煎茶图（复原）"
  },
  song: {
    text: `
      <h2 class="detail-title">宋：极致精雅，点茶艺术</h2>
      <div class="detail-section">
        <h3 class="detail-subtitle">技艺巅峰</h3>
        <p class="detail-text">盛行“点茶法”。将茶末置于盏中，以少量沸水调成膏状，再持续注入沸水，同时用茶筅快速击拂，形成丰富的白色沫浡（汤花）。技艺高超者可在汤花上作画，称“茶百戏”或“水丹青”。</p>
      </div>
      <div class="detail-section">
        <h3 class="detail-subtitle">斗茶风尚</h3>
        <p class="detail-text">上至皇室，下至市井，盛行评比茶质、技艺、器具的“斗茶”活动。黑釉建盏因能衬托白色汤花而备受推崇。</p>
      </div>
      <div class="detail-section">
        <h3 class="detail-subtitle">宫廷茶事</h3>
        <p class="detail-text">宋徽宗赵佶亲撰《大观茶论》，将宫廷点茶艺术推向极致。贡茶体系完善，北苑官焙的“龙凤团茶”制作精巧，价值连城。</p>
      </div>
    `,
    img: "/assest/历史2.png",
    imgCaption: "宋代点茶与斗茶场景"
  },
  ming: {
    text: `
      <h2 class="detail-title">明：返璞归真，散泡风行</h2>
      <div class="detail-section">
        <h3 class="detail-subtitle">革命性变革</h3>
        <p class="detail-text">明太祖朱元璋下诏“罢造龙团，惟采芽茶以进”，废除了耗时费工的团饼贡茶，改贡散叶茶。这一政令极大地推动了制茶与饮茶方式的根本性变革。</p>
      </div>
      <div class="detail-section">
        <h3 class="detail-subtitle">饮法简化</h3>
        <p class="detail-text">散茶“瀹泡法”（直接用沸水冲泡茶叶）成为主流，壶、盏搭配的简便饮法迅速普及，更贴近日常生活，沿用至今。</p>
      </div>
      <div class="detail-section">
        <h3 class="detail-subtitle">茶类百花齐放</h3>
        <p class="detail-text">炒青制茶工艺成熟，绿茶发展至高峰。同时，在散茶基础上，黄茶、黑茶、花茶等茶类工艺也得到创新与发展，为六大茶类体系奠定基础。</p>
      </div>
    `,
    img: "/assest/历史3.png",
    imgCaption: "明代散茶冲泡场景"
  },
  qing: {
    text: `
      <h2 class="detail-title">清：茶通四海，俗雅共荣</h2>
      <div class="detail-section">
        <h3 class="detail-subtitle">对外贸易的支柱</h3>
        <p class="detail-text">茶叶成为最重要的出口商品。通过广州“十三行”，大量茶叶销往欧洲，尤其深受英国社会喜爱，形成了巨大的贸易顺差。为扭转逆差，英国向东输入鸦片，成为鸦片战争的重要经济背景。</p>
      </div>
      <div class="detail-section">
        <h3 class="detail-subtitle">饮茶全民化</h3>
        <p class="detail-text">茶馆在城市乡村遍地开花，成为最重要的公共社交空间。不同地域、阶层的饮茶习俗（如粤式早茶、京式大碗茶、功夫茶）最终定型，茶彻底融入市井生活。</p>
      </div>
      <div class="detail-section">
        <h3 class="detail-subtitle">茶叶品类完善</h3>
        <p class="detail-text">乌龙茶、红茶、白茶工艺在清代进一步发展成熟。六大茶类（绿、白、黄、青/乌龙、红、黑）在此时已基本齐全，制茶技术达到古代社会的高峰。</p>
      </div>
    `,
    img: "/assest/历史4.png",
    imgCaption: "清代广州十三行茶叶贸易"
  }
};

// 显示详情弹窗
function showDetail(dynasty) {
  selectedDynasty.value = dynasty;
  // 禁止页面滚动
  document.body.style.overflow = 'hidden';
}

// 关闭详情弹窗
function closeDetail() {
  selectedDynasty.value = null;
  // 恢复页面滚动
  document.body.style.overflow = 'auto';
}

// 卡片数据（可自由修改）
const cardList = ref([
  {
    id: 1,
    title: '唐',
    poem: '"国破山河在，城春草木深。" —— 杜甫《春望》'
  },
  {
    id: 2,
    title: '宋',
    poem: '"大江东去，浪淘尽，千古风流人物。" —— 苏轼《念奴娇·赤壁怀古》'
  },
  {
    id: 3,
    title: '明',
    poem: '"山高月小，水落石出。" —— 苏轼《后赤壁赋》（明代文人常引用）'
  },
  {
    id: 4,
    title: '清',
    poem: '"千山鸟飞绝，万径人踪灭。" —— 柳宗元《江雪》'
  }
])

// 当前激活的卡片ID
const activeId = ref(null)

// 切换卡片展开/收起
const toggleCard = (id) => {
  // 点击已展开的 → 收起
  if (activeId.value === id) {
    activeId.value = null
  } else {
    // 点击未展开的 → 展开
    activeId.value = id
  }
}

// ESC 收起所有
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') activeId.value = null
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.history-scroll {
  background-color: #f0f8f5; /* 适配原图浅青绿背景 */
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50px 20px;
  /* 基础字体 - 匹配截图中的楷体体系 */
  font-family: "楷体", "KaiTi", "STKaiti", "楷_GB2312", "Microsoft Yahei", serif;
  color: #2c4a40; /* 主文字色-深青绿 */
}

/* 主标题样式 - 完全匹配截图字体和样式 */
.main-title {
  font-size: 2.5rem; /* 匹配截图2.5rem */
  color: #2c3e50; /* 截图中的深墨色 */
  margin-bottom: 15px; /* 匹配截图15px */
  text-align: center;
  font-weight: bold; /* 加粗 */
  /* 核心字体匹配 - 优先楷体系列 */
  font-family: "楷体", "KaiTi", "STKaiti", "楷_GB2312", serif;
  position: relative;
  z-index: 10;
  padding: 10px;
  cursor: pointer;
}

.subtitle {
  font-size: 1.1rem;
  color: #6a8c80; /* 副标题浅青绿 */
  margin-bottom: 80px; /* 增加底部间距，确保不被下方元素遮挡 */
  text-align: center;
  line-height: 1.6;
  border-bottom: 2px solid #4ba88f; /* 青绿分割线 */
  padding-bottom: 15px;
  width: 60%;
  /* 副标题也使用楷体 */
  font-family: "楷体", "KaiTi", "STKaiti", "楷_GB2312", serif;
  position: relative;
  z-index: 10;
}

/* 卷轴容器 - 适配新中式风格 */
.scroll-container {
  width: 90%;
  max-width: 1400px;
  min-height: 600px;
  position: relative;
  overflow: hidden;
  /* 浅青绿纹理背景 */
  background: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%234ba88f' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E"), 
              #f8fcf9;
  border: 8px solid #d9ebe5; /* 浅青绿边框 */
  border-radius: 8px;
  box-shadow: 0 8px 25px rgba(75, 168, 143, 0.1);
  /* 默认状态 */
  width: 90%;
  opacity: 1;
  display: flex;
  align-items: stretch; /* 拉伸以填充 */
  justify-content: center; /* 水平居中 */
  margin-top: 20px; /* 增加顶部间距，确保不遮挡上方元素 */
  z-index: 1;
  padding: 40px;
}

/* 卷轴展开动画 - 仅在可见时触发 */
.scroll-container.animate {
  animation: scroll-open 3s ease-out 0.5s forwards;
  width: 0;
  opacity: 0;
}

/* 卷轴展开动画关键帧 */
@keyframes scroll-open {
  0% { 
    width: 0; 
    opacity: 0;
  }
  100% { 
    width: 90%; 
    opacity: 1;
  }
}

/* 卷轴轴头 - 青绿风格 */
.scroll-left-axis, .scroll-right-axis {
  position: absolute;
  top: 0;
  height: 100%;
  width: 25px;
  background-color: #4ba88f; /* 青绿轴头 */
  z-index: 2;
}
.scroll-left-axis {
  left: 0;
  border-radius: 5px 0 0 5px;
}
.scroll-right-axis {
  right: 0;
  border-radius: 0 5px 5px 0;
}

/* 轴头纹理 - 浅纹 */
.scroll-left-axis::after, .scroll-right-axis::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: repeating-linear-gradient(
    90deg,
    rgba(255,255,255,0.1),
    rgba(255,255,255,0.1) 5px,
    transparent 5px,
    transparent 10px
  );
}

/* 朝代图片容器 - 网格布局 占满卷轴 */
.dynasty-images {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 0;
  width: 100%;
  height: 100%;
  align-items: stretch;
  justify-content: stretch;
  align-content: stretch;
}

/* 朝代图片卡片 */
.dynasty-card {
  background-color: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(75, 168, 143, 0.15);
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 1;
  transform: translateY(0);
  border: 1px solid #e8f4f0;
  height: 100%; /* 卡片高度100% */
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 5;
  padding: 0;
  min-height: 450px;
}

/* 图片容器 */
.dynasty-img-container {
  flex: 1;
  display: flex;
  align-items: stretch;
  justify-content: stretch;
  padding: 0;
  background-color: white;
  overflow: hidden;
  border-radius: 8px 8px 0 0;
  min-height: 300px;
  aspect-ratio: 4/5;
}

.dynasty-img-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px 8px 0 0;
  object-position: center center;
}

/* 明清主题图片居中调整 */
.dynasty-card.ming .dynasty-img-container img {
  object-position: 60% center;
}

.dynasty-card.qing .dynasty-img-container img {
  object-position: 55% center;
}

/* 卡片逐个渐显 - 仅在卷轴可见时触发 */
.scroll-container.animate .dynasty-card {
  opacity: 0;
  transform: translateY(20px);
}

.scroll-container.animate .dynasty-card.tang { animation: fade-in 0.8s ease-out 3s forwards; }
.scroll-container.animate .dynasty-card.song { animation: fade-in 0.8s ease-out 3.3s forwards; }
.scroll-container.animate .dynasty-card.ming { animation: fade-in 0.8s ease-out 3.6s forwards; }
.scroll-container.animate .dynasty-card.qing { animation: fade-in 0.8s ease-out 3.9s forwards; }

/* 卡片悬浮效果 */
.dynasty-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 20px rgba(75, 168, 143, 0.25);
  border-color: #4ba88f;
}

/* 朝代图片样式 */
.dynasty-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

/* 卡片底部文字 - 也使用楷体匹配整体风格 */
.card-footer {
  padding: 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  font-family: "楷体", "KaiTi", "STKaiti", "楷_GB2312", serif;
  background-color: #f8fcf9;
  border-radius: 0 0 8px 8px;
  flex-shrink: 0;
}

.card-title {
  font-size: 2rem;
  color: #2c4a40;
  margin-bottom: 10px;
  font-weight: bold;
}

.card-desc {
  font-size: 1rem;
  color: #6a8c80;
}

/* 内容渐显动画 */
@keyframes fade-in {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

/* 详情弹窗 - 青绿风格 */
.detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(44, 74, 64, 0.7); /* 深青绿半透明 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
  padding: 20px;
}

/* 弹窗内容容器 - 左右布局 */
.modal-content {
  background-color: #fff;
  border: 1px solid #4ba88f;
  border-radius: 10px;
  width: 90%;
  max-width: 1100px; /* 加宽弹窗 */
  max-height: 85vh;
  overflow-y: auto;
  padding: 40px;
  position: relative;
  animation: modal-fade 0.5s ease-out;
  box-shadow: 0 10px 30px rgba(75, 168, 143, 0.2);
  display: grid;
  grid-template-columns: 2fr 1fr; /* 文字2份 图片1份 */
  gap: 30px;
}

/* 弹窗图片容器 */
.modal-img-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.modal-img {
  width: 100%;
  height: auto;
  max-height: 500px;
  border-radius: 8px;
  object-fit: cover;
  border: 3px solid #e8f4f0;
}

.modal-img-caption {
  margin-top: 15px;
  font-size: 0.9rem;
  color: #6a8c80;
  text-align: center;
  font-family: "楷体", "KaiTi", "STKaiti", "楷_GB2312", serif;
}

/* 弹窗文字内容容器 */
.modal-text-container {
  overflow-y: auto;
  max-height: 70vh;
  font-family: "楷体", "KaiTi", "STKaiti", "楷_GB2312", serif;
  color: #111;
  font-size: 20px;
}

/* 弹窗渐显动画 */
@keyframes modal-fade {
  0% { opacity: 0; transform: scale(0.8); }
  100% { opacity: 1; transform: scale(1); }
}

/* 关闭按钮 - 青绿风格 */
.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 1.5rem;
  color: #4ba88f;
  cursor: pointer;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f8fcf9;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid #4ba88f;
  transition: all 0.2s ease;
  z-index: 10;
}

.close-btn:hover {
  background-color: #4ba88f;
  color: #fff;
}

/* 详情内容样式 */
.detail-title {
  font-size: 2rem;
  color: #2c4a40;
  margin-bottom: 20px;
  border-bottom: 2px solid #4ba88f;
  padding-bottom: 10px;
  text-align: center;
  font-weight: bold;
}

.detail-section {
  margin: 25px 0;
}

.detail-subtitle {
  font-size: 1.3rem;
  color: #4ba88f;
  margin-bottom: 10px;
  font-weight: bold;
}

.detail-text {
  font-size: 1.05rem;
  line-height: 1.8;
  color: #3a5a50;
  margin-left: 20px;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .history-scroll {
    padding: 40px 20px;
  }
  
  .main-title {
    font-size: 2.2rem;
  }
  
  .subtitle {
    width: 80%;
    margin-bottom: 60px;
  }
  
  .scroll-container {
    min-height: 600px;
    padding: 30px;
  }
  
  .dynasty-images {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    padding: 0;
  }
  
  .dynasty-card {
    min-height: 350px;
  }
  
  .dynasty-img-container {
    min-height: 250px;
  }
  
  .card-title {
    font-size: 1.8rem;
  }
  
  /* 弹窗改为上下布局 */
  .modal-content {
    grid-template-columns: 1fr;
    max-width: 800px;
    padding: 30px;
  }
  
  .modal-img {
    max-height: 300px;
  }
}

@media (max-width: 768px) {
  .history-scroll {
    padding: 30px 15px;
  }
  
  .main-title {
    font-size: 2rem;
  }
  
  .subtitle {
    width: 90%;
    margin-bottom: 50px;
    font-size: 1rem;
  }
  
  .scroll-container {
    min-height: 500px;
    padding: 20px;
  }
  
  .dynasty-images {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .dynasty-card {
    min-height: 300px;
  }
  
  .dynasty-img-container {
    min-height: 200px;
  }
  
  .card-title {
    font-size: 1.5rem;
  }
  
  .card-desc {
    font-size: 0.9rem;
  }
  
  .modal-content {
    padding: 20px;
    width: 95%;
  }
  
  .detail-title {
    font-size: 1.6rem;
  }
  
  .detail-subtitle {
    font-size: 1.1rem;
  }
  
  .detail-text {
    font-size: 0.95rem;
    line-height: 1.6;
  }
  
  .modal-img {
    max-height: 250px;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 1.8rem;
  }
  
  .subtitle {
    width: 95%;
    margin-bottom: 40px;
    font-size: 0.9rem;
  }
  
  .scroll-container {
    min-height: 400px;
    padding: 15px;
  }
  
  .dynasty-card {
    min-height: 250px;
  }
  
  .dynasty-img-container {
    min-height: 180px;
  }
  
  .card-title {
    font-size: 1.3rem;
  }
  
  .card-desc {
    font-size: 0.8rem;
  }
  
  .modal-content {
    padding: 15px;
  }
  
  .detail-title {
    font-size: 1.4rem;
  }
  
  .detail-subtitle {
    font-size: 1rem;
  }
  
  .detail-text {
    font-size: 0.9rem;
    line-height: 1.5;
    margin-left: 10px;
  }
  
  .modal-img {
    max-height: 200px;
  }
  
  .modal-img-caption {
    font-size: 0.8rem;
  }
}



* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
}

.accordion-container {
  display: flex;
  gap: 10px;
  max-width: 1200px;
  width: 100%;
  overflow-x: auto;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.card {
  flex: 0 0 80px;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  transition: all 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
  background-size: cover;
  background-position: center;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 20px;
  color: white;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.7);
}

.card.expanded {
  flex: 0 0 600px;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, transparent 50%);
  z-index: 1;
}

.card-content {
  position: relative;
  z-index: 2;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.5s ease;
}

.card.expanded .card-content {
  opacity: 1;
  transform: translateY(0);
}

.card-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.card-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 10px;
}

.card-poem {
  font-size: 16px;
  line-height: 1.5;
  font-style: italic;
}

/* 背景图 */
.card-1 {
  background-image: url('https://images.unsplash.com/photo-1507525428034-b723cf961d3e?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=600&q=80');
}
.card-2 {
  background-image: url('https://images.unsplash.com/photo-1506929562872-bb421503ef21?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=600&q=80');
}
.card-3 {
  background-image: url('https://images.unsplash.com/photo-1441974231531-c6227db76b6e?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=600&q=80');
}
.card-4 {
  background-image: url('https://images.unsplash.com/photo-1519681393784-d120f8b8e4f9?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=600&q=80');
}

/* 响应式 */
@media (max-width: 768px) {
  .accordion-container {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }
  .card {
    flex: 0 0 60px;
    height: 120px;
    width: 100%;
  }
  .card.expanded {
    flex: 0 0 200px;
    height: auto;
  }
}
</style>