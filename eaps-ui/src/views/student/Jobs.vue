<template>
  <div class="jobs-container">
    <div class="filter-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="职位名称、公司、描述" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="地点">
          <el-select 
            v-model="searchForm.location" 
            placeholder="工作地点" 
            clearable
          >
            <el-option 
              v-for="item in locationOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职位类型">
          <el-select 
            v-model="searchForm.jobType" 
            placeholder="职位类型" 
            clearable
          >
            <el-option label="全职" value="FULL_TIME" />
            <el-option label="兼职" value="PART_TIME" />
            <el-option label="实习" value="INTERNSHIP" />
          </el-select>
        </el-form-item>
        <el-form-item label="薪资范围">
          <el-select 
            v-model="searchForm.salaryRange" 
            placeholder="薪资范围" 
            clearable
          >
            <el-option label="面议" value="0-0" />
            <el-option label="3k以下" value="0-3000" />
            <el-option label="3k-5k" value="3000-5000" />
            <el-option label="5k-10k" value="5000-10000" />
            <el-option label="10k-15k" value="10000-15000" />
            <el-option label="15k-20k" value="15000-20000" />
            <el-option label="20k-30k" value="20000-30000" />
            <el-option label="30k以上" value="30000-999999" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="loading" class="loading-section">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="jobList.length === 0" class="no-data">
      <el-empty description="暂无职位信息" />
      <div v-if="loadRetries > 0" class="retry-section">
        <span>数据加载异常，已重试 {{ loadRetries }} 次</span>
        <el-button type="primary" size="small" @click="retryLoadData">重新加载</el-button>
      </div>
    </div>

    <div v-else class="job-list">
      <div 
        v-for="job in jobList" 
        :key="job.id" 
        class="job-card"
        @click="openJobDetail(job.id)"
      >
        <div class="job-header">
          <h3 class="job-title">{{ job.title }}</h3>
          <div class="job-salary">{{ job.salaryRange || job.salary }}</div>
        </div>
        <div class="company-info">
          <span class="company-name">{{ job.companyName }}</span>
          <span class="location">{{ job.location }}</span>
        </div>
        <div class="job-tags">
          <el-tag v-if="job.jobType || job.type" size="small" type="info">{{ job.jobType || job.type }}</el-tag>
          <el-tag 
            v-for="(tag, index) in (job.jobTags || job.tags)" 
            :key="index" 
            size="small" 
            type="success"
          >
            {{ typeof tag === 'string' ? tag : tag.name }}
          </el-tag>
        </div>
        <div class="job-footer">
          <span class="publish-time">{{ formatTime(job.publishTime || job.publishedAt) }}</span>
        </div>
      </div>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      v-model="jobDetailVisible"
      title="职位详情"
      width="60%"
      destroy-on-close
    >
      <div v-if="selectedJob" class="job-detail">
        <div class="job-header">
          <h2>{{ selectedJob.title }}</h2>
          <div class="job-salary">{{ selectedJob.salaryRange }}</div>
        </div>

        <div class="company-info">
          <div class="company-name">{{ selectedJob.companyName }}</div>
          <div class="job-meta">
            <span>{{ selectedJob.location }}</span>
            <span>{{ selectedJob.experience }}</span>
            <span>{{ selectedJob.education }}</span>
            <span>{{ selectedJob.jobType }}</span>
          </div>
        </div>

        <el-divider />

        <div class="job-section">
          <h3>职位描述</h3>
          <div class="section-content" v-html="selectedJob.description"></div>
        </div>

        <div class="job-section" v-if="selectedJob.requirement">
          <h3>职位要求</h3>
          <div class="section-content" v-html="selectedJob.requirement"></div>
        </div>

        <div class="job-section" v-if="selectedJob.workTime">
          <h3>工作时间</h3>
          <div class="section-content">{{ selectedJob.workTime }}</div>
        </div>

        <div class="job-section" v-if="selectedJob.welfares && selectedJob.welfares.length > 0">
          <h3>福利待遇</h3>
          <div class="welfare-tags">
            <el-tag 
              v-for="(welfare, index) in selectedJob.welfares" 
              :key="index"
              class="welfare-tag" 
              effect="plain"
            >
              {{ welfare }}
            </el-tag>
          </div>
        </div>

        <div class="job-section" v-if="selectedJob.jobTags && selectedJob.jobTags.length > 0">
          <h3>岗位标签</h3>
          <div class="welfare-tags">
            <el-tag 
              v-for="(tag, index) in selectedJob.jobTags" 
              :key="index"
              class="welfare-tag" 
              type="success"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>

        <div class="job-section" v-if="selectedJob.contactPerson || (selectedJob.contactInfo && (selectedJob.contactInfo.email || selectedJob.contactInfo.phone))">
          <h3>联系方式</h3>
          <div class="contact-info">
            <p v-if="selectedJob.contactPerson">联系人：{{ selectedJob.contactPerson }}</p>
            <p v-if="selectedJob.contactInfo && selectedJob.contactInfo.email">邮箱：{{ selectedJob.contactInfo.email }}</p>
            <p v-if="selectedJob.contactInfo && selectedJob.contactInfo.phone">电话：{{ selectedJob.contactInfo.phone }}</p>
            <p v-if="selectedJob.contactMethod && !selectedJob.contactInfo">联系方式：{{ selectedJob.contactMethod }}</p>
          </div>
        </div>

        <div class="job-section" v-if="selectedJob.headcount">
          <h3>招聘人数</h3>
          <div class="section-content">{{ selectedJob.headcount }}人</div>
        </div>

        <div class="job-footer">
          <span>发布时间：{{ formatTime(selectedJob.publishTime || selectedJob.publishedAt) }}</span>
          <span>截止时间：{{ selectedJob.validUntil ? selectedJob.validUntil.split('T')[0] : '未设置' }}</span>
        </div>

        <div class="dialog-footer">
          <el-button type="primary" @click="handleApplyJob">申请职位</el-button>
          <el-button @click="contactEmployer">联系HR</el-button>
          <el-button @click="jobDetailVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getJobList, getJobDetail, applyForJob } from '@/api/job'
