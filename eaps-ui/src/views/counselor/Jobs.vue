<template>
  <div class="jobs-page">
    <h2 class="page-title">岗位审核管理</h2>
    
    <!-- 筛选面板 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="岗位名称">
          <el-input v-model="filterForm.title" placeholder="请输入" clearable></el-input>
        </el-form-item>
        <el-form-item label="企业名称">
          <el-input v-model="filterForm.company" placeholder="请输入" clearable></el-input>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable>
            <el-option label="待审核" value="pending"></el-option>
            <el-option label="已通过" value="approved"></el-option>
            <el-option label="已驳回" value="rejected"></el-option>
            <el-option label="已结束" value="closed"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 岗位列表 -->
    <el-card class="job-list-card">
      <template #header>
        <div class="card-header">
          <span>岗位列表</span>
          <div class="tabs">
            <el-radio-group v-model="activeTab" size="small" @change="handleTabChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="pending">待审核</el-radio-button>
              <el-radio-button label="approved">已通过</el-radio-button>
              <el-radio-button label="rejected">已驳回</el-radio-button>
              <el-radio-button label="closed">已结束</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      
      <el-table
        :data="filteredJobList"
        v-loading="loading"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="岗位名称" min-width="150"></el-table-column>
        <el-table-column prop="company" label="发布企业" min-width="150"></el-table-column>
        <el-table-column prop="location" label="工作地点" width="120"></el-table-column>
        <el-table-column prop="salary" label="薪资范围" width="120"></el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.status)"
              size="small"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              plain
              @click.stop="viewJob(scope.row)"
            >查看</el-button>
            <el-button 
              v-if="scope.row.status === 'pending'"
              size="small" 
              type="success" 
              plain
              @click.stop="handleAudit(scope.row, 'approve')"
            >通过</el-button>
            <el-button 
              v-if="scope.row.status === 'pending'"
              size="small" 
              type="danger" 
              plain
              @click.stop="handleAudit(scope.row, 'reject')"
            >驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalJobs"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 岗位详情对话框 -->
    <el-dialog
      v-model="jobDetailVisible"
      title="岗位详情"
      width="70%"
      destroy-on-close
    >
      <template v-if="currentJob">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="岗位名称">{{ currentJob.title }}</el-descriptions-item>
          <el-descriptions-item label="发布企业">{{ currentJob.company }}</el-descriptions-item>
          <el-descriptions-item label="工作地点">{{ currentJob.location }}</el-descriptions-item>
          <el-descriptions-item label="薪资范围">{{ currentJob.salary }}</el-descriptions-item>
          <el-descriptions-item label="招聘人数">{{ currentJob.count + '人' }}</el-descriptions-item>
          <el-descriptions-item label="工作类型">{{ currentJob.type }}</el-descriptions-item>
          <el-descriptions-item label="学历要求">{{ currentJob.education }}</el-descriptions-item>
          <el-descriptions-item label="经验要求">{{ currentJob.experience }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentJob.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ currentJob.deadline }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(currentJob.status)">
              {{ getStatusText(currentJob.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="投递数量">{{ currentJob.applications || 0 }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="job-tags">
          <h4>岗位标签</h4>
          <div>
            <el-tag 
              v-for="tag in currentJob.tags" 
              :key="tag"
              class="tag-item"
            >{{ tag }}</el-tag>
            <el-empty v-if="!currentJob.tags || currentJob.tags.length === 0" description="暂无标签" :image-size="60"></el-empty>
          </div>
        </div>
        
        <div class="job-welfare">
          <h4>岗位福利</h4>
          <div>
            <el-tag 
              v-for="welfare in currentJob.welfare" 
              :key="welfare"
              class="tag-item"
              type="success"
            >{{ welfare }}</el-tag>
            <el-empty v-if="!currentJob.welfare || currentJob.welfare.length === 0" description="暂无福利信息" :image-size="60"></el-empty>
          </div>
        </div>
        
        <div class="job-description">
          <h4>岗位描述</h4>
          <div class="description-content">{{ currentJob.description }}</div>
        </div>
        
        <div class="job-requirements">
          <h4>岗位要求</h4>
          <div class="requirements-content">{{ currentJob.requirements }}</div>
        </div>
        
        <div v-if="currentJob.status === 'pending'" class="dialog-footer">
          <el-form :model="auditForm" ref="auditFormRef" label-width="100px">
            <el-form-item label="审核意见" prop="remarks">
              <el-input
                v-model="auditForm.remarks"
                type="textarea"
                :rows="3"
                placeholder="请输入审核意见"
              ></el-input>
            </el-form-item>
            
            <div class="problem-checkboxes">
              <p>问题标记（可多选）：</p>
              <el-checkbox-group v-model="auditForm.problems">
                <el-checkbox label="岗位信息不完整">岗位信息不完整</el-checkbox>
                <el-checkbox label="存在歧视性内容">存在歧视性内容</el-checkbox>
                <el-checkbox label="薪资与岗位不匹配">薪资与岗位不匹配</el-checkbox>
                <el-checkbox label="岗位内容与标题不符">岗位内容与标题不符</el-checkbox>
                <el-checkbox label="存在违规信息">存在违规信息</el-checkbox>
              </el-checkbox-group>
            </div>
          </el-form>
          
          <div class="dialog-buttons">
            <el-button @click="jobDetailVisible = false">取消</el-button>
            <el-button type="success" @click="handleAudit(currentJob, 'approve')">
              通过审核
            </el-button>
            <el-button type="danger" @click="handleAudit(currentJob, 'reject')">
              驳回申请
            </el-button>
          </div>
        </div>
        
        <div v-else-if="currentJob.status === 'rejected'" class="rejection-reason">
          <h4>驳回原因</h4>
          <p>{{ currentJob.rejectReason || '未提供驳回原因' }}</p>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCounselorStore } from '@/store/counselorStore'

// 使用Pinia store
const counselorStore = useCounselorStore()
const route = useRoute()
const router = useRouter()

// 页面状态
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const totalJobs = ref(0)
const jobDetailVisible = ref(false)
const currentJob = ref(null)

// 筛选表单
const filterForm = reactive({
  title: '',
  company: '',
  status: ''
})

// 审核表单
const auditForm = reactive({
  remarks: '',
  problems: []
})

const auditFormRef = ref(null)

// 使用store中的loading状态和岗位任务列表
const loading = computed(() => counselorStore.loading.jobs)
const jobTasks = computed(() => counselorStore.jobTasks)

// 根据筛选条件和Tab过滤岗位列表
const filteredJobList = computed(() => {
  let result = [...jobTasks.value]
  
  // 根据活动的Tab过滤
  if (activeTab.value !== 'all') {
    result = result.filter(job => job.status === activeTab.value)
  }
  
  // 根据过滤表单过滤
  if (filterForm.title) {
    result = result.filter(job => 
      job.title && job.title.toLowerCase().includes(filterForm.title.toLowerCase())
    )
  }
  
  if (filterForm.company) {
    result = result.filter(job => 
      job.company && job.company.toLowerCase().includes(filterForm.company.toLowerCase())
    )
  }
  
  if (filterForm.status) {
    result = result.filter(job => job.status === filterForm.status)
  }
  
  return result
})

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'approved': return 'success'
    case 'rejected': return 'danger'
    case 'closed': return 'info'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待审核'
    case 'approved': return '已通过'
    case 'rejected': return '已驳回'
    case 'closed': return '已结束'
    default: return '未知状态'
  }
}

// 处理查询
const handleSearch = () => {
  currentPage.value = 1
  loadJobTasks()
}

// 重置过滤条件
const resetFilter = () => {
  filterForm.title = ''
  filterForm.company = ''
  filterForm.status = ''
  handleSearch()
}

// 处理Tab切换
const handleTabChange = () => {
  currentPage.value = 1
  loadJobTasks()
}

// 处理每页数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  loadJobTasks()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadJobTasks()
}

