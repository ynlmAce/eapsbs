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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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

// 页码相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalJobs = ref(0)

// 表格数据
const jobList = ref([])
const loading = ref(false)
const activeTab = ref('pending')

// 岗位详情相关
const jobDetailVisible = ref(false)
const currentJob = ref(null)

// 过滤后的岗位列表
const filteredJobList = computed(() => {
  let list = [...jobList.value]
  
  // 根据选项卡筛选
  if (activeTab.value !== 'all') {
    list = list.filter(item => item.status === activeTab.value)
  }
  
  // 根据表单筛选
  if (filterForm.title) {
    list = list.filter(item => item.title.includes(filterForm.title))
  }
  
  if (filterForm.company) {
    list = list.filter(item => item.company.includes(filterForm.company))
  }
  
  if (filterForm.status) {
    list = list.filter(item => item.status === filterForm.status)
  }
  
  return list
})

// 获取状态文字
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已驳回',
    'closed': '已结束'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger',
    'closed': 'info'
  }
  return typeMap[status] || ''
}

// 获取岗位列表
const fetchJobList = async () => {
  loading.value = true
  try {
    // 模拟接口请求
    setTimeout(() => {
      // 模拟数据
      jobList.value = [
        {
          id: 1,
          title: '前端开发工程师',
          company: '腾讯科技有限公司',
          location: '深圳',
          salary: '15k-25k',
          count: 5,
          type: '全职',
          education: '本科及以上',
          experience: '1-3年',
          description: '负责公司Web前端项目的开发和维护工作，与后端工程师配合，实现产品前端功能和交互效果，提升用户体验。',
          requirements: '1. 计算机相关专业本科及以上学历；\n2. 熟悉HTML、CSS、JavaScript，熟悉Vue、React等前端框架；\n3. 了解常见的浏览器兼容性问题及解决方案；\n4. 具有良好的团队协作精神和沟通能力；\n5. 有大型Web应用开发经验者优先。',
          tags: ['Vue', 'JavaScript', 'HTML5', 'CSS3'],
          welfare: ['五险一金', '年终奖', '节日福利', '弹性工作制', '免费班车'],
          createdAt: '2023-06-10 09:30:45',
          deadline: '2023-09-10',
          status: 'pending',
          applications: 0
        },
        {
          id: 2,
          title: '后端开发工程师',
          company: '阿里巴巴集团',
          location: '杭州',
          salary: '20k-35k',
          count: 8,
          type: '全职',
          education: '本科及以上',
          experience: '3-5年',
          description: '负责公司服务端架构设计和API开发，保证系统高可用、高性能、可扩展。',
          requirements: '1. 计算机相关专业本科及以上学历；\n2. 熟悉Java或Go语言，熟悉Spring Boot框架；\n3. 熟悉MySQL、Redis等数据库；\n4. 了解分布式系统设计和微服务架构；\n5. 良好的问题分析和解决能力。',
          tags: ['Java', 'Spring Boot', 'MySQL', '微服务'],
          welfare: ['五险一金', '年终奖', '股票期权', '免费三餐', '团队旅游'],
          createdAt: '2023-06-12 14:20:30',
          deadline: '2023-08-30',
          status: 'approved',
          applications: 12
        },
        {
          id: 3,
          title: '人工智能算法工程师',
          company: '百度在线网络技术有限公司',
          location: '北京',
          salary: '30k-50k',
          count: 3,
          type: '全职',
          education: '硕士及以上',
          experience: '3年以上',
          description: '负责公司AI算法的研究和开发，推动AI技术在产品中的应用和落地。',
          requirements: '1. 计算机、数学相关专业硕士及以上学历；\n2. 熟悉机器学习和深度学习算法；\n3. 熟练掌握Python，熟悉TensorFlow或PyTorch框架；\n4. 具有NLP或计算机视觉项目经验；\n5. 有相关顶会论文发表者优先。',
          tags: ['Python', '机器学习', '深度学习', 'NLP'],
          welfare: ['五险一金', '年终奖', '餐补', '健身房', '学术交流'],
          createdAt: '2023-06-15 10:45:20',
          deadline: '2023-07-30',
          status: 'pending',
          applications: 0
        },
        {
          id: 4,
          title: '产品经理',
          company: '小米科技有限责任公司',
          location: '北京',
          salary: '15k-30k',
          count: 2,
          type: '全职',
          education: '本科及以上',
          experience: '2-5年',
          description: '负责公司产品的规划、设计和迭代，挖掘用户需求，提升用户体验。',
          requirements: '1. 计算机或设计相关专业本科及以上学历；\n2. 具有2年以上产品经理经验；\n3. 良好的沟通能力和逻辑思维能力；\n4. 熟悉互联网产品开发流程；\n5. 有ToB产品经验者优先。',
          tags: ['产品设计', '用户体验', '需求分析'],
          welfare: ['五险一金', '年终奖', '弹性工作', '免费健身'],
          createdAt: '2023-06-18 16:30:10',
          deadline: '2023-08-10',
          status: 'rejected',
          rejectReason: '岗位描述与要求不匹配，薪资范围与市场水平差异较大，请修改后重新提交',
          applications: 0
        },
        {
          id: 5,
          title: 'UI设计师',
          company: '字节跳动科技有限公司',
          location: '上海',
          salary: '12k-22k',
          count: 4,
          type: '全职',
          education: '大专及以上',
          experience: '1-3年',
          description: '负责公司产品的界面设计，打造优秀的视觉体验。',
          requirements: '1. 设计相关专业大专及以上学历；\n2. 熟练使用设计软件如Sketch、Figma、Adobe XD等；\n3. 具有良好的审美能力和色彩搭配能力；\n4. 理解用户体验设计原则；\n5. 有移动端设计经验者优先。',
          tags: ['UI设计', 'Figma', '用户体验', '移动端'],
          welfare: ['五险一金', '年终奖', '生日福利', '下午茶'],
          createdAt: '2023-06-20 11:20:40',
          deadline: '2023-09-05',
          status: 'closed',
          applications: 25
        }
      ]
      
      totalJobs.value = jobList.value.length
      loading.value = false
    }, 1000)
  } catch (error) {
    console.error('获取岗位列表失败:', error)
    ElMessage.error('获取岗位列表失败')
    loading.value = false
  }
}

