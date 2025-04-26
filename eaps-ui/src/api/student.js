import request from '@/utils/request'
import { callApi, callUploadApi } from '@/utils/apiUtils'
import { handleResponse } from '@/utils/api'

/**
 * 获取学生个人档案信息
 * @returns {Promise} - 返回学生档案信息
 */
export function getStudentProfile() {
  console.log('开始调用API: getStudentProfile');
  
  // 获取token
  const token = localStorage.getItem('token');
  if (!token) {
    console.error('获取学生档案失败: 未找到token');
    return Promise.reject(new Error('未登录'));
  }
  
  // 添加更多调试信息
  console.log('请求学生档案，使用token进行认证:', token.substring(0, 10) + '...');
  
  return request({
    url: '/api/student/profile',
    method: 'post',
    data: {}, // 发送空对象，让后端从当前登录上下文中获取用户ID
    headers: {
      'auth': token // 确保token使用'auth'作为头名称
    }
  })
  .then(response => {
    console.log('学生档案API原始响应:', response);
    
    // 检查响应是否正确
    if (!response) {
      console.error('API返回空响应');
      throw new Error('服务器返回空响应');
    }
    
    // 记录详细信息以便调试
    if (response.error !== 0) {
      console.error(`API返回错误 - 错误码: ${response.error}, 消息: ${response.message}`);
    } else if (!response.body) {
      console.warn('API返回成功但无数据体');
    } else {
      console.log('API返回成功，数据体包含字段:', Object.keys(response.body));
    }
    
    // 返回完整响应，由调用者处理
    return response;
  })
  .catch(error => {
    console.error('获取学生档案请求失败:', error);
    
    // 添加更多错误详情
    if (error.response) {
      console.error('HTTP状态:', error.response.status);
      console.error('响应头:', error.response.headers);
      console.error('响应体:', error.response.data);
    }
    
    throw error;
  });
}

/**
 * 更新学生个人档案信息
 * @param {Object} profileData - 学生档案数据
 * @param {string} [profileData.name] - 姓名（只读）
 * @param {string} [profileData.contact] - 联系方式（只读）
 * @param {string} [profileData.school] - 学校（只读）
 * @param {string} [profileData.major] - 专业（只读）
 * @param {string} [profileData.grade] - 年级（只读）
 * @param {string} [profileData.selfIntroduction] - 自我介绍
 * @param {Object} [profileData.jobPreferences] - 求职意向
 * @returns {Promise} - 返回更新结果
 */
export function updateStudentProfile(profileData) {
  const dataToUpdate = { ...profileData };
  
  // 打印将要发送的原始数据
  console.log('学生档案更新 - 原始数据:', JSON.stringify(dataToUpdate));
  
  // 处理时间范围格式 - 转换日期对象为ISO字符串
  if (Array.isArray(dataToUpdate.educationExperience)) {
    dataToUpdate.educationExperience = dataToUpdate.educationExperience.map(item => {
      const newItem = { ...item };
      if (Array.isArray(newItem.timeRange) && newItem.timeRange.length === 2) {
        // 确保时间范围是字符串格式
        newItem.timeRange = newItem.timeRange.map(date => 
          date instanceof Date ? date.toISOString().split('T')[0] : date
        );
      }
      return newItem;
    });
  }
  
  if (Array.isArray(dataToUpdate.internshipExperience)) {
    dataToUpdate.internshipExperience = dataToUpdate.internshipExperience.map(item => {
      const newItem = { ...item };
      if (Array.isArray(newItem.timeRange) && newItem.timeRange.length === 2) {
        newItem.timeRange = newItem.timeRange.map(date => 
          date instanceof Date ? date.toISOString().split('T')[0] : date
        );
      }
      return newItem;
    });
  }
  
  if (Array.isArray(dataToUpdate.projectExperience)) {
    dataToUpdate.projectExperience = dataToUpdate.projectExperience.map(item => {
      const newItem = { ...item };
      if (Array.isArray(newItem.timeRange) && newItem.timeRange.length === 2) {
        newItem.timeRange = newItem.timeRange.map(date => 
          date instanceof Date ? date.toISOString().split('T')[0] : date
        );
      }
      return newItem;
    });
  }
  
  // 减少序列化处理，尝试以原始结构发送数据
  // 只对jobPreferences进行序列化，因为它的结构相对简单
  if (dataToUpdate.jobPreferences && typeof dataToUpdate.jobPreferences === 'object') {
    dataToUpdate.jobPreferences = JSON.stringify(dataToUpdate.jobPreferences);
  }
  
  // 打印处理后准备发送的数据
  console.log('学生档案更新 - 处理后发送数据:', dataToUpdate);
  
  // 尝试使用不同的URL，可能需要调用单独的API路径
  return request({
    url: '/api/student/profile/update',
    method: 'post',
    data: dataToUpdate || {},
    timeout: 30000 // 30秒超时
  })
  .then(response => {
    // 记录完整的响应内容
    console.log('更新档案完整响应:', response);
    
    // 处理可能的各种响应格式
    if (response && typeof response === 'object') {
      if ('error' in response) {
        // 标准响应格式: {error, body, message}
        if (response.error === 0) {
          return response.body;
        } else {
          console.error('更新档案失败:', response.message);
          return Promise.reject(new Error(response.message || '更新失败'));
        }
      } else {
        // 直接返回数据
        return response;
      }
    } else {
      console.error('更新档案返回格式异常:', response);
      return Promise.reject(new Error('服务器响应格式异常'));
    }
  })
  .catch(error => {
    console.error('更新档案请求失败:', error);
    throw error;
  });
}