// 行点击
const handleRowClick = (row) => {
  viewJob(row)
}

// 查看岗位详情
const viewJob = (job) => {
  currentJob.value = job
  jobDetailVisible.value = true
  // 重置审核表单
  auditForm.remarks = ''
  auditForm.problems = []
}

// 处理审核/驳回操作
const handleAudit = async (job, action) => {
  // 确保有当前岗位
  if (!job) return
  
  try {
    // 确认操作
    const confirmMessage = action === 'approve' 
      ? '确定通过该岗位申请吗？' 
      : '确定驳回该岗位申请吗？'
      
    await ElMessageBox.confirm(confirmMessage, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: action === 'approve' ? 'success' : 'warning'
    })
    
    // 准备审核意见
    let reason = auditForm.remarks
    if (action === 'reject' && auditForm.problems.length > 0) {
      // 如果是驳回且选择了问题标记，将问题标记添加到审核意见中
      reason = `问题: ${auditForm.problems.join(', ')}${reason ? '\n' + reason : ''}`
    }
    
    // 调用store的processTask方法处理任务
    const result = await counselorStore.processTask({
      taskId: job.id,
      type: 'jobAudit',
      action,
      reason
    })
    
    if (result.success) {
      ElMessage.success(action === 'approve' ? '岗位申请已通过' : '岗位申请已驳回')
      jobDetailVisible.value = false
      // 重新加载数据
      loadJobTasks()
      // 通知Dashboard更新
      counselorStore.fetchDashboardData()
    } else {
      ElMessage.error(result.message || '处理失败')
    }
  } catch (e) {
    // 用户取消操作，不做处理
    if (e !== 'cancel') {
      console.error('处理审核出错:', e)
      ElMessage.error('操作失败: ' + e.message)
    }
  }
}

