import request from './request';

// 管理员登录
export const login = (username, password) => {
  console.log('前端请求参数：', { username, password })
  return request({
    url: '/api/admin/user/login',
    method: 'post',
    data: {
      username: username,
      password: password
    }
  });
};

// 管理员登录（别名，用于兼容现有代码）
export const userLogin = login;



// 管理员获取用户信息
export const getUserShow = (page, pageSize, username) => {
  console.log('前端请求参数：', { page, pageSize, username })
  return request({
    url: '/api/admin/user/showUserAdmin',
    method: 'post',
    params: {
      page: page,
      pageSize: pageSize,
      username: username
    }
  });
};

//封禁用户
export const banUser = (userId) => {
  console.log('前端请求参数：', { userId})
  return request({
    url: '/api/admin/user/banUser',
    method: 'post',
    params: {
      userId: userId
    }
  });
}

//解封用户
export const unbanUser = (userId) => {
  console.log('前端请求参数：', { userId})
  return request({
    url: '/api/admin/user/switchBanUser',
    method: 'post',
    params: {
      userId: userId
    }
  });
}

// 存储sessionId
let currentSessionId = '';

/**
 * 获取图形验证码
 * 后端生成包含随机字符、干扰线等防机器识别措施的验证码图片
 * @returns {Promise<Blob>} 验证码图片Blob数据
 */
export const getCaptchaImage = () => {
  // 添加时间戳防止浏览器缓存
  const timestamp = new Date().getTime();
  
  return request({
    url: `/api/user/captcha/image?t=${timestamp}`,
    method: 'get',
    responseType: 'blob', // 指定响应类型为blob，用于接收图片数据
    withCredentials: true // 确保携带cookie，用于会话跟踪
  }).then(response => {
    console.log('验证码响应数据:', response);
    console.log('响应类型:', typeof response);
    console.log('响应是否为Blob:', response instanceof Blob);
    
    // 确保返回的是Blob对象
    if (!(response instanceof Blob)) {
      console.error('返回数据不是Blob对象:', response);
      throw new Error('验证码获取失败：返回数据格式错误');
    }
    
    console.log('Blob类型:', response.type);
    console.log('Blob大小:', response.size);
    
    // 检查返回的是否是图片类型
    if (response.type && !response.type.startsWith('image/')) {
      // 如果不是图片，可能是错误信息
      return response.text().then(text => {
        console.error('验证码接口返回非图片数据:', text);
        throw new Error('验证码获取失败：服务器返回错误');
      });
    }
    return response;
  });
};

/**
 * 验证用户输入的验证码
 * @param {string} code - 用户输入的验证码
 * @returns {Promise<Object>} 验证结果对象
 *   - success: boolean 验证是否成功
 *   - message: string 验证结果消息
 */
export const verifyCaptcha = (code) => {
  // 参数验证
  if (!code || typeof code !== 'string') {
    return Promise.reject(new Error('验证码不能为空'));
  }
  
  // 去除前后空格
  const trimmedCode = code.trim();
  
  if (trimmedCode.length === 0) {
    return Promise.reject(new Error('验证码不能为空'));
  }
  
  // 验证码长度校验（通常验证码为4-6位）
  if (trimmedCode.length < 4 || trimmedCode.length > 6) {
    return Promise.reject(new Error('验证码长度不正确'));
  }

  console.log('验证验证码，code:', trimmedCode);

  // 使用统一的request实例发送请求
  return request({
    url: '/api/user/captcha/verify',
    method: 'post',
    params: {
      code: trimmedCode
    },
    withCredentials: true // 确保携带cookie
  }).then(response => {
    console.log('验证码验证结果:', response);
    
    // 统一返回格式
    if (response && (response.code === 1 || response.code === 200)) {
      return {
        success: true,
        message: response.msg || '验证成功',
        data: response.data
      };
    } else {
      return {
        success: false,
        message: response?.msg || '验证失败',
        data: response?.data
      };
    }
  }).catch(error => {
    console.error('验证码验证请求失败:', error);
    throw new Error(error.message || '验证码验证请求失败');
  });
};