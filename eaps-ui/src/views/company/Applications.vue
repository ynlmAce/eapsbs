<template>
  <div class="applications-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>收到的申请</h2>
          <div class="view-toggle">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button label="job">按岗位查看</el-radio-button>
              <el-radio-button label="student">按学生查看</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      
      <!-- 按岗位查看 -->
      <div v-if="viewMode === 'job'">
        <div class="filter-container">
          <el-input
            v-model="jobSearchKeyword"
            placeholder="搜索岗位名称"
            clearable
            style="width: 200px;"
            @clear="filterJobApplications"
          />
          <el-select 
            v-model="jobStatusFilter" 
            placeholder="岗位状态" 
            clearable
            style="width: 150px;"
            @change="filterJobApplications"
          >
            <el-option
              v-for="item in jobStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-button type="primary" @click="filterJobApplications">筛选</el-button>
        </div>
        
        <el-collapse v-model="activeJobPanels">
          <el-collapse-item 
            v-for="job in filteredJobs" 
            :key="job.id" 
            :name="job.id"
          >
            <template #title>
              <div class="job-collapse-title">
                <span class="job-title">{{ job.title }}</span>
                <el-tag :type="getJobStatusType(job.status)" size="small">{{ getJobStatusText(job.status) }}</el-tag>
                <span class="application-count">{{ job.applicationCount }}份申请</span>
              </div>
            </template>
            
            <el-table
              :data="job.applications"
              style="width: 100%"
              v-loading="job.loading"
              row-key="id"
            >
              <el-table-column label="学生信息" min-width="180">
                <template #default="scope">
                  <div class="student-info">
                    <el-avatar :size="40" :src="scope.row.studentAvatar || defaultAvatar"></el-avatar>
                    <div class="student-detail">
                      <div class="student-name">{{ scope.row.studentName }}</div>
                      <div class="student-school">{{ scope.row.studentSchool }} · {{ scope.row.studentMajor }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="applyTime" label="投递时间" width="180" sortable />
              <el-table-column prop="status" label="状态" width="150">
                <template #default="scope">
                  <el-select 
                    v-model="scope.row.status" 
                    size="small" 
                    @change="updateApplicationStatus(scope.row)"
                    :loading="scope.row.updating"
                  >
                    <el-option
                      v-for="item in applicationStatusOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280" fixed="right">
                <template #default="scope">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="viewStudentProfile(scope.row.studentId)"
                    plain
                  >
                    查看档案
                  </el-button>
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="downloadResume(scope.row)"
                    :disabled="!scope.row.hasResume"
                    plain
                  >
                    下载简历
                  </el-button>
                  <el-button 
                    type="success" 
                    size="small" 
                    @click="contactStudent(scope.row)"
                    plain
                  >
                    联系学生
                  </el-button>
                  <el-dropdown trigger="click" @command="handleMoreAction">
                    <el-button size="small" plain>
                      更多<i class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item :command="{type: 'favorite', application: scope.row}">
                          {{ scope.row.isFavorite ? '取消收藏' : '收藏学生' }}
                        </el-dropdown-item>
                        <el-dropdown-item :command="{type: 'blacklist', application: scope.row}">
                          {{ scope.row.isBlacklisted ? '移出黑名单' : '加入黑名单' }}
                        </el-dropdown-item>
                        <el-dropdown-item :command="{type: 'notes', application: scope.row}">
                          {{ scope.row.hasNotes ? '编辑备注' : '添加备注' }}
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </template>
              </el-table-column>
            </el-table>
          </el-collapse-item>
        </el-collapse>
        
        <el-empty v-if="filteredJobs.length === 0" description="暂无符合条件的岗位申请"></el-empty>
      </div>
      
      <!-- 按学生查看 -->
      <div v-else>
        <div class="filter-container">
          <el-input
            v-model="studentSearchKeyword"
            placeholder="搜索学生姓名/学校"
            clearable
            style="width: 200px;"
            @clear="filterStudentApplications"
          />
          <el-select 
            v-model="studentStatusFilter" 
            placeholder="申请状态" 
            clearable 
            style="width: 150px;"
            @change="filterStudentApplications"
          >
            <el-option
              v-for="item in applicationStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-select 
            v-model="studentTagFilter" 
            placeholder="学生标签" 
            clearable
            style="width: 150px;"
            @change="filterStudentApplications"
          >
            <el-option label="已收藏" value="favorite" />
            <el-option label="已拉黑" value="blacklist" />
          </el-select>
          <el-button type="primary" @click="filterStudentApplications">筛选</el-button>
        </div>
        
        <el-table
          :data="filteredStudentApplications"
          style="width: 100%"
          v-loading="loading"
          row-key="id"
        >
          <el-table-column label="学生信息" min-width="180">
            <template #default="scope">
              <div class="student-info">
                <el-avatar :size="40" :src="scope.row.studentAvatar || defaultAvatar"></el-avatar>
                <div class="student-detail">
                  <div class="student-name">
                    {{ scope.row.studentName }}
                    <el-tag v-if="scope.row.isFavorite" type="warning" size="small">已收藏</el-tag>
                    <el-tag v-if="scope.row.isBlacklisted" type="danger" size="small">已拉黑</el-tag>
                  </div>
                  <div class="student-school">{{ scope.row.studentSchool }} · {{ scope.row.studentMajor }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="jobTitle" label="应聘岗位" min-width="150" />
          <el-table-column prop="applyTime" label="投递时间" width="180" sortable />
          <el-table-column prop="status" label="状态" width="150">
            <template #default="scope">
              <el-select 
                v-model="scope.row.status" 
                size="small" 
                @change="updateApplicationStatus(scope.row)"
                :loading="scope.row.updating"
              >
                <el-option
                  v-for="item in applicationStatusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewStudentProfile(scope.row.studentId)"
                plain
              >
                查看档案
              </el-button>
              <el-button 
                type="primary" 
                size="small" 
                @click="downloadResume(scope.row)"
                :disabled="!scope.row.hasResume"
                plain
              >
                下载简历
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click="contactStudent(scope.row)"
                plain
              >
                联系学生
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
            :total="totalStudentApplications"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
        
        <el-empty v-if="filteredStudentApplications.length === 0" description="暂无符合条件的申请记录"></el-empty>
      </div>
    </el-card>
    
    <!-- 添加/编辑备注对话框 -->
    <el-dialog
      v-model="notesDialogVisible"
      title="学生备注"
      width="30%"
    >
      <div v-if="selectedApplication">
        <p>学生：{{ selectedApplication.studentName }}</p>
        <p>应聘岗位：{{ selectedApplication.jobTitle }}</p>
        <el-input
          v-model="applicationNotes"
          type="textarea"
          :rows="4"
          placeholder="请输入备注内容..."
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="notesDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveApplicationNotes" :loading="savingNotes">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 批量下载简历对话框 -->
    <el-dialog
      v-model="batchDownloadVisible"
      title="批量下载简历"
      width="40%"
    >
      <div>
        <p>选择要下载的简历类型：</p>
        <el-checkbox-group v-model="downloadOptions">
          <el-checkbox label="attachments">附件简历</el-checkbox>
          <el-checkbox label="structured">生成标准简历</el-checkbox>
        </el-checkbox-group>
        
        <div class="download-info">
          <p>当前选择岗位：{{ selectedJob ? selectedJob.title : '全部岗位' }}</p>
          <p>可下载简历数量：{{ availableResumesCount }}</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDownloadVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBatchDownload" :loading="downloading">
            下载
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
// 引入API相关函数，实际开发时需要实现这些函数
// import { getJobApplications, getStudentApplications, updateApplicationStatus, toggleFavorite, toggleBlacklist, saveNotes, batchDownloadResumes } from '@/api/company';

const router = useRouter();
const viewMode = ref('job');
const loading = ref(false);
const defaultAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png');

// 按岗位查看相关变量
const jobs = ref([]);
const activeJobPanels = ref([]);
const jobSearchKeyword = ref('');
const jobStatusFilter = ref('');

// 按学生查看相关变量
const studentApplications = ref([]);
const studentSearchKeyword = ref('');
const studentStatusFilter = ref('');
const studentTagFilter = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const totalStudentApplications = ref(0);

// 备注对话框相关
const notesDialogVisible = ref(false);
const selectedApplication = ref(null);
const applicationNotes = ref('');
const savingNotes = ref(false);

// 批量下载相关
const batchDownloadVisible = ref(false);
const downloadOptions = ref(['attachments']);
const selectedJob = ref(null);
const downloading = ref(false);
const availableResumesCount = ref(0);

// 岗位状态选项
const jobStatusOptions = [
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '招聘中' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'EXPIRED', label: '已结束' }
];

// 申请状态选项
const applicationStatusOptions = [
  { value: 'APPLIED', label: '已投递' },
  { value: 'VIEWED', label: '已查看' },
  { value: 'INTERVIEW_INVITED', label: '已邀请面试' },
  { value: 'INTERVIEWED', label: '已面试' },
  { value: 'OFFER_RECEIVED', label: '已发放Offer' },
  { value: 'REJECTED', label: '已拒绝' }
];

// 筛选后的岗位列表
const filteredJobs = computed(() => {
  if (!jobSearchKeyword.value && !jobStatusFilter.value) {
    return jobs.value;
  }
  
  return jobs.value.filter(job => {
    const matchKeyword = !jobSearchKeyword.value || job.title.toLowerCase().includes(jobSearchKeyword.value.toLowerCase());
    const matchStatus = !jobStatusFilter.value || job.status === jobStatusFilter.value;
    return matchKeyword && matchStatus;
  });
});

// 筛选后的学生申请列表
const filteredStudentApplications = computed(() => {
  if (!studentSearchKeyword.value && !studentStatusFilter.value && !studentTagFilter.value) {
    return studentApplications.value;
  }
  
  return studentApplications.value.filter(app => {
    const matchKeyword = !studentSearchKeyword.value || 
      app.studentName.toLowerCase().includes(studentSearchKeyword.value.toLowerCase()) ||
      app.studentSchool.toLowerCase().includes(studentSearchKeyword.value.toLowerCase());
    
    const matchStatus = !studentStatusFilter.value || app.status === studentStatusFilter.value;
    
    let matchTag = true;
    if (studentTagFilter.value === 'favorite') {
      matchTag = app.isFavorite;
    } else if (studentTagFilter.value === 'blacklist') {
      matchTag = app.isBlacklisted;
    }
    
    return matchKeyword && matchStatus && matchTag;
  });
});

// 获取岗位状态标签类型
const getJobStatusType = (status) => {
  const statusMap = {
    'PENDING': 'info',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'EXPIRED': 'info'
  };
  return statusMap[status] || 'info';
};

// 获取岗位状态显示文本
const getJobStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '招聘中',
    'REJECTED': '已驳回',
    'EXPIRED': '已结束'
  };
  return statusMap[status] || '未知状态';
};

