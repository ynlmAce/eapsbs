import request from '@/utils/request'
import { handleResponse } from '@/utils/api'

/**
 * 获取岗位列表（学生端）
 * @param {Object} params - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @param {string} [params.keyword] - 搜索关键词
 * @param {Array} [params.tags] - 岗位标签数组
 * @param {string} [params.location] - 地点
 * @param {string} [params.salary] - 薪资范围
 * @param {string} [params.education] - 学历要求
 * @param {string} [params.experience] - 经验要求
 * @param {string} [params.jobType] - 工作类型
 * @returns {Promise} - 返回岗位列表
 */
export function getJobList(params) {
  // 添加时间戳，避免缓存问题
  const queryParams = {
    ...params,
    _t: new Date().getTime()
  };
  
  return request({
    url: '/api/job/list',
    method: 'post',
    data: queryParams || {}
  }).then(response => {
    console.log('岗位列表原始响应:', response);
    
    // 判断响应格式
    if (response && typeof response === 'object') {
      if (response.hasOwnProperty('error')) {
        // 标准响应格式
        if (response.error === 0 && response.body) {
          return response.body;
        } else {
          // 错误情况，返回明确的错误信息
          return Promise.reject(new Error(response.message || '获取岗位列表失败'));
        }
      } else {
        // 已经是处理过的数据
        return response;
      }
    } else {
      return Promise.reject(new Error('获取岗位列表失败，响应格式错误'));
    }
  }).catch(error => {
    console.error('获取岗位列表请求失败:', error);
    return Promise.reject(error);
  });
}


/**
 * 获取岗位详情（通过ID）
 * @param {number} jobId - 岗位ID
 * @returns {Promise} - 返回岗位详情
 */
export function getJobById(jobId) {
  return request({
    url: '/api/job/detail',
    method: 'post',
    data: { jobId }
  }).then(handleResponse)
}

/**
 * 企业发布新岗位
 * @param {Object} jobData - 岗位数据
 * @param {string} jobData.title - 岗位名称
 * @param {string} jobData.description - 岗位描述
 * @param {string} jobData.requirements - 岗位要求
 * @param {string} jobData.salaryRange - 薪资范围
 * @param {string} jobData.location - 工作地点
 * @param {string} jobData.education - 学历要求
 * @param {string} jobData.experience - 经验要求
 * @param {string} jobData.jobType - 工作类型
 * @param {number} jobData.headcount - 招聘人数
 * @param {string} jobData.workTime - 工作时间
 * @param {Array} jobData.welfareTags - 福利标签数组
 * @param {Array} jobData.jobTags - 岗位标签数组
 * @param {string} jobData.expireDate - 招聘有效期
 * @param {boolean} jobData.showContact - 是否显示联系人信息
 * @returns {Promise} - 返回发布结果
 */
export function createJob(jobData) {
  return request({
    url: '/api/job/create',
    method: 'post',
    data: jobData
  }).then(handleResponse)
}

/**
 * 编辑岗位
 * @param {Object} jobData - 岗位数据
 * @param {number} jobData.id - 岗位ID
 * @param {string} [jobData.title] - 岗位名称
 * @param {string} [jobData.description] - 岗位描述
 * @param {string} [jobData.requirements] - 岗位要求
 * @param {string} [jobData.salaryRange] - 薪资范围
 * @param {string} [jobData.location] - 工作地点
 * @param {string} [jobData.education] - 学历要求
 * @param {string} [jobData.experience] - 经验要求
 * @param {string} [jobData.jobType] - 工作类型
 * @param {number} [jobData.headcount] - 招聘人数
 * @param {string} [jobData.workTime] - 工作时间
 * @param {Array} [jobData.welfareTags] - 福利标签数组
 * @param {Array} [jobData.jobTags] - 岗位标签数组
 * @param {string} [jobData.expireDate] - 招聘有效期
 * @param {boolean} [jobData.showContact] - 是否显示联系人信息
 * @returns {Promise} - 返回编辑结果
 */
export function updateJob(jobData) {
  return request({
    url: '/api/job/update',
    method: 'post',
    data: jobData
  }).then(handleResponse)
}

/**
 * 删除岗位
 * @param {number} jobId - 岗位ID
 * @returns {Promise} - 返回删除结果
 */
export function deleteJob(jobId) {
  return request({
    url: '/api/job/delete',
    method: 'post',
    data: { jobId }
  }).then(handleResponse)
}

