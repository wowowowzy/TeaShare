<template>
  <section id="tea-making" class="tea-making-section content-section section-reverse">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">制茶有道· 品茶有韵</h2>
        <div class="section-line"></div>
        <p class="section-desc">手作茶青韵，杯藏岁月香</p>
        
        <!-- 茶种选择控件 -->
        <div class="tea-type-selector">
          <div 
            v-for="teaType in teaTypes" 
            :key="teaType.value"
            class="tea-type-item"
            :class="{ 'active': selectedTeaType === teaType.value }"
            @click="selectTeaType(teaType.value)"
          >
            <!-- <div class="tea-icon">{{ teaType.icon }}</div> -->
            <span>{{ teaType.label }}</span>
          </div>
        </div>
      </div>
      
      <!-- 制茶工艺流程图 - 图片形式 -->
      <div class="process-flowchart-image">
        <div class="flow-image-container">
          <!-- 背景图片 -->
          <div class="flow-background">
            <img src="/assest/producttea1.png" alt="制茶工艺流程" />
          </div>
          
          <!-- 步骤标记点 -->
          <div class="flow-steps">
            <div 
              v-for="(step, index) in makingSteps" 
              :key="step.id" 
              class="flow-step-point"
              :style="getStepPosition(index)"
              @click="selectMakingStep(index)"
              :class="{ 'active': currentMakingStep === index }"
            >
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-name">{{ step.name }}</div>
            </div>
          </div>
          
        </div>
      </div>

      <!-- 制茶步骤详情 -->
      <div class="making-detail">
        <transition name="fade-slide" mode="out-in">
          <div :key="currentMakingStep" class="step-detail">
            <div class="step-header">
              <h3>{{ makingSteps[currentMakingStep].name }}</h3>
              <div class="step-time">耗时：{{ makingSteps[currentMakingStep].time }}</div>
            </div>
            
            <div class="step-content-grid">
              <!-- 左侧：步骤描述 -->
              <div class="step-description">
                <h4>详细说明</h4>
                <p class="step-purpose"><strong>目的：</strong>{{ makingSteps[currentMakingStep].purpose }}</p>
                
                <!-- 发酵步骤显示对比表格 -->
                <div v-if="makingSteps[currentMakingStep].name === '发酵'" class="fermentation-table">
                  <h5>发酵茶类对比：</h5>
                  <table class="tea-comparison-table">
                    <thead>
                      <tr>
                        <th>茶类</th>
                        <th>发酵度</th>
                        <th>工艺</th>
                        <th>时间</th>
                        <th>特征</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td>绿茶</td>
                        <td>0%</td>
                        <td>不发酵</td>
                        <td>0</td>
                        <td>清汤绿叶</td>
                      </tr>
                      <tr>
                        <td>黄茶</td>
                        <td>10-20%</td>
                        <td>闷黄</td>
                        <td>2-3天</td>
                        <td>黄汤黄叶</td>
                      </tr>
                      <tr>
                        <td>白茶</td>
                        <td>20-30%</td>
                        <td>微发酵</td>
                        <td>2-3天</td>
                        <td>白毫显露</td>
                      </tr>
                      <tr>
                        <td>青茶</td>
                        <td>30-70%</td>
                        <td>做青</td>
                        <td>4-8小时</td>
                        <td>绿叶红边</td>
                      </tr>
                      <tr>
                        <td>红茶</td>
                        <td>80-90%</td>
                        <td>全发酵</td>
                        <td>3-6小时</td>
                        <td>红汤红叶</td>
                      </tr>
                      <tr>
                        <td>黑茶</td>
                        <td>100%</td>
                        <td>后发酵</td>
                        <td>数月至年</td>
                        <td>陈香醇厚</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                
                <div v-else class="step-requirements">
                  <h5>具体要求：</h5>
                  <ul>
                    <li v-for="(req, reqIndex) in makingSteps[currentMakingStep].requirements" :key="reqIndex">{{ req }}</li>
                  </ul>
                </div>
                
                <div class="step-standard">
                  <h5>判断标准：</h5>
                  <p>{{ makingSteps[currentMakingStep].standard }}</p>
                </div>
              </div>

              <!-- 右侧：图片展示 -->
              <div class="step-visual">
                <div class="step-image">
                  <img :src="makingSteps[currentMakingStep].image" :alt="makingSteps[currentMakingStep].name" />
                </div>
                <div v-if="makingSteps[currentMakingStep].tips" class="step-tips">
                  <h5>💡 小贴士</h5>
                  <p>{{ makingSteps[currentMakingStep].tips }}</p>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- 制茶大师访谈 -->
      <div class="master-interview">
        <h3>🎥 品茶大师访谈</h3>
        <div class="video-container">
          <div class="video-placeholder" @click="loadAndPlayVideo">
            <img v-if="!isPlayerVisible" src="/assest/视频封面.png" alt="制茶大师访谈" class="video-cover" />
            <video v-else ref="videoElement" controls style="width: 100%; border-radius: 16px;" preload="metadata">
              <source :src="currentVideoSrc" type="video/mp4">
              您的浏览器不支持视频播放。
            </video>
            <div v-if="!isPlayerVisible" class="video-overlay">
              <div class="video-icon">▶</div>
              <p>点击观看品茶大师展示如何喝茶</p>
            </div>
          </div>
        </div>
        <div class="master-info">
          <div class="master-avatar">
            <img src="/assest/人物头像.png" alt="制茶大师" />
          </div>
          <div class="master-details">
            <h4>王琼 中国茶文化推广的先行者</h4>
            <p>和静园品牌创始人、和静茶修学堂创始，被誉为 "中国第一女茶人"</p>
            <div class="master-quote">
              "借一杯茶，把自己修成拥有幸福能力的人"
            </div>
          </div>
          <button class="tea-set-3d-button" @click="goToTradition">
            <span class="button-icon"></span>
            <span class="button-text">点击跳转到3D茶具进行沉浸式体验</span>
          </button>
        </div>
      </div>
    </div>
    

  </section>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const goToTradition = () => {
  router.push('/tradition')
}

