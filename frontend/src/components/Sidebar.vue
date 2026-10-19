<template>
  <aside class="sidebar">
    <el-menu
      mode="vertical"
      :default-active="activeMenu"
      class="sidebar-menu"
      @select="handleMenuSelect"
    >
      <!-- 置顶菜单项 -->
      <el-menu-item index="back-to-top" @click.stop="scrollToTop">
        <span>置顶</span>
      </el-menu-item>
      
      <el-menu-item index="tea-varieties">
        <span>特色茶种</span>
      </el-menu-item>
      <el-menu-item index="tea-making">
        <span>制茶品茗</span>
      </el-menu-item>
      <el-menu-item index="tea-history">
        <span>茶源历史</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// 当前激活的菜单项
const activeMenu = ref('')

// 菜单数据
const menuItems = [
  { id: 'tea-varieties', label: '特色茶种' },
  { id: 'tea-making', label: '制茶品茗' },
  { id: 'tea-history', label: '茶源历史' }
]

// 防抖计时器
let scrollTimer = null

// 处理菜单选择
const handleMenuSelect = (index) => {
  if (index === 'back-to-top') {
    scrollToTop()
    return
  }
  activeMenu.value = index
  const element = document.getElementById(index)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

// 监听滚动，更新激活状态（防抖）
const handleScroll = () => {
  if (scrollTimer) {
    clearTimeout(scrollTimer)
  }
  scrollTimer = setTimeout(() => {
    // 检查是否在页面顶部
    if (window.scrollY < 50) {
      activeMenu.value = 'back-to-top'
      return
    }

    const windowHeight = window.innerHeight
    let foundActive = false

    menuItems.forEach(item => {
      const element = document.getElementById(item.id)
      if (element) {
        const rect = element.getBoundingClientRect()
        if (rect.top <= windowHeight * 0.5 && rect.bottom >= windowHeight * 0.25) {
          activeMenu.value = item.id
          foundActive = true
        }
      }
    })

    if (!foundActive && menuItems.length > 0) {
      const lastElement = document.getElementById(menuItems[menuItems.length - 1].id)
      if (lastElement && lastElement.getBoundingClientRect().bottom <= windowHeight) {
        activeMenu.value = menuItems[menuItems.length - 1].id
      }
    }
  }, 150)
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  handleScroll()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (scrollTimer) {
    clearTimeout(scrollTimer)
  }
})
</script>

<style scoped>
/* 侧边栏容器 */
.sidebar {
  position: fixed;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 90;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 10px;
  padding: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(100, 160, 120, 0.08);
}

/* Element Plus 菜单样式覆盖 */
.sidebar-menu {
  border: none;
  background: transparent;
  min-width: 100px;
  width: 100px;
}

.sidebar-menu :deep(.el-menu-item) {
  margin: 3px 0;
  padding: 10px 10px;
  border-radius: 6px;
  transition: all 0.2s ease;
  color: #5a6c65;
  font-size: 13px;
  justify-content: flex-start;
  min-height: 38px;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(100, 160, 120, 0.08);
  color: #4a8060;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #64a078 0%, #4a8060 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(100, 160, 120, 0.3);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .sidebar {
    left: 8px;
    padding: 4px;
  }
  
  .sidebar-menu {
    min-width: 90px;
    width: 90px;
  }
  
  .sidebar-menu :deep(.el-menu-item) {
    padding: 8px 8px;
    font-size: 12px;
    min-height: 36px;
  }
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: auto;
    right: 10px;
    top: auto;
    bottom: 75px;
    transform: none;
    padding: 4px;
    border-radius: 12px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  }
  
  .sidebar-menu {
    display: flex;
    flex-direction: row;
    min-width: auto;
    width: auto;
  }
  
  .sidebar-menu :deep(.el-menu-item) {
    padding: 10px 12px;
    font-size: 12px;
    min-height: 40px;
    margin: 0 3px;
  }
}

@media (max-width: 480px) {
  .sidebar {
    right: 8px;
    bottom: 65px;
    padding: 3px;
  }
  
  .sidebar-menu :deep(.el-menu-item) {
    padding: 8px 10px;
    font-size: 11px;
    min-height: 36px;
  }
}
</style>