// 加载岗位申请数据
const loadJobApplications = async () => {
  loading.value = true;
  try {
    // 实际开发时使用API调用
    // const res = await getJobApplications();
    // jobs.value = res.body.list;
    
    // 模拟数据
    setTimeout(() => {
      jobs.value = [
        {
          id: 1,
          title: '前端开发工程师',
          status: 'APPROVED',
          applicationCount: 12,
          applications: generateApplications(12, '前端开发工程师'),
          loading: false
        },
        {
          id: 2,
          title: 'Java后端工程师',
          status: 'APPROVED',
          applicationCount: 8,
          applications: generateApplications(8, 'Java后端工程师'),
          loading: false
        },
        {
          id: 3,
          title: '产品经理',
          status: 'EXPIRED',
          applicationCount: 5,
          applications: generateApplications(5, '产品经理'),
          loading: false
        },
        {
          id: 4,
          title: 'UI设计师',
          status: 'PENDING',
          applicationCount: 0,
          applications: [],
          loading: false
        }
      ];
      loading.value = false;
      
      // 默认展开第一个面板
      if (jobs.value.length > 0) {
        activeJobPanels.value = [jobs.value[0].id];
      }
    }, 500);
  } catch (error) {
    console.error('加载岗位申请数据失败:', error);
    ElMessage.error('加载岗位申请数据失败，请稍后重试');
    loading.value = false;
  }
};