/**
 * 刷新岗位（更新时间，提高排序）
 * @param {number} jobId - 岗位ID
 * @returns {Promise} - 返回刷新结果
 */
export function refreshJob(jobId) {
  return request({
    url: '/api/job/refresh',
    method: 'post',
    data: { jobId }
  }).then(handleResponse)
}

/**
 * 结束岗位招聘
 * @param {number} jobId - 岗位ID
 * @returns {Promise} - 返回操作结果
 */
export function endJobRecruitment(jobId) {
  return request({
    url: '/api/job/end-recruitment',
    method: 'post',
    data: { jobId }
  }).then(handleResponse)
}

/**
 * 学生投递简历
 * @param {number} jobId - 岗位ID
 * @param {number} [resumeId] - 简历ID，可选参数
 * @param {string} [coverLetter] - 求职信，可选参数
 * @returns {Promise} - 返回投递结果
 */
export function applyForJob(jobId, resumeId = null, coverLetter = '') {
  return request({
    url: '/api/job/apply',
    method: 'post',
    data: { 
      jobId,
      resumeId, // 单个简历ID
      coverLetter // 求职信
    }
  }).then(handleResponse)
}

/**
 * 获取岗位标签列表
 * @returns {Promise} - 返回岗位标签列表
 */
export function getJobTags() {
  return request({
    url: '/api/job/tags',
    method: 'post',
    data: {}
  }).then(handleResponse)
}

/**
 * 获取福利标签列表
 * @returns {Promise} - 返回福利标签列表
 */
export function getWelfareTags() {
  return request({
    url: '/api/job/welfare-tags',
    method: 'post',
    data: {}
  }).then(handleResponse)
}

/**
 * 添加自定义福利标签（需审核）
 * @param {string} tagName - 标签名称
 * @param {string} category - 标签类别
 * @returns {Promise} - 返回操作结果
 */
export function addCustomWelfareTag(tagName, category) {
  return request({
    url: '/api/job/welfare-tag/custom',
    method: 'post',
    data: { 
      tagName,
      category
    }
  }).then(handleResponse)
}

/**
 * 获取学生投递记录
 * @param {Object} params 查询参数
 * @returns {Promise} 投递记录列表
 */
export function getStudentApplications(params) {
  return request({
    url: '/api/job/application/list/student',
    method: 'POST',
    data: params || {}
  })
}

/**
 * 获取企业收到的申请
 * @param {Object} params 查询参数
 * @returns {Promise} 申请列表
 */
export function getCompanyApplications(params) {
  return request({
    url: '/api/job/application/list/company',
    method: 'POST',
    data: params || {}
  })
}

/**
 * 企业处理申请
 * @param {Number} applicationId 申请ID
 * @param {Object} data 处理数据
 * @returns {Promise} 处理结果
 */
export function processApplication(applicationId, data) {
  return request({
    url: '/api/job/application/process',
    method: 'POST',
    data: {
      applicationId,
      status: data.status,
      notes: data.notes || ''
    }
  }).then(handleResponse)
}

/**
 * 企业批量下载简历
 * @param {Array} applicationIds 申请ID数组
 * @returns {Promise} 下载结果
 */
export function batchDownloadResumes(applicationIds) {
  return request({
    url: '/api/job/application/batch-download',
    method: 'POST',
    data: { applicationIds },
    responseType: 'blob'
  })
}

/**
 * 企业收藏学生
 * @param {Number} studentId 学生ID
 * @returns {Promise} 收藏结果
 */
export function favoriteStudent(studentId) {
  return request({
    url: '/api/company/favorite/student',
    method: 'POST',
    data: { studentId }
  })
}

/**
 * 企业拉黑学生
 * @param {Number} studentId 学生ID
 * @param {String} reason 拉黑原因
 * @returns {Promise} 拉黑结果
 */
export function blacklistStudent(studentId, reason) {
  return request({
    url: '/api/company/blacklist/student',
    method: 'POST',
    data: { 
      studentId,
      reason 
    }
  })
}

/**
 * 学生撤回申请
 * @param {Number} applicationId 申请ID
 * @returns {Promise} 撤回结果
 */
export function withdrawApplication(applicationId) {
  return request({
    url: '/api/job/application/withdraw',
    method: 'POST',
    data: {
      applicationId
    }
  }).then(handleResponse)
} 