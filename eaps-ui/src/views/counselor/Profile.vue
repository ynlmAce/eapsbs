<template>
  <div class="counselor-profile">
    <h2 class="page-title">个人档案</h2>
    
    <el-card class="profile-card">
      <el-form 
        :model="profileForm" 
        :rules="rules" 
        ref="profileFormRef" 
        label-width="120px"
        class="profile-form"
      >
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="工号" prop="employeeId">
          <el-input v-model="profileForm.employeeId" disabled></el-input>
        </el-form-item>
        
        <el-form-item label="姓名" prop="name">
          <el-input v-model="profileForm.name"></el-input>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contact">
          <el-input v-model="profileForm.contact"></el-input>
        </el-form-item>
        
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="profileForm.email"></el-input>
        </el-form-item>
        
        <el-form-item label="所属学院" prop="college">
          <el-input v-model="profileForm.college"></el-input>
        </el-form-item>
        
        <el-form-item label="职称" prop="title">
          <el-select v-model="profileForm.title" placeholder="请选择职称">
            <el-option label="讲师" value="讲师"></el-option>
            <el-option label="副教授" value="副教授"></el-option>
            <el-option label="教授" value="教授"></el-option>
          </el-select>
        </el-form-item>
        
        <el-divider content-position="left">辅导员信息</el-divider>
        
        <el-form-item label="专业方向" prop="specialization">
          <el-input v-model="profileForm.specialization"></el-input>
        </el-form-item>
        
        <el-form-item label="带队经验" prop="experience">
          <el-input-number v-model="profileForm.experience" :min="0" :max="50" :step="1"></el-input-number>
          <span class="unit-label">年</span>
        </el-form-item>
        
        <el-form-item label="办公室地点" prop="officeLocation">
          <el-input v-model="profileForm.officeLocation"></el-input>
        </el-form-item>
        
        <el-form-item label="办公时间" prop="officeHours">
          <el-input v-model="profileForm.officeHours" placeholder="例如：周一至周五 9:00-17:00"></el-input>
        </el-form-item>
        
        <el-form-item label="简介" prop="introduction">
          <el-input 
            type="textarea" 
            v-model="profileForm.introduction"
            :rows="4"
            placeholder="请输入您的个人简介或工作经历"
          ></el-input>
        </el-form-item>
        
        <el-form-item class="form-buttons">
          <el-button type="primary" @click="submitForm">保存信息</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="password-card">
      <div class="card-header">
        <h3>修改密码</h3>
      </div>
      <el-form 
        :model="passwordForm" 
        :rules="passwordRules" 
        ref="passwordFormRef" 
        label-width="120px"
        class="password-form"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            show-password
            placeholder="请输入原密码"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            show-password
            placeholder="请输入新密码"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            show-password
            placeholder="请再次输入新密码"
          ></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="changePassword">修改密码</el-button>
          <el-button @click="resetPasswordForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, Phone, Message } from '@element-plus/icons-vue'
import { getCounselorProfile, updateCounselorProfile } from '@/api/counselor'
import { changePassword as apiChangePassword } from '@/api/user'
import { useRouter } from 'vue-router'

// 表单数据
const profileForm = reactive({
  employeeId: '',
  name: '',
  contact: '',
  email: '',
  college: '',
  title: '',
  specialization: '',
  experience: 0,
  officeLocation: '',
  officeHours: '',
  introduction: ''
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符之间', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

// 密码表单数据
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码表单校验规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 表单引用
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

// 路由
const router = useRouter()

// 获取辅导员信息
const fetchCounselorProfile = async () => {
  try {
    const response = await getCounselorProfile()
    if (response && response.error === 0 && response.body) {
      // 将body中的响应数据复制到表单对象中
      Object.assign(profileForm, response.body)
      
      // 如果某些字段未从后端返回，设置默认值
      if (profileForm.experience === null || profileForm.experience === undefined) {
        profileForm.experience = 0
      }
      
      console.log('辅导员资料已加载：', profileForm)
    } else {
      console.error('获取辅导员档案失败，响应格式不正确', response)
      ElMessage.error('获取个人信息失败，数据格式不正确')
    }
  } catch (error) {
    console.error('获取个人信息出错:', error)
    ElMessage.error('获取个人信息失败，请稍后重试')
  }
}

// 提交表单
const submitForm = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 提交前检查必填项是否已填写
        if (!profileForm.email) {
          ElMessage.warning('请填写电子邮箱')
          return
        }
        
        // 创建一个新对象，防止提交非表单数据
        const profileData = { ...profileForm }
        console.log('提交的数据：', profileData)
        
        // 提交数据更新请求
        const response = await updateCounselorProfile(profileData)
        
        // 处理响应
        if (response && response.error === 0) {
          ElMessage.success('个人信息保存成功')
          
          // 重新获取信息，确保显示最新数据
          await fetchCounselorProfile()
        } else {
          console.error('保存个人信息失败：', response)
          ElMessage.error('保存失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        console.error('保存个人信息出错:', error)
        ElMessage.error('保存失败，请稍后重试')
      }
    } else {
      ElMessage.warning('请正确填写表单信息')
      return false
    }
  })
}

// 重置表单
const resetForm = () => {
  ElMessageBox.confirm('确定要重置表单吗？所有未保存的修改将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    if (profileFormRef.value) {
      profileFormRef.value.resetFields()
      fetchCounselorProfile() // 重新获取原始数据
    }
  }).catch(() => {})
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 首先检查localStorage中是否有用户信息和ID
        const userInfo = localStorage.getItem('userInfo');
        if (!userInfo) {
          ElMessage.error('登录信息已失效，请重新登录');
          setTimeout(() => {
            router.push('/login');
          }, 2000);
          return;
        }
        
        console.log('开始修改密码，当前密码数据:', {
          oldPassword: passwordForm.oldPassword.length + '位字符', 
          newPassword: passwordForm.newPassword.length + '位字符'
        });
        
        const response = await apiChangePassword(passwordForm.oldPassword, passwordForm.newPassword)
        
        if (response && response.error === 0) {
          ElMessage.success('密码修改成功')
          resetPasswordForm()
        } else {
          console.error('修改密码失败：', response)
          ElMessage.error('修改密码失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        console.error('修改密码出错:', error)
        if (error.message && error.message.includes('未能获取用户ID')) {
          ElMessage.error('登录信息已失效，请重新登录');
          setTimeout(() => {
            router.push('/login');
          }, 2000);
        } else {
          ElMessage.error('修改密码失败，请稍后重试：' + (error.message || '未知错误'))
        }
      }
    } else {
      ElMessage.warning('请正确填写表单信息')
      return false
    }
  })
}

// 重置密码表单
const resetPasswordForm = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

// 页面加载时获取数据
onMounted(() => {
  console.log('辅导员个人页面已加载，开始获取档案数据')
  fetchCounselorProfile()
})
</script>

<style scoped>
.counselor-profile {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-weight: bold;
  color: #303133;
}

.profile-card, .password-card {
  margin-bottom: 20px;
}

.password-card {
  margin-top: 30px;
}

.card-header {
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  color: #303133;
}

.unit-label {
  margin-left: 10px;
  color: #606266;
}

.form-buttons {
  margin-top: 20px;
}

.el-divider {
  margin: 30px 0 20px;
}

.el-divider__text {
  font-weight: bold;
  color: #409EFF;
}
</style> 