/**
 * 获取学生结构化简历信息
 * @returns {Promise} - 返回学生结构化简历信息
 */
export function getStudentResume() {
  return request({
    url: '/api/student/resume',
    method: 'post',
    data: {}
  }).then(handleResponse)
}

/**
 * 更新学生教育经历
 * @param {Object} data 包含educationExperiences数组的对象
 * @returns {Promise} 请求结果
 */
export function updateEducationExperience(data) {
  return request({
    url: '/api/student/resume/education',
    method: 'post',
    data
  })
  .then(response => {
    console.log('更新教育经历完整响应:', response);
    
    // 处理可能的各种响应格式
    if (response && typeof response === 'object') {
      if ('error' in response) {
        // 标准响应格式: {error, body, message}
        if (response.error === 0) {
          return response.body;
        } else {
          console.error('更新教育经历失败:', response.message);
          return Promise.reject(new Error(response.message || '更新失败'));
        }
      } else {
        // 直接返回数据
        return response;
      }
    } else {
      console.error('更新教育经历返回格式异常:', response);
      return Promise.reject(new Error('服务器响应格式异常'));
    }
  })
  .catch(error => {
    console.error('更新教育经历请求失败:', error);
    throw error;
  });
}

/**
 * 更新学生项目/实习经历
 * @param {Object} data 包含projectExperiences数组的对象
 * @returns {Promise} 请求结果
 */
export function updateProjectExperience(data) {
  console.log('准备发送项目/实习经历数据:', data);
  
  // 尝试预处理时间范围数据，确保格式正确
  if (data && data.projectExperiences && Array.isArray(data.projectExperiences)) {
    data.projectExperiences = data.projectExperiences.map(item => {
      const newItem = { ...item };
      
      // 处理时间范围格式问题
      if (Array.isArray(newItem.timeRange)) {
        if (newItem.timeRange.length === 2) {
          // 确保日期是ISO格式字符串
          newItem.timeRange = newItem.timeRange.map(date => {
            if (date instanceof Date) {
              return date.toISOString().split('T')[0];
            } else if (typeof date === 'string') {
              return date;
            } else if (date && typeof date === 'object' && date._isAMomentObject) {
              // 处理moment对象
              return date.format('YYYY-MM-DD');
            }
            return null; // 对于无效日期，使用null
          });
        } else if (newItem.timeRange.length === 0) {
          // 空时间范围，设置为null防止格式错误
          newItem.timeRange = null;
        }
      } else {
        // 非数组类型，设置为null
        newItem.timeRange = null;
      }
      
      return newItem;
    });
  }
  
  console.log('处理后的项目/实习经历数据:', data);
  
  return request({
    url: '/api/student/resume/project',
    method: 'post',
    data
  })
  .then(response => {
    console.log('更新项目/实习经历完整响应:', response);
    
    // 处理可能的各种响应格式
    if (response && typeof response === 'object') {
      if ('error' in response) {
        // 标准响应格式: {error, body, message}
        if (response.error === 0) {
          return response.body;
        } else {
          console.error('更新项目/实习经历失败:', response.message);
          return Promise.reject(new Error(response.message || '更新失败'));
        }
      } else {
        // 直接返回数据
        return response;
      }
    } else {
      console.error('更新项目/实习经历返回格式异常:', response);
      return Promise.reject(new Error('服务器响应格式异常'));
    }
  })
  .catch(error => {
    console.error('更新项目/实习经历请求失败:', error);
    
    // 增加更多错误详情输出
    if (error.response) {
      console.error('HTTP状态:', error.response.status);
      if (error.response.data) {
        console.error('响应数据:', error.response.data);
      }
    }
    
    // 保留原始错误对象，以便调用者可以查看完整的错误信息
    throw error;
  });
}

/**
 * 更新学生技能标签
 * @param {Object} data 包含skillTags数组的对象
 * @returns {Promise} 请求结果
 */
export function updateSkillTags(data) {
  return request({
    url: '/api/student/resume/skills',
    method: 'post',
    data
  })
  .then(response => {
    console.log('更新技能标签完整响应:', response);
    
    // 处理可能的各种响应格式
    if (response && typeof response === 'object') {
      if ('error' in response) {
        // 标准响应格式: {error, body, message}
        if (response.error === 0) {
          return response.body;
        } else {
          console.error('更新技能标签失败:', response.message);
          return Promise.reject(new Error(response.message || '更新失败'));
        }
      } else {
        // 直接返回数据
        return response;
      }
    } else {
      console.error('更新技能标签返回格式异常:', response);
      return Promise.reject(new Error('服务器响应格式异常'));
    }
  })
  .catch(error => {
    console.error('更新技能标签请求失败:', error);
    throw error;
  });
}

