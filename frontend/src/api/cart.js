import request from './request';

// 购物车相关API
export const cartApi = {
  // 添加商品到购物车
  addToCart: (goodsId, quantity) => {
    return request({
      url: '/api/user/cart/insert',
      method: 'POST',
      params: {
        goodsId,
        quantity
      }
    });
  },

  // 获取购物车列表
  getCartList: () => {
    return request({
      url: '/api/user/cart/get',
      method: 'GET'
    });
  },

  // 更新购物车商品数量
  updateCartItem: (goodsId, quantity) => {
    return request({
      url: '/api/user/cart/update',
      method: 'PUT',
      params: {
        goodsId,
        quantity
      }
    });
  },

  // 删除购物车商品
  deleteCartItems: (goodsIds) => {
    return request({
      url: '/api/user/cart/delete',
      method: 'DELETE',
      params: {
        goodsIds
      }
    });
  }
};

//支付接口 goodList: [{goodsId: 1, quantity: 2}]
export const payApi = (couponId, goodList)=> {
  if (couponId !== null) {
    return request({
      url: '/api/user/order/pay',
      method: 'POST',
      params: {
        couponId
      },
      data: goodList
    });
  } else {
    return request({
      url: '/api/user/order/pay',
      method: 'POST',
      data: goodList
    });
  }
}

//秒杀商品支付接口
export const paySeckillGoods = (goodsId)=> {
  return request({
    url: '/api/user/seckill',
    method: 'GET',
    params: {
      goodsId
    }
  });
}