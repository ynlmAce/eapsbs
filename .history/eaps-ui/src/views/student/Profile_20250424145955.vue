<template>
  <div class="student-profile">
    <el-card class="profile-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <h3>个人信息档案</h3>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存信息</el-button>
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
        <el-form-item prop="skills">
          <el-select
            v-model="form.skills"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入技能标签"
            :max-collapse-tags="5"
            style="width: 100%"
          >
            <el-option
              v-for="item in skillOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-divider></el-divider>
        
        <!-- 求职意向 -->
        <h4>求职意向</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作类型" prop="jobPreferences.jobType">
              <el-select v-model="form.jobPreferences.jobType" multiple placeholder="请选择工作类型" style="width: 100%">
                <el-option
                  v-for="item in jobTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望薪资" prop="jobPreferences.expectedSalary">
              <el-select v-model="form.jobPreferences.expectedSalary" placeholder="请选择期望薪资" style="width: 100%">
                <el-option label="3k以下" value="0-3k"></el-option>
                <el-option label="3k-5k" value="3k-5k"></el-option>
                <el-option label="5k-7k" value="5k-7k"></el-option>
                <el-option label="7k-10k" value="7k-10k"></el-option>
                <el-option label="10k-15k" value="10k-15k"></el-option>
                <el-option label="15k-20k" value="15k-20k"></el-option>
                <el-option label="20k-30k" value="20k-30k"></el-option>
                <el-option label="30k以上" value="30k+"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="期望城市" prop="jobPreferences.locations">
              <el-select v-model="form.jobPreferences.locations" multiple placeholder="请选择期望城市" style="width: 100%">
                <el-option
                  v-for="item in cityOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望行业" prop="jobPreferences.industries">
              <el-select v-model="form.jobPreferences.industries" multiple placeholder="请选择期望行业" style="width: 100%">
                <el-option
                  v-for="item in industryOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
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
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getStudentProfile, updateStudentProfile, uploadResumeFile } from '@/api/student'

const profileFormRef = ref(null)
const loading = ref(false)
const saving = ref(false)

// 表单数据
const form = reactive({
  name: '',
  studentId: '',
  school: '',
  major: '',
  grade: '',
  contact: '',
  selfIntroduction: '',
  educationExperience: [],
  internshipExperience: [],
  projectExperience: [],
  skills: [],
  jobPreferences: {
    jobType: [],
    expectedSalary: '',
    locations: [],
    industries: []
  },
  behaviorScore: 100, // 只读
  resumeFiles: []
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  school: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

// 技能选项
const skillOptions = [
  { value: 'java', label: 'Java' },
  { value: 'python', label: 'Python' },
  { value: 'javascript', label: 'JavaScript' },
  { value: 'html', label: 'HTML/CSS' },
  { value: 'vue', label: 'Vue.js' },
  { value: 'react', label: 'React' },
  { value: 'spring', label: 'Spring' },
  { value: 'mysql', label: 'MySQL' },
  { value: 'mongodb', label: 'MongoDB' },
  { value: 'redis', label: 'Redis' },
  { value: 'docker', label: 'Docker' },
  { value: 'linux', label: 'Linux' },
  { value: 'git', label: 'Git' },
  { value: 'office', label: 'Office' },
  { value: 'photoshop', label: 'Photoshop' },
  { value: 'english', label: '英语能力' }
]

// 工作类型选项
const jobTypeOptions = [
  { value: 'fulltime', label: '全职' },
  { value: 'parttime', label: '兼职' },
  { value: 'internship', label: '实习' }
]

// 城市选项
const cityOptions = [
  { value: 'beijing', label: '北京' },
  { value: 'shanghai', label: '上海' },
  { value: 'guangzhou', label: '广州' },
  { value: 'shenzhen', label: '深圳' },
  { value: 'hangzhou', label: '杭州' },
  { value: 'nanjing', label: '南京' },
  { value: 'wuhan', label: '武汉' },
  { value: 'chengdu', label: '成都' }
]

// 行业选项
const industryOptions = [
  { value: 'internet', label: '互联网/IT' },
  { value: 'finance', label: '金融' },
  { value: 'education', label: '教育' },
  { value: 'medical', label: '医疗健康' },
  { value: 'manufacturing', label: '制造业' },
  { value: 'estate', label: '房地产' },
  { value: 'culture', label: '文化/媒体' },
  { value: 'service', label: '服务业' }
]

// 获取学生档案信息
const getProfile = async () => {
  loading.value = true
  try {
    const res = await getStudentProfile()
    if (res.error === 0 && res.body) {
      Object.assign(form, res.body)
    }
    loading.value = false
  } catch (error) {
    console.error('获取个人档案失败:', error)
    ElMessage.error('获取个人档案失败，请稍后重试')
    loading.value = false
  }
}

// 保存个人档案信息
const saveProfile = async () => {
  if (!profileFormRef.value) return
  
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请完善必填信息')
    return
  }
  
  saving.value = true
  try {
    const res = await updateStudentProfile(form)
    if (res.error === 0) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存个人档案失败:', error)
    ElMessage.error('保存失败，请稍后重试')
    saving.value = false
  }
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

// 简历文件列表
const resumeFileList = ref([])

// 处理简历文件变更
const handleResumeChange = async (file, fileList) => {
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
  
  // 上传文件到服务器
  const formData = new FormData()
  formData.append('file', file.raw)
  
  try {
    const res = await uploadResumeFile(formData)
    console.log('简历上传响应:', res)
    
    if (res && !res.error) {
      ElMessage.success('简历上传成功')
      form.resumeFiles.push({
        fileId: res.id || res.fileId,
        fileName: res.fileName || file.name,
        fileUrl: res.filePath || res.fileUrl,
        uploadedAt: new Date().toISOString()
      })
    } else {
      ElMessage.error(res?.message || '上传失败')
    }
  } catch (error) {
    console.error('简历上传失败:', error)
    ElMessage.error('上传失败，请稍后重试')
  }
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

// 页面加载时获取数据
onMounted(() => {
  getProfile()
})
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