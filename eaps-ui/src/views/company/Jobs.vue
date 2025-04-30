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
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Location, Timer, Collection, Calendar, Search, User } from '@element-plus/icons-vue'
import { getJobList, createJob, updateJob, deleteJob, refreshJob } from '@/api/job'
import { callApi } from '@/utils/apiUtils'

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
    case 'active':
      return '招聘中'
    case 'pending':
      return '待审核'
    case 'rejected':
      return '已驳回'
    case 'ended':
    case 'closed':
      return '已结束'
    case 'draft':
      return '草稿'
    default:
      return '未知状态'
  }
}

// 获取状态对应的标签类型
const getStatusType = (status) => {
  switch (status) {
    case 'recruiting':
    case 'active':
      return 'success'
    case 'pending':
      return 'warning'
    case 'rejected':
      return '已驳回'
    case 'ended':
    case 'closed':
      return 'info'
    case 'draft':
      return ''
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
const fetchJobs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value,
      status: activeTab.value === 'all' ? '' : activeTab.value
    }
    
    const result = await callApi(getJobList(params))
    
    jobs.value = result.list || []
    total.value = result.total || 0
    
    // 更新计数
    statusCounts.recruiting = result.statusCounts?.recruiting || 0
    statusCounts.pending = result.statusCounts?.pending || 0
    statusCounts.rejected = result.statusCounts?.rejected || 0
    statusCounts.ended = result.statusCounts?.ended || 0
  } catch (error) {
    // callApi已处理错误提示
    console.error('获取岗位列表失败:', error)
    jobs.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchJobs()
}

// 处理标签切换
const handleTabChange = () => {
  currentPage.value = 1
  fetchJobs()
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchJobs()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchJobs()
}

// 查看申请
const viewApplications = (job) => {
  router.push({
    path: '/company/applications',
    query: { jobId: job.id }
  })
}

// 处理创建岗位
const handleCreateJob = () => {
  router.push('/company/job/create')
}

// 处理编辑岗位
const handleEdit = (job) => {
  router.push(`/company/job/edit/${job.id}`)
}

// 处理刷新岗位
const handleRefresh = async (job) => {
  try {
    await ElMessageBox.confirm('确定要刷新此岗位吗？刷新后岗位将会排在前面', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const loading = ElLoading.service({
      lock: true,
      text: '刷新中...',
      background: 'rgba(255, 255, 255, 0.7)'
    })
    
    await callApi(refreshJob(job.id), {
      showSuccess: true,
      successMsg: '岗位刷新成功'
    })
    
    fetchJobs()
    loading.close()
  } catch (error) {
    if (error !== 'cancel') {
      // callApi已处理错误提示
      console.error('岗位刷新失败:', error)
    }
  }
}

// 处理结束招聘
const handleEnd = async (job) => {
  try {
    await ElMessageBox.confirm('确定要结束此岗位的招聘吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const loading = ElLoading.service({
      lock: true,
      text: '处理中...',
      background: 'rgba(255, 255, 255, 0.7)'
    })
    
    await callApi(updateJob({
      jobId: job.id,
      status: 'ended'
    }), {
      showSuccess: true, 
      successMsg: '已结束岗位招聘'
    })
    
    fetchJobs()
    loading.close()
  } catch (error) {
    if (error !== 'cancel') {
      // callApi已处理错误提示
      console.error('结束岗位招聘失败:', error)
    }
  }
}

// 处理重新提交
const handleResubmit = async (job) => {
  router.push(`/company/job/edit/${job.id}?resubmit=true`)
}

// 处理重新开放
const handleReopen = async (job) => {
  try {
    await ElMessageBox.confirm('确定要重新开放此岗位吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const loading = ElLoading.service({
      lock: true,
      text: '处理中...',
      background: 'rgba(255, 255, 255, 0.7)'
    })
    
    await callApi(updateJob({
      jobId: job.id,
      status: 'pending' // 重新开放需要重新审核
    }), {
      showSuccess: true,
      successMsg: '岗位已提交重新审核'
    })
    
    fetchJobs()
    loading.close()
  } catch (error) {
    if (error !== 'cancel') {
      // callApi已处理错误提示
      console.error('重新开放岗位失败:', error)
    }
  }
}

// 处理删除岗位
const handleDelete = async (job) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除此岗位吗？删除后将无法恢复，相关申请记录也将被删除',
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )
    
    const loading = ElLoading.service({
      lock: true,
      text: '删除中...',
      background: 'rgba(255, 255, 255, 0.7)'
    })
    
    await callApi(deleteJob(job.id), {
      showSuccess: true,
      successMsg: '岗位删除成功'
    })
    
    fetchJobs()
    loading.close()
  } catch (error) {
    if (error !== 'cancel') {
      // callApi已处理错误提示
      console.error('删除岗位失败:', error)
    }
  }
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