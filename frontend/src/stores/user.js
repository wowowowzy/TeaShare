import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { login as loginApi, register as registerApi, getUserShow as getUserShowApi, logout as logoutApi } from '../api/user';
import { ElMessage } from 'element-plus';
import router from '../router';

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '');
  // 添加错误处理，确保JSON.parse失败时不会导致userInfo被清空
  let initialUserInfo = {};
  try {
    initialUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  } catch (error) {
    console.error('解析用户信息失败，使用空对象:', error);
    initialUserInfo = {};
  }
  const userInfo = ref(initialUserInfo);
  const sessionId = ref(localStorage.getItem('sessionId') || '');

  // Getters
  const isLoggedIn = computed(() => !!token.value);
  const userName = computed(() => userInfo.value?.username || '');
  const avatar = computed(() => userInfo.value?.avatar || '');
  const getSessionId = computed(() => sessionId.value || '');
  const userId = computed(() => userInfo.value?.userId || userInfo.value?.id || '');
  // Actions
  // 登录
  const login = async (loginData) => {
    try {
      console.log('登录请求数据:', loginData, loginData.username, loginData.password);
      const res = await loginApi(loginData.username, loginData.password);
      console.log('登录响应数据:', res);
      
      // 由于request.js的响应拦截器已经处理了code字段，
      // 当code === 1时，res已经是res.data
      // 所以这里直接检查res中是否有token字段
      if (res && res.token) {
        // res已经是data部分
        const userData = res;
        token.value = userData.token || '';
        // 构建userInfo对象，包含必要的用户信息
        // 优先使用 userId，如果不存在则使用 id，确保userId有默认值
        const userId = userData.userId || userData.id || '';
        userInfo.value = {
          userId: userId,  // 确保 userId 被存储
          id: userId,      // 同时存储 id 以兼容不同场景
          username: userData.username || userData.name || '',
          avatar: userData.avatar || '',
          // 保留其他可能的用户信息
          ...userData
        };
        sessionId.value = userData.sessionId || '';
        // 存储到localStorage
        localStorage.setItem('token', token.value);
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
        localStorage.setItem('sessionId', sessionId.value);
        // 同时存储到big-user对象中，确保兼容性
        const bigUser = {
          token: token.value,
          user: userInfo.value
        };
        localStorage.setItem('big-user', JSON.stringify(bigUser));
        console.log('登录成功，存储的用户信息:', {
          token: token.value,
          userInfo: userInfo.value,
          sessionId: sessionId.value,
          bigUser: bigUser
        });
        return true;
      } else {
        // 登录失败，显示错误信息
        ElMessage.error(res.msg || '登录失败');
        return false;
      }
    } catch (error) {
      console.error('登录失败:', error);
      // 更详细的错误信息
      if (error.response) {
        ElMessage.error(`登录失败: ${error.response.data?.msg || '服务器错误'}`);
      } else if (error.message) {
        ElMessage.error(`登录失败: ${error.message}`);
      } else {
        ElMessage.error('登录失败，请检查网络连接');
      }
      return false;
    }
  };

  // 注册
  const register = async (registerData) => {
    try {
      console.log('注册请求数据:', registerData);
      console.log('username类型:', typeof registerData.username);
      console.log('password类型:', typeof registerData.password);
      console.log('phone类型:', typeof registerData.phone);
      
      // 确保参数是字符串类型
      const username = String(registerData.username);
      const password = String(registerData.password);
      const phone = String(registerData.phone);
      
      console.log('转换后的参数:', { username, password, phone });
      const res = await registerApi(username, password, phone);
      console.log('注册响应数据:', res);
      if (res.code === 1 || res.code === 200) {
        ElMessage.success('注册成功');
        return true;
      } else {
        // 注册失败，显示错误信息
        ElMessage.error(res.msg || '注册失败');
        return false;
      }
    } catch (error) {
      console.error('注册失败:', error);
      console.error('错误堆栈:', error.stack);
      ElMessage.error('注册失败，请检查网络连接');
      return false;
    }
  };

  // 获取用户信息
  const getUserShow = async () => {
    try {
      const res = await getUserShowApi();
      // 由于响应拦截器已经处理了code=1的情况，直接返回了res.data
      // 所以res已经是用户信息对象
      const userData = res;
      // 确保userData存在且有效
      if (userData) {
        // 优先使用 API 返回的 userId，如果不存在则使用 API 返回的 id，再不存在则使用本地存储中已有的 userId 或 id
        const userId = userData.userId || userData.id || userInfo.value.userId || userInfo.value.id || '';
        userInfo.value = {
          userId: userId,  // 确保 userId 被存储
          id: userId,      // 同时存储 id 以兼容不同场景
          username: userData.username || userData.userName || userData.name,
          avatar: userData.avatar ? userData.avatar.replace(/[`]/g, '') : '',
          // 保留其他可能的用户信息
          ...userData
        };
        // 如果返回数据中包含token和sessionId，也更新并存储
        if (userData.token) {
          token.value = userData.token;
          localStorage.setItem('token', token.value);
        }
        if (userData.sessionId) {
          sessionId.value = userData.sessionId;
          localStorage.setItem('sessionId', sessionId.value);
        }
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
        // 同时更新big-user对象
        const bigUser = {
          token: token.value,
          user: userInfo.value
        };
        localStorage.setItem('big-user', JSON.stringify(bigUser));
        console.log('获取用户信息成功:', userInfo.value);
        return true;
      } else {
        console.warn('获取用户信息失败：返回数据为空');
        return false;
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
      // 错误时保留原有用户信息，不做任何修改
      return false;
    }
  };

  // 退出登录
  const logout = async () => {
    console.log('logout函数被调用了，调用栈:', new Error().stack);
    try {
      await logoutApi();
    } catch (error) {
      console.error('退出登录请求失败:', error);
    } finally {
      // 清除本地存储
      token.value = '';
      userInfo.value = {};
      sessionId.value = '';
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      localStorage.removeItem('sessionId');
      ElMessage.success('退出登录成功');
      router.push('/login');
    }
  };

  // 更新头像
  const updateAvatar = (avatarUrl) => {
    userInfo.value.avatar = avatarUrl;
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
  };

  // 清除用户信息（用于token过期等情况）
  const clearUserInfo = () => {
    token.value = '';
    userInfo.value = {};
    sessionId.value = '';
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('sessionId');
  };

  return {
    token,
    userInfo,
    sessionId,
    isLoggedIn,
    userName,
    avatar,
    userId,
    getSessionId,
    login,
    register,
    getUserShow,
    logout,
    updateAvatar,
    clearUserInfo
  };
});