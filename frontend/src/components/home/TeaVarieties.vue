<template>
  <section id="tea-varieties" class="tea-varieties-section content-section">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">特色茶种</h2>
        <div class="section-line"></div>
        <p class="section-desc">品味广东特有的茶香韵味</p>
      </div>
      
      <!-- 茶种选择器 -->
      <div class="tea-selector">
        <div 
          v-for="tea in teas" 
          :key="tea.id"
          class="tea-selector-item"
          :class="{ 'active': selectedTeaId === tea.id }"
          @click="selectTea(tea.id)"
        >
          <div class="selector-icon">{{ tea.icon }}</div>
          <div class="selector-name">{{ tea.name }}</div>
        </div>
      </div>

      <!-- 茶种详细信息 -->
      <div class="tea-detail-container">
        <div v-for="tea in teas" :key="tea.id" v-show="selectedTeaId === tea.id" class="tea-detail">
          <!-- 左侧：详细说明 -->
          <div class="tea-info-left" v-if="tea.varieties[currentVarietyIndex]">
            <h3 class="tea-name">{{ tea.varieties[currentVarietyIndex].name }}</h3>
            <div class="tea-characteristics">
              <div class="characteristic-item">
                <span class="label">产地：</span>
                <span class="value">{{ tea.varieties[currentVarietyIndex].origin }}</span>
              </div>
              <div class="characteristic-item">
                <span class="label">冲泡方法：</span>
                <span class="value">{{ tea.varieties[currentVarietyIndex].brewing }}</span>
              </div>
              <div class="characteristic-item">
                <span class="label">口感特点：</span>
                <span class="value">{{ tea.varieties[currentVarietyIndex].taste }}</span>
              </div>
            </div>

            <!-- 历史文化背景 -->
            <div class="tea-story">
              <h4>历史文化背景</h4>
              <p class="story-content">{{ tea.varieties[currentVarietyIndex].history }}</p>
            </div>

            
          </div>

          <!-- 右侧：品种选择与展示 -->
          <div class="tea-info-right">
            <!-- 品种细分 -->
            <div class="variety-types" v-if="tea.varieties">
              <h4>品种细分</h4>
              <div class="variety-tags">
                <span v-for="(variety, index) in tea.varieties" :key="variety.name" 
                      class="variety-tag"
                      :class="{ 'active': currentVarietyIndex === index }"
                      @click="setVariety(index)">
                  {{ variety.name }}
                </span>
              </div>
            </div>

            <!-- 品种展示区域 -->
            <div class="variety-carousel" v-if="tea.varieties && tea.varieties.length > 0 && tea.varieties[currentVarietyIndex]">
              <div class="carousel-container">
                <img :src="tea.varieties[currentVarietyIndex].image" 
                     :alt="tea.varieties[currentVarietyIndex].name" 
                     class="carousel-image" 
                     :key="`${tea.id}-${currentVarietyIndex}`" />
              </div>
              <div class="carousel-indicators">
                <span v-for="(variety, index) in tea.varieties" :key="variety.name" 
                      class="indicator" 
                      :class="{ 'active': currentVarietyIndex === index }"
                      @click="setVariety(index)"></span>
              </div>
              <!-- 品种详细信息 -->
              <div class="variety-info">
                <h5 class="variety-name">温馨提示</h5>
                <p class="variety-description">{{ getVarietyDescription(tea.name, tea.varieties[currentVarietyIndex].name) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

// 茶种数据
const teas = ref([
  {
    id: 1,
    name: '绿茶',
    icon: '🍃',
    origin: '浙江、安徽、江苏等地',
    feature: '清汤绿叶，清香鲜爽',
    brewing: '80-85℃温水，冲泡1-2分钟',
    tasting: '汤色碧绿，滋味鲜爽，香气清幽',
    story: '绿茶是中国六大茶类之一，为不发酵茶。其历史可追溯至唐代，陆羽《茶经》中已有详细记载。绿茶制作工艺独特，通过杀青、揉捻、干燥等工序，保留了茶叶天然的绿色和营养成分，被誉为"茶中君子"。',
    aromas: [
      { type: '清香', name: '嫩香', intensity: 85 },
      { type: '花香', name: '兰花香', intensity: 70 },
      { type: '草香', name: '青草香', intensity: 75 }
    ],
    varieties: [
      { 
        name: '蕉岭绿茶', 
        image: '/assest/绿茶/绿1.jpg',
        origin: '广东省梅州市蕉岭县',
        brewing: '80-85℃,先润茶 3 秒，再注水至七分满，第一泡 30 秒，后续逐次延长浸泡时间，可冲泡 5-7 次',
        taste: '汤色黄绿明亮，香气清香持久，入口香醇甘滑，带有明显的高山 “山韵”，回甘悠长，耐冲泡，还有独特的富硒风味，被称为 “寿乡第一茶”',
        history: '种植历史可追溯至清康熙元年（1662 年），距今已有 300 多年，史料记载当时的黄坑茶（蕉岭绿茶的前身）就已远销南洋，是海外侨胞寄托乡愁的 “家乡味道”，如今也是蕉岭 “十大长寿食品” 之首"之首。'
      },
      { 
        name: '柏塘山茶', 
        image: '/assest/绿茶/绿2.jpg',
        origin: '广东省惠州市博罗县柏塘镇',
        brewing: '85-90℃,采用 “三才冲泡法”，第一泡 15 秒快速出汤，后续每次延长 10-15 秒，可冲泡 7-8 次',
        taste: '冲泡后汤色从清绿渐转金黄，香气带有炒米香、花蜜香的复合气息，入口顺滑醇厚，味浓甘甜，回甘强烈，茶汤带有独特的矿物质感，茶气十足',
        history: '起源可追溯至晋代，兴于唐、盛于宋，距今已有一千多年历史。传说东晋葛洪在罗浮山修道时，就曾饮用柏塘山茶；明清时期，罗浮山茶已是广东知名茶品，柏塘镇至今保留着传统的客家炒青制茶工艺，是岭南茶文化的 “活化石”'
      },
      { 
        name: '仁化白毛尖', 
        image: '/assest/绿茶/绿3.jpg',
        origin: '广东省韶关市仁化县',
        brewing: '85-90℃,先温杯，投茶后沿杯壁注水，不搅拌，让茶叶自然舒展，第一泡 40 秒，可冲泡 4-6 次，出汤时尽量沥干，避免闷熟茶叶',
        taste: '汤色清澈透亮，香气清幽如兰，入口鲜爽甘醇，舌底生津，回甘持久，茶汤滑润，带有独特的毫韵',
        history: '创制于明代，清嘉庆年间已成为宫廷贡品，是仁化 “三宝”（兰花、香菇、白毛茶）之一。其制作坚守 “自然萎凋、文火慢烘” 的古法，不炒不揉，保留了茶叶最原始的香气，至今仍是岭南名茶中的珍品'
      },
      { 
        name: '梅县绿茶', 
        image: '/assest/绿茶/绿4.jpg',
        origin: '广东省梅州市梅县区',
        brewing: '80-85℃,润茶 5 秒后注水，第一泡 30 秒出汤，后续每次延长 10 秒，可冲泡 5-6 次；陈年梅县绿茶可用 90℃水温冲泡，风味更醇厚',
        taste: '汤色黄绿明亮，香气带有明显的栗香与花香，入口醇厚甘爽，带有独特的 “高山韵”，部分陈放后的梅县绿茶茶性更温和，汤色渐深如琥珀，兼具品饮与收藏价值',
        history: '梅州客家种茶历史可追溯至魏晋时期，梅县绿茶的炒青工艺在明末清初已成熟，其中清凉山绿茶更是梅州客家地区的百年名茶。客家人南迁时，将炒茶工艺带到粤东，发明了 “客家炒绿”，突破了绿茶不能久存的局限，是客家生存智慧与茶文化的结合'
      },
      { 
        name: '乐昌白毛茶', 
        image: '/assest/绿茶/绿5.jpg',
        origin: '广东省韶关市乐昌市九峰山',
        brewing: '85℃左右,温杯投茶，润茶 3 秒后注水，第一泡 40 秒出汤，优质白毛茶可冲泡 8-10 次仍保持风味；若用九峰泉水冲泡，风味更佳',
        taste: '汤色金黄透亮，香气高长持久，带有兰花香、蜜香的复合型香气，入口鲜爽甘醇，岩韵明显，回甘持久，耐泡性极强，春茶茶多酚含量高，鲜爽感突出',
        history: '种植历史超过 1200 年，唐代陆羽在广州任职时，就曾专程到乐昌考究韶州茶（即乐昌白毛茶）；清代时，沿溪山白毛茶被列为朝廷贡品。八成为手工制作，保留了原始的绿茶工艺，是岭南高山茶的代表'
      }
    ]
  },
  {
    id: 2,
    name: '红茶',
    icon: '🍂',
    origin: '福建、云南、安徽等地',
    feature: '红汤红叶，醇厚甘甜',
    brewing: '90-95℃热水，浸泡3-5分钟',
    tasting: '汤色红艳明亮，滋味醇厚回甘',
    story: '红茶是全发酵茶，起源于福建武夷山。17世纪传入欧洲后，迅速成为世界范围内最受欢迎的茶类之一。红茶制作经过萎凋、揉捻、发酵、干燥等工序，茶叶中的茶多酚氧化后形成独特的红色茶汤和甜醇口感。',
    aromas: [
      { type: '甜香', name: '焦糖香', intensity: 80 },
      { type: '花香', name: '玫瑰香', intensity: 70 },
      { type: '果香', name: '桂圆香', intensity: 65 }
    ],
    varieties: [
      { 
        name: '象窝红茶', 
        image: '/assest/红茶/红1.jpg',
        origin: '广东省云浮市新兴县',
        brewing: '90-95℃,温杯后投茶，快速润茶 3 秒，第一泡 10 秒出汤，后续每次延长 5-10 秒，优质象窝红茶可冲泡 7-10 次，尾水仍有明显甜香',
        taste: '冲泡后汤色橙红透亮，香气以浓郁的蜜香、花果香为主，部分带有独特的山韵；入口鲜醇浓爽，几乎无苦涩感，回甘迅速，茶氨酸含量远高于普通红茶，口感清甜顺滑，耐泡性极强',
        history: '新兴象窝山自唐代就有种茶记载，当时的 “水源山” 茶常被用作药引，距今已有千年植茶历史。这里是禅宗六祖惠能的故乡，茶文化与禅文化深度融合，象窝茶也被赋予了 “禅茶一味” 的文化内涵；如今作为广东十大名茶之一，它也是新兴长寿文化与生态农业的标志性名片'
      },
      { 
        name: '鹤山红茶', 
        image: '/assest/红茶/红2.jpg',
        origin: '广东省江门市鹤山市',
        brewing: '90-95℃,传统功夫泡法：温杯投茶，第一泡 15 秒出汤，后续逐次延长浸泡时间；也可采用焖泡法，用 90℃水焖泡 3-5 分钟，风味更醇厚',
        taste: '干茶条索紧细乌润，冲泡后茶汤红润透亮，边缘带有明显 “黄金圈”；香气以桂圆甜香、花果香为主，随着冲泡次数增加，会逐渐展现出优雅的果韵；入口爽滑甘醇，几乎无苦涩感，甜感突出，回甘持久，是典型的 “广式甜红茶” 风味',
        history: '种茶历史可追溯至宋元时期，距今已有 700 多年，民间流传 “未有鹤山县，先有鹤山茶” 的说法。明清时期，鹤山红茶通过海上丝绸之路远销南洋，成为侨乡华侨寄托乡愁的 “家乡味道”；如今它的传统制作工艺已被列入非遗名录，是侨乡茶文化的活化石'
      },
      { 
        name: '开平大沙茶', 
        image: '/assest/红茶/红3.jpg',
        origin: '广东省江门市开平市大沙镇',
        brewing: '90-95℃,温杯投茶，润茶 3 秒后注水，第一泡 15 秒出汤，后续每次延长 10 秒，高山红茶耐泡性强，可冲泡 6-8 次；也可用玻璃杯焖泡，感受茶汤从浅橙到深红的变化',
        taste: '高山红茶干茶肥壮带金毫，冲泡后汤色橙红明亮，香气带有明显的山野花香与蜜韵；入口滋味醇厚饱满，苦涩味极轻，转化快，回甘生津明显，尾水依然清甜，带有独特的高山矿物质气息，是典型的高山红茶风味',
        history: '种茶历史可追溯至唐代，禅宗六祖惠能在天露山弘法期间，将茶与禅修结合，形成了独特的 “禅茶一味” 传统，至今大沙镇仍保留着千余棵古茶树。大沙茶制作技艺已传承超百年，是侨乡开平的代表性特产，如今也是 “侨都茶香小镇” 的核心名片'
      }
    ]
  },
  {
    id: 3,
    name: '青茶',
    icon: '☕',
    origin: '福建、广东、台湾等地',
    feature: '绿叶红镶边，香气馥郁',
    brewing: '100℃沸水，功夫茶冲泡',
    tasting: '汤色金黄，香气悠长，滋味醇厚',
    story: '乌龙茶属于半发酵茶，介于绿茶和红茶之间。其制作工艺复杂精细，包含萎凋、摇青、杀青、揉捻、烘焙等多道工序。乌龙茶讲究"闻香品韵"，冲泡时香气四溢，品饮时回甘生津，是中国茶文化的瑰宝。',
    aromas: [
      { type: '花香', name: '兰花香', intensity: 85 },
      { type: '果香', name: '果香', intensity: 75 },
      { type: '奶香', name: '奶油香', intensity: 60 }
    ],
    varieties: [
      { 
        name: '凤凰单丛', 
        image: '/assest/乌龙茶/乌龙1.jpg',
        origin: '广东省潮州市潮安区凤凰镇',
        brewing: '100℃沸水，采用潮汕功夫茶冲泡法，前 3 泡快进快出（3-5 秒出汤），后续逐泡延长浸泡时间，优质单丛可冲泡 10-15 次，杯底留香持久',
        taste: '条索紧结重实，色泽乌褐油润；茶汤金黄明澈，香气以天然花果香为主（如蜜兰香、鸭屎香、桂花香等），香型多达上百种；入口香醇顺滑，喉底回甘鲜爽，带有独特的 “山韵”，饮毕杯底留香，有 “绕鼻三日不绝” 的美誉',
        history: '种植历史超千年，畲族先民是最早的种茶者；南宋末年宋帝南逃时饮此茶，赐名 “宋茶”，现存树龄超 600 年的 “宋种” 母树；清代同治、光绪年间确立 “单株采制、单株制作” 的工艺体系，明清时期已成为贡品，是潮汕功夫茶文化的核心载体'
      },
      { 
        name: '岭头单丛', 
        image: '/assest/乌龙茶/乌龙2.jpg',
        origin: '广东省潮州市饶平县浮滨镇岭头村',
        brewing: '95-100℃,前两泡快冲快出（5 秒内出汤），后续每次延长 10 秒，可冲泡 8-10 次；焖泡法可将茶投入壶中，用沸水焖泡 3-5 分钟，更能激发蜜韵甜香',
        taste: '汤色橙黄明亮，香气以浓烈的蜜糖甜香为主，夹杂优雅的兰花香；入口醇厚甘爽，几乎无苦涩感，回甘生津强烈，尾水依然清甜，是典型的 “蜜韵兰香” 风格',
        history: '发源于饶平岭头村，种植历史超 300 年；1981 年被广东省农业厅列为独立茶树品种，1988 年被定为省级良种，2002 年被审定为国家茶树良种；作为饶平的标志性特产，它承载着潮汕侨乡的茶文化记忆，也是当地茶农世代传承的工艺结晶'
      },
      { 
        name: '石古坪乌龙', 
        image: '/assest/乌龙茶/乌龙3.jpg',
        origin: '广东省潮州市潮安区凤凰镇石古坪畲族村',
        brewing: '95-100℃,前 3 泡 5-10 秒快速出汤，后续逐泡延长，可冲泡 7-10 次；冲泡时需避免长时间闷泡，防止香气流失',
        taste: '汤色黄绿清澈，香气清高悠长，带有清新的茉莉花香；滋味鲜醇爽口，回甘明显，叶底嫩绿柔软，叶边呈 “一线红”，是轻发酵乌龙茶的典型风味',
        history: '种植历史超百年，由当地畲族茶农培育而成，至今仍保留着传统的 “骑马式采茶法” 和摇青工艺；石古坪村是畲族聚居地，乌龙茶制作技艺是畲族茶文化的重要组成部分，产量稀少，是凤凰乌龙中的小众珍品'
      }
    ]
  }
])

// 选中的茶种
const selectedTeaId = ref(1)

// 当前品种索引
const currentVarietyIndex = ref(0)

// 图片加载状态
const imageLoaded = ref(false)

// 轮播定时器
let carouselTimer = null

// 选择茶种
const selectTea = (id) => {
  selectedTeaId.value = id
  currentVarietyIndex.value = 0
  imageLoaded.value = false
  startCarousel()
}

// 设置品种
const setVariety = (index) => {
  currentVarietyIndex.value = index
  imageLoaded.value = false
  resetCarouselTimer()
}

// 图片加载完成
const onImageLoad = () => {
  imageLoaded.value = true
}

// 图片加载失败
const onImageError = (e) => {
  imageLoaded.value = true
  console.warn('图片加载失败:', e.target.src)
}

// 开始轮播
const startCarousel = () => {
  if (carouselTimer) {
    clearInterval(carouselTimer)
  }
  carouselTimer = setInterval(() => {
    const currentTea = teas.value.find(tea => tea.id === selectedTeaId.value)
    if (currentTea) {
      currentVarietyIndex.value = (currentVarietyIndex.value + 1) % currentTea.varieties.length
    }
  }, 15000)
}

// 重置轮播定时器
const resetCarouselTimer = () => {
  startCarousel()
}

// 获取品种详细描述
const getVarietyDescription = (teaName, varietyName) => {
  const descriptions = {
    '绿茶': {
      '蕉岭绿茶': '蕉岭绿茶对湿热体质更友好，但脾胃虚寒者慎饮哦~',
      '柏塘山茶': '柏塘山茶在午饭后喝解腻效果最好',
      '仁化白毛尖': '仁化白毛尖适合怕热、易上火体质，胃寒者慎饮	',
      '梅县绿茶': '梅县绿茶的陈年款秋冬午后喝更舒服，新茶建议春夏饮用',
      '乐昌白毛茶': '乐昌白毛茶适合湿热体质，但是胃寒、孕妇慎饮'
    },
    '红茶': {
      '象窝红茶': '象窝红茶在上午 / 午后喝最佳，不要空腹、不要睡前喝；禅茶风味适合搭配清淡点心哦',
      '鹤山红茶': '鹤山红茶在午餐后喝解腻效果拉满，搭配广式烧腊、煲仔饭绝配！',
      '开平大沙茶': '开平大沙茶午后或傍晚喝都可，不影响睡眠，老人小孩也能适量喝'
    },
    '青茶': {
      '凤凰单丛': '凤凰单丛上午 / 午后喝最佳，睡前 4 小时不建议喝；高香款容易刺激肠胃，建议搭配点心',
      '岭头单丛': '午饭后喝解腻效果拉满，蜜韵甜香适合搭配潮汕点心',
      '石古坪乌龙': '适合怕热、易上火体质，脾胃虚寒者慎饮'
    }
  }
  
  return descriptions[teaName]?.[varietyName] || `${varietyName}是${teaName}的优质品种，具有独特的香气和滋味。`
}

// 组件挂载时开始轮播
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  startCarousel()
})

