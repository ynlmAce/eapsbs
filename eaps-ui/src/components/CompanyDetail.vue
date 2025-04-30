<template>
  <div class="company-detail">
    <el-card v-if="loading">
      <el-skeleton :rows="10" animated />
    </el-card>
    
    <div v-else-if="company">
      <!-- 公司基本信息 -->
      <el-card class="mb-20">
        <div class="company-header">
          <div class="company-header-left">
            <el-avatar
              :size="80"
              :src="company.logoUrl || defaultLogo"
              class="company-logo"
            />
            <div class="company-info">
              <h2 class="company-name">{{ company.name }}</h2>
              <div class="company-tags">
                <el-tag v-if="company.industry" type="info">{{ company.industry }}</el-tag>
                <el-tag v-if="company.size" type="info">{{ company.size }}</el-tag>
                <el-tag v-if="company.isCertified" type="success">已认证</el-tag>
              </div>
            </div>
          </div>
          <div class="company-rating" v-if="ratingOverview.ratingCount > 0">
            <div class="rating-score">
              <span class="score">{{ formatScore(ratingOverview.overallAvg) }}</span>
              <span class="max">/5分</span>
            </div>
            <div class="rating-star">
              <el-rate
                v-model="ratingOverview.overallAvg"
                disabled
                show-score
                text-color="#ff9900"
                score-template=""
              />
            </div>
            <div class="rating-count">{{ ratingOverview.ratingCount }}条评价</div>
          </div>
          <div class="company-rating no-rating" v-else>
            <span class="no-rating-text">暂无评价</span>
          </div>
        </div>
        
        <el-divider />
        
        <div class="company-description">
          <h3>公司信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="企业名称">{{ company.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="统一社会信用代码">{{ company.unifiedSocialCreditCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="行业">{{ company.industry || '-' }}</el-descriptions-item>
            <el-descriptions-item label="规模">{{ company.size || '-' }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ company.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="HR联系方式">{{ (company.hrContact && company.hrContact.phone) ? company.hrContact.phone : '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="company-jobs">
          <h3>在招岗位</h3>
          <el-table :data="jobList" v-loading="loadingJobs" style="width: 100%" size="small" border>
            <el-table-column prop="title" label="岗位名称" min-width="120" />
            <el-table-column prop="salary" label="薪资" min-width="100" />
            <el-table-column prop="location" label="工作地点" min-width="120" />
            <el-table-column prop="publishedAt" label="发布时间" min-width="120">
              <template #default="scope">
                {{ formatDate(scope.row.publishedAt) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination" v-if="jobsTotal > pageSize">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="jobsTotal"
              :page-size="pageSize"
              :current-page="jobsPage"
              @current-change="handleJobsPageChange"
            />
          </div>
        </div>
        
        <div class="company-contact" v-if="company.hrContact">
          <h3>联系方式</h3>
          <p v-if="company.hrContact.name"><strong>联系人：</strong>{{ company.hrContact.name }}</p>
          <p v-if="company.hrContact.phone"><strong>电话：</strong>{{ company.hrContact.phone }}</p>
          <p v-if="company.hrContact.email"><strong>邮箱：</strong>{{ company.hrContact.email }}</p>
          <p v-if="company.address"><strong>地址：</strong>{{ company.address }}</p>
        </div>
      </el-card>
      
      <!-- 公司评分详情 -->
      <el-card v-if="ratingOverview.ratingCount > 0" class="mb-20">
        <template #header>
          <div class="card-header">
            <h3>评分详情</h3>
          </div>
        </template>
        
        <div class="rating-details">
          <div class="rating-item">
            <span class="rating-label">岗位真实性：</span>
            <div class="rating-bar">
              <el-rate
                v-model="ratingOverview.jobAuthenticityAvg"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
            </div>
          </div>
          
          <div class="rating-item">
            <span class="rating-label">面试体验：</span>
            <div class="rating-bar">
              <el-rate
                v-model="ratingOverview.interviewExperienceAvg"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
            </div>
          </div>
          
          <div class="rating-item">
            <span class="rating-label">公司环境：</span>
            <div class="rating-bar">
              <el-rate
                v-model="ratingOverview.workEnvironmentAvg"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
            </div>
          </div>
          
          <div class="rating-item">
            <span class="rating-label">福利兑现：</span>
            <div class="rating-bar">
              <el-rate
                v-model="ratingOverview.welfareDeliveryAvg"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 评价列表 -->
      <el-card>
        <template #header>
          <div class="card-header">
            <h3>评价列表</h3>
            <div class="rating-filter">
              <el-radio-group v-model="sortBy" size="small" @change="fetchRatings">
                <el-radio-button label="newest">最新</el-radio-button>
                <el-radio-button label="score">评分</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
        
        <rating-list
          :ratings="ratings"
          :loading="loadingRatings"
          :total-ratings="totalRatings"
          :current-page="currentPage"
          :page-size="pageSize"
          :show-actions="true"
          :can-report="true"
          :can-delete="false"
          @page-change="handlePageChange"
          @refresh="fetchRatings"
        />
      </el-card>
    </div>
    
    <el-empty v-else description="未找到公司信息" />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
// import { getCompanyProfile, getCompanyJobs } from '@/api/company';
import { getCompanyProfile, getCompanyJobs, getCompanyDetail } from '@/api/company';
import { getCompanyRatings, getCompanyRatingOverview } from '@/api/rating';
import { ElLoading, ElMessage } from 'element-plus';
import RatingList from '@/components/RatingList.vue';

const props = defineProps({
  companyId: {
    type: [Number, String],
    required: true
  }
});

// 默认头像
const defaultLogo = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png');

// 公司信息
const company = ref(null);
const loading = ref(true);

// 评价相关数据
const ratings = ref([]);
const loadingRatings = ref(false);
const totalRatings = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const sortBy = ref('newest');

// 评分概览
const ratingOverview = ref({
  overallAvg: 0,
  jobAuthenticityAvg: 0,
  interviewExperienceAvg: 0,
  workEnvironmentAvg: 0,
  welfareDeliveryAvg: 0,
  ratingCount: 0
});

// 岗位相关数据
const jobList = ref([]);
const loadingJobs = ref(false);
const jobsTotal = ref(0);
const jobsPage = ref(1);

// 监听公司ID变化
watch(() => props.companyId, (newVal) => {
  if (newVal) {
    fetchCompanyData();
  }
}, { immediate: true });

// 获取公司信息
const fetchCompanyData = async () => {
  if (!props.companyId) return;
  
  loading.value = true;
  
  try {
    // const companyData = await getCompanyProfile(props.companyId);
    const companyData = await getCompanyDetail(props.companyId);
    company.value = companyData;
    
    // 获取评分概览
    await fetchRatingOverview();
    
    // 获取评价列表
    await fetchRatings();

    // 获取岗位列表
    await fetchCompanyJobs();
  } catch (error) {
    console.error('获取公司信息失败:', error);
    ElMessage.error('获取公司信息失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 获取评分概览
const fetchRatingOverview = async () => {
  try {
    const data = await getCompanyRatingOverview(props.companyId);
    ratingOverview.value = data;
  } catch (error) {
    console.error('获取评分概览失败:', error);
  }
};

// 获取评价列表
const fetchRatings = async () => {
  loadingRatings.value = true;
  
  try {
    const params = {
      companyId: props.companyId,
      page: currentPage.value,
      limit: pageSize.value,
      sortBy: sortBy.value
    };
    
    const result = await getCompanyRatings(params);
    ratings.value = result.list || [];
    totalRatings.value = result.total || 0;
  } catch (error) {
    console.error('获取评价列表失败:', error);
    ElMessage.error('获取评价列表失败，请稍后重试');
  } finally {
    loadingRatings.value = false;
  }
};

// 获取岗位列表
const fetchCompanyJobs = async () => {
  loadingJobs.value = true;
  try {
    const res = await getCompanyJobs({ companyId: props.companyId, page: jobsPage.value, limit: pageSize.value });
    jobList.value = res.list || [];
    jobsTotal.value = res.total || 0;
  } catch (e) {
    jobList.value = [];
    jobsTotal.value = 0;
  } finally {
    loadingJobs.value = false;
  }
};

// 处理分页
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchRatings();
};

// 处理岗位分页
const handleJobsPageChange = (page) => {
  jobsPage.value = page;
  fetchCompanyJobs();
};

// 格式化评分
const formatScore = (score) => {
  return parseFloat(score).toFixed(1);
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};
</script>

<style scoped>
.company-detail {
  width: 100%;
}

.mb-20 {
  margin-bottom: 20px;
}

.company-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.company-header-left {
  display: flex;
  align-items: center;
}

.company-logo {
  margin-right: 15px;
}

.company-info {
  display: flex;
  flex-direction: column;
}

.company-name {
  font-size: 24px;
  margin: 0 0 10px 0;
  font-weight: bold;
}

.company-tags {
  display: flex;
  gap: 10px;
}

.company-rating {
  text-align: center;
  padding: 0 20px;
  border-left: 1px solid #eee;
}

.rating-score {
  margin-bottom: 5px;
}

.score {
  font-size: 28px;
  font-weight: bold;
  color: #ff9900;
}

.max {
  font-size: 14px;
  color: #909399;
}

.rating-count {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.no-rating-text {
  font-size: 14px;
  color: #909399;
}

.company-description h3,
.company-contact h3 {
  font-size: 16px;
  margin: 15px 0;
  font-weight: bold;
}

.company-contact p {
  margin: 5px 0;
  line-height: 1.6;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
}

.rating-details {
  padding: 10px 0;
}

.rating-item {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
}

.rating-label {
  width: 120px;
  text-align: right;
  margin-right: 20px;
  color: #606266;
}

.rating-bar {
  flex: 1;
}

.rating-card {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 0;
}

.rating-card:last-child {
  border-bottom: none;
}

.rating-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.rating-user {
  font-weight: bold;
}

.rating-time {
  color: #909399;
  font-size: 12px;
}

.rating-score-overview {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.rating-score-text {
  margin-left: 10px;
  font-weight: bold;
  color: #ff9900;
}

.rating-detail-scores {
  margin-bottom: 10px;
}

.job-applied {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #606266;
}

.rating-tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.rating-comment {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.6;
}

.no-data {
  padding: 30px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.rating-list-loading {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .company-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .company-rating {
    border-left: none;
    padding: 15px 0 0 0;
    border-top: 1px solid #eee;
    margin-top: 15px;
    width: 100%;
  }
  
  .rating-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .rating-label {
    width: 100%;
    text-align: left;
    margin-bottom: 5px;
  }
  
  .rating-bar {
    width: 100%;
  }
}
</style> 