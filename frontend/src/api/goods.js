import request from './request';

// 获取所有商品列表
export const findAllGoods = (params) => {
  return request({
    url: '/api/user/findAllGoods',
    method: 'get',
    params: {
      page: params.page || 1,
      pageSize: params.pageSize || 10,
      goodsName: params.goodsName || '',
      categoryId: params.categoryId || ''
    }
  });
};

// 根据ID获取商品详情
export const findGoodById = (id) => {
  return request({
    url: '/api/user/findGoodById',
    method: 'get',
    params: {
      goodsId: id
    }
  });
};

// 根据分类ID获取商品列表
export const findGoodsByCategory = (categoryId, page = 1, pageSize = 10) => {
  return request({
    url: '/api/user/findAllGoods',
    method: 'get',
    params: {
      page: page,
      pageSize: pageSize,
      categoryId: categoryId
    }
  });
};

// 搜索商品
export const searchGoods = (goodsName, page = 1, pageSize = 10) => {
  return request({
    url: '/api/user/findAllGoods',
    method: 'get',
    params: {
      page: page,
      pageSize: pageSize,
      goodsName: goodsName
    }
  });
};

// 获取秒杀商品信息
export const getSeckillGoods = (params) => {
  return request({
    url: '/api/user/getAllSeckillGoods',
    method: 'get',
  });
};

//支付秒杀商品
export const paySeckillGoods = (id) => {
  return request({
    url: '/api/user/seckill',
    method: 'get',
    params: {
      goodsId: id
    }
  });
};

// 评价某个商品
export const commentGoods = (goodsId, content, level, file) => {
  const formData = new FormData();
  formData.append('goodsId', goodsId);
  formData.append('content', content);
  formData.append('level', level);
  if (file) {
    formData.append('file', file);
  }
  
  return request({
    url: '/api/user/review',
    method: 'post',
    data: formData
  });
};

//查看某个商品的评论
// 返回数据：
// {
//   "code": 1,
//     "msg": "success",
//       "data": [
//         {
//           "reviewId": 4,
//           "userId": 21,
//           "username": "xhz",
//           "content": "q11111",
//           "level": 1,
//           "likeCount": 1,
//           "image": "https://java-web-itheima-zy.oss-cn-beijing.aliyuncs.com/9748f64a-7cb1-4905-b867-2dff82805527.png"
//         }
//       ]
// }
export const getCommentByGoodsId = (goodsId) => {
  return request({
    url: '/api/user/getReviewByGoodsId',
    method: 'get',
    params: {
      goodsId: goodsId
    }
  });
}

//点赞取消评论
export const likeComment = (reviewId, cancel) => {
  return request({
    url: '/api/user/switchLikeReview',
    method: 'post',
    params: {
      reviewId: reviewId,//评论id
      cancel: cancel//是否取消点赞,1是点赞，-1是不点赞
    }
  });
}
