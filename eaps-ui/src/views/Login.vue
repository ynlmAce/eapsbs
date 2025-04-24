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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const showTestTip = ref(true)

// 组件挂载时，清理可能导致问题的旧状态
onMounted(() => {
  // 如果检测到URL参数中有错误信息，显示相应提示
  if (window.location.search.includes('auth_error=true')) {
    ElMessage.error('登录状态异常，请重新登录')
  }
  
  // 检查是否有旧的登录状态可能导致冲突
  if (localStorage.getItem('token')) {
    console.log('检测到旧的登录状态，清理防止冲突')
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userRole')
    userStore.$reset()
  }
})

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
        
        // 确保本地状态干净
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userRole')
        userStore.$reset()
        
        // 打印调试信息
        console.log('登录信息:', {
          username: loginForm.username,
          password: '******', // 不打印密码
          role: loginForm.role
        })
        
        // 使用store的login方法调用真实API
        const result = await userStore.login(
          loginForm.username,
          loginForm.password, 
          loginForm.role
        )
        
        if (result.success) {
          ElMessage.success('登录成功')
          
          // 检查token是否存在
          if (!localStorage.getItem('token')) {
            console.error('登录成功但token未保存')
            ElMessage.warning('登录状态异常，请稍后重试')
            return
          }
          
          // 根据用户角色跳转到不同的页面
          const role = loginForm.role.toLowerCase()
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
          ElMessage.error(result.message || '登录失败，请检查账号和密码')
        }
      } catch (error) {
        console.error('登录错误:', error)
        
        // 详细的错误信息
        if (error.response) {
          console.error('错误状态:', error.response.status)
          console.error('错误数据:', error.response.data)
        }
        
        // 特殊处理403错误
        if (error.response && error.response.status === 403) {
          ElMessage.error('登录验证失败：权限被拒绝，请检查账号信息')
        } else {
          ElMessage.error('登录失败，请稍后重试')
        }
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