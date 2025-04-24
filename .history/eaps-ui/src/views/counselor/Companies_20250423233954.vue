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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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

// 页码相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalCompanies = ref(0)

// 表格数据
const companyList = ref([])
const loading = ref(false)
const activeTab = ref('pending')

// 企业详情相关
const companyDetailVisible = ref(false)
const currentCompany = ref(null)

// 过滤后的企业列表
const filteredCompanyList = computed(() => {
  let list = [...companyList.value]
  
  // 根据选项卡筛选
  if (activeTab.value !== 'all') {
    list = list.filter(item => item.status === activeTab.value)
  }
  
  // 根据表单筛选
  if (filterForm.name) {
    list = list.filter(item => item.name.includes(filterForm.name))
  }
  
  if (filterForm.status) {
    list = list.filter(item => item.status === filterForm.status)
  }
  
  return list
})

// 禁用日期（今天之前的日期）
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 今天之前的日期禁用
}

// 获取状态文字
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待审核',
    'certified': '已认证',
    'rejected': '已驳回',
    'expired': '已过期'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    'pending': 'warning',
    'certified': 'success',
    'rejected': 'danger',
    'expired': 'info'
  }
  return typeMap[status] || ''
}

// 获取企业列表
const fetchCompanyList = async () => {
  loading.value = true
  try {
    // 模拟接口请求
    setTimeout(() => {
      // 模拟数据
      companyList.value = [
        {
          id: 1,
          name: '腾讯科技有限公司',
          code: '91440300708461136T',
          industry: '互联网',
          size: '10000人以上',
          address: '深圳市南山区高新科技园区',
          hrContact: '张经理',
          contactPhone: '13800138000',
          email: 'hr@tencent.example.com',
          description: '腾讯是中国最大的互联网综合服务提供商之一，是中国服务用户最多的互联网企业之一。',
          logoUrl: 'https://mat1.gtimg.com/www/images/qq2012/qqlogo_1x.png',
          licenseUrl: 'https://example.com/license.jpg',
          status: 'certified',
          createdAt: '2023-01-15 10:30:45',
          certificationDate: '2023-01-20 14:22:36',
          expiryDate: '2024-01-20'
        },
        {
          id: 2,
          name: '阿里巴巴集团',
          code: '91330100716105852H',
          industry: '电子商务',
          size: '10000人以上',
          address: '杭州市余杭区文一西路969号',
          hrContact: '李经理',
          contactPhone: '13900139000',
          email: 'hr@alibaba.example.com',
          description: '阿里巴巴集团经营多项业务，且为所投资的公司提供行政支持。',
          logoUrl: 'https://img.alicdn.com/tfs/TB1Zv8_lxSYBuNjSspjXXX73VXa-390-63.png',
          licenseUrl: 'https://example.com/license2.jpg',
          status: 'pending',
          createdAt: '2023-04-22 16:30:45',
          certificationDate: null,
          expiryDate: null
        },
        {
          id: 3,
          name: '字节跳动科技有限公司',
          code: '91110108MA01GC9U3X',
          industry: '互联网',
          size: '10000人以上',
          address: '北京市海淀区中关村大街1号',
          hrContact: '王经理',
          contactPhone: '13700137000',
          email: 'hr@bytedance.example.com',
          description: '字节跳动是最早将人工智能应用于移动互联网场景的科技企业之一。',
          logoUrl: 'https://sf3-ttcdn-tos.pstatp.com/obj/ttfe/ATSX/mainland/Avatar.png',
          licenseUrl: 'https://example.com/license3.jpg',
          status: 'pending',
          createdAt: '2023-05-01 09:20:15',
          certificationDate: null,
          expiryDate: null
        },
        {
          id: 4,
          name: '百度在线网络技术有限公司',
          code: '91110000802100433B',
          industry: '互联网',
          size: '10000人以上',
          address: '北京市海淀区上地十街10号',
          hrContact: '赵经理',
          contactPhone: '13600136000',
          email: 'hr@baidu.example.com',
          description: '百度是拥有强大互联网基础的领先AI公司。',
          logoUrl: 'https://www.baidu.com/img/flexible/logo/pc/result.png',
          licenseUrl: 'https://example.com/license4.jpg',
          status: 'rejected',
          createdAt: '2023-03-12 14:10:25',
          certificationDate: null,
          expiryDate: null
        },
        {
          id: 5,
          name: '小米科技有限责任公司',
          code: '91110108551385082Q',
          industry: '电子设备制造',
          size: '10000人以上',
          address: '北京市海淀区清河中街68号',
          hrContact: '钱经理',
          contactPhone: '13500135000',
          email: 'hr@xiaomi.example.com',
          description: '小米公司正式成立于2010年4月，是一家以手机、智能硬件和IoT平台为核心的互联网公司。',
          logoUrl: 'https://cdn.cnbj1.fds.api.mi-img.com/mi.com-assets/shop/img/logo-mi2.png',
          licenseUrl: 'https://example.com/license5.jpg',
          status: 'expired',
          createdAt: '2023-01-05 11:20:35',
          certificationDate: '2023-01-10 09:15:30',
          expiryDate: '2023-07-10'
        }
      ]
      
      totalCompanies.value = companyList.value.length
      loading.value = false
    }, 1000)
  } catch (error) {
    console.error('获取企业列表失败:', error)
    ElMessage.error('获取企业列表失败')
    loading.value = false
  }
}

// 切换选项卡
const handleTabChange = (tab) => {
  currentPage.value = 1
}

// 查看企业详情
const viewCompany = (company) => {
  currentCompany.value = { ...company }
  companyDetailVisible.value = true
  
  // 重置认证表单
  certForm.expiryDate = ''
  certForm.remarks = ''
  
  // 设置默认有效期（一年后）
  const oneYearLater = new Date()
  oneYearLater.setFullYear(oneYearLater.getFullYear() + 1)
  certForm.expiryDate = oneYearLater
}

// 处理认证
const handleCertification = (company, action) => {
  if (action === 'approve' && !certForm.expiryDate) {
    return ElMessage.warning('请选择认证有效期')
  }
  
  const actionText = action === 'approve' ? '通过' : '驳回'
  const actionType = action === 'approve' ? 'success' : 'warning'
  
  ElMessageBox.confirm(
    `确定要${actionText}【${company.name}】的企业认证申请吗？`,
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
      const index = companyList.value.findIndex(item => item.id === company.id)
      if (index !== -1) {
        const updatedCompany = { ...companyList.value[index] }
        if (action === 'approve') {
          updatedCompany.status = 'certified'
          updatedCompany.certificationDate = new Date().toLocaleString()
          updatedCompany.expiryDate = certForm.expiryDate.toLocaleDateString()
        } else {
          updatedCompany.status = 'rejected'
        }
        companyList.value[index] = updatedCompany
      }
      
      ElMessage({
        type: actionType,
        message: `已${actionText}企业认证申请`
      })
      
      companyDetailVisible.value = false
    }, 500)
  }).catch(() => {})
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 重置筛选
const resetFilter = () => {
  filterForm.name = ''
  filterForm.status = ''
  handleSearch()
}

// 处理行点击
const handleRowClick = (row) => {
  viewCompany(row)
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
  fetchCompanyList()
})
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