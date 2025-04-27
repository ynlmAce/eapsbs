import request from '@/utils/request'
import { handleResponse } from '@/utils/api'

/**
 * 提交企业评分评价
 * @param {Object} ratingData - 评分评价数据
 * @param {number} ratingData.companyId - 企业ID
 * @param {number} [ratingData.jobId] - 相关岗位ID
 * @param {number} ratingData.score - 总体评分(1-5)
 * @param {number} ratingData.jobTruthScore - 岗位真实性评分(1-5)
 * @param {number} ratingData.interviewScore - 面试体验评分(1-5)
 * @param {number} ratingData.environmentScore - 公司环境评分(1-5)
 * @param {number} ratingData.welfareScore - 福利兑现评分(1-5)
 * @param {string} ratingData.comment - 评价内容
 * @param {boolean} [ratingData.isAnonymous=true] - 是否匿名评价
 * @returns {Promise} - 返回操作结果
 */
export function submitRating(ratingData) {
  return request({
    url: '/api/rating/submit',
    method: 'post',
    data: ratingData
  }).then(handleResponse)
}

/**
 * 获取企业评价列表
 * @param {Object} params - 查询参数
 * @param {number} params.companyId - 企业ID
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @param {string} [params.sortBy='newest'] - 排序方式(newest, highest, lowest)
 * @returns {Promise} - 返回企业评价列表
 */
export function getCompanyRatings(params) {
  return request({
    url: '/api/rating/company-ratings',
    method: 'post',
    data: params
  }).then(handleResponse)
}

/**
 * 获取企业综合评分
 * @param {number} companyId - 企业ID
 * @returns {Promise} - 返回企业综合评分信息
 */
export function getCompanyRatingOverview(companyId) {
  return request({
    url: '/api/rating/overview',
    method: 'post',
    data: { companyId }
  }).then(handleResponse)
}

/**
 * 获取学生提交的评价列表
 * @param {Object} [params] - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @returns {Promise} - 返回学生提交的评价列表
 */
export function getStudentRatings(params) {
  return request({
    url: '/api/rating/student-ratings',
    method: 'post',
    data: params || {}
  }).then(handleResponse)
}

/**
 * 企业获取收到的评价列表
 * @param {Object} [params] - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @returns {Promise} - 返回企业收到的评价列表
 */
export function getCompanyReceivedRatings(params) {
  return request({
    url: '/api/rating/received-ratings',
    method: 'post',
    data: params || {}
  }).then(handleResponse)
}

/**
 * 举报不当评价
 * @param {number} ratingId - 评价ID
 * @param {string} reason - 举报原因
 * @returns {Promise} - 返回操作结果
 */
export function reportRating(ratingId, reason) {
  return request({
    url: '/api/rating/report',
    method: 'post',
    data: {
      ratingId,
      reason
    }
  }).then(handleResponse)
}

/**
 * 删除自己发布的评价
 * @param {number} ratingId - 评价ID
 * @returns {Promise} - 返回操作结果
 */
export function deleteRating(ratingId) {
  return request({
    url: '/api/rating/delete',
    method: 'post',
    data: { ratingId }
  }).then(handleResponse)
}

/**
 * 辅导员处理被举报的评价
 * @param {number} reportId - 举报记录ID
 * @param {string} action - 处理操作('keep'保留, 'delete'删除)
 * @param {string} [resolution] - 处理说明
 * @returns {Promise} - 返回操作结果
 */
export function handleReportedRating(reportId, action, resolution) {
  return request({
    url: '/api/rating/handle-report',
    method: 'post',
    data: {
      reportId,
      action,
      resolution
    }
  }).then(handleResponse)
} 