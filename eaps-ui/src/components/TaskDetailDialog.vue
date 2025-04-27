<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    width="60%"
    class="task-detail-dialog"
    destroy-on-close
  >
    <el-skeleton :loading="loading" animated :rows="10">
      <template #default>
        <div v-if="task">
          <!-- 基本任务信息 -->
          <el-descriptions title="任务信息" :column="2" border>
            <el-descriptions-item label="任务ID">{{ task.id }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(task.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="任务类型">
              <el-tag :type="getTypeTag(task.type)">{{ getTypeName(task.type) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="getPriorityTag(task.priority)">{{ getPriorityName(task.priority) }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 岗位审核任务详情 -->
          <div v-if="task.type === 'jobAudit'" class="mt-20">
            <h3 class="section-title">岗位信息</h3>
            <el-card v-if="task.jobDetails" class="job-details-card">
              <div class="job-header">
                <h2 class="job-title">{{ task.jobDetails.title }}</h2>
                <div class="job-company">
                  <span>{{ task.companyName }}</span>
                  <el-tag v-if="task.companyDetails?.certificationStatus === 'certified'" size="small" type="success">已认证</el-tag>
                </div>
              </div>
              
              <div class="job-basics">
                <el-tag>{{ task.jobDetails.location }}</el-tag>
                <el-tag>{{ task.jobDetails.salary }}</el-tag>
                <el-tag>{{ task.jobDetails.education }}</el-tag>
                <el-tag>{{ task.jobDetails.experience }}</el-tag>
                <el-tag>{{ task.jobDetails.jobType }}</el-tag>
                <el-tag type="info">招聘人数：{{ task.jobDetails.headcount }}人</el-tag>
                <el-tag type="info">有效期至：{{ formatDate(task.jobDetails.validUntil, 'YYYY-MM-DD') }}</el-tag>
              </div>
              
              <div class="job-section">
                <h3>岗位描述</h3>
                <div class="job-content">{{ task.jobDetails.description }}</div>
              </div>
              
              <div class="job-section">
                <h3>岗位要求</h3>
                <div class="job-content">{{ task.jobDetails.requirement }}</div>
              </div>
              
              <div class="job-section">
                <h3>工作时间</h3>
                <div class="job-content">{{ task.jobDetails.workTime }}</div>
              </div>
              
              <div class="job-section" v-if="task.jobDetails.jobTags && task.jobDetails.jobTags.length > 0">
                <h3>岗位标签</h3>
                <div class="tag-list">
                  <el-tag v-for="(tag, index) in task.jobDetails.jobTags" :key="index" class="tag-item">{{ tag }}</el-tag>
                </div>
              </div>
              
              <div class="job-section" v-if="task.jobDetails.welfareTags && task.jobDetails.welfareTags.length > 0">
                <h3>福利标签</h3>
                <div class="tag-list">
                  <el-tag v-for="(tag, index) in task.jobDetails.welfareTags" :key="index" type="success" class="tag-item">{{ tag }}</el-tag>
                </div>
              </div>
              
              <div class="job-section" v-if="task.jobDetails.contactPerson && task.jobDetails.showContact">
                <h3>联系信息</h3>
                <div class="contact-info">
                  <p>联系人：{{ task.jobDetails.contactPerson }}</p>
                  <p>联系方式：{{ task.jobDetails.contactMethod }}</p>
                </div>
              </div>
            </el-card>
            <el-empty v-else description="暂无岗位详情数据"></el-empty>
          </div>

          <!-- 企业认证任务详情 -->
          <div v-if="task.type === 'companyCertification'" class="mt-20">
            <h3 class="section-title">企业信息</h3>
            <el-card v-if="task.companyDetails" class="company-details-card">
              <!-- 企业基本信息 -->
              <div class="company-header">
                <div class="company-name">{{ task.companyDetails.name }}</div>
                <div class="company-code">统一社会信用代码：{{ task.companyDetails.unifiedSocialCreditCode }}</div>
              </div>
              
              <div class="company-section">
                <h3>企业简介</h3>
                <div class="company-content">{{ task.companyDetails.description || '暂无企业简介' }}</div>
              </div>
              
              <div class="company-basics">
                <div class="info-item">
                  <span class="label">行业：</span>
                  <span>{{ task.companyDetails.industry || '未填写' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">规模：</span>
                  <span>{{ task.companyDetails.size || '未填写' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">地址：</span>
                  <span>{{ task.companyDetails.address || '未填写' }}</span>
                </div>
                <div class="info-item">
                  <span class="label">联系人：</span>
                  <span>{{ task.companyDetails.hrContact || '未填写' }}</span>
                </div>
              </div>
              
              <!-- 企业Logo和营业执照 -->
              <div class="company-files">
                <div class="logo-section">
                  <h3>企业Logo</h3>
                  <el-image 
                    v-if="task.companyDetails.logoPath" 
                    :src="task.companyDetails.logoPath" 
                    fit="contain" 
                    class="company-logo"
                  ></el-image>
                  <el-empty v-else description="未上传Logo" :image-size="100"></el-empty>
                </div>
                
                <div class="license-section">
                  <h3>营业执照</h3>
                  <el-image 
                    v-if="task.companyDetails.licensePath" 
                    :src="task.companyDetails.licensePath" 
                    fit="contain" 
                    class="company-license"
                    :preview-src-list="[task.companyDetails.licensePath]"
                  ></el-image>
                  <el-empty v-else description="未上传营业执照" :image-size="100"></el-empty>
                </div>
              </div>
            </el-card>
            <el-empty v-else description="暂无企业详情数据"></el-empty>
          </div>
        </div>
        <el-empty v-else description="暂无任务信息"></el-empty>
      </template>
    </el-skeleton>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">关闭</el-button>
        <el-button v-if="task && canProcess" type="primary" @click="handleTask">处理此任务</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { formatDate as formatDateUtil } from '@/utils/dateUtil';
import { getJobDetail } from '@/api/job';
import { getCompanyDetail } from '@/api/company';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  task: {
    type: Object,
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:visible', 'process', 'refresh']);
const localLoading = ref(false);
const localTask = ref(null);

watch(() => props.task, async (newVal) => {
  if (newVal) {
    localTask.value = {...newVal};
    
    // 如果是岗位审核任务且缺少完整岗位数据，则获取完整数据
    if (newVal.type === 'jobAudit' && newVal.jobDetails && 
        (!newVal.jobDetails.jobTags || !newVal.jobDetails.welfareTags)) {
      await fetchJobDetails(newVal.jobDetails.id);
    }
    
    // 如果是企业认证任务且缺少完整企业数据，则获取完整数据
    if (newVal.type === 'companyCertification' && newVal.companyDetails && 
        !newVal.companyDetails.licensePath) {
      await fetchCompanyDetails(newVal.companyDetails.id);
    }
  }
}, { immediate: true, deep: true });

// 获取完整的岗位详情
const fetchJobDetails = async (jobId) => {
  if (!jobId) return;
  
  try {
    localLoading.value = true;
    const { error, body } = await getJobDetail(jobId);
    
    if (error === 0 && body) {
      // 更新本地任务的岗位详情
      localTask.value = {
        ...localTask.value,
        jobDetails: {
          ...localTask.value.jobDetails,
          ...body,
          // 确保标签数据存在
          jobTags: body.jobTags || [],
          welfareTags: body.welfareTags || []
        }
      };
    }
  } catch (error) {
    console.error('获取岗位详情失败:', error);
  } finally {
    localLoading.value = false;
  }
};

// 获取完整的企业详情
const fetchCompanyDetails = async (companyId) => {
  if (!companyId) return;
  
  try {
    localLoading.value = true;
    const { error, body } = await getCompanyDetail(companyId);
    
    if (error === 0 && body) {
      // 更新本地任务的企业详情
      localTask.value = {
        ...localTask.value,
        companyDetails: {
          ...localTask.value.companyDetails,
          ...body
        }
      };
    }
  } catch (error) {
    console.error('获取企业详情失败:', error);
  } finally {
    localLoading.value = false;
  }
};

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

const title = computed(() => {
  if (!localTask.value) return '任务详情';
  return `任务详情 - ${getTypeName(localTask.value.type)}`;
});

const canProcess = computed(() => {
  return localTask.value && localTask.value.status === 'pending';
});

// 格式化日期
const formatDate = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return '';
  return formatDateUtil(date, format);
};

// 获取任务类型对应的标签类型
const getTypeTag = (type) => {
  const typeMap = {
    'companyCertification': 'success',
    'jobAudit': 'primary',
    'reportHandling': 'danger'
  };
  return typeMap[type] || 'info';
};

// 获取任务类型名称
const getTypeName = (type) => {
  const typeMap = {
    'companyCertification': '企业认证',
    'jobAudit': '岗位审核',
    'reportHandling': '举报处理'
  };
  return typeMap[type] || '未知类型';
};

// 获取优先级对应的标签类型
const getPriorityTag = (priority) => {
  const priorityMap = {
    'high': 'danger',
    'normal': 'warning',
    'low': 'info'
  };
  return priorityMap[priority] || 'info';
};

// 获取优先级名称
const getPriorityName = (priority) => {
  const priorityMap = {
    'high': '高',
    'normal': '中',
    'low': '低'
  };
  return priorityMap[priority] || '中';
};

// 处理任务
const handleTask = () => {
  emit('process', localTask.value);
};

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false;
};
</script>

<style scoped>
.task-detail-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 15px 0;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}

.mt-20 {
  margin-top: 20px;
}

.job-details-card, .company-details-card {
  margin-bottom: 20px;
}

.job-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.job-title {
  font-size: 20px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.job-company {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
}

.job-basics {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.job-section, .company-section {
  margin-bottom: 20px;
}

.job-section h3, .company-section h3 {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.job-content, .company-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin-bottom: 5px;
}

.contact-info p {
  margin: 5px 0;
  color: #606266;
}

.company-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.company-name {
  font-size: 20px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.company-code {
  color: #606266;
}

.company-basics {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
}

.info-item .label {
  width: 80px;
  color: #909399;
}

.company-files {
  display: flex;
  gap: 20px;
}

.logo-section, .license-section {
  flex: 1;
}

.company-logo, .company-license {
  width: 100%;
  max-height: 200px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .company-files {
    flex-direction: column;
  }
}
</style> 