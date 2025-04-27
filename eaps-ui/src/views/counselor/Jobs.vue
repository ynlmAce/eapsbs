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
            <el-option label="已通过" value="active"></el-option>
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
          <div class="action-buttons">
            <el-button size="small" type="primary" plain @click="refreshJobTasks">
              <el-icon><Refresh /></el-icon> 刷新数据
            </el-button>
          </div>
          <div class="tabs">
            <el-radio-group v-model="activeTab" size="small" @change="handleTabChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="pending">待审核</el-radio-button>
              <el-radio-button label="active">已通过</el-radio-button>
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
        <el-table-column label="岗位名称" min-width="150">
          <template #default="scope">
            {{ scope.row.jobDetails?.title || scope.row.title }}
          </template>
        </el-table-column>
        <el-table-column label="发布企业" min-width="150">
          <template #default="scope">
            {{ scope.row.companyName }}
          </template>
        </el-table-column>
        <el-table-column label="工作地点" width="120">
          <template #default="scope">
            {{ scope.row.jobDetails?.location || scope.row.location }}
          </template>
        </el-table-column>
        <el-table-column label="薪资范围" width="120">
          <template #default="scope">
            {{ scope.row.jobDetails?.salary || scope.row.salary }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="180">
          <template #default="scope">
            {{ scope.row.jobDetails?.publishedAt || scope.row.publishedAt || scope.row.createdAt }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.jobDetails?.status || scope.row.status)"
              size="small"
            >
              {{ getStatusText(scope.row.jobDetails?.status || scope.row.status) }}
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
              v-if="isPendingStatus(scope.row)"
              size="small" 
              type="success" 
              plain
              @click.stop="handleAudit(scope.row, 'approve')"
            >通过</el-button>
            <el-button 
              v-if="isPendingStatus(scope.row)"
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
          <el-descriptions-item label="岗位名称">{{ currentJob.jobDetails?.title }}</el-descriptions-item>
          <el-descriptions-item label="发布企业">{{ currentJob.companyName }}</el-descriptions-item>
          <el-descriptions-item label="工作地点">{{ currentJob.jobDetails?.location }}</el-descriptions-item>
          <el-descriptions-item label="薪资范围">{{ currentJob.jobDetails?.salary }}</el-descriptions-item>
          <el-descriptions-item label="招聘人数">{{ currentJob.jobDetails?.headcount }}人</el-descriptions-item>
          <el-descriptions-item label="工作类型">{{ currentJob.jobDetails?.jobType }}</el-descriptions-item>
          <el-descriptions-item label="学历要求">{{ currentJob.jobDetails?.education }}</el-descriptions-item>
          <el-descriptions-item label="经验要求">{{ currentJob.jobDetails?.experience }}</el-descriptions-item>
          <el-descriptions-item label="工作时间">{{ currentJob.jobDetails?.workTime }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentJob.jobDetails?.publishedAt || currentJob.jobDetails?.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="有效期至">{{ currentJob.jobDetails?.validUntil }}</el-descriptions-item>
          <el-descriptions-item v-if="currentJob.jobDetails?.showContact" label="联系信息">
            {{ currentJob.jobDetails?.contactPerson }} / {{ currentJob.jobDetails?.contactMethod }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(currentJob.jobDetails?.status)">
              {{ getStatusText(currentJob.jobDetails?.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="投递数量">{{ currentJob.applications || 0 }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="job-tags">
          <h4>岗位标签</h4>
          <div>
            <el-tag 
              v-for="tag in currentJob.jobDetails?.jobTags" 
              :key="tag"
              class="tag-item"
            >{{ tag }}</el-tag>
            <el-empty v-if="!currentJob.jobDetails?.jobTags || currentJob.jobDetails?.jobTags.length === 0" description="暂无标签" :image-size="60"></el-empty>
          </div>
        </div>
        
        <div class="job-welfare">
          <h4>岗位福利</h4>
          <div>
            <el-tag 
              v-for="welfare in currentJob.jobDetails?.welfareTags" 
              :key="welfare"
              class="tag-item"
              type="success"
            >{{ welfare }}</el-tag>
            <el-empty v-if="!currentJob.jobDetails?.welfareTags || currentJob.jobDetails?.welfareTags.length === 0" description="暂无福利信息" :image-size="60"></el-empty>
          </div>
        </div>
        
        <div class="job-description">
          <h4>岗位描述</h4>
          <div class="description-content">{{ currentJob.jobDetails?.description }}</div>
        </div>
        
        <div class="job-requirements">
          <h4>岗位要求</h4>
          <div class="requirements-content">{{ currentJob.jobDetails?.requirement }}</div>
        </div>
        
        <div v-if="isPendingStatus(currentJob)" class="dialog-footer">
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
        
        <div v-else-if="isRejectedStatus(currentJob)" class="rejection-reason">
          <h4>驳回原因</h4>
          <p>{{ currentJob.jobDetails?.rejectReason || '未提供驳回原因' }}</p>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { useCounselorStore } from '@/store/counselorStore'
import { getTasksList } from '@/api/counselor'

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
  
  console.log('过滤前的任务列表:', result);
  
  // 根据活动的Tab过滤
  if (activeTab.value !== 'all') {
    result = result.filter(job => {
      // 仅使用job_posting表的状态进行筛选
      const jobStatus = job.jobDetails?.status || job.status || '';
      
      // 状态值匹配逻辑
      if (activeTab.value === 'active') {
        // 对于已通过标签，显示岗位状态为active/approved/recruiting的记录
        return ['active', 'approved', 'recruiting'].includes(jobStatus.toLowerCase());
      } else if (activeTab.value === 'pending') {
        // 待审核状态
        return jobStatus.toLowerCase() === 'pending';
      } else if (activeTab.value === 'rejected') {
        // 已驳回状态
        return jobStatus.toLowerCase() === 'rejected';
      } else if (activeTab.value === 'closed') {
        // 已结束状态
        return ['closed', 'ended'].includes(jobStatus.toLowerCase());
      } else {
        // 其他状态匹配岗位状态
        return jobStatus.toLowerCase() === activeTab.value.toLowerCase();
      }
    });
  }
  
  // 根据过滤表单过滤
  if (filterForm.title) {
    result = result.filter(job => {
      const title = job.jobDetails?.title || job.title || '';
      return title.toLowerCase().includes(filterForm.title.toLowerCase());
    })
  }
  
  if (filterForm.company) {
    result = result.filter(job => { 
      const company = job.companyName || '';
      return company.toLowerCase().includes(filterForm.company.toLowerCase());
    })
  }
  
  if (filterForm.status) {
    result = result.filter(job => {
      // 仅使用job_posting表的状态进行筛选
      const jobStatus = job.jobDetails?.status || job.status || '';
      
      // 状态值匹配逻辑
      if (filterForm.status === 'active') {
        // 对于已通过筛选，显示岗位状态为active/approved/recruiting的记录
        return ['active', 'approved', 'recruiting'].includes(jobStatus.toLowerCase());
      } else if (filterForm.status === 'pending') {
        // 待审核状态
        return jobStatus.toLowerCase() === 'pending';
      } else if (filterForm.status === 'rejected') {
        // 已驳回状态
        return jobStatus.toLowerCase() === 'rejected';
      } else if (filterForm.status === 'closed') {
        // 已结束状态
        return ['closed', 'ended'].includes(jobStatus.toLowerCase());
      } else {
        // 其他状态匹配岗位状态
        return jobStatus.toLowerCase() === filterForm.status.toLowerCase();
      }
    });
  }
  
  console.log('过滤后的任务列表:', result);
  return result
})

// 获取状态标签类型
const getStatusType = (status) => {
  // 打印状态值以便调试
  console.log('状态值:', status);
  
  // 统一转换为小写并去除空格
  const normalizedStatus = status?.toString().toLowerCase().trim();
  
  switch (normalizedStatus) {
    case 'draft': return 'info'
    case 'pending': return 'warning'
    case 'active': 
    case 'approved': 
    case 'recruiting': return 'success'
    case 'rejected': return 'danger'
    case 'closed': 
    case 'ended': return 'info'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  // 统一转换为小写并去除空格
  const normalizedStatus = status?.toString().toLowerCase().trim();
  
  switch (normalizedStatus) {
    case 'draft': return '草稿'
    case 'pending': return '待审核'
    case 'active': 
    case 'approved': 
    case 'recruiting': return '已通过'
    case 'rejected': return '已驳回'
    case 'closed': 
    case 'ended': return '已结束'
    default: return status ? `未知(${status})` : '未知状态'
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
    console.log('岗位数据:', job); // 添加这行来查看完整数据结构
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
    
    // 设置审核后的状态
    const status = action === 'approve' ? 'active' : 'rejected'
    
    // 获取正确的任务ID
    const taskId = job.id;
    
    // 获取岗位ID
    const jobId = job.targetItemId || (job.jobDetails ? job.jobDetails.id : null);
    
    if (!taskId) {
      ElMessage.error('无法获取任务ID');
      return;
    }
    
    // 调用store的processTask方法处理任务
    const result = await counselorStore.processTask({
      taskId: taskId,
      type: 'jobAudit',
      action,
      reason,
      status,
      jobId
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
      // 使用job_posting表的状态进行筛选
      filters.jobStatus = activeTab.value
    }
    
    // 添加搜索筛选
    if (filterForm.title) {
      filters.title = filterForm.title
    }
    
    if (filterForm.company) {
      filters.company = filterForm.company
    }
    
    if (filterForm.status) {
      filters.jobStatus = filterForm.status
    }
    
    // 检查是否有来自工作台的任务ID
    const fromDashboard = route.query.fromDashboard === 'true'
    const taskId = route.query.taskId
    
    if (fromDashboard && taskId) {
      filters.taskId = taskId
    }
    
    // 使用新的方法获取所有状态的岗位任务
    const result = await counselorStore.fetchAllJobTasks({
      page: currentPage.value,
      pageSize: pageSize.value,
      filters
    })
    
    console.log('API返回的岗位任务数据:', JSON.stringify(result));
    console.log('Store中的jobTasks:', JSON.stringify(counselorStore.jobTasks));
    
    // 检查不同状态的任务数量
    const statusCounts = {
      total: counselorStore.jobTasks.length,
      pending: 0,
      active: 0,
      approved: 0,
      recruiting: 0,
      rejected: 0,
      closed: 0,
      other: 0
    };
    
    counselorStore.jobTasks.forEach(task => {
      const status = (task.jobDetails?.status || task.status || '').toLowerCase();
      if (status === 'pending') statusCounts.pending++;
      else if (status === 'active') statusCounts.active++;
      else if (status === 'approved') statusCounts.approved++;
      else if (status === 'recruiting') statusCounts.recruiting++;
      else if (status === 'rejected') statusCounts.rejected++;
      else if (status === 'closed') statusCounts.closed++;
      else statusCounts.other++;
    });
    
    console.log('不同状态任务数量:', statusCounts);
    
    totalJobs.value = result.total || 0
    
    // 如果有特定的任务ID，自动打开该岗位详情
    if (taskId && jobTasks.value.length > 0) {
      const targetJob = jobTasks.value.find(j => j.id.toString() === taskId.toString())
      if (targetJob) {
        console.log('找到匹配的岗位任务:', JSON.stringify(targetJob));
        viewJob(targetJob)
      }
    }
  } catch (error) {
    console.error('加载岗位审核任务失败', error)
    ElMessage.error('加载数据失败，请刷新重试')
  }
}

// 刷新岗位任务列表
const refreshJobTasks = () => {
  activeTab.value = 'all';
  filterForm.title = '';
  filterForm.company = '';
  filterForm.status = '';
  currentPage.value = 1;
  loadJobTasks();
  ElMessage.success('刷新成功');
}

// 初始化函数
const init = () => {
  // 检查URL参数，设置初始Tab
  if (route.query.status) {
    activeTab.value = route.query.status.toString()
  }
  
  // 加载数据
  loadJobTasks()
}

// 页面加载时初始化数据
onMounted(() => {
  init();
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

// 添加状态检查辅助函数
const isPendingStatus = (job) => {
  // 使用job_posting表的状态来判断是否为待处理
  const jobStatus = job.jobDetails?.status || job.status || '';
  return jobStatus.toLowerCase() === 'pending';
}

// 添加已驳回状态检查辅助函数
const isRejectedStatus = (job) => {
  // 岗位状态为rejected的是已驳回
  const jobStatus = job.jobDetails?.status || job.status || '';
  return jobStatus.toLowerCase() === 'rejected';
}
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

.action-buttons {
  margin-right: auto;
  margin-left: 20px;
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