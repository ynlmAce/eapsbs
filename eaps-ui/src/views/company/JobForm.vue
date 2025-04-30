<template>
  <div class="job-form-container">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑岗位' : '发布新岗位' }}</h2>
      <el-button @click="goBack">返回岗位列表</el-button>
    </div>
    
    <el-card class="job-form-card">
      <el-form 
        ref="jobFormRef" 
        :model="jobForm" 
        :rules="rules" 
        label-width="120px"
        label-position="left"
      >
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="岗位名称" prop="title">
          <el-input v-model="jobForm.title" placeholder="请输入岗位名称" />
        </el-form-item>
        
        <el-form-item label="工作地点" prop="location">
          <el-input v-model="jobForm.location" placeholder="请输入工作地点" />
        </el-form-item>
        
        <el-form-item label="薪资范围" prop="salary">
          <el-input v-model="jobForm.salary" placeholder="请输入薪资范围，如：5000-8000元/月" />
        </el-form-item>
        
        <el-form-item label="学历要求" prop="education">
          <el-select v-model="jobForm.education" placeholder="请选择学历要求">
            <el-option label="不限" value="不限" />
            <el-option label="大专" value="大专" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
            <el-option label="博士" value="博士" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="经验要求" prop="experience">
          <el-select v-model="jobForm.experience" placeholder="请选择经验要求">
            <el-option label="不限" value="不限" />
            <el-option label="应届毕业生" value="应届毕业生" />
            <el-option label="1年以下" value="1年以下" />
            <el-option label="1-3年" value="1-3年" />
            <el-option label="3-5年" value="3-5年" />
            <el-option label="5年以上" value="5年以上" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="工作类型" prop="jobType">
          <el-select v-model="jobForm.jobType" placeholder="请选择工作类型">
            <el-option label="全职" value="全职" />
            <el-option label="兼职" value="兼职" />
            <el-option label="实习" value="实习" />
            <el-option label="校园招聘" value="校园招聘" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="招聘人数" prop="headcount">
          <el-input-number v-model="jobForm.headcount" :min="1" :max="999" />
        </el-form-item>
        
        <el-form-item label="工作时间" prop="workTime">
          <el-input v-model="jobForm.workTime" placeholder="请输入工作时间，如：周一至周五，9:00-18:00" />
        </el-form-item>
        
        <el-divider content-position="left">岗位描述</el-divider>
        
        <el-form-item label="岗位描述" prop="description">
          <el-input 
            v-model="jobForm.description" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入岗位描述，包括岗位职责等"
          />
        </el-form-item>
        
        <el-form-item label="岗位要求" prop="requirement">
          <el-input 
            v-model="jobForm.requirement" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入岗位要求，包括技能要求等"
          />
        </el-form-item>
        
        <el-divider content-position="left">标签与分类</el-divider>
        
        <el-form-item label="岗位标签" prop="jobTags">
          <el-select
            v-model="jobForm.jobTags"
            multiple
            filterable
            placeholder="请选择岗位标签"
          >
            <el-option
              v-for="tag in jobTagOptions"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
              :style="tag.name === '急招' ? 'background:#ff5252;color:#fff;font-weight:bold;' : ''"
            >
              <span :style="tag.name === '急招' ? 'color:#ff5252;font-weight:bold;' : ''">{{ tag.name }}</span>
            </el-option>
          </el-select>
          <div style="margin-top:6px;color:#ff5252;font-size:13px;">
            如岗位急需招聘，请务必勾选"急招"标签，平台将优先推荐！
          </div>
        </el-form-item>
        
        <el-form-item label="福利标签" prop="welfareTags">
          <el-select
            v-model="jobForm.welfareTags"
            multiple
            filterable
            placeholder="请选择福利标签"
          >
            <el-option-group
              v-for="group in welfareTagGroups"
              :key="group.category"
              :label="group.category"
            >
              <el-option
                v-for="tag in group.tags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-option-group>
          </el-select>
        </el-form-item>
        
        <el-divider content-position="left">其它信息</el-divider>
        
        <el-form-item label="招聘有效期" prop="validUntil">
          <el-date-picker
            v-model="jobForm.validUntil"
            type="date"
            placeholder="选择招聘截止日期"
            :disabled-date="disabledDate"
            format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="jobForm.contactPerson" placeholder="请输入联系人姓名" />
        </el-form-item>
        
        <el-form-item label="联系方式" prop="contactMethod">
          <el-input v-model="jobForm.contactMethod" placeholder="请输入联系方式，如电话或邮箱" />
        </el-form-item>
        
        <el-form-item label="联系方式可见" prop="showContact">
          <el-switch v-model="jobForm.showContact" />
          <span class="hint">开启后，求职者可以看到联系方式</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">{{ isEdit ? '保存修改' : '发布岗位' }}</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getJobDetail, createJob, updateJob, getJobTags, getWelfareTags } from '@/api/job'
import { callApi } from '@/utils/apiUtils'

const route = useRoute()
const router = useRouter()
const jobFormRef = ref(null)
const isEdit = computed(() => route.params.id !== undefined)
const submitting = ref(false)
const jobTagOptions = ref([])
const welfareTagGroups = ref([])