onUnmounted(() => {
  if (carouselTimer) {
    clearInterval(carouselTimer)
  }
})
</script>

<style scoped>
/* 特色茶种样式 */
.tea-varieties-section {
  padding: 80px 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f5e9 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  text-align: center;
  margin-bottom: 50px;
}

.section-title {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 15px;
  font-family: "STKaiti", "KaiTi", "楷体", serif;
}

.section-line {
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #4db6ac, #789F6F);
  margin: 0 auto 15px;
  border-radius: 2px;
}

.section-desc {
  color: #789F6F;
  font-size: 1.1rem;
}

/* 茶种选择器 */
.tea-selector {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 40px;
}

.tea-selector-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 30px;
  background: white;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.tea-selector-item:hover,
.tea-selector-item.active {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(77, 182, 172, 0.3);
  border: 2px solid #4db6ac;
}

.selector-icon {
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.selector-name {
  font-size: 1.2rem;
  font-weight: 600;
  color: #2c3e50;
}

/* 茶种详情 */
.tea-detail-container {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  max-width: 1160px;
  margin: 0 auto;
}

.tea-detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  align-items: start;
}

/* 左侧：详细说明 */
.tea-info-left {
  background: #fafbfb;
  border-radius: 16px;
  padding: 30px;
}

.tea-name {
  font-size: 2rem;
  color: #2c3e50;
  margin-bottom: 25px;
  font-family: "STKaiti", "KaiTi", "楷体", serif;
  padding-bottom: 15px;
  border-bottom: 2px solid #e8f5e9;
}