import { createChatSession } from '@/api/chat'
import router from '@/router'

const loading = ref(false)
const jobList = ref([])
const selectedJob = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const jobDetailVisible = ref(false)
const searchForm = reactive({
  keyword: '',
  location: '',
  jobType: '',
  salaryRange: ''
})
const isInitialized = ref(false)
const loadRetries = ref(0)
const maxRetries = 3
let requestTimer = null
let loadingTimer = null

const locationOptions = ref([
  { label: '北京', value: '北京' },
  { label: '上海', value: '上海' },
  { label: '广州', value: '广州' },
  { label: '深圳', value: '深圳' },
  { label: '杭州', value: '杭州' },
  { label: '南京', value: '南京' },
  { label: '武汉', value: '武汉' },
  { label: '成都', value: '成都' },
  { label: '西安', value: '西安' },
  { label: '重庆', value: '重庆' }
])

const loadJobList = async (isRetry = false) => {
  if (requestTimer) {
    clearTimeout(requestTimer)
  }

  requestTimer = setTimeout(async () => {
    try {
      if (!isRetry) {
        loading.value = true

        loadingTimer = setTimeout(() => {
          if (loading.value) {
            console.log('加载状态持续中...')
          }
        }, 500)
      }

      let salaryMin = null
      let salaryMax = null
      if (searchForm.salaryRange) {
        const [min, max] = searchForm.salaryRange.split('-')
        salaryMin = min !== '' ? Number(min) : null
        salaryMax = max !== '' ? Number(max) : null
      }

      const timestamp = new Date().getTime()
      
      const params = {
        page: currentPage.value,
        limit: pageSize.value,
        keyword: searchForm.keyword || null,
        location: searchForm.location || null,
        jobType: searchForm.jobType || null,
        salaryMin,
        salaryMax,
        _t: timestamp  // 添加时间戳防止缓存
      }

      console.log('请求参数:', params)
      const res = await getJobList(params)
      console.log('职位列表响应:', res)

      if (res && typeof res === 'object') {
        if (res.list && Array.isArray(res.list)) {
          jobList.value = res.list
          total.value = res.total || 0
          isInitialized.value = true
          loadRetries.value = 0
        } else if (!isRetry && !isInitialized.value && loadRetries.value < maxRetries) {
          loadRetries.value++
          console.log(`首次加载数据为空，进行第 ${loadRetries.value} 次重试...`)
          setTimeout(() => loadJobList(true), 1000)
        }
      }
    } catch (error) {
      console.error('加载职位列表失败:', error)
      if (!isRetry && !isInitialized.value && loadRetries.value < maxRetries) {
        loadRetries.value++
        console.log(`加载失败，进行第 ${loadRetries.value} 次重试...`)
        setTimeout(() => loadJobList(true), 1000)
      }
    } finally {
      if (loadingTimer) {
        clearTimeout(loadingTimer)
      }
      loading.value = false
    }
  }, isRetry ? 0 : 200)
}

const retryLoadData = () => {
  loadRetries.value = 0
  loadJobList()
}

