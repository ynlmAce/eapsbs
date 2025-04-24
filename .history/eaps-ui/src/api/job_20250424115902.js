import request from './axios'

/**
 * 获取岗位列表
 * @param {Object} params 查询参数
 * @returns {Promise} 岗位列表数据
 */
export function getJobList(params) {
  return request({
    url: '/api/job/list',
    method: 'POST',
    data: params || {}
  })
}

/**
 * 获取岗位详情
 * @param {Number} jobId 岗位ID
 * @returns {Promise} 岗位详情数据
 */
export function getJobDetail(jobId) {
  return request({
    url: '/api/job/detail',
    method: 'POST',
    data: { jobId }
  })
}

/**
 * 企业发布新岗位
 * @param {Object} data 岗位信息
 * @returns {Promise} 发布结果
 */
export function publishJob(data) {
  return request({
    url: '/api/job/publish',
    method: 'POST',
    data
  })
}

/**
 * 学生投递简历
 * @param {Number} jobId 岗位ID
 * @param {Number} resumeId 简历ID
 * @param {String} coverLetter 求职信
 * @returns {Promise} 投递结果
 */
export function applyJob(jobId, resumeId, coverLetter) {
  return request({
    url: '/api/job/apply',
    method: 'POST',
    data: {
      jobId,
      resumeId,
      coverLetter
    }
  })
}

/**
 * 获取学生投递记录
 * @param {Object} params 查询参数
 * @returns {Promise} 投递记录列表
 */
export function getStudentApplications(params) {
  return request({
    url: '/api/job/application/list/student',
    method: 'POST',
    data: params || {}
  })
}

/**
 * 获取企业收到的申请
 * @param {Object} params 查询参数
 * @returns {Promise} 申请列表
 */
export function getCompanyApplications(params) {
  return request({
    url: '/api/job/application/list/company',
    method: 'POST',
    data: params || {}
  })
}

/**
 * 企业处理申请
 * @param {Number} applicationId 申请ID
 * @param {Object} data 处理数据
 * @returns {Promise} 处理结果
 */
export function processApplication(applicationId, data) {
  return request({
    url: '/api/job/application/process',
    method: 'POST',
    data: {
      applicationId,
      ...data
    }
  })
}

/**
 * 企业批量下载简历
 * @param {Array} applicationIds 申请ID数组
 * @returns {Promise} 下载结果
 */
export function batchDownloadResumes(applicationIds) {
  return request({
    url: '/api/job/application/batch-download',
    method: 'POST',
    data: { applicationIds },
    responseType: 'blob'
  })
}

// 企业更新岗位
export const updateJob = (jobId, data) => {
  return request({
    url: `/company/jobs/${jobId}`,
    method: 'put',
    data
  })
}

// 企业删除岗位
export const deleteJob = (jobId) => {
  return request({
    url: `/company/jobs/${jobId}`,
    method: 'delete'
  })
}

// 企业刷新岗位
export const refreshJob = (jobId) => {
  return request({
    url: `/company/jobs/${jobId}/refresh`,
    method: 'put'
  })
}

// 企业收藏学生
export const favoriteStudent = (studentId) => {
  return request({
    url: '/company/favorites/students',
    method: 'post',
    data: { studentId }
  })
}

// 企业拉黑学生
export const blacklistStudent = (studentId) => {
  return request({
    url: '/company/blacklist/students',
    method: 'post',
    data: { studentId }
  })
} 