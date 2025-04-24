import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { ENV } from '@/config/env'

// 创建axios实例
const request = axios.create({
  baseURL: ENV.API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  withCredentials: false // 跨域请求不携带cookie
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
      tokenValue: token ? `${token.substring(0, 10)}...` : 'none',
      headers: { ...config.headers }
    })
    
    if (token) {
      // 确保token的正确格式和传递
      // 后端只接受auth头中的token
      config.headers.auth = token
      
      // 可选：尝试其他常见的授权头格式
      // config.headers.Authorization = `Bearer ${token}`
      // config.headers['X-Auth-Token'] = token
    }
    
    // 确保POST请求至少有一个空对象作为参数
    if (config.method?.toLowerCase() === 'post' && !config.data) {
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
      clearUserAuth()
      router.push('/login')
      return Promise.reject(new Error('未登录或登录已过期'))
    } else if (res.error === 403) {
      // 权限不足
      ElMessage.error('您没有权限执行此操作')
      return Promise.reject(new Error('权限不足'))
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
      
      // 处理403 Forbidden错误
      if (error.response.status === 403) {
        ElMessage.error('您没有权限访问此资源')
        
        // 如果是页面资源访问，可能是权限或路由问题
        if (error.config.responseType === 'document' || 
            error.config.url.includes('.html') || 
            error.config.url.includes('.htm')) {
          // 可能是前端路由权限问题
          router.push('/')
          return Promise.reject(error)
        }
        
        // 如果API调用返回403，可能是token问题
        if (localStorage.getItem('token')) {
          console.log('检测到403错误，可能是token无效或过期')
          clearUserAuth()
          
          // 添加额外查询参数，便于调试
          setTimeout(() => {
            window.location.href = '/login?auth_error=forbidden'
          }, 1000)
        }
      }
      
      // 处理401 Unauthorized错误
      if (error.response.status === 401) {
        ElMessage.error('登录已过期或未授权')
        clearUserAuth()
        router.push('/login?auth_error=unauthorized')
        return Promise.reject(error)
      }
    }
    
    ElMessage.error('网络请求失败: ' + (error.message || '未知错误'))
    return Promise.reject(error)
  }
)

// 辅助函数：清除用户认证信息
function clearUserAuth() {
  console.log('清除用户认证信息')
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userRole')
}

export default request 