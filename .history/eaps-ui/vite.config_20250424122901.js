import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
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
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 将所有其他API请求也代理到后端服务器
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/student': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/company': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/job': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/chat': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/rating': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/job-tag': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/counselor': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/report': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
