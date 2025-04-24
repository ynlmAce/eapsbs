// 导入环境配置
import { ENV } from '@/config/env'

// 此文件用于移除或关闭MockJS，转为使用实际后端API
export const removeMock = () => {
  console.log('已移除Mock数据，切换至实际API')
}

// 导出是否启用Mock数据的标志
export const enableMock = ENV.ENABLE_MOCK

// 导出API基础URL配置
export const apiBaseUrl = ENV.API_BASE_URL 