import request from '@/utils/request'

/**
 * 获取待处理任务统计
 * @returns {Promise} 待处理任务统计
 */
export function getTasksCount() {
  return request({
    url: '/api/counselor/tasks/count',
    method: 'post'
  })
}

/**
 * 获取任务列表
 * @param {String} type 任务类型
 * @param {Number} page 页码
 * @param {Number} pageSize 每页大小
 * @returns {Promise} 任务列表
 */
export function getTasksList(data) {
  return request({
    url: '/api/counselor/tasks/list',
    method: 'post',
    data
  })
}

/**
 * 处理任务
 * @param {Number} taskId 任务ID
 * @param {String} action 操作类型
 * @param {String} reason 原因
 * @param {String} notes 备注
 * @returns {Promise} 处理结果
 */
export function processTask(taskId, action, reason, notes) {
  return request({
    url: '/api/counselor/task/process',
    method: 'post',
    data: {
      taskId,
      action,
      reason,
      notes
    }
  })
}

/**
 * 获取历史操作记录
 * @param {Object} params 查询参数
 * @returns {Promise} 历史操作记录
 */
export function getOperationHistory(data) {
  return request({
    url: '/api/counselor/operations/history',
    method: 'post',
    data
  })
}

/**
 * 获取辅导员资料
 * @returns {Promise} 辅导员资料
 */
export function getCounselorProfile() {
  return request({
    url: '/api/counselor/profile',
    method: 'post'
  })
}

/**
 * 更新辅导员资料
 * @param {Object} data 辅导员资料
 * @returns {Promise} 更新结果
 */
export function updateCounselorProfile(data) {
  return request({
    url: '/api/counselor/profile/update',
    method: 'post',
    data
  })
} 