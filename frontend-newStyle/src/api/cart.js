import request from './request'

export const cartApi = {
  addToCart: (goodsId, quantity) =>
    request({ url: '/api/user/cart/insert', method: 'post', params: { goodsId, quantity } }),

  getCartList: () => request({ url: '/api/user/cart/get', method: 'get' }),

  updateCartItem: (goodsId, quantity) =>
    request({ url: '/api/user/cart/update', method: 'put', params: { goodsId, quantity } }),

  deleteCartItems: (goodsIds) =>
    request({ url: '/api/user/cart/delete', method: 'delete', params: { goodsIds } })
}

/**
 * 下单：goodList = [{ goodsId, quantity }]，couponId 可选。
 * 注意：该接口不接收收货地址参数。
 */
export const payApi = (couponId, goodList) =>
  request({
    url: '/api/user/order/pay',
    method: 'post',
    ...(couponId !== null && couponId !== undefined ? { params: { couponId } } : {}),
    data: goodList
  })
