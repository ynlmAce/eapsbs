<template>
  <div class="forgot-password-container">
    <el-card class="forgot-password-card">
      <template #header>
        <div class="card-header">
          <h2>忘记密码</h2>
        </div>
      </template>
      
      <el-steps :active="activeStep" finish-status="success" simple>
        <el-step title="验证账号" />
        <el-step title="回答密保问题" />
        <el-step title="设置新密码" />
      </el-steps>
      
      <!-- 第一步：验证账号 -->
      <div v-if="activeStep === 0" class="step-container">
        <el-form :model="form" :rules="usernameRules" ref="usernameFormRef" label-width="100px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入您的用户名/学号/工号"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="verifyUsername">下一步</el-button>
            <el-button @click="goToLogin">返回登录</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 第二步：回答密保问题 -->
      <div v-else-if="activeStep === 1" class="step-container">
        <el-form :model="form" :rules="securityRules" ref="securityFormRef" label-width="100px">
          <el-form-item label="密保问题" prop="securityQuestion">
            <el-input v-model="securityQuestion" disabled></el-input>
          </el-form-item>
          <el-form-item label="密保答案" prop="securityAnswer">
            <el-input v-model="form.securityAnswer" placeholder="请输入密保答案" show-password></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="verifySecurityAnswer">下一步</el-button>
            <el-button @click="activeStep = 0">上一步</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 第三步：设置新密码 -->
      <div v-else-if="activeStep === 2" class="step-container">
        <el-form :model="form" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" show-password>
              <template #append>
                <el-tooltip :content="passwordStrengthTip" placement="top">
                  <div class="password-strength" :class="passwordStrength"></div>
                </el-tooltip>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="resetPassword">重置密码</el-button>
            <el-button @click="activeStep = 1">上一步</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 完成步骤 -->
      <div v-else class="step-container success-container">
        <el-result
          icon="success"
          title="密码重置成功"
          sub-title="您的密码已经重置成功，请使用新密码登录"
        >
          <template #extra>
            <el-button type="primary" @click="goToLogin">前往登录</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { verifyUsername, verifySecurityAnswer, resetPassword } from '@/api/user';

const router = useRouter();
const activeStep = ref(0);
const usernameFormRef = ref(null);
const securityFormRef = ref(null);
const passwordFormRef = ref(null);
const securityQuestion = ref('');

const form = reactive({
  username: '',
  securityAnswer: '',
  newPassword: '',
  confirmPassword: ''
});

// 表单验证规则
const usernameRules = {
  username: [
    { required: true, message: '请输入用户名/学号/工号', trigger: 'blur' }
  ]
};

const securityRules = {
  securityAnswer: [
    { required: true, message: '请输入密保答案', trigger: 'blur' }
  ]
};

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else {
    if (form.confirmPassword !== '') {
      passwordFormRef.value.validateField('confirmPassword');
    }
    callback();
  }
};

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入密码不一致'));
  } else {
    callback();
  }
};

const passwordRules = {
  newPassword: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 8, message: '密码长度至少为8个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
};

// 密码强度计算
const passwordStrength = computed(() => {
  const password = form.newPassword;
  if (!password) return '';
  
  let strength = 0;
  if (password.length >= 8) strength++;
  if (/[A-Z]/.test(password)) strength++;
  if (/[a-z]/.test(password)) strength++;
  if (/[0-9]/.test(password)) strength++;
  if (/[^A-Za-z0-9]/.test(password)) strength++;
  
  if (strength <= 2) return 'weak';
  if (strength <= 4) return 'medium';
  return 'strong';
});

const passwordStrengthTip = computed(() => {
  switch (passwordStrength.value) {
    case 'weak':
      return '密码强度：弱';
    case 'medium':
      return '密码强度：中';
    case 'strong':
      return '密码强度：强';
    default:
      return '请输入密码';
  }
});

// 第一步：验证账号
const verifyUsername = async () => {
  try {
    await usernameFormRef.value.validate();
    
    // 调用后端API验证用户名是否存在
    try {
      const response = await verifyUsername(form.username);
      
      if (response.error === 0) {
        // 获取密保问题
        securityQuestion.value = response.body.securityQuestion;
        activeStep.value = 1;
      } else {
        ElMessage.error(response.message || '验证用户名失败');
      }
    } catch (error) {
      console.error('验证用户名出错:', error);
      ElMessage.error('验证用户名失败，请稍后重试');
    }
  } catch (error) {
    console.error('表单验证失败:', error);
  }
};

// 第二步：验证密保答案
const verifySecurityAnswer = async () => {
  try {
    await securityFormRef.value.validate();
    
    // 调用后端API验证密保答案
    try {
      const response = await verifySecurityAnswer(form.username, form.securityAnswer);
      
      if (response.error === 0) {
        activeStep.value = 2;
      } else {
        ElMessage.error(response.message || '密保答案验证失败');
      }
    } catch (error) {
      console.error('验证密保答案出错:', error);
      ElMessage.error('验证密保答案失败，请稍后重试');
    }
  } catch (error) {
    console.error('表单验证失败:', error);
  }
};

// 第三步：重置密码
const resetPassword = async () => {
  try {
    await passwordFormRef.value.validate();
    
    // 调用后端API重置密码
    try {
      const response = await resetPassword(form.username, form.securityAnswer, form.newPassword);
      
      if (response.error === 0) {
        activeStep.value = 3;
      } else {
        ElMessage.error(response.message || '重置密码失败');
      }
    } catch (error) {
      console.error('重置密码出错:', error);
      ElMessage.error('重置密码失败，请稍后重试');
    }
  } catch (error) {
    console.error('表单验证失败:', error);
  }
};

// 跳转到登录页
const goToLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.forgot-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.forgot-password-card {
  width: 480px;
  max-width: 90%;
}

.card-header {
  display: flex;
  justify-content: center;
  align-items: center;
}

.step-container {
  margin-top: 20px;
  padding: 10px;
}

.success-container {
  text-align: center;
  padding: 20px 0;
}

.password-strength {
  width: 20px;
  height: 10px;
  border-radius: 5px;
}

.weak {
  background-color: #f56c6c;
}

.medium {
  background-color: #e6a23c;
}

.strong {
  background-color: #67c23a;
}
</style> 