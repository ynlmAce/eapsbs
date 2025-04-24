import { defineStore } from 'pinia'
import axios from 'axios'
import { register as apiRegister } from '../api/user' // 导入API函数

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
    currentUser: (state) => state.userInfo
  },
  
  actions: {
    async login(username, password, role) {
      try {
        console.log('登录参数:', { username, password, role });

        // 简单直接的方式，使用硬编码的账号密码进行匹配
        if (
          (username === 'student123' && password === 'Student123') ||
          (username === '91110108MA01GYT44K' && password === 'Company123') ||
          (username === 'counselor456' && password === 'Counselor123')
        ) {
          let userInfo, userRole;
          
          // 根据用户名或选择的角色确定用户类型
          if (username === 'student123' || role === 'student') {
            userRole = 'student';
            userInfo = {
              id: 1001,
              name: '测试学生',
              studentId: 'student123',
              school: '示例大学',
              major: '计算机科学与技术',
              contact: '13800138001'
            };
          } else if (username === '91110108MA01GYT44K' || role === 'company') {
            userRole = 'company';
            userInfo = {
              id: 2001,
              name: '测试企业',
              creditCode: '91110108MA01GYT44K',
              industry: '互联网/IT',
              contactPerson: '企业HR',
              contact: '13800138002'
            };
          } else if (username === 'counselor456' || role === 'counselor') {
            userRole = 'counselor';
            userInfo = {
              id: 3001,
              name: '测试辅导员',
              employeeId: 'counselor456',
              college: '计算机学院',
              contact: '13800138003'
            };
          }
          
          // 如果指定了角色但与用户名匹配的默认角色不同，使用指定的角色
          if (role) {
            userRole = role;
          }
          
          // 生成模拟token
          const token = `mock-token-${Date.now()}`;
          
          // 存储到localStorage
          localStorage.setItem('token', token);
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          localStorage.setItem('userRole', userRole);
          
          // 使用$patch方法更新状态，避免直接设置属性
          this.$patch({
            token: token,
            userInfo: userInfo,
            userRole: userRole,
            isAuthenticated: true
          });
          
          console.log('登录成功:', { userRole, userInfo });
          
          return { success: true };
        }
        
        // 如果没有匹配的账号，返回失败
        console.log('登录失败: 未找到匹配的账号');
        return {
          success: false,
          message: '账号或密码错误'
        };
      } catch (error) {
        console.error('登录出错:', error);
        return {
          success: false,
          message: '登录过程中出现错误，请稍后重试'
        };
      }
    },
    
    async register(userData) {
      try {
        console.log('注册数据:', userData);
        // 使用模拟数据进行注册（开发阶段）
        // 在实际环境中，取消下方注释，使用真实API
        // const response = await apiRegister(userData);
        
        // 模拟成功响应
        console.log('注册成功，模拟响应');
        return { 
          success: true, 
          data: {
            id: Date.now(),
            username: userData.username,
            role: userData.role
          }
        };
      } catch (error) {
        // 详细记录错误信息
        console.error('注册错误:', error);
        console.error('错误详情:', {
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data,
          message: error.message
        });
        
        // 返回用户友好的错误信息
        return {
          success: false,
          message: error.response?.data?.message || error.message || '注册失败，请稍后重试'
        };
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