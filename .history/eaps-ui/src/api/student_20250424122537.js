import request from './axios'

/**
 * 获取学生个人档案信息
 * @returns {Promise} 返回学生档案信息
 */
export function getStudentProfile() {
  return request({
    url: '/student/profile',
    method: 'POST',
    data: {}
  })
}

/**
 * 更新学生个人档案信息
 * @param {Object} data 学生档案信息
 * @returns {Promise} 更新结果
 */
export function updateStudentProfile(data) {
  return request({
    url: '/student/profile/update',
    method: 'POST',
    data
  })
}

/**
 * 上传学生简历文件
 * @param {FormData} formData 包含文件的表单数据
 * @returns {Promise} 上传结果
 */
export function uploadResumeFile(formData) {
  return request({
    url: '/student/resume/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除已上传的简历文件
 * @param {String} fileId 文件ID
 * @returns {Promise} 删除结果
 */
export function deleteResumeFile(fileId) {
  return request({
    url: '/student/resume/delete',
    method: 'POST',
    data: { fileId }
  })
} 