import { defineStore } from 'pinia'
import axios from 'axios'
import { register as apiRegister, login as apiLogin, updateUserProfile as apiUpdateProfile } from '../api/user' // 导入API函数

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
  state: () => {
    // 一次性获取所有localStorage项，避免多次调用
    const storedToken = localStorage.getItem('token') || '';
    const storedUserRole = localStorage.getItem('userRole') || '';
    
    // 更安全地解析userInfo
    let storedUserInfo = null;
    try {
      const userInfoStr = localStorage.getItem('userInfo');
      if (userInfoStr && userInfoStr !== 'undefined' && userInfoStr !== 'null') {
        storedUserInfo = JSON.parse(userInfoStr);
      }
    } catch (e) {
      console.error('解析用户信息时出错:', e);
      // 如果解析错误，清除可能损坏的数据
      localStorage.removeItem('userInfo');
    }
    
    return {
      token: storedToken,
      userInfo: storedUserInfo,
      userRole: storedUserRole,
      isAuthenticated: !!storedToken
    };
  },
  
  getters: {
    isLoggedIn: (state) => state.isAuthenticated,
    currentUser: (state) => state.userInfo
  },
  
  actions: {
    async login(username, password, role) {
      try {
        console.log('登录参数:', { username, password, role });

        // 使用真实API登录
        const userInfo = await apiLogin(username, password, role);
        
        // 添加调试日志，查看返回的实际数据结构
        console.log('API返回的原始数据:', userInfo);
        
        if (userInfo && userInfo.token) {
          // 存储到localStorage
          localStorage.setItem('token', userInfo.token);
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          localStorage.setItem('userRole', userInfo.role);
          
          // 使用$patch方法更新状态
          this.$patch({
            token: userInfo.token,
            userInfo: userInfo,
            userRole: userInfo.role,
            isAuthenticated: true
          });
          
          console.log('登录成功:', userInfo);
          
          return { success: true };
        }
        
        // 如果API返回失败或没有token
        console.log('登录失败:', userInfo);
        return {
          success: false,
          message: '登录失败，请检查账号和密码'
        };
      } catch (error) {
        console.error('登录出错:', error);
        return {
          success: false,
          message: error.response?.data?.message || error.message || '登录过程中出现错误，请稍后重试'
        };
      }
    },
    
    async register(userData) {
      try {
        console.log('注册数据:', userData);
        // 使用真实API进行注册
        const response = await apiRegister(userData);
        
        // 返回真实API响应
        console.log('注册成功:', response);
        return { 
          success: true, 
          data: response
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
        // 使用真实API调用
        const response = await apiUpdateProfile(profileData);
        
        // 更新状态
        if (response) {
          this.userInfo = response;
          localStorage.setItem('userInfo', JSON.stringify(response));
          return { success: true };
        }
        
        return { success: false, message: '更新资料失败' };
      } catch (error) {
        return {
          success: false,
          message: error.response?.data?.message || error.message || '更新资料失败，请稍后重试'
        };
      }
    }
  }
}) 