import request from './request'
import { API_BASE } from '../utils/apiBase'

/* ------------------------------ 用户 ------------------------------ */

export const login = (username, password) =>
  request({ url: '/api/user/login', method: 'post', data: { username, password } })

export const register = (username, password, phone) =>
  request({ url: '/api/user/register', method: 'post', data: { username, password, phone } })

export const logout = () => request({ url: '/api/user/logout', method: 'post' })

export const getUserShow = () => request({ url: '/api/user/show', method: 'get' })

export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/user/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/* ------------------------------ 验证码 ------------------------------ */

/**
 * 验证码图片：返回 blob，用 fetch 以携带第一方 Session Cookie。
 * 与 verifyCaptcha 必须使用同一 Cookie 会话。
 */
export async function fetchCaptchaImage() {
  const res = await fetch(`${API_BASE}/api/user/captcha/image`, {
    method: 'GET',
    credentials: 'include',
    cache: 'no-store'
  })
  if (!res.ok) throw new Error(`验证码获取失败：${res.status}`)
  return res.blob()
}

export async function verifyCaptcha(code) {
  const params = new URLSearchParams()
  params.append('code', code)
  const res = await fetch(`${API_BASE}/api/user/captcha/verify`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params
  })
  if (!res.ok) throw new Error(`验证码校验失败：${res.status}`)
  return res.json() // { code, msg, data }
}

/* ------------------------------ 订单 ------------------------------ */

export const getOrders = () => request({ url: '/api/user/order/getOrders', method: 'get' })

export const getOrderById = (orderId) =>
  request({ url: '/api/user/order/getOrderById', method: 'get', params: { orderId } })

/* ------------------------------ 地址 ------------------------------ */

export const getAddressList = () => request({ url: '/api/user/address/list', method: 'get' })

export const addAddress = (address) =>
  request({ url: '/api/user/address/add', method: 'post', data: address })

export const updateAddress = (address) =>
  request({
    url: '/api/user/address/update',
    method: 'put',
    data: address,
    params: { addressId: address.id }
  })

export const deleteAddress = (id) =>
  request({ url: '/api/user/address/delete', method: 'delete', params: { id } })

/* ------------------------------ 优惠券 ------------------------------ */

export const getCoupons = () => request({ url: '/api/user/coupon/getCoupon', method: 'get' })
