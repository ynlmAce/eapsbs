// 此文件用于移除或关闭MockJS，转为使用实际后端API
export const removeMock = () => {
  console.log('已移除Mock数据，切换至实际API')
}

// 在开发环境下，可以根据需要开启Mock
export const enableMock = false 