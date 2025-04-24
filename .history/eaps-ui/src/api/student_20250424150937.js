import request from '@/utils/request'
import { callApi, callUploadApi } from '@/utils/apiUtils'

/**
 * 获取学生个人档案信息
 * @param {Object} options 选项
 * @returns {Promise} 返回学生档案信息
 */
export function getStudentProfile(options = {}) {
  return callApi(
    request({
      url: '/api/student/profile',
      method: 'post',
      data: {}
    }),
    options
  )
}

/**
 * 更新学生个人档案信息
 * @param {Object} data 学生档案信息
 * @param {Object} options 选项
 * @returns {Promise} 更新结果
 */
export function updateStudentProfile(data, options = {}) {
  const defaultOptions = {
    showSuccess: true,
    successMsg: '档案信息更新成功'
  }
  return callApi(
    request({
      url: '/api/student/profile/update',
      method: 'post',
      data
    }),
    { ...defaultOptions, ...options }
  )
}

/**
 * 上传学生简历文件
 * @param {File} file 文件对象
 * @param {Object} options 选项
 * @returns {Promise} 上传结果
 */
export function uploadResume(file, options = {}) {
  const formData = new FormData()
  formData.append('file', file)

  return callUploadApi(
    request({
      url: '/api/student/resume/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }),
    options
  )
}

/**
 * 上传学生简历文件（供Profile页面使用）
 * @param {FormData} formData 表单数据
 * @param {Object} options 选项
 * @returns {Promise} 上传结果
 */
export function uploadResumeFile(formData, options = {}) {
  const defaultOptions = {
    showSuccess: true,
    successMsg: '简历上传成功'
  }
  return callUploadApi(
    request({
      url: '/api/student/resume/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }),
    { ...defaultOptions, ...options }
  )
}

/**
 * 获取学生投递记录
 * @param {Object} data 查询条件
 * @param {Object} options 选项
 * @returns {Promise} 学生投递记录
 */
export function getStudentApplications(data, options = {}) {
  return callApi(
    request({
      url: '/api/job/application/list/student',
      method: 'post',
      data
    }),
    options
  )
}

/**
 * 添加技能标签关联
 * @param {String} skillTagId 技能标签ID
 * @param {Object} options 选项
 * @returns {Promise} 添加结果
 */
export function addStudentSkill(skillTagId, options = {}) {
  const defaultOptions = {
    showSuccess: true,
    successMsg: '技能添加成功'
  }
  return callApi(
    request({
      url: '/api/student/skill/add',
      method: 'post',
      data: {
        skillTagId
      }
    }),
    { ...defaultOptions, ...options }
  )
}

/**
 * 移除技能标签关联
 * @param {String} skillTagId 技能标签ID
 * @param {Object} options 选项
 * @returns {Promise} 移除结果
 */
export function removeStudentSkill(skillTagId, options = {}) {
  const defaultOptions = {
    showSuccess: true,
    successMsg: '技能移除成功'
  }
  return callApi(
    request({
      url: '/api/student/skill/remove',
      method: 'post',
      data: {
        skillTagId
      }
    }),
    { ...defaultOptions, ...options }
  )
} 