// 茶种选择
const teaTypes = [
  { value: 'green', label: '绿茶', icon: '' },
  { value: 'black', label: '红茶', icon: '' },
  { value: 'oolong', label: '青茶', icon: '' }
]

const selectedTeaType = ref('green')

// 视频相关状态
const videoSource = ref('https://java-web-itheima-zy.oss-cn-beijing.aliyuncs.com/TopVideo.mp4')
const currentVideoSrc = ref('')// 初始为空！不加载
const isPlayerVisible = ref(false)
const videoElement = ref(null)

// // 切换视频播放状态
// function loadAndPlayVideo() {
//   // 立即显示视频播放器
//   isPlayerVisible.value = true
//   currentVideoSrc.value = videoSource // 点击才赋值
//   // // 等待2秒后自动播放
//   // setTimeout(() => {
//   //   if (videoElement.value) {
//   //     videoElement.value.play().catch(err => {
//   //       console.error('自动播放失败:', err)
//   //     })
//   //   }
//   // }, 1000)
// }

  // 加载并播放视频（修复播放逻辑）
const loadAndPlayVideo = () => {
  if (isPlayerVisible.value) return // 防止重复点击

  // 1. 显示播放器
  isPlayerVisible.value = true
  // 2. 赋值视频路径（确保DOM更新后再加载）
  setTimeout(() => {
    currentVideoSrc.value = videoSource.value
    // 3. 等待视频加载元数据后尝试播放（兼容浏览器自动播放策略）
    if (videoElement.value) {
      // 监听加载完成事件
      videoElement.value.onloadedmetadata = () => {
        // 尝试播放（用户交互后播放，符合浏览器策略）
        videoElement.value.play().catch(err => {
          console.warn('视频自动播放失败（需用户手动点击播放）:', err)
          // 失败时不报错，让用户手动点击控件播放
        })
      }
      // 手动触发加载
      videoElement.value.load()
    }
  }, 100) // 缩短延迟，提升体验
}


// 组件销毁时清理
onUnmounted(() => {
  if (videoElement.value) {
    videoElement.value.pause()
    videoElement.value.src = ''
    videoElement.value.load() // 释放资源
  }
  isPlayerVisible.value = false
  currentVideoSrc.value = ''
})