// 表单数据
const jobForm = reactive({
  title: '',
  description: '',
  requirement: '',
  location: '',
  salary: '',
  education: '',
  experience: '',
  jobType: '',
  headcount: 1,
  workTime: '',
  jobTags: [],
  welfareTags: [],
  validUntil: '',
  contactPerson: '',
  contactMethod: '',
  showContact: false
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入岗位名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入工作地点', trigger: 'blur' }
  ],
  salary: [
    { required: true, message: '请输入薪资范围', trigger: 'blur' }
  ],
  education: [
    { required: true, message: '请选择学历要求', trigger: 'change' }
  ],
  experience: [
    { required: true, message: '请选择经验要求', trigger: 'change' }
  ],
  jobType: [
    { required: true, message: '请选择工作类型', trigger: 'change' }
  ],
  headcount: [
    { required: true, message: '请输入招聘人数', trigger: 'blur' }
  ],
  workTime: [
    { required: true, message: '请输入工作时间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入岗位描述', trigger: 'blur' },
    { min: 10, message: '岗位描述不能少于10个字符', trigger: 'blur' }
  ],
  requirement: [
    { required: true, message: '请输入岗位要求', trigger: 'blur' },
    { min: 10, message: '岗位要求不能少于10个字符', trigger: 'blur' }
  ],
  validUntil: [
    { required: true, message: '请选择招聘有效期', trigger: 'change' }
  ]
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 不能选择今天之前的日期
}

// 返回岗位列表页
const goBack = () => {
  router.push('/company/jobs')
}

// 获取标签数据
const fetchTagData = async () => {
  try {
    // 获取岗位标签
    const jobTagsResult = await callApi(getJobTags())
    jobTagOptions.value = jobTagsResult || []
    
    // 获取福利标签
    const welfareTagsResult = await callApi(getWelfareTags())
    
    // 按分类组织福利标签
    const groupedTags = {}
    welfareTagsResult.forEach(tag => {
      const category = tag.category || '其他'
      if (!groupedTags[category]) {
        groupedTags[category] = []
      }
      groupedTags[category].push(tag)
    })
    
    // 转换为组件所需格式
    welfareTagGroups.value = Object.keys(groupedTags).map(category => ({
      category,
      tags: groupedTags[category]
    }))
  } catch (error) {
    console.error('获取标签数据失败:', error)
  }
}

// 获取岗位详情
const fetchJobDetail = async (jobId) => {
  try {
    const jobDetail = await callApi(getJobDetail(jobId))
    if (jobDetail) {
      // 将日期字符串转换为Date对象
      if (jobDetail.validUntil) {
        jobDetail.validUntil = new Date(jobDetail.validUntil)
      }
      // 修正：确保标签为ID数组（兼容后端返回字符串、对象、ID）
      if (Array.isArray(jobDetail.jobTags)) {
        jobForm.jobTags = jobDetail.jobTags.map(tag =>
          typeof tag === 'object'
            ? tag.id
            : ((jobTagOptions.value.find(opt => opt.name === tag) || {}).id || tag)
        )
      }
      if (Array.isArray(jobDetail.welfareTags)) {
        const allWelfareTags = welfareTagGroups.value.flatMap(group => group.tags)
        jobForm.welfareTags = jobDetail.welfareTags.map(tag =>
          typeof tag === 'object'
            ? tag.id
            : ((allWelfareTags.find(opt => opt.name === tag) || {}).id || tag)
        )
      }
      // 填充其它表单数据
      Object.keys(jobForm).forEach(key => {
        if (jobDetail[key] !== undefined && key !== 'jobTags' && key !== 'welfareTags') {
          jobForm[key] = jobDetail[key]
        }
      })
      // 强制补全companyId
      if (jobDetail.companyId) {
        jobForm.companyId = jobDetail.companyId
      }
    }
  } catch (error) {
    console.error('获取岗位详情失败:', error)
    ElMessage.error('获取岗位详情失败，请重试')
  }
}

// 提交表单
const submitForm = async () => {
  if (!jobFormRef.value) return
  
  await jobFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请正确填写所有必填项')
      return
    }
    
    // 提交前处理标签为ID数组
    if (Array.isArray(jobForm.jobTags)) {
      jobForm.jobTags = jobForm.jobTags.map(tag => typeof tag === 'object' ? tag.id : tag);
    }
    if (Array.isArray(jobForm.welfareTags)) {
      jobForm.welfareTags = jobForm.welfareTags.map(tag => typeof tag === 'object' ? tag.id : tag);
    }
    
    // 格式化日期为字符串
    const formData = { ...jobForm }
    if (formData.validUntil instanceof Date) {
      formData.validUntil = formData.validUntil.toISOString().split('T')[0]
    }
    
    submitting.value = true
    try {
      if (isEdit.value) {
        formData.id = parseInt(route.params.id)
        // 打印调试
        console.log('提交岗位更新数据:', formData)
        // 自动补全companyId（如后端需要）
        if (!formData.companyId && jobForm.companyId) {
          formData.companyId = jobForm.companyId
        }
        await callApi(updateJob(formData), {
          showSuccess: true,
          successMsg: '岗位更新成功'
        })
      } else {
        // 创建岗位
        await callApi(createJob(formData), {
          showSuccess: true,
          successMsg: '岗位创建成功，等待审核'
        })
      }
      
      // 成功后返回列表页
      router.push('/company/jobs')
    } catch (error) {
      console.error('提交岗位表单失败:', error)
    } finally {
      submitting.value = false
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
    if (jobFormRef.value) {
      jobFormRef.value.resetFields()
      
      // 如果是编辑模式，重新获取初始数据
      if (isEdit.value) {
        fetchJobDetail(route.params.id)
      }
    }
  }).catch(() => {
    // 取消重置，不做任何操作
  })
}

// 组件挂载时初始化数据
onMounted(async () => {
  // 获取标签数据
  await fetchTagData()
  
  // 如果是编辑模式，获取岗位详情
  if (isEdit.value) {
    await fetchJobDetail(route.params.id)
  }
})
</script>

<style scoped>
.job-form-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.job-form-card {
  margin-bottom: 30px;
}

.el-divider {
  margin: 24px 0;
}

.el-divider__text {
  font-weight: bold;
  color: #409EFF;
}

.hint {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style> 