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
        
        <!-- 实习经历 -->
        <h4>实习经历</h4>
        <div v-for="(internship, index) in form.internshipExperience" :key="'internship-' + index" class="experience-item">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item :label="'公司名称'" :prop="'internshipExperience.' + index + '.companyName'" :rules="{ required: true, message: '请输入公司名称', trigger: 'blur' }">
                <el-input v-model="internship.companyName" placeholder="请输入公司名称"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="'职位名称'" :prop="'internshipExperience.' + index + '.position'" :rules="{ required: true, message: '请输入职位名称', trigger: 'blur' }">
                <el-input v-model="internship.position" placeholder="请输入职位名称"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item :label="'时间段'" :prop="'internshipExperience.' + index + '.timeRange'" :rules="{ required: true, message: '请选择时间段', trigger: 'change' }">
                <el-date-picker v-model="internship.timeRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row>
            <el-col :span="24">
              <el-form-item :label="'工作内容'" :prop="'internshipExperience.' + index + '.description'" :rules="{ required: true, message: '请描述工作内容', trigger: 'blur' }">
                <el-input v-model="internship.description" type="textarea" :rows="3" placeholder="请描述您的工作内容、职责、成果等"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-button type="danger" icon="Delete" circle size="small" @click="removeInternship(index)" class="remove-btn"></el-button>
          <el-divider v-if="index < form.internshipExperience.length - 1"></el-divider>
        </div>
        
        <div class="add-item-container">
          <el-button type="primary" plain @click="addInternship">添加实习经历</el-button>
        </div>
        
        <el-divider></el-divider>
        
        <!-- 项目经历 -->
        <h4>项目经历</h4>
        <div v-for="(project, index) in form.projectExperience" :key="'project-' + index" class="experience-item">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item :label="'项目名称'" :prop="'projectExperience.' + index + '.projectName'" :rules="{ required: true, message: '请输入项目名称', trigger: 'blur' }">
                <el-input v-model="project.projectName" placeholder="请输入项目名称"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="'担任角色'" :prop="'projectExperience.' + index + '.role'" :rules="{ required: true, message: '请输入担任角色', trigger: 'blur' }">
                <el-input v-model="project.role" placeholder="请输入担任角色"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item :label="'时间段'" :prop="'projectExperience.' + index + '.timeRange'" :rules="{ required: true, message: '请选择时间段', trigger: 'change' }">
                <el-date-picker v-model="project.timeRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row>
            <el-col :span="24">
              <el-form-item :label="'项目描述'" :prop="'projectExperience.' + index + '.description'" :rules="{ required: true, message: '请描述项目内容', trigger: 'blur' }">
                <el-input v-model="project.description" type="textarea" :rows="3" placeholder="请描述项目背景、您的职责、使用的技术、取得的成果等"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-button type="danger" icon="Delete" circle size="small" @click="removeProject(index)" class="remove-btn"></el-button>
          <el-divider v-if="index < form.projectExperience.length - 1"></el-divider>
        </div>
        
        <div class="add-item-container">
          <el-button type="primary" plain @click="addProject">添加项目经历</el-button>
        </div>
        
        <el-divider></el-divider>
        
        <!-- 技能标签 -->
        <h4>技能标签</h4>
        <el-form-item prop="skillTags">
          <el-tag
            v-for="tag in form.skillTags"
            :key="tag"
            class="skill-tag"
            closable
            @close="removeSkillTag(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="inputVisible"
            ref="tagInputRef"
            v-model="inputValue"
            class="tag-input"
            size="small"
            @keyup.enter="handleInputConfirm"
            @blur="handleInputConfirm"
          />
          <el-button v-else class="button-new-tag" size="small" @click="showInput">+ 添加技能</el-button>
        </el-form-item>
        
        <el-divider></el-divider>
        
        <!-- 求职意向 -->
        <h4>求职意向</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="期望工作类型" prop="jobPreferences.jobType">
              <el-select v-model="form.jobPreferences.jobType" multiple placeholder="请选择期望工作类型" style="width: 100%">
                <el-option label="全职" value="全职"></el-option>
                <el-option label="兼职" value="兼职"></el-option>
                <el-option label="实习" value="实习"></el-option>
                <el-option label="校招" value="校招"></el-option>
                <el-option label="社招" value="社招"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望薪资" prop="jobPreferences.expectedSalary">
              <el-select v-model="form.jobPreferences.expectedSalary" placeholder="请选择期望薪资" style="width: 100%">
                <el-option label="面议" value="面议"></el-option>
                <el-option label="3k以下" value="3k以下"></el-option>
                <el-option label="3k-5k" value="3k-5k"></el-option>
                <el-option label="5k-7k" value="5k-7k"></el-option>
                <el-option label="7k-10k" value="7k-10k"></el-option>
                <el-option label="10k-15k" value="10k-15k"></el-option>
                <el-option label="15k-20k" value="15k-20k"></el-option>
                <el-option label="20k-30k" value="20k-30k"></el-option>
                <el-option label="30k以上" value="30k以上"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="期望行业" prop="jobPreferences.expectedIndustry">
              <el-select v-model="form.jobPreferences.expectedIndustry" multiple placeholder="请选择期望行业" style="width: 100%">
                <el-option label="IT/互联网/通信" value="IT/互联网/通信"></el-option>
                <el-option label="金融/银行/保险" value="金融/银行/保险"></el-option>
                <el-option label="教育/培训" value="教育/培训"></el-option>
                <el-option label="房地产/建筑" value="房地产/建筑"></el-option>
                <el-option label="医疗/健康" value="医疗/健康"></el-option>
                <el-option label="消费品" value="消费品"></el-option>
                <el-option label="汽车/机械/制造" value="汽车/机械/制造"></el-option>
                <el-option label="能源/环保" value="能源/环保"></el-option>
                <el-option label="政府/非盈利机构" value="政府/非盈利机构"></el-option>
                <el-option label="其他" value="其他"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望工作地点" prop="jobPreferences.expectedLocation">
              <el-select v-model="form.jobPreferences.expectedLocation" multiple placeholder="请选择期望工作地点" style="width: 100%">
                <el-option label="北京" value="北京"></el-option>
                <el-option label="上海" value="上海"></el-option>
                <el-option label="广州" value="广州"></el-option>
                <el-option label="深圳" value="深圳"></el-option>
                <el-option label="杭州" value="杭州"></el-option>
                <el-option label="南京" value="南京"></el-option>
                <el-option label="武汉" value="武汉"></el-option>
                <el-option label="成都" value="成都"></el-option>
                <el-option label="西安" value="西安"></el-option>
                <el-option label="其他" value="其他"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider></el-divider>
        
        <!-- 简历上传 -->
        <h4>简历附件</h4>
        <el-upload
          class="resume-upload"
          action="#"
          :auto-upload="false"
          :on-change="handleResumeChange"
          :on-remove="handleResumeRemove"
          :file-list="resumeFileList"
          :limit="1"
        >
          <el-button type="primary">上传简历</el-button>
          <template #tip>
            <div class="el-upload__tip">只能上传PDF或DOCX文件，且不超过5MB</div>
          </template>
        </el-upload>
        
        <el-divider></el-divider>
        
        <!-- 行为分数（只读） -->
        <h4>行为分数</h4>
        <el-form-item prop="behaviorScore">
          <el-progress :percentage="form.behaviorScore" :format="percentageFormat" :status="getBehaviorScoreStatus"></el-progress>
          <div class="score-note">
            行为分反映您在平台上的综合表现，包括信息完善度、投递简历后的响应度、面试出勤率等因素。保持良好的行为习惯有助于提高企业对您的关注度。
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