// 绿茶制作步骤数据
const greenTeaSteps = [
  {
    id: 1,
    name: '采茶',
    time: '春季最佳（明前、雨前）',
    purpose: '采摘新鲜嫩芽，保证茶叶品质。通过科学的采摘标准，确保茶叶嫩度、鲜度和完整性。',
    requirements: [
      '时间：清晨至上午10点，避免露水未干',
      '天气：晴天、无露水，避免雨天采摘',
      '手法：手采、提手采，避免指甲掐断',
      '嫩度标准：一芽一叶、一芽二叶',
      '采摘等级：特级（单芽）、一级（一芽一叶）、二级（一芽二叶）'
    ],
    standard: '芽叶完整，色泽鲜绿，无病虫害，无机械损伤',
    image: '/assest/绿茶制作/采茶.png',
    tips: '采摘时要用手指轻轻折断，不要用指甲掐断，以免损伤茶芽'
  },
  {
    id: 2,
    name: '杀青',
    time: '2-5分钟',
    purpose: '破坏酶活性，固定品质，散发青草气，形成香气。终止酶促氧化，保持茶叶色泽。',
    requirements: [
      '温度：220-280℃',
      '方法：炒青（铁锅/滚筒杀青机）、蒸青、烘青',
      '手工锅炒：传统技艺，香气独特',
      '机械滚筒：效率高，适合大规模生产',
      '时间控制：根据茶叶嫩度和含水量调整'
    ],
    standard: '叶色由鲜绿转为暗绿，叶质由柔软变为稍硬，香气由青气转为清香',
    image: '/assest/绿茶制作/杀青.png',
    tips: '杀青是绿茶制作的关键工序，要严格控制温度和时间'
  },
  {
    id: 3,
    name: '揉捻',
    time: '30-60分钟',
    purpose: '塑造外形，破坏细胞，促进内含物浸出。形成茶叶的条索形状。',
    requirements: [
      '方法：手工揉捻（推揉、团揉、抓揉）、机械揉捻',
      '压力控制：轻揉、中揉、重揉',
      '掌握轻重缓急原则：先轻后重，逐步加压',
      '保持茶叶完整性：避免过度破碎'
    ],
    standard: '条索紧结，茶汁充分溢出，叶色油润，细胞破碎率适中',
    image: '/assest/绿茶制作/揉捻.png',
    tips: '揉捻力度要适中，过重会碎茶，过轻则茶汁溢出不足'
  },
  {
    id: 4,
    name: '烘干',
    time: '20-40分钟',
    purpose: '去除水分，固定品质，便于储存，发展香气。降低含水量至安全储存标准。',
    requirements: [
      '方法：烘干（低温慢烘、高温快烘）、炒干（锅炒、滚筒）',
      '温度控制：初干100-120℃、复干80-100℃、足干60-80℃',
      '摊放均匀：确保受热均匀',
      '分次干燥：先高温快干，后低温慢烘'
    ],
    standard: '茶叶干燥，手捏成粉，香气浓郁，含水量≤6%',
    image: '/assest/绿茶制作/烘干.jpg',
    tips: '干燥要分两次进行，先高温快干去除大部分水分'
  }
]

