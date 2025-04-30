<template>
  <div class="rating-list-component">
    <div v-if="loading" class="rating-list-loading">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="ratings.length === 0" class="no-data">
      暂无评价
    </div>
    
    <div v-else class="rating-list">
      <div v-for="rating in ratings" :key="rating.id" class="rating-card">
        <div class="rating-header">
          <span class="rating-user">{{ rating.studentName || '匿名用户' }}</span>
          <span class="rating-time">{{ formatDate(rating.submittedAt) }}</span>
        </div>
        
        <div class="rating-score-overview">
          <el-rate v-model="rating.overallScore" disabled />
          <span class="rating-score-text">{{ rating.overallScore }}分</span>
        </div>
        
        <div class="rating-detail-scores">
          <span v-if="rating.jobTitle" class="job-applied">应聘岗位：{{ rating.jobTitle }}</span>
          <div class="rating-tag-group">
            <el-tag v-if="rating.jobAuthenticityScore" size="small" type="info">
              岗位真实: {{ rating.jobAuthenticityScore }}分
            </el-tag>
            <el-tag v-if="rating.interviewExperienceScore" size="small" type="info">
              面试体验: {{ rating.interviewExperienceScore }}分
            </el-tag>
            <el-tag v-if="rating.workEnvironmentScore" size="small" type="info">
              公司环境: {{ rating.workEnvironmentScore }}分
            </el-tag>
            <el-tag v-if="rating.welfareDeliveryScore" size="small" type="info">
              福利兑现: {{ rating.welfareDeliveryScore }}分
            </el-tag>
          </div>
        </div>
        
        <div class="rating-comment" v-if="rating.comment">
          {{ rating.comment }}
        </div>
        
        <div class="rating-actions" v-if="showActions">
          <el-button 
            v-if="canReport && !isReportingLoading[rating.id]" 
            type="text" 
            size="small" 
            @click="reportRating(rating.id)"
          >
            举报
          </el-button>
          <el-button 
            v-if="canDelete && rating.studentId === currentUserId && !isDeleteLoading[rating.id]" 
            type="text" 
            size="small" 
            @click="deleteRating(rating.id)"
          >
            删除
          </el-button>
          <span v-if="isReportingLoading[rating.id] || isDeleteLoading[rating.id]">
            <el-icon class="is-loading"><Loading /></el-icon>
          </span>
        </div>
      </div>
    </div>
    
    <div class="pagination" v-if="totalRatings > 0">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="totalRatings"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { reportRating as reportRatingAPI, deleteRating as deleteRatingAPI } from '@/api/rating';
import { Loading } from '@element-plus/icons-vue';

const props = defineProps({
  ratings: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  totalRatings: {
    type: Number,
    default: 0
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  showActions: {
    type: Boolean,
    default: false
  },
  canReport: {
    type: Boolean,
    default: false
  },
  canDelete: {
    type: Boolean,
    default: false
  },
  currentUserId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits(['page-change', 'refresh']);

// 加载状态
const isReportingLoading = reactive({});
const isDeleteLoading = reactive({});

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 处理分页
const handlePageChange = (page) => {
  emit('page-change', page);
};

// 举报评价
const reportRating = async (ratingId) => {
  try {
    // 弹窗确认
    const reason = await ElMessageBox.prompt('请输入举报原因', '举报评价', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请详细描述举报理由（如虚假信息、恶意评价等）',
      inputValidator: (value) => {
        return value.trim().length > 0 ? true : '举报原因不能为空';
      }
    });
    
    if (reason.value.trim()) {
      isReportingLoading[ratingId] = true;
      const result = await reportRatingAPI(ratingId, reason.value.trim());
      if (result === true) {
        ElMessage.success('举报成功，我们会尽快处理');
      } else {
        ElMessage.error('举报失败，请稍后重试');
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('举报评价失败:', error);
      ElMessage.error('举报失败，请稍后重试');
    }
  } finally {
    isReportingLoading[ratingId] = false;
  }
};

// 删除评价
const deleteRating = async (ratingId) => {
  try {
    // 弹窗确认
    await ElMessageBox.confirm(
      '确定要删除此评价吗？删除后将无法恢复。',
      '删除评价',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    isDeleteLoading[ratingId] = true;
    
    const result = await deleteRatingAPI(ratingId);
    
    if (result && result.error === 0) {
      ElMessage.success('评价已成功删除');
      emit('refresh');
    } else {
      ElMessage.error(result?.message || '删除失败，请稍后重试');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error);
      ElMessage.error('删除失败，请稍后重试');
    }
  } finally {
    isDeleteLoading[ratingId] = false;
  }
};
</script>

<style scoped>
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
  margin-bottom: 10px;
}

.rating-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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
</style> 