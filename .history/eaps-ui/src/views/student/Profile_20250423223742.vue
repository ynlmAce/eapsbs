<template>
  <div class="student-profile">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>个人信息档案</h3>
          <el-button type="primary" @click="saveProfile">保存信息</el-button>
        </div>
      </template>
      
      <el-form :model="form" label-width="100px" :rules="rules" ref="profileFormRef">
        <!-- 基础信息 -->
        <h4>基础信息</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学号" prop="studentId">
              <el-input v-model="form.studentId" placeholder="请输入学号" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学校" prop="school">
              <el-input v-model="form.school" placeholder="请输入学校名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-input v-model="form.major" placeholder="请输入专业名称"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%">
                <el-option label="大一" value="大一"></el-option>
                <el-option label="大二" value="大二"></el-option>
                <el-option label="大三" value="大三"></el-option>
                <el-option label="大四" value="大四"></el-option>
                <el-option label="研一" value="研一"></el-option>
                <el-option label="研二" value="研二"></el-option>
                <el-option label="研三" value="研三"></el-option>
                <el-option label="博一" value="博一"></el-option>
                <el-option label="博二" value="博二"></el-option>
                <el-option label="博三" value="博三"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="contact">
              <el-input v-model="form.contact" placeholder="请输入联系方式"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider></el-divider>
        
        <!-- 自我介绍 -->
        <h4>自我介绍</h4>
        <el-form-item prop="selfIntroduction">
          <el-input v-model="form.selfIntroduction" type="textarea" :rows="4" placeholder="请简要介绍自己，包括性格特点、优势等（500字以内）"></el-input>
        </el-form-item>
        
        <el-divider></el-divider>
        
        <!-- 教育经历 -->
        <h4>教育经历</h4>
        <div v-for="(edu, index) in form.educationExperience" :key="'edu-' + index" class="experience-item">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item :label="'学校名称'" :prop="'educationExperience.' + index + '.schoolName'" :rules="{ required: true, message: '请输入学校名称', trigger: 'blur' }">
                <el-input v-model="edu.schoolName" placeholder="请输入学校名称"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="'专业'" :prop="'educationExperience.' + index + '.major'" :rules="{ required: true, message: '请输入专业', trigger: 'blur' }">
                <el-input v-model="edu.major" placeholder="请输入专业"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item :label="'学历'" :prop="'educationExperience.' + index + '.degree'" :rules="{ required: true, message: '请选择学历', trigger: 'change' }">
                <el-select v-model="edu.degree" placeholder="请选择学历" style="width: 100%">
                  <el-option label="高中" value="高中"></el-option>
                  <el-option label="大专" value="大专"></el-option>
                  <el-option label="本科" value="本科"></el-option>
                  <el-option label="硕士" value="硕士"></el-option>
                  <el-option label="博士" value="博士"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="'时间段'" :prop="'educationExperience.' + index + '.timeRange'" :rules="{ required: true, message: '请选择时间段', trigger: 'change' }">
                <el-date-picker v-model="edu.timeRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row>
            <el-col :span="24">
              <el-form-item :label="'描述'" :prop="'educationExperience.' + index + '.description'">
                <el-input v-model="edu.description" type="textarea" :rows="2" placeholder="请描述您在校期间的主要经历、成绩等"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-button type="danger" icon="Delete" circle size="small" @click="removeEducation(index)" class="remove-btn"></el-button>
          <el-divider v-if="index < form.educationExperience.length - 1"></el-divider>
        </div>
        
        <div class="add-item-container">
          <el-button type="primary" plain @click="addEducation">添加教育经历</el-button>
        </div>
        
        <el-divider></el-divider>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 表单引用
const profileFormRef = ref(null)

// 表单数据
const form = reactive({
  name: '',
  studentId: '',
  school: '',
  major: '',
  grade: '',
  contact: '',
  selfIntroduction: '',
  educationExperience: [
    {
      schoolName: '',
      major: '',
      degree: '',
      timeRange: [],
      description: ''
    }
  ],
  internshipExperience: [],
  projectExperience: [],
  skillTags: [],
  jobPreferences: {
    jobType: [],
    expectedLocation: [],
    expectedSalary: '',
    expectedIndustry: []
  },
  behaviorScore: 100, // 只读
  resumeFiles: []
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  school: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 在组件挂载时获取学生档案信息
onMounted(() => {
  // 模拟从服务器获取数据
  setTimeout(() => {
    form.name = '张三'
    form.studentId = '202200001'
    form.school = '示例大学'
    form.major = '计算机科学与技术'
    form.grade = '大三'
    form.contact = '13800138000'
    form.selfIntroduction = '我是一名充满热情的计算机专业学生，对人工智能和软件开发有浓厚兴趣。善于团队合作，有良好的沟通能力和解决问题的能力。'
  }, 1000)

  /**
   * TODO: 实际实现时调用API获取学生个人档案信息
   * const fetchStudentProfile = async () => {
   *   const res = await api.student.getProfile()
   *   if (res.success) {
   *     Object.assign(form, res.data)
   *   }
   * }
   * fetchStudentProfile()
   */
})

// 添加教育经历
const addEducation = () => {
  form.educationExperience.push({
    schoolName: '',
    major: '',
    degree: '',
    timeRange: [],
    description: ''
  })
}

// 移除教育经历
const removeEducation = (index) => {
  form.educationExperience.splice(index, 1)
}

// 保存个人信息
const saveProfile = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.error('表单填写有误，请检查')
    return
  }
  
  // 模拟API调用保存信息
  ElMessage.success('个人信息保存成功')

  /**
   * TODO: 实际实现时调用API保存学生个人档案信息
   * try {
   *   const res = await api.student.saveProfile(form)
   *   if (res.success) {
   *     ElMessage.success('个人信息保存成功')
   *   } else {
   *     ElMessage.error(res.message || '保存失败')
   *   }
   * } catch (error) {
   *   console.error('保存个人信息失败:', error)
   *   ElMessage.error('系统错误，请稍后重试')
   * }
   */
}
</script>

<style scoped>
.student-profile {
  padding: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h4 {
  font-size: 16px;
  font-weight: bold;
  margin: 15px 0;
}

.experience-item {
  position: relative;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 5px;
  margin-bottom: 15px;
}

.remove-btn {
  position: absolute;
  top: 10px;
  right: 10px;
}

.add-item-container {
  display: flex;
  justify-content: center;
  margin: 15px 0;
}
</style> 