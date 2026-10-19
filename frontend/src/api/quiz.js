import request from './request';

export const getQuizQuestions = () => {
  return request({
    url: '/api/user/getQuestion',
    method: 'get'
  });
};

export const submitQuizAnswers = (questionIds, optionLabels) => {
  return request({
    url: '/api/user/getAnswer',
    method: 'get',
    params: {
      questionId: questionIds.join(','),
      optionLabel: optionLabels.join(',')
    }
  });
};
