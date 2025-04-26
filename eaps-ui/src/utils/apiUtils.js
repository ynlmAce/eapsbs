import { ElMessage, ElLoading } from 'element-plus'
import router from '@/router'

/**
 * 处理API响应结果
 * @param {Object} response API响应结果
 * @param {Object} options 配置选项
 * @param {boolean} options.showSuccess 是否显示成功提示，默认为false
 * @param {string} options.successMsg 成功提示信息，默认为'操作成功'
 * @param {boolean} options.returnFullResponse 是否返回完整响应，默认为false，仅返回body
 * @returns {Promise} 处理后的Promise
 */
export function handleApiResponse(response, options = {}) {
  const {
    showSuccess = false,
    successMsg = '操作成功',
    returnFullResponse = false
  } = options

  return new Promise((resolve, reject) => {
    if (!response) {
      console.error('API处理器收到空响应');
      ElMessage.error('服务器无响应')
      reject(new Error('服务器无响应'))
      return
    }

    console.log('API响应处理:', response)

    // 处理响应结果
    if (response.error === 0) {
      // 操作成功
      console.log('API调用成功, 返回数据:', response.body);
      
      if (showSuccess) {
        ElMessage.success(successMsg)
      }
      resolve(returnFullResponse ? response : response.body)
    } else if (response.error === 401) {
      // 未授权，需要登录
      console.error('API调用未授权 (401):', response.message);
      ElMessage.error(response.message || '未登录或登录已过期，请重新登录')
      clearUserAuth()
      router.push('/login')
      reject(new Error(response.message || '未登录或登录已过期'))
    } else if (response.error === 403) {
      // 权限不足
      console.error('API调用权限不足 (403):', response.message);
      ElMessage.error(response.message || '您没有权限执行此操作')
      reject(new Error(response.message || '权限不足'))
    } else if (response.error === 500) {
      // 系统异常
      console.error('API调用系统异常 (500):', response.message);
      ElMessage.error('系统异常: ' + (response.message || '未知错误'))
      reject(new Error(response.message || '系统异常'))
    } else {
      // 其他业务异常
      console.error('API调用业务异常:', response);
      ElMessage.error(response.message || '操作失败')
      reject(new Error(response.message || '操作失败'))
    }
  })
}

/**
 * 处理文件上传响应
 * @param {Object} response API响应结果
 * @param {Object} options 配置选项
 * @param {boolean} options.showSuccess 是否显示成功提示，默认为true
 * @param {string} options.successMsg 成功提示信息，默认为'上传成功'
 * @param {boolean} options.returnFullResponse 是否返回完整响应，默认为false，仅返回body
 * @returns {Promise} 处理后的Promise
 */
export function handleUploadResponse(response, options = {}) {
  const {
    showSuccess = true,
    successMsg = '上传成功',
    returnFullResponse = false
  } = options

  return handleApiResponse(response, {
    showSuccess,
    successMsg,
    returnFullResponse
  })
}

/**
 * 统一处理API调用
 * @param {Promise} apiPromise - API请求Promise
 * @param {Object} options - 配置选项
 * @returns {Promise} - 处理后的Promise
 */
export function callApi(apiPromise, options = {}) {
  const {
    showLoading = false,
    loadingText = '加载中...',
    showSuccess = false,
    successMsg = '操作成功',
    showError = true,
    errorMsg = '操作失败',
    transformResponse = (res) => res
  } = options

  let loadingInstance = null
  if (showLoading) {
    loadingInstance = ElLoading.service({
      text: loadingText,
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }

  return apiPromise
    .then(res => {
      // 检查返回的数据格式，确保处理正确
      // API可能直接返回body，也可能返回 {error, body, message} 结构
      console.log('API原始返回:', res)
      
      // 判断结果格式
      if (res && typeof res === 'object' && res.hasOwnProperty('error')) {
        // 标准格式：{error, body, message}
        if (res.error === 0) {
          // 成功
          if (showSuccess) {
            ElMessage.success(successMsg)
          }
          return transformResponse(res.body)
        } else {
          // 出错
          if (showError) {
            ElMessage.error(res.message || errorMsg)
          }
          return Promise.reject(new Error(res.message || errorMsg))
        }
      } else {
        // 已经是处理过的body数据
        if (showSuccess) {
          ElMessage.success(successMsg)
        }
        return transformResponse(res)
      }
    })
    .catch(error => {
      if (showError) {
        console.error('API调用错误:', error)
        ElMessage.error(error.message || errorMsg)
      }
      return Promise.reject(error)
    })
    .finally(() => {
      if (loadingInstance) {
        loadingInstance.close()
      }
    })
}

/**
 * 统一处理文件上传API调用
 * @param {Promise} apiPromise - API请求Promise
 * @param {Object} options - 配置选项
 * @returns {Promise} - 处理后的Promise
 */
export function callUploadApi(apiPromise, options = {}) {
  const {
    showLoading = true,
    loadingText = '上传中...',
    showSuccess = true,
    successMsg = '上传成功',
    showError = true,
    errorMsg = '上传失败'
  } = options

  let loadingInstance = null
  if (showLoading) {
    loadingInstance = ElLoading.service({
      text: loadingText,
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }

  return apiPromise
    .then(res => {
      // API可能直接返回body，也可能返回 {error, body, message} 结构
      console.log('上传API原始返回:', res)
      
      // 判断结果格式
      if (res && typeof res === 'object' && res.hasOwnProperty('error')) {
        // 标准格式：{error, body, message}
        if (res.error === 0) {
          // 成功
          if (showSuccess) {
            ElMessage.success(successMsg)
          }
          return res.body
        } else {
          // 出错
          if (showError) {
            ElMessage.error(res.message || errorMsg)
          }
          return Promise.reject(new Error(res.message || errorMsg))
        }
      } else {
        // 已经是处理过的body数据
        if (showSuccess) {
          ElMessage.success(successMsg)
        }
        return res
      }
    })
    .catch(error => {
      if (showError) {
        console.error('上传API调用错误:', error)
        ElMessage.error(error.message || errorMsg)
      }
      return Promise.reject(error)
    })
    .finally(() => {
      if (loadingInstance) {
        loadingInstance.close()
      }
    })
}

/**
 * 清除用户认证信息
 */
function clearUserAuth() {
  console.log('清除用户认证信息')
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userRole')
}

export default {
  handleApiResponse,
  handleUploadResponse,
  callApi,
  callUploadApi
} 