<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>创建您的账号，开启就业之旅</p>
      </div>
      
      <el-tabs v-model="activeRole" class="register-tabs" @tab-click="handleRoleChange">
        <el-tab-pane label="学生注册" name="student">
          <el-form
            ref="studentFormRef"
            :model="studentForm"
            :rules="studentRules"
            label-position="top"
            class="register-form"
            @submit.prevent="submitStudentForm"
          >
            <el-form-item prop="studentId" label="学号">
              <el-input
                v-model="studentForm.studentId"
                placeholder="请输入学号"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="name" label="姓名">
              <el-input
                v-model="studentForm.name"
                placeholder="请输入姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="school" label="学校">
              <el-input
                v-model="studentForm.school"
                placeholder="请输入学校名称"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="major" label="专业">
              <el-input
                v-model="studentForm.major"
                placeholder="请输入专业"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="securityQuestion" label="密保问题">
              <el-input
                v-model="studentForm.securityQuestion"
                placeholder="请输入密保问题"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="securityAnswer" label="密保答案">
              <el-input
                v-model="studentForm.securityAnswer"
                placeholder="请输入密保答案"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="contact" label="联系方式">
              <el-input
                v-model="studentForm.contact"
                placeholder="请输入手机号"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="password" label="密码">
              <el-input
                v-model="studentForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword" label="确认密码">
              <el-input
                v-model="studentForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" native-type="submit" :loading="loading" class="register-button">
                {{ loading ? '注册中...' : '立即注册' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="企业注册" name="company">
          <el-form
            ref="companyFormRef"
            :model="companyForm"
            :rules="companyRules"
            label-position="top"
            class="register-form"
            @submit.prevent="submitCompanyForm"
          >
            <el-form-item prop="name" label="企业名称">
              <el-input
                v-model="companyForm.name"
                placeholder="请输入企业名称"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="creditCode" label="统一社会信用代码">
              <el-input
                v-model="companyForm.creditCode"
                placeholder="请输入统一社会信用代码"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="contactPerson" label="联系人">
              <el-input
                v-model="companyForm.contactPerson"
                placeholder="请输入联系人姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="securityQuestion" label="密保问题">
              <el-input
                v-model="companyForm.securityQuestion"
                placeholder="请输入密保问题"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="securityAnswer" label="密保答案">
              <el-input
                v-model="companyForm.securityAnswer"
                placeholder="请输入密保答案"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="contact" label="联系方式">
              <el-input
                v-model="companyForm.contact"
                placeholder="请输入联系电话"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="password" label="密码">
              <el-input
                v-model="companyForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword" label="确认密码">
              <el-input
                v-model="companyForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" native-type="submit" :loading="loading" class="register-button">
                {{ loading ? '注册中...' : '立即注册' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="辅导员注册" name="counselor">
          <el-form
            ref="counselorFormRef"
            :model="counselorForm"
            :rules="counselorRules"
            label-position="top"
            class="register-form"
            @submit.prevent="submitCounselorForm"
          >
            <el-form-item prop="employeeId" label="工号">
              <el-input
                v-model="counselorForm.employeeId"
                placeholder="请输入工号"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="name" label="姓名">
              <el-input
                v-model="counselorForm.name"
                placeholder="请输入姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="securityQuestion" label="密保问题">
              <el-input
                v-model="counselorForm.securityQuestion"
                placeholder="请输入密保问题"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="securityAnswer" label="密保答案">
              <el-input
                v-model="counselorForm.securityAnswer"
                placeholder="请输入密保答案"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="contact" label="联系方式">
              <el-input
                v-model="counselorForm.contact"
                placeholder="请输入手机号"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="password" label="密码">
              <el-input
                v-model="counselorForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword" label="确认密码">
              <el-input
                v-model="counselorForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" native-type="submit" :loading="loading" class="register-button">
                {{ loading ? '注册中...' : '立即注册' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      
      <div class="register-footer">
        <p>已有账号? <router-link to="/login" class="login-link">立即登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const activeRole = ref('student')

// 表单引用
const studentFormRef = ref(null)
const companyFormRef = ref(null)
const counselorFormRef = ref(null)

// 学生表单数据
const studentForm = reactive({
  studentId: '',
  name: '',
  school: '',
  major: '',
  contact: '',
  securityQuestion: '您的出生地是？',
  securityAnswer: '',
  password: '',
  confirmPassword: ''
})

// 企业表单数据
const companyForm = reactive({
  name: '',
  creditCode: '',
  contactPerson: '',
  contact: '',
  securityQuestion: '您的出生地是？',
  securityAnswer: '',
  password: '',
  confirmPassword: ''
})

// 辅导员表单数据
const counselorForm = reactive({
  employeeId: '',
  name: '',
  contact: '',
  securityQuestion: '您的出生地是？',
  securityAnswer: '',
  password: '',
  confirmPassword: ''
})

// 表单校验规则
const validateConfirmPassword = (rule, value, callback) => {
  if (activeRole.value === 'student') {
    if (value !== studentForm.password) {
      callback(new Error('两次输入密码不一致'))
    } else {
      callback()
    }
  } else if (activeRole.value === 'company') {
    if (value !== companyForm.password) {
      callback(new Error('两次输入密码不一致'))
    } else {
      callback()
    }
  } else if (activeRole.value === 'counselor') {
    if (value !== counselorForm.password) {
      callback(new Error('两次输入密码不一致'))
    } else {
      callback()
    }
  }
}

// 学生表单验证规则
const studentRules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 5, max: 20, message: '长度在 5 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  school: [
    { required: true, message: '请输入学校', trigger: 'blur' }
  ],
  major: [
    { required: true, message: '请输入专业', trigger: 'blur' }
  ],
  securityQuestion: [
    { required: true, message: '请输入密保问题', trigger: 'blur' }
  ],
  securityAnswer: [
    { required: true, message: '请输入密保答案', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 企业表单验证规则
const companyRules = {
  name: [
    { required: true, message: '请输入企业名称', trigger: 'blur' }
  ],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { pattern: /^[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}$/, message: '请输入正确的统一社会信用代码', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  securityQuestion: [
    { required: true, message: '请输入密保问题', trigger: 'blur' }
  ],
  securityAnswer: [
    { required: true, message: '请输入密保答案', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 辅导员表单验证规则
const counselorRules = {
  employeeId: [
    { required: true, message: '请输入工号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  securityQuestion: [
    { required: true, message: '请输入密保问题', trigger: 'blur' }
  ],
  securityAnswer: [
    { required: true, message: '请输入密保答案', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 监听路由参数，设置默认标签页
onMounted(() => {
  const { role } = route.query
  if (role) {
    // 将小写角色值转为大写
    const roleUpper = role.toUpperCase()
    if (['STUDENT', 'COMPANY', 'COUNSELOR'].includes(roleUpper)) {
      activeRole.value = role.toLowerCase() // 标签页名称仍然使用小写
    }
  }
})

// 角色切换处理
const handleRoleChange = () => {
  // 切换角色时重置表单
  if (studentFormRef.value) studentFormRef.value.resetFields()
  if (companyFormRef.value) companyFormRef.value.resetFields()
  if (counselorFormRef.value) counselorFormRef.value.resetFields()
}

// 学生注册表单提交
const submitStudentForm = async () => {
  if (!studentFormRef.value) return
  
  await studentFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        const userData = {
          username: studentForm.studentId,
          password: studentForm.password,
          confirmPassword: studentForm.password,
          role: 'STUDENT',
          name: studentForm.name,
          contact: studentForm.contact,
          school: studentForm.school,
          major: studentForm.major,
          securityQuestion: studentForm.securityQuestion,
          securityAnswer: studentForm.securityAnswer
        }
        
        const result = await userStore.register(userData)
        
        if (result.success) {
          ElMessage.success('学生注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(result.message || '注册失败，请重试')
        }
      } catch (error) {
        console.error('注册错误:', error)
        ElMessage.error('注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 企业注册表单提交
const submitCompanyForm = async () => {
  if (!companyFormRef.value) return
  
  await companyFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        const userData = {
          username: companyForm.creditCode,
          password: companyForm.password,
          confirmPassword: companyForm.password,
          role: 'COMPANY',
          name: companyForm.name,
          contact: companyForm.contact,
          unifiedSocialCreditCode: companyForm.creditCode,
          securityQuestion: companyForm.securityQuestion,
          securityAnswer: companyForm.securityAnswer
        }
        
        const result = await userStore.register(userData)
        
        if (result.success) {
          ElMessage.success('企业注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(result.message || '注册失败，请重试')
        }
      } catch (error) {
        console.error('注册错误:', error)
        ElMessage.error('注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 辅导员注册表单提交
const submitCounselorForm = async () => {
  if (!counselorFormRef.value) return
  
  await counselorFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        const userData = {
          username: counselorForm.employeeId,
          password: counselorForm.password,
          confirmPassword: counselorForm.password,
          role: 'COUNSELOR',
          name: counselorForm.name,
          contact: counselorForm.contact,
          securityQuestion: counselorForm.securityQuestion,
          securityAnswer: counselorForm.securityAnswer
        }
        
        const result = await userStore.register(userData)
        
        if (result.success) {
          ElMessage.success('辅导员注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(result.message || '注册失败，请重试')
        }
      } catch (error) {
        console.error('注册错误:', error)
        ElMessage.error('注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 2rem;
}

.register-box {
  width: 100%;
  max-width: 600px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 2.5rem;
}

.register-header {
  text-align: center;
  margin-bottom: 2rem;
}

.register-header h2 {
  font-size: 1.75rem;
  color: #303133;
  margin-bottom: 0.5rem;
}

.register-header p {
  color: #909399;
  font-size: 0.95rem;
}

.register-tabs {
  margin-bottom: 1.5rem;
}

.register-form {
  margin-top: 1.5rem;
}

.register-button {
  width: 100%;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
}

.register-footer {
  text-align: center;
  margin-top: 1.5rem;
  color: #606266;
  font-size: 0.95rem;
}

.login-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .register-box {
    padding: 1.5rem;
  }
  
  .register-header h2 {
    font-size: 1.5rem;
  }
}
</style> 