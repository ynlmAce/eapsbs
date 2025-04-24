import axios from 'axios'
import { ElMessage } from 'element-plus'
import { ENV } from '@/config/env'

// 创建axios实例 - 用于公共访问的API，不需要认证
const publicRequest = axios.create({
  baseURL: ENV.API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  withCredentials: false
})

// 请求拦截器 - 不添加token
publicRequest.interceptors.request.use(
  config => {
    console.log('公共请求拦截器:', {
      url: config.url,
      method: config.method
    })
    
    // 确保POST请求至少有一个空对象作为参数
    if (config.method?.toLowerCase() === 'post' && !config.data) {
      config.data = {}
    }
    
    return config
  },
  error => {
    console.error('公共请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理API响应
publicRequest.interceptors.response.use(
  response => {
    const res = response.data
    console.log('公共API响应原始数据:', res)
    
    // 完整返回所有数据，由调用方自行处理
    return res
  },
  error => {
    // 请求失败时的错误信息
    console.error('公共请求错误:', error)
    
    if (error.response) {
      console.error('响应状态:', error.response.status, error.response.statusText)
      console.error('响应数据:', error.response.data)
    }
    
    ElMessage.error('网络请求失败: ' + (error.message || '未知错误'))
    return Promise.reject(error)
  }
)

export default publicRequest 