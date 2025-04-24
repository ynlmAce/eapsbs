import request from './axios'

// 岗位列表查询
export const getJobs = (params) => {
  return request({
    url: '/jobs',
    method: 'get',
    params
  })
}

// 岗位详情
export const getJobDetail = (jobId) => {
  return request({
    url: `/jobs/${jobId}`,
    method: 'get'
  })
}

// 企业发布岗位
export const createJob = (data) => {
  return request({
    url: '/company/jobs',
    method: 'post',
    data
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

// 学生投递简历
export const applyJob = (jobId, data) => {
  return request({
    url: `/student/applications/${jobId}`,
    method: 'post',
    data
  })
}

// 学生查询投递记录
export const getStudentApplications = (params) => {
  return request({
    url: '/student/applications',
    method: 'get',
    params
  })
}

// 企业查询收到的申请
export const getCompanyApplications = (params) => {
  return request({
    url: '/company/applications',
    method: 'get',
    params
  })
}

// 企业处理申请
export const processApplication = (applicationId, data) => {
  return request({
    url: `/company/applications/${applicationId}`,
    method: 'put',
    data
  })
}

// 企业批量下载简历
export const batchDownloadResumes = (applicationIds) => {
  return request({
    url: '/company/applications/download',
    method: 'post',
    data: { applicationIds },
    responseType: 'blob'
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