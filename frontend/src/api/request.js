import axios from 'axios';
import JSONBig from 'json-bigint';
import { ElMessage } from 'element-plus';
import router from '../router';
import { useUserStore } from '../stores/user';
import { API_BASE } from '../utils/apiBase';

// 创建json-bigint实例
const jsonBig = JSONBig({
  storeAsString: true // 将所有大数字转换为字符串
});

// 创建axios实例
const request = axios.create({
  baseURL: API_BASE, // 后端API地址（默认相对路径，由 Nginx/代理转发）
  timeout: 100000, // 增加超时时间到100秒
  withCredentials: true, // 携带cookie
  // 自定义响应转换
  transformResponse: [function (data) {
    try {
      // 尝试使用json-bigint解析数据
      return jsonBig.parse(data);
    } catch (error) {
      // 如果解析失败，直接返回原始数据（可能是纯文本）
      console.log('响应解析失败，返回原始数据:', data);
      return data;
    }
  }]
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 获取token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['token'] = `${token}`;
    } else {
      console.warn('未找到token，请求头中将不包含token');
    }
    
    // 携带sessionId用于AI对话
    const sessionId = localStorage.getItem('sessionId');
    if (sessionId) {
      config.headers['sessionId'] = `${sessionId}`;
    }
    
    // 确保携带cookie
    config.withCredentials = true;
    return config;
  },
  (error) => {
    // 对请求错误做些什么
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 对响应数据做点什么
    console.log('响应数据:', response.data);
    
    // 检查响应类型，如果是blob或arraybuffer，直接返回响应数据
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response.data;
    }
    
    // 处理JSON格式响应
    const res = response.data;
    
    // 检查是否是纯文本响应（智能问答API）
    if (typeof res === 'string') {
      // 对于纯文本响应，直接返回
      return res;
    }
    
    // 对于JSON响应，检查code字段
    // 成功code为1或200，失败code为0或其他
    if (res.code === 1 || res.code === 200) {
      // 成功响应，返回data字段（如果有的话）
      const data = res.data || res;
      
      // 检查当前请求的URL，只处理订单相关的API
      const url = response.config.url;
      if (url && (url.includes('/user/order/getOrders') || url.includes('/user/order/getOrderById'))) {
        // 处理订单数据，确保订单ID作为字符串处理以避免数字精度问题
        if (Array.isArray(data)) {
          // 订单列表
          return data.map(item => {
            if (item.orderId) {
              return {
                ...item,
                orderId: item.orderId.toString()
              };
            }
            return item;
          });
        } else if (data && data.orderId) {
          // 单个订单
          return {
            ...data,
            orderId: data.orderId.toString()
          };
        }
      }
      
      return data;
    } else {
      // 失败响应，抛出错误（包含后端返回的错误信息）
      const errorMsg = res.msg || '请求失败';
      ElMessage.error(errorMsg);
      return Promise.reject(new Error(errorMsg));
    }
  },
  (error) => {
    // 对响应错误做点什么
    if (error.response) {
      // 检查是否是智能问答相关的API路径，如果是，则不触发退出登录逻辑
      const isAiApi = error.config.url.includes('/api/user/ai/');
      // 检查是否是退出登录请求
      const isLogoutRequest = error.config.url.includes('/logout');
      
      switch (error.response.status) {
        case 401:
          // 未授权，对于智能问答API，只显示错误信息，不清除token和跳转到登录页
          if (isAiApi) {
            ElMessage.error('API返回未授权错误');
          } else {
            // 检查是否是获取用户信息的API
            const isGetUserInfo = error.config.url.includes('/api/user/show');
            if (isGetUserInfo) {
              // 对于获取用户信息的API，只显示错误信息，不清除token和跳转到登录页
              // 这样可以保留本地存储的用户信息，避免刷新页面后用户信息丢失
              ElMessage.error('获取用户信息失败，保留本地存储的用户信息');
            } else {
              // 其他API，清除token并跳转到登录页
              const userStore = useUserStore();
              userStore.clearUserInfo();
              ElMessage.error('用户登录已过期');
              router.push('/login');
            }
          }
          break;
        case 403:
          ElMessage.error('没有权限访问');
          break;
        case 404:
          // 退出登录请求不显示404错误
          if (!isLogoutRequest) {
            ElMessage.error('请求的资源不存在');
          }
          break;
        case 500:
          ElMessage.error('服务器内部错误');
          break;
        default:
          ElMessage.error(error.response.data?.msg || error.response.statusText || '网络错误');
      }
    } else if (error.message) {
      // 处理超时等错误
      ElMessage.error(`服务器暂时无法响应：${error.message}`);
    } else {
      ElMessage.error('网络连接失败');
    }
    return Promise.reject(error);
  }
);

export default request;