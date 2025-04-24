<template>
  <div class="jobs-container">
    <div class="filter-container">
      <el-card class="filter-card">
        <el-form :model="filterForm" ref="filterFormRef" label-width="80px" inline>
          <el-form-item label="关键词">
            <el-input v-model="filterForm.keyword" placeholder="职位名称/公司名称" clearable @clear="handleSearch"></el-input>
          </el-form-item>
          
          <el-form-item label="工作地点">
            <el-select v-model="filterForm.location" placeholder="选择工作地点" clearable @clear="handleSearch">
              <el-option v-for="item in locationOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="工作类型">
            <el-select v-model="filterForm.jobType" placeholder="选择工作类型" clearable @clear="handleSearch">
              <el-option label="全职" value="全职"></el-option>
              <el-option label="兼职" value="兼职"></el-option>
              <el-option label="实习" value="实习"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="薪资范围">
            <el-select v-model="filterForm.salaryRange" placeholder="选择薪资范围" clearable @clear="handleSearch">
              <el-option v-for="item in salaryOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    
    <div class="main-content">
      <div class="job-list">
        <div class="job-list-header">
          <h2>岗位列表</h2>
          <div class="sort-options">
            <el-radio-group v-model="sortBy" size="small" @change="handleSort">
              <el-radio-button label="latest">最新发布</el-radio-button>
              <el-radio-button label="salary">薪资最高</el-radio-button>
              <el-radio-button label="hot">最热门</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>
        
        <div v-else-if="jobs.length === 0" class="empty-container">
          <el-empty description="暂无符合条件的岗位信息"></el-empty>
        </div>
        
        <div v-else class="job-list-content">
          <el-card v-for="job in jobs" :key="job.id" class="job-card" shadow="hover" @click="viewJobDetail(job)">
            <div class="job-card-header">
              <h3 class="job-title">{{ job.title }}</h3>
              <div class="job-salary">{{ job.salary }}</div>
            </div>
            
            <div class="job-card-company">
              <el-avatar :size="40" :src="job.companyLogo" fit="cover">
                {{ job.companyName.substring(0, 1) }}
              </el-avatar>
              <div class="company-info">
                <div class="company-name">
                  {{ job.companyName }}
                  <el-tag size="small" type="success" v-if="job.isAuthenticated">已认证</el-tag>
                </div>
                <div class="company-meta">
                  <span>{{ job.industry }}</span>
                  <span class="separator">|</span>
                  <span>{{ job.size }}</span>
                </div>
              </div>
            </div>
            
            <div class="job-card-meta">
              <div class="meta-item">
                <el-icon><Location /></el-icon>
                <span>{{ job.location }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Suitcase /></el-icon>
                <span>{{ job.jobType }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Collection /></el-icon>
                <span>{{ job.education }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Timer /></el-icon>
                <span>{{ job.experience }}</span>
              </div>
            </div>
            
            <div class="job-card-tags">
              <el-tag v-for="tag in job.tags" :key="tag" size="small" class="job-tag">{{ tag }}</el-tag>
            </div>
            
            <div class="job-card-footer">
              <span class="publish-time">发布时间: {{ formatTime(job.publishTime) }}</span>
            </div>
          </el-card>
          
          <div class="pagination-container">
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
        </div>
      </div>
    </div>
    
    <!-- 岗位详情对话框 -->
    <el-dialog v-model="jobDetailVisible" title="岗位详情" width="60%" destroy-on-close>
      <div v-if="currentJob" class="job-detail">
        <div class="job-detail-header">
          <h2>{{ currentJob.title }}</h2>
          <div class="job-salary">{{ currentJob.salary }}</div>
        </div>
        
        <div class="job-detail-company">
          <el-avatar :size="60" :src="currentJob.companyLogo" fit="cover">
            {{ currentJob.companyName.substring(0, 1) }}
          </el-avatar>
          <div class="company-info">
            <div class="company-name">
              {{ currentJob.companyName }}
              <el-tag size="small" type="success" v-if="currentJob.isAuthenticated">已认证</el-tag>
            </div>
            <div class="company-meta">
              <span>{{ currentJob.industry }}</span>
              <span class="separator">|</span>
              <span>{{ currentJob.size }}</span>
              <span class="separator">|</span>
              <span v-if="currentJob.companyRating">评分: {{ currentJob.companyRating }}</span>
            </div>
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="job-detail-meta">
          <div class="meta-row">
            <div class="meta-item">
              <span class="meta-label">工作地点:</span>
              <span class="meta-value">{{ currentJob.location }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">工作类型:</span>
              <span class="meta-value">{{ currentJob.jobType }}</span>
            </div>
          </div>
          <div class="meta-row">
            <div class="meta-item">
              <span class="meta-label">学历要求:</span>
              <span class="meta-value">{{ currentJob.education }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">经验要求:</span>
              <span class="meta-value">{{ currentJob.experience }}</span>
            </div>
          </div>
          <div class="meta-row">
            <div class="meta-item">
              <span class="meta-label">招聘人数:</span>
              <span class="meta-value">{{ currentJob.recruitCount }}人</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">发布时间:</span>
              <span class="meta-value">{{ formatTime(currentJob.publishTime) }}</span>
            </div>
          </div>
        </div>
        
        <div class="job-tags">
          <el-tag v-for="tag in currentJob.tags" :key="tag" size="small" class="job-tag">{{ tag }}</el-tag>
        </div>
        
        <el-divider></el-divider>
        
        <div class="job-detail-content">
          <h3>职位描述</h3>
          <div class="job-description" v-html="formatJobDescription(currentJob.description)"></div>
          
          <h3>职位要求</h3>
          <div class="job-requirements" v-html="formatJobDescription(currentJob.requirements)"></div>
          
          <h3 v-if="currentJob.benefits && currentJob.benefits.length > 0">福利待遇</h3>
          <div v-if="currentJob.benefits && currentJob.benefits.length > 0" class="job-benefits">
            <el-tag v-for="benefit in currentJob.benefits" :key="benefit" class="benefit-tag">{{ benefit }}</el-tag>
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="job-contact">
          <h3>联系方式</h3>
          <div class="contact-info">
            <div><span class="contact-label">联系人:</span> {{ currentJob.contactName }}</div>
            <div><span class="contact-label">联系电话:</span> {{ currentJob.contactPhone }}</div>
            <div><span class="contact-label">联系邮箱:</span> {{ currentJob.contactEmail }}</div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="jobDetailVisible = false">取消</el-button>
          <el-button type="primary" @click="handleApply" :disabled="hasApplied">{{ hasApplied ? '已投递' : '投递简历' }}</el-button>
          <el-button type="success" @click="handleContact">联系企业</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Suitcase, Collection, Timer } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 筛选表单
const filterForm = reactive({
  keyword: '',
  location: '',
  jobType: '',
  salaryRange: ''
})

// 筛选选项
const locationOptions = ref(['北京', '上海', '广州', '深圳', '杭州', '南京', '武汉', '成都', '西安'])
const salaryOptions = [
  { label: '3k以下', value: '3k以下' },
  { label: '3k-5k', value: '3k-5k' },
  { label: '5k-7k', value: '5k-7k' },
  { label: '7k-10k', value: '7k-10k' },
  { label: '10k-15k', value: '10k-15k' },
  { label: '15k-20k', value: '15k-20k' },
  { label: '20k-30k', value: '20k-30k' },
  { label: '30k以上', value: '30k以上' }
]

// 岗位数据
const loading = ref(false)
const jobs = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const sortBy = ref('latest')

// 岗位详情
const jobDetailVisible = ref(false)
const currentJob = ref(null)
const hasApplied = ref(false)

// 格式化发布时间
const formatTime = (time) => {
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 不到1小时，显示多少分钟前
  if (diff < 60 * 60 * 1000) {
    return Math.floor(diff / (60 * 1000)) + '分钟前'
  }
  
  // 不到1天，显示多少小时前
  if (diff < 24 * 60 * 60 * 1000) {
    return Math.floor(diff / (60 * 60 * 1000)) + '小时前'
  }
  
  // 不到7天，显示多少天前
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    return Math.floor(diff / (24 * 60 * 60 * 1000)) + '天前'
  }
  
  // 其他情况显示具体日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化岗位描述（将换行符转换为HTML）
const formatJobDescription = (text) => {
  if (!text) return ''
  return text.replace(/\n/g, '<br>')
}

// 页面加载时获取数据
onMounted(() => {
  fetchJobs()
})

// 获取岗位列表
const fetchJobs = () => {
  loading.value = true
  
  // 模拟获取数据
  setTimeout(() => {
    const mockJobs = []
    
    // 生成随机岗位数据
    for (let i = 1; i <= 30; i++) {
      mockJobs.push({
        id: i,
        title: '软件工程师' + i,
        salary: ['3k-5k', '5k-7k', '7k-10k', '10k-15k', '15k-20k'][Math.floor(Math.random() * 5)],
        companyName: '示例科技有限公司' + i,
        companyLogo: '', // 实际项目中应有真实URL
        isAuthenticated: Math.random() > 0.3,
        industry: 'IT/互联网/通信',
        size: ['50-200人', '200-500人', '500-1000人'][Math.floor(Math.random() * 3)],
        location: locationOptions.value[Math.floor(Math.random() * locationOptions.value.length)],
        jobType: ['全职', '兼职', '实习'][Math.floor(Math.random() * 3)],
        education: ['大专', '本科', '硕士', '博士'][Math.floor(Math.random() * 4)],
        experience: ['经验不限', '1-3年', '3-5年', '5年以上'][Math.floor(Math.random() * 4)],
        tags: ['五险一金', '年终奖', '弹性工作', '定期体检'].slice(0, Math.floor(Math.random() * 4) + 1),
        publishTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000),
        
        // 详情页需要的其他信息
        recruitCount: Math.floor(Math.random() * 10) + 1,
        companyRating: (Math.random() * 3 + 2).toFixed(1),
        description: '岗位职责：\n1. 负责公司产品的设计和开发\n2. 优化产品性能和用户体验\n3. 编写高质量、可维护的代码\n4. 参与需求分析和技术方案设计',
        requirements: '任职要求：\n1. 计算机相关专业本科及以上学历\n2. 熟悉Java或Python编程语言\n3. 有良好的沟通能力和团队合作精神\n4. 能够独立思考，有较强的问题解决能力',
        benefits: ['五险一金', '年终奖', '弹性工作', '定期体检', '免费班车', '员工旅游'],
        contactName: '张经理',
        contactPhone: '13800138000',
        contactEmail: 'hr@example.com'
      })
    }
    
    // 根据筛选条件过滤
    let filteredJobs = [...mockJobs]
    
    if (filterForm.keyword) {
      const keyword = filterForm.keyword.toLowerCase()
      filteredJobs = filteredJobs.filter(job => 
        job.title.toLowerCase().includes(keyword) || 
        job.companyName.toLowerCase().includes(keyword)
      )
    }
    
    if (filterForm.location) {
      filteredJobs = filteredJobs.filter(job => job.location === filterForm.location)
    }
    
    if (filterForm.jobType) {
      filteredJobs = filteredJobs.filter(job => job.jobType === filterForm.jobType)
    }
    
    if (filterForm.salaryRange) {
      filteredJobs = filteredJobs.filter(job => job.salary === filterForm.salaryRange)
    }
    
    // 排序
    if (sortBy.value === 'latest') {
      filteredJobs.sort((a, b) => new Date(b.publishTime) - new Date(a.publishTime))
    } else if (sortBy.value === 'salary') {
      filteredJobs.sort((a, b) => {
        const getSalaryValue = (salaryStr) => {
          const match = salaryStr.match(/(\d+)k-(\d+)k/)
          return match ? parseInt(match[2]) : 0
        }
        return getSalaryValue(b.salary) - getSalaryValue(a.salary)
      })
    } else if (sortBy.value === 'hot') {
      // 根据一些热度指标排序，这里简单模拟
      filteredJobs.sort(() => Math.random() - 0.5)
    }
    
    total.value = filteredJobs.length
    
    // 分页
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    jobs.value = filteredJobs.slice(start, end)
    
    loading.value = false
  }, 1000)

  /**
   * TODO: 实际实现时从API获取岗位列表
   * const fetchJobList = async () => {
   *   try {
   *     loading.value = true
   *     const res = await api.job.list({
   *       keyword: filterForm.keyword,
   *       location: filterForm.location,
   *       jobType: filterForm.jobType,
   *       salaryRange: filterForm.salaryRange,
   *       sortBy: sortBy.value,
   *       page: currentPage.value,
   *       pageSize: pageSize.value
   *     })
   *     if (res.success) {
   *       jobs.value = res.data.list
   *       total.value = res.data.total
   *     }
   *   } catch (error) {
   *     console.error('获取岗位列表失败:', error)
   *     ElMessage.error('获取岗位列表失败，请刷新重试')
   *   } finally {
   *     loading.value = false
   *   }
   * }
   * fetchJobList()
   */
}

// 处理每页展示数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchJobs()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchJobs()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchJobs()
}

// 重置筛选条件
const resetFilter = () => {
  Object.keys(filterForm).forEach(key => {
    filterForm[key] = ''
  })
  currentPage.value = 1
  fetchJobs()
}

// 处理排序
const handleSort = () => {
  fetchJobs()
}

// 查看岗位详情
const viewJobDetail = (job) => {
  currentJob.value = job
  jobDetailVisible.value = true
  
  // 检查是否已投递
  hasApplied.value = false
  
  /**
   * TODO: 实际实现时从API检查是否已投递
   * const checkApplied = async () => {
   *   try {
   *     const res = await api.application.checkApplied(job.id)
   *     if (res.success) {
   *       hasApplied.value = res.data.applied
   *     }
   *   } catch (error) {
   *     console.error('检查投递状态失败:', error)
   *   }
   * }
   * checkApplied()
   */
}

// 投递简历
const handleApply = () => {
  if (hasApplied.value) {
    ElMessage.warning('您已经投递过该岗位')
    return
  }
  
  // 检查是否已完善简历
  const hasResumeCompleted = true // 实际项目中需要检查
  
  if (!hasResumeCompleted) {
    ElMessageBox.confirm('您的简历信息不完整，是否前往完善简历?', '提示', {
      confirmButtonText: '前往完善',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/student/profile')
    })
    return
  }
  
  // 确认投递
  ElMessageBox.confirm('确定要投递简历到该岗位吗?', '投递确认', {
    confirmButtonText: '确定投递',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 模拟投递成功
    setTimeout(() => {
      hasApplied.value = true
      ElMessage.success('简历投递成功')
    }, 1000)
    
    /**
     * TODO: 实际实现时调用API投递简历
     * const applyJob = async () => {
     *   try {
     *     const res = await api.application.apply({
     *       jobId: currentJob.value.id
     *     })
     *     if (res.success) {
     *       hasApplied.value = true
     *       ElMessage.success('简历投递成功')
     *     } else {
     *       ElMessage.error(res.message || '投递失败')
     *     }
     *   } catch (error) {
     *     console.error('投递简历失败:', error)
     *     ElMessage.error('系统错误，请稍后重试')
     *   }
     * }
     * applyJob()
     */
  })
}

// 联系企业
const handleContact = () => {
  // 模拟创建会话并跳转到聊天页面
  ElMessage.success('即将跳转到聊天页面')
  
  /**
   * TODO: 实际实现时创建聊天会话并跳转
   * const createChatSession = async () => {
   *   try {
   *     const res = await api.chat.createSession({
   *       companyId: currentJob.value.companyId,
   *       jobId: currentJob.value.id
   *     })
   *     if (res.success) {
   *       router.push({
   *         path: '/student/chat',
   *         query: { sessionId: res.data.sessionId }
   *       })
   *     } else {
   *       ElMessage.error(res.message || '创建会话失败')
   *     }
   *   } catch (error) {
   *     console.error('创建聊天会话失败:', error)
   *     ElMessage.error('系统错误，请稍后重试')
   *   }
   * }
   * createChatSession()
   */
}
</script>

<style scoped>
.jobs-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.filter-card {
  padding: 10px;
}

.main-content {
  display: flex;
}

.job-list {
  flex: 1;
}

.job-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.job-list-header h2 {
  margin: 0;
}

.loading-container, .empty-container {
  padding: 40px 0;
  text-align: center;
}

.job-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.job-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.job-title {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.job-salary {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.job-card-company {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.company-info {
  margin-left: 15px;
}

.company-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.company-name .el-tag {
  margin-left: 10px;
}

.company-meta {
  font-size: 14px;
  color: #909399;
}

.separator {
  margin: 0 5px;
}

.job-card-meta {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-right: 15px;
  margin-bottom: 10px;
  color: #606266;
}

.meta-item .el-icon {
  margin-right: 5px;
}

.job-card-tags {
  margin-bottom: 15px;
}

.job-tag {
  margin-right: 10px;
  margin-bottom: 5px;
}

.job-card-footer {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 岗位详情样式 */
.job-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.job-detail-header h2 {
  margin: 0;
  font-size: 24px;
}

.job-detail-company {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.job-detail-meta {
  margin-bottom: 20px;
}

.meta-row {
  display: flex;
  margin-bottom: 10px;
}

.meta-item {
  flex: 1;
}

.meta-label {
  color: #909399;
  margin-right: 5px;
}

.meta-value {
  color: #303133;
  font-weight: 500;
}

.job-tags {
  margin-bottom: 20px;
}

.job-detail-content {
  margin-bottom: 20px;
}

.job-detail-content h3 {
  margin-top: 20px;
  margin-bottom: 10px;
}

.job-description, .job-requirements {
  line-height: 1.6;
  color: #606266;
  white-space: pre-line;
}

.job-benefits {
  margin-top: 10px;
}

.benefit-tag {
  margin-right: 10px;
  margin-bottom: 10px;
  padding: 6px 12px;
}

.job-contact {
  margin-bottom: 20px;
}

.job-contact h3 {
  margin-top: 20px;
  margin-bottom: 10px;
}

.contact-info {
  line-height: 1.8;
}

.contact-label {
  color: #909399;
  margin-right: 5px;
}
</style> 