// 切换选项卡
const handleTabChange = (tab) => {
  currentPage.value = 1
}

// 查看岗位详情
const viewJob = (job) => {
  currentJob.value = { ...job }
  jobDetailVisible.value = true
  
  // 重置审核表单
  auditForm.remarks = ''
  auditForm.problems = []
}

// 处理审核
const handleAudit = (job, action) => {
  const actionText = action === 'approve' ? '通过' : '驳回'
  const actionType = action === 'approve' ? 'success' : 'warning'
  
  // 如果是驳回但没有填写原因或选择问题，给出提示
  if (action === 'reject' && auditForm.remarks.trim() === '' && auditForm.problems.length === 0) {
    return ElMessage.warning('请填写驳回原因或选择问题')
  }
  
  ElMessageBox.confirm(
    `确定要${actionText}【${job.title}】的岗位吗？`,
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: actionType
    }
  ).then(() => {
    // 模拟API调用
    setTimeout(() => {
      // 更新本地数据
      const index = jobList.value.findIndex(item => item.id === job.id)
      if (index !== -1) {
        const updatedJob = { ...jobList.value[index] }
        if (action === 'approve') {
          updatedJob.status = 'approved'
        } else {
          updatedJob.status = 'rejected'
          // 合并问题和备注作为驳回原因
          let rejectReason = ''
          if (auditForm.problems.length > 0) {
            rejectReason += '问题：' + auditForm.problems.join('、') + '。 '
          }
          if (auditForm.remarks.trim() !== '') {
            rejectReason += '备注：' + auditForm.remarks
          }
          updatedJob.rejectReason = rejectReason
        }
        jobList.value[index] = updatedJob
      }
      
      ElMessage({
        type: actionType,
        message: `已${actionText}岗位`
      })
      
      jobDetailVisible.value = false
    }, 500)
  }).catch(() => {})
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 重置筛选
const resetFilter = () => {
  filterForm.title = ''
  filterForm.company = ''
  filterForm.status = ''
  handleSearch()
}

// 处理行点击
const handleRowClick = (row) => {
  viewJob(row)
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
  fetchJobList()
})
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