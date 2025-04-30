<template>
  <div class="dashboard">
    <h2 class="page-title">辅导员工作台</h2>
    
    <!-- 任务概览 -->
    <el-row :gutter="20" class="task-overview">
      <el-col :span="8">
        <el-card shadow="hover" class="task-card" :class="{ 'disabled-card': dashboard.companyCertTasks === 0 }">
          <div class="task-info">
            <div class="task-title">企业认证</div>
            <div class="task-count">{{ dashboard.companyCertTasks }}</div>
          </div>
          <div class="task-icon">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <el-button 
            type="primary" 
            plain 
            size="small" 
            class="task-action"
            :disabled="dashboard.companyCertTasks === 0"
            @click="goToCompanies"
          >处理</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="task-card" :class="{ 'disabled-card': dashboard.jobAuditTasks === 0 }">
          <div class="task-info">
            <div class="task-title">岗位审核</div>
            <div class="task-count">{{ dashboard.jobAuditTasks }}</div>
          </div>
          <div class="task-icon">
            <el-icon><Briefcase /></el-icon>
          </div>
          <el-button 
            type="primary" 
            plain 
            size="small" 
            class="task-action"
            :disabled="dashboard.jobAuditTasks === 0"
            @click="goToJobs"
          >处理</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="task-card" :class="{ 'disabled-card': dashboard.reportHandling === 0 }">
          <div class="task-info">
            <div class="task-title">举报处理</div>
            <div class="task-count">{{ dashboard.reportHandling }}</div>
          </div>
          <div class="task-icon">
            <el-icon><Star /></el-icon>
          </div>
          <el-button 
            type="primary" 
            plain 
            size="small" 
            class="task-action"
            :disabled="dashboard.reportHandling === 0"
            @click="goToReports()"
          >处理</el-button>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 待处理任务列表 -->
    <el-card class="pending-tasks">
      <template #header>
        <div class="task-header">
          <h3>待处理任务</h3>
          <el-radio-group v-model="taskType" size="small" @change="handleTypeChange">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="company">企业认证</el-radio-button>
            <el-radio-button label="job">岗位审核</el-radio-button>
            <el-radio-button label="report">举报处理</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      
      <el-table
        :data="filteredTasks"
        style="width: 100%"
        v-loading="loading"
        :empty-text="loading ? '加载中...' : '暂无待处理任务'"
        border
        highlight-current-row
        @row-click="viewTaskDetail"
      >
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="type" label="任务类型" width="110">
          <template #default="scope">
            <el-tag :type="getTaskTypeTag(scope.row.type)">
              {{ getTaskTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="目标名称">
          <template #default="scope">
            {{ scope.row.title || scope.row.companyName || '未知任务' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityTag(scope.row.priority)">
              {{ getPriorityName(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click.stop="viewTaskDetail(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click.stop="handleTask(scope.row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalTasks"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="taskDetailDialogVisible"
      :title="'任务详情 - ' + getTaskTypeName(currentTask?.type)"
      width="50%"
    >
      <template v-if="currentTask">
        <div class="task-detail">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
            <el-descriptions-item label="任务类型">
              <el-tag :type="getTaskTypeTag(currentTask.type)">
                {{ getTaskTypeName(currentTask.type) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="目标名称">{{ currentTask.targetName }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentTask.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="getPriorityTag(currentTask.priority)">
                {{ getPriorityName(currentTask.priority) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="currentTask.description" label="详情描述">
              {{ currentTask.description }}
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 企业认证详情 -->
          <div v-if="currentTask.type === 'companyCertification'" class="task-specific-details">
            <h4>企业资质信息</h4>
            <p>企业名称：{{ currentTask.details?.name }}</p>
            <p>统一社会信用代码：{{ currentTask.details?.unified_social_credit_code }}</p>
            <p>行业：{{ currentTask.details?.industry }}</p>
            <p>企业规模：{{ currentTask.details?.size }}</p>
            <p>地址：{{ currentTask.details?.address }}</p>
            <p>HR联系方式：{{ currentTask.details?.hr_contact }}</p>
            <p>认证状态：{{ getCertificationStatus(currentTask.details?.certification_status) }}</p>
            <p v-if="currentTask.details?.certification_expiry_date">认证有效期：{{ currentTask.details?.certification_expiry_date }}</p>
            <div class="document-preview">
              <p>营业执照：</p>
              <el-image
                v-if="currentTask.details?.licenseUrl"
                :src="currentTask.details.licenseUrl"
                :preview-src-list="[currentTask.details.licenseUrl]"
                fit="contain"
                style="width: 100%; max-height: 300px;"
              ></el-image>
            </div>
            <div v-if="currentTask.details?.description" class="company-description">
              <p>公司简介：</p>
              <p>{{ currentTask.details?.description }}</p>
            </div>
          </div>
          
          <!-- 岗位审核详情 -->
          <div v-if="currentTask.type === 'jobAudit'" class="task-specific-details">
            <h4>岗位信息</h4>
            <p>岗位名称：{{ currentTask.details?.title }}</p>
            <p>所属企业：{{ currentTask.companyName }}</p>
            <p>工作地点：{{ currentTask.details?.location }}</p>
            <p>薪资范围：{{ currentTask.details?.salary }}</p>
            <p>招聘人数：{{ currentTask.details?.headcount }}人</p>
            <p>学历要求：{{ currentTask.details?.education }}</p>
            <p>经验要求：{{ currentTask.details?.experience }}</p>
            <p>工作类型：{{ currentTask.details?.jobType }}</p>
            <p>工作时间：{{ currentTask.details?.workTime }}</p>
            <p>联系人为：{{ currentTask.details?.contactPerson }}</p>
            <p>联系方式：{{ currentTask.details?.contactMethod }}</p>
            <p>有效期至：{{ currentTask.details?.validUntil }}</p>
            <p>岗位描述：{{ currentTask.details?.description }}</p>
            <p>岗位要求：{{ currentTask.details?.requirement }}</p>
            <div v-if="currentTask.details?.jobTags && currentTask.details?.jobTags.length > 0" class="job-tags">
              <p>岗位标签：</p>
              <el-tag v-for="tag in currentTask.details?.jobTags" :key="tag" class="mx-1" type="info">
                {{ tag }}
              </el-tag>
            </div>
            <div v-if="currentTask.details?.welfareTags && currentTask.details?.welfareTags.length > 0" class="welfare-tags">
              <p>岗位福利：</p>
              <el-tag v-for="tag in currentTask.details?.welfareTags" :key="tag" class="mx-1" type="success">
                {{ tag }}
              </el-tag>
            </div>
          </div>
          
          <!-- 举报处理详情 -->
          <div v-if="currentTask.type === 'reportHandling'" class="task-specific-details">
            <h4>举报信息</h4>
            <p>举报人：{{ currentTask.details?.reporter }}</p>
            <p>举报原因：{{ currentTask.details?.reason }}</p>
            <p>举报时间：{{ currentTask.details?.reportTime }}</p>
            
            <div v-if="currentTask.details?.reportedItemType === 'rating'" class="report-content">
              <h4>被举报的评价内容</h4>
              <p>评价对象：{{ currentTask.details?.targetName }}</p>
              <p>评分：{{ currentTask.details?.score }}分</p>
              <p>评价内容：{{ currentTask.details?.content }}</p>
            </div>
            
            <div v-if="currentTask.details?.reportedItemType === 'message'" class="report-content">
              <h4>被举报的消息内容</h4>
              <p>发送者：{{ currentTask.details?.sender }}</p>
              <p>发送时间：{{ currentTask.details?.sendTime }}</p>
              <p>消息内容：{{ currentTask.details?.content }}</p>
            </div>
          </div>
        </div>
      </template>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="taskDetailDialogVisible = false">关闭</el-button>
          <!-- <el-button type="primary" @click="handleTaskFromDialog">处理此任务</el-button> -->
        </span>
      </template>
    </el-dialog>
    
    <!-- 处理任务对话框 -->
    <el-dialog
      v-model="handleTaskDialogVisible"
      :title="'处理任务 - ' + getTaskTypeName(currentTask?.type)"
      width="40%"
    >
      <el-form :model="handleForm" label-width="100px" ref="handleFormRef">
        <el-form-item label="处理结果" prop="action" :rules="{ required: true, message: '请选择处理结果', trigger: 'change' }">
          <el-radio-group v-model="handleForm.action">
            <el-radio :label="'approve'" v-if="currentTask?.type !== 'reportHandling'">通过</el-radio>
            <el-radio :label="'reject'" v-if="currentTask?.type !== 'reportHandling'">驳回</el-radio>
            <el-radio :label="'delete'" v-if="currentTask?.type === 'reportHandling'">删除内容</el-radio>
            <el-radio :label="'ignore'" v-if="currentTask?.type === 'reportHandling'">忽略举报</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="处理原因" prop="reason" :rules="{ required: true, message: '请填写处理原因', trigger: 'blur' }">
          <el-input v-model="handleForm.reason" type="textarea" :rows="4" placeholder="请填写处理原因或说明"></el-input>
        </el-form-item>
        
        <el-form-item label="扣除行为分" prop="behaviorScore" v-if="currentTask?.type === 'reportHandling'">
          <el-input-number v-model="handleForm.behaviorScore" :min="0" :max="20" :step="5"></el-input-number>
          <div class="score-hint">*不当行为将扣除学生行为分，扣分范围0-20分</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleTaskDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTaskHandle">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, Briefcase, Star, ChatDotRound } from '@element-plus/icons-vue'
import { useCounselorStore } from '@/store/counselorStore'

// 使用Pinia store
const counselorStore = useCounselorStore()
const router = useRouter()

// 页面状态
const taskType = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const taskDetailDialogVisible = ref(false)
const handleTaskDialogVisible = ref(false)
const currentTask = ref(null)
const refreshInterval = ref(null)

// 处理任务表单
const handleForm = ref({
  action: '',
  reason: '',
  behaviorScore: 0
})
const handleFormRef = ref(null)

// 使用Pinia store中的数据
const dashboard = computed(() => counselorStore.dashboard)
const loading = computed(() => counselorStore.loading.dashboard)

// 计算所有任务列表
const allTasks = computed(() => {
  // 合并所有类型的任务并标记类型
  const companyTasksWithType = counselorStore.companyTasks.map(task => ({
    ...task,
    type: 'companyCertification'
  }))
  
  const jobTasksWithType = counselorStore.jobTasks.map(task => ({
    ...task,
    type: 'jobAudit'
  }))
  
  const reportTasksWithType = counselorStore.reportTasks.map(task => ({
    ...task,
    type: 'reportHandling'
  }))
  
  return [...companyTasksWithType, ...jobTasksWithType, ...reportTasksWithType]
})

// 根据选择的类型过滤任务
const filteredTasks = computed(() => {
  if (taskType.value === 'all') {
    return allTasks.value
  } else if (taskType.value === 'company') {
    return allTasks.value.filter(task => task.type === 'companyCertification')
  } else if (taskType.value === 'job') {
    return allTasks.value.filter(task => task.type === 'jobAudit')
  } else if (taskType.value === 'report') {
    return allTasks.value.filter(task => task.type === 'reportHandling')
  }
  return allTasks.value
})

// 计算任务总数
const totalTasks = computed(() => filteredTasks.value.length)

// 跳转到企业认证页面
const goToCompanies = () => {
  router.push({
    path: '/counselor/companies',
    query: { 
      fromDashboard: true,
      status: 'pending'
    }
  })
}

// 跳转到岗位审核页面
const goToJobs = () => {
  router.push({
    path: '/counselor/jobs',
    query: { 
      fromDashboard: true,
      status: 'pending'
    }
  })
}

// 跳转到举报处理页面
const goToReports = (reportType) => {
  router.push({
    path: '/counselor/reports',
    query: { 
      fromDashboard: true,
      type: reportType,
      status: 'pending'
    }
  })
}

// 处理任务类型变更
const handleTypeChange = () => {
  currentPage.value = 1
  loadTasks()
}

// 查看任务详情
const viewTaskDetail = (row) => {
  currentTask.value = {
    ...row,
    targetName: row.title || row.companyName || '未知任务'
  }

  // 根据任务类型，将相应的详情数据附加到currentTask.details中
  if (row.type === 'jobAudit' && row.jobDetails) {
    currentTask.value.details = row.jobDetails
  } else if (row.type === 'companyCertification' && row.companyDetails) {
    currentTask.value.details = row.companyDetails
  } else if (row.type === 'reportHandling') {
    // 处理举报详情数据
    currentTask.value.details = {
      // 此处可根据实际举报数据结构进行处理
      reporter: row.reporter || '匿名用户',
      reason: row.reportReason || '未提供原因',
      reportTime: row.reportedAt || new Date().toLocaleString(),
      reportedItemType: row.reportedItemType || 'unknown',
      // 根据举报类型设置其他属性
    }
  }

  taskDetailDialogVisible.value = true
}

// 处理任务
const handleTask = (task) => {
  // 根据任务类型跳转到对应页面处理
  if (task.type === 'companyCertification') {
    router.push({
      path: '/counselor/companies',
      query: { 
        taskId: task.id,
        fromDashboard: true
      }
    })
  } else if (task.type === 'jobAudit') {
    router.push({
      path: '/counselor/jobs',
      query: { 
        taskId: task.id,
        fromDashboard: true
      }
    })
  } else if (task.type === 'reportHandling') {
    router.push({
      path: '/counselor/reports',
      query: { 
        taskId: task.id,
        fromDashboard: true
      }
    })
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadTasks()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadTasks()
}

// 获取任务类型标签颜色
const getTaskTypeTag = (type) => {
  switch (type) {
    case 'companyCertification': return 'success'
    case 'jobAudit': return 'primary'
    case 'reportHandling': return 'danger'
    default: return 'info'
  }
}

// 获取任务类型名称
const getTaskTypeName = (type) => {
  switch (type) {
    case 'companyCertification': return '企业认证'
    case 'jobAudit': return '岗位审核'
    case 'reportHandling': return '举报处理'
    default: return '未知类型'
  }
}

// 获取优先级标签颜色
const getPriorityTag = (priority) => {
  switch (priority) {
    case 'high': return 'danger'
    case 'normal': return 'warning'
    case 'low': return 'info'
    default: return 'info'
  }
}

// 获取优先级名称
const getPriorityName = (priority) => {
  switch (priority) {
    case 'high': return '高'
    case 'normal': return '中'
    case 'low': return '低'
    default: return '中'
  }
}

// 加载任务数据
const loadTasks = async () => {
  // 根据当前选中的任务类型加载对应数据
  if (taskType.value === 'company' || taskType.value === 'all') {
    await counselorStore.fetchCompanyTasks({
      page: currentPage.value,
      pageSize: pageSize.value
    })
  }
  
  if (taskType.value === 'job' || taskType.value === 'all') {
    await counselorStore.fetchJobTasks({
      page: currentPage.value,
      pageSize: pageSize.value
    })
  }
  
  if (taskType.value === 'report' || taskType.value === 'all') {
    await counselorStore.fetchReportTasks({
      page: currentPage.value,
      pageSize: pageSize.value
    })
  }
}

// 刷新所有任务数据
const refreshAllData = async () => {
  await counselorStore.fetchDashboardData()
  await loadTasks()
}

// 页面挂载时加载数据
onMounted(() => {
  refreshAllData()
  
  // 设置自动刷新（每5分钟刷新一次）
  refreshInterval.value = setInterval(() => {
    refreshAllData()
  }, 5 * 60 * 1000)
})

// 页面销毁前清除定时器
onBeforeUnmount(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
  }
})

// 监听查询参数变化，重新加载数据
watch(
  () => [taskType.value, currentPage.value, pageSize.value],
  () => {
    loadTasks()
  }
)

// 从对话框中处理任务
const handleTaskFromDialog = () => {
  taskDetailDialogVisible.value = false
  handleTaskDialogVisible.value = true
}

// 提交任务处理
const submitTaskHandle = async () => {
  if (!handleFormRef.value) return
  
  await handleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 根据任务类型提交不同的处理请求
        if (currentTask.value.type === 'companyCertification') {
          await counselorStore.handleCompanyTask({
            taskId: currentTask.value.id,
            action: handleForm.value.action,
            reason: handleForm.value.reason
          })
        } else if (currentTask.value.type === 'jobAudit') {
          await counselorStore.handleJobTask({
            taskId: currentTask.value.id,
            action: handleForm.value.action,
            reason: handleForm.value.reason
          })
        } else if (currentTask.value.type === 'reportHandling') {
          await counselorStore.handleReportTask({
            taskId: currentTask.value.id,
            action: handleForm.value.action,
            reason: handleForm.value.reason,
            behaviorScore: handleForm.value.behaviorScore
          })
        }
        
        ElMessage.success('任务处理成功')
        handleTaskDialogVisible.value = false
        
        // 重置表单
        handleForm.value = {
          action: '',
          reason: '',
          behaviorScore: 0
        }
        
        // 刷新任务列表
        refreshAllData()
      } catch (error) {
        ElMessage.error('处理失败：' + (error.message || '未知错误'))
      }
    }
  })
}

// 获取认证状态
const getCertificationStatus = (status) => {
  switch (status) {
    case 'certified': return '已认证'
    case 'pending': return '认证中'
    case 'rejected': return '认证未通过'
    case 'expired': return '已过期'
    default: return '未知状态'
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.task-overview {
  margin-bottom: 20px;
}

.task-card {
  height: 180px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-radius: 14px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.06);
  background: #fff;
  transition: box-shadow 0.3s, transform 0.3s;
}

.task-card:hover {
  transform: translateY(-6px) scale(1.03);
  box-shadow: 0 8px 24px 0 rgba(64,158,255,0.18);
}

.task-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 10px;
}

.task-title {
  font-size: 17px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.task-count {
  font-size: 44px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 2px;
  letter-spacing: 2px;
}

.task-icon {
  position: absolute;
  right: 18px;
  top: 18px;
  font-size: 32px;
  color: #e4e7ed;
}

.task-action {
  margin-top: 10px;
  align-self: center;
  border-radius: 6px;
  font-size: 15px;
  padding: 4px 18px;
}

.disabled-card {
  opacity: 0.6;
  filter: grayscale(0.2);
}

.pending-tasks {
  margin-bottom: 20px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-header h3 {
  margin: 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.task-detail {
  margin-bottom: 20px;
}

.task-specific-details {
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 5px;
}

.document-preview, .job-description, .job-requirements, .report-content {
  margin-top: 15px;
}

.score-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 