import request from './request';

//获取所有商品列表
export const getAllGoods = (page, pageSize, goodsName, categoryId) => {
  return request({
    url: '/api/admin/goods/findAll',
    method: 'get',
    params: {
      page: page,
      pageSize: pageSize,
      goodsName: goodsName,
      categoryId: categoryId,
    }
  });
};

//查询商品详情
export const getGoodsDetail = (goodsId) => {
  return request({
    url: '/api/admin/goods/findGoodById',
    method: 'get',
    params: {
      goodsId: goodsId,
    }
  });
}

//添加商品
// 传入的数据格式：{
//   "goodsName": "红茶",
//   "goodsIntro": "esse id est",
//   "goodsPrice": 597.59,
//   "categoryId": 66,
//   "stockNum": 59,
//   "goodsImage": "https://loremflickr.com/400/400?lock=4104188464812772"
// }
export const addGoods = (goods) => {
  return request({
    url: '/api/admin/goods/add',
    method: 'post',
    data: goods
  });
}

//修改商品
export const updateGoods = (goods) => {
  // 提取goodsId作为URL参数
  const { goodsId, ...goodsData } = goods;
  return request({
    url: '/api/admin/goods/update',
    method: 'post',
    params: {
      goodsId: goodsId
    },
    data: goodsData
  });
}

//删除商品
export const deleteGoods = (goodsId) => {
  return request({
    url: '/api/admin/goods/delete',
    method: 'delete',
    params: {
      goodsId: goodsId,
    }
  });
}

//阿里oss
export const uploadGoodsImage = (file) => {
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