/**
 * 上传学生附件简历
 * @param {File} file - 简历文件
 * @returns {Promise} - 返回上传结果
 */
export function uploadResumeFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/student/resume/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(handleResponse)
}

/**
 * 获取学生所有简历文件列表
 * @returns {Promise} - 返回简历文件列表
 */
export function getResumeFiles() {
  return request({
    url: '/api/student/resume/files',
    method: 'post',
    data: {}
  }).then(handleResponse)
}

/**
 * 获取学生的项目和实习经历
 * @returns {Promise} - 返回项目和实习经历数据
 */
export function getProjectExperiences() {
  return request({
    url: '/api/student/resume/experiences',
    method: 'post',
    data: {}
  }).then(response => {
    console.log('获取项目/实习经历完整响应:', response);
    
    // 处理可能的各种响应格式
    if (response && typeof response === 'object') {
      if ('error' in response) {
        // 标准响应格式: {error, body, message}
        if (response.error === 0) {
          return response.body;
        } else {
          console.error('获取项目/实习经历失败:', response.message);
          return Promise.reject(new Error(response.message || '获取失败'));
        }
      } else {
        // 直接返回数据
        return response;
      }
    } else {
      console.error('获取项目/实习经历返回格式异常:', response);
      return Promise.reject(new Error('服务器响应格式异常'));
    }
  })
  .catch(error => {
    console.error('获取项目/实习经历请求失败:', error);
    throw error;
  });
}

/**
 * 删除学生简历文件
 * @param {number} fileId - 文件ID
 * @returns {Promise} - 返回删除结果
 */
export function deleteResumeFile(fileId) {
  return request({
    url: '/api/student/resume/file/delete',
    method: 'post',
    data: { fileId }
  }).then(handleResponse)
}

/**
 * 获取学生行为分
 * @returns {Promise} - 返回学生行为分
 */
export function getBehaviorScore() {
  return request({
    url: '/api/student/behavior-score',
    method: 'post',
    data: {}
  }).then(handleResponse)
}

/**
 * 获取学生投递记录
 * @param {Object} params - 查询参数
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.limit=10] - 每页数量
 * @param {string} [params.status] - 状态筛选
 * @returns {Promise} - 返回投递记录
 */
export function getApplicationRecords(params) {
  return request({
    url: '/api/student/applications',
    method: 'post',
    data: params || {}
  }).then(handleResponse)
}

/**
 * 获取可用的技能标签列表
 * @returns {Promise} - 返回技能标签列表
 */
export function getSkillTags() {
  return request({
    url: '/api/common/skill-tags',
    method: 'post',
    data: {}
  }).then(handleResponse)
}

/**
 * 上传学生简历文件
 * @param {File} file 文件对象或FormData
 * @param {Object} options 选项
 * @returns {Promise} 上传结果
 */
export function uploadResume(fileOrFormData, options = {}) {
  // 如果传入的是FormData，直接使用
  const formData = fileOrFormData instanceof FormData 
    ? fileOrFormData 
    : (() => {
        const newFormData = new FormData();
        newFormData.append('file', fileOrFormData);
        return newFormData;
      })();

  const defaultOptions = {
    showSuccess: true,
    successMsg: '简历上传成功'
  };

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
  );
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

/**
 * 获取学生自己的技能标签
 * @param {Object} options 选项
 * @returns {Promise} 学生技能标签列表
 */
export function getStudentSkills(options = {}) {
  const defaultOptions = {
    showError: true
  }
  return callApi(
    request({
      url: '/api/student/skills',
      method: 'post',
      data: {}
    }),
    { ...defaultOptions, ...options }
  )
}

/**
 * 获取学生的教育经历
 * @returns {Promise} - 返回教育经历数据
 */
export function getEducationExperiences() {
  return request({
    url: '/api/student/resume/education-experiences',
    method: 'post',
    data: {}
  }).then(response => {
    console.log('获取教育经历完整响应:', response);
    
    // 处理可能的各种响应格式
    if (response && typeof response === 'object') {
      if ('error' in response) {
        // 标准响应格式: {error, body, message}
        if (response.error === 0) {
          return response.body;
        } else {
          console.error('获取教育经历失败:', response.message);
          return Promise.reject(new Error(response.message || '获取失败'));
        }
      } else {
        // 直接返回数据
        return response;
      }
    } else {
      console.error('获取教育经历返回格式异常:', response);
      return Promise.reject(new Error('服务器响应格式异常'));
    }
  })
  .catch(error => {
    console.error('获取教育经历请求失败:', error);
    throw error;
  });
} 