const openJobDetail = async (jobId) => {
  try {
    loading.value = true
    const res = await getJobDetail(jobId)
    selectedJob.value = res
    console.log('职位详情数据结构:', JSON.stringify(res, null, 2))
    jobDetailVisible.value = true
  } catch (error) {
    console.error('获取职位详情失败:', error)
    ElMessage.error('获取职位详情失败')
  } finally {
    loading.value = false
  }
}

const handleApplyJob = async () => {
  if (!selectedJob.value) return
  
  try {
    await ElMessageBox.confirm('确定要申请该职位吗？', '申请确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await applyForJob(selectedJob.value.id, null, '')
    ElMessage.success('申请成功')
    jobDetailVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('申请职位失败:', error)
      ElMessage.error('申请失败: ' + (error.message || '未知错误'))
    }
  }
}

const contactEmployer = async () => {
  if (!selectedJob.value) return
  
  try {
    const res = await createChatSession({
      targetType: 'COMPANY',
      targetId: selectedJob.value.companyId,
      jobId: selectedJob.value.id
    })
    
    router.push({
      name: 'Chat',
      params: { id: res.id }
    })
  } catch (error) {
    console.error('创建聊天会话失败:', error)
    ElMessage.error('创建聊天会话失败')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadJobList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  loadJobList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadJobList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadJobList()
}

const formatJobType = (type) => {
  const types = {
    'FULL_TIME': '全职',
    'PART_TIME': '兼职',
    'INTERNSHIP': '实习'
  }
  return types[type] || type
}

const formatExperience = (experience) => {
  const experiences = {
    'NO_EXPERIENCE': '无经验要求',
    'ENTRY_LEVEL': '1年以下',
    'JUNIOR': '1-3年',
    'MID_LEVEL': '3-5年',
    'SENIOR': '5-10年',
    'EXPERT': '10年以上'
  }
  return experiences[experience] || experience
}

const formatEducation = (education) => {
  const educations = {
    'HIGH_SCHOOL': '高中',
    'JUNIOR_COLLEGE': '大专',
    'BACHELOR': '本科',
    'MASTER': '硕士',
    'DOCTORATE': '博士',
    'NO_REQUIREMENT': '无要求'
  }
  return educations[education] || education
}

const formatSalary = (min, max) => {
  if (!min && !max) return '薪资面议'
  if (min === 0 && max === 0) return '薪资面议'
  
  const formatNumber = (num) => {
    if (num >= 10000) {
      return (num / 10000).toFixed(0) + 'k'
    }
    return (num / 1000).toFixed(0) + 'k'
  }
  
  if (min && max) {
    return `${formatNumber(min)}-${formatNumber(max)}`
  } else if (min) {
    return `${formatNumber(min)}以上`
  } else if (max) {
    return `${formatNumber(max)}以下`
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return '未知'
  
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000))
    return `${minutes}分钟前`
  }
  
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    return `${hours}小时前`
  }
  
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = ['日', '一', '二', '三', '四', '五', '六']
    return `星期${days[date.getDay()]}`
  }
  
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  console.log('Jobs组件挂载，开始加载数据...')
  loadJobList()
})

onUnmounted(() => {
  if (requestTimer) {
    clearTimeout(requestTimer)
  }
  if (loadingTimer) {
    clearTimeout(loadingTimer)
  }
})
</script>

<style scoped>
.jobs-container {
  padding: 20px;
}

.filter-section {
  background: #fff;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.loading-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  min-height: 400px;
}

.no-data {
  background: #fff;
  padding: 50px;
  border-radius: 4px;
  text-align: center;
}

.retry-section {
  margin-top: 20px;
}

.job-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.job-card {
  background: #fff;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.job-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.job-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0;
  color: #303133;
}

.job-salary {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
}

.company-info {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  color: #606266;
  font-size: 14px;
}

.job-tags {
  margin-bottom: 15px;
}

.job-tags .el-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.job-footer {
  color: #909399;
  font-size: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.job-detail .job-header {
  margin-bottom: 15px;
}

.job-detail .job-header h2 {
  margin: 0 0 10px 0;
  font-size: 22px;
}

.job-detail .company-info {
  margin-bottom: 20px;
}

.job-detail .company-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.job-detail .job-meta {
  color: #606266;
  font-size: 14px;
}

.job-detail .job-meta span {
  margin-right: 15px;
}

.job-section {
  margin-bottom: 20px;
}

.job-section h3 {
  font-size: 16px;
  margin-bottom: 10px;
  font-weight: bold;
}

.section-content {
  line-height: 1.6;
  white-space: pre-wrap;
}

.welfare-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.welfare-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.contact-info {
  line-height: 1.6;
}

.job-footer {
  margin-top: 20px;
  color: #909399;
  font-size: 13px;
  display: flex;
  justify-content: space-between;
}

.dialog-footer {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 15px;
}
</style>