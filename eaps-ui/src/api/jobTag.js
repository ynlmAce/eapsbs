import request from './axios'

/**
 * 获取所有岗位标签
 * @returns {Promise<Object>} 返回标签列表
 */
export function getAllJobTags() {
  return request({
    url: '/job-tag/list',
    method: 'POST',
    data: {}
  })
}

/**
 * 获取标签详情
 * @param {number} id 标签ID
 * @returns {Promise<Object>} 返回标签详情
 */
export function getJobTagDetail(id) {
  return request({
    url: '/job-tag/detail',
    method: 'POST',
    data: {
      id
    }
  })
}

/**
 * 保存或更新标签
 * @param {Object} jobTag 标签对象
 * @returns {Promise<Object>} 返回保存结果
 */
export function saveJobTag(jobTag) {
  return request({
    url: '/job-tag/save',
    method: 'POST',
    data: jobTag
  })
}

/**
 * 删除标签
 * @param {number} id 标签ID
 * @returns {Promise<Object>} 返回删除结果
 */
export function removeJobTag(id) {
  return request({
    url: '/job-tag/remove',
    method: 'POST',
    data: {
      id
    }
  })
} 