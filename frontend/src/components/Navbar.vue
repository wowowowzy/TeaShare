<template>
  <nav class="navbar" :class="{ scrolled: isScrolled, transparent: isTransparent }">
    <div class="navbar-container">
      <div class="navbar-logo">茗承古韵</div>
      
      <!-- 桌面端菜单 -->
      <ul class="navbar-menu">
        <li><router-link to="/">首页</router-link></li>
        <li><router-link to="/master">名师讲解</router-link></li>
        <li><router-link to="/tradition">3d茶具</router-link></li>
        <li><router-link to="/community">茶友社</router-link></li>
        <li><router-link to="/quiz">AI小茗</router-link></li>
        <li><router-link to="/shop">茶文化商城</router-link></li>
        <li v-if="!userStore.isLoggedIn"><router-link to="/login">登录</router-link></li>
        <li v-else class="user-info">
          <div class="user-dropdown" ref="dropdownRef">
            <div class="user-trigger" @click="toggleDropdown">
              <img v-if="userAvatar && !avatarError" :src="userAvatar" alt="头像" class="user-avatar-img" @error="handleImageError">
              <img v-else :src="defaultAvatar" alt="默认头像" class="user-avatar-img">
              <span class="user-name">{{ userStore.userName }}</span>
              <span class="dropdown-arrow" :class="{ 'rotate': showDropdown }">▼</span>
            </div>
            <div class="dropdown-menu" v-show="showDropdown">
              <div class="dropdown-item" @click="goToProfile">
                <span class="dropdown-icon">👤</span>
                <span class="dropdown-text">个人中心</span>
              </div>
              <div class="dropdown-item" @click="goToMyPosts">
                <span class="dropdown-icon">📝</span>
                <span class="dropdown-text">我的帖子</span>
              </div>
              <div class="dropdown-item" @click="goToCart">
                <span class="dropdown-icon">🛒</span>
                <span class="dropdown-text">购物车</span>
              </div>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item" @click="handleLogout">
                <span class="dropdown-icon">🚪</span>
                <span class="dropdown-text">退出登录</span>
              </div>
            </div>
          </div>
        </li>
      </ul>
      
      <!-- 移动端菜单按钮 -->
      <div class="mobile-menu-btn" @click="toggleMobileMenu">
        <div class="menu-icon" :class="{ 'open': showMobileMenu }"></div>
      </div>
    </div>
    
    <!-- 移动端菜单 -->
    <div class="mobile-menu" v-show="showMobileMenu">
      <ul class="mobile-menu-list">
        <li><router-link to="/" @click="toggleMobileMenu">首页</router-link></li>
        <li><router-link to="/master" @click="toggleMobileMenu">名师讲解</router-link></li>
        <li><router-link to="/community" @click="toggleMobileMenu">茶友社</router-link></li>
        <li><router-link to="/quiz" @click="toggleMobileMenu">AI小智</router-link></li>
        <li><router-link to="/shop" @click="toggleMobileMenu">茶文化商城</router-link></li>
        <li v-if="!userStore.isLoggedIn"><router-link to="/login" @click="toggleMobileMenu">登录</router-link></li>
        <li v-else class="mobile-user-info">
          <div class="mobile-user-trigger" @click="toggleMobileUserMenu">
            <img v-if="userAvatar && !avatarError" :src="userAvatar" alt="头像" class="user-avatar-img" @error="handleImageError">
            <img v-else :src="defaultAvatar" alt="默认头像" class="user-avatar-img">
            <span class="user-name">{{ userStore.userName }}</span>
            <span class="dropdown-arrow" :class="{ 'rotate': showMobileUserMenu }">▼</span>
          </div>
          <div class="mobile-user-menu" v-show="showMobileUserMenu">
            <div class="dropdown-item" @click="goToProfile; toggleMobileMenu">
              <span class="dropdown-icon">👤</span>
              <span class="dropdown-text">个人中心</span>
            </div>
            <div class="dropdown-item" @click="goToMyPosts; toggleMobileMenu">
              <span class="dropdown-icon">📝</span>
              <span class="dropdown-text">我的帖子</span>
            </div>
            <div class="dropdown-item" @click="goToCart; toggleMobileMenu">
              <span class="dropdown-icon">🛒</span>
              <span class="dropdown-text">购物车</span>
            </div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item" @click="handleLogout; toggleMobileMenu">
              <span class="dropdown-icon">🚪</span>
              <span class="dropdown-text">退出登录</span>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useUserStore } from '../stores/user';
import { useRouter } from 'vue-router';

// Props
const props = defineProps({
  // 是否透明背景（仅首页使用）
  transparent: {
    type: Boolean,
    default: false
  }
});

// 用户store
const userStore = useUserStore();

// 路由
const router = useRouter();

// 滚动状态
const isScrolled = ref(false);

// 下拉菜单显示状态
const showDropdown = ref(false);

