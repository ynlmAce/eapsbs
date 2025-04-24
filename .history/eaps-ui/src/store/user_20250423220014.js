import { defineStore } from 'pinia'
import axios from 'axios'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    userRole: localStorage.getItem('userRole') || '',
    isAuthenticated: !!localStorage.getItem('token')
  }),
  
  getters: {
    isLoggedIn: (state) => state.isAuthenticated,
    currentUser: (state) => state.userInfo,
    userRole: (state) => state.userRole
  },
  
  actions: {
    async login(username, password) {
      try {
        // 模拟接口调用，后续替换为实际接口
        const response = await axios.post('/api/auth/login', { username, password })
        
        const { token, userInfo, role } = response.data
        
        // 存储到localStorage
        localStorage.setItem('token', token)
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        localStorage.setItem('userRole', role)
        
        // 更新状态
        this.token = token
        this.userInfo = userInfo
        this.userRole = role
        this.isAuthenticated = true
        
        return { success: true }
      } catch (error) {
        return {
          success: false,
          message: error.response?.data?.message || '登录失败，请检查用户名和密码'
        }
      }
    },
    
    async register(userData) {
      try {
        // 模拟接口调用，后续替换为实际接口
        const response = await axios.post('/api/auth/register', userData)
        return { success: true, data: response.data }
      } catch (error) {
        return {
          success: false,
          message: error.response?.data?.message || '注册失败，请稍后重试'
        }
      }
    },
    
    logout() {
      // 清除localStorage
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userRole')
      
      // 重置状态
      this.token = ''
      this.userInfo = null
      this.userRole = ''
      this.isAuthenticated = false
    },
    
    async updateUserProfile(profileData) {
      try {
        // 模拟接口调用，后续替换为实际接口
        const response = await axios.put('/api/user/profile', profileData)
        
        // 更新状态
        this.userInfo = response.data
        localStorage.setItem('userInfo', JSON.stringify(response.data))
        
        return { success: true }
      } catch (error) {
        return {
          success: false,
          message: error.response?.data?.message || '更新资料失败，请稍后重试'
        }
      }
    }
  }
}) 