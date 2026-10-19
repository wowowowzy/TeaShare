import request from './request';

//获取秒杀商品
export const getSeckillGoods = () => {
  return request({
    url: '/api/admin/seckill/getAllSeckillGoods',
    method: 'get',
  });
};

//设置秒杀商品
export const setSeckillGoods = (goodsId, stock, goodsPrice) => {
  return request({
    url: '/api/admin/seckill/initStock',
    method: 'post',
    params: { goodsId, stock, goodsPrice },
  });
};
