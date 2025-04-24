import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd())
  
  // 默认API目标地址
  const apiTarget = env.VITE_API_BASE_URL || 'http://localhost:8080'
  
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      }
    },
    server: {
      proxy: {
        // 将所有/api开头的请求代理到后端服务器
        '/api': {
          target: apiTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        },
        // 将所有其他API请求也代理到后端服务器
        '/auth': {
          target: apiTarget,
          changeOrigin: true
        },
        '/user': {
          target: apiTarget,
          changeOrigin: true
        },
        '/student': {
          target: apiTarget,
          changeOrigin: true
        },
        '/company': {
          target: apiTarget,
          changeOrigin: true
        },
        '/job': {
          target: apiTarget,
          changeOrigin: true
        },
        '/chat': {
          target: apiTarget,
          changeOrigin: true
        },
        '/rating': {
          target: apiTarget,
          changeOrigin: true
        },
        '/job-tag': {
          target: apiTarget,
          changeOrigin: true
        },
        '/counselor': {
          target: apiTarget,
          changeOrigin: true
        },
        '/report': {
          target: apiTarget,
          changeOrigin: true
        }
      }
    },
    // 定义环境变量默认值
    define: {
      'import.meta.env.VITE_APP_TITLE': JSON.stringify(env.VITE_APP_TITLE || '大学生就业帮扶系统'),
      'import.meta.env.VITE_ENABLE_MOCK': JSON.stringify(env.VITE_ENABLE_MOCK || 'false'),
      'import.meta.env.VITE_ENABLE_DEBUG': JSON.stringify(env.VITE_ENABLE_DEBUG || 'true'),
      'import.meta.env.VITE_FILE_UPLOAD_LIMIT': JSON.stringify(env.VITE_FILE_UPLOAD_LIMIT || '10485760')
    }
  }
})
