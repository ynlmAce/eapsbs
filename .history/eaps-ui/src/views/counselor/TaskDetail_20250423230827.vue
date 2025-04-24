<template>
  <div class="task-detail-container">
    <el-page-header @back="goBack" :title="'返回任务列表'" :content="taskTypeText">
      <template #extra>
        <el-tag v-if="task" :type="getTaskStatusType(task.status)">{{ getTaskStatusText(task.status) }}</el-tag>
      </template>
    </el-page-header>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="!task" class="not-found">
      <el-empty description="未找到任务或任务已被删除" />
      <el-button type="primary" @click="goBack">返回任务列表</el-button>
    </div>
    
    <div v-else class="task-content">
      <!-- 企业认证任务 -->
      <div v-if="task.type === 'COMPANY_CERTIFICATION'" class="certification-task">
        <el-descriptions title="企业信息" :column="2" border>
          <el-descriptions-item label="企业名称">{{ task.company.name }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ task.company.contactPerson }}</el-descriptions-item>
          <el-descriptions-item label="社会信用代码">{{ task.company.unifiedSocialCreditCode }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ task.company.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="企业地址" :span="2">{{ task.company.address }}</el-descriptions-item>
          <el-descriptions-item label="企业简介" :span="2">
            {{ task.company.description }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="doc-preview">
          <h3>营业执照</h3>
          <el-image
            v-if="task.company.licenseUrl"
            :src="task.company.licenseUrl"
            fit="contain"
            :preview-src-list="[task.company.licenseUrl]"
            class="license-image"
          />
          <div v-else class="empty-doc">
            <el-empty description="未上传营业执照" />
          </div>
        </div>
        
        <div class="action-panel">
          <el-form :model="approvalForm" label-position="top">
            <el-form-item v-if="task.status === 'PENDING'" label="审核结果">
              <el-radio-group v-model="approvalForm.action">
                <el-radio label="approve">通过</el-radio>
                <el-radio label="reject">驳回</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item v-if="task.status === 'PENDING' && approvalForm.action === 'approve'" label="认证有效期">
              <el-date-picker
                v-model="approvalForm.expiryDate"
                type="date"
                placeholder="选择有效期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :disabled-date="disabledDate"
              />
            </el-form-item>
            
            <el-form-item label="备注说明">
              <el-input
                v-model="approvalForm.notes"
                type="textarea"
                :rows="4"
                placeholder="请输入审核意见或说明..."
                :disabled="task.status !== 'PENDING'"
              />
            </el-form-item>
            
            <el-form-item v-if="task.status === 'PENDING'">
              <el-button type="primary" @click="submitApproval" :loading="submitting">提交审核</el-button>
              <el-button @click="goBack">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <!-- 岗位审核任务 -->
      <div v-else-if="task.type === 'JOB_APPROVAL'" class="job-task">
        <el-descriptions title="岗位信息" :column="2" border>
          <el-descriptions-item label="岗位名称">{{ task.job.title }}</el-descriptions-item>
          <el-descriptions-item label="发布企业">{{ task.job.companyName }}</el-descriptions-item>
          <el-descriptions-item label="工作地点">{{ task.job.location }}</el-descriptions-item>
          <el-descriptions-item label="薪资范围">{{ task.job.salary }}</el-descriptions-item>
          <el-descriptions-item label="招聘人数">{{ task.job.headcount }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ task.job.publishTime }}</el-descriptions-item>
          <el-descriptions-item label="岗位类型">{{ task.job.type }}</el-descriptions-item>
          <el-descriptions-item label="学历要求">{{ task.job.education }}</el-descriptions-item>
          <el-descriptions-item label="岗位描述" :span="2">
            <div class="job-description">{{ task.job.description }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="岗位要求" :span="2">
            <div class="job-requirements">{{ task.job.requirements }}</div>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="tags-section">
          <h3>岗位标签</h3>
          <div class="tags-container">
            <el-tag 
              v-for="tag in task.job.tags" 
              :key="tag" 
              class="job-tag"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
        
        <div class="tags-section">
          <h3>福利标签</h3>
          <div class="tags-container">
            <el-tag 
              v-for="welfare in task.job.welfares" 
              :key="welfare.id" 
              class="welfare-tag"
              :type="welfare.isCustom ? 'warning' : ''"
              :effect="welfare.isCustom ? 'plain' : 'light'"
            >
              {{ welfare.name }} <span v-if="welfare.isCustom">(自定义)</span>
            </el-tag>
          </div>
        </div>
        
        <div class="action-panel">
          <el-form :model="approvalForm" label-position="top">
            <el-form-item v-if="task.status === 'PENDING'" label="审核结果">
              <el-radio-group v-model="approvalForm.action">
                <el-radio label="approve">通过</el-radio>
                <el-radio label="reject">驳回</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="备注说明">
              <el-input
                v-model="approvalForm.notes"
                type="textarea"
                :rows="4"
                placeholder="请输入审核意见或说明..."
                :disabled="task.status !== 'PENDING'"
              />
            </el-form-item>
            
            <el-form-item v-if="task.status === 'PENDING'">
              <el-button type="primary" @click="submitApproval" :loading="submitting">提交审核</el-button>
              <el-button @click="goBack">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <!-- 内容举报任务 -->
      <div v-else-if="task.type === 'REPORT_REVIEW'" class="report-task">
        <el-descriptions title="举报信息" :column="2" border>
          <el-descriptions-item label="举报类型">{{ getReportTypeText(task.report.itemType) }}</el-descriptions-item>
          <el-descriptions-item label="举报人">{{ task.report.reporterName }}</el-descriptions-item>
          <el-descriptions-item label="举报时间">{{ task.report.reportTime }}</el-descriptions-item>
          <el-descriptions-item label="举报原因">{{ task.report.reason }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="reported-content">
          <h3>被举报内容</h3>
          
          <div v-if="task.report.itemType === 'RATING'" class="rating-content">
            <el-card>
              <template #header>
                <div class="rating-header">
                  <div>对 <strong>{{ task.report.targetCompany }}</strong> 的评价</div>
                  <div class="rating-score">
                    <el-rate v-model="task.report.content.score" disabled />
                    <span>{{ task.report.content.score }}分</span>
                  </div>
                </div>
              </template>
              <div class="rating-details">
                <p>{{ task.report.content.comment }}</p>
                <div class="rating-meta">
                  <span>评价时间: {{ task.report.content.ratingTime }}</span>
                  <span v-if="task.report.content.isAnonymous" class="anonymous-tag">匿名评价</span>
                </div>
              </div>
            </el-card>
          </div>
          
          <div v-else-if="task.report.itemType === 'MESSAGE'" class="message-content">
            <el-card>
              <template #header>
                <div class="message-header">
                  <div>聊天消息</div>
                  <div class="message-meta">
                    <span>发送者: {{ task.report.content.senderName }}</span>
                    <span>发送时间: {{ task.report.content.sendTime }}</span>
                  </div>
                </div>
              </template>
              <div class="message-details">
                <p v-if="task.report.content.type === 'text'">{{ task.report.content.content }}</p>
                <div v-else-if="task.report.content.type === 'image'" class="message-image">
                  <el-image
                    :src="task.report.content.content"
                    fit="contain"
                    :preview-src-list="[task.report.content.content]"
                  />
                </div>
                <div v-else-if="task.report.content.type === 'file'" class="message-file">
                  <span>文件: {{ task.report.content.fileName }}</span>
                  <el-button size="small" @click="previewFile(task.report.content.content)">预览文件</el-button>
                </div>
              </div>
            </el-card>
            
            <div class="chat-context">
              <h4>消息上下文</h4>
              <el-timeline>
                <el-timeline-item 
                  v-for="(message, index) in task.report.content.context" 
                  :key="index"
                  :timestamp="message.sendTime"
                  :type="message.id === task.report.content.id ? 'danger' : ''"
                >
                  <p class="timeline-title">{{ message.senderName }}:</p>
                  <p>{{ message.content }}</p>
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>
        </div>
        
        <div class="action-panel">
          <el-form :model="approvalForm" label-position="top">
            <el-form-item v-if="task.status === 'PENDING'" label="处理结果">
              <el-radio-group v-model="approvalForm.action">
                <el-radio label="approve">确认违规并删除</el-radio>
                <el-radio label="reject">不违规，忽略举报</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item v-if="task.status === 'PENDING' && approvalForm.action === 'approve'" label="处罚措施">
              <el-checkbox v-model="approvalForm.reduceScore">扣除用户行为分</el-checkbox>
              <el-input-number 
                v-if="approvalForm.reduceScore" 
                v-model="approvalForm.scoreReduction" 
                :min="1" 
                :max="10"
                size="small"
                class="score-input"
              />
            </el-form-item>
            
            <el-form-item label="处理说明">
              <el-input
                v-model="approvalForm.notes"
                type="textarea"
                :rows="4"
                placeholder="请输入处理意见或说明..."
                :disabled="task.status !== 'PENDING'"
              />
            </el-form-item>
            
            <el-form-item v-if="task.status === 'PENDING'">
              <el-button type="primary" @click="submitApproval" :loading="submitting">提交处理</el-button>
              <el-button @click="goBack">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <!-- 其他类型任务 -->
      <div v-else class="unsupported-task">
        <el-empty description="暂不支持此类型任务的详情展示" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
// 引入API相关函数，实际开发时需要实现这些函数
// import { getTaskDetail, submitTaskApproval } from '@/api/counselor';

const route = useRoute();
const router = useRouter();
const taskId = computed(() => route.params.id);

const loading = ref(true);
const task = ref(null);
const submitting = ref(false);
const approvalForm = reactive({
  action: 'approve',
  notes: '',
  expiryDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10), // 默认一年后
  reduceScore: false,
  scoreReduction: 3
});

// 任务类型文本
const taskTypeText = computed(() => {
  if (!task.value) return '任务详情';
  
  const typeMap = {
    'COMPANY_CERTIFICATION': '企业认证审核',
    'JOB_APPROVAL': '岗位审核',
    'REPORT_REVIEW': '内容举报处理'
  };
  
  return typeMap[task.value.type] || '任务详情';
});

// 获取任务状态标签类型
const getTaskStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'COMPLETED': 'success',
    'REJECTED': 'danger'
  };
  return statusMap[status] || 'info';
};

