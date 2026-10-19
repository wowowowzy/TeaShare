<template>
  <div class="immersive-scene">
    <div class="floating-particles">
      <div class="particle"></div>
      <div class="particle"></div>
      <div class="particle"></div>
      <div class="particle"></div>
      <div class="particle"></div>
      <div class="particle"></div>
      <div class="particle"></div>
      <div class="particle"></div>
      <div class="particle"></div>
      <div class="particle"></div>
    </div>
    <div class="soft-glow"></div>
    <div ref="canvasContainer" class="canvas-container"></div>

    <div class="ui-overlay">
      <!-- <div class="header-controls">
        <div class="title">
          <br>
          <br>
          <h1>潮汕工夫茶茶室</h1>
          <p>国家级非物质文化遗产</p>
        </div>
      </div> -->

      <div class="toolbar-controls">
        <button @click="resetView" class="control-btn">
          <span>重置视角</span>
        </button>
        <!-- <button @click="toggleAutoRotate" class="control-btn" :class="{ active: autoRotateEnabled }">
          <span>{{ autoRotateEnabled ? '停止' : '自动' }}</span>
        </button> -->
        <button @click="toggleWireframe" class="control-btn" :class="{ active: wireframeEnabled }">
          <span>线框</span>
        </button>
      </div>


      <div class="loading-overlay" v-if="isLoading">
        <div class="loading-spinner"></div>
        <p>{{ loadingStatus }}</p>
        <div class="loading-progress">
          <div class="loading-bar" :style="{ width: loadingProgress + '%' }"></div>
        </div>
      </div>

      <div class="error-overlay" v-if="hasError">
        <h3>模型加载失败</h3>
        <p>{{ errorMessage }}</p>
        <button @click="reloadModel" class="retry-btn">
          <span>重新加载</span>
        </button>
      </div>

      <!-- 茶具信息面板 - 点击圆圈后显示 -->
      <Transition name="slide-fade">
        <div v-if="showInfoPanel && selectedTeaware" class="teaware-info-panel" @click.stop>
          <div class="info-header">
            <div class="info-icon">{{ selectedTeaware.icon }}</div>
            <div class="info-title">
              <h2>{{ selectedTeaware.name }}</h2>
              <span class="info-origin"> {{ selectedTeaware.origin }}</span>
            </div>
            <button @click="closeInfoPanel" class="close-btn">✕</button>
          </div>

          <div class="info-content">
            <div class="info-section">
              <h3> 历史渊源</h3>
              <p>{{ selectedTeaware.history }}</p>
            </div>

            <div class="info-row">
              <div class="info-section">
                <h3> 材质</h3>
                <p class="material-name">{{ selectedTeaware.material }}</p>
              </div>
              <div class="info-section">
                <h3> 材质特性</h3>
                <p>{{ selectedTeaware.materialFeature }}</p>
              </div>
            </div>

            <div class="info-section">
              <h3> 使用方法</h3>
              <p>{{ selectedTeaware.usage }}</p>
            </div>

            <div class="info-section cultural-section">
              <h3> 文化寓意</h3>
              <p>{{ selectedTeaware.culturalMeaning }}</p>
            </div>
          </div>
        </div>
      </Transition>

      <!-- 提示信息 -->
      <div class="interaction-tip">
        <span>点击圆圈标记查看详情</span>
      </div>

      <!-- 圆形标记层 - 可点击交互 -->
      <div class="highlight-markers">
        <div v-for="(marker, index) in markers" :key="'marker-' + index" class="marker"
          :class="{ active: marker.active }" :style="{ left: marker.x + 'px', top: marker.y + 'px' }"
          @click.stop="handleMarkerClick(marker)">
          <div class="marker-ring"></div>
          <div class="marker-dot"></div>
          <div class="marker-pulse"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 潮汕工夫茶3D展示组件
 * 实现3D茶具模型展示、交互标记、信息面板等功能
 */
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { DRACOLoader } from 'three/examples/jsm/loaders/DRACOLoader.js'

// ========== 基础状态管理 ==========
const canvasContainer = ref(null)       // 渲染容器
const isLoading = ref(true)             // 加载状态
const hasError = ref(false)             // 错误状态
const errorMessage = ref('')            // 错误信息
const loadingStatus = ref('正在初始化...') // 加载状态文本
const loadingProgress = ref(0)          // 加载进度
const autoRotateEnabled = ref(false)    // 自动旋转开关
const wireframeEnabled = ref(false)     // 线框模式开关

// ========== 交互状态管理 ==========
const showInfoPanel = ref(false)        // 信息面板显示状态
const selectedTeaware = ref(null)       // 当前选中的茶具信息
const activeMarkerIndex = ref(-1)       // 当前激活的标记索引
const mouse = new THREE.Vector2()       // 鼠标坐标
const tooltipContent = ref('')          // 工具提示内容
const tooltipPosition = ref({ x: 0, y: 0 }) // 工具提示位置

// ========== 标记状态管理 ==========
const markers = ref([])                 // 圆形标记数组

// ========== THREE.js 对象引用 ==========
let scene, camera, renderer, controls, raycaster
let modelGroup                          // 3D模型组
let animationFrameId                    // 动画帧ID
let dracoLoader                         // DRACO压缩加载器
let raycastObjects = []                 // 可交互对象数组

// ========== 常量配置 ==========
// const GLB_MODEL_URL = import.meta.env.VITE_MODEL_URL || '/api/model/tea.glb'
const GLB_MODEL_URL = 'https://java-web-itheima-zy.oss-cn-beijing.aliyuncs.com/tea.glb'


