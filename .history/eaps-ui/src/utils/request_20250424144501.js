import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { ENV } from '@/config/env'

// 创建axios实例
const request = axios.create({
  baseURL: ENV.API_BASE_URL || '/api',
  timeout: 15000
})

// 请求拦截器 - 添加token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    
    // 详细的调试信息
    console.log('请求拦截器:', {
      url: config.url,
      method: config.method,
      hasToken: !!token,
      headers: config.headers
    })
    
    if (token) {
      // 尝试多种认证头格式，提高兼容性
      config.headers.auth = token
      config.headers.Authorization = `Bearer ${token}`  // Bearer认证方式
      config.headers['X-Auth-Token'] = token           // 自定义头
    }
    
    // 确保POST请求至少有一个空对象作为参数
    if (config.method === 'post' && !config.data) {
      config.data = {}
    }
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理错误
request.interceptors.response.use(
  response => {
    const res = response.data
    console.log('API响应原始数据:', res)
    
    if (res.error === 0) {
      // 返回业务数据主体
      return res.body
    } else if (res.error === 401) {
      // 未登录，跳转到登录页
      ElMessage.error('未登录或登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userRole')
      router.push('/login')
      return Promise.reject(new Error('未登录或登录已过期'))
    } else if (res.error === 500) {
      // 系统异常
      ElMessage.error('系统异常: ' + res.message)
      return Promise.reject(new Error(res.message))
    } else {
      // 业务异常
      ElMessage.error(res.message || '未知错误')
      return Promise.reject(new Error(res.message || '未知错误'))
    }
  },
  error => {
    // 请求失败时的错误信息
    console.error('请求错误:', error)
    if (error.response) {
      console.error('响应状态:', error.response.status, error.response.statusText)
      console.error('响应数据:', error.response.data)
    }
    
    ElMessage.error('网络请求失败: ' + (error.message || '未知错误'))
    return Promise.reject(error)
  }
)

export default request 