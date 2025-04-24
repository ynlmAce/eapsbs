import request from './axios'

// 用户认证API
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 用户档案API
export const getUserProfile = () => {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

export const updateUserProfile = (data) => {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

// 学生简历API
export const uploadResumeFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/student/resume/file',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const updateStructuredResume = (data) => {
  return request({
    url: '/student/resume/structured',
    method: 'put',
    data
  })
}

// 企业档案API
export const uploadCompanyLogo = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/company/logo',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const uploadCompanyLicense = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/company/license',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 