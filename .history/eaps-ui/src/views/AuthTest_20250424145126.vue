<template>
  <div class="auth-test-container">
    <h2>认证测试页面</h2>
    
    <div class="quick-fix" v-if="needsFix">
      <el-alert
        title="检测到认证问题"
        type="warning"
        :closable="false"
        show-icon
      >
        <p>{{ fixMessage }}</p>
        <div class="fix-actions">
          <el-button type="primary" @click="applyQuickFix">
            应用快速修复
          </el-button>
          <el-button @click="navigateToLogin">
            返回登录页
          </el-button>
        </div>
      </el-alert>
    </div>
    
    <div class="auth-status">
      <h3>认证状态</h3>
      <p><strong>Token存在:</strong> {{ !!token }}</p>
      <p v-if="token"><strong>Token值:</strong> {{ tokenDisplay }}</p>
      <p><strong>用户角色:</strong> {{ userRole || '未登录' }}</p>
      <p><strong>登录状态:</strong> {{ isLoggedIn ? '已登录' : '未登录' }}</p>
    </div>
    
    <div class="navigation">
      <h3>导航选项</h3>
      <el-button type="primary" @click="navigateToProfile">
        前往个人主页
      </el-button>
      <el-button type="warning" @click="navigateToLogin">
        返回登录页
      </el-button>
      <el-button type="success" @click="navigateToHome">
        返回首页
      </el-button>
    </div>
    
    <div class="actions">
      <h3>测试操作</h3>
      <el-button type="primary" @click="testGetUserInfo" :loading="loading.userInfo">
        测试获取用户信息
      </el-button>
      
      <el-button type="warning" @click="testProtectedEndpoint" :loading="loading.protected">
        测试受保护接口
      </el-button>
      
      <el-button type="success" @click="testPublicEndpoint" :loading="loading.public">
        测试公开接口
      </el-button>
      
      <el-button type="danger" @click="clearAuth">
        清除认证信息
      </el-button>
    </div>
    
    <div class="response" v-if="response">
      <h3>API响应</h3>
      <pre>{{ response }}</pre>
    </div>
    
    <div class="auth-debug">
      <h3>认证调试</h3>
      <p><strong>当前URL:</strong> {{ currentUrl }}</p>
      <p><strong>URLSearchParams:</strong> {{ urlParams }}</p>
      <p><strong>LocalStorage Token:</strong> <code>{{ tokenTruncated }}</code></p>
      <p><strong>LocalStorage UserRole:</strong> <code>{{ localStorage.getItem('userRole') }}</code></p>
      <p><strong>LocalStorage UserInfo:</strong> 
        <code>{{ userInfoDisplay }}</code>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getUserInfo } from '@/api/user'

const userStore = useUserStore()

// 响应式状态
const token = ref(localStorage.getItem('token') || '')
const userRole = ref(localStorage.getItem('userRole') || '')
const userInfo = ref(null)
const response = ref(null)
const loading = ref({
  userInfo: false,
  protected: false,
  public: false
})

// 计算属性
const isLoggedIn = computed(() => !!token.value)
const tokenDisplay = computed(() => {
  if (!token.value) return ''
  // 只显示token的前10个字符，后面用...代替
  return `${token.value.substring(0, 10)}...${token.value.substring(token.value.length - 5)}`
})

const tokenTruncated = computed(() => {
  const t = localStorage.getItem('token')
  if (!t) return '无'
  if (t.length <= 20) return t
  return `${t.substring(0, 10)}...${t.substring(t.length - 5)}`
})

const currentUrl = computed(() => window.location.href)
const urlParams = computed(() => {
  const params = {}
  new URLSearchParams(window.location.search).forEach((value, key) => {
    params[key] = value
  })
  return JSON.stringify(params)
})

const userInfoDisplay = computed(() => {
  try {
    const info = localStorage.getItem('userInfo')
    if (!info) return '无'
    const parsed = JSON.parse(info)
    return JSON.stringify(parsed, null, 2)
  } catch (e) {
    return `解析错误: ${e.message}`
  }
})

const needsFix = computed(() => {
  // 判断是否需要修复
  return !token.value || !userRole.value
})

