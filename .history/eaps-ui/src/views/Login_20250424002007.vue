<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>用户登录</h2>
        <p>欢迎回来，请登录您的账号</p>
      </div>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        label-position="top" 
        class="login-form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="username" label="用户名/账号">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名/学号/工号" 
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password" label="密码">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="role" label="用户类型">
          <el-radio-group v-model="loginForm.role" class="role-group">
            <el-radio label="student">学生</el-radio>
            <el-radio label="company">企业</el-radio>
            <el-radio label="counselor">辅导员</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <div class="form-actions">
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
          <router-link to="/forgot-password" class="forgot-password">忘记密码?</router-link>
        </div>
        
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" class="login-button">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>还没有账号? <router-link to="/register" class="register-link">立即注册</router-link></p>
      </div>
      
      <el-alert
        v-if="showTestTip"
        title="测试账号信息"
        type="info"
        :closable="true"
        @close="showTestTip = false"
      >
        <p><strong>学生:</strong> student123 / Student123</p>
        <p><strong>企业:</strong> 91110108MA01GYT44K / Company123</p>
        <p><strong>辅导员:</strong> counselor456 / Counselor123</p>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const showTestTip = ref(true)

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  role: 'student',
  remember: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名/学号/工号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ]
}

// 表单引用
const loginFormRef = ref(null)

// 处理表单提交
const handleSubmit = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        // 打印调试信息
        console.log('登录信息:', {
          username: loginForm.username,
          password: loginForm.password,
          role: loginForm.role
        })
        
        // 直接在组件中处理登录逻辑
        const { username, password, role } = loginForm
        let loginSuccess = false
        let userInfo = null
        
        // 简单的账号密码验证
        if (username === 'student123' && password === 'Student123') {
          loginSuccess = true
          userInfo = {
            id: 1001,
            name: '测试学生',
            studentId: 'student123',
            school: '示例大学',
            major: '计算机科学与技术',
            contact: '13800138001'
          }
        } else if (username === '91110108MA01GYT44K' && password === 'Company123') {
          loginSuccess = true
          userInfo = {
            id: 2001,
            name: '测试企业',
            creditCode: '91110108MA01GYT44K',
            industry: '互联网/IT',
            contactPerson: '企业HR',
            contact: '13800138002'
          }
        } else if (username === 'counselor456' && password === 'Counselor123') {
          loginSuccess = true
          userInfo = {
            id: 3001,
            name: '测试辅导员',
            employeeId: 'counselor456',
            college: '计算机学院',
            contact: '13800138003'
          }
        }
        
        if (loginSuccess) {
          // 生成模拟token
          const token = `mock-token-${Date.now()}`
          
          // 存储到localStorage
          localStorage.setItem('token', token)
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
          localStorage.setItem('userRole', role)
          // 额外存储userName，用于显示在页面上
          localStorage.setItem('userName', userInfo.name)
          
          ElMessage.success('登录成功')
          
          // 根据用户角色跳转到不同的页面
          if (role === 'student') {
            router.push('/student/profile')
          } else if (role === 'company') {
            router.push('/company/profile')
          } else if (role === 'counselor') {
            router.push('/counselor/dashboard')
          } else {
            router.push('/')
          }
        } else {
          ElMessage.error('账号或密码错误')
        }
      } catch (error) {
        console.error('登录错误:', error)
        ElMessage.error('登录失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 2rem;
}

.login-box {
  width: 100%;
  max-width: 450px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 2.5rem;
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-header h2 {
  font-size: 1.75rem;
  color: #303133;
  margin-bottom: 0.5rem;
}

.login-header p {
  color: #909399;
  font-size: 0.95rem;
}

.login-form {
  margin-bottom: 1.5rem;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.forgot-password {
  color: #409eff;
  text-decoration: none;
  font-size: 0.95rem;
}

.forgot-password:hover {
  text-decoration: underline;
}

.login-button {
  width: 100%;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
}

.login-footer {
  text-align: center;
  margin-top: 1.5rem;
  color: #606266;
  font-size: 0.95rem;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}

.role-group {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

@media (max-width: 576px) {
  .login-box {
    padding: 1.5rem;
  }
  
  .login-header h2 {
    font-size: 1.5rem;
  }
}
</style> 