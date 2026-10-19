<template>
  <div class="app">
    <Navbar :transparent="isHomePage" />
    <router-view />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import Navbar from './components/Navbar.vue';
import { useUserStore } from './stores/user';

const route = useRoute();
const isHomePage = ref(route.path === '/');
const userStore = useUserStore();

watch(() => route.path, (newPath) => {
  isHomePage.value = newPath === '/';
});

// 应用启动时恢复用户信息
onMounted(async () => {
  console.log('应用启动，检查登录状态...');
  console.log('当前token:', userStore.token);
  console.log('当前userInfo:', userStore.userInfo);
  
  // 如果用户已经登录（有token），但userInfo为空，尝试从localStorage恢复
  if (userStore.token && (!userStore.userInfo || Object.keys(userStore.userInfo).length === 0)) {
    try {
      const storedUserInfo = localStorage.getItem('userInfo');
      if (storedUserInfo) {
        const parsedUserInfo = JSON.parse(storedUserInfo);
        userStore.userInfo = parsedUserInfo;
        console.log('从localStorage恢复的用户信息:', parsedUserInfo);
      }
    } catch (error) {
      console.error('从localStorage恢复用户信息失败:', error);
    }
  }
  
  // 如果用户已经登录（有token），获取最新的用户信息
  if (userStore.token) {
    console.log('应用启动，检测到token，正在获取最新用户信息...');
    try {
      await userStore.getUserShow();
    } catch (error) {
      console.error('获取最新用户信息失败，保留本地存储的用户信息:', error);
      // 即使获取失败，也不做任何处理，保留本地存储的用户信息
    }
  }
});
</script>

<style>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Arial', sans-serif;
  background-color: #f5f5f5;
  color: #008080; /* 青色字体 */
}

.app {
  min-height: 100vh;
}

/* 通用按钮样式 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #008080;
  color: white;
}

.btn-primary:hover {
  background-color: #006666;
  transform: translateY(-2px);
}

/* 通用卡片样式 */
.card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}


</style>
