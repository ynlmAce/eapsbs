import { ElMessage } from 'element-plus'
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
      ElMessage.error('服务器无响应')
      reject(new Error('服务器无响应'))
      return
    }

    console.log('API响应处理:', response)

    // 处理响应结果
    if (response.error === 0) {
      // 操作成功
      if (showSuccess) {
        ElMessage.success(successMsg)
      }
      resolve(returnFullResponse ? response : response.body)
    } else if (response.error === 401) {
      // 未授权，需要登录
      ElMessage.error(response.message || '未登录或登录已过期，请重新登录')
      clearUserAuth()
      router.push('/login')
      reject(new Error(response.message || '未登录或登录已过期'))
    } else if (response.error === 403) {
      // 权限不足
      ElMessage.error(response.message || '您没有权限执行此操作')
      reject(new Error(response.message || '权限不足'))
    } else if (response.error === 500) {
      // 系统异常
      ElMessage.error('系统异常: ' + (response.message || '未知错误'))
      reject(new Error(response.message || '系统异常'))
    } else {
      // 其他业务异常
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
 * 异步处理API调用
 * @param {Function} apiCall API调用函数
 * @param {Object} options 处理选项
 * @returns {Promise} 处理结果
 */
export async function callApi(apiCall, options = {}) {
  try {
    const response = await apiCall
    return await handleApiResponse(response, options)
  } catch (error) {
    console.error('API调用失败:', error)
    ElMessage.error(error.message || '请求失败，请稍后重试')
    return Promise.reject(error)
  }
}

/**
 * 处理文件上传API调用
 * @param {Function} uploadCall 上传API调用函数
 * @param {Object} options 处理选项
 * @returns {Promise} 处理结果
 */
export async function callUploadApi(uploadCall, options = {}) {
  try {
    const response = await uploadCall
    return await handleUploadResponse(response, options)
  } catch (error) {
    console.error('文件上传失败:', error)
    ElMessage.error(error.message || '上传失败，请稍后重试')
    return Promise.reject(error)
  }
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