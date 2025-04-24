<template>
  <div class="applications-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>我的投递记录</h2>
          <div class="filters">
            <el-select v-model="statusFilter" placeholder="状态筛选" clearable>
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              :shortcuts="dateShortcuts"
            />
          </div>
        </div>
      </template>
      
      <el-table
        :data="filteredApplications"
        style="width: 100%"
        v-loading="loading"
        row-key="id"
      >
        <el-table-column prop="jobTitle" label="岗位名称" min-width="120">
          <template #default="scope">
            <el-link type="primary" @click="viewJobDetail(scope.row.jobId)">
              {{ scope.row.jobTitle }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="companyName" label="企业名称" min-width="120">
          <template #default="scope">
            <div class="company-info">
              <el-avatar :size="30" :src="scope.row.companyLogo || defaultLogo"></el-avatar>
              <span class="company-name">{{ scope.row.companyName }}</span>
              <el-tag v-if="scope.row.companyVerified" type="success" size="small">已认证</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="投递时间" width="180" sortable />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="canRate(scope.row)"
              type="primary" 
              size="small" 
              @click="rateCompany(scope.row)"
              plain
            >
              评价企业
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="contactCompany(scope.row)"
              :disabled="!canContact(scope.row)"
              plain
            >
              联系HR
            </el-button>
            <el-popconfirm
              v-if="canCancel(scope.row)"
              title="确定要撤回申请吗?"
              @confirm="cancelApplication(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small" plain>撤回</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalApplications"
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
      <job-detail v-if="jobDetailVisible" :job-id="selectedJobId" />
    </el-dialog>
    
    <!-- 评价企业对话框 -->
    <el-dialog
      v-model="rateDialogVisible"
      title="评价企业"
      width="50%"
      destroy-on-close
    >
      <div v-if="rateDialogVisible" class="rating-form">
        <h3>{{ selectedApplication.companyName }} - {{ selectedApplication.jobTitle }}</h3>
        
        <div class="rating-item">
          <span class="rating-label">岗位真实性：</span>
          <el-rate v-model="ratingForm.jobAuthenticityScore" />
        </div>
        
        <div class="rating-item">
          <span class="rating-label">面试体验：</span>
          <el-rate v-model="ratingForm.interviewExperienceScore" />
        </div>
        
        <div class="rating-item">
          <span class="rating-label">公司环境：</span>
          <el-rate v-model="ratingForm.companyEnvironmentScore" />
        </div>
        
        <div class="rating-item">
          <span class="rating-label">福利兑现：</span>
          <el-rate v-model="ratingForm.benefitsFulfillmentScore" />
        </div>
        
        <div class="rating-item">
          <span class="rating-label">整体评分：</span>
          <el-rate v-model="ratingForm.overallScore" />
        </div>
        
        <div class="rating-item">
          <span class="rating-label">评价内容：</span>
          <el-input
            type="textarea"
            v-model="ratingForm.comment"
            :rows="4"
            placeholder="请分享您的经历和感受，帮助其他同学了解这家企业..."
            :maxlength="500"
            show-word-limit
          />
        </div>
        
        <div class="rating-item">
          <el-checkbox v-model="ratingForm.isAnonymous">匿名评价（企业无法看到您的个人信息）</el-checkbox>
        </div>
        
        <div class="dialog-footer">
          <el-button @click="rateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRating" :loading="submitting">提交评价</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import JobDetail from '../../components/JobDetail.vue';
// 引入API相关函数，实际开发时需要实现这些函数
// import { getApplicationList, cancelApplicationById, submitCompanyRating } from '@/api/application';

const router = useRouter();
const loading = ref(false);
const applications = ref([]);
const totalApplications = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const statusFilter = ref('');
const dateRange = ref([]);
const jobDetailVisible = ref(false);
const selectedJobId = ref(null);
const rateDialogVisible = ref(false);
const selectedApplication = ref({});
const submitting = ref(false);
const defaultLogo = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png');

// 评分表单
const ratingForm = reactive({
  jobAuthenticityScore: 0,
  interviewExperienceScore: 0,
  companyEnvironmentScore: 0,
  benefitsFulfillmentScore: 0,
  overallScore: 0,
  comment: '',
  isAnonymous: true
});

// 状态选项
const statusOptions = [
  { value: 'pending', label: '待查看' },
  { value: 'viewed', label: '已查看' },
  { value: 'interviewed', label: '已面试' },
  { value: 'accepted', label: '已录用' },
  { value: 'rejected', label: '已拒绝' },
  { value: 'canceled', label: '已撤回' }
];

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
      return [start, end];
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
      return [start, end];
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
      return [start, end];
    }
  }
];

