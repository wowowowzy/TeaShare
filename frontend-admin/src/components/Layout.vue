<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-container">
          <i class="el-icon-coffee sidebar-logo"></i>
          <h1>茗承古韵后台管理系统</h1>
        </div>
      </div>
      <nav class="sidebar-nav">
        <ul>
          <li>
            <router-link to="/dashboard" class="nav-item">
              <i class="el-icon-s-home"></i>
              <span>仪表盘</span>
            </router-link>
          </li>
          <li>
            <router-link to="/users" class="nav-item">
              <i class="el-icon-user"></i>
              <span>用户管理</span>
            </router-link>
          </li>
          <li>
            <router-link to="/products" class="nav-item">
              <i class="el-icon-goods"></i>
              <span>产品管理</span>
            </router-link>
          </li>
          <li>
            <router-link to="/orders" class="nav-item">
              <i class="el-icon-document"></i>
              <span>订单管理</span>
            </router-link>
          </li>
          <li>
            <router-link to="/seckill" class="nav-item">
              <i class="el-icon-s-promotion"></i>
              <span>秒杀管理</span>
            </router-link>
          </li>
          <li>
            <router-link to="/log" class="nav-item">
              <i class="el-icon-notebook-2"></i>
              <span>活动日志</span>
            </router-link>
          </li>
        </ul>
      </nav>
    </aside>
    
    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部导航 -->
      <header class="top-nav">
        <div class="nav-left">
          <button class="menu-toggle" @click="toggleSidebar">
            <i class="el-icon-menu"></i>
          </button>
        </div>
        <div class="nav-right">
          <div class="user-info">
            <div class="user-avatar">
              <img :src="displayUserInfo.avatar || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tea%20ceremony%20master%20avatar%20professional%20portrait&image_size=square'" :alt="displayUserInfo.username || '管理员头像'">
            </div>
            <div class="user-details">
              <span class="user-name">{{ displayUserInfo.username || '管理员' }}</span>
              <span class="user-role">{{ displayUserInfo.role || '系统管理员' }}</span>
            </div>
            <el-dropdown>
              <i class="el-icon-arrow-down"></i>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>个人设置</el-dropdown-item>
                  <el-dropdown-item>修改密码</el-dropdown-item>
                  <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <!-- 退出登录按钮 -->
          <button class="logout-btn" @click="logout">
            <i class="el-icon-switch-button"></i>
            <span>退出登录</span>
          </button>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <div class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';

const router = useRouter();
const userStore = useUserStore();
const sidebarCollapsed = ref(false);

// 计算属性，确保响应式显示用户信息
const displayUserInfo = computed(() => {
  console.log('Layout 组件中的 userInfo:', userStore.userInfo);
  return userStore.userInfo || {};
});

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
};

const logout = () => {
  userStore.clearUserInfo();
  router.push('/login');
};

// 组件挂载时确保用户状态已初始化
onMounted(() => {
  console.log('Layout 组件挂载，当前用户信息:', userStore.userInfo);
  // 如果用户信息为空，尝试重新初始化
  if (!userStore.userInfo || Object.keys(userStore.userInfo).length === 0) {
    console.log('用户信息为空，重新初始化...');
    userStore.initUserState();
  }
});
</script>

<style>
/* 全局样式重置，防止body和html产生滚动条 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body, html {
  height: 100%;
  overflow: hidden;
}
</style>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background-color: #f5f5dc;
  background-image: url('https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=subtle%20tea%20leaves%20pattern%20light%20beige%20background&image_size=landscape_16_9');
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
}

/* 侧边栏样式 */
.sidebar {
  width: 240px;
  background-color: #3a6442;
  color: #fff;
  transition: all 0.3s ease;
  overflow-y: auto;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sidebar-logo {
  font-size: 24px;
  color: #f5f5dc;
}

.sidebar-header h1 {
  font-size: 18px;
  margin: 0;
  font-weight: 600;
  color: #f5f5dc;
}

.sidebar-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  color: #f5f5dc;
  text-decoration: none;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.nav-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
  border-left-color: #f5f5dc;
}

.nav-item i {
  margin-right: 12px;
  font-size: 18px;
}

.nav-item.router-link-active {
  background-color: rgba(255, 255, 255, 0.2);
  color: #fff;
  border-left-color: #f5f5dc;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: rgba(245, 245, 220, 0.9);
}

/* 顶部导航样式 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.menu-toggle {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #4CAF50;
  padding: 0;
  margin-right: 20px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 20px;
  transition: background-color 0.3s ease;
}

.user-info:hover {
  background-color: rgba(76, 175, 80, 0.1);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #4CAF50;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.user-role {
  font-size: 12px;
  color: #666;
}

.user-info i {
  font-size: 16px;
  color: #4CAF50;
}

/* 退出登录按钮样式 */
.logout-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: #f78989;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
}

.logout-btn:active {
  transform: translateY(0);
}

.logout-btn i {
  font-size: 16px;
}

/* 页面内容样式 */
.page-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
    transform: translateX(-100%);
  }
  
  .sidebar.show {
    transform: translateX(0);
  }
  
  .main-content {
    width: 100%;
  }
  
  .user-details {
    display: none;
  }
  
  .logout-btn span {
    display: none;
  }
}
</style>
