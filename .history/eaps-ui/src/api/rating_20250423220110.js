import request from './axios'

// 获取企业评价列表
export const getCompanyRatings = (companyId, params) => {
  return request({
    url: `/companies/${companyId}/ratings`,
    method: 'get',
    params
  })
}

// 学生提交企业评价
export const submitRating = (companyId, data) => {
  return request({
    url: `/companies/${companyId}/ratings`,
    method: 'post',
    data
  })
}

// 举报不当评价
export const reportRating = (ratingId, data) => {
  return request({
    url: `/reports/ratings/${ratingId}`,
    method: 'post',
    data
  })
}

// 企业查看收到的评价
export const getCompanyReceivedRatings = (params) => {
  return request({
    url: '/company/ratings',
    method: 'get',
    params
  })
}

// 辅导员处理举报的评价
export const processCounselorReport = (reportId, data) => {
  return request({
    url: `/counselor/reports/${reportId}`,
    method: 'put',
    data
  })
} 