// 茶具信息数据库
const teawareData = {
  teapot: {
    name: '茶壶',
    icon: '',
    origin: '中国西晋',
    history: '茶壶雏形可追溯至西晋，唐代随煎茶法成熟而发展，宋代点茶时形制趋于精巧，明清紫砂壶艺鼎盛，成为泡茶不可或缺的核心器具。',
    material: '紫砂/陶瓷/玻璃',
    materialFeature: '紫砂透气不渗水，陶瓷保温易清洁，玻璃通透可观茶形。',
    usage: '1. 温壶预热；2. 投茶注水；3. 静置闷泡；4. 出汤分茶。',
    culturalMeaning: '茶壶承载着"壶小乾坤大"的茶道意境，象征着包容内敛的处世哲学。'
  },
  cup: {
    name: '品茗杯',
    icon: '',
    origin: '中国唐宋',
    history: '品茗杯起源于唐宋时期，福建德化白瓷以其"白如雪、薄如纸、润如玉"的特点闻名于世，被誉为"中国白"。',
    material: '白瓷',
    materialFeature: '质地细腻，釉面光洁，保温适中，能很好地衬托茶汤色泽，便于观察汤色和叶底。',
    usage: '1. 用沸水烫杯预热；2. 承接茶汤；3. 观色闻香后小口品饮；4. 品后用茶巾擦拭杯底。',
    culturalMeaning: '品茗杯象征着茶道中的"品"字，体现了品茶人对茶的尊重和对生活的品味追求。'
  },
  teaTray: {
    name: '公道杯',
    icon: '',
    origin: '中国宋代',
    history: '公道杯又称"茶海"，起源于宋代，是工夫茶席中核心器具之一，最初为古代酒具演变而来，后融入茶道成为均匀茶汤的关键器物。',
    material: '玻璃/陶瓷/紫砂',
    materialFeature: '玻璃款通透可视茶汤色泽，陶瓷款隔热防烫，紫砂款能吸附茶香，容量适配茶席茶具，分茶便捷均匀。',
    usage: '1. 承接茶壶冲泡好的茶汤；2. 均匀茶汤浓度，避免各杯茶汤浓淡不一；3. 分茶入品茗杯，保证每位茶客茶汤一致；4. 临时盛放剩余茶汤。',
    culturalMeaning: '公道杯承载着茶道"公平无偏"的核心理念，象征待人接物公允平和，体现了中式茶道的人文精神。'
  },
  teaSet: {
    name: '茶道六君子',
    icon: '',
    origin: '中国唐代',
    history: '茶道六君子起源于唐代煎茶时期，经过历代演变形成了现在的组合：茶筒、茶夹、茶漏、茶拨、茶针、茶则。',
    material: '竹木/铜制',
    materialFeature: '天然材质，工艺精美，实用与美观兼具。',
    usage: '茶筒：收纳工具；茶夹：夹取茶杯；茶漏：辅助投茶；茶拨：拨茶入壶；茶针：疏通壶嘴；茶则：量取茶叶。',
    culturalMeaning: '茶道六君子体现了儒家"礼"的思想，是茶道礼仪的重要组成部分。'
  },
  teapet: {
    name: '茶宠',
    icon: '',
    origin: '中国宋代',
    history: '茶宠是茶文化中的经典雅玩，起源于中国宋代，是茶人在品茶、泡茶时用来滋养、赏玩的紫砂小摆件，历经千年成为茶桌必备文玩。',
    material: '紫砂/陶土',
    materialFeature: '原矿紫砂或优质陶土烧制，透气性好，长期茶水滋养会越发光润、包浆醇厚，极具把玩价值。',
    usage: '茶桌摆件、茶水滋养把玩、增添品茶意境、寓意吉祥招财，是茶席文化的重要点缀。',
    culturalMeaning: '茶宠象征着茶人的生活情趣与祈福心愿，体现了中式茶道中静心、赏玩、修身养性的文化内涵。'
  },
  kettle: {
    name: '煮水壶',
    icon: '',
    origin: '中国汉代',
    history: '煮茶历史可追溯至汉代，唐代盛行煎茶，宋代流行点茶，煮水壶是煮茶必备器具。',
    material: '不锈钢/陶瓷',
    materialFeature: '耐高温，导热均匀，保温性能好。',
    usage: '1. 加水至八分满；2. 加热至沸腾；3. 用于冲泡茶叶或煮茶。',
    culturalMeaning: '煮水壶象征着"水火既济"，体现了阴阳调和的哲学思想。'
  },
  teaclear: {
    name: '茶洗',
    icon: '',
    origin: '唐代',
    history: '茶洗又称"茶盂"，起源于唐代，是传统工夫茶席中用于清洗茶具、承接废水的专用器具，随茶道发展逐渐成为茶桌必备配角。',
    material: '陶瓷/竹制/紫砂',
    materialFeature: '器型宽大且边缘圆润，内壁光滑易清洁，陶瓷款耐磨耐用，竹制款轻便雅致，兼具实用与观赏性。',
    usage: '1. 冲洗茶杯、茶宠等茶具；2. 承接洗茶、温杯的废水；3. 临时放置待使用的干净茶具。',
    culturalMeaning: '茶洗承载着茶道中"洁净为先"的理念，象征着涤除尘俗、静心品茶的茶道意境。'
  },
  smellcup: {
    name: '闻香杯',
    icon: '',
    origin: '中国清代',
    history: '闻香杯是工夫茶席的专属器具，起源于清代，多与品茗杯搭配使用，是中式茶道中品鉴茶香的核心器具。',
    material: '白瓷/紫砂/青瓷',
    materialFeature: '杯身瘦长收口，能聚拢茶香不易散失，材质细腻触感温润，便于手持闻香。',
    usage: '1. 承接茶汤，静置片刻聚拢茶香；2. 倒出茶汤后，持杯轻嗅杯底留香；3. 与品茗杯搭配，提升茶道仪式感。',
    culturalMeaning: '闻香杯承载着茶道"闻香品茗"的雅趣，体现了古人从嗅觉到味觉的沉浸式品茶智慧。'
  }
}

