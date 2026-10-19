import request from './request';

//猜你想看
export const getGuessYouLike = () => {
  return request({
    url: '/api/user/community/maybeLike',
    method: 'get'
  });
};

export const getPostList = (params) => {
  return request({
    url: '/api/user/community/getPostList',
    method: 'get',
    params
  });
};

export const getPostDetail = (postId) => {
  return request({
    url: '/api/user/community/getPostDetail',
    method: 'get',
    params: { postId }
  });
};

export const createPost = (data) => {
  return request({
    url: '/api/user/community/createPost',
    method: 'post',
    data
  });
};

export const updateMyPost = (data) => {
  return request({
    url: '/api/user/community/updateMyPost',
    method: 'put',
    data
  });
};

export const deleteMyPost = (postId) => {
  return request({
    url: '/api/user/community/deleteMyPost',
    method: 'delete',
    params: { postId }
  });
};

export const getMyPost = (params) => {
  return request({
    url: '/api/user/community/getMyPost',
    method: 'get',
    params
  });
};

export const getCollect = (params) => {
  return request({
    url: '/api/user/community/getCollect',
    method: 'get',
    params
  });
};

export const comment = (data) => {
  return request({
    url: '/api/user/community/comment',
    method: 'post',
    data
  });
};

export const updateComment = (commentId, data) => {
  return request({
    url: `/api/user/community/updateComment/${commentId}`,
    method: 'put',
    data
  });
};

export const deleteComment = (commentId) => {
  return request({
    url: '/api/user/community/deleteComment',
    method: 'delete',
    params: { commentId: commentId }
  });
};

export const getComments = (postId) => {
  return request({
    url: '/api/user/community/getComments',
    method: 'get',
    params: { postId }
  });
};

export const uploadPostImage = (postId, formData) => {
  return request({
    url: '/api/user/uploadPostImage',
    method: 'post',
    params: { postId },
    data: formData
  });
};

export const likePost = (postId) => {
  return request({
    url: '/api/user/community/switchLikePost',
    method: 'post',
    params: { postId, cancel: 1 }
  });
};

export const unlikePost = (postId) => {
  return request({
    url: '/api/user/community/switchLikePost',
    method: 'post',
    params: { postId, cancel: -1 }
  });
};

export const favoritePost = (postId) => {
  return request({
    url: '/api/user/community/switchCollect',
    method: 'post',
    params: { postId, cancel: 1 }
  });
};

export const unfavoritePost = (postId) => {
  return request({
    url: '/api/user/community/switchCollect',
    method: 'post',
    params: { postId, cancel: -1 }
  });
};

export const likeComment = (commentId) => {
  return request({
    url: '/api/user/community/switchLikeComment',
    method: 'post',
    params: { commentId, cancel: 1 }
  });
};

export const unlikeComment = (commentId) => {
  return request({
    url: '/api/user/community/switchLikeComment',
    method: 'post',
    params: { commentId, cancel: -1 }
  });
};
