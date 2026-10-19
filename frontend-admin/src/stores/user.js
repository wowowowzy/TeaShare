import { ref } from 'vue';
import { defineStore } from 'pinia';
import { userLogin, getUserShow } from '../api/user';

export const useUserStore = defineStore('user', () => {
  const token = ref('');
  const userInfo = ref({});

  // 初始化用户状态
  const initUserState = () => {
    try {
      // 从本地存储加载用户信息
      const storedToken = localStorage.getItem('token');
      const storedUserInfo = localStorage.getItem('userInfo');
      
      console.log('初始化用户状态 - token:', storedToken ? '存在' : '不存在');
      console.log('初始化用户状态 - userInfo:', storedUserInfo ? '存在' : '不存在');
      
      if (storedToken) {
        token.value = storedToken;
      }
      
      if (storedUserInfo) {
        try {
          const parsedInfo = JSON.parse(storedUserInfo);
          console.log('解析的用户信息:', parsedInfo);
          userInfo.value = parsedInfo;
        } catch (parseError) {
          console.error('解析用户信息失败:', parseError);
          localStorage.removeItem('userInfo');
        }
      }
      
      console.log('初始化后的 userInfo:', userInfo.value);
    } catch (error) {
      console.error('初始化用户状态失败:', error);
      clearUserInfo();
    }
  };

  const setToken = (newToken) => {
    try {
      token.value = newToken || '';
      if (token.value) {
        localStorage.setItem('token', token.value);
      } else {
        localStorage.removeItem('token');
      }
    } catch (error) {
      console.error('存储token失败:', error);
    }
  };

  const setUserInfo = (info) => {
    try {
      userInfo.value = info || {};
      console.log('设置用户信息:', userInfo.value);
      if (Object.keys(userInfo.value).length > 0) {
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
      } else {
        localStorage.removeItem('userInfo');
      }
    } catch (error) {
      console.error('存储用户信息失败:', error);
    }
  };

  const clearUserInfo = () => {
    try {
      userInfo.value = {};
      setToken('');
      localStorage.removeItem('userInfo');
    } catch (error) {
      console.error('清除用户信息失败:', error);
    }
  };

  const login = async (username, password) => {
    try {
      const res = await userLogin(username, password);
      console.log('登录响应:', res);
      
      // 根据实际接口返回结构提取数据
      // 接口返回: { code: 1, msg: 'success', data: { userId, username, token, avatar, ... } }
      const responseData = res?.data || res;
      
      const nextToken = responseData?.token || '';
      const userData = {
        userId: responseData?.userId,
        username: responseData?.username,
        avatar: responseData?.avatar,
        state: responseData?.state,
        sessionId: responseData?.sessionId
      };
      
      console.log('提取的token:', nextToken);
      console.log('提取的用户数据:', userData);
      
      setToken(nextToken);
      setUserInfo(userData);
      
      return true;
    } catch (error) {
      console.error('登录失败:', error);
      clearUserInfo();
      throw error;
    }
  };

  const getUserInfo = async () => {
    try {
      const res = await getUserShow();
      setUserInfo(res || {});
      return userInfo.value;
    } catch (error) {
      console.error('获取用户信息失败:', error);
      return {};
    }
  };

  // 检查用户是否已登录
  const isLoggedIn = () => {
    return !!token.value;
  };

  // 立即初始化用户状态
  initUserState();

  return {
    token,
    userInfo,
    login,
    getUserShow: getUserInfo,
    clearUserInfo,
    isLoggedIn,
    initUserState
  };
});