// 茶具数据列表，用于随机分配
const teawareList = Object.values(teawareData)

// 标记位置偏移配置（可根据实际模型调整）
// 键为索引，值为{x(左右), y(上下), z(前后)}偏移量（像素）
// x: 正值向右，负值向左
// y: 正值向上，负值向下  
// z: 正值向前(靠近相机)，负值向后(远离相机)
// 使用 reactive 使其成为响应式对象
const markerOffsets = reactive({
  0: { x: 13, y: 120, z: -50 },
  1: { x: 278, y: 20, z: -160 },
  2: { x: 87, y: 20, z: -150 },
  3: { x: 115, y: 128, z: -300 },
  4: { x: 140, y: 30, z: 100 },
  5: { x: -100, y: -30, z: 100 },
  6: { x: 176, y: -10, z: -160 },
  7: { x: 393, y: 5, z: -80 }
})

/**
 * 根据对象名称或索引获取茶具数据
 * 支持多种命名方式匹配，提高兼容性
 * @param {string} objectName - 3D对象名称
 * @returns {object} - 茶具数据对象
 */
function getTeawareData(objectName) {
  const nameMap = {
    'teapot': 'teapot',
    'tea_pot': 'teapot',
    'pot': 'teapot',
    'cup': 'cup',
    'tea_cup': 'cup',
    'teaTray': 'teaTray',
    'tray': 'teaTray',
    'tea_tray': 'teaTray',
    'teaSet': 'teaSet',
    'tools': 'teaSet',
    'tea_set': 'teaSet',
    'book': 'book',
    'tea_book': 'book',
    'kettle': 'kettle',
    'tea_kettle': 'kettle',
    'faircup': 'faircup',
    'tea_faircup': 'faircup',
    'caddy': 'teaCaddy',
    'container': 'teaCaddy',
    'tea_caddy': 'teaCaddy',
    'tea_container': 'teaCaddy',
    'bowl': 'cup',
    'tea_bowl': 'cup',
    'jar': 'teaCaddy',
    'tea_jar': 'teaCaddy'
  }

  const lowerName = objectName.toLowerCase()

  // 首先尝试按名称匹配
  for (const [key, value] of Object.entries(nameMap)) {
    if (lowerName.includes(key)) {
      return teawareData[value]
    }
  }

  // 如果名称不匹配，尝试按索引分配
  if (lowerName.startsWith('object_')) {
    const index = parseInt(lowerName.split('_')[1])
    if (!isNaN(index)) {
      const teawareIndex = index % teawareList.length
      return teawareList[teawareIndex]
    }
  }

  // 默认返回第一个茶具
  return teawareList[0]
}

// 获取对象显示名称（用于提示）
function getObjectName(objectName) {
  const data = getTeawareData(objectName)
  return data ? data.name : objectName
}

// 视角配置参数 - 统一管理
const VIEW_CONFIG = {
  cameraPosition: { x: 0, y: -100, z: -11 },  // 相机位置（拉近）
  targetPosition: { x: 0, y: -52, z: -2 }     // 旋转中心（贴近桌面高度）
}

function initThree() {
  const container = canvasContainer.value
  if (!container) {
    console.error('容器元素不存在')
    return
  }

  const rect = container.getBoundingClientRect()
  const width = rect.width || container.clientWidth || window.innerWidth
  const height = rect.height || container.clientHeight || window.innerHeight

  scene = new THREE.Scene()
  scene.background = null  // 设置为透明，让CSS背景显示

  // 视角设置：透视相机，视角65度，近裁剪面0.1，远裁剪面100
  camera = new THREE.PerspectiveCamera(65, width / height, 0.1, 100)
  camera.position.set(VIEW_CONFIG.cameraPosition.x, VIEW_CONFIG.cameraPosition.y, VIEW_CONFIG.cameraPosition.z)

  renderer = new THREE.WebGLRenderer({
    antialias: true,
    alpha: true
  })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  container.appendChild(renderer.domElement)

  // 轨道控制器：用于控制相机视角交互
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true        // 启用阻尼效果，使视角移动更平滑
  controls.dampingFactor = 0.05        // 阻尼系数
  controls.enableZoom = true           // 允许缩放
  controls.enablePan = true            // 允许平移
  controls.maxPolarAngle = Math.PI / 2.1  // 最大垂直视角（防止翻转）
  controls.minDistance = 1.5           // 最近距离
  controls.maxDistance = 5            // 最远距离
  controls.target.set(VIEW_CONFIG.targetPosition.x, VIEW_CONFIG.targetPosition.y, VIEW_CONFIG.targetPosition.z)
  controls.update()

  // 初始化射线检测器
  raycaster = new THREE.Raycaster()

  initLights()
  initDracoLoader()
  loadGLBModel()
  animate()
  window.addEventListener('resize', onWindowResize)
}

function initDracoLoader() {
  dracoLoader = new DRACOLoader()
  dracoLoader.setDecoderPath('https://cdnjs.cloudflare.com/ajax/libs/three.js/r132/draco/')
  dracoLoader.setDecoderConfig({ type: 'js' })
}

function initLights() {
  // 调整灯光色调，更贴近户外自然光（暖调自然光）
  const ambientLight = new THREE.AmbientLight(0xf5f8f0, 0.7)
  scene.add(ambientLight)

  // 主光源：模拟户外阳光，偏暖黄
  const mainLight = new THREE.DirectionalLight(0xfff8e8, 1.3)
  mainLight.position.set(10, 15, 10)
  mainLight.castShadow = true
  mainLight.shadow.mapSize.width = 1024
  mainLight.shadow.mapSize.height = 1024
  mainLight.shadow.camera.near = 0.5
  mainLight.shadow.camera.far = 50
  mainLight.shadow.camera.left = -15
  mainLight.shadow.camera.right = 15
  mainLight.shadow.camera.top = 15
  mainLight.shadow.camera.bottom = -15
  scene.add(mainLight)

  // 补光：模拟户外环境漫反射光，偏青绿色调
  const fillLight = new THREE.DirectionalLight(0xe8f5f0, 0.5)
  fillLight.position.set(-10, 10, -10)
  scene.add(fillLight)

  // 点光源：模拟户外散射光，更柔和
  const pointLight = new THREE.PointLight(0xf8f5e6, 0.8, 40)
  pointLight.position.set(0, 4, 0)
  scene.add(pointLight)
}

