<template>
  <div class="student-profile">
    <el-card class="profile-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <h3>个人信息档案</h3>
          <div>
            <el-button type="primary" @click="saveProfile" :loading="saving">保存信息</el-button>
            <el-button v-if="ENV.MODE === 'development'" type="info" @click="testProfileAPI">测试API</el-button>
            <!-- 仅在开发环境下显示跳过选项 -->
            <el-switch 
              v-if="ENV.MODE === 'development'"
              v-model="skipExperienceSave" 
              inactive-text="跳过项目/实习经历" 
              style="margin-left: 10px;"
            ></el-switch>
          </div>
        </div>
      </template>
      
      <el-form :model="form" label-width="100px" :rules="rules" ref="profileFormRef">
        <!-- 基础信息 -->
        <h4>基础信息</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" disabled></el-input>
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
              <el-input v-model="form.school" placeholder="请输入学校名称" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-input v-model="form.major" placeholder="请输入专业名称" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%" disabled>
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
              <el-input v-model="form.contact" placeholder="请输入联系方式" disabled></el-input>
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
import { 
  getStudentProfile, 
  updateStudentProfile, 
  uploadResume,
  updateEducationExperience,
  updateProjectExperience,
  updateSkillTags,
  getProjectExperiences,
  getEducationExperiences,
  getSkillTags,
  getStudentSkills
} from '@/api/student'
import { useUserStore } from '@/store/user' // 修复导入路径
import { useRouter } from 'vue-router'
import { ENV } from '@/config/env'

const profileFormRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const userStore = useUserStore() // 获取用户存储实例
const router = useRouter()

