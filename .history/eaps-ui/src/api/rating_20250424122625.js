import request from './axios'

/**
 * 提交企业评价
 * @param {Object} data 评价数据
 * @returns {Promise} 提交结果
 */
export function submitRating(data) {
  return request({
    url: '/rating/submit',
    method: 'POST',
    data
  })
}

/**
 * 获取企业评价列表
 * @param {Number} companyId 企业ID
 * @param {Object} params 查询参数
 * @returns {Promise} 评价列表
 */
export function getRatingList(companyId, params = {}) {
  return request({
    url: '/rating/list',
    method: 'POST',
    data: {
      companyId,
      page: params.page || 1,
      pageSize: params.pageSize || 10,
      sortBy: params.sortBy || 'time'
    }
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
    url: '/report/submit',
    method: 'POST',
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