function setupMaterial(material) {
  material.envMapIntensity = 1.0
  if (material.isStandardMaterial || material.isMeshPhysicalMaterial) {
    material.roughness = Math.min(material.roughness, 0.8)
    material.metalness = Math.max(material.metalness, 0.1)
  }
}

function loadGLBModel() {
  loadingStatus.value = '正在加载模型资源...'

  const loader = new GLTFLoader()
  loader.setDRACOLoader(dracoLoader)
  loader.load(
    GLB_MODEL_URL,
    (gltf) => {


      loadingStatus.value = '正在处理模型...'
      modelGroup = gltf.scene

      // 统一开启阴影
      modelGroup.traverse((child) => {
        if (child.isMesh) {
          child.castShadow = true
          child.receiveShadow = true
          if (child.material) {
            Array.isArray(child.material)
              ? child.material.forEach(mat => setupMaterial(mat))
              : setupMaterial(child.material)
          }
        }
      })

      // ========== 核心修复：正确居中 + 高度正常 ==========
      const box = new THREE.Box3().setFromObject(modelGroup)
      const center = box.getCenter(new THREE.Vector3())
      const size = box.getSize(new THREE.Vector3())
      const maxDim = Math.max(size.x, size.y, size.z)
      const scale = maxDim > 0 ? 8 / maxDim : 1

      // 把模型几何中心移到世界原点(0,0,0)
      modelGroup.position.copy(center).multiplyScalar(-1)
      // 统一缩放
      modelGroup.scale.setScalar(scale)

      // 收集可交互对象
      raycastObjects = []
      modelGroup.traverse((child) => {
        if (child.isMesh && child.name) {
          raycastObjects.push(child)
        }
      })


      // 生成圆形标记
      generateMarkers()

      // 添加点击事件监听（确保只绑定一次）
      renderer.domElement.removeEventListener('click', onMouseClick)
      renderer.domElement.removeEventListener('mousemove', onMouseMove)
      renderer.domElement.removeEventListener('mousemove', updateMarkers)
      renderer.domElement.addEventListener('click', onMouseClick)
      renderer.domElement.addEventListener('mousemove', onMouseMove)
      renderer.domElement.addEventListener('mousemove', updateMarkers)


      // 模型已居中到原点，使用统一配置的视角
      scene.add(modelGroup)
      camera.position.set(VIEW_CONFIG.cameraPosition.x, VIEW_CONFIG.cameraPosition.y, VIEW_CONFIG.cameraPosition.z)
      controls.target.set(VIEW_CONFIG.targetPosition.x, VIEW_CONFIG.targetPosition.y, VIEW_CONFIG.targetPosition.z)
      controls.update()

      loadingProgress.value = 100
      loadingStatus.value = '加载完成'

      setTimeout(() => {
        isLoading.value = false
      }, 500)
    },
    (xhr) => {
      loadingProgress.value = Math.round((xhr.loaded / xhr.total) * 100)
      loadingStatus.value = `正在加载... ${loadingProgress.value}%`
    },
    (err) => {
      console.error('GLB模型加载失败:', err)
      hasError.value = true
      errorMessage.value = err.message || '无法加载3D模型，请检查模型路径'
      isLoading.value = false
    }
  )
}

// 动画循环
function animate() {
  animationFrameId = requestAnimationFrame(animate)

  // 自动旋转视角：绕Y轴缓慢旋转
  if (autoRotateEnabled.value && modelGroup) {
    modelGroup.rotation.y += 0.003
  }

  controls.update()                    // 更新控制器状态
  
  // 自动旋转时不更新标记位置，保持标记固定在屏幕上
  // 只有手动旋转相机时才更新标记位置
  if (!autoRotateEnabled.value) {
    updateMarkers(false)               // 手动操作时更新标记位置
  }
  
  renderer.render(scene, camera)
}

// 窗口大小变化时更新视角
function onWindowResize() {
  const width = canvasContainer.value.clientWidth
  const height = canvasContainer.value.clientHeight

  camera.aspect = width / height      // 更新相机宽高比
  camera.updateProjectionMatrix()     // 更新投影矩阵
  renderer.setSize(width, height)
}

// 重置视角到初始状态
function resetView() {
  camera.position.set(VIEW_CONFIG.cameraPosition.x, VIEW_CONFIG.cameraPosition.y, VIEW_CONFIG.cameraPosition.z)
  controls.target.set(VIEW_CONFIG.targetPosition.x, VIEW_CONFIG.targetPosition.y, VIEW_CONFIG.targetPosition.z)
  controls.update()
}

function toggleAutoRotate() {
  autoRotateEnabled.value = !autoRotateEnabled.value
}

function toggleWireframe() {
  wireframeEnabled.value = !wireframeEnabled.value

  if (modelGroup) {
    modelGroup.traverse((child) => {
      if (child.isMesh) {
        if (Array.isArray(child.material)) {
          child.material.forEach((mat) => {
            mat.wireframe = wireframeEnabled.value
          })
        } else if (child.material) {
          child.material.wireframe = wireframeEnabled.value
        }
      }
    })
  }
}

