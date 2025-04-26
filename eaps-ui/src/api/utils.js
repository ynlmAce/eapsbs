/**
 * API工具函数
 * 用于处理后端API返回值的通用函数
 */
import { ElMessage } from 'element-plus'

/**
 * 处理API响应
 * @param {Promise} apiPromise - API请求Promise
 * @param {Object} options - 配置选项
 * @param {boolean} options.showSuccessMsg - 是否显示成功消息
 * @param {string} options.successMsg - 成功消息内容
 * @param {boolean} options.showErrorMsg - 是否显示错误消息
 * @param {Function} options.onSuccess - 成功回调函数
 * @param {Function} options.onError - 错误回调函数
 * @returns {Promise} 处理后的Promise
 */
export const handleApiResponse = async (apiPromise, options = {}) => {
  const {
    showSuccessMsg = false,
    successMsg = '操作成功',
    showErrorMsg = true,
    onSuccess,
    onError
  } = options

  try {
    const response = await apiPromise

    // API响应成功，error为0
    if (response && response.error === 0) {
      // 显示成功消息
      if (showSuccessMsg) {
        ElMessage.success(successMsg)
      }

      // 执行成功回调
      if (onSuccess && typeof onSuccess === 'function') {
        onSuccess(response.body)
      }

      // 返回响应体
      return response.body
    } else {
      // 处理错误情况（这部分在axios.js的拦截器中已经处理，这里是额外保障）
      const errorMsg = response?.message || '未知错误'
      if (showErrorMsg) {
        ElMessage.error(errorMsg)
      }

      // 执行错误回调
      if (onError && typeof onError === 'function') {
        onError(errorMsg)
      }

      return Promise.reject(new Error(errorMsg))
    }
  } catch (error) {
    // 处理请求异常
    const errorMsg = error.message || '请求失败'
    if (showErrorMsg) {
      ElMessage.error(errorMsg)
    }

    // 执行错误回调
    if (onError && typeof onError === 'function') {
      onError(errorMsg)
    }

    return Promise.reject(error)
  }
}

/**
 * 生成API文档内容
 * @param {Object} apiInfo - API信息对象
 * @returns {string} 格式化的API文档内容
 */
export const generateApiDoc = (apiInfo) => {
  const {
    name,
    description,
    url,
    method = 'POST',
    requireLogin = true,
    params = '无',
    responseType = 'json',
    response = '{}'
  } = apiInfo

  return `
接口名称：${name}
- ${description || '无描述'}
- 接口地址: ${url}
- 方法: ${method}
- 需要登录: ${requireLogin ? '是' : '否'}
- 请求参数: ${typeof params === 'string' ? params : JSON.stringify(params, null, 2)}
- 响应类型: ${responseType}
- 返回值：${typeof response === 'string' ? response : JSON.stringify(response, null, 2)}
`.trim()
}

export default {
  handleApiResponse,
  generateApiDoc
} 