const fixMessage = computed(() => {
  if (!token.value) return '未检测到有效的认证token，这会导致API调用403错误'
  if (!userRole.value) return '未检测到用户角色信息，这会导致路由权限判断问题'
  return '认证状态存在问题，建议应用快速修复'
})

// 方法
const testGetUserInfo = async () => {
  loading.value.userInfo = true
  response.value = null
  
  try {
    const res = await getUserInfo()
    response.value = JSON.stringify(res, null, 2)
    ElMessage.success('获取用户信息成功')
  } catch (error) {
    response.value = `错误: ${error.message}\n${JSON.stringify(error?.response?.data || {}, null, 2)}`
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value.userInfo = false
  }
}

const testProtectedEndpoint = async () => {
  loading.value.protected = true
  response.value = null
  
  try {
    // 尝试访问需要认证的API
    const res = await request({
      url: '/api/user/profile',
      method: 'get'
    })
    response.value = JSON.stringify(res, null, 2)
    ElMessage.success('访问受保护接口成功')
  } catch (error) {
    response.value = `错误: ${error.message}\n${JSON.stringify(error?.response?.data || {}, null, 2)}`
    ElMessage.error('访问受保护接口失败')
  } finally {
    loading.value.protected = false
  }
}

const testPublicEndpoint = async () => {
  loading.value.public = true
  response.value = null
  
  try {
    // 尝试访问公开API
    const res = await request({
      url: '/api/auth/login', // 不发送任何数据，只测试连接
      method: 'options'
    })
    response.value = JSON.stringify(res, null, 2)
    ElMessage.success('访问公开接口成功')
  } catch (error) {
    response.value = `错误: ${error.message}\n${JSON.stringify(error?.response?.data || {}, null, 2)}`
    ElMessage.error('访问公开接口失败')
  } finally {
    loading.value.public = false
  }
}

const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userRole')
  
  token.value = ''
  userRole.value = ''
  userInfo.value = null
  
  userStore.$reset()
  
  ElMessage.success('已清除认证信息')
}

const navigateToProfile = () => {
  const role = userRole.value?.toLowerCase() || ''
  let targetPath = '/'
  
  if (role === 'student') {
    targetPath = '/student/profile'
  } else if (role === 'company') {
    targetPath = '/company/profile'
  } else if (role === 'counselor') {
    targetPath = '/counselor/dashboard'
  }
  
  window.location.href = targetPath
}

const navigateToLogin = () => {
  window.location.href = '/login'
}

const navigateToHome = () => {
  window.location.href = '/'
}

const applyQuickFix = () => {
  // 这里实现一个简单的修复方案
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfoObj = JSON.parse(userInfoStr)
      if (userInfoObj.token && !localStorage.getItem('token')) {
        localStorage.setItem('token', userInfoObj.token)
        token.value = userInfoObj.token
        ElMessage.success('已修复token')
      }
      
      if (userInfoObj.role && !localStorage.getItem('userRole')) {
        const normalizedRole = userInfoObj.role.toLowerCase()
        localStorage.setItem('userRole', normalizedRole)
        userRole.value = normalizedRole
        ElMessage.success('已修复角色信息')
      }
    } catch (e) {
      console.error('修复失败:', e)
      ElMessage.error('修复失败: ' + e.message)
    }
  } else {
    ElMessage.error('无可用的用户信息，无法修复')
  }
}

// 生命周期钩子
onMounted(() => {
  // 尝试从store获取更新的信息
  if (userStore.token) {
    token.value = userStore.token
  }
  
  if (userStore.userRole) {
    userRole.value = userStore.userRole
  }
  
  try {
    const storedInfo = localStorage.getItem('userInfo')
    if (storedInfo) {
      userInfo.value = JSON.parse(storedInfo)
    }
  } catch (e) {
    console.error('解析用户信息失败:', e)
  }
})
</script>

<style scoped>
.auth-test-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.quick-fix {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.auth-status,
.navigation,
.actions,
.response,
.auth-debug {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.navigation {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.response pre {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

code {
  background-color: #f0f0f0;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}
</style> 