function reloadModel() {
  hasError.value = false
  errorMessage.value = ''
  isLoading.value = true
  loadingProgress.value = 0

  if (modelGroup) {
    modelGroup.traverse((child) => {
      if (child.isMesh) {
        if (child.geometry) child.geometry.dispose()
        if (child.material) {
          if (Array.isArray(child.material)) {
            child.material.forEach((mat) => mat.dispose())
          } else {
            child.material.dispose()
          }
        }
      }
    })
    scene.remove(modelGroup)
    modelGroup = null
  }

  loadGLBModel()
}

// 鼠标点击事件处理 - 仅处理3D场景交互，不显示文字说明
function onMouseClick(event) {
  event.stopPropagation()
  // 点击桌面区域不显示任何文字说明
  // 只有点击圆圈标记才会显示（通过 handleMarkerClick 处理）
}

let previousHoveredObject = null

// 移除对象高亮效果（已禁用）
function removeHighlight(obj) { }

// 添加对象高亮效果（已禁用）
function addHighlight(obj) { }

// 鼠标移动事件处理
function onMouseMove(event) {
  const rect = renderer.domElement.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(mouse, camera)
  const intersects = raycaster.intersectObjects(raycastObjects)

  // 移除之前高亮的对象
  if (previousHoveredObject) {
    removeHighlight(previousHoveredObject)
    previousHoveredObject = null
  }

  if (intersects.length > 0) {
    const hoveredObject = intersects[0].object
    const teawareInfo = getTeawareData(hoveredObject.name)

    if (teawareInfo) {
      renderer.domElement.style.cursor = 'pointer'
      tooltipContent.value = teawareInfo.name
      tooltipPosition.value = { x: event.clientX, y: event.clientY }
      // 添加高亮效果
      addHighlight(hoveredObject)
      previousHoveredObject = hoveredObject
    } else {
      renderer.domElement.style.cursor = 'default'
      tooltipContent.value = ''
    }
  } else {
    renderer.domElement.style.cursor = 'default'
    tooltipContent.value = ''
  }
}

/**
 * 根据索引获取标记偏移量（支持x(左右), y(上下), z(前后)）
 * @param {number} index - 对象索引
 * @returns {object} - 偏移量{x, y, z}
 */
function getMarkerOffset(index) {
  if (markerOffsets[index] !== undefined) {
    return markerOffsets[index]
  }
  return { x: 0, y: 0, z: 0 }
}

/**
 * 生成圆形标记
 * 将3D场景中的可交互对象转换为2D屏幕上的圆形标记
 */
function generateMarkers() {
  const newMarkers = []
  const containerRect = canvasContainer.value.getBoundingClientRect()

  // 选择主要的茶具对象生成标记，最多8个
  const mainObjects = raycastObjects.slice(0, Math.min(8, raycastObjects.length))

  mainObjects.forEach((obj, index) => {
    // 获取对象中心位置（使用世界坐标，不受模型旋转影响）
    const box = new THREE.Box3().setFromObject(obj)
    const center = box.getCenter(new THREE.Vector3())

    // 将局部坐标转换为世界坐标，确保不受父节点旋转影响
    obj.localToWorld(center)

    // 获取标记偏移量（支持x(左右), y(上下), z(前后)）
    const offset = getMarkerOffset(index)

    // 将offset的x、y、z都作为3D世界坐标偏移（转换为3D单位）
    center.x += offset.x * 0.01
    center.y += offset.y * 0.01
    center.z += offset.z * 0.01

    // 存储固定的3D世界坐标，确保标记点位置固定
    const fixedWorldPosition = new THREE.Vector3(center.x, center.y, center.z)

    // 将3D坐标转换为2D屏幕坐标
    const screenPos = center.clone()
    screenPos.project(camera)

    let x = (screenPos.x * 0.5 + 0.5) * containerRect.width
    let y = (-screenPos.y * 0.5 + 0.5) * containerRect.height

    // 使用 reactive 创建响应式对象，确保属性变化能触发视图更新
    newMarkers.push(reactive({
      x,
      y,
      object: obj,
      worldPosition: fixedWorldPosition, // 存储固定的3D世界坐标
      teaware: teawareList[index % teawareList.length],
      active: false,
      index
    }))
  })

  markers.value = newMarkers
}

/**
 * 更新标记位置（相机移动时调用）
 * 使用固定的3D世界坐标更新屏幕位置，确保标记点固定在3D空间中
 * @param {boolean} forceRecalc - 是否强制重新计算3D坐标（当markerOffsets变化时使用）
 */
function updateMarkers(forceRecalc = false) {
  // 自动旋转期间不更新标记位置，保持标记固定在屏幕上
  if (autoRotateEnabled.value) {
    return
  }
  
  const containerRect = canvasContainer.value.getBoundingClientRect()

  markers.value.forEach(marker => {
    let worldPos = marker.worldPosition

    // 强制重新计算或没有存储的世界坐标时，重新计算
    if (forceRecalc || !worldPos) {
      if (marker.object) {
        const box = new THREE.Box3().setFromObject(marker.object)
        worldPos = box.getCenter(new THREE.Vector3())
        marker.object.localToWorld(worldPos)

        // 应用最新的markerOffsets偏移
        const offset = getMarkerOffset(marker.index)
        worldPos.x += offset.x * 0.01
        worldPos.y += offset.y * 0.01
        worldPos.z += offset.z * 0.01

        // 更新存储的世界坐标
        marker.worldPosition = worldPos.clone()
      } else {
        return
      }
    }

    // 将3D世界坐标转换为2D屏幕坐标
    const screenPos = worldPos.clone()
    screenPos.project(camera)

    let x = (screenPos.x * 0.5 + 0.5) * containerRect.width
    let y = (-screenPos.y * 0.5 + 0.5) * containerRect.height

    // 强制更新位置（不依赖变化检测）
    marker.x = x
    marker.y = y
  })
}

/**
 * 关闭信息面板
 */