// 移动端菜单显示状态
const showMobileMenu = ref(false);

// 移动端用户菜单显示状态
const showMobileUserMenu = ref(false);

// 下拉菜单引用
const dropdownRef = ref(null);

// 头像加载失败状态
const avatarError = ref(false);

// 计算是否透明
const isTransparent = ref(props.transparent);

// 默认头像 - 使用本地图片
const defaultAvatar = '/assest/default.jpg';

// 计算用户头像
const userAvatar = computed(() => {
  // 重置头像错误状态
  avatarError.value = false;
  const avatar = userStore.userInfo?.avatar;
  console.log('导航栏用户头像URL:', avatar);
  // 检查头像URL是否有效
  if (avatar) {
    // 获取时间戳，用于避免头像缓存
    const timestamp = localStorage.getItem('avatarTimestamp') || Date.now().toString();
    // 处理头像URL，添加时间戳参数
    let avatarUrl = avatar;
    if (avatarUrl) {
      // 检查URL是否已经包含查询参数
      if (avatarUrl.includes('?')) {
        // 已经有查询参数，添加时间戳
        avatarUrl = `${avatarUrl}&t=${timestamp}`;
      } else {
        // 没有查询参数，添加时间戳作为第一个参数
        avatarUrl = `${avatarUrl}?t=${timestamp}`;
      }
    }
    // 检查是否是外部链接（以http://或https://开头）
    if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
      // 允许阿里云OSS的链接
      if (avatarUrl.includes('aliyuncs.com')) {
        console.log('检测到阿里云OSS头像URL，允许使用');
        return avatarUrl;
      }
      // 允许本地服务器的链接（含当前站点域名，避免写死 IP）
      if (avatarUrl.includes('127.0.0.1') || avatarUrl.includes('localhost')
          || avatarUrl.includes('wowowzy.art')) {
        console.log('检测到本站服务器头像URL，允许使用');
        return avatarUrl;
      }
      // 检查是否是之前的失效URL
      if (avatarUrl.includes('dtstatic.com')) {
        console.warn('检测到失效的头像URL，使用默认头像');
        return defaultAvatar;
      }
      console.warn('检测到外部头像URL，使用默认头像（网络环境限制）');
      return defaultAvatar;
    }
    return avatarUrl;
  }
  return defaultAvatar;
});

// 切换下拉菜单显示/隐藏
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

// 切换移动端菜单显示/隐藏
const toggleMobileMenu = () => {
  showMobileMenu.value = !showMobileMenu.value;
  showMobileUserMenu.value = false;
};

// 切换移动端用户菜单显示/隐藏
const toggleMobileUserMenu = () => {
  showMobileUserMenu.value = !showMobileUserMenu.value;
};

// 处理头像加载失败
const handleImageError = (event) => {
  avatarError.value = true;
  // 尝试使用默认头像
  event.target.src = defaultAvatar;
};

// 跳转到个人中心
const goToProfile = () => {
  showDropdown.value = false;
  showMobileUserMenu.value = false;
  router.push('/profile');
};

// 跳转到我的帖子
const goToMyPosts = () => {
  showDropdown.value = false;
  showMobileUserMenu.value = false;
  router.push('/community/my-posts');
};

// 跳转到购物车
const goToCart = () => {
  showDropdown.value = false;
  showMobileUserMenu.value = false;
  router.push('/cart');
};

// 点击外部关闭下拉菜单和移动端菜单
const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    showDropdown.value = false;
  }
  // 关闭移动端菜单
  if (!event.target.closest('.mobile-menu-btn') && !event.target.closest('.mobile-menu')) {
    showMobileMenu.value = false;
    showMobileUserMenu.value = false;
  }
};

// 检测滚动
const handleScroll = () => {
  if (props.transparent) {
    isScrolled.value = window.scrollY > 50;
  }
};

// 退出登录
const handleLogout = () => {
  userStore.logout();
};

// 生命周期
onMounted(() => {
  if (props.transparent) {
    window.addEventListener('scroll', handleScroll);
  }
  // 添加点击外部关闭下拉菜单的事件监听
  document.addEventListener('click', handleClickOutside);
  
  // 组件挂载时，直接使用登录返回的用户信息
  console.log('Navbar组件挂载，用户信息:', userStore.userInfo);
  console.log('用户头像:', userStore.userInfo?.avatar);
});

// 监听props.transparent变化
watch(() => props.transparent, (newVal) => {
  isTransparent.value = newVal;
  if (!newVal) {
    isScrolled.value = false;
  }
});

