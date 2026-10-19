import request from './request';
import { API_BASE } from '../utils/apiBase';

// 验证URL有效性
function isValidUrl(url) {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
}

// 检查URL可访问性
async function checkUrlAccessibility(url) {
  try {
    const response = await fetch(url, {
      method: 'HEAD',
      timeout: 5000
    });
    return response.ok;
  } catch (error) {
    console.error('URL访问检查失败:', error);
    return false;
  }
}

// 智能问答接口 - 支持流式数据
export const generateStream = async (message, sessionId, image = null, onChunk, onComplete, onError) => {
  // console.log('前端请求参数：', { message, sessionId, image: image ? (typeof image === 'string' ? 'URL' : '文件') : '无' });
  
  try {
    let url = `${API_BASE}/api/user/ai/generateStream`;
    let fetchOptions = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      credentials: 'include'
    };
    
    // 添加token到请求头
    const token = localStorage.getItem('token');
    if (token) {
      fetchOptions.headers['token'] = token;
    }
    
    // 构建请求参数
    const params = new URLSearchParams();
    params.append('message', message);
    params.append('sessionId', sessionId);
    
    if (image) {
      // 检查是否是图片URL
      if (typeof image === 'string' && isValidUrl(image)) {
        console.log('检测到图片URL:', image);
        
        // 检查URL可访问性
        const isAccessible = await checkUrlAccessibility(image);
        if (!isAccessible) {
          console.error('图片URL不可访问:', image);
          throw new Error('图片URL不可访问，请检查URL是否正确');
        }
        
        params.append('imageUrl', image);
      } else {
        // 使用 FormData 格式上传图片文件
        const formData = new FormData();
        formData.append('image', image);
        formData.append('message', message);
        formData.append('sessionId', sessionId);
        
        delete fetchOptions.headers['Content-Type'];
        fetchOptions.body = formData;
      }
    } else {
      // 普通文本请求
      fetchOptions.body = params;
    }
    
    // console.log('发起流式请求:', url);
    const response = await fetch(url, fetchOptions);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    // 处理流式响应
    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    
    while (true) {
      const { done, value } = await reader.read();
      
      if (done) {
        // console.log('流式响应结束');
        if (onComplete) {
          onComplete();
        }
        break;
      }
      
      const chunk = decoder.decode(value, { stream: true });
      
      // 去除data:前缀并过滤空行
      const cleanedChunk = chunk
        .split('\n')
        .map(line => line.replace(/data:/g, '').trim())
        .filter(line => line.length > 0)
        .join('');
      
      // 实时处理，确保数据一到达就立即传递给回调
      if (onChunk && cleanedChunk.length > 0) {
        // 立即调用回调，不做任何延迟
        onChunk(cleanedChunk);
      }
    }
  } catch (error) {
    console.error('API请求失败:', error);
    if (onError) {
      onError(error);
    }
    throw error;
  }
};

// 获取历史查询记录
export const getHistories = (userId) => {
  return request({
    url: '/api/user/ai/getHistories',
    method: 'get',
    params: {
      userId: userId
    }
  });
};

// 新建对话
export const createNewSession = () => {
  return request({
    url: '/api/user/ai/newSession',
    method: 'get'
  });
};