import request from './request'

/** 随机抽 5 道题 */
export const getQuizQuestions = () => request({ url: '/api/user/getQuestion', method: 'get' })

/**
 * 提交答案：后端接收 questionId[] 与 optionLabel[] 两个数组。
 * 用 axios 的数组序列化（questionId=1&questionId=2&...）而非逗号拼接。
 */
export const submitQuizAnswers = (questionIds, optionLabels) =>
  request({
    url: '/api/user/getAnswer',
    method: 'get',
    params: { questionId: questionIds, optionLabel: optionLabels },
    paramsSerializer: {
      indexes: null
    }
  })
