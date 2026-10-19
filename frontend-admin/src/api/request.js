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

// 自定义params序列化函数，确保大整数作为字符串传递
const customParamsSerializer = (params) => {
  const parts = [];
  for (const key in params) {
    if (params.hasOwnProperty(key)) {
      let value = params[key];
      // 将所有值转换为字符串，避免数字精度问题
      if (value !== null && value !== undefined) {
        value = String(value);
      }
      parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`);
    }
  }
  return parts.join('&');
};

// 创建axios实例
const request = axios.create({
  baseURL: API_BASE, // 后端API地址（默认相对路径，由 Nginx/代理转发）
  timeout: 100000, // 增加超时时间到100秒
  withCredentials: true, // 携带cookie
  // 全局自定义params序列化器
  paramsSerializer: customParamsSerializer,
  // 自定义响应转换
  transformResponse: [function (data, headers) {
    // 如果是Blob或ArrayBuffer类型的响应，直接返回原始数据
    // 这种情况通常用于文件下载或图片获取
    if (data instanceof Blob || data instanceof ArrayBuffer || typeof data === 'object' && data !== null && data.type && data.type.includes('image')) {
      return data;
    }
    
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
    console.log('========== 请求拦截器 ==========');
    console.log('localStorage中的token:', token);
    console.log('请求URL:', config.url);
    console.log('请求params:', config.params);

    // 处理订单审批接口的orderId参数，确保作为字符串传递
    if (config.url && config.url.includes('/api/admin/goods/approval')) {
      console.log('检测到审批接口');
      
      if (config.params && config.params.orderId) {
        console.log('原始orderId:', config.params.orderId);
        console.log('orderId类型:', typeof config.params.orderId);
        
        // 强制转换为字符串
        const originalOrderId = config.params.orderId;
        config.params.orderId = String(originalOrderId);
        
        console.log('处理后orderId:', config.params.orderId);
      }
      
      if (config.params) {
        console.log('完整URL:', config.baseURL + config.url + '?' + customParamsSerializer(config.params));
      }
    }

    if (token) {
      config.headers['token'] = `${token}`;
      console.log('已设置token到请求头');
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
    console.log('请求头token:', config.headers['token']);
    console.log('=================================');
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
    // 不打印Blob数据，避免消耗Blob对象
    if (response.config.responseType !== 'blob' && response.config.responseType !== 'arraybuffer') {
      console.log('响应数据:', response.data);
    } else {
      console.log('响应类型:', response.config.responseType);
      console.log('响应数据大小:', response.data?.size || '未知');
    }

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

      // 检查当前请求的URL，处理订单相关的API
      const url = response.config.url;
      if (url && (url.includes('/user/order/') || url.includes('/api/admin/goods/showApprovalList'))) {
        // 处理订单数据，确保订单ID和商品ID作为字符串处理以避免数字精度问题
        if (Array.isArray(data)) {
          // 订单列表
          return data.map(item => {
            // 处理订单ID
            let processedItem = {
              ...item,
              orderId: item.orderId?.toString() || item.orderId
            };
            
            // 处理订单商品的商品ID
            if (processedItem.orderDTOList && Array.isArray(processedItem.orderDTOList)) {
              processedItem.orderDTOList = processedItem.orderDTOList.map(goods => ({
                ...goods,
                goodsId: goods.goodsId?.toString() || goods.goodsId
              }));
            }
            
            return processedItem;
          });
        } else if (data && data.orderId) {
          // 单个订单
          let processedData = {
            ...data,
            orderId: data.orderId.toString()
          };
          
          // 处理订单商品的商品ID
          if (processedData.orderDTOList && Array.isArray(processedData.orderDTOList)) {
            processedData.orderDTOList = processedData.orderDTOList.map(goods => ({
              ...goods,
              goodsId: goods.goodsId?.toString() || goods.goodsId
            }));
          }
          
          return processedData;
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