// 红茶制作步骤数据
const blackTeaSteps = [
  {
    id: 1,
    name: '采茶',
    time: '春季最佳',
    purpose: '采摘新鲜嫩芽，保证茶叶品质。选择适合红茶制作的茶树品种。',
    requirements: [
      '时间：清晨至上午10点',
      '天气：晴天采摘',
      '手法：手采、提手采',
      '嫩度标准：一芽二叶、一芽三叶',
      '选择大叶种茶树鲜叶'
    ],
    standard: '芽叶完整，色泽鲜绿，无病虫害',
    image: '/assest/红茶制作/采茶1.png',
    tips: '红茶制作对鲜叶要求较高，需选择成熟度适中的鲜叶'
  },
  {
    id: 2,
    name: '挑拣',
    time: '10-20分钟',
    purpose: '去除杂质、老叶、病叶，保证原料纯净度。',
    requirements: [
      '去除老梗、老叶',
      '剔除病虫叶、破损叶',
      '去除非茶类夹杂物',
      '分级筛选'
    ],
    standard: '原料纯净，无杂质，叶片完整',
    image: '/assest/红茶制作/挑拣.png',
    tips: '挑拣是保证红茶品质的重要环节'
  },
  {
    id: 3,
    name: '萎凋',
    time: '6-12小时',
    purpose: '散失水分（30-40%），促进酶活性，形成香气前体物质。',
    requirements: [
      '日光萎凋：香气高，但依赖天气',
      '室内自然萎凋：稳定但时间长',
      '摊放厚度：3-5厘米，保持通风',
      '适时翻动：确保均匀失水'
    ],
    standard: '手感叶质柔软，叶色由鲜绿转为暗绿，青草气减退',
    image: '/assest/红茶制作/萎凋.png',
    tips: '萎凋程度要根据天气和茶叶品种灵活掌握'
  },
  {
    id: 4,
    name: '揉捻',
    time: '30-60分钟',
    purpose: '塑造外形，破坏细胞，促进内含物浸出，为发酵创造条件。',
    requirements: [
      '方法：手工揉捻、机械揉捻（桶式、盘式）',
      '压力控制：先轻后重',
      '保持茶叶完整性'
    ],
    standard: '条索紧结，茶汁充分溢出，细胞破碎率适中',
    image: '/assest/红茶制作/揉捻.png',
    tips: '揉捻力度要适中，过重会产生碎末'
  },
  {
    id: 5,
    name: '发酵',
    time: '3-6小时',
    purpose: '通过酶促氧化，形成红茶特有的红色汤色和香气。',
    requirements: [
      '温度：25-30℃',
      '湿度：90%以上',
      '厚度：8-12厘米',
      '翻拌：根据发酵程度适时翻拌'
    ],
    standard: '叶色呈铜红色，青草气消失，花果香显现',
    image: '/assest/红茶制作/发酵.png',
    tips: '发酵是红茶制作的灵魂，要严格控制温湿度'
  },
  {
    id: 6,
    name: '干燥',
    time: '20-40分钟',
    purpose: '停止发酵，固定品质，便于储存，发展香气。',
    requirements: [
      '方法：烘干、炒干',
      '温度控制：初干100-120℃、复干80-100℃',
      '分次干燥：先高温快干，后低温慢烘'
    ],
    standard: '茶叶干燥，手捏成粉，含水量≤6%',
    image: '/assest/红茶制作/干燥.png',
    tips: '干燥要彻底，否则容易返潮'
  }
]