onUnmounted(() => {
  if (props.transparent) {
    window.removeEventListener('scroll', handleScroll);
  }
  // 移除点击外部关闭下拉菜单的事件监听
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
/* 导航栏样式 */
.navbar {
  background-color: rgba(0, 128, 128, 0.9);
  color: white;
  padding: 15px 0;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: background-color 0.3s ease;
}

/* 透明导航栏样式（仅首页） */
.navbar.transparent {
  background-color: transparent;
  box-shadow: none;
}

/* 滚动时导航栏样式 */
.navbar.transparent.scrolled {
  background-color: rgba(0, 128, 128, 0.9);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.navbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.navbar-logo {
  font-size: 24px;
  font-weight: bold;
  color: white;
}

.navbar-menu {
  display: flex;
  list-style: none;
  align-items: center;
}

.navbar-menu li {
  margin-left: 20px;
}

.navbar-menu a {
  color: white;
  text-decoration: none;
  transition: color 0.3s ease;
  font-size: 16px;
}

.navbar-menu a:hover {
  color: #E0FFFF;
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
}

/* 用户下拉菜单容器 */
.user-dropdown {
  position: relative;
  cursor: pointer;
}

/* 用户触发区域 */
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px;
  border-radius: 20px;
  transition: background-color 0.3s ease;
}

.user-trigger:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 用户头像图片 */
.user-avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid white;
}

/* 默认用户头像 */
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #008080;
  font-weight: bold;
  font-size: 14px;
  border: 2px solid white;
}

/* 用户名 */
.user-name {
  font-size: 14px;
  color: white;
}

/* 下拉箭头 */
.dropdown-arrow {
  font-size: 10px;
  color: white;
  transition: transform 0.3s ease;
}

.dropdown-arrow.rotate {
  transform: rotate(180deg);
}

/* 下拉菜单 */
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 10px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 150px;
  padding: 8px 0;
  z-index: 1001;
  animation: dropdownFadeIn 0.2s ease;
}

@keyframes dropdownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 下拉菜单项 */
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  color: #333 !important;
  text-decoration: none;
  font-size: 14px;
  transition: background-color 0.2s ease;
  cursor: pointer;
  background-color: transparent;
  border: none;
  width: 100%;
  text-align: left;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
  color: #008080 !important;
}

/* 下拉菜单图标 */
.dropdown-icon {
  font-size: 16px;
  flex-shrink: 0;
}

/* 下拉菜单文字 */
.dropdown-text {
  color: #333;
  font-size: 14px;
  white-space: nowrap;
}

/* 下拉菜单分割线 */
.dropdown-divider {
  height: 1px;
  background-color: #e0e0e0;
  margin: 8px 0;
}

/* 移动端菜单按钮 */
.mobile-menu-btn {
  display: none;
  cursor: pointer;
  z-index: 1002;
}

.menu-icon {
  width: 30px;
  height: 24px;
  position: relative;
  cursor: pointer;
}

.menu-icon::before,
.menu-icon::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 2px;
  background-color: white;
  transition: all 0.3s ease;
}

.menu-icon::before {
  top: 0;
}

.menu-icon::after {
  bottom: 0;
}

.menu-icon::before + span {
  position: absolute;
  top: 11px;
  width: 100%;
  height: 2px;
  background-color: white;
  transition: all 0.3s ease;
}

.menu-icon.open::before {
  transform: rotate(45deg);
  top: 11px;
}

.menu-icon.open::after {
  transform: rotate(-45deg);
  bottom: 11px;
}

.menu-icon.open::before + span {
  opacity: 0;
  transform: translateX(-20px);
}

/* 移动端菜单 */
.mobile-menu {
  position: fixed;
  top: 0;
  right: 0;
  width: 80%;
  max-width: 300px;
  height: 100vh;
  background-color: rgba(0, 128, 128, 0.95);
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.2);
  z-index: 1001;
  animation: slideInRight 0.3s ease;
  overflow-y: auto;
}

.mobile-menu-list {
  list-style: none;
  padding: 80px 20px 20px;
}

.mobile-menu-list li {
  margin-bottom: 15px;
}

.mobile-menu-list a {
  display: block;
  color: white;
  text-decoration: none;
  padding: 15px 20px;
  border-radius: 8px;
  transition: background-color 0.3s ease;
  font-size: 16px;
}

.mobile-menu-list a:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 移动端用户信息 */
.mobile-user-info {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.mobile-user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 20px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.mobile-user-trigger:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.mobile-user-menu {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  margin-top: 10px;
  overflow: hidden;
}

.mobile-user-menu .dropdown-item {
  color: white;
  border: none;
  background-color: transparent;
}

.mobile-user-menu .dropdown-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

/* 动画 */
@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-menu {
    display: none;
  }
  
  .mobile-menu-btn {
    display: block;
  }
  
  .navbar-container {
    padding: 0 15px;
  }
  
  .navbar-logo {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .mobile-menu {
    width: 90%;
  }
  
  .mobile-menu-list {
    padding: 70px 15px 15px;
  }
  
  .mobile-menu-list a {
    padding: 12px 15px;
    font-size: 14px;
  }
}
</style>