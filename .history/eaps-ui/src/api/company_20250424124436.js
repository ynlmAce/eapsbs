import request from '@/utils/request'

// 获取企业资料
export function getCompanyProfile() {
  return request({
    url: '/api/company/profile',
    method: 'post'
  })
}

// 更新企业资料
export function updateCompanyProfile(data) {
  return request({
    url: '/api/company/profile/update',
    method: 'post',
    data
  })
}

// 上传企业营业执照
export function uploadLicense(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/company/license/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取企业收到的申请列表
export function getCompanyApplications(data) {
  return request({
    url: '/api/job/application/list/company',
    method: 'post',
    data
  })
}

// 收藏学生
export function favoriteStudent(studentId) {
  return request({
    url: '/api/company/student/favorite',
    method: 'post',
    data: {
      studentId
    }
  })
}

// 拉黑学生
export function blacklistStudent(studentId, reason) {
  return request({
    url: '/api/company/student/blacklist',
    method: 'post',
    data: {
      studentId,
      reason
    }
  })
}

// 更新申请状态
export function updateApplicationStatus(applicationId, status, notes) {
  return request({
    url: '/api/job/application/update-status',
    method: 'post',
    data: {
      applicationId,
      status,
      notes
    }
  })
} 