// 乌龙茶制作步骤数据
const oolongTeaSteps = [
  {
    id: 1,
    name: '采摘',
    time: '春季、秋季',
    purpose: '采摘成熟度适中的鲜叶，保证乌龙茶特有的香气和口感。',
    requirements: [
      '时间：清晨采摘',
      '嫩度标准：一芽二叶至三叶',
      '选择适制乌龙茶的茶树品种',
      '避免雨天采摘'
    ],
    standard: '芽叶完整，色泽油绿，无病虫害',
    image: '/assest/乌龙茶制作/采摘.png',
    tips: '乌龙茶对鲜叶成熟度要求较高'
  },
  {
    id: 2,
    name: '晒青',
    time: '30-60分钟',
    purpose: '散失部分水分，激活酶活性，促进香气形成。',
    requirements: [
      '温度：阳光充足但避免暴晒',
      '摊放：均匀摊放，厚度2-3厘米',
      '翻动：每隔10-15分钟翻动一次',
      '程度：叶面光泽消失，青草气减退'
    ],
    standard: '叶色变暗绿，叶质柔软，青草气减弱',
    image: '/assest/乌龙茶制作/晒青.png',
    tips: '晒青时间要根据阳光强度灵活掌握'
  },
  {
    id: 3,
    name: '晾青',
    time: '1-2小时',
    purpose: '使鲜叶水分重新分布均匀，恢复叶张弹性。',
    requirements: [
      '环境：阴凉通风处',
      '摊放：厚度适当，保持通风',
      '时间：根据鲜叶状态调整'
    ],
    standard: '叶张恢复弹性，水分分布均匀',
    image: '/assest/乌龙茶制作/晾青.png',
    tips: '晾青是做青的准备阶段，不可忽视'
  },
  {
    id: 4,
    name: '做青',
    time: '4-8小时',
    purpose: '通过摇青和静置交替，促进香气形成和叶边缘红变。',
    requirements: [
      '摇青次数：3-5次',
      '摇青强度：由轻到重',
      '静置时间：逐步延长',
      '环境：温度22-26℃，湿度70-80%'
    ],
    standard: '叶边缘红变，呈绿叶红边状态，花香浓郁',
    image: '/assest/乌龙茶制作/做青.png',
    tips: '做青是乌龙茶香气形成的关键工序'
  },
  {
    id: 5,
    name: '杀青',
    time: '2-5分钟',
    purpose: '破坏酶活性，固定品质，终止做青过程。',
    requirements: [
      '温度：220-280℃',
      '方法：炒青、滚简杀青',
      '时间：根据茶叶状态调整'
    ],
    standard: '叶色由黄绿转为墨绿，青草气消失',
    image: '/assest/乌龙茶制作/杀青.png',
    tips: '杀青要及时，防止过度发酵'
  },
  {
    id: 6,
    name: '揉捻',
    time: '20-40分钟',
    purpose: '塑造外形，破坏细胞，促进内含物浸出。',
    requirements: [
      '方法：手工揉捻、机械揉捻',
      '压力：先轻后重',
      '保持茶叶完整性'
    ],
    standard: '条索紧结，茶汁充分溢出',
    image: '/assest/乌龙茶制作/揉捻.png',
    tips: '乌龙茶揉捻程度适中即可'
  },
  {
    id: 7,
    name: '烘焙',
    time: '30-60分钟',
    purpose: '干燥茶叶，发展香气，形成乌龙茶特有风味。',
    requirements: [
      '温度：初烘100-120℃、复烘60-80℃',
      '方法：炭火烘焙、机械烘焙',
      '分次烘焙：先高温快干，后低温慢烘',
      '摊放：均匀摊放，厚度适中'
    ],
    standard: '茶叶干燥，手捏成粉，香气浓郁持久',
    image: '/assest/乌龙茶制作/烘培.png',
    tips: '烘焙是乌龙茶香气形成的重要环节'
  }
]

// 根据茶种获取制作步骤
const makingSteps = computed(() => {
  switch (selectedTeaType.value) {
    case 'green':
      return greenTeaSteps
    case 'black':
      return blackTeaSteps
    case 'oolong':
      return oolongTeaSteps
    default:
      return greenTeaSteps
  }
})

// 当前选中的制茶步骤
const currentMakingStep = ref(0)

// 选择制茶步骤
const selectMakingStep = (index) => {
  currentMakingStep.value = index
}

// 选择茶种
const selectTeaType = (teaType) => {
  selectedTeaType.value = teaType
  currentMakingStep.value = 0
}

// 不同茶种的步骤点位坐标（基于容器百分比）
const stepPointsMap = {
  green: [
    { x: 26, y: 40 },   // 1. 采茶
    { x: 39, y: 40 },   // 2. 杀青
    { x: 58, y: 40 },   // 3. 揉捻
    { x: 72.5, y:40 }    // 4. 烘干
  ],
  black: [
    { x: 17, y: 80 },   // 1. 采茶
    { x: 26, y: 35 },   // 2. 挑拣
    { x: 38.5, y: 35},   // 3. 萎凋
    { x: 59, y: 35 },   // 4. 揉捻
    { x: 72.5, y: 35 },   // 5. 发酵
    { x: 82, y: 80 }    // 6. 干燥
  ],
  oolong: [
    { x: 17, y: 80 },   // 1. 采摘
    { x: 26, y: 38 },   // 2. 晒青
    { x: 39, y: 38 },   // 3. 晾青
    { x: 49.8, y: 79 },   // 4. 做青
    { x: 58, y: 38 },   // 5. 杀青
    { x: 72.5, y: 38 },   // 6. 揉捻
    { x: 81, y: 80 }    // 7. 烘焙
  ]
}

