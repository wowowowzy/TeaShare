import request from './request';

//获取所有用户订单
export const getAllOrders = () => {
  return request({
    url: '/api/admin/goods/showApprovalList',
    method: 'get',
  });
};

//管理员审批订单
export const approvalOrder = (orderId) => {
  console.log('approvalOrder 接收的 orderId:', orderId);
  console.log('approvalOrder 接收的 orderId 类型:', typeof orderId);
  
  // 使用params参数方式传递orderId，确保经过请求拦截器处理
  return request({
    url: '/api/admin/goods/approval',
    method: 'post',
    params: {
      orderId
    }
  });
}