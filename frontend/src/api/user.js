import request from './request';
import { API_BASE } from '../utils/apiBase';

// 用户 管理员登录
export const login = (username, password) => {
  console.log('前端请求参数：', { username, password })
  return request({
    url: '/api/user/login',
    method: 'post',
    data: {
      username: username,
      password: password
    }
  });
};

// 用户注册
export const register = (username, password, phone) => {
  console.log('注册API接收到的数据:', { username, password, phone });
  return request({
    url: '/api/user/register',
    method: 'post',
    data: {
      username: username,
      password: password,
      phone: phone
    }
  });
};



// 退出登录
export const logout = () => {
  return request({
    url: '/api/user/logout',
    method: 'post'
  });
};

// 生成验证码图片
export const getCaptchaImage = () => {
  return request({
    url: '/api/user/captcha/image',
    method: 'get',
    withCredentials: true // 确保携带cookie
  });
};

// 验证验证码
export const verifyCaptcha = (code) => {
  const params = new URLSearchParams();
  params.append('code', code);

  // console.log('验证验证码，code:', code);
  // console.log('当前cookie:', document.cookie);

  // 使用fetch API，确保与验证码生成请求使用相同的cookie处理机制
  return fetch(`${API_BASE}/api/user/captcha/verify`, {
    method: 'POST',
    credentials: 'include', // 携带cookie
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: params
  }).then(response => {
    console.log('验证码验证响应状态:', response.status);
    console.log('验证码验证响应头:', {
      'set-cookie': response.headers.get('set-cookie'),
      'access-control-allow-credentials': response.headers.get('access-control-allow-credentials'),
      'access-control-allow-origin': response.headers.get('access-control-allow-origin')
    });
    return response.json();
  }).then(data => {
    console.log('验证码验证结果:', data);
    return data;
  });
};

// 上传头像
export const uploadAvatar = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return request({
    url: '/api/user/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

// 获取用户信息（新接口）
export const getUserShow = () => {
  return request({
    url: '/api/user/show',
    method: 'get'
  });
};

// 获取用户订单列表
export const getOrders = () => {
  return request({
    url: '/api/user/order/getOrders',
    method: 'get'
  });
};

// 获取订单详情
export const getOrderById = (orderId) => {
  return request({
    url: '/api/user/order/getOrderById',
    method: 'get',
    params: {
      orderId
    }
  });
};

// 获取地址列表
export const getAddressList = () => {
  return request({
    url: '/api/user/address/list',
    method: 'get'
  });
};

// 添加地址
export const addAddress = (address) => {
  return request({
    url: '/api/user/address/add',
    method: 'post',
    data: address
  });
};

// 更新地址
export const updateAddress = (address) => {
  return request({
    url: '/api/user/address/update',
    method: 'put',
    data: address,
    params: {
      addressId: address.id
    },
  });
};

// 删除地址
export const deleteAddress = (id) => {
  return request({
    url: '/api/user/address/delete',
    method: 'delete',
    params: {
      id
    }
  });
};

// 获取优惠券列表
export const getCoupons = () => {
  return request({
    url: '/api/user/coupon/getCoupon',
    method: 'get'
  });
};