// 获取当前茶种的步骤点位
const currentStepPoints = computed(() => {
  return stepPointsMap[selectedTeaType.value] || stepPointsMap.green
})

// 计算步骤位置
const getStepPosition = (index) => {
  const points = currentStepPoints.value
  const point = points[index] || { x: 0, y: 0 }
  return {
    top: `${point.y}%`,
    left: `${point.x}%`
  }
}
</script>

<style scoped>
/* 制茶过程样式 */
.tea-making-section {
  padding: 80px 0;
  background: linear-gradient(135deg, #e8f5e9 0%, #f5f7fa 100%);
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
  color: #2c504d;
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
  margin-bottom: 30px;
}

/* 茶种选择器 */
.tea-type-selector {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.tea-type-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 28px;
  background: white;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 2px solid transparent;
}

.tea-type-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(77, 182, 172, 0.2);
  border-color: #4db6ac;
}

.tea-type-item.active {
  background: linear-gradient(135deg, #4db6ac, #789F6F);
  color: white;
  box-shadow: 0 4px 16px rgba(77, 182, 172, 0.4);
  border-color: #4db6ac;
}

.tea-icon {
  font-size: 1.5rem;
}

.tea-type-item span {
  font-weight: 600;
  font-size: 1rem;
}

/* 工艺流程图 - 图片形式 */
.process-flowchart-image {
  margin-bottom: 50px;
  position: relative;
}

.flow-image-container {
  position: relative;
  width: 100%;
  height: 400px;
  border-radius: 16px;
  overflow: visible;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.flow-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  filter: brightness(0.8); /* 调淡背景图 */
}

.flow-background img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.flow-connections {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
  pointer-events: none;
}

.flow-connections svg {
  width: 100%;
  height: 100%;
}

.connection-line {
  stroke-dasharray: 2000;
  stroke-dashoffset: 2000;
  animation: drawLine 3s ease-out forwards;
  filter: drop-shadow(0 2px 8px rgba(77, 182, 172, 0.5));
}

@keyframes drawLine {
  to {
    stroke-dashoffset: 0;
  }
}

.flow-steps {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 3;
}

.flow-step-point {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0;
  transform: scale(0.8);
  animation: fadeInScale 0.6s ease forwards;
}

/* 移除步骤点位的动画延迟，确保茶种切换时所有点位同时显示 */
.flow-step-point {
  animation-delay: 0s;
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.flow-step-point:hover,
.flow-step-point.active {
  transform: scale(1.1);
  z-index: 10;
}

.flow-step-point .step-number {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #4db6ac, #789F6F);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 8px;
  box-shadow: 0 4px 12px rgba(77, 182, 172, 0.5);
  transition: all 0.3s ease;
}

.flow-step-point:hover .step-number,
.flow-step-point.active .step-number {
  transform: scale(1.2);
  box-shadow: 0 6px 16px rgba(77, 182, 172, 0.7);
}

.flow-step-point .step-name {
  font-weight: 600;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  font-size: 14px;
  text-align: center;
  background: rgba(227, 223, 231, 0.053);
  padding: 4px 8px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.flow-step-point:hover .step-name,
.flow-step-point.active .step-name {
  background: rgba(77, 182, 172, 0.9);
  color: white;
  font-weight: 700;
  transform: translateY(-2px);
}

/* 步骤详情 */
.making-detail {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  margin-bottom: 50px;
  max-width: 1160px;
  margin-left: auto;
  margin-right: auto;
}

.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #eee;
}

.step-header h3 {
  font-size: 1.8rem;
  color: #2c3e50;
  font-family: "STKaiti", "KaiTi", "楷体", serif;
}

.step-time {
  background: linear-gradient(135deg, #4db6ac, #789F6F);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
}

.step-content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}

.step-description h4 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 1.3rem;
}

.step-purpose {
  color: #555;
  margin-bottom: 20px;
  line-height: 1.6;
}

.step-purpose strong {
  color: #4db6ac;
}

.step-requirements,
.step-standard {
  margin-bottom: 20px;
}

.step-requirements h5,
.step-standard h5 {
  color: #4db6ac;
  margin-bottom: 10px;
}

.step-requirements ul {
  list-style: none;
  padding: 0;
}

.step-requirements li {
  padding: 8px 0;
  padding-left: 20px;
  position: relative;
  color: #555;
}

.step-requirements li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #4db6ac;
  font-weight: bold;
}

