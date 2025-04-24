import { defineStore } from 'pinia'
import axios from 'axios'

// 测试账号数据
const testAccounts = [
  {
    username: 'student123',
    password: 'Student123',
    role: 'student',
    userInfo: {
      id: 1001,
      name: '测试学生',
      studentId: 'student123',
      school: '示例大学',
      major: '计算机科学与技术',
      contact: '13800138001'
    }
  },
  {
    username: '91110108MA01GYT44K',
    password: 'Company123',
    role: 'company',
    userInfo: {
      id: 2001,
      name: '测试企业',
      creditCode: '91110108MA01GYT44K',
      industry: '互联网/IT',
      contactPerson: '企业HR',
      contact: '13800138002'
    }
  },
  {
    username: 'counselor456',
    password: 'Counselor123',
    role: 'counselor',
    userInfo: {
      id: 3001,
      name: '测试辅导员',
      employeeId: 'counselor456',
      college: '计算机学院',
      contact: '13800138003'
    }
  }
]

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
    async login(username, password, role) {
      try {
        console.log('Store登录参数:', { username, password, role })
        
        // 开发环境：使用模拟数据进行登录验证
        // 查找匹配的测试账号
        const account = testAccounts.find(
          acc => acc.username === username && acc.password === password && acc.role === role
        )
        
        console.log('匹配到的账号:', account)
        
        if (account) {
          // 生成模拟token
          const token = `mock-token-${Date.now()}`
          
          // 存储到localStorage
          localStorage.setItem('token', token)
          localStorage.setItem('userInfo', JSON.stringify(account.userInfo))
          localStorage.setItem('userRole', account.role)
          
          // 更新状态
          this.token = token
          this.userInfo = account.userInfo
          this.userRole = account.role
          this.isAuthenticated = true
          
          return { success: true }
        }
        
        // 如果没有匹配的测试账号，则返回失败
        return {
          success: false,
          message: '账号或密码错误，或所选角色不匹配'
        }
        
        /* 实际环境下的API调用代码，目前注释掉
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
        */
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