function closeInfoPanel() {
  showInfoPanel.value = false
  markers.value.forEach(m => {
    m.active = false
  })
  activeMarkerIndex.value = -1
}

/**
 * 处理标记点击 - 显示对应茶具信息
 * @param {object} marker - 被点击的标记对象
 */
function handleMarkerClick(marker) {
  if (!marker.teaware) return

  // 如果点击的是当前激活的标记，关闭面板
  if (marker.index === activeMarkerIndex.value && showInfoPanel.value) {
    closeInfoPanel()
    return
  }

  // 更新所有标记的激活状态
  markers.value.forEach((m, i) => {
    m.active = i === marker.index
  })
  activeMarkerIndex.value = marker.index

  // 设置选中的茶具并显示面板
  selectedTeaware.value = marker.teaware
  showInfoPanel.value = true
}

function cleanup() {
  cancelAnimationFrame(animationFrameId)
  window.removeEventListener('resize', onWindowResize)

  if (renderer && renderer.domElement) {
    renderer.domElement.removeEventListener('click', onMouseClick)
    renderer.domElement.removeEventListener('mousemove', onMouseMove)
  }

  if (modelGroup) {
    modelGroup.traverse((child) => {
      if (child.isMesh) {
        if (child.geometry) child.geometry.dispose()
        if (child.material) {
          if (Array.isArray(child.material)) {
            child.material.forEach((mat) => mat.dispose())
          } else {
            child.material.dispose()
          }
        }
      }
    })
  }

  if (renderer) {
    renderer.dispose()
  }

  if (controls) {
    controls.dispose()
  }
}

onMounted(() => {
  initThree()

  // 监听 markerOffsets 变化，自动触发更新
  watch(markerOffsets, (newVal, oldVal) => {
    console.log('markerOffsets变化:', newVal)
    if (markers.value.length > 0) {
      // 强制重新计算3D坐标以应用新的偏移值
      updateMarkers(true)
    }
  }, { deep: true })
})

onUnmounted(() => {
  cleanup()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>

<style scoped>
.immersive-scene {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  /* 调整背景色为户外茶园/自然环境的色调（浅绿+浅棕+天空蓝渐变） */
  background:
    radial-gradient(ellipse 90% 70% at 50% -40%, rgba(180, 220, 190, 0.3) 0%, transparent 55%),
    radial-gradient(ellipse 70% 60% at 70% 100%, rgba(160, 190, 140, 0.25) 0%, transparent 55%),
    radial-gradient(ellipse 50% 45% at 25% 90%, rgba(190, 210, 170, 0.2) 0%, transparent 55%),
    radial-gradient(ellipse 45% 40% at 35% 10%, rgba(200, 230, 210, 0.18) 0%, transparent 60%),
    radial-gradient(ellipse 35% 35% at 65% 20%, rgba(170, 220, 190, 0.15) 0%, transparent 60%),
    /* 基础渐变调整为户外自然的浅色调（大地色+草木绿+浅蓝） */
    linear-gradient(180deg, #f5f8f0 0%, #e8f0e0 25%, #d8e8d0 50%, #c8e0c0 75%, #b8d8b0 100%);
}

.immersive-scene::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 2px 2px, rgba(100, 120, 80, 0.03) 1px, transparent 0);
  background-size: 40px 40px;
  pointer-events: none;
  opacity: 0.6;
}

.immersive-scene::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse 80% 95% at 50% -40%, rgba(160, 200, 170, 0.18) 0%, transparent 65%),
    radial-gradient(ellipse 50% 30% at 50% 100%, rgba(140, 180, 150, 0.15) 0%, transparent 55%);
  pointer-events: none;
  animation: ambientPulse 12s ease-in-out infinite;
}

@keyframes ambientPulse {

  0%,
  100% {
    opacity: 0.5;
  }

  30% {
    opacity: 0.8;
  }

  60% {
    opacity: 0.6;
  }

  80% {
    opacity: 0.9;
  }
}

.floating-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
  z-index: 1;
}

.particle {
  position: absolute;
  width: 3px;
  height: 3px;
  /* 粒子颜色调整为户外自然的浅绿/米白色 */
  background: radial-gradient(circle, rgba(100, 140, 80, 0.7) 0%, transparent 70%);
  border-radius: 50%;
  animation: floatUp 15s linear infinite;
}

.particle:nth-child(1) {
  left: 10%;
  animation-delay: 0s;
  animation-duration: 18s;
}

.particle:nth-child(2) {
  left: 20%;
  animation-delay: 3s;
  animation-duration: 22s;
}

.particle:nth-child(3) {
  left: 35%;
  animation-delay: 6s;
  animation-duration: 16s;
}

.particle:nth-child(4) {
  left: 50%;
  animation-delay: 9s;
  animation-duration: 20s;
}

.particle:nth-child(5) {
  left: 65%;
  animation-delay: 12s;
  animation-duration: 17s;
}

.particle:nth-child(6) {
  left: 80%;
  animation-delay: 15s;
  animation-duration: 21s;
}

.particle:nth-child(7) {
  left: 90%;
  animation-delay: 2s;
  animation-duration: 19s;
}

.particle:nth-child(8) {
  left: 15%;
  animation-delay: 7s;
  animation-duration: 23s;
}

.particle:nth-child(9) {
  left: 45%;
  animation-delay: 11s;
  animation-duration: 15s;
}

.particle:nth-child(10) {
  left: 70%;
  animation-delay: 14s;
  animation-duration: 24s;
}

@keyframes floatUp {
  0% {
    transform: translateY(100vh) scale(0);
    opacity: 0;
  }

  10% {
    opacity: 0.8;
    transform: translateY(90vh) scale(1);
  }

  90% {
    opacity: 0.6;
    transform: translateY(10vh) scale(0.8);
  }

  100% {
    transform: translateY(-10vh) scale(0);
    opacity: 0;
  }
}

