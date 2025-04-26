<template>
  <div class="reports-page">
    <h2 class="page-title">举报管理</h2>
    
    <!-- 筛选面板 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="举报类型">
          <el-select v-model="filterForm.type" placeholder="全部" clearable>
            <el-option label="评价举报" value="rating"></el-option>
            <el-option label="消息举报" value="message"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable>
            <el-option label="待处理" value="pending"></el-option>
            <el-option label="已处理" value="resolved"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 举报列表 -->
    <el-card class="report-list-card">
      <template #header>
        <div class="card-header">
          <span>举报列表</span>
          <div class="tabs">
            <el-radio-group v-model="activeTab" size="small" @change="handleTabChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="pending">待处理</el-radio-button>
              <el-radio-button label="resolved">已处理</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      
      <el-table
        :data="filteredReportList"
        v-loading="loading"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="type" label="举报类型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)" size="small">
              {{ getTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporter" label="举报人" width="120"></el-table-column>
        <el-table-column prop="targetInfo" label="举报内容" min-width="200"></el-table-column>
        <el-table-column prop="reason" label="举报原因" min-width="180"></el-table-column>
        <el-table-column prop="createdAt" label="举报时间" width="180"></el-table-column>
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              plain
              @click.stop="viewReport(scope.row)"
            >查看</el-button>
            <el-button 
              v-if="scope.row.status === 'pending'"
              size="small" 
              type="success" 
              plain
              @click.stop="handleReportAction(scope.row, 'ignore')"
            >忽略</el-button>
            <el-button 
              v-if="scope.row.status === 'pending'"
              size="small" 
              type="danger" 
              plain
              @click.stop="handleReportAction(scope.row, 'remove')"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalReports"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 举报详情对话框 -->
    <el-dialog
      v-model="reportDetailVisible"
      title="举报详情"
      width="60%"
      destroy-on-close
    >
      <template v-if="currentReport">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="举报ID">{{ currentReport.id }}</el-descriptions-item>
          <el-descriptions-item label="举报类型">
            <el-tag :type="getTypeTag(currentReport.type)">
              {{ getTypeText(currentReport.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="举报人">{{ currentReport.reporter }}</el-descriptions-item>
          <el-descriptions-item label="举报时间">{{ currentReport.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentReport.status)">
              {{ getStatusText(currentReport.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentReport.status === 'resolved'" label="处理时间">
            {{ currentReport.resolvedAt || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="举报原因" :span="2">
            {{ currentReport.reason }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="report-content">
          <h4>举报内容</h4>
          <div v-if="currentReport.type === 'rating'" class="rating-report">
            <el-card class="rating-card">
              <div class="rating-header">
                <span>对 {{ currentReport.targetDetails.company }} 的评价</span>
                <div class="rating-score">
                  <el-rate
                    v-model="currentReport.targetDetails.score"
                    disabled
                    show-score
                    text-color="#ff9900"
                  />
                </div>
              </div>
              <div class="rating-content">
                {{ currentReport.targetDetails.content }}
              </div>
              <div class="rating-date">
                评价时间: {{ currentReport.targetDetails.createdAt }}
              </div>
            </el-card>
          </div>
          
          <div v-else-if="currentReport.type === 'message'" class="message-report">
            <el-card class="message-card">
              <div class="message-header">
                <span>{{ currentReport.targetDetails.sender }} 的消息</span>
                <div class="message-time">{{ currentReport.targetDetails.sentAt }}</div>
              </div>
              <div class="message-content">
                {{ currentReport.targetDetails.content }}
              </div>
              <div v-if="currentReport.targetDetails.session" class="message-session">
                会话: {{ currentReport.targetDetails.session }}
              </div>
            </el-card>
          </div>
        </div>
        
        <div v-if="currentReport.status === 'pending'" class="dialog-footer">
          <el-form :model="actionForm" ref="actionFormRef" label-width="100px">
            <el-form-item label="处理意见" prop="remarks">
              <el-input
                v-model="actionForm.remarks"
                type="textarea"
                :rows="3"
                placeholder="请输入处理意见"
              ></el-input>
            </el-form-item>
            
            <el-form-item label="行为分惩罚" v-if="currentReport.type === 'message'">
              <el-switch
                v-model="actionForm.punishScore"
                :active-value="true"
                :inactive-value="false"
                active-text="扣除行为分"
                inactive-text="不扣分"
              />
              <el-input-number 
                v-if="actionForm.punishScore" 
                v-model="actionForm.scoreValue" 
                :min="1" 
                :max="10"
                class="score-input"
              ></el-input-number>
            </el-form-item>
          </el-form>
          
          <div class="dialog-buttons">
            <el-button @click="reportDetailVisible = false">取消</el-button>
            <el-button type="primary" @click="handleReportAction(currentReport, 'ignore')">
              忽略举报
            </el-button>
            <el-button type="danger" @click="handleReportAction(currentReport, 'remove')">
              删除内容
            </el-button>
          </div>
        </div>
        
        <div v-else-if="currentReport.status === 'resolved'" class="resolution-info">
          <h4>处理结果</h4>
          <p>{{ currentReport.resolution || '未提供处理记录' }}</p>
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
const totalReports = ref(0)
const reportDetailVisible = ref(false)
const currentReport = ref(null)

// 筛选表单
const filterForm = reactive({
  type: '',
  status: ''
})

// 处理表单
const actionForm = reactive({
  remarks: '',
  punishScore: false,
  scoreValue: 5
})

const actionFormRef = ref(null)

// 使用store中的loading状态和举报任务列表
const loading = computed(() => counselorStore.loading.reports)
const reportTasks = computed(() => counselorStore.reportTasks)

// 根据筛选条件和Tab过滤举报列表
const filteredReportList = computed(() => {
  let result = [...reportTasks.value]
  
  // 根据活动的Tab过滤
  if (activeTab.value !== 'all') {
    result = result.filter(report => report.status === activeTab.value)
  }
  
  // 根据过滤表单过滤
  if (filterForm.type) {
    result = result.filter(report => report.type === filterForm.type)
  }
  
  if (filterForm.status) {
    result = result.filter(report => report.status === filterForm.status)
  }
  
  // 检查URL中是否有类型参数
  const routeType = route.query.type
  if (routeType && !filterForm.type) {
    result = result.filter(report => report.type === routeType)
  }
  
  return result
})

// 获取类型标签
const getTypeTag = (type) => {
  switch (type) {
    case 'rating': return 'warning'
    case 'message': return 'info'
    default: return ''
  }
}

// 获取类型文本
const getTypeText = (type) => {
  switch (type) {
    case 'rating': return '评价举报'
    case 'message': return '消息举报'
    default: return '未知类型'
  }
}

// 获取状态标签
const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'resolved': return 'success'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待处理'
    case 'resolved': return '已处理'
    default: return '未知状态'
  }
}

// 处理查询
const handleSearch = () => {
  currentPage.value = 1
  loadReportTasks()
}

// 重置过滤条件
const resetFilter = () => {
  filterForm.type = ''
  filterForm.status = ''
  handleSearch()
}

// 处理Tab切换
const handleTabChange = () => {
  currentPage.value = 1
  loadReportTasks()
}

// 处理每页数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  loadReportTasks()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadReportTasks()
}

// 行点击
const handleRowClick = (row) => {
  viewReport(row)
}

// 查看举报详情
const viewReport = (report) => {
  currentReport.value = report
  reportDetailVisible.value = true
  // 重置处理表单
  actionForm.remarks = ''
  actionForm.punishScore = false
  actionForm.scoreValue = 5
}

// 处理举报
const handleReportAction = async (report, action) => {
  // 确保有当前举报
  if (!report) return
  
  try {
    // 确认操作
    const confirmMessage = action === 'ignore' 
      ? '确定忽略此举报吗？' 
      : '确定删除被举报内容吗？'
      
    await ElMessageBox.confirm(confirmMessage, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: action === 'ignore' ? 'warning' : 'error'
    })
    
    // 准备处理意见
    let reason = actionForm.remarks
    let notes = undefined
    
    // 如果是删除且需要扣除行为分
    if (action === 'remove' && actionForm.punishScore) {
      notes = `扣除行为分: ${actionForm.scoreValue}`
    }
    
    // 调用store的processTask方法处理任务
    const result = await counselorStore.processTask({
      taskId: report.id,
      type: 'reportHandling',
      subtype: report.type, // 传递举报子类型（rating或message）
      action,
      reason,
      notes
    })
    
    if (result.success) {
      ElMessage.success(action === 'ignore' ? '已忽略此举报' : '已删除被举报内容')
      reportDetailVisible.value = false
      // 重新加载数据
      loadReportTasks()
      // 通知Dashboard更新
      counselorStore.fetchDashboardData()
    } else {
      ElMessage.error(result.message || '处理失败')
    }
  } catch (e) {
    // 用户取消操作，不做处理
    if (e !== 'cancel') {
      console.error('处理举报出错:', e)
      ElMessage.error('操作失败: ' + e.message)
    }
  }
}

// 加载举报处理任务
const loadReportTasks = async () => {
  try {
    const filters = {}
    
    // 根据当前tab状态添加筛选条件
    if (activeTab.value !== 'all') {
      filters.status = activeTab.value
    }
    
    // 添加搜索筛选
    if (filterForm.type) {
      filters.type = filterForm.type
    }
    
    if (filterForm.status) {
      filters.status = filterForm.status
    }
    
    // 检查URL中是否有类型参数
    const routeType = route.query.type
    if (routeType && !filters.type) {
      filters.type = routeType
    }
    
    // 检查是否有来自工作台的任务ID
    const fromDashboard = route.query.fromDashboard === 'true'
    const taskId = route.query.taskId
    
    if (fromDashboard && taskId) {
      filters.taskId = taskId
    }
    
    const result = await counselorStore.fetchReportTasks({
      page: currentPage.value,
      pageSize: pageSize.value,
      filters
    })
    
    totalReports.value = result.total || 0
    
    // 如果有特定的任务ID，自动打开该举报详情
    if (taskId && reportTasks.value.length > 0) {
      const targetReport = reportTasks.value.find(r => r.id.toString() === taskId.toString())
      if (targetReport) {
        viewReport(targetReport)
      }
    }
  } catch (error) {
    console.error('加载举报处理任务失败', error)
    ElMessage.error('加载数据失败，请刷新重试')
  }
}

// 页面加载时初始化数据
onMounted(() => {
  // 检查URL参数，设置初始Tab和筛选条件
  if (route.query.status) {
    activeTab.value = route.query.status.toString()
  }
  
  if (route.query.type) {
    filterForm.type = route.query.type.toString()
  }
  
  // 加载数据
  loadReportTasks()
})

// 监听路由变化，重新加载数据
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.status) {
      activeTab.value = newQuery.status.toString()
    }
    
    if (newQuery.type) {
      filterForm.type = newQuery.type.toString()
    }
    
    if (newQuery.fromDashboard === 'true') {
      // 重置到第一页
      currentPage.value = 1
      loadReportTasks()
    }
  }
)
</script>

<style scoped>
.reports-page {
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

.report-list-card {
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

.report-content {
  margin-top: 20px;
}

.report-content h4 {
  margin-bottom: 15px;
  font-weight: bold;
  color: #303133;
}

.rating-card,
.message-card {
  margin-bottom: 20px;
}

.rating-header,
.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.rating-content,
.message-content {
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
  margin-bottom: 10px;
  line-height: 1.6;
}

.rating-date,
.message-time,
.message-session {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.score-input {
  margin-left: 10px;
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

.resolution-info {
  margin-top: 20px;
  padding: 15px;
  background-color: #f0f9eb;
  border-radius: 4px;
  border-left: 3px solid #67c23a;
}

.resolution-info h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-weight: bold;
  color: #303133;
}

.resolution-info p {
  margin: 0;
  line-height: 1.6;
  white-space: pre-line;
}
</style> 