// 获取任务状态显示文本
const getTaskStatusText = (status) => {
  const statusMap = {
    'PENDING': '待处理',
    'COMPLETED': '已完成',
    'REJECTED': '已驳回'
  };
  return statusMap[status] || '未知状态';
};

// 获取举报类型显示文本
const getReportTypeText = (itemType) => {
  const typeMap = {
    'RATING': '企业评价',
    'MESSAGE': '聊天消息',
    'CHAT_SESSION': '聊天会话'
  };
  return typeMap[itemType] || '未知类型';
};

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000; // 禁用今天之前的日期
};

// 预览文件
const previewFile = (fileUrl) => {
  window.open(fileUrl, '_blank');
};

// 返回任务列表
const goBack = () => {
  router.push('/counselor/dashboard');
};

// 加载任务详情
const loadTaskDetail = async () => {
  if (!taskId.value) {
    loading.value = false;
    return;
  }
  
  loading.value = true;
  try {
    // 实际开发时使用API调用
    // const res = await getTaskDetail(taskId.value);
    // task.value = res.body;
    
    // 模拟数据
    setTimeout(() => {
      // 模拟不同类型的任务数据
      const taskType = Math.random() < 0.33 ? 'COMPANY_CERTIFICATION' : (Math.random() < 0.5 ? 'JOB_APPROVAL' : 'REPORT_REVIEW');
      
      if (taskType === 'COMPANY_CERTIFICATION') {
        task.value = {
          id: taskId.value,
          type: 'COMPANY_CERTIFICATION',
          status: 'PENDING',
          createdAt: '2023-06-10 14:30:25',
          priority: 'HIGH',
          company: {
            id: 1,
            name: '科技星球有限公司',
            contactPerson: '张经理',
            unifiedSocialCreditCode: '91110108MA01GR1T2L',
            contactPhone: '13800138000',
            address: '北京市海淀区中关村南大街5号',
            description: '科技星球是一家专注于人工智能和大数据的科技公司，致力于为企业提供智能化解决方案。',
            licenseUrl: 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
          }
        };
      } else if (taskType === 'JOB_APPROVAL') {
        task.value = {
          id: taskId.value,
          type: 'JOB_APPROVAL',
          status: 'PENDING',
          createdAt: '2023-06-15 10:20:18',
          priority: 'MEDIUM',
          job: {
            id: 101,
            title: '前端开发工程师',
            companyName: '科技星球有限公司',
            location: '北京市海淀区',
            salary: '15k-25k',
            headcount: 5,
            publishTime: '2023-06-14 16:30:45',
            type: '全职',
            education: '本科及以上',
            description: '负责公司产品的Web前端开发，与后端开发人员协作实现产品功能，优化用户体验。',
            requirements: '1. 熟悉HTML、CSS、JavaScript等前端技术；\n2. 熟悉Vue、React等主流前端框架；\n3. 有良好的编码习惯和问题解决能力；\n4. 有相关项目经验者优先。',
            tags: ['前端开发', 'Vue', 'React', '互联网'],
            welfares: [
              { id: 1, name: '五险一金', isCustom: false },
              { id: 2, name: '年终奖', isCustom: false },
              { id: 3, name: '弹性工作制', isCustom: false },
              { id: 4, name: '技术分享会', isCustom: true }
            ]
          }
        };
      } else {
        task.value = {
          id: taskId.value,
          type: 'REPORT_REVIEW',
          status: 'PENDING',
          createdAt: '2023-06-18 09:15:36',
          priority: 'HIGH',
          report: {
            id: 201,
            itemType: Math.random() < 0.5 ? 'RATING' : 'MESSAGE',
            reporterName: '学生_李明',
            reportTime: '2023-06-17 23:45:30',
            reason: '内容包含不适当的语言和虚假信息',
            targetCompany: '未来科技有限公司',
            content: Math.random() < 0.5 ? {
              // 评价内容
              id: 301,
              score: 1.5,
              comment: '这家公司面试时承诺的条件与实际完全不符，还存在加班不给加班费的情况，希望同学们谨慎考虑！',
              ratingTime: '2023-06-15 18:20:15',
              isAnonymous: true
            } : {
              // 消息内容
              id: 401,
              senderName: 'HR小王',
              sendTime: '2023-06-16 15:30:22',
              type: 'text',
              content: '你的简历太烂了，这种水平还来应聘，浪费大家时间！',
              context: [
                {
                  id: 400,
                  senderName: '学生_李明',
                  sendTime: '2023-06-16 15:25:10',
                  content: '您好，请问我的面试结果如何？'
                },
                {
                  id: 401,
                  senderName: 'HR小王',
                  sendTime: '2023-06-16 15:30:22',
                  content: '你的简历太烂了，这种水平还来应聘，浪费大家时间！'
                },
                {
                  id: 402,
                  senderName: '学生_李明',
                  sendTime: '2023-06-16 15:32:05',
                  content: '好的，谢谢您的反馈，我会继续努力提升自己的。'
                }
              ]
            }
          }
        };
      }
      
      loading.value = false;
    }, 800);
  } catch (error) {
    console.error('加载任务详情失败:', error);
    ElMessage.error('加载任务详情失败，请稍后重试');
    loading.value = false;
  }
};

