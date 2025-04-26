import request from '@/utils/request';
import { handleResponse } from '@/utils/api';

/**
 * 获取学生投递记录列表
 * @param {Object} params - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @param {string} [params.status] - 筛选状态
 * @returns {Promise} - 返回投递记录列表
 */
export function getApplicationList(params) {
  return request({
    url: '/application/list',
    method: 'post',
    data: params || {}
  }).then(handleResponse);
}

/**
 * 获取特定投递记录详情
 * @param {number} applicationId - 投递记录ID
 * @returns {Promise} - 返回投递记录详情
 */
export function getApplicationDetail(applicationId) {
  return request({
    url: '/application/detail',
    method: 'post',
    data: { applicationId }
  }).then(handleResponse);
}

/**
 * 学生撤回投递申请
 * @param {number} applicationId - 投递记录ID
 * @returns {Promise} - 返回操作结果
 */
export function withdrawApplication(applicationId) {
  return request({
    url: '/api/job/application/withdraw',
    method: 'post',
    data: { applicationId }
  }).then(handleResponse);
}

/**
 * 企业查看学生简历（标记为已读）
 * @param {number} applicationId - 投递记录ID
 * @returns {Promise} - 返回操作结果
 */
export function viewStudentResume(applicationId) {
  return request({
    url: '/application/view-resume',
    method: 'post',
    data: { applicationId }
  }).then(handleResponse);
}

/**
 * 下载学生简历
 * @param {number} resumeFileId - 简历文件ID
 * @returns {Promise} - 返回blob数据
 */
export function downloadResume(resumeFileId) {
  return request({
    url: '/application/download-resume',
    method: 'post',
    data: { resumeFileId },
    responseType: 'blob'
  });
}

/**
 * 企业发送面试邀请
 * @param {number} applicationId - 投递记录ID
 * @param {Object} interviewData - 面试信息
 * @param {string} interviewData.time - 面试时间
 * @param {string} interviewData.location - 面试地点
 * @param {string} interviewData.contact - 联系人
 * @param {string} interviewData.contactInfo - 联系方式
 * @param {string} interviewData.note - 备注信息
 * @returns {Promise} - 返回操作结果
 */
export function sendInterviewInvitation(applicationId, interviewData) {
  return request({
    url: '/application/interview-invitation',
    method: 'post',
    data: {
      applicationId,
      ...interviewData
    }
  }).then(handleResponse);
}

/**
 * 学生回复面试邀请
 * @param {number} applicationId - 投递记录ID
 * @param {boolean} accept - 是否接受面试
 * @param {string} [reason] - 拒绝原因
 * @returns {Promise} - 返回操作结果
 */
export function replyToInterviewInvitation(applicationId, accept, reason) {
  return request({
    url: '/application/reply-interview',
    method: 'post',
    data: {
      applicationId,
      accept,
      reason
    }
  }).then(handleResponse);
}

/**
 * 企业拒绝学生申请
 * @param {number} applicationId - 投递记录ID
 * @param {string} [reason] - 拒绝原因
 * @returns {Promise} - 返回操作结果
 */
export function rejectApplication(applicationId, reason) {
  return request({
    url: '/application/reject',
    method: 'post',
    data: {
      applicationId,
      reason
    }
  }).then(handleResponse);
}

/**
 * 企业录用学生
 * @param {number} applicationId - 投递记录ID
 * @param {Object} offerData - 录用信息
 * @param {string} offerData.position - 录用职位
 * @param {string} offerData.department - 部门
 * @param {string} offerData.salary - 薪资
 * @param {string} offerData.startDate - 入职日期
 * @param {string} offerData.note - 备注信息
 * @returns {Promise} - 返回操作结果
 */
export function sendOffer(applicationId, offerData) {
  return request({
    url: '/application/offer',
    method: 'post',
    data: {
      applicationId,
      ...offerData
    }
  }).then(handleResponse);
}

/**
 * 学生回复录用邀请
 * @param {number} applicationId - 投递记录ID
 * @param {boolean} accept - 是否接受录用
 * @param {string} [reason] - 拒绝原因
 * @returns {Promise} - 返回操作结果
 */
export function replyToOffer(applicationId, accept, reason) {
  return request({
    url: '/application/reply-offer',
    method: 'post',
    data: {
      applicationId,
      accept,
      reason
    }
  }).then(handleResponse);
}

/**
 * 获取学生的所有应用状态数量统计
 * @returns {Promise} - 返回状态统计数据
 */
export function getApplicationStatusCounts() {
  return request({
    url: '/application/status-counts',
    method: 'post',
    data: {}
  }).then(handleResponse);
}

/**
 * 企业获取招聘流程各状态申请数量统计
 * @param {number} [jobId] - 可选的岗位ID，不传则统计所有岗位
 * @returns {Promise} - 返回状态统计数据
 */
export function getRecruitmentStatusCounts(jobId) {
  return request({
    url: '/application/recruitment-status-counts',
    method: 'post',
    data: jobId ? { jobId } : {}
  }).then(handleResponse);
} 