// 加载学生申请数据
const loadStudentApplications = async () => {
  loading.value = true;
  try {
    // 实际开发时使用API调用
    // const res = await getStudentApplications({
    //   page: currentPage.value,
    //   pageSize: pageSize.value,
    //   status: studentStatusFilter.value,
    //   keyword: studentSearchKeyword.value,
    //   tag: studentTagFilter.value
    // });
    // studentApplications.value = res.body.list;
    // totalStudentApplications.value = res.body.total;
    
    // 模拟数据
    setTimeout(() => {
      studentApplications.value = [
        ...generateApplications(8, '前端开发工程师'),
        ...generateApplications(7, 'Java后端工程师'),
        ...generateApplications(5, '产品经理')
      ];
      totalStudentApplications.value = 20;
      loading.value = false;
    }, 500);
  } catch (error) {
    console.error('加载学生申请数据失败:', error);
    ElMessage.error('加载学生申请数据失败，请稍后重试');
    loading.value = false;
  }
};

// 生成模拟申请数据
const generateApplications = (count, jobTitle) => {
  const applications = [];
  const schools = ['清华大学', '北京大学', '复旦大学', '上海交通大学', '武汉大学', '浙江大学'];
  const majors = ['计算机科学与技术', '软件工程', '电子信息工程', '数据科学', '通信工程'];
  const statuses = ['APPLIED', 'VIEWED', 'INTERVIEW_INVITED', 'INTERVIEWED', 'OFFER_RECEIVED', 'REJECTED'];
  
  for (let i = 0; i < count; i++) {
    applications.push({
      id: `${jobTitle}-${i}`,
      jobId: jobTitle === '前端开发工程师' ? 1 : (jobTitle === 'Java后端工程师' ? 2 : 3),
      jobTitle,
      studentId: 1000 + i,
      studentName: `学生${i + 1}`,
      studentAvatar: i % 3 === 0 ? `https://cube.elemecdn.com/${i % 10}/88/03b0d39583f48206768a7534e55bcpng.png` : '',
      studentSchool: schools[i % schools.length],
      studentMajor: majors[i % majors.length],
      applyTime: new Date(Date.now() - i * 86400000).toISOString().substring(0, 19).replace('T', ' '),
      status: statuses[i % statuses.length],
      hasResume: i % 4 !== 3,
      isFavorite: i % 5 === 0,
      isBlacklisted: i % 7 === 0,
      hasNotes: i % 3 === 0,
      notes: i % 3 === 0 ? '这个学生表现很好，有潜力' : '',
      updating: false
    });
  }
  
  return applications;
};

