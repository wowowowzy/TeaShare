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

/**
 * 后端全局异常处理器返回的错误体形如 {"code":0,"msg":"..."}；
 * 流式接口的兜底事件形如 {"type":"error","code":0,"msg":"..."}。
 * 两者都识别，返回可读 msg；不是错误体则返回 null。
 *
 * 背景：上游大模型异常（如 Connection reset）时，Spring 的全局异常处理器会把
 * Result JSON 写进同一条 SSE 流。若直接当文本渲染，用户就会看到一串裸 JSON。
 */
function parseBackendError(text) {
  if (!text) return null;
  const t = String(text).trim();
  if (!t.startsWith('{')) return null;
  try {
    const obj = JSON.parse(t);
    if (!obj || typeof obj !== 'object') return null;
    // 流式兜底事件
    if (obj.type === 'error') return obj.msg || 'AI 服务暂时不可用，请稍后重试';
    // 普通接口错误体（code !== 1）
    if (typeof obj.code === 'number' && obj.code !== 1 && obj.code !== 200) {
      return obj.msg || 'AI 服务异常';
    }
  } catch {
    // 不是完整 JSON，忽略
  }
  return null;
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
      // 非 2xx 时后端也可能返回 Result JSON，尽量取出 msg
      const errText = await response.text().catch(() => '');
      throw new Error(parseBackendError(errText) || `AI 服务请求失败（HTTP ${response.status}）`);
    }
    
    // 处理流式响应
    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let received = '';
    let receivedSent = false;
    
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
      
      if (cleanedChunk.length === 0) continue;
      
      if (!receivedSent) {
        // 尚未开始渲染：先累积，用来判断整段流是否为后端错误体。
        // 阈值取小一点，既够识别 JSON 错误体，又不明显拖慢首字显示。
        received += cleanedChunk;
        const looksLikeJson = received.trim().startsWith('{');
        const asError = looksLikeJson ? parseBackendError(received) : null;
        
        if (asError) {
          // 是错误体：继续累积到流结束，不渲染
          continue;
        }
        if (!looksLikeJson || received.length >= 120) {
          receivedSent = true;
          if (onChunk) onChunk(received);
        }
        continue;
      }
      
      // 实时处理，确保数据一到达就立即传递给回调
      if (onChunk) {
        onChunk(cleanedChunk);
      }
    }
    
    // 全程未渲染：要么是错误体，要么是很短的正常回复
    if (!receivedSent) {
      const backendMsg = parseBackendError(received);
      if (backendMsg) {
        throw new Error(backendMsg);
      }
      if (received.trim() && onChunk) {
        onChunk(received);
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