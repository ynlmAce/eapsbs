import request from '@/utils/request'

/**
 * 获取学生个人档案信息
 * @returns {Promise} 返回学生档案信息
 */
export function getStudentProfile() {
  return request({
    url: '/api/student/profile',
    method: 'post'
  })
}

/**
 * 更新学生个人档案信息
 * @param {Object} data 学生档案信息
 * @returns {Promise} 更新结果
 */
export function updateStudentProfile(data) {
  return request({
    url: '/api/student/profile/update',
    method: 'post',
    data
  })
}

/**
 * 上传学生简历文件
 * @param {FormData} formData 包含文件的表单数据
 * @returns {Promise} 上传结果
 */
export function uploadResume(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/student/resume/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取学生投递记录
 * @param {Object} data 查询条件
 * @returns {Promise} 学生投递记录
 */
export function getStudentApplications(data) {
  return request({
    url: '/api/job/application/list/student',
    method: 'post',
    data
  })
}

/**
 * 添加技能标签关联
 * @param {String} skillTagId 技能标签ID
 * @returns {Promise} 添加结果
 */
export function addStudentSkill(skillTagId) {
  return request({
    url: '/api/student/skill/add',
    method: 'post',
    data: {
      skillTagId
    }
  })
}

/**
 * 移除技能标签关联
 * @param {String} skillTagId 技能标签ID
 * @returns {Promise} 移除结果
 */
export function removeStudentSkill(skillTagId) {
  return request({
    url: '/api/student/skill/remove',
    method: 'post',
    data: {
      skillTagId
    }
  })
} 