// 提交审核结果
const submitApproval = async () => {
  // 表单验证
  if (task.value.type === 'COMPANY_CERTIFICATION' && approvalForm.action === 'approve' && !approvalForm.expiryDate) {
    return ElMessage.warning('请选择认证有效期');
  }
  
  if (approvalForm.action === 'reject' && !approvalForm.notes) {
    return ElMessage.warning('驳回时请填写备注说明');
  }
  
  // 确认提交
  ElMessageBox.confirm(
    approvalForm.action === 'approve' 
      ? (task.value.type === 'REPORT_REVIEW' ? '确认此内容违规并删除？' : '确认通过审核？')
      : (task.value.type === 'REPORT_REVIEW' ? '确认此内容不违规？' : '确认驳回申请？'),
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    submitting.value = true;
    try {
      // 实际开发时使用API调用
      // await submitTaskApproval(taskId.value, {
      //   action: approvalForm.action,
      //   notes: approvalForm.notes,
      //   expiryDate: approvalForm.expiryDate,
      //   reduceScore: approvalForm.reduceScore,
      //   scoreReduction: approvalForm.scoreReduction
      // });
      
      // 模拟提交成功
      setTimeout(() => {
        ElMessage.success('处理成功');
        submitting.value = false;
        // 更新本地任务状态
        task.value.status = approvalForm.action === 'approve' ? 'COMPLETED' : 'REJECTED';
      }, 800);
    } catch (error) {
      console.error('提交审核结果失败:', error);
      ElMessage.error('提交审核结果失败，请稍后重试');
      submitting.value = false;
    }
  }).catch(() => {});
};

// 生命周期钩子
onMounted(() => {
  loadTaskDetail();
});
</script>

<style scoped>
.task-detail-container {
  padding: 20px;
}

.loading-container, .not-found {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.task-content {
  margin-top: 20px;
}

.doc-preview {
  margin-top: 20px;
}

.license-image {
  max-width: 100%;
  max-height: 400px;
  border: 1px solid #e6e6e6;
}

.empty-doc {
  border: 1px dashed #e6e6e6;
  padding: 20px;
  border-radius: 4px;
}

.action-panel {
  margin-top: 30px;
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 4px;
  border: 1px solid #e6e6e6;
}

.job-description, .job-requirements {
  white-space: pre-line;
}

.tags-section {
  margin-top: 20px;
}

.tags-container {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.job-tag, .welfare-tag {
  margin-right: 0;
}

.reported-content {
  margin-top: 20px;
}

.rating-header, .message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rating-score {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-meta, .message-meta {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 14px;
}

.anonymous-tag {
  color: #e6a23c;
}

.chat-context {
  margin-top: 20px;
}

.timeline-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.message-image {
  max-width: 300px;
}

.message-file {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
}

.score-input {
  margin-left: 10px;
}
</style> 