// 获取状态对应的类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending':
      return 'info';
    case 'viewed':
      return 'primary';
    case 'interviewed':
      return 'warning';
    case 'accepted':
      return 'success';
    case 'rejected':
      return 'danger';
    case 'canceled':
      return 'info';
    default:
      return 'info';
  }
};

// 获取状态对应的文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待查看';
    case 'viewed':
      return '已查看';
    case 'interviewed':
      return '已面试';
    case 'accepted':
      return '已录用';
    case 'rejected':
      return '已拒绝';
    case 'canceled':
      return '已撤回';
    default:
      return '未知状态';
  }
};

// 判断是否可以评价企业（只有面试过或被录用的可以评价）
const canRate = (application) => {
  return ['interviewed', 'accepted'].includes(application.status);
};

// 判断是否可以联系企业（除了被拒绝和已撤回的都可以联系）
const canContact = (application) => {
  return !['rejected', 'canceled'].includes(application.status);
};

// 判断是否可以撤回申请（只有待查看和已查看的可以撤回）
const canCancel = (application) => {
  return ['pending', 'viewed'].includes(application.status);
};

// 根据筛选条件过滤投递记录
const filteredApplications = computed(() => {
  let result = [...applications.value];
  
  // 按状态筛选
  if (statusFilter.value) {
    result = result.filter(item => item.status === statusFilter.value);
  }
  
  // 按日期范围筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]).getTime();
    const endDate = new Date(dateRange.value[1]).getTime() + 24 * 60 * 60 * 1000 - 1; // 包含结束日期当天
    
    result = result.filter(item => {
      const applyTime = new Date(item.applyTime).getTime();
      return applyTime >= startDate && applyTime <= endDate;
    });
  }
  
  return result;
});

// 获取投递记录列表
const fetchApplications = () => {
  loading.value = true;
  
  // 模拟API调用
  setTimeout(() => {
    // 模拟数据
    applications.value = [
      {
        id: 1,
        jobId: 101,
        jobTitle: '前端开发工程师',
        companyId: 201,
        companyName: '示例科技有限公司',
        companyLogo: '',
        companyVerified: true,
        applyTime: '2023-09-01 14:30:22',
        status: 'pending', // pending, viewed, interviewed, accepted, rejected, canceled
        notes: '',
        resumeId: 301
      },
      {
        id: 2,
        jobId: 102,
        jobTitle: '后端Java开发工程师',
        companyId: 202,
        companyName: '星辰软件科技有限公司',
        companyLogo: '',
        companyVerified: true,
        applyTime: '2023-08-25 10:15:48',
        status: 'viewed',
        notes: '',
        resumeId: 301
      },
      {
        id: 3,
        jobId: 103,
        jobTitle: 'UI设计师',
        companyId: 203,
        companyName: '创意设计工作室',
        companyLogo: '',
        companyVerified: false,
        applyTime: '2023-08-15 16:42:33',
        status: 'interviewed',
        notes: '面试表现良好，等待最终结果',
        resumeId: 301
      },
      {
        id: 4,
        jobId: 104,
        jobTitle: '产品经理',
        companyId: 204,
        companyName: '互联网科技有限公司',
        companyLogo: '',
        companyVerified: true,
        applyTime: '2023-08-10 09:28:17',
        status: 'accepted',
        notes: '已通过面试，请等待入职通知',
        resumeId: 301
      },
      {
        id: 5,
        jobId: 105,
        jobTitle: '数据分析师',
        companyId: 205,
        companyName: '数据智能科技有限公司',
        companyLogo: '',
        companyVerified: true,
        applyTime: '2023-08-05 11:20:36',
        status: 'rejected',
        notes: '与岗位要求不太匹配，谢谢您的关注',
        resumeId: 301
      }
    ];
    
    totalApplications.value = applications.value.length;
    loading.value = false;
  }, 1000);
};

