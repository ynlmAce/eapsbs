<template>
  <div class="dashboard">
    <h2 class="page-title">辅导员工作台</h2>
    
    <!-- 任务概览 -->
    <el-row :gutter="20" class="task-overview">
      <el-col :span="6">
        <el-card shadow="hover" class="task-card">
          <div class="task-info">
            <div class="task-title">企业认证</div>
            <div class="task-count">{{ dashboardData.companyCertTasks }}</div>
          </div>
          <div class="task-icon">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <el-button 
            type="primary" 
            plain 
            size="small" 
            class="task-action"
            :disabled="dashboardData.companyCertTasks === 0"
            @click="goToCompanies"
          >处理</el-button>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="task-card">
          <div class="task-info">
            <div class="task-title">岗位审核</div>
            <div class="task-count">{{ dashboardData.jobAuditTasks }}</div>
          </div>
          <div class="task-icon">
            <el-icon><Briefcase /></el-icon>
          </div>
          <el-button 
            type="primary" 
            plain 
            size="small" 
            class="task-action"
            :disabled="dashboardData.jobAuditTasks === 0"
            @click="goToJobs"
          >处理</el-button>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="task-card">
          <div class="task-info">
            <div class="task-title">评价举报</div>
            <div class="task-count">{{ dashboardData.reportedRatings }}</div>
          </div>
          <div class="task-icon">
            <el-icon><Star /></el-icon>
          </div>
          <el-button 
            type="primary" 
            plain 
            size="small" 
            class="task-action"
            :disabled="dashboardData.reportedRatings === 0"
            @click="goToReports('rating')"
          >处理</el-button>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="task-card">
          <div class="task-info">
            <div class="task-title">消息举报</div>
            <div class="task-count">{{ dashboardData.reportedMessages }}</div>
          </div>
          <div class="task-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <el-button 
            type="primary" 
            plain 
            size="small" 
            class="task-action"
            :disabled="dashboardData.reportedMessages === 0"
            @click="goToReports('message')"
          >处理</el-button>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 待处理任务列表 -->
    <el-card class="pending-tasks">
      <template #header>
        <div class="task-header">
          <h3>待处理任务</h3>
          <el-radio-group v-model="taskType" size="small">
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
      >
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="type" label="任务类型" width="110">
          <template #default="scope">
            <el-tag :type="getTaskTypeTag(scope.row.type)">
              {{ getTaskTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetName" label="目标名称" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityTag(scope.row.priority)">
              {{ getPriorityName(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewTaskDetail(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleTask(scope.row)">处理</el-button>
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
          <div v-if="currentTask.type === 'company_cert'" class="task-specific-details">
            <h4>企业资质信息</h4>
            <p>企业名称：{{ currentTask.details?.name }}</p>
            <p>统一社会信用代码：{{ currentTask.details?.code }}</p>
            <p>行业：{{ currentTask.details?.industry }}</p>
            <p>企业规模：{{ currentTask.details?.size }}</p>
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
          </div>
          
          <!-- 岗位审核详情 -->
          <div v-if="currentTask.type === 'job_audit'" class="task-specific-details">
            <h4>岗位信息</h4>
            <p>岗位名称：{{ currentTask.details?.title }}</p>
            <p>所属企业：{{ currentTask.details?.company }}</p>
            <p>工作地点：{{ currentTask.details?.location }}</p>
            <p>薪资范围：{{ currentTask.details?.salary }}</p>
            <p>招聘人数：{{ currentTask.details?.count }}人</p>
            <div class="job-description">
              <p>岗位描述：</p>
              <p>{{ currentTask.details?.description }}</p>
            </div>
            <div class="job-requirements">
              <p>岗位要求：</p>
              <p>{{ currentTask.details?.requirements }}</p>
            </div>
          </div>
          
          <!-- 举报处理详情 -->
          <div v-if="currentTask.type === 'rating_report' || currentTask.type === 'message_report'" class="task-specific-details">
            <h4>举报信息</h4>
            <p>举报人：{{ currentTask.details?.reporter }}</p>
            <p>举报原因：{{ currentTask.details?.reason }}</p>
            <p>举报时间：{{ currentTask.details?.reportTime }}</p>
            
            <div v-if="currentTask.type === 'rating_report'" class="report-content">
              <h4>被举报的评价内容</h4>
              <p>评价对象：{{ currentTask.details?.targetName }}</p>
              <p>评分：{{ currentTask.details?.score }}分</p>
              <p>评价内容：{{ currentTask.details?.content }}</p>
            </div>
            
            <div v-if="currentTask.type === 'message_report'" class="report-content">
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
          <el-button type="primary" @click="handleTaskFromDialog">处理此任务</el-button>
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
            <el-radio :label="'approve'" v-if="currentTask?.type !== 'rating_report' && currentTask?.type !== 'message_report'">通过</el-radio>
            <el-radio :label="'reject'" v-if="currentTask?.type !== 'rating_report' && currentTask?.type !== 'message_report'">驳回</el-radio>
            <el-radio :label="'delete'" v-if="currentTask?.type === 'rating_report' || currentTask?.type === 'message_report'">删除内容</el-radio>
            <el-radio :label="'ignore'" v-if="currentTask?.type === 'rating_report' || currentTask?.type === 'message_report'">忽略举报</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="处理原因" prop="reason" :rules="{ required: true, message: '请填写处理原因', trigger: 'blur' }">
          <el-input v-model="handleForm.reason" type="textarea" :rows="4" placeholder="请填写处理原因或说明"></el-input>
        </el-form-item>
        
        <el-form-item label="扣除行为分" prop="behaviorScore" v-if="currentTask?.type === 'rating_report' || currentTask?.type === 'message_report'">
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, Briefcase, Star, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()

// 仪表盘数据
const dashboardData = reactive({
  companyCertTasks: 0,
  jobAuditTasks: 0,
  reportedRatings: 0,
  reportedMessages: 0
})

// 任务列表相关
const loading = ref(false)
const tasks = ref([])
const taskType = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const totalTasks = ref(0)

// 任务详情对话框
const taskDetailDialogVisible = ref(false)
const currentTask = ref(null)

// 处理任务对话框
const handleTaskDialogVisible = ref(false)
const handleFormRef = ref(null)
const handleForm = reactive({
  action: '',
  reason: '',
  behaviorScore: 0
})

// 根据选择的任务类型过滤任务列表
const filteredTasks = computed(() => {
  if (taskType.value === 'all') {
    return tasks.value
  } else if (taskType.value === 'company') {
    return tasks.value.filter(task => task.type === 'company_cert')
  } else if (taskType.value === 'job') {
    return tasks.value.filter(task => task.type === 'job_audit')
  } else if (taskType.value === 'report') {
    return tasks.value.filter(task => task.type === 'rating_report' || task.type === 'message_report')
  }
  return tasks.value
})

// 获取任务类型名称
const getTaskTypeName = (type) => {
  switch (type) {
    case 'company_cert':
      return '企业认证'
    case 'job_audit':
      return '岗位审核'
    case 'rating_report':
      return '评价举报'
    case 'message_report':
      return '消息举报'
    default:
      return '未知类型'
  }
}

// 获取任务类型对应的标签类型
const getTaskTypeTag = (type) => {
  switch (type) {
    case 'company_cert':
      return 'success'
    case 'job_audit':
      return 'primary'
    case 'rating_report':
      return 'warning'
    case 'message_report':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取优先级名称
const getPriorityName = (priority) => {
  switch (priority) {
    case 'high':
      return '高'
    case 'medium':
      return '中'
    case 'low':
      return '低'
    default:
      return '普通'
  }
}

// 获取优先级对应的标签类型
const getPriorityTag = (priority) => {
  switch (priority) {
    case 'high':
      return 'danger'
    case 'medium':
      return 'warning'
    case 'low':
      return 'info'
    default:
      return ''
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchDashboardData()
  fetchTasks()
})

// 获取仪表盘数据
const fetchDashboardData = () => {
  // 模拟获取数据
  setTimeout(() => {
    dashboardData.companyCertTasks = Math.floor(Math.random() * 10)
    dashboardData.jobAuditTasks = Math.floor(Math.random() * 15)
    dashboardData.reportedRatings = Math.floor(Math.random() * 8)
    dashboardData.reportedMessages = Math.floor(Math.random() * 5)
  }, 500)

  /**
   * TODO: 实际实现时从API获取仪表盘数据
   * const fetchData = async () => {
   *   try {
   *     const res = await api.counselor.getDashboardData()
   *     if (res.success) {
   *       Object.assign(dashboardData, res.data)
   *     }
   *   } catch (error) {
   *     console.error('获取仪表盘数据失败:', error)
   *     ElMessage.error('获取数据失败，请刷新重试')
   *   }
   * }
   * fetchData()
   */
}

// 获取任务列表
const fetchTasks = () => {
  loading.value = true
  
  // 模拟获取数据
  setTimeout(() => {
    const mockTasks = []
    
    // 生成随机任务数据
    for (let i = 1; i <= 25; i++) {
      const types = ['company_cert', 'job_audit', 'rating_report', 'message_report']
      const priorities = ['high', 'medium', 'low', 'normal']
      const type = types[Math.floor(Math.random() * types.length)]
      
      let targetName = ''
      switch (type) {
        case 'company_cert':
          targetName = '示例科技有限公司' + i
          break
        case 'job_audit':
          targetName = '软件工程师岗位' + i
          break
        case 'rating_report':
          targetName = '对XX公司的评价'
          break
        case 'message_report':
          targetName = '聊天消息举报'
          break
      }
      
      mockTasks.push({
        id: 10000 + i,
        type,
        targetName,
        createdAt: new Date(Date.now() - Math.random() * 10 * 24 * 60 * 60 * 1000).toLocaleString(),
        priority: priorities[Math.floor(Math.random() * priorities.length)],
        description: '这是任务的详细描述信息...',
        details: {
          // 根据任务类型生成不同的详情
          name: type === 'company_cert' ? targetName : '',
          code: type === 'company_cert' ? '9111000012345678' + i : '',
          industry: type === 'company_cert' ? 'IT/互联网/通信' : '',
          size: type === 'company_cert' ? '200-500人' : '',
          licenseUrl: type === 'company_cert' ? 'https://example.com/license.jpg' : '',
          
          title: type === 'job_audit' ? targetName : '',
          company: type === 'job_audit' ? '示例科技有限公司' : '',
          location: type === 'job_audit' ? '北京市海淀区' : '',
          salary: type === 'job_audit' ? '15k-25k' : '',
          count: type === 'job_audit' ? Math.floor(Math.random() * 10) + 1 : 0,
          description: type === 'job_audit' ? '岗位职责：\n1. 负责公司产品的设计和开发\n2. 优化产品性能和用户体验' : '',
          requirements: type === 'job_audit' ? '任职要求：\n1. 计算机相关专业本科及以上学历\n2. 熟悉Java或Python编程语言' : '',
          
          reporter: type === 'rating_report' || type === 'message_report' ? '学生用户' + Math.floor(Math.random() * 1000) : '',
          reason: type === 'rating_report' ? '评价内容含有虚假信息' : type === 'message_report' ? '消息内容含有骚扰信息' : '',
          reportTime: type === 'rating_report' || type === 'message_report' ? new Date(Date.now() - Math.random() * 5 * 24 * 60 * 60 * 1000).toLocaleString() : '',
          targetName: type === 'rating_report' ? '示例科技有限公司' : '',
          score: type === 'rating_report' ? Math.floor(Math.random() * 3) + 1 : 0,
          content: type === 'rating_report' ? '这家公司非常糟糕，面试过程不专业...' : type === 'message_report' ? '你好，可以加我微信吗？我们私下聊聊...' : '',
          sender: type === 'message_report' ? '企业用户' + Math.floor(Math.random() * 100) : '',
          sendTime: type === 'message_report' ? new Date(Date.now() - Math.random() * 5 * 24 * 60 * 60 * 1000).toLocaleString() : ''
        }
      })
    }
    
    tasks.value = mockTasks.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
    totalTasks.value = mockTasks.length
    loading.value = false
  }, 1000)

  /**
   * TODO: 实际实现时从API获取任务列表
   * const fetchTaskList = async () => {
   *   try {
   *     loading.value = true
   *     const res = await api.counselor.getTasks({
   *       type: taskType.value !== 'all' ? taskType.value : null,
   *       page: currentPage.value,
   *       pageSize: pageSize.value
   *     })
   *     if (res.success) {
   *       tasks.value = res.data.list
   *       totalTasks.value = res.data.total
   *     }
   *   } catch (error) {
   *     console.error('获取任务列表失败:', error)
   *     ElMessage.error('获取任务列表失败，请刷新重试')
   *   } finally {
   *     loading.value = false
   *   }
   * }
   * fetchTaskList()
   */
}

// 处理每页展示数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTasks()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTasks()
}

// 查看任务详情
const viewTaskDetail = (task) => {
  currentTask.value = task
  taskDetailDialogVisible.value = true
}

// 处理任务
const handleTask = (task) => {
  currentTask.value = task
  handleTaskDialogVisible.value = true
  
  // 根据任务类型设置默认的处理结果
  if (task.type === 'rating_report' || task.type === 'message_report') {
    handleForm.action = 'delete'
    handleForm.behaviorScore = 5
  } else {
    handleForm.action = 'approve'
  }
  
  handleForm.reason = ''
}

// 从详情对话框处理任务
const handleTaskFromDialog = () => {
  taskDetailDialogVisible.value = false
  handleTask(currentTask.value)
}

// 提交任务处理结果
const submitTaskHandle = async () => {
  const valid = await handleFormRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  
  // 模拟提交处理结果
  setTimeout(() => {
    ElMessage.success('任务处理成功')
    handleTaskDialogVisible.value = false
    
    // 从列表中移除已处理的任务
    const index = tasks.value.findIndex(t => t.id === currentTask.value.id)
    if (index !== -1) {
      tasks.value.splice(index, 1)
    }
    
    // 更新仪表盘数据
    if (currentTask.value.type === 'company_cert') {
      dashboardData.companyCertTasks--
    } else if (currentTask.value.type === 'job_audit') {
      dashboardData.jobAuditTasks--
    } else if (currentTask.value.type === 'rating_report') {
      dashboardData.reportedRatings--
    } else if (currentTask.value.type === 'message_report') {
      dashboardData.reportedMessages--
    }
  }, 1000)

  /**
   * TODO: 实际实现时调用API提交任务处理结果
   * try {
   *   const res = await api.counselor.handleTask({
   *     taskId: currentTask.value.id,
   *     action: handleForm.action,
   *     reason: handleForm.reason,
   *     behaviorScore: handleForm.behaviorScore || 0
   *   })
   *   
   *   if (res.success) {
   *     ElMessage.success('任务处理成功')
   *     handleTaskDialogVisible.value = false
   *     
   *     // 从列表中移除已处理的任务
   *     const index = tasks.value.findIndex(t => t.id === currentTask.value.id)
   *     if (index !== -1) {
   *       tasks.value.splice(index, 1)
   *     }
   *     
   *     // 更新仪表盘数据
   *     if (currentTask.value.type === 'company_cert') {
   *       dashboardData.companyCertTasks--
   *     } else if (currentTask.value.type === 'job_audit') {
   *       dashboardData.jobAuditTasks--
   *     } else if (currentTask.value.type === 'rating_report') {
   *       dashboardData.reportedRatings--
   *     } else if (currentTask.value.type === 'message_report') {
   *       dashboardData.reportedMessages--
   *     }
   *     
   *     // 如果当前页没有数据了，回到上一页
   *     if (tasks.value.length === 0 && currentPage.value > 1) {
   *       currentPage.value--
   *     }
   *     
   *     // 重新获取任务列表
   *     fetchTasks()
   *   } else {
   *     ElMessage.error(res.message || '处理失败')
   *   }
   * } catch (error) {
   *   console.error('处理任务失败:', error)
   *   ElMessage.error('系统错误，请稍后重试')
   * }
   */
}

// 导航到企业管理页面
const goToCompanies = () => {
  router.push('/counselor/companies')
}

// 导航到岗位审核页面
const goToJobs = () => {
  router.push('/counselor/jobs')
}

// 导航到举报处理页面
const goToReports = (type) => {
  router.push({
    path: '/counselor/reports',
    query: { type }
  })
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
  height: 160px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: all 0.3s;
}

.task-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
}

.task-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.task-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.task-count {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
}

.task-icon {
  position: absolute;
  right: 20px;
  top: 20px;
  font-size: 30px;
  color: #dcdfe6;
}

.task-action {
  margin-top: 15px;
  align-self: center;
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