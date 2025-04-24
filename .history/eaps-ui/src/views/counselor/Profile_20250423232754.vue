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

// 获取辅导员信息
const fetchCounselorProfile = async () => {
  try {
    // 模拟从API获取数据
    const response = {
      data: {
        error: 0,
        body: {
          employeeId: 'T20240001',
          name: '张老师',
          contact: '13800138000',
          email: 'zhang@example.edu.cn',
          college: '计算机科学与技术学院',
          title: '副教授',
          specialization: '计算机科学',
          experience: 8,
          officeLocation: '教学楼B区302室',
          officeHours: '周一至周五 14:00-16:00',
          introduction: '从事大学生就业指导工作8年，擅长IT行业就业咨询和职业规划指导。'
        },
        message: '获取成功'
      }
    }
    
    // 如果API调用成功，更新表单数据
    if (response.data.error === 0) {
      Object.assign(profileForm, response.data.body)
    } else {
      ElMessage.error(response.data.message || '获取个人信息失败')
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
        // 这里应该调用实际的API
        ElMessage.success('个人信息保存成功')
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
        // 这里应该调用实际的API
        ElMessage.success('密码修改成功')
        resetPasswordForm()
      } catch (error) {
        console.error('修改密码出错:', error)
        ElMessage.error('修改密码失败，请稍后重试')
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