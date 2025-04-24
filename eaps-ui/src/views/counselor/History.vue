<template>
  <div class="history-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>审核历史记录</h2>
          <div class="filters">
            <el-select v-model="typeFilter" placeholder="任务类型" clearable>
              <el-option
                v-for="item in taskTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-select v-model="resultFilter" placeholder="处理结果" clearable>
              <el-option
                v-for="item in resultOptions"
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
            />
            <el-button type="primary" @click="fetchHistoryRecords">查询</el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="historyRecords" 
        style="width: 100%" 
        v-loading="loading"
        row-key="id"
      >
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="taskType" label="任务类型" width="150">
          <template #default="scope">
            {{ getTaskTypeText(scope.row.taskType) }}
          </template>
        </el-table-column>
        <el-table-column prop="targetName" label="审核对象" min-width="180" />
        <el-table-column prop="operationTime" label="处理时间" width="180" sortable />
        <el-table-column prop="result" label="处理结果" width="120">
          <template #default="scope">
            <el-tag :type="getResultType(scope.row.result)">
              {{ getResultText(scope.row.result) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="处理备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewDetail(scope.row)"
              plain
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalRecords"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="'记录详情 #' + (selectedRecord ? selectedRecord.id : '')"
      width="60%"
    >
      <div v-if="selectedRecord" class="record-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务类型">{{ getTaskTypeText(selectedRecord.taskType) }}</el-descriptions-item>
          <el-descriptions-item label="处理结果">
            <el-tag :type="getResultType(selectedRecord.result)">
              {{ getResultText(selectedRecord.result) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核对象">{{ selectedRecord.targetName }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ selectedRecord.operationTime }}</el-descriptions-item>
          <el-descriptions-item label="处理备注" :span="2">{{ selectedRecord.notes || '无' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-content" v-if="selectedRecord.taskType === 'COMPANY_CERTIFICATION'">
          <h3>企业信息</h3>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="企业名称">{{ selectedRecord.detail.companyName }}</el-descriptions-item>
            <el-descriptions-item label="社会信用代码">{{ selectedRecord.detail.unifiedSocialCreditCode }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ selectedRecord.detail.contactPerson }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ selectedRecord.detail.contactPhone }}</el-descriptions-item>
            <el-descriptions-item label="认证状态">
              <el-tag :type="selectedRecord.result === 'APPROVED' ? 'success' : 'danger'">
                {{ selectedRecord.result === 'APPROVED' ? '已认证' : '未认证' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="认证有效期" v-if="selectedRecord.result === 'APPROVED'">
              {{ selectedRecord.detail.expiryDate }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-content" v-else-if="selectedRecord.taskType === 'JOB_APPROVAL'">
          <h3>岗位信息</h3>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="岗位名称">{{ selectedRecord.detail.jobTitle }}</el-descriptions-item>
            <el-descriptions-item label="发布企业">{{ selectedRecord.detail.companyName }}</el-descriptions-item>
            <el-descriptions-item label="工作地点">{{ selectedRecord.detail.location }}</el-descriptions-item>
            <el-descriptions-item label="薪资范围">{{ selectedRecord.detail.salary }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="selectedRecord.result === 'APPROVED' ? 'success' : 'danger'">
                {{ selectedRecord.result === 'APPROVED' ? '已通过' : '已拒绝' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-content" v-else-if="selectedRecord.taskType === 'REPORT_REVIEW'">
          <h3>举报信息</h3>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="举报类型">{{ getReportTypeText(selectedRecord.detail.reportType) }}</el-descriptions-item>
            <el-descriptions-item label="举报人">{{ selectedRecord.detail.reporterName }}</el-descriptions-item>
            <el-descriptions-item label="举报原因">{{ selectedRecord.detail.reason }}</el-descriptions-item>
            <el-descriptions-item label="处理结果">
              <el-tag :type="selectedRecord.result === 'APPROVED' ? 'success' : 'warning'">
                {{ selectedRecord.result === 'APPROVED' ? '确认违规并已删除' : '不违规' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="处罚措施" v-if="selectedRecord.result === 'APPROVED' && selectedRecord.detail.scoreReduction">
              扣除行为分 {{ selectedRecord.detail.scoreReduction }} 分
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
// 引入API相关函数，实际开发时需要实现这些函数
// import { getOperationHistory } from '@/api/counselor';

const router = useRouter();
const loading = ref(false);
const historyRecords = ref([]);
const totalRecords = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const typeFilter = ref('');
const resultFilter = ref('');
const dateRange = ref([]);
const detailDialogVisible = ref(false);
const selectedRecord = ref(null);

// 任务类型选项
const taskTypeOptions = [
  { value: 'COMPANY_CERTIFICATION', label: '企业认证' },
  { value: 'JOB_APPROVAL', label: '岗位审核' },
  { value: 'REPORT_REVIEW', label: '举报处理' }
];

// 处理结果选项
const resultOptions = [
  { value: 'APPROVED', label: '已通过/确认违规' },
  { value: 'REJECTED', label: '已拒绝/不违规' }
];

// 获取任务类型显示文本
const getTaskTypeText = (type) => {
  const typeMap = {
    'COMPANY_CERTIFICATION': '企业认证',
    'JOB_APPROVAL': '岗位审核',
    'REPORT_REVIEW': '举报处理'
  };
  return typeMap[type] || '未知类型';
};

// 获取举报类型显示文本
const getReportTypeText = (type) => {
  const typeMap = {
    'RATING': '企业评价',
    'MESSAGE': '聊天消息',
    'CHAT_SESSION': '聊天会话'
  };
  return typeMap[type] || '未知类型';
};

// 获取处理结果标签类型
const getResultType = (result) => {
  const resultMap = {
    'APPROVED': 'success',
    'REJECTED': 'danger'
  };
  return resultMap[result] || 'info';
};

// 获取处理结果显示文本
const getResultText = (result) => {
  const resultMap = {
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  };
  return resultMap[result] || '未知结果';
};

// 获取历史记录
const fetchHistoryRecords = async () => {
  loading.value = true;
  
  try {
    // 实际开发时使用API调用
    // const res = await getOperationHistory({
    //   page: currentPage.value,
    //   pageSize: pageSize.value,
    //   type: typeFilter.value,
    //   result: resultFilter.value,
    //   startDate: dateRange.value && dateRange.value[0],
    //   endDate: dateRange.value && dateRange.value[1]
    // });
    // historyRecords.value = res.body.list;
    // totalRecords.value = res.body.total;
    
    // 模拟数据
    setTimeout(() => {
      historyRecords.value = [
        {
          id: 1001,
          taskType: 'COMPANY_CERTIFICATION',
          targetName: '科技星球有限公司',
          operationTime: '2023-06-15 14:30:25',
          result: 'APPROVED',
          notes: '企业资质完备，认证通过',
          detail: {
            companyName: '科技星球有限公司',
            unifiedSocialCreditCode: '91110108MA01GR1T2L',
            contactPerson: '张经理',
            contactPhone: '13800138000',
            expiryDate: '2024-06-15'
          }
        },
        {
          id: 1002,
          taskType: 'JOB_APPROVAL',
          targetName: '前端开发工程师 - 科技星球有限公司',
          operationTime: '2023-06-16 10:20:18',
          result: 'APPROVED',
          notes: '岗位信息真实，福利标签合规',
          detail: {
            jobTitle: '前端开发工程师',
            companyName: '科技星球有限公司',
            location: '北京市海淀区',
            salary: '15k-25k'
          }
        },
        {
          id: 1003,
          taskType: 'REPORT_REVIEW',
          targetName: '举报企业评价 - 未来科技有限公司',
          operationTime: '2023-06-17 16:45:30',
          result: 'REJECTED',
          notes: '评价内容属于主观表达，未发现明显违规',
          detail: {
            reportType: 'RATING',
            reporterName: 'HR小李',
            reason: '恶意评价，包含虚假信息'
          }
        },
        {
          id: 1004,
          taskType: 'REPORT_REVIEW',
          targetName: '举报聊天消息 - HR小王',
          operationTime: '2023-06-18 09:15:36',
          result: 'APPROVED',
          notes: '消息内容包含侮辱性语言，违反平台规范',
          detail: {
            reportType: 'MESSAGE',
            reporterName: '学生_李明',
            reason: '内容包含不适当的语言和虚假信息',
            scoreReduction: 5
          }
        },
        {
          id: 1005,
          taskType: 'COMPANY_CERTIFICATION',
          targetName: '智行科技有限公司',
          operationTime: '2023-06-19 11:30:40',
          result: 'REJECTED',
          notes: '营业执照与填写信息不符，请修改后重新提交',
          detail: {
            companyName: '智行科技有限公司',
            unifiedSocialCreditCode: '91110108MA01GR3T4M',
            contactPerson: '李总',
            contactPhone: '13900139000'
          }
        }
      ];
      totalRecords.value = 5;
      loading.value = false;
    }, 500);
  } catch (error) {
    console.error('获取历史记录失败:', error);
    ElMessage.error('获取历史记录失败，请稍后重试');
    loading.value = false;
  }
};

// 查看详情
const viewDetail = (record) => {
  selectedRecord.value = record;
  detailDialogVisible.value = true;
};

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchHistoryRecords();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchHistoryRecords();
};

// 生命周期钩子
onMounted(() => {
  fetchHistoryRecords();
});
</script>

<style scoped>
.history-container {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.record-detail {
  padding: 10px;
}

.detail-content {
  margin-top: 20px;
}
</style> 