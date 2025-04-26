import request from '@/utils/request'
import { handleResponse } from '@/utils/api'

/**
 * 获取企业档案信息
 * @param {number} [companyId] - 企业ID，不传则获取当前登录企业信息
 * @returns {Promise} - 返回企业档案信息
 */
export function getCompanyProfile(companyId) {
  return request({
    url: '/api/company/profile',
    method: 'post',
    data: companyId ? { companyId } : {}
  }).then(handleResponse)
}

/**
 * 更新企业档案信息
 * @param {Object} profileData - 企业档案数据
 * @param {string} [profileData.name] - 企业名称
 * @param {string} [profileData.industry] - 行业
 * @param {string} [profileData.size] - 规模
 * @param {string} [profileData.address] - 地址
 * @param {string} [profileData.description] - 公司简介
 * @param {string} [profileData.hrContact] - HR联系方式
 * @returns {Promise} - 返回更新结果
 */
export function updateCompanyProfile(profileData) {
  return request({
    url: '/api/company/profile/update',
    method: 'post',
    data: profileData || {}
  }).then(handleResponse)
}

/**
 * 上传企业Logo
 * @param {File} file - Logo文件
 * @returns {Promise} - 返回上传结果
 */
export function uploadCompanyLogo(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/company/logo/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(handleResponse)
}

/**
 * 上传企业营业执照
 * @param {File} file - 营业执照文件
 * @returns {Promise} - 返回上传结果
 */
export function uploadCompanyLicense(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/company/license/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(handleResponse)
}

/**
 * 获取企业认证状态
 * @returns {Promise} - 返回认证状态信息
 */
export function getCertificationStatus() {
  return request({
    url: '/api/company/certification/status',
    method: 'post',
    data: {}
  }).then(handleResponse)
}

/**
 * 获取企业发布的岗位列表
 * @param {Object} params - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @param {string} [params.status] - 状态筛选
 * @returns {Promise} - 返回岗位列表
 */
export function getCompanyJobs(params) {
  return request({
    url: '/api/company/jobs',
    method: 'post',
    data: params || {}
  }).then(handleResponse)
}

/**
 * 获取企业收到的岗位申请列表
 * @param {Object} params - 查询参数
 * @param {number} [params.jobId] - 岗位ID，不传则获取所有岗位的申请
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @param {string} [params.status] - 状态筛选
 * @param {string} [params.groupBy] - 分组方式，可选值：job, student
 * @returns {Promise} - 返回申请列表
 */
export function getCompanyApplications(params) {
  return request({
    url: '/api/job/application/list/company',
    method: 'post',
    data: params || {}
  }).then(handleResponse)
}

/**
 * 处理学生申请
 * @param {number} applicationId - 申请ID
 * @param {string} status - 新状态
 * @param {string} [notes] - 处理备注
 * @returns {Promise} - 返回处理结果
 */
export function processApplication(applicationId, status, notes) {
  return request({
    url: '/api/company/application/process',
    method: 'post',
    data: {
      applicationId,
      status,
      notes: notes || ''
    }
  }).then(handleResponse)
}

/**
 * 批量下载学生简历
 * @param {Array} applicationIds - 申请ID数组
 * @returns {Promise} - 返回文件下载信息
 */
export function batchDownloadResumes(applicationIds) {
  return request({
    url: '/api/company/applications/download-resumes',
    method: 'post',
    data: { applicationIds: applicationIds || [] },
    responseType: 'blob'
  }).then(response => {
    // 直接返回blob数据，由调用方处理文件下载
    return response;
  })
}

/**
 * 获取企业收到的评价列表
 * @param {Object} params - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @returns {Promise} - 返回评价列表
 */
export function getCompanyRatings(params) {
  return request({
    url: '/api/company/ratings',
    method: 'post',
    data: params || {}
  }).then(handleResponse)
}

/**
 * 收藏/取消收藏学生
 * @param {number} studentId - 学生ID
 * @param {boolean} isFavorite - 是否收藏
 * @returns {Promise} - 返回操作结果
 */
export function toggleFavoriteStudent(studentId, isFavorite) {
  return request({
    url: '/api/company/student/favorite',
    method: 'post',
    data: { studentId, isFavorite }
  }).then(handleResponse)
}

/**
 * 拉黑/取消拉黑学生
 * @param {number} studentId - 学生ID
 * @param {string} reason - 拉黑原因
 * @param {boolean} isBlacklisted - 是否拉黑
 * @returns {Promise} - 返回操作结果
 */
export function toggleBlacklistStudent(studentId, reason, isBlacklisted) {
  return request({
    url: '/api/company/student/blacklist',
    method: 'post',
    data: { studentId, reason, isBlacklisted }
  }).then(handleResponse)
}

/**
 * 获取收藏学生列表
 * @param {Object} params - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @returns {Promise} - 返回收藏学生列表
 */
export function getFavoriteStudents(params) {
  return request({
    url: '/api/company/favorite-students',
    method: 'post',
    data: params || {}
  }).then(handleResponse)
}

/**
 * 获取拉黑学生列表
 * @param {Object} params - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @returns {Promise} - 返回拉黑学生列表
 */
export function getBlacklistedStudents(params) {
  return request({
    url: '/api/company/blacklisted-students',
    method: 'post',
    data: params || {}
  }).then(handleResponse)
} 