// 筛选岗位申请
const filterJobApplications = () => {
  // 实际开发时可能需要重新加载数据
  // loadJobApplications();
};

// 筛选学生申请
const filterStudentApplications = () => {
  // 实际开发时重新加载数据
  loadStudentApplications();
};

// 更新申请状态
const updateApplicationStatus = async (application) => {
  application.updating = true;
  try {
    // 实际开发时使用API调用
    // await updateApplicationStatus(application.id, application.status);
    
    // 模拟更新成功
    setTimeout(() => {
      ElMessage.success('状态已更新');
      application.updating = false;
    }, 500);
  } catch (error) {
    console.error('更新申请状态失败:', error);
    ElMessage.error('更新申请状态失败，请稍后重试');
    application.updating = false;
  }
};

// 查看学生档案
const viewStudentProfile = (studentId) => {
  // 跳转到学生档案页
  window.open(`/student-profile/${studentId}`, '_blank');
};

// 下载简历
const downloadResume = (application) => {
  if (!application.hasResume) {
    return ElMessage.warning('该学生未上传简历');
  }
  
  // 实际开发时使用API调用下载简历
  // window.open(`/api/resume/download/${application.studentId}`, '_blank');
  
  ElMessage.success('简历下载中...');
};

// 联系学生
const contactStudent = (application) => {
  // 跳转到聊天页面
  router.push({
    name: 'CompanyChat',
    params: { 
      type: 'student',
      id: application.studentId
    },
    query: { 
      jobId: application.jobId,
      jobTitle: application.jobTitle
    }
  });
};

// 处理更多操作
const handleMoreAction = ({ type, application }) => {
  if (type === 'favorite') {
    toggleFavoriteStudent(application);
  } else if (type === 'blacklist') {
    toggleBlacklistStudent(application);
  } else if (type === 'notes') {
    openNotesDialog(application);
  }
};

// 切换收藏学生
const toggleFavoriteStudent = async (application) => {
  try {
    // 实际开发时使用API调用
    // await toggleFavorite(application.id, !application.isFavorite);
    
    // 模拟操作成功
    setTimeout(() => {
      application.isFavorite = !application.isFavorite;
      ElMessage.success(application.isFavorite ? '已收藏学生' : '已取消收藏');
    }, 500);
  } catch (error) {
    console.error('操作失败:', error);
    ElMessage.error('操作失败，请稍后重试');
  }
};