// 表单引用和标签输入框引用
const profileFormRef = ref(null)
const tagInputRef = ref(null)

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
    form.skillTags = ['Java', 'Python', 'Vue.js', '数据分析', '机器学习']
    form.jobPreferences = {
      jobType: ['实习', '校招'],
      expectedLocation: ['北京', '上海', '杭州'],
      expectedSalary: '10k-15k',
      expectedIndustry: ['IT/互联网/通信']
    }
    form.behaviorScore = 85
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

// 技能标签相关
const inputVisible = ref(false)
const inputValue = ref('')

// 显示技能标签输入框
const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    tagInputRef.value.focus()
  })
}

// 处理技能标签输入确认
const handleInputConfirm = () => {
  if (inputValue.value) {
    if (form.skillTags.includes(inputValue.value)) {
      ElMessage.warning('该技能标签已存在')
    } else if (form.skillTags.length >= 10) {
      ElMessage.warning('最多添加10个技能标签')
    } else {
      form.skillTags.push(inputValue.value)
    }
  }
  inputVisible.value = false
  inputValue.value = ''
}

// 移除技能标签
const removeSkillTag = (tag) => {
  form.skillTags.splice(form.skillTags.indexOf(tag), 1)
}

// 简历文件列表
const resumeFileList = ref([])

// 处理简历文件变更
const handleResumeChange = (file, fileList) => {
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过5MB')
    resumeFileList.value = fileList.filter(f => f.uid !== file.uid)
    return
  }
  
  const fileExtension = file.name.split('.').pop().toLowerCase()
  if (!['pdf', 'docx'].includes(fileExtension)) {
    ElMessage.error('只能上传PDF或DOCX格式的文件')
    resumeFileList.value = fileList.filter(f => f.uid !== file.uid)
    return
  }
  
  resumeFileList.value = fileList
  
  // 实际项目中需要上传文件到服务器
  // TODO: 文件上传逻辑
}

// 处理简历文件移除
const handleResumeRemove = (file, fileList) => {
  resumeFileList.value = fileList
}

// 行为分数状态
const getBehaviorScoreStatus = computed(() => {
  if (form.behaviorScore >= 90) return 'success'
  if (form.behaviorScore >= 70) return ''
  if (form.behaviorScore >= 50) return 'warning'
  return 'exception'
})

// 分数格式化
const percentageFormat = (percentage) => {
  return percentage + '分'
}

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

// 添加实习经历
const addInternship = () => {
  form.internshipExperience.push({
    companyName: '',
    position: '',
    timeRange: [],
    description: ''
  })
}

// 移除实习经历
const removeInternship = (index) => {
  form.internshipExperience.splice(index, 1)
}

// 添加项目经历
const addProject = () => {
  form.projectExperience.push({
    projectName: '',
    role: '',
    timeRange: [],
    description: ''
  })
}

// 移除项目经历
const removeProject = (index) => {
  form.projectExperience.splice(index, 1)
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

.skill-tag {
  margin-right: 10px;
  margin-bottom: 10px;
}

.tag-input {
  width: 120px;
  margin-right: 10px;
  vertical-align: bottom;
}

.button-new-tag {
  margin-right: 10px;
  height: 32px;
  padding-top: 0;
  padding-bottom: 0;
}

.resume-upload {
  margin-bottom: 20px;
}

.score-note {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style> 