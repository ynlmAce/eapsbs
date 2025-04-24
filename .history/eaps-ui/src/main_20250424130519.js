import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'

// 样式导入顺序很重要，先导入默认样式
import './style.css'
// 然后导入Element Plus样式
import 'element-plus/dist/index.css'
// 最后导入全局样式覆盖
import '@/assets/css/global.css'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入环境配置
import { ENV } from '@/config/env'

// 移除Mock，使用实际API
import { removeMock } from '@/mock'

// 如果不使用Mock，则调用removeMock
if (!ENV.ENABLE_MOCK) {
  removeMock()
}

// 创建Pinia状态管理实例
const pinia = createPinia()

// 创建Vue应用实例
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 打印环境信息
console.log('当前环境:', ENV.NODE_ENV)
console.log('API基础URL:', ENV.API_BASE_URL)
console.log('是否开启Mock:', ENV.ENABLE_MOCK)

// 注册插件
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 挂载应用
app.mount('#app')