// 查看岗位详情
const viewJobDetail = (jobId) => {
  selectedJobId.value = jobId;
  jobDetailVisible.value = true;
};

// 联系企业HR
const contactCompany = (application) => {
  router.push({
    path: '/student/chat',
    query: { companyId: application.companyId, jobId: application.jobId }
  });
};

// 评价企业
const rateCompany = (application) => {
  selectedApplication.value = application;
  // 重置评分表单
  ratingForm.jobAuthenticityScore = 0;
  ratingForm.interviewExperienceScore = 0;
  ratingForm.companyEnvironmentScore = 0;
  ratingForm.benefitsFulfillmentScore = 0; 
  ratingForm.overallScore = 0;
  ratingForm.comment = '';
  ratingForm.isAnonymous = true;
  
  rateDialogVisible.value = true;
};

// 提交评价
const submitRating = async () => {
  // 表单验证
  if (ratingForm.overallScore === 0) {
    ElMessage.warning('请至少填写整体评分');
    return;
  }
  
  try {
    submitting.value = true;
    
    // 实际开发时调用API
    // await submitCompanyRating({
    //   companyId: selectedApplication.value.companyId,
    //   jobId: selectedApplication.value.jobId,
    //   jobAuthenticityScore: ratingForm.jobAuthenticityScore,
    //   interviewExperienceScore: ratingForm.interviewExperienceScore,
    //   companyEnvironmentScore: ratingForm.companyEnvironmentScore,
    //   benefitsFulfillmentScore: ratingForm.benefitsFulfillmentScore,
    //   overallScore: ratingForm.overallScore,
    //   comment: ratingForm.comment,
    //   isAnonymous: ratingForm.isAnonymous
    // });
    
    // 模拟API调用
    setTimeout(() => {
      ElMessage.success('评价提交成功，感谢您的反馈！');
      rateDialogVisible.value = false;
      submitting.value = false;
    }, 1000);
    
  } catch (error) {
    console.error('提交评价失败:', error);
    ElMessage.error('提交评价失败，请稍后重试');
    submitting.value = false;
  }
};

// 撤回申请
const cancelApplication = async (applicationId) => {
  try {
    // 实际开发时调用API
    // await cancelApplicationById(applicationId);
    
    // 模拟API调用
    ElMessage.success('申请已成功撤回');
    
    // 更新本地数据状态
    const index = applications.value.findIndex(app => app.id === applicationId);
    if (index !== -1) {
      applications.value[index].status = 'canceled';
    }
  } catch (error) {
    console.error('撤回申请失败:', error);
    ElMessage.error('撤回申请失败，请稍后重试');
  }
};

// 分页相关
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchApplications();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchApplications();
};

// 生命周期钩子
onMounted(() => {
  fetchApplications();
});
</script>

<style scoped>
.applications-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filters {
  display: flex;
  gap: 15px;
}

.company-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.company-name {
  margin-left: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.rating-form {
  padding: 10px;
}

.rating-item {
  margin-bottom: 15px;
}

.rating-label {
  display: inline-block;
  width: 120px;
  text-align: right;
  margin-right: 10px;
}

.dialog-footer {
  margin-top: 20px;
  text-align: right;
}
</style> 