import request from './axios'

/**
 * 获取待处理任务统计
 * @returns {Promise} 待处理任务统计
 */
export function getTasksCount() {
  return request({
    url: '/api/counselor/tasks/count',
    method: 'POST',
    data: {}
  })
}

/**
 * 获取任务列表
 * @param {String} type 任务类型
 * @param {Number} page 页码
 * @param {Number} pageSize 每页大小
 * @returns {Promise} 任务列表
 */
export function getTasksList(type, page = 1, pageSize = 10) {
  return request({
    url: '/api/counselor/tasks/list',
    method: 'POST',
    data: {
      type,
      page,
      pageSize
    }
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
    method: 'POST',
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
export function getOperationsHistory(params) {
  return request({
    url: '/api/counselor/operations/history',
    method: 'POST',
    data: params || {}
  })
} 