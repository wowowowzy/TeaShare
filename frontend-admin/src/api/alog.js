import request from './request';

//获取日志
export const getLog = () => {
  return request({
    url: '/api/admin/log/showLog',
    method: 'get',
  });
};