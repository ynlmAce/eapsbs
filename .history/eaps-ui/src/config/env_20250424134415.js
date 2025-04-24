/**
 * 环境配置文件
 * 用于管理不同环境的配置信息
 */

// 环境变量配置
export const ENV = {
  // 当前环境
  NODE_ENV: import.meta.env.NODE_ENV || 'development',
  
  // API基础URL
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  
  // 应用名称
  APP_TITLE: import.meta.env.VITE_APP_TITLE || '大学生就业帮扶系统',
  
  // 上传文件大小限制（默认10MB）
  FILE_UPLOAD_LIMIT: import.meta.env.VITE_FILE_UPLOAD_LIMIT || 10485760,
  
  // 是否启用调试模式
  ENABLE_DEBUG: import.meta.env.VITE_ENABLE_DEBUG === 'true' || true,
  
  // 是否启用Mock数据
  ENABLE_MOCK: import.meta.env.VITE_ENABLE_MOCK === 'true' || false
}

// 判断是否为开发环境
export const isDev = ENV.NODE_ENV === 'development'

// 判断是否为生产环境
export const isProd = ENV.NODE_ENV === 'production'

// 判断是否为测试环境
export const isTest = ENV.NODE_ENV === 'test'

// 导出环境配置
export default ENV 