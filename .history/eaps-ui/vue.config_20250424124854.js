const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8081', // 后端API地址
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api' // 不重写路径
        }
      }
    }
  },
  // 避免生产环境生成sourcemap
  productionSourceMap: false
}) 