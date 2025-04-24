<template>
  <div class="jobs-management">
    <div class="page-header">
      <h2>岗位管理</h2>
      <el-button type="primary" @click="handleCreateJob">发布新岗位</el-button>
    </div>
    
    <el-tabs v-model="activeTab" @tab-click="handleTabChange">
      <el-tab-pane label="全部岗位" name="all"></el-tab-pane>
      <el-tab-pane label="招聘中" name="recruiting">
        <template #label>
          <span>招聘中</span>
          <el-badge :value="statusCounts.recruiting" :hidden="statusCounts.recruiting === 0" class="status-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="待审核" name="pending">
        <template #label>
          <span>待审核</span>
          <el-badge :value="statusCounts.pending" :hidden="statusCounts.pending === 0" class="status-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="已驳回" name="rejected">
        <template #label>
          <span>已驳回</span>
          <el-badge :value="statusCounts.rejected" :hidden="statusCounts.rejected === 0" class="status-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="已结束" name="ended">
        <template #label>
          <span>已结束</span>
          <el-badge :value="statusCounts.ended" :hidden="statusCounts.ended === 0" class="status-badge" />
        </template>
      </el-tab-pane>
    </el-tabs>
    
    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索岗位名称" clearable @clear="handleSearch">
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="jobs.length === 0" class="empty-container">
      <el-empty description="暂无岗位信息"></el-empty>
    </div>
    
    <div v-else class="jobs-list">
      <el-card v-for="job in jobs" :key="job.id" class="job-card" shadow="hover">
        <div class="job-header">
          <div class="job-title-container">
            <h3 class="job-title">{{ job.title }}</h3>
            <el-tag :type="getStatusType(job.status)" size="small">{{ getStatusText(job.status) }}</el-tag>
          </div>
          <div class="job-salary">{{ job.salary }}</div>
        </div>
        
        <div class="job-meta">
          <div class="meta-item">
            <el-icon><Location /></el-icon>
            <span>{{ job.location }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Timer /></el-icon>
            <span>{{ job.experience }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Collection /></el-icon>
            <span>{{ job.education }}</span>
          </div>
          <div class="meta-item">
            <el-icon><User /></el-icon>
            <span>{{ job.recruitCount }}人</span>
          </div>
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatTime(job.publishTime) }}</span>
          </div>
        </div>
        
        <div class="job-stats">
          <div class="stat-item">
            <div class="stat-value">{{ job.viewCount }}</div>
            <div class="stat-label">浏览</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ job.applicationCount }}</div>
            <div class="stat-label">申请</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ job.interviewCount }}</div>
            <div class="stat-label">面试</div>
          </div>
        </div>
        
        <div class="job-actions">
          <el-button v-if="job.status === 'recruiting'" size="small" @click="handleRefresh(job)">刷新</el-button>
          <el-button v-if="job.status === 'recruiting'" size="small" @click="handleEnd(job)">结束招聘</el-button>
          <el-button v-if="job.status === 'rejected'" size="small" type="primary" @click="handleResubmit(job)">重新提交</el-button>
          <el-button v-if="job.status === 'ended'" size="small" type="success" @click="handleReopen(job)">重新开放</el-button>
          <el-button size="small" type="primary" @click="handleEdit(job)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(job)">删除</el-button>
          <el-button size="small" @click="viewApplications(job)">查看申请</el-button>
        </div>
        
        <div v-if="job.status === 'rejected'" class="rejection-reason">
          <div class="reason-title">驳回原因:</div>
          <div class="reason-content">{{ job.rejectionReason }}</div>
        </div>
      </el-card>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 这里将实现岗位编辑/创建对话框和其它对话框，但暂时不展开详细实现 -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Timer, Collection, Calendar, Search, User } from '@element-plus/icons-vue'

const router = useRouter()

// 状态计数
const statusCounts = reactive({
  recruiting: 0,
  pending: 0,
  rejected: 0,
  ended: 0
})

// 岗位数据
const activeTab = ref('all')
const searchKeyword = ref('')
const loading = ref(false)
const jobs = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'recruiting':
      return '招聘中'
    case 'pending':
      return '待审核'
    case 'rejected':
      return '已驳回'
    case 'ended':
      return '已结束'
    default:
      return '未知状态'
  }
}