.soft-glow {
  position: absolute;
  top: -30%;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  height: 70%;
  /* 光晕调整为户外阳光的暖绿色调 */
  background: radial-gradient(ellipse at center, rgba(160, 200, 170, 0.25) 0%, transparent 70%);
  pointer-events: none;
  z-index: 2;
  animation: glowPulse 10s ease-in-out infinite;
}

@keyframes glowPulse {

  0%,
  100% {
    opacity: 0.4;
    transform: translateX(-50%) scale(1);
  }

  50% {
    opacity: 0.7;
    transform: translateX(-50%) scale(1.1);
  }
}

.canvas-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 3;
}

.ui-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 10;
}

.header-controls {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 20px 30px;
  background: linear-gradient(to bottom, rgba(80, 120, 90, 0.5) 0%, transparent 100%);
}

.toolbar-controls {
  position: absolute;
  top: 80px;
  right: 30px;
  display: flex;
  gap: 12px;
  padding: 0px 20px;

  border-radius: 30px;
  backdrop-filter: blur(10px);
  pointer-events: auto;
}

.title {
  color: #2a4030;
  /* 文字颜色调整为深草木绿，更自然 */
}

.title h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 5px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  /* 阴影调浅，更贴合户外 */
}

.title p {
  font-size: 14px;
  color: rgba(40, 70, 50, 0.8);
  /* 副标题颜色调整为深绿 */
  margin: 0;
}

.control-buttons {
  display: flex;
  gap: 12px;

  ;
}

.control-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(240, 250, 240, 0.8);
  border: none;
  border-radius: 25px;
  color: #2a4030;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  pointer-events: auto;
  backdrop-filter: blur(10px);
}



.control-btn.active {
  /* 激活状态调整为自然绿 */
  background: rgba(100, 160, 120, 0.8);
  border-color: rgba(80, 120, 90, 0.6);
  color: #fff;
}

.control-btn .icon {
  font-size: 16px;
}

.info-panel {
  position: absolute;
  bottom: 80px;
  left: 30px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  overflow: hidden;
  max-width: 320px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  pointer-events: auto;
}

