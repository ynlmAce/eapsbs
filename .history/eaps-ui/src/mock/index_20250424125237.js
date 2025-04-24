// 此文件用于移除或关闭MockJS，转为使用实际后端API
export const removeMock = () => {
  console.log('已移除Mock数据，切换至实际API')
}

// 在开发环境下，可以根据需要开启Mock
// 从环境变量中获取是否启用Mock，默认为false
export const enableMock = import.meta.env.VITE_ENABLE_MOCK === 'true'

// 导出API基础URL配置
export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api' 