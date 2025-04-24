import request from '@/utils/request'

/**
 * 提交企业评价
 * @param {Object} data 评价数据
 * @returns {Promise} 提交结果
 */
export function submitRating(data) {
  return request({
    url: '/api/rating/submit',
    method: 'post',
    data
  })
}

/**
 * 获取企业评价列表
 * @param {Number} companyId 企业ID
 * @param {Object} params 查询参数
 * @returns {Promise} 评价列表
 */
export function getRatingList(data) {
  return request({
    url: '/api/rating/list',
    method: 'post',
    data
  })
}

/**
 * 举报评价
 * @param {Number} ratingId 评价ID
 * @param {String} reason 举报原因
 * @returns {Promise} 举报结果
 */
export function reportRating(ratingId, reason) {
  return request({
    url: '/api/report/submit',
    method: 'post',
    data: {
      reportedItemType: 'rating',
      reportedItemId: ratingId,
      reason
    }
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