.panel-header {
  /* 面板头部调整为自然绿渐变 */
  background: linear-gradient(135deg, #64a078 0%, #4a8060 100%);
  padding: 16px 20px;
}

.panel-header h3 {
  color: #fff;
  font-size: 18px;
  margin: 0;
  font-weight: 500;
}

.panel-content {
  padding: 16px 20px;
}

.panel-content p {
  color: #406050;
  /* 文本颜色调整为深绿 */
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 16px;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.feature {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: rgba(100, 160, 120, 0.08);
  border-radius: 8px;
}

.feature-icon {
  font-size: 18px;
}

.feature span:last-child {
  color: #305040;
  /* 特性文字颜色深绿 */
  font-size: 13px;
}

.interaction-hint {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 30px;
  padding: 12px 24px;
  /* 提示框背景调整为浅绿半透明 */
  background: rgba(240, 250, 240, 0.8);
  border-radius: 30px;
  backdrop-filter: blur(10px);
}

.hint-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(40, 70, 50, 0.9);
  /* 提示文字颜色深绿 */
  font-size: 13px;
}

.hint-icon {
  font-size: 16px;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  /* 加载背景调整为浅绿半透明 */
  background: rgba(240, 250, 240, 0.95);
  z-index: 100;
  pointer-events: auto;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(80, 120, 90, 0.2);
  border-top-color: #64a078;
  /* 加载动画颜色自然绿 */
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-overlay p {
  color: rgba(40, 70, 50, 0.8);
  /* 加载文字颜色深绿 */
  font-size: 14px;
  margin-bottom: 12px;
}

.loading-progress {
  width: 200px;
  height: 4px;
  background: rgba(80, 120, 90, 0.2);
  border-radius: 2px;
  overflow: hidden;
}

.loading-bar {
  height: 100%;
  /* 进度条颜色自然绿渐变 */
  background: linear-gradient(90deg, #64a078, #84c098);
  transition: width 0.3s ease;
}

.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  /* 错误背景调整为浅红+浅绿 */
  background: rgba(250, 240, 240, 0.95);
  z-index: 100;
  pointer-events: auto;
}

.error-icon {
  font-size: 64px;
  color: #a05050;
  /* 错误图标颜色深红棕 */
  margin-bottom: 16px;
}

.error-overlay h3 {
  color: #503030;
  /* 错误标题颜色深棕 */
  font-size: 24px;
  margin: 0 0 12px;
}

.error-overlay p {
  color: rgba(80, 50, 50, 0.8);
  /* 错误描述颜色棕红 */
  font-size: 14px;
  margin: 0 0 24px;
  text-align: center;
  max-width: 300px;
}

.retry-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 30px;
  /* 重试按钮颜色自然绿 */
  background: linear-gradient(135deg, #64a078 0%, #4a8060 100%);
  border: none;
  border-radius: 25px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(100, 160, 120, 0.4);
}

/* 茶具信息面板样式 */
.teaware-info-panel {
  position: absolute;
  bottom: 30px;
  left: 30px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  overflow: hidden;
  max-width: 400px;
  width: calc(100% - 60px);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  pointer-events: auto;
  z-index: 20;
}

.info-header {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #64a078 0%, #4a8060 100%);
}

.info-icon {
  font-size: 40px;
  margin-right: 16px;
}

.info-title {
  flex: 1;
}

.info-title h2 {
  color: #fff;
  font-size: 22px;
  margin: 0 0 5px;
  font-weight: 600;
}

.info-origin {
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.info-content {
  padding: 20px 24px;
}

.info-section {
  margin-bottom: 20px;
}

.info-section:last-child {
  margin-bottom: 0;
}

.info-section h3 {
  color: #4a8060;
  font-size: 15px;
  margin: 0 0 10px;
  font-weight: 600;
}

.info-section p {
  color: #555;
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
}

.material-name {
  font-weight: 600;
  color: #3a6050 !important;
}

.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.cultural-section {
  padding: 16px;
  background: linear-gradient(135deg, rgba(100, 160, 120, 0.1) 0%, rgba(74, 128, 96, 0.05) 100%);
  border-radius: 12px;
}

.cultural-section p {
  color: #4a7060;
}

.interaction-tip {
  position: absolute;
  top: 80px;
  left: 30px;
  padding: 10px 20px;
  background: rgba(240, 250, 240, 0.9);
  border-radius: 25px;
  backdrop-filter: blur(10px);
  pointer-events: auto;
}

.interaction-tip span {
  color: #3a6050;
  font-size: 13px;
}

/* 过渡动画 */
.slide-fade-enter-active {
  transition: all 0.4s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* 响应式设计 - 确保不同屏幕尺寸下交互一致 */
@media (max-width: 768px) {
  .teaware-info-panel {
    bottom: 20px;
    left: 20px;
    right: 20px;
    width: auto;
    max-width: none;
    max-height: 70vh;
    overflow-y: auto;
  }

  .info-row {
    grid-template-columns: 1fr;
  }

  .interaction-tip {
    right: 20px;
    font-size: 12px;
    padding: 8px 16px;
  }

  .marker {
    width: 32px;
    height: 32px;
    margin-left: -16px;
    margin-top: -16px;
  }

  .marker-ring {
    width: 26px;
    height: 26px;
    margin-left: -13px;
    margin-top: -13px;
  }

  .marker-dot {
    width: 10px;
    height: 10px;
    margin-left: -5px;
    margin-top: -5px;
  }

  .marker-pulse {
    width: 32px;
    height: 32px;
    margin-left: -16px;
    margin-top: -16px;
  }
}

@media (max-width: 480px) {
  .info-header {
    padding: 16px 20px;
  }

  .info-icon {
    font-size: 32px;
    margin-right: 12px;
  }

  .info-title h2 {
    font-size: 18px;
  }

  .info-content {
    padding: 16px 20px;
  }

  .interaction-tip {
    right: 15px;
    bottom: 20px;
    padding: 6px 12px;
    font-size: 11px;
  }

  .marker {
    width: 28px;
    height: 28px;
    margin-left: -14px;
    margin-top: -14px;
  }

  .marker-ring {
    width: 22px;
    height: 22px;
    margin-left: -11px;
    margin-top: -11px;
    border-width: 1.5px;
  }

  .marker-dot {
    width: 8px;
    height: 8px;
    margin-left: -4px;
    margin-top: -4px;
  }

  .marker-pulse {
    width: 28px;
    height: 28px;
    margin-left: -14px;
    margin-top: -14px;
    border-width: 1.5px;
  }

  .toolbar-controls {
    top: 15px;
    right: 15px;
    gap: 8px;
  }

  .control-btn {
    padding: 8px 14px;
    font-size: 12px;
  }
}

@media (max-width: 320px) {
  .teaware-info-panel {
    bottom: 15px;
    left: 15px;
    right: 15px;
  }

  .marker {
    width: 24px;
    height: 24px;
    margin-left: -12px;
    margin-top: -12px;
  }

  .marker-ring {
    width: 20px;
    height: 20px;
    margin-left: -10px;
    margin-top: -10px;
  }

  .marker-dot {
    width: 7px;
    height: 7px;
    margin-left: -3.5px;
    margin-top: -3.5px;
  }

  .marker-pulse {
    width: 24px;
    height: 24px;
    margin-left: -12px;
    margin-top: -12px;
  }
}

/* 圆形高亮标记样式 */
.highlight-markers {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 15;
}

.highlight-markers .marker {
  pointer-events: auto;
}

.marker {
  position: absolute;
  width: 36px;
  height: 36px;
  margin-left: -18px;
  margin-top: -18px;
  cursor: pointer;
  pointer-events: auto;
  transition: transform 0.3s ease;
}

.marker:hover {
  transform: scale(1.2);
}

.marker-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 30px;
  height: 30px;
  margin-left: -15px;
  margin-top: -15px;
  border: 2px solid rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  animation: ring-pulse 2s ease-in-out infinite;
}

.marker-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 12px;
  height: 12px;
  margin-left: -6px;
  margin-top: -6px;
  background: radial-gradient(circle, #ffffff 0%, #e0e0e0 100%);
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.6);
}

.marker-pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 36px;
  height: 36px;
  margin-left: -18px;
  margin-top: -18px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  animation: pulse-expand 2s ease-out infinite;
}

.marker.active .marker-ring {
  border-color: #ffffff;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.8);
}

.marker.active .marker-dot {
  background: radial-gradient(circle, #ffffff 0%, #f0f0f0 50%, #e0e0e0 100%);
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.8);
}

@keyframes ring-pulse {

  0%,
  100% {
    transform: scale(1);
    opacity: 0.8;
  }

  50% {
    transform: scale(1.1);
    opacity: 0.5;
  }
}

@keyframes pulse-expand {
  0% {
    transform: scale(0.8);
    opacity: 0.6;
  }

  100% {
    transform: scale(1.6);
    opacity: 0;
  }
}

/* 优化信息面板样式 - 更贴合设计图 */
.teaware-info-panel {
  background: rgba(245, 248, 242, 0.98);
  border: 1px solid rgba(100, 160, 120, 0.2);
}

.info-header {
  background: linear-gradient(135deg, #5a906d 0%, #3d6b54 100%);
}

.info-title h2 {
  font-weight: 600;
  letter-spacing: 1px;
}

.info-item {
  border-bottom: 1px dashed rgba(100, 160, 120, 0.2);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 500;
  color: #3d6b54;
}

.info-text {
  color: #4a5c4e;
  line-height: 1.7;
}
</style>