// 加载岗位审核任务
const loadJobTasks = async () => {
  try {
    const filters = {}
    
    // 根据当前tab状态添加筛选条件
    if (activeTab.value !== 'all') {
      filters.status = activeTab.value
    }
    
    // 添加搜索筛选
    if (filterForm.title) {
      filters.title = filterForm.title
    }
    
    if (filterForm.company) {
      filters.company = filterForm.company
    }
    
    if (filterForm.status) {
      filters.status = filterForm.status
    }
    
    // 检查是否有来自工作台的任务ID
    const fromDashboard = route.query.fromDashboard === 'true'
    const taskId = route.query.taskId
    
    if (fromDashboard && taskId) {
      filters.taskId = taskId
    }
    
    const result = await counselorStore.fetchJobTasks({
      page: currentPage.value,
      pageSize: pageSize.value,
      filters
    })
    
    totalJobs.value = result.total || 0
    
    // 如果有特定的任务ID，自动打开该岗位详情
    if (taskId && jobTasks.value.length > 0) {
      const targetJob = jobTasks.value.find(j => j.id.toString() === taskId.toString())
      if (targetJob) {
        viewJob(targetJob)
      }
    }
  } catch (error) {
    console.error('加载岗位审核任务失败', error)
    ElMessage.error('加载数据失败，请刷新重试')
  }
}

// 页面加载时初始化数据
onMounted(() => {
  // 检查URL参数，设置初始Tab
  if (route.query.status) {
    activeTab.value = route.query.status.toString()
  }
  
  // 加载数据
  loadJobTasks()
})

// 监听路由变化，重新加载数据
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.status) {
      activeTab.value = newQuery.status.toString()
    }
    
    if (newQuery.fromDashboard === 'true') {
      // 重置到第一页
      currentPage.value = 1
      loadJobTasks()
    }
  }
)
</script>

<style scoped>
.jobs-page {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-weight: bold;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.job-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.job-tags,
.job-welfare,
.job-description,
.job-requirements,
.rejection-reason {
  margin-top: 20px;
}

.job-tags h4,
.job-welfare h4,
.job-description h4,
.job-requirements h4,
.rejection-reason h4 {
  margin-bottom: 10px;
  font-weight: bold;
  color: #303133;
}

.tag-item {
  margin-right: 8px;
  margin-bottom: 8px;
}

.description-content,
.requirements-content {
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
  line-height: 1.6;
  white-space: pre-line;
}

.problem-checkboxes {
  margin-top: 10px;
  margin-bottom: 20px;
}

.problem-checkboxes p {
  margin-bottom: 10px;
  color: #606266;
}

.dialog-footer {
  margin-top: 30px;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.dialog-buttons {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.rejection-reason {
  padding: 10px;
  background-color: #fff0f0;
  border-radius: 4px;
  border-left: 3px solid #f56c6c;
}
</style> 