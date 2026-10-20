import request from './request'

/* ------------------------------ 商品 ------------------------------ */

export const findAllGoods = ({ page = 1, pageSize = 10, goodsName = '', categoryId = '' } = {}) =>
  request({
    url: '/api/user/findAllGoods',
    method: 'get',
    params: { page, pageSize, goodsName, categoryId }
  })

export const findGoodById = (goodsId) =>
  request({ url: '/api/user/findGoodById', method: 'get', params: { goodsId } })

export const getSeckillGoods = () => request({ url: '/api/user/getAllSeckillGoods', method: 'get' })

/** 秒杀下单：返回值是提示文案字符串 */
export const paySeckillGoods = (goodsId) =>
  request({ url: '/api/user/seckill', method: 'get', params: { goodsId } })

/* ------------------------------ 评论 ------------------------------ */

export const commentGoods = (goodsId, content, level, file) => {
  const formData = new FormData()
  formData.append('goodsId', goodsId)
  formData.append('content', content)
  formData.append('level', level)
  if (file) formData.append('file', file)
  return request({
    url: '/api/user/review',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const getCommentByGoodsId = (goodsId) =>
  request({ url: '/api/user/getReviewByGoodsId', method: 'get', params: { goodsId } })

export const likeComment = (reviewId, cancel) =>
  request({
    url: '/api/user/switchLikeReview',
    method: 'post',
    params: { reviewId, cancel }
  })

export const getMyReview = () => request({ url: '/api/user/getMyReview', method: 'get' })