// 表单数据
const form = reactive({
  id: '',
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
  selfIntroduction: [
    { max: 500, message: '自我介绍不能超过500个字符', trigger: 'blur' }
  ]
}

// 技能选项
const skillOptions = ref([]);

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

// 在script部分增加变量
const skipExperienceSave = ref(false);

// 获取学生档案信息
const getProfile = async () => {
  loading.value = true // 开始加载时设置为true
  
  try {
    // 清理现有表单数据，避免数据混淆
    console.log('开始获取学生档案数据...')
    
    // 检查登录状态
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('您的登录已过期，请重新登录')
      router.push('/login?expired=true')
      return
    }
    
    try {
      // 调用API获取数据
      const response = await getStudentProfile()
      
      // 根据API响应结构处理数据
      console.log('原始API响应:', response)
      
      if (!response) {
        throw new Error('服务器没有返回数据')
      }
      
      // 统一处理各种可能的响应格式
      let profileData = null
      
      if (response.error === 0 && response.body) {
        // 标准格式 {error, body, message}
        profileData = response.body
        console.log('从response.body获取数据:', profileData)
      } else if (response.error !== undefined && response.error !== 0) {
        // 错误响应
        throw new Error(response.message || '获取档案失败')
      } else {
        // 可能直接返回的是数据本身
        profileData = response
        console.log('直接使用response作为数据:', profileData)
      }
      
      if (!profileData) {
        throw new Error('无法解析学生档案数据')
      }
      
      console.log('解析后的档案数据:', profileData)
      
      // 重置表单数据，防止混合旧数据
      form.educationExperience = [];
      form.internshipExperience = [];
      form.projectExperience = [];
      form.skills = [];
      form.jobPreferences = {
        jobType: [],
        expectedSalary: '',
        locations: [],
        industries: []
      };
      
      // 填充表单数据 - 基础信息
      form.id = profileData.id || '';
      form.name = profileData.name || '';
      
      // 获取用户登录账号作为学号
      const userInfo = localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')) : null;
      form.studentId = profileData.studentId || profileData.username || (userInfo ? userInfo.username : '');
      
      form.school = profileData.school || '';
      form.major = profileData.major || '';
      form.grade = profileData.grade || '';
      form.contact = profileData.contact || '';
      
      // 自我介绍
      form.selfIntroduction = profileData.selfIntroduction || '';
      
      // 解析教育经历 - 可能是字符串或者数组
      if (profileData.educationExperience) {
        try {
          if (typeof profileData.educationExperience === 'string') {
            form.educationExperience = JSON.parse(profileData.educationExperience) || [];
          } else if (Array.isArray(profileData.educationExperience)) {
            form.educationExperience = profileData.educationExperience;
          }
        } catch (e) {
          console.error('解析educationExperience失败:', e);
          form.educationExperience = [];
        }
      }
      
      if (form.educationExperience.length === 0) {
        // 添加一个默认的教育经历项，并确保包含所有必填字段
        form.educationExperience.push({
          schoolName: '',
          major: '',
          degree: '',
          timeRange: [],
          description: ''
        });
      } else {
        // 确保每个教育经历项都有必填字段
        form.educationExperience = form.educationExperience.map(edu => ({
          schoolName: edu.schoolName || '',
          major: edu.major || '',
          degree: edu.degree || '',
          timeRange: edu.timeRange || [],
          description: edu.description || ''
        }));
      }
      
      // 解析实习经历 - 可能是字符串或者数组
      if (profileData.internshipExperience) {
        try {
          if (typeof profileData.internshipExperience === 'string') {
            form.internshipExperience = JSON.parse(profileData.internshipExperience) || [];
          } else if (Array.isArray(profileData.internshipExperience)) {
            form.internshipExperience = profileData.internshipExperience;
          }
        } catch (e) {
          console.error('解析internshipExperience失败:', e);
          form.internshipExperience = [];
        }
      }
      
      // 确保每个实习经历项都有必填字段
      if (form.internshipExperience.length > 0) {
        form.internshipExperience = form.internshipExperience.map(internship => ({
          companyName: internship.companyName || '',
          position: internship.position || '',
          timeRange: internship.timeRange || [],
          description: internship.description || ''
        }));
      }
      
      // 解析项目经历 - 可能是字符串或者数组
      if (profileData.projectExperience) {
        try {
          if (typeof profileData.projectExperience === 'string') {
            form.projectExperience = JSON.parse(profileData.projectExperience) || [];
          } else if (Array.isArray(profileData.projectExperience)) {
            form.projectExperience = profileData.projectExperience;
          }
        } catch (e) {
          console.error('解析projectExperience失败:', e);
          form.projectExperience = [];
        }
      }
      
      // 确保每个项目经历项都有必填字段
      if (form.projectExperience.length > 0) {
        form.projectExperience = form.projectExperience.map(project => ({
          projectName: project.projectName || '',
          role: project.role || '',
          timeRange: project.timeRange || [],
          description: project.description || ''
        }));
      }
      
      // 技能标签 - 可能是字符串或者数组
      if (profileData.skills) {
        try {
          if (typeof profileData.skills === 'string') {
            form.skills = JSON.parse(profileData.skills) || [];
          } else if (Array.isArray(profileData.skills)) {
            form.skills = profileData.skills;
          }
        } catch (e) {
          console.error('解析skills失败:', e);
          form.skills = [];
        }
      }
      
      // 求职意向
      if (profileData.jobPreferences) {
        try {
          let preferences = profileData.jobPreferences;
          
          if (typeof preferences === 'string') {
            preferences = JSON.parse(preferences);
          }
          
          form.jobPreferences = {
            jobType: Array.isArray(preferences.jobType) ? preferences.jobType : [],
            expectedSalary: preferences.expectedSalary || '',
            locations: Array.isArray(preferences.locations) ? preferences.locations : [],
            industries: Array.isArray(preferences.industries) ? preferences.industries : []
          };
        } catch (e) {
          console.error('解析jobPreferences失败:', e);
          form.jobPreferences = {
            jobType: [],
            expectedSalary: '',
            locations: [],
            industries: []
          };
        }
      }
      
      // 行为分数
      form.behaviorScore = profileData.behaviorScore || 100;
      
      // 简历文件
      if (Array.isArray(profileData.resumeFiles) && profileData.resumeFiles.length > 0) {
        form.resumeFiles = profileData.resumeFiles;
        resumeFileList.value = profileData.resumeFiles.map(file => ({
          name: file.fileName || file.name,
          url: file.fileUrl || file.url
        }));
      } else {
        form.resumeFiles = [];
        resumeFileList.value = [];
      }
      
      console.log('学生档案数据加载成功，表单数据:', form)
      
    } catch (apiError) {
      console.error('API调用失败:', apiError)
      ElMessage.error(`获取档案数据失败: ${apiError.message || '未知错误'}`)
    }
    
  } catch (error) {
    console.error('整体获取档案流程失败:', error)
    ElMessage.error('获取个人档案时发生错误，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

// 保存个人档案信息
const saveProfile = async () => {
  // 先进行表单验证
  if (!profileFormRef.value) {
    ElMessage.warning('表单引用不可用，请刷新页面重试');
    return;
  }
  
  try {
    // 等待表单验证完成
    await profileFormRef.value.validate();
    
    // 表单验证通过，显示加载态
    saving.value = true;
    ElMessage.info('正在保存数据...');
    console.log('开始保存个人档案');

    try {
      // 步骤1：保存基本信息到student_profile表
      console.log('步骤1：保存基本信息');
      const basicProfileData = {
        id: form.id,
        name: form.name,
        contact: form.contact,
        school: form.school,
        major: form.major,
        grade: form.grade,
        selfIntroduction: form.selfIntroduction,
        jobPreferences: JSON.stringify(form.jobPreferences || {})
      }
      console.log('基本信息数据:', basicProfileData);
      const basicResponse = await updateStudentProfile(basicProfileData);
      console.log('基本信息API响应:', basicResponse);
      
      // 检查响应格式并处理
      if (basicResponse && typeof basicResponse === 'object') {
        // 如果是标准响应格式 {error, body, message}
        if ('error' in basicResponse) {
          if (basicResponse.error !== 0) {
            throw new Error(`保存基本信息失败: ${basicResponse.message || '未知错误'}`);
          }
          // 保存结果
          console.log('基本信息保存结果:', basicResponse.body);
        } 
        // 如果直接返回结果（如true或其他值）
        else {
          console.log('基本信息保存结果(直接返回对象):', basicResponse);
        }
      } else {
        // 如果不是对象，可能是API返回的基本类型值(比如true)
        console.log('基本信息保存结果(直接返回基本类型):', basicResponse);
        // 不抛出错误，允许处理基本类型的响应
      }
      
      // 步骤2：保存教育经历到student_structured_resume表
      console.log('步骤2：保存教育经历');
      if (form.educationExperience && form.educationExperience.length > 0) {
        // 确保所有必要字段都存在
        const validEducations = form.educationExperience.filter(edu => 
          edu.schoolName && edu.major && edu.degree);
        
        if (validEducations.length > 0) {
          const educationData = {
            educationExperiences: validEducations.map(edu => ({
              schoolName: edu.schoolName,
              major: edu.major,
              degree: edu.degree,
              timeRange: edu.timeRange,
              description: edu.description || ''
            }))
          }
          console.log('教育经历数据:', educationData);
          const educationResponse = await updateEducationExperience(educationData);
          console.log('教育经历API响应:', educationResponse);
          
          // 检查响应格式并处理
          if (educationResponse && typeof educationResponse === 'object') {
            if ('error' in educationResponse) {
              if (educationResponse.error !== 0) {
                throw new Error(`保存教育经历失败: ${educationResponse.message || '未知错误'}`);
              }
              console.log('教育经历保存结果:', educationResponse.body);
            } else {
              console.log('教育经历保存结果(直接返回对象):', educationResponse);
            }
          } else {
            // 如果不是对象，可能是API返回的基本类型值(比如true)
            console.log('教育经历保存结果(直接返回基本类型):', educationResponse);
            // 不抛出错误，允许处理基本类型的响应
          }
        } else {
          console.log('没有有效的教育经历数据需要保存');
        }
      }
      
      // 步骤3：保存项目/实习经历
      console.log('步骤3：保存项目/实习经历');
      
      // 检查是否跳过项目/实习经历保存
      if (skipExperienceSave.value) {
        console.log('根据设置跳过项目/实习经历保存');
        ElMessage.warning('已跳过项目/实习经历保存');
      } else {
        const combinedExperiences = [];
        
        // 添加实习经历
        if (form.internshipExperience && form.internshipExperience.length > 0) {
          // 过滤有效的实习经历
          const validInternships = form.internshipExperience.filter(internship => 
            internship.companyName && internship.position && internship.description);
            
          console.log('有效的实习经历数据:', validInternships);
          
          validInternships.forEach(internship => {
            // 检查并输出时间范围
            console.log(`实习 ${internship.companyName} 的时间范围:`, 
              internship.timeRange, 
              '类型:', Array.isArray(internship.timeRange) ? 'Array' : typeof internship.timeRange
            );
            
            // 格式化日期为YYYY-MM-DD
            let formattedTimeRange = [];
            if (Array.isArray(internship.timeRange) && internship.timeRange.length === 2) {
              formattedTimeRange = internship.timeRange.map(date => {
                if (date instanceof Date) {
                  // 仅提取日期部分，不含时区信息 (YYYY-MM-DD)
                  return date.toISOString().split('T')[0];
                } else if (typeof date === 'string') {
                  // 如果已经是字符串，尝试提取日期部分
                  if (date.includes('T')) {
                    return date.split('T')[0];
                  }
                  return date;
                }
                return null; // 对于无效值
              });
            }
            
            console.log(`实习 ${internship.companyName} 的格式化后时间范围:`, formattedTimeRange);
            
            combinedExperiences.push({
              type: 'internship',
              companyName: internship.companyName,
              position: internship.position,
              timeRange: formattedTimeRange.length === 2 ? formattedTimeRange : null,
              description: internship.description
            });
          });
        }
        
        // 添加项目经历
        if (form.projectExperience && form.projectExperience.length > 0) {
          // 过滤有效的项目经历
          const validProjects = form.projectExperience.filter(project => 
            project.projectName && project.role && project.description);
            
          console.log('有效的项目经历数据:', validProjects);
          
          validProjects.forEach(project => {
            // 检查并输出时间范围
            console.log(`项目 ${project.projectName} 的时间范围:`, 
              project.timeRange, 
              '类型:', Array.isArray(project.timeRange) ? 'Array' : typeof project.timeRange
            );
            
            // 格式化日期为YYYY-MM-DD
            let formattedTimeRange = [];
            if (Array.isArray(project.timeRange) && project.timeRange.length === 2) {
              formattedTimeRange = project.timeRange.map(date => {
                if (date instanceof Date) {
                  // 仅提取日期部分，不含时区信息 (YYYY-MM-DD)
                  return date.toISOString().split('T')[0];
                } else if (typeof date === 'string') {
                  // 如果已经是字符串，尝试提取日期部分
                  if (date.includes('T')) {
                    return date.split('T')[0];
                  }
                  return date;
                }
                return null; // 对于无效值
              });
            }
            
            console.log(`项目 ${project.projectName} 的格式化后时间范围:`, formattedTimeRange);
            
            combinedExperiences.push({
              type: 'project',
              projectName: project.projectName,
              role: project.role,
              timeRange: formattedTimeRange.length === 2 ? formattedTimeRange : null,
              description: project.description
            });
          });
        }
        
        // 项目经历保存逻辑
        if (combinedExperiences.length > 0) {
          const projectData = {
            projectExperiences: combinedExperiences
          }
          // 详细打印将要发送的数据
          console.log('项目/实习经历数据结构:', projectData);
          console.log('项目/实习经历数据字符串形式:', JSON.stringify(projectData));
          
          try {
            const projectResponse = await updateProjectExperience(projectData);
            console.log('项目/实习经历API响应:', projectResponse);
            
            // 检查响应格式并处理
            if (projectResponse && typeof projectResponse === 'object') {
              if ('error' in projectResponse) {
                if (projectResponse.error !== 0) {
                  throw new Error(`保存项目/实习经历失败: ${projectResponse.message || '未知错误'}`);
                }
                console.log('项目/实习经历保存结果:', projectResponse.body);
              } else {
                console.log('项目/实习经历保存结果(直接返回对象):', projectResponse);
              }
            } else {
              // 如果不是对象，可能是API返回的基本类型值(比如true)
              console.log('项目/实习经历保存结果(直接返回基本类型):', projectResponse);
              // 不抛出错误，允许处理基本类型的响应
            }
          } catch (error) {
            console.error('项目/实习经历保存出错:', error);
            // 如果只有项目/实习经历保存失败，继续保存其他内容
            if (error.response && error.response.data) {
              console.error('服务器返回的错误详情:', error.response.data);
            }
            ElMessage.warning('项目和实习经历保存失败，但其他信息已保存');
          }
        } else {
          console.log('没有有效的项目/实习经历数据需要保存');
        }
      }
      
      // 步骤4：保存技能标签到student_structured_resume表
      console.log('步骤4：保存技能标签');
      if (form.skills && form.skills.length > 0) {
        const skillsData = {
          skillTags: form.skills.map(skill => {
            // 如果是对象格式，提取值；如果是字符串，直接使用
            return typeof skill === 'object' ? skill.value : skill
          })
        }
        console.log('技能标签数据:', skillsData);
        const skillsResponse = await updateSkillTags(skillsData);
        console.log('技能标签API响应:', skillsResponse);
        
        // 检查响应格式并处理
        if (skillsResponse && typeof skillsResponse === 'object') {
          if ('error' in skillsResponse) {
            if (skillsResponse.error !== 0) {
              throw new Error(`保存技能标签失败: ${skillsResponse.message || '未知错误'}`);
            }
            console.log('技能标签保存结果:', skillsResponse.body);
          } else {
            console.log('技能标签保存结果(直接返回对象):', skillsResponse);
          }
        } else {
          // 如果不是对象，可能是API返回的基本类型值(比如true)
          console.log('技能标签保存结果(直接返回基本类型):', skillsResponse);
          // 不抛出错误，允许处理基本类型的响应
        }
      }
      
      // 所有数据保存成功
      console.log('所有数据保存成功');
      ElMessage.success('个人档案保存成功');
      
      // 延迟获取最新数据，确保后端处理完毕
      setTimeout(() => {
        getProfile();
        // 刷新页面
        window.location.reload();
      }, 500);
      
    } catch (error) {
      console.error('保存个人档案失败:', error);
      ElMessage.error(`保存失败: ${error.message || '未知错误'}`);
    } finally {
      saving.value = false;
    }
  } catch (validationError) {
    console.error('表单验证失败:', validationError);
    ElMessage.error('请检查表单，确保所有必填字段都已填写');
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
  console.log('处理简历文件变更:', file);
  
  // 检查文件大小
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过5MB');
    resumeFileList.value = fileList.filter(f => f.uid !== file.uid);
    return;
  }
  
  // 检查文件类型
  const fileExtension = file.name.split('.').pop().toLowerCase();
  if (!['pdf', 'docx'].includes(fileExtension)) {
    ElMessage.error('只能上传PDF或DOCX格式的文件');
    resumeFileList.value = fileList.filter(f => f.uid !== file.uid);
    return;
  }
  
  resumeFileList.value = fileList;
  
  // 上传文件到服务器
  const formData = new FormData();
  formData.append('file', file.raw);
  
  try {
    ElMessage.info('正在上传简历...');
    const res = await uploadResume(formData);
    console.log('简历上传响应:', res);
    
    // 修改这部分逻辑，优化响应处理
    // 直接检查返回的数据是否包含必要字段，不再强制要求error字段
    if (res) {
      // 处理两种可能的响应格式：
      // 1. {error: 0, body: {...}, message: ''}
      // 2. 直接返回文件数据对象
      const fileData = res.body || res;
      
      // 确保返回的数据符合预期结构 - 检查必要字段
      if (fileData.id !== undefined && fileData.filePath) {
        form.resumeFiles = form.resumeFiles || [];
        form.resumeFiles.push({
          fileId: fileData.id,
          fileName: fileData.fileName || file.name,
          fileUrl: fileData.filePath,
          uploadedAt: fileData.uploadedAt || new Date().toISOString()
        });
        ElMessage.success('简历上传成功');
        return;
      }
    }
    
    // 如果上面的条件都不满足，则抛出错误
    throw new Error('简历上传失败，服务器返回格式与预期不符');
    
  } catch (error) {
    console.error('简历上传失败:', error);
    ElMessage.error(`简历上传失败: ${error.message || '请稍后重试'}`);
    resumeFileList.value = resumeFileList.value.filter(f => f.uid !== file.uid);
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
onMounted(async () => {
  console.log('Profile页面加载')
  
  // 获取学生档案信息
  await getProfile()
  
  // 加载项目和实习经历数据
  await loadProjectExperiences()
  
  // 加载教育经历数据
  await loadEducationExperiences()
  
  // 加载技能标签
  await loadSkillTags()
  
  // 加载学生已添加的技能标签
  await loadStudentSkills()
  
  loading.value = false
})

// 添加加载项目和实习经历的方法
const loadProjectExperiences = async () => {
  try {
    console.log('开始获取项目/实习经历数据...')
    const response = await getProjectExperiences()
    
    // 输出原始响应数据
    console.log('项目/实习经历原始响应:', response)
    
    // 提取项目和实习经历数据
    if (response && response.projectExperiences) {
      // 清空现有数据
      form.projectExperience = []
      form.internshipExperience = []
      
      // 分类处理数据
      const projectData = []
      const internshipData = []
      
      // 遍历所有数据并进行分类
      for (const exp of response.projectExperiences) {
        if (exp.type === 'project') {
          // 项目经历
          projectData.push({
            projectName: exp.projectName || '',
            role: exp.role || '',
            timeRange: Array.isArray(exp.timeRange) ? exp.timeRange : [],
            description: exp.description || ''
          })
        } else if (exp.type === 'internship') {
          // 实习经历
          internshipData.push({
            companyName: exp.companyName || '',
            position: exp.position || '',
            timeRange: Array.isArray(exp.timeRange) ? exp.timeRange : [],
            description: exp.description || ''
          })
        }
      }
      
      // 更新表单数据
      form.projectExperience = projectData.length > 0 ? projectData : []
      form.internshipExperience = internshipData.length > 0 ? internshipData : []
      
      // 如果项目经历为空，添加一个空白项
      if (form.projectExperience.length === 0 && projectData.length === 0) {
        addProject()
      }
      
      // 如果实习经历为空，添加一个空白项
      if (form.internshipExperience.length === 0 && internshipData.length === 0) {
        addInternship()
      }
      
      console.log('成功加载项目经历数据:', form.projectExperience.length, '条')
      console.log('成功加载实习经历数据:', form.internshipExperience.length, '条')
    } else {
      console.log('没有获取到项目/实习经历数据或数据格式不正确')
      // 添加默认空白项
      if (form.projectExperience.length === 0) {
        addProject()
      }
      if (form.internshipExperience.length === 0) {
        addInternship()
      }
    }
  } catch (error) {
    console.error('获取项目/实习经历数据失败:', error)
    ElMessage.warning('获取项目/实习经历数据失败，将使用空白数据')
    
    // 添加默认空白项
    if (form.projectExperience.length === 0) {
      addProject()
    }
    if (form.internshipExperience.length === 0) {
      addInternship()
    }
  }
}

// 测试API调用
const testProfileAPI = async () => {
  try {
    ElMessage.info('正在测试API，请查看控制台输出');
    
    // 测试直接获取数据
    console.log('1. 测试获取学生档案数据');
    const getResponse = await getStudentProfile();
    console.log('获取档案响应:', getResponse);
    
    // 构造一个最小的更新对象测试
    const minimalUpdate = {
      id: form.id,
      selfIntroduction: '这是一个测试更新 - ' + new Date().toISOString()
    };
    
    console.log('2. 测试最小更新:', minimalUpdate);
    const updateResponse = await updateStudentProfile(minimalUpdate);
    console.log('最小更新响应:', updateResponse);
    
    // 更新成功后重新获取
    console.log('3. 重新获取数据检查更新结果');
    const refreshResponse = await getStudentProfile();
    console.log('刷新数据响应:', refreshResponse);
    
    ElMessage.success('API测试完成，请查看控制台输出');
  } catch (error) {
    console.error('API测试失败:', error);
    ElMessage.error(`API测试失败: ${error.message || '未知错误'}`);
  }
};

// 加载教育经历数据
const loadEducationExperiences = async () => {
  try {
    console.log('开始加载教育经历数据...')
    const response = await getEducationExperiences()
    
    if (response && response.educationExperiences && Array.isArray(response.educationExperiences)) {
      console.log('获取到教育经历数据:', response.educationExperiences)
      
      // 清空现有数据
      form.educationExperience = []
      
      // 将API返回的数据填充到表单中
      if (response.educationExperiences.length > 0) {
        form.educationExperience = response.educationExperiences.map(edu => ({
          schoolName: edu.schoolName || '',
          major: edu.major || '',
          degree: edu.degree || '',
          timeRange: edu.timeRange || [],
          description: edu.description || ''
        }))
      } else {
        // 如果没有数据，添加一个空白项
        addEducation()
      }
    } else {
      console.log('没有获取到教育经历数据或数据格式不正确')
      // 添加默认空白项
      if (form.educationExperience.length === 0) {
        addEducation()
      }
    }
  } catch (error) {
    console.error('获取教育经历数据失败:', error)
    ElMessage.warning('获取教育经历数据失败，将使用空白数据')
    
    // 添加默认空白项
    if (form.educationExperience.length === 0) {
      addEducation()
    }
  }
}

// 加载技能标签
const loadSkillTags = async () => {
  try {
    console.log('开始获取技能标签列表...');
    const response = await getSkillTags();
    
    console.log('技能标签API响应:', response);
    
    if (response && Array.isArray(response)) {
      // 直接使用返回的标签列表
      skillOptions.value = response;
      console.log('加载到技能标签数量:', response.length);
    } else if (response && response.length > 0) {
      // 可能是特殊格式，尝试转换
      skillOptions.value = response.map(tag => {
        if (typeof tag === 'string') {
          return { value: tag, label: tag };
        } else if (typeof tag === 'object') {
          return { 
            value: tag.value || tag.name || '', 
            label: tag.label || tag.name || ''
          };
        }
        return null;
      }).filter(Boolean);
      
      console.log('处理后的技能标签:', skillOptions.value);
    } else {
      console.warn('未获取到技能标签或格式不正确');
      // 保留默认的技能标签
    }
  } catch (error) {
    console.error('获取技能标签失败:', error);
    ElMessage.warning('获取技能标签失败，使用默认选项');
  }
};

// 加载学生已添加的技能标签
const loadStudentSkills = async () => {
  try {
    console.log('开始获取学生已添加的技能标签...');
    const response = await getStudentSkills();
    
    console.log('学生已添加的技能标签:', response);
    
    if (response && Array.isArray(response)) {
      // 直接使用返回的标签列表
      form.skills = response;
      console.log('加载到学生已添加的技能标签数量:', response.length);
    } else if (response && response.length > 0) {
      // 可能是特殊格式，尝试转换
      form.skills = response.map(tag => {
        if (typeof tag === 'string') {
          return { value: tag, label: tag };
        } else if (typeof tag === 'object') {
          return { 
            value: tag.value || tag.name || '', 
            label: tag.label || tag.name || ''
          };
        }
        return null;
      }).filter(Boolean);
      
      console.log('处理后的学生已添加的技能标签:', form.skills);
    } else {
      console.warn('未获取到学生已添加的技能标签或格式不正确');
      // 保留默认的技能标签
    }
  } catch (error) {
    console.error('获取学生已添加的技能标签失败:', error);
    ElMessage.warning('获取学生已添加的技能标签失败，使用默认选项');
  }
};
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