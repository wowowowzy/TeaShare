import request from './request'

/* 猜你喜欢（推荐） */
export const getGuessYouLike = () =>
  request({ url: '/api/user/community/maybeLike', method: 'get' })

/* 帖子列表（分页 + 搜索） */
export const getPostList = (params) =>
  request({ url: '/api/user/community/getPostList', method: 'get', params })

export const getPostDetail = (postId) =>
  request({ url: '/api/user/community/getPostDetail', method: 'get', params: { postId } })

export const createPost = (data) =>
  request({ url: '/api/user/community/createPost', method: 'post', data })

export const updateMyPost = (data) =>
  request({ url: '/api/user/community/updateMyPost', method: 'put', data })

export const deleteMyPost = (postId) =>
  request({ url: '/api/user/community/deleteMyPost', method: 'delete', params: { postId } })

export const getMyPost = () => request({ url: '/api/user/community/getMyPost', method: 'get' })

export const getCollect = () => request({ url: '/api/user/community/getCollect', method: 'get' })

/* 评论 */
export const comment = (data) =>
  request({ url: '/api/user/community/comment', method: 'post', data })

export const deleteComment = (commentId) =>
  request({ url: '/api/user/community/deleteComment', method: 'delete', params: { commentId } })

/* 点赞 / 收藏：cancel = 1 操作，-1 取消 */
export const switchLikePost = (postId, cancel) =>
  request({ url: '/api/user/community/switchLikePost', method: 'post', params: { postId, cancel } })

export const switchCollect = (postId, cancel) =>
  request({ url: '/api/user/community/switchCollect', method: 'post', params: { postId, cancel } })

export const switchLikeComment = (commentId, cancel) =>
  request({
    url: '/api/user/community/switchLikeComment',
    method: 'post',
    params: { commentId, cancel }
  })

/* 帖子图片上传 */
export const uploadPostImage = (postId, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/user/uploadPostImage',
    method: 'post',
    params: { postId },
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
