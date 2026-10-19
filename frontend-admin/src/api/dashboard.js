import request from './request';

//获取年度月度订单销售数量统计
//案例：{
// "code": 1,
//   "msg": "success",
//     "data": [
//       {
//         "year": 2026,
//         "month": 1,
//         "num": 1
//       },
//       {
//         "year": 2026,
//         "month": 2,
//         "num": 1
//       },
//       {
//         "year": 2026,
//         "month": 3,
//         "num": 46
//       }
//     ]
// }
export const getOrderCountStatistics = () => {
  return request({
    url: '/api/admin/echart/showOrderCount',
    method: 'get',
  });
};

//获取年度月度客户注册数量统计
//案例：{
// "code": 1,
//   "msg": "success",
//     "data": [
//       {
//         "year": 2026,
//         "month": 1,
//         "num": 1
//       },
//       {
//         "year": 2026,
//         "month": 2,
//         "num": 1
//       },
//       {
//         "year": 2026,
//         "month": 3,
//         "num": 18
//       }
//     ]
// }
export const getCustomerCountStatistics = () => {
  return request({
    url: '/api/admin/echart/showRegistrationCount',
    method: 'get',
  });
}

//获取商品种类购买数量分布统计  
//案例：{
// "code": 1,
//   "msg": "success",
//     "data": [
//       {
//         "categoryId": 1,
//         "orderCount": 44
//       },
//       {
//         "categoryId": 2,
//         "orderCount": 2
//       },
//       {
//         "categoryId": 5,
//         "orderCount": 1
//       },
//       {
//         "categoryId": 4,
//         "orderCount": 2
//       },
//       {
//         "categoryId": 3,
//         "orderCount": 2
//       },
//       {
//         "categoryId": 6,
//         "orderCount": 2
//       }
//     ]
// }
export const getGoodsCategoryCountStatistics = () => {
  return request({
    url: '/api/admin/echart/showOrderCategoryCount',
    method: 'get',
  });
}

//仪表盘数据展示
// //案例：{
// "code": 1,
//   "msg": "success",
//     "data": {
//   "goodsTotal": 81,
//     "orderTotal": 46,
//       "userTotal": 20,
//         "priceTotal": 21448.00
// }
// }
export const getDashboardData = () => {
  return request({
    url: '/api/admin/echart/showData',
    method: 'get',
  });
}