<template>
  <div class="job-detail-container">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-container">
          <div class="skeleton-header">
            <el-skeleton-item variant="h1" style="width: 40%" />
            <el-skeleton-item variant="text" style="width: 30%; margin-top: 10px" />
          </div>
          <div class="skeleton-info">
            <el-skeleton-item variant="text" style="width: 20%" />
            <el-skeleton-item variant="text" style="width: 20%" />
            <el-skeleton-item variant="text" style="width: 20%" />
          </div>
          <div class="skeleton-description">
            <el-skeleton-item variant="h3" style="width: 30%" />
            <el-skeleton-item variant="text" style="width: 90%" />
            <el-skeleton-item variant="text" style="width: 90%" />
            <el-skeleton-item variant="text" style="width: 70%" />
          </div>
          <div class="skeleton-requirements">
            <el-skeleton-item variant="h3" style="width: 30%" />
            <el-skeleton-item variant="text" style="width: 90%" />
            <el-skeleton-item variant="text" style="width: 80%" />
            <el-skeleton-item variant="text" style="width: 75%" />
          </div>
        </div>
      </template>

      <template #default>
        <div v-if="jobData" class="job-data">
          <!-- 岗位标题和基本信息 -->
          <div class="job-header">
            <h1>{{ jobData.title }}</h1>
            <div class="job-salary">{{ jobData.salaryRange }}</div>
            <div class="job-publish-info">
              发布时间: {{ formatDate(jobData.publishTime) }} · 招聘人数: {{ jobData.count || '若干' }}人
            </div>
          </div>

          <!-- 岗位基本信息标签 -->
          <div class="job-tags">
            <el-tag v-if="jobData.location" type="info">{{ jobData.location }}</el-tag>
            <el-tag v-if="jobData.education" type="info">{{ jobData.education }}</el-tag>
            <el-tag v-if="jobData.experience" type="info">{{ jobData.experience }}</el-tag>
            <el-tag v-if="jobData.type" type="info">{{ jobData.type }}</el-tag>
          </div>

          <!-- 公司信息 -->
          <div class="company-info">
            <div class="company-basic">
              <el-avatar :size="50" :src="jobData.companyLogo || defaultLogo"></el-avatar>
              <div class="company-name-info">
                <h3>{{ jobData.companyName }}</h3>
                <div class="company-tags">
                  <el-tag v-if="jobData.companyVerified" type="success" size="small">已认证</el-tag>
                  <el-tag v-if="jobData.companyIndustry" type="info" size="small">{{ jobData.companyIndustry }}</el-tag>
                  <el-tag v-if="jobData.companySize" type="info" size="small">{{ jobData.companySize }}</el-tag>
                </div>
              </div>
              <div v-if="jobData.companyRating" class="company-rating">
                <span>综合评分: </span>
                <el-rate
                  v-model="jobData.companyRating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}"
                />
              </div>
            </div>
          </div>

          <!-- 福利标签 -->
          <div v-if="jobData.welfare && jobData.welfare.length" class="welfare-tags">
            <h3>公司福利</h3>
            <div class="tags-container">
              <el-tag 
                v-for="(tag, index) in jobData.welfare" 
                :key="index"
                type="success"
                effect="plain"
                class="welfare-tag"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <!-- 职位描述 -->
          <div class="job-section">
            <h3>职位描述</h3>
            <div class="section-content" v-html="formatContent(jobData.description)"></div>
          </div>

          <!-- 职位要求 -->
          <div class="job-section">
            <h3>职位要求</h3>
            <div class="section-content" v-html="formatContent(jobData.requirements)"></div>
          </div>

          <!-- 联系信息 -->
          <div v-if="jobData.contactInfo" class="job-section">
            <h3>联系方式</h3>
            <div class="contact-info">
              <p v-if="jobData.contactInfo.name">联系人：{{ jobData.contactInfo.name }}</p>
              <p v-if="jobData.contactInfo.phone">联系电话：{{ jobData.contactInfo.phone }}</p>
              <p v-if="jobData.contactInfo.email">联系邮箱：{{ jobData.contactInfo.email }}</p>
            </div>
          </div>
        </div>
        <div v-else class="no-data">
          <el-empty description="没有找到岗位信息"></el-empty>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