.tea-characteristics {
  margin-bottom: 30px;
}

.characteristic-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding: 12px 15px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.characteristic-item .label {
  font-weight: 600;
  color: #4db6ac;
  min-width: 90px;
  font-size: 0.95rem;
}

.characteristic-item .value {
  color: #555;
  font-size: 0.95rem;
  line-height: 1.5;
}

/* 趣味故事 */
.tea-story {
  background: white;
  padding: 25px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.tea-story h4 {
  margin-bottom: 15px;
  color: #2c3e50;
  font-size: 1.1rem;
  font-weight: 600;
}

.story-content {
  color: #555;
  line-height: 1.7;
  font-size: 0.95rem;
  text-align: justify;
}

/* 香气特征 */
.aroma-chart {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.aroma-chart h4 {
  margin-bottom: 15px;
  color: #2c3e50;
  font-size: 1.1rem;
  font-weight: 600;
}

.aroma-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.aroma-tag {
  background: linear-gradient(90deg, #4db6ac, #789F6F);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.aroma-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(77, 182, 172, 0.4);
}

/* 右侧：品种选择与展示 */
.tea-info-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 品种细分 */
.variety-types {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
}

.variety-types h4 {
  margin-bottom: 15px;
  color: #2c3e50;
  font-size: 1rem;
  font-weight: 600;
}

.variety-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.variety-tag {
  background: white;
  padding: 7px 16px;
  border-radius: 20px;
  border: 2px solid #4db6ac;
  color: #4db6ac;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.variety-tag:hover,
.variety-tag.active {
  background: linear-gradient(90deg, #4db6ac, #789F6F);
  color: white;
  border-color: transparent;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(77, 182, 172, 0.35);
}

/* 品种展示区域 */
.variety-carousel {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.carousel-container {
  width: 100%;
  aspect-ratio: 4/3;
  overflow: hidden;
  border-radius: 12px;
  background: #f0f2f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 0.4s ease;
}

.carousel-container:hover .carousel-image {
  transform: scale(1.02);
}

.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ddd;
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator.active {
  background: #4db6ac;
  transform: scale(1.2);
}

/* 品种详细信息 */
.variety-info {
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.variety-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.variety-name::before {
  content: '🌿';
  font-size: 1.2rem;
}

.variety-description {
  color: #666;
  line-height: 1.6;
  font-size: 0.9rem;
}



/* 响应式布局 */
@media (max-width: 768px) {
  .tea-varieties-section {
    padding: 40px 0;
  }
  
  .container {
    padding: 0 15px;
  }
  
  .section-header {
    margin-bottom: 30px;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .tea-selector {
    flex-direction: column;
    align-items: center;
    gap: 15px;
  }
  
  .tea-selector-item {
    width: 100%;
    max-width: 250px;
    padding: 15px 20px;
  }
  
  .tea-detail-container {
    padding: 20px;
  }
  
  .tea-detail {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .tea-info-left {
    padding: 20px;
  }
  
  .tea-name {
    font-size: 1.5rem;
  }
  
  .characteristic-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .characteristic-item .label {
    min-width: auto;
    margin-bottom: 0;
  }
  
  .tea-story,
  .aroma-chart,
  .variety-types {
    padding: 18px;
  }
  
  .aroma-tags {
    justify-content: center;
  }
  
  .variety-tags {
    justify-content: center;
  }
  
  .variety-info {
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .section-title {
    font-size: 1.8rem;
  }
  
  .section-desc {
    font-size: 1rem;
  }
  
  .tea-selector-item {
    max-width: 200px;
    padding: 12px 15px;
  }
  
  .selector-icon {
    font-size: 2rem;
  }
  
  .selector-name {
    font-size: 1rem;
  }
  
  .tea-detail-container {
    padding: 15px;
  }
  
  .tea-name {
    font-size: 1.3rem;
  }
  
  .carousel-container {
    aspect-ratio: 4/3;
  }
  
  .variety-info {
    padding: 12px;
  }
  
  .variety-name {
    font-size: 1rem;
  }
  
  .variety-description {
    font-size: 0.85rem;
  }
}
</style>