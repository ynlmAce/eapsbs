<template>
  <div class="companies-page">
    <h2 class="page-title">企业认证管理</h2>
    
    <!-- 筛选面板 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="企业名称">
          <el-input v-model="filterForm.name" placeholder="请输入" clearable></el-input>
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable>
            <el-option label="待审核" value="pending"></el-option>
            <el-option label="已认证" value="certified"></el-option>
            <el-option label="已驳回" value="rejected"></el-option>
            <el-option label="已过期" value="expired"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 企业列表 -->
    <el-card class="company-list-card">
      <template #header>
        <div class="card-header">
          <span>企业列表</span>
          <div class="tabs">
            <el-radio-group v-model="activeTab" size="small" @change="handleTabChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="pending">待审核</el-radio-button>
              <el-radio-button label="certified">已认证</el-radio-button>
              <el-radio-button label="rejected">已驳回</el-radio-button>
              <el-radio-button label="expired">已过期</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      
      <el-table
        :data="filteredCompanyList"
        v-loading="loading"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="企业名称" min-width="150"></el-table-column>
        <el-table-column prop="industry" label="所属行业" min-width="120"></el-table-column>
        <el-table-column prop="size" label="企业规模" width="120"></el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180"></el-table-column>
        <el-table-column prop="status" label="认证状态" width="100">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.status)"
              size="small"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="certificationDate" label="认证时间" width="180">
          <template #default="scope">
            {{ scope.row.certificationDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="认证到期" width="180">
          <template #default="scope">
            {{ scope.row.expiryDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              plain
              @click.stop="viewCompany(scope.row)"
            >查看</el-button>
            <el-button 
              v-if="scope.row.status === 'pending'"
              size="small" 
              type="success" 
              plain
              @click.stop="handleCertification(scope.row, 'approve')"
            >通过</el-button>
            <el-button 
              v-if="scope.row.status === 'pending'"
              size="small" 
              type="danger" 
              plain
              @click.stop="handleCertification(scope.row, 'reject')"
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
          :total="totalCompanies"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 企业详情对话框 -->
    <el-dialog
      v-model="companyDetailVisible"
      title="企业详情"
      width="60%"
      destroy-on-close
    >
      <template v-if="currentCompany">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="企业名称">{{ currentCompany.name }}</el-descriptions-item>
          <el-descriptions-item label="统一社会信用代码">{{ currentCompany.code }}</el-descriptions-item>
          <el-descriptions-item label="所属行业">{{ currentCompany.industry }}</el-descriptions-item>
          <el-descriptions-item label="企业规模">{{ currentCompany.size }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ currentCompany.address }}</el-descriptions-item>
          <el-descriptions-item label="HR联系人">{{ currentCompany.hrContact }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ currentCompany.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentCompany.email }}</el-descriptions-item>
          <el-descriptions-item label="认证状态">
            <el-tag :type="getStatusType(currentCompany.status)">
              {{ getStatusText(currentCompany.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="公司简介" :span="2">
            {{ currentCompany.description || '暂无简介' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="company-logo">
          <h4>企业Logo</h4>
          <el-image
            v-if="currentCompany.logoUrl"
            :src="currentCompany.logoUrl"
            :preview-src-list="[currentCompany.logoUrl]"
            fit="contain"
            style="width: 200px; height: 100px;"
          ></el-image>
          <el-empty v-else description="暂无Logo" :image-size="100"></el-empty>
        </div>
        
        <div class="company-license">
          <h4>营业执照</h4>
          <el-image
            v-if="currentCompany.licenseUrl"
            :src="currentCompany.licenseUrl"
            :preview-src-list="[currentCompany.licenseUrl]"
            fit="contain"
            style="width: 100%; max-height: 300px;"
          ></el-image>
          <el-empty v-else description="暂无营业执照" :image-size="100"></el-empty>
        </div>
        
        <div v-if="currentCompany.status === 'pending'" class="dialog-footer">
          <el-form :model="certForm" ref="certFormRef" label-width="120px">
            <el-form-item label="认证有效期" prop="expiryDate" :rules="[{ required: true, message: '请选择有效期', trigger: 'change' }]">
              <el-date-picker
                v-model="certForm.expiryDate"
                type="date"
                placeholder="选择有效期"
                :disabled-date="disabledDate"
              ></el-date-picker>
            </el-form-item>
            <el-form-item label="审核意见" prop="remarks">
              <el-input
                v-model="certForm.remarks"
                type="textarea"
                :rows="3"
                placeholder="请输入审核意见"
              ></el-input>
            </el-form-item>
          </el-form>
          <div class="dialog-buttons">
            <el-button @click="companyDetailVisible = false">取消</el-button>
            <el-button type="success" @click="handleCertification(currentCompany, 'approve')">
              通过认证
            </el-button>
            <el-button type="danger" @click="handleCertification(currentCompany, 'reject')">
              驳回申请
            </el-button>
          </div>
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
const totalCompanies = ref(0)
const companyDetailVisible = ref(false)
const currentCompany = ref(null)

// 筛选表单
const filterForm = reactive({
  name: '',
  status: ''
})

// 认证表单
const certForm = reactive({
  expiryDate: '',
  remarks: ''
})

const certFormRef = ref(null)

// 使用store中的loading状态和企业任务列表
const loading = computed(() => counselorStore.loading.companies)
const companyTasks = computed(() => counselorStore.companyTasks)

// 根据筛选条件和Tab过滤企业列表
const filteredCompanyList = computed(() => {
  let result = [...companyTasks.value]
  
  // 根据活动的Tab过滤
  if (activeTab.value !== 'all') {
    result = result.filter(company => company.status === activeTab.value)
  }
  
  // 根据过滤表单过滤
  if (filterForm.name) {
    result = result.filter(company => 
      company.name && company.name.toLowerCase().includes(filterForm.name.toLowerCase())
    )
  }
  
  if (filterForm.status) {
    result = result.filter(company => company.status === filterForm.status)
  }
  
  return result
})

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'certified': return 'success'
    case 'rejected': return 'danger'
    case 'expired': return 'info'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待审核'
    case 'certified': return '已认证'
    case 'rejected': return '已驳回'
    case 'expired': return '已过期'
    default: return '未知状态'
  }
}

// 日期选择器限制日期
const disabledDate = (time) => {
  // 禁用今天之前的日期
  return time.getTime() < Date.now() - 8.64e7
}

// 处理查询
const handleSearch = () => {
  currentPage.value = 1
  loadCompanyTasks()
}

// 重置过滤条件
const resetFilter = () => {
  filterForm.name = ''
  filterForm.status = ''
  handleSearch()
}

// 处理Tab切换
const handleTabChange = () => {
  currentPage.value = 1
  loadCompanyTasks()
}

// 处理每页数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  loadCompanyTasks()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadCompanyTasks()
}

// 行点击
const handleRowClick = (row) => {
  viewCompany(row)
}

// 查看企业详情
const viewCompany = (company) => {
  currentCompany.value = company
  companyDetailVisible.value = true
}

// 处理认证/驳回操作
const handleCertification = async (company, action) => {
  // 确保有当前企业
  if (!company) return
  
  // 如果是通过认证，需要验证表单
  if (action === 'approve') {
    // 验证是否已选择有效期
    if (!certForm.expiryDate) {
      ElMessage.warning('请选择认证有效期')
      return
    }
  }
  
  try {
    // 确认操作
    const confirmMessage = action === 'approve' 
      ? '确定通过该企业的认证申请吗？' 
      : '确定驳回该企业的认证申请吗？'
      
    await ElMessageBox.confirm(confirmMessage, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: action === 'approve' ? 'success' : 'warning'
    })
    
    // 调用store的processTask方法处理任务
    const result = await counselorStore.processTask({
      taskId: company.id,
      type: 'companyCertification',
      action,
      reason: certForm.remarks,
      notes: action === 'approve' ? `认证有效期至${certForm.expiryDate}` : undefined
    })
    
    if (result.success) {
      ElMessage.success(action === 'approve' ? '企业认证已通过' : '企业认证已驳回')
      companyDetailVisible.value = false
      // 重新加载数据
      loadCompanyTasks()
      // 通知Dashboard更新
      counselorStore.fetchDashboardData()
    } else {
      ElMessage.error(result.message || '处理失败')
    }
  } catch (e) {
    // 用户取消操作，不做处理
    if (e !== 'cancel') {
      console.error('处理认证出错:', e)
      ElMessage.error('操作失败: ' + e.message)
    }
  }
}

// 加载企业认证任务
const loadCompanyTasks = async () => {
  try {
    const filters = {}
    
    // 根据当前tab状态添加筛选条件
    if (activeTab.value !== 'all') {
      filters.status = activeTab.value
    }
    
    // 添加搜索筛选
    if (filterForm.name) {
      filters.name = filterForm.name
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
    
    const result = await counselorStore.fetchCompanyTasks({
      page: currentPage.value,
      pageSize: pageSize.value,
      filters
    })
    
    totalCompanies.value = result.total || 0
    
    // 如果有特定的任务ID，自动打开该企业详情
    if (taskId && companyTasks.value.length > 0) {
      const targetCompany = companyTasks.value.find(c => c.id.toString() === taskId.toString())
      if (targetCompany) {
        viewCompany(targetCompany)
      }
    }
  } catch (error) {
    console.error('加载企业认证任务失败', error)
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
  loadCompanyTasks()
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
      loadCompanyTasks()
    }
  }
)
</script>

<style scoped>
.companies-page {
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

.company-list-card {
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

.company-logo,
.company-license {
  margin-top: 20px;
}

.company-logo h4,
.company-license h4 {
  margin-bottom: 10px;
  font-weight: bold;
  color: #303133;
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
</style> 