.step-visual {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.step-image {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.step-image img {
  width: 100%;
  height: auto;
  display: block;
}

.step-tips {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  padding: 20px;
  border-radius: 12px;
  border-left: 4px solid #ff9800;
}

.step-tips h5 {
  color: #e65100;
  margin-bottom: 10px;
}

.step-tips p {
  color: #bf360c;
  font-size: 0.95rem;
}

/* 发酵茶类对比表格 */
.fermentation-table {
  margin-bottom: 20px;
}

.fermentation-table h5 {
  color: #4db6ac;
  margin-bottom: 15px;
  font-size: 1.1rem;
}

.tea-comparison-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tea-comparison-table thead {
  background: linear-gradient(135deg, #4db6ac, #789F6F);
  color: white;
}

.tea-comparison-table th,
.tea-comparison-table td {
  padding: 10px 12px;
  text-align: center;
  border: 1px solid #e0e0e0;
  font-size: 0.85rem;
}

.tea-comparison-table th {
  font-weight: 600;
}

.tea-comparison-table tbody tr:nth-child(even) {
  background: #f5f5f5;
}

.tea-comparison-table tbody tr:hover {
  background: #e8f5e9;
}

/* 过渡动画效果 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.5s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 步骤详情动画 */
.step-detail {
  animation: slideInUp 0.6s ease forwards;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 图片和文字的动画 */
.step-image img {
  transition: all 0.5s ease;
}

.step-image:hover img {
  transform: scale(1.05);
}

.step-requirements li {
  opacity: 0;
  transform: translateX(-20px);
  animation: fadeInLeft 0.5s ease forwards;
}

.step-requirements li:nth-child(1) { animation-delay: 0.1s; }
.step-requirements li:nth-child(2) { animation-delay: 0.2s; }
.step-requirements li:nth-child(3) { animation-delay: 0.3s; }
.step-requirements li:nth-child(4) { animation-delay: 0.4s; }
.step-requirements li:nth-child(5) { animation-delay: 0.5s; }

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 制茶大师访谈 */
.master-interview {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  max-width: 1160px;
  margin: 0 auto;
}

.master-interview h3 {
  text-align: center;
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 30px;
  font-family: "STKaiti", "KaiTi", "楷体", serif;
}

.video-container {
  margin-bottom: 30px;
}

.video-placeholder {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  aspect-ratio: 16/9;
  max-width: 100%;
  height: auto;
}

.video-placeholder:hover {
  transform: scale(1.02);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.video-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.3s ease;
}

.video-placeholder:hover .video-overlay {
  background: rgba(0, 0, 0, 0.7);
}

.video-icon {
  font-size: 4rem;
  margin-bottom: 15px;
  opacity: 0.8;
  transition: all 0.3s ease;
}

.video-placeholder:hover .video-icon {
  transform: scale(1.1);
  opacity: 1;
}

.video-overlay p {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 500;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.master-info {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.master-avatar {
  flex-shrink: 0;
}

.master-avatar img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: contain;
  background: #f5f5f5;
  border: 4px solid #e5e8e7;
}

.master-details h4 {
  font-size: 1.3rem;
  color: #2c3e50;
  margin-bottom: 10px;
}

.master-details p {
  color: #555;
  margin-bottom: 15px;
  line-height: 1.6;
}

.master-quote {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  padding: 20px;
  border-radius: 12px;
  border-left: 4px solid #4db6ac;
  font-style: italic;
  color: #2e7d32;
  font-size: 1.1rem;
}

.tea-set-3d-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  /* background: linear-gradient(135deg, #64a078 0%, #e0e8e3 50%, #3d6b52 100%); */
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  /* box-shadow: 0 4px 15px rgba(100, 160, 120, 0.4); */
  min-width: 180px;
  margin: auto;
  margin-bottom: 11px;
  align-self: flex-end;
}

.tea-set-3d-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(228, 230, 229, 0.5);
  /* background: linear-gradient(135deg, #7ab08a 0%, #5a9070 50%, #4d7b62 100%); */
}

.tea-set-3d-button:active {
  transform: translateY(-1px);
  /* box-shadow: 0 4px 12px rgba(100, 160, 120, 0.4); */
  /* background: linear-gradient(135deg, #5a9070 0%, #4a8060 50%, #3d6b52 100%); */
}

.tea-set-3d-button .button-icon {
  font-size: 28px;
  transition: transform 0.3s ease;
}

.tea-set-3d-button:hover .button-icon {
  transform: rotate(-10deg) scale(1.1);
}

.tea-set-3d-button .button-text {
  font-size: 13px;
  font-weight: 600;
  color: rgb(223, 51, 51);
  text-align: center;
  line-height: 1.4;
}

/* 视频播放器容器 */
.video-player-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.video-player-container video {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.video-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.video-controls span {
  font-weight: bold;
  color: #8B4513;
}

@media (max-width: 768px) {
  .tea-making-section {
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
  
  .tea-type-selector {
    gap: 12px;
  }
  
  .tea-type-item {
    padding: 10px 20px;
  }
  
  .tea-icon {
    font-size: 1.2rem;
  }
  
  .tea-type-item span {
    font-size: 0.9rem;
  }
  
  .process-flowchart-image {
    margin-bottom: 30px;
  }
  
  .flow-image-container {
    height: 300px;
  }
  
  .flow-step-point .step-number {
    width: 35px;
    height: 35px;
    font-size: 14px;
  }
  
  .flow-step-point .step-name {
    font-size: 12px;
    padding: 3px 6px;
  }
  
  .making-detail {
    padding: 20px;
  }
  
  .step-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .step-content-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .step-description h4 {
    font-size: 1.1rem;
  }
  
  .tea-comparison-table {
    font-size: 0.8rem;
  }
  
  .tea-comparison-table th,
  .tea-comparison-table td {
    padding: 8px 10px;
  }
  
  .master-interview {
    padding: 20px;
  }
  
  .master-interview h3 {
    font-size: 1.5rem;
  }
  
  .video-placeholder {
    aspect-ratio: 16/9;
  }
  
  .video-icon {
    font-size: 3rem;
  }
  
  .master-info {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .master-avatar {
    align-self: center;
  }
  
  .master-avatar img {
    width: 80px;
    height: 80px;
  }
  
  .master-details h4 {
    font-size: 1.1rem;
  }
  
  .master-quote {
    padding: 15px;
    font-size: 1rem;
  }
  
  .tea-set-3d-button {
    margin-left: 0;
    align-self: center;
    min-width: 100%;
    padding: 14px 20px;
  }
  
  .tea-set-3d-button .button-icon {
    font-size: 24px;
  }
  
  .tea-set-3d-button .button-text {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .section-title {
    font-size: 1.8rem;
  }
  
  .section-desc {
    font-size: 1rem;
  }
  
  .flow-image-container {
    height: 250px;
  }
  
  .flow-step-point .step-number {
    width: 30px;
    height: 30px;
    font-size: 12px;
  }
  
  .flow-step-point .step-name {
    font-size: 10px;
    padding: 2px 4px;
  }
  
  .making-detail {
    padding: 15px;
  }
  
  .step-header h3 {
    font-size: 1.5rem;
  }
  
  .tea-comparison-table {
    font-size: 0.7rem;
  }
  
  .tea-comparison-table th,
  .tea-comparison-table td {
    padding: 6px 8px;
  }
  
  .master-interview {
    padding: 15px;
  }
  
  .master-interview h3 {
    font-size: 1.3rem;
  }
  
  .video-icon {
    font-size: 2.5rem;
  }
  
  .video-overlay p {
    font-size: 0.9rem;
  }
  
  .master-avatar img {
    width: 70px;
    height: 70px;
  }
  
  .master-details h4 {
    font-size: 1rem;
  }
  
  .master-details p {
    font-size: 0.9rem;
  }
  
  .master-quote {
    padding: 12px;
    font-size: 0.9rem;
  }
}



</style>