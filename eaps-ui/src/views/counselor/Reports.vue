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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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

// 页码相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalReports = ref(0)

// 表格数据
const reportList = ref([])
const loading = ref(false)
const activeTab = ref('pending')

// 举报详情相关
const reportDetailVisible = ref(false)
const currentReport = ref(null)

// 过滤后的举报列表
const filteredReportList = computed(() => {
  let list = [...reportList.value]
  
  // 根据选项卡筛选
  if (activeTab.value !== 'all') {
    list = list.filter(item => item.status === activeTab.value)
  }
  
  // 根据表单筛选
  if (filterForm.type) {
    list = list.filter(item => item.type === filterForm.type)
  }
  
  if (filterForm.status) {
    list = list.filter(item => item.status === filterForm.status)
  }
  
  return list
})

// 获取类型文字
const getTypeText = (type) => {
  const typeMap = {
    'rating': '评价举报',
    'message': '消息举报'
  }
  return typeMap[type] || type
}

// 获取类型标签
const getTypeTag = (type) => {
  const tagMap = {
    'rating': 'warning',
    'message': 'info'
  }
  return tagMap[type] || ''
}

// 获取状态文字
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待处理',
    'resolved': '已处理'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    'pending': 'danger',
    'resolved': 'success'
  }
  return typeMap[status] || ''
}

// 获取举报列表
const fetchReportList = async () => {
  loading.value = true
  try {
    // 模拟接口请求
    setTimeout(() => {
      // 模拟数据
      reportList.value = [
        {
          id: 1,
          type: 'rating',
          reporter: '张同学',
          targetInfo: '对"腾讯科技有限公司"的评价',
          reason: '评价内容虚假，不符合实际情况',
          createdAt: '2023-07-15 10:30:45',
          status: 'pending',
          targetDetails: {
            company: '腾讯科技有限公司',
            score: 1.5,
            content: '公司面试流程混乱，HR 态度恶劣，承诺的面试福利也没有兑现，不推荐大家投递。',
            createdAt: '2023-07-14 16:22:30'
          }
        },
        {
          id: 2,
          type: 'message',
          reporter: '李企业',
          targetInfo: '张同学在私信中发送违规内容',
          reason: '学生在私信中发送辱骂性言论',
          createdAt: '2023-07-16 14:20:15',
          status: 'pending',
          targetDetails: {
            sender: '张同学',
            content: '你们公司就是骗子，浪费我时间！@#¥%……&*（）',
            sentAt: '2023-07-16 14:15:30',
            session: '腾讯科技前端开发工程师岗位沟通'
          }
        },
        {
          id: 3,
          type: 'rating',
          reporter: '王企业',
          targetInfo: '对"阿里巴巴集团"的评价',
          reason: '学生从未参加我们公司的面试，却发布了负面评价',
          createdAt: '2023-07-17 09:45:10',
          status: 'pending',
          targetDetails: {
            company: '阿里巴巴集团',
            score: 2,
            content: '面试官迟到半小时，提的问题也很奇怪，没有问专业技能，全是些无关的问题。最后也没有任何反馈。',
            createdAt: '2023-07-17 09:30:20'
          }
        },
        {
          id: 4,
          type: 'message',
          reporter: '赵同学',
          targetInfo: '李企业在私信中发送骚扰内容',
          reason: '企业HR发送不适当的内容',
          createdAt: '2023-07-18 16:50:25',
          status: 'resolved',
          resolvedAt: '2023-07-19 10:15:40',
          resolution: '经查证，举报属实，已删除相关消息并对企业进行警告。',
          targetDetails: {
            sender: '李企业',
            content: '你的照片看起来很漂亮，可以加个微信私聊吗？工作以外我们可以多了解一下。',
            sentAt: '2023-07-18 16:45:15',
            session: '产品经理岗位沟通'
          }
        },
        {
          id: 5,
          type: 'rating',
          reporter: '钱企业',
          targetInfo: '对"百度在线网络技术有限公司"的评价',
          reason: '评价中包含虚假信息和诋毁内容',
          createdAt: '2023-07-20 11:30:05',
          status: 'resolved',
          resolvedAt: '2023-07-20 15:22:10',
          resolution: '经核实，评价内容确有不实之处，已删除该评价。',
          targetDetails: {
            company: '百度在线网络技术有限公司',
            score: 1,
            content: '这家公司完全是骗子，根本不会录用人，只是收集简历和个人信息，大家千万不要上当！',
            createdAt: '2023-07-20 10:15:30'
          }
        }
      ]
      
      totalReports.value = reportList.value.length
      loading.value = false
    }, 1000)
  } catch (error) {
    console.error('获取举报列表失败:', error)
    ElMessage.error('获取举报列表失败')
    loading.value = false
  }
}

// 切换选项卡
const handleTabChange = (tab) => {
  currentPage.value = 1
}

// 查看举报详情
const viewReport = (report) => {
  currentReport.value = { ...report }
  reportDetailVisible.value = true
  
  // 重置处理表单
  actionForm.remarks = ''
  actionForm.punishScore = false
  actionForm.scoreValue = 5
}

// 处理举报
const handleReportAction = (report, action) => {
  const actionText = action === 'ignore' ? '忽略' : '删除内容并惩罚'
  const confirmTitle = action === 'ignore' ? '确认忽略举报' : '确认删除内容'
  let confirmContent = `确定要${actionText}这条举报吗？`
  
  if (action === 'remove' && actionForm.punishScore) {
    confirmContent += `\n将扣除举报对象 ${actionForm.scoreValue} 分行为分。`
  }
  
  ElMessageBox.confirm(
    confirmContent,
    confirmTitle,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: action === 'ignore' ? 'info' : 'warning'
    }
  ).then(() => {
    // 模拟API调用
    setTimeout(() => {
      // 更新本地数据
      const index = reportList.value.findIndex(item => item.id === report.id)
      if (index !== -1) {
        const updatedReport = { ...reportList.value[index] }
        updatedReport.status = 'resolved'
        updatedReport.resolvedAt = new Date().toLocaleString()
        
        // 构建处理结果描述
        let resolution = action === 'ignore' ? '经审核，举报内容不构成违规，已忽略该举报。' : '经审核，举报属实，已删除相关内容'
        
        if (actionForm.remarks) {
          resolution += `\n备注：${actionForm.remarks}`
        }
        
        if (action === 'remove' && actionForm.punishScore) {
          resolution += `\n已扣除行为分 ${actionForm.scoreValue} 分。`
        }
        
        updatedReport.resolution = resolution
        reportList.value[index] = updatedReport
      }
      
      ElMessage.success('举报处理成功')
      reportDetailVisible.value = false
    }, 500)
  }).catch(() => {})
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 重置筛选
const resetFilter = () => {
  filterForm.type = ''
  filterForm.status = ''
  handleSearch()
}

// 处理行点击
const handleRowClick = (row) => {
  viewReport(row)
}

// 分页相关
const handleSizeChange = (size) => {
  pageSize.value = size
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 页面加载时获取数据
onMounted(() => {
  fetchReportList()
})
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