// 获取状态对应的标签类型
const getStatusType = (status) => {
  switch (status) {
    case 'recruiting':
      return 'success'
    case 'pending':
      return 'warning'
    case 'rejected':
      return 'danger'
    case 'ended':
      return 'info'
    default:
      return ''
  }
}

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
      const statuses = ['recruiting', 'pending', 'rejected', 'ended']
      const status = statuses[Math.floor(Math.random() * statuses.length)]
      
      mockJobs.push({
        id: i,
        title: '软件工程师' + i,
        salary: ['3k-5k', '5k-7k', '7k-10k', '10k-15k', '15k-20k'][Math.floor(Math.random() * 5)],
        location: ['北京', '上海', '广州', '深圳', '杭州'][Math.floor(Math.random() * 5)],
        education: ['大专', '本科', '硕士', '博士'][Math.floor(Math.random() * 4)],
        experience: ['经验不限', '1-3年', '3-5年', '5年以上'][Math.floor(Math.random() * 4)],
        recruitCount: Math.floor(Math.random() * 10) + 1,
        publishTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000),
        status,
        viewCount: Math.floor(Math.random() * 1000),
        applicationCount: Math.floor(Math.random() * 100),
        interviewCount: Math.floor(Math.random() * 20),
        rejectionReason: status === 'rejected' ? '岗位描述不够详细，请补充岗位职责和要求' : ''
      })
    }
    
    // 统计各状态数量
    statusCounts.recruiting = mockJobs.filter(job => job.status === 'recruiting').length
    statusCounts.pending = mockJobs.filter(job => job.status === 'pending').length
    statusCounts.rejected = mockJobs.filter(job => job.status === 'rejected').length
    statusCounts.ended = mockJobs.filter(job => job.status === 'ended').length
    
    // 根据标签筛选
    let filteredJobs = [...mockJobs]
    
    if (activeTab.value !== 'all') {
      filteredJobs = filteredJobs.filter(job => job.status === activeTab.value)
    }
    
    // 根据关键词搜索
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      filteredJobs = filteredJobs.filter(job => job.title.toLowerCase().includes(keyword))
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
   *     const res = await api.company.jobs.list({
   *       status: activeTab.value !== 'all' ? activeTab.value : null,
   *       keyword: searchKeyword.value,
   *       page: currentPage.value,
   *       pageSize: pageSize.value
   *     })
   *     if (res.success) {
   *       jobs.value = res.data.list
   *       total.value = res.data.total
   *       
   *       // 更新各状态数量
   *       statusCounts.recruiting = res.data.counts.recruiting || 0
   *       statusCounts.pending = res.data.counts.pending || 0
   *       statusCounts.rejected = res.data.counts.rejected || 0
   *       statusCounts.ended = res.data.counts.ended || 0
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

// 处理标签切换
const handleTabChange = () => {
  currentPage.value = 1
  fetchJobs()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchJobs()
}

// 创建新岗位
const handleCreateJob = () => {
  ElMessage.success('跳转到岗位创建页面')
  // TODO: 跳转到岗位创建页面或打开创建对话框
}

// 编辑岗位
const handleEdit = (job) => {
  ElMessage.success(`编辑岗位: ${job.title}`)
  // TODO: 跳转到岗位编辑页面或打开编辑对话框
}

// 删除岗位
const handleDelete = (job) => {
  ElMessageBox.confirm(`确定要删除岗位"${job.title}"吗？此操作不可逆。`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 模拟删除成功
    ElMessage.success('岗位删除成功')
    
    // 从列表中移除
    const index = jobs.value.findIndex(j => j.id === job.id)
    if (index !== -1) {
      jobs.value.splice(index, 1)
    }
    
    // 更新各状态数量
    statusCounts[job.status]--
    
    /**
     * TODO: 实际实现时调用API删除岗位
     * const deleteJob = async () => {
     *   try {
     *     const res = await api.company.jobs.delete(job.id)
     *     if (res.success) {
     *       ElMessage.success('岗位删除成功')
     *       
     *       // 重新获取岗位列表
     *       fetchJobs()
     *     } else {
     *       ElMessage.error(res.message || '删除失败')
     *     }
     *   } catch (error) {
     *     console.error('删除岗位失败:', error)
     *     ElMessage.error('系统错误，请稍后重试')
     *   }
     * }
     * deleteJob()
     */
  }).catch(() => {})
}

// 刷新岗位
const handleRefresh = (job) => {
  ElMessageBox.confirm('刷新后，岗位将会在列表中排名靠前。确定要刷新吗？', '刷新确认', {
    confirmButtonText: '确定刷新',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 模拟刷新成功
    ElMessage.success('岗位刷新成功')
    
    // 更新发布时间
    job.publishTime = new Date()
    
    /**
     * TODO: 实际实现时调用API刷新岗位
     * const refreshJob = async () => {
     *   try {
     *     const res = await api.company.jobs.refresh(job.id)
     *     if (res.success) {
     *       ElMessage.success('岗位刷新成功')
     *       
     *       // 更新发布时间
     *       job.publishTime = new Date()
     *     } else {
     *       ElMessage.error(res.message || '刷新失败')
     *     }
     *   } catch (error) {
     *     console.error('刷新岗位失败:', error)
     *     ElMessage.error('系统错误，请稍后重试')
     *   }
     * }
     * refreshJob()
     */
  }).catch(() => {})
}

// 结束招聘
const handleEnd = (job) => {
  ElMessageBox.confirm('结束招聘后，该岗位将不再显示在学生端。确定要结束吗？', '结束确认', {
    confirmButtonText: '确定结束',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 模拟结束成功
    ElMessage.success('已结束招聘')
    
    // 更新状态
    job.status = 'ended'
    
    // 更新各状态数量
    statusCounts.recruiting--
    statusCounts.ended++
    
    /**
     * TODO: 实际实现时调用API结束招聘
     * const endJob = async () => {
     *   try {
     *     const res = await api.company.jobs.end(job.id)
     *     if (res.success) {
     *       ElMessage.success('已结束招聘')
     *       
     *       // 重新获取岗位列表
     *       fetchJobs()
     *     } else {
     *       ElMessage.error(res.message || '操作失败')
     *     }
     *   } catch (error) {
     *     console.error('结束招聘失败:', error)
     *     ElMessage.error('系统错误，请稍后重试')
     *   }
     * }
     * endJob()
     */
  }).catch(() => {})
}

// 重新提交被驳回的岗位
const handleResubmit = (job) => {
  ElMessageBox.confirm('重新提交后，岗位将进入待审核状态。确定要重新提交吗？', '提交确认', {
    confirmButtonText: '确定提交',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 模拟提交成功
    ElMessage.success('岗位已重新提交，等待审核')
    
    // 更新状态
    job.status = 'pending'
    
    // 更新各状态数量
    statusCounts.rejected--
    statusCounts.pending++
    
    /**
     * TODO: 实际实现时调用API重新提交岗位
     * const resubmitJob = async () => {
     *   try {
     *     const res = await api.company.jobs.resubmit(job.id)
     *     if (res.success) {
     *       ElMessage.success('岗位已重新提交，等待审核')
     *       
     *       // 重新获取岗位列表
     *       fetchJobs()
     *     } else {
     *       ElMessage.error(res.message || '提交失败')
     *     }
     *   } catch (error) {
     *     console.error('重新提交岗位失败:', error)
     *     ElMessage.error('系统错误，请稍后重试')
     *   }
     * }
     * resubmitJob()
     */
  }).catch(() => {})
}

// 重新开放已结束的岗位
const handleReopen = (job) => {
  ElMessageBox.confirm('重新开放后，岗位将进入待审核状态。确定要重新开放吗？', '开放确认', {
    confirmButtonText: '确定开放',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    // 模拟开放成功
    ElMessage.success('岗位已重新开放，等待审核')
    
    // 更新状态
    job.status = 'pending'
    
    // 更新各状态数量
    statusCounts.ended--
    statusCounts.pending++
    
    /**
     * TODO: 实际实现时调用API重新开放岗位
     * const reopenJob = async () => {
     *   try {
     *     const res = await api.company.jobs.reopen(job.id)
     *     if (res.success) {
     *       ElMessage.success('岗位已重新开放，等待审核')
     *       
     *       // 重新获取岗位列表
     *       fetchJobs()
     *     } else {
     *       ElMessage.error(res.message || '开放失败')
     *     }
     *   } catch (error) {
     *     console.error('重新开放岗位失败:', error)
     *     ElMessage.error('系统错误，请稍后重试')
     *   }
     * }
     * reopenJob()
     */
  }).catch(() => {})
}

// 查看申请
const viewApplications = (job) => {
  router.push({
    path: '/company/applications',
    query: { jobId: job.id }
  })
}
</script>

<style scoped>
.jobs-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.status-badge {
  margin-top: -2px;
  margin-left: 5px;
}

.search-bar {
  margin: 20px 0;
  max-width: 400px;
}

.loading-container, .empty-container {
  margin: 40px 0;
  text-align: center;
}

.job-card {
  margin-bottom: 20px;
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.job-title-container {
  display: flex;
  align-items: center;
}

.job-title {
  margin: 0;
  margin-right: 10px;
  font-size: 18px;
  font-weight: bold;
}

.job-salary {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.job-meta {
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

.job-stats {
  display: flex;
  margin-bottom: 15px;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  padding: 15px 0;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.job-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.rejection-reason {
  margin-top: 15px;
  padding: 10px;
  background-color: #fef0f0;
  border-radius: 4px;
}

.reason-title {
  font-weight: bold;
  color: #f56c6c;
  margin-bottom: 5px;
}

.reason-content {
  color: #606266;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style> 