// 实际开发时需要引入真实的API
// import { getJobDetailById } from '@/api/job';

const props = defineProps({
  jobId: {
    type: [Number, String],
    required: true
  }
});

const loading = ref(true);
const jobData = ref(null);
const defaultLogo = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
};

// 格式化内容，将换行转换为HTML
const formatContent = (content) => {
  if (!content) return '';
  return content.replace(/\n/g, '<br>');
};

// 获取岗位详情
const fetchJobDetail = async () => {
  if (!props.jobId) return;
  
  loading.value = true;
  try {
    // 实际开发时需要替换为真实API调用
    // const res = await getJobDetailById(props.jobId);
    // jobData.value = res.data;
    
    // 模拟数据
    setTimeout(() => {
      jobData.value = {
        id: props.jobId,
        title: '前端开发工程师',
        salaryRange: '12k-20k/月',
        publishTime: '2023-08-15',
        count: 5,
        location: '北京',
        education: '本科及以上',
        experience: '1-3年',
        type: '全职',
        companyName: '示例科技有限公司',
        companyLogo: '',
        companyVerified: true,
        companyIndustry: '互联网/IT',
        companySize: '100-499人',
        companyRating: 4.5,
        welfare: ['五险一金', '年终奖', '弹性工作', '定期体检', '团队建设', '免费零食'],
        description: '1. 负责公司产品的前端开发，确保良好的用户体验和性能表现；\n2. 与后端开发团队协作，实现产品功能和界面；\n3. 负责前端架构设计和技术选型；\n4. 优化前端性能和用户交互体验；\n5. 编写可维护的高质量代码和自动化测试。',
        requirements: '1. 计算机相关专业本科及以上学历；\n2. 熟练掌握HTML、CSS、JavaScript等前端技术；\n3. 熟悉Vue/React等主流前端框架；\n4. 了解前端工程化和模块化开发；\n5. 具备良好的代码风格和编程习惯；\n6. 有良好的沟通能力和团队协作精神。',
        contactInfo: {
          name: 'HR李先生',
          phone: '13800138000',
          email: 'hr@example.com'
        }
      };
      loading.value = false;
    }, 1000);
    
  } catch (error) {
    console.error('获取岗位详情失败:', error);
    ElMessage.error('获取岗位详情失败，请稍后重试');
    loading.value = false;
  }
};

onMounted(() => {
  fetchJobDetail();
});
</script>

<style scoped>
.job-detail-container {
  padding: 10px;
}

.skeleton-container {
  padding: 20px;
}

.skeleton-header, .skeleton-info, .skeleton-description, .skeleton-requirements {
  margin-bottom: 20px;
}

.job-header {
  margin-bottom: 20px;
}

.job-header h1 {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.job-salary {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 10px;
}

.job-publish-info {
  font-size: 14px;
  color: #909399;
}

.job-tags {
  margin-bottom: 20px;
}

.job-tags .el-tag {
  margin-right: 10px;
}

.company-info {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.company-basic {
  display: flex;
  align-items: center;
}

.company-name-info {
  margin-left: 15px;
  flex: 1;
}

.company-name-info h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.company-tags {
  display: flex;
  gap: 5px;
}

.company-rating {
  display: flex;
  align-items: center;
}

.welfare-tags {
  margin-bottom: 20px;
}

.welfare-tags h3 {
  font-size: 16px;
  margin-bottom: 10px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.welfare-tag {
  margin-bottom: 5px;
}

.job-section {
  margin-bottom: 20px;
}

.job-section h3 {
  font-size: 16px;
  margin-bottom: 10px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.section-content {
  line-height: 1.6;
  color: #606266;
}

.contact-info {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.contact-info p {
  margin: 5px 0;
}

.no-data {
  padding: 40px 0;
}

@media (max-width: 768px) {
  .company-basic {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .company-name-info {
    margin-left: 0;
    margin-top: 10px;
  }
  
  .company-rating {
    margin-top: 10px;
  }
}
</style> 