<template>
  <div class="student-layout">
    <el-container>
      <el-aside width="200px">
        <el-menu
          class="el-menu-vertical"
          :default-active="activeMenu"
          router
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <el-menu-item index="/student/profile">
            <el-icon><UserFilled /></el-icon>
            <span>个人档案</span>
          </el-menu-item>
          <el-menu-item index="/student/jobs">
            <el-icon><Briefcase /></el-icon>
            <span>岗位浏览</span>
          </el-menu-item>
          <el-menu-item index="/student/applications">
            <el-icon><Files /></el-icon>
            <span>投递记录</span>
          </el-menu-item>
          <el-menu-item index="/student/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>沟通中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-container">
            <div class="logo">大学生就业帮扶系统</div>
            <div class="user-info">
              <el-dropdown>
                <span class="user-dropdown-link">
                  {{ userInfo.name || '学生用户' }}
                  <el-icon><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="goToProfile">个人档案</el-dropdown-item>
                    <el-dropdown-item @click="handlePasswordChange">修改密码</el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="30%">
      <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordChange">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { UserFilled, Briefcase, Files, ChatDotRound, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { changePassword } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 从userStore获取用户信息
const userInfo = computed(() => userStore.userInfo || { name: '学生用户' })

// 修改密码相关
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
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

// 处理密码修改请求
const handlePasswordChange = () => {
  passwordDialogVisible.value = true
}

// 提交密码修改
const submitPasswordChange = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    const res = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    if (res && res.error === 0) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
      // 重置表单
      passwordFormRef.value.resetFields()
    } else {
      ElMessage.error(res?.message || '密码修改失败')
    }
  } catch (error) {
    console.error('密码修改失败:', error)
    ElMessage.error('密码修改失败，请稍后重试')
  }
}

// 跳转到个人档案页
const goToProfile = () => {
  router.push('/student/profile')
}

// 处理登出
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 使用userStore的logout方法
    userStore.logout()
    
    // 重定向到登录页
    router.push('/login')
    ElMessage.success('已成功退出登录')
  }).catch(() => {})
}
</script>

<style scoped>
.student-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #545c64;
  color: white;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-container {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 18px;
  font-weight: bold;
}

.user-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.user-dropdown-link .el-icon {
  margin-left: 8px;
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
}
</style> 