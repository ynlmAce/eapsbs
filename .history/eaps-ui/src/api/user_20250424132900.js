import request from '@/utils/request'

/**
 * 用户登录
 * @param {string} username 用户名
 * @param {string} password 密码
 * @param {string} role 角色
 * @returns {Promise} 登录结果
 */
export function login(username, password, role) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data: {
      username,
      password,
      role
    }
  })
}

/**
 * 用户注册
 * @param {Object} data 注册参数
 * @returns {Promise} 注册结果
 */
export function register(data) {
  console.log('发送注册请求:', data) // 添加调试日志
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise} 登出结果
 */
export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

/**
 * 修改密码
 * @param {string} oldPassword 旧密码
 * @param {string} newPassword 新密码
 * @returns {Promise} 修改结果
 */
export function changePassword(oldPassword, newPassword) {
  return request({
    url: '/api/auth/changePassword',
    method: 'post',
    data: {
      oldPassword,
      newPassword
    }
  })
}

/**
 * 重置密码
 * @param {string} username 用户名
 * @param {string} securityAnswer 安全答案
 * @returns {Promise} 重置结果
 */
export function resetPassword(username, securityAnswer) {
  return request({
    url: '/api/user/reset-password',
    method: 'post',
    data: {
      username,
      securityAnswer
    }
  })
}

/**
 * 获取用户信息
 * @returns {Promise} 用户信息
 */
export function getUserInfo() {
  return request({
    url: '/api/user/info',
    method: 'post'
  })
}

/**
 * 获取企业资料
 * @returns {Promise} 企业资料
 */
export function getCompanyProfile() {
  return request({
    url: '/company/profile',
    method: 'POST',
    data: {}
  })
}

/**
 * 更新企业资料
 * @param {Object} data 企业资料数据
 * @returns {Promise} 更新结果
 */
export function updateCompanyProfile(data) {
  return request({
    url: '/company/profile/update',
    method: 'POST',
    data
  })
}

/**
 * 上传企业logo
 * @param {FormData} formData 包含文件的表单数据
 * @returns {Promise} 上传结果
 */
export function uploadCompanyLogo(formData) {
  return request({
    url: '/company/logo/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传企业营业执照
 * @param {FormData} formData 包含文件的表单数据
 * @returns {Promise} 上传结果
 */
export function uploadCompanyLicense(formData) {
  return request({
    url: '/company/license/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
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