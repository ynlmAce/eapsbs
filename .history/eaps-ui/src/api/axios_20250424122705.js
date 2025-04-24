import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080', // 指向后端服务器地址
  timeout: 10000, // 请求超时时间：10秒
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token，并在请求头中添加auth
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['auth'] = token
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 根据接口文档规范，检查错误码
    if (res.error !== 0) {
      // 根据接口文档处理不同的错误码
      switch (res.error) {
        case 401:
          // 需要登录
          ElMessage.error('请先登录')
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 500:
          // 系统异常
          ElMessage.error('系统异常，请联系管理员')
          break
        default:
          // 其他业务异常，直接弹出message内容
          ElMessage.error(res.message || '未知错误')
      }
      return Promise.reject(new Error(res.message || '未知错误'))
    } else {
      // 正常返回数据
      return res
    }
  },
  error => {
    // 处理网络错误
    console.error('响应错误:', error)
    ElMessage.error('网络错误，请检查您的网络连接')
    return Promise.reject(error)
  }
)

// 调试信息
console.log('API请求基础URL:', service.defaults.baseURL)

export default service 