// 切换黑名单
const toggleBlacklistStudent = async (application) => {
  if (!application.isBlacklisted) {
    // 确认加入黑名单
    ElMessageBox.confirm('加入黑名单后，该学生将无法再次申请贵公司岗位，是否继续？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        // 实际开发时使用API调用
        // await toggleBlacklist(application.id, true);
        
        // 模拟操作成功
        setTimeout(() => {
          application.isBlacklisted = true;
          ElMessage.success('已将学生加入黑名单');
        }, 500);
      } catch (error) {
        console.error('操作失败:', error);
        ElMessage.error('操作失败，请稍后重试');
      }
    }).catch(() => {});
  } else {
    try {
      // 实际开发时使用API调用
      // await toggleBlacklist(application.id, false);
      
      // 模拟操作成功
      setTimeout(() => {
        application.isBlacklisted = false;
        ElMessage.success('已将学生移出黑名单');
      }, 500);
    } catch (error) {
      console.error('操作失败:', error);
      ElMessage.error('操作失败，请稍后重试');
    }
  }
};

// 打开备注对话框
const openNotesDialog = (application) => {
  selectedApplication.value = application;
  applicationNotes.value = application.notes || '';
  notesDialogVisible.value = true;
};

// 保存应用备注
const saveApplicationNotes = async () => {
  if (!selectedApplication.value) return;
  
  savingNotes.value = true;
  try {
    // 实际开发时使用API调用
    // await saveNotes(selectedApplication.value.id, applicationNotes.value);
    
    // 模拟保存成功
    setTimeout(() => {
      selectedApplication.value.notes = applicationNotes.value;
      selectedApplication.value.hasNotes = !!applicationNotes.value;
      
      ElMessage.success('备注已保存');
      notesDialogVisible.value = false;
      savingNotes.value = false;
    }, 500);
  } catch (error) {
    console.error('保存备注失败:', error);
    ElMessage.error('保存备注失败，请稍后重试');
    savingNotes.value = false;
  }
};

// 打开批量下载对话框
const openBatchDownloadDialog = (job = null) => {
  selectedJob.value = job;
  
  // 计算可下载的简历数量
  if (job) {
    availableResumesCount.value = job.applications.filter(app => app.hasResume).length;
  } else {
    availableResumesCount.value = studentApplications.value.filter(app => app.hasResume).length;
  }
  
  batchDownloadVisible.value = true;
};

// 确认批量下载
const confirmBatchDownload = async () => {
  if (downloadOptions.value.length === 0) {
    return ElMessage.warning('请至少选择一种简历类型');
  }
  
  downloading.value = true;
  try {
    // 实际开发时使用API调用
    // await batchDownloadResumes({
    //   jobId: selectedJob.value ? selectedJob.value.id : null,
    //   includeAttachments: downloadOptions.value.includes('attachments'),
    //   includeStructured: downloadOptions.value.includes('structured')
    // });
    
    // 模拟下载
    setTimeout(() => {
      ElMessage.success('简历打包下载中，请稍候...');
      batchDownloadVisible.value = false;
      downloading.value = false;
    }, 1000);
  } catch (error) {
    console.error('批量下载失败:', error);
    ElMessage.error('批量下载失败，请稍后重试');
    downloading.value = false;
  }
};

// 分页相关
const handleSizeChange = (val) => {
  pageSize.value = val;
  loadStudentApplications();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadStudentApplications();
};

// 监听视图模式变化
watch(viewMode, (newValue) => {
  if (newValue === 'job') {
    loadJobApplications();
  } else {
    loadStudentApplications();
  }
});

// 生命周期钩子
onMounted(() => {
  loadJobApplications();
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

.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
}

.job-collapse-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.job-title {
  font-weight: 500;
  font-size: 16px;
}

.application-count {
  color: #909399;
  font-size: 14px;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.student-detail {
  display: flex;
  flex-direction: column;
}

.student-name {
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
}

.student-school {
  font-size: 13px;
  color: #606266;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.download-info {
  margin-top: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

/* 给折叠面板添加一些间距 */
.el-collapse-item {
  margin-bottom: 10px;
}
</style> 