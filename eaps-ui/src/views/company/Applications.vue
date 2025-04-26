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
                    @change="updateApplicationStatusHandler(scope.row)"
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
            @clear="loadStudentApplications"
          />
          <el-select 
            v-model="studentStatusFilter" 
            placeholder="申请状态" 
            clearable 
            style="width: 150px;"
            @change="loadStudentApplications"
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
            @change="loadStudentApplications"
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
                @change="updateApplicationStatusHandler(scope.row)"
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { getCompanyApplications, processApplication, toggleFavoriteStudent, toggleBlacklistStudent } from '@/api/company'
import { batchDownloadResumes } from '@/api/job'
import { createChatSession } from '@/api/chat'
import { callApi } from '@/utils/apiUtils'

const route = useRoute()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 视图模式：按岗位查看或按学生查看
const viewMode = ref('job')

// 筛选参数
const jobSearchKeyword = ref('')
const jobStatusFilter = ref('')
const studentSearchKeyword = ref('')
const studentStatusFilter = ref('')
const studentTagFilter = ref('')

// 岗位相关数据
const activeJobPanels = ref([])
const jobs = ref([])
const filteredJobs = computed(() => {
  let result = [...jobs.value]
  
  // 按关键词筛选
  if (jobSearchKeyword.value) {
    const keyword = jobSearchKeyword.value.trim().toLowerCase()
    result = result.filter(job => 
      job.title && job.title.toLowerCase().includes(keyword)
    )
  }
  
  // 按状态筛选
  if (jobStatusFilter.value) {
    result = result.filter(job => job.status === jobStatusFilter.value)
  }
  
  return result
})

// 学生申请相关数据
const studentApplications = ref([])
const filteredStudentApplications = computed(() => {
  let result = [...studentApplications.value]
  
  // 按关键词筛选
  if (studentSearchKeyword.value) {
    const keyword = studentSearchKeyword.value.trim().toLowerCase()
    result = result.filter(app => 
      (app.studentName && app.studentName.toLowerCase().includes(keyword)) ||
      (app.studentSchool && app.studentSchool.toLowerCase().includes(keyword))
    )
  }
  
  // 按状态筛选
  if (studentStatusFilter.value) {
    result = result.filter(app => app.status === studentStatusFilter.value)
  }
  
  // 按标签筛选
  if (studentTagFilter.value === 'favorite') {
    result = result.filter(app => app.isFavorite)
  } else if (studentTagFilter.value === 'blacklist') {
    result = result.filter(app => app.isBlacklisted)
  }
  
  return result
})

// 状态选项
const jobStatusOptions = [
  { value: 'recruiting', label: '招聘中' },
  { value: 'pending', label: '待审核' },
  { value: 'rejected', label: '已驳回' },
  { value: 'ended', label: '已结束' }
]

const applicationStatusOptions = [
  { value: 'pending', label: '待处理' },
  { value: 'viewed', label: '已查看' },
  { value: 'contacted', label: '已联系' },
  { value: 'interviewed', label: '已面试' },
  { value: 'accepted', label: '已录用' },
  { value: 'rejected', label: '不合适' }
]

// 加载状态
const loading = ref(false)
const selectedApplication = ref(null)
const notesDialogVisible = ref(false)
const applicationNotes = ref('')
const savingNotes = ref(false)
const batchDownloadVisible = ref(false)
const downloadOptions = ref(['attachments'])
const selectedJob = ref(null)
const availableResumesCount = ref(0)
const downloading = ref(false)
const totalStudentApplications = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 获取岗位状态对应的类型
const getJobStatusType = (status) => {
  switch (status) {
    case 'recruiting':
    case 'active':
      return 'success'
    case 'pending':
      return 'warning'
    case 'rejected':
      return 'danger'
    case 'ended':
    case 'closed':
      return 'info'
    default:
      return ''
  }
}

// 获取岗位状态文本
const getJobStatusText = (status) => {
  switch (status) {
    case 'recruiting':
    case 'active':
      return '招聘中'
    case 'pending':
      return '待审核'
    case 'rejected':
      return '已驳回'
    case 'ended':
    case 'closed':
      return '已结束'
    default:
      return '未知状态'
  }
}

// 监听路由参数
watch(() => route.query, (query) => {
  if (query.jobId) {
    // 如果URL参数中有jobId，则设置为按岗位查看模式并展开对应岗位
    viewMode.value = 'job'
    
    const jobId = parseInt(query.jobId, 10)
    if (!isNaN(jobId)) {
      // 确保对应岗位面板展开
      if (!activeJobPanels.value.includes(jobId)) {
        activeJobPanels.value.push(jobId)
      }
    }
  }
}, { immediate: true })

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})

// 加载数据
const loadData = async () => {
  loading.value = true
  
  try {
    // 按当前视图模式加载数据
    if (viewMode.value === 'job') {
      await loadJobApplications()
    } else {
      await loadStudentApplications()
    }
  } catch (error) {
    // 错误已由callApi处理
    console.error('加载申请数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载按岗位组织的申请
const loadJobApplications = async () => {
  try {
    const result = await callApi(getCompanyApplications({
      groupBy: 'job'
    }))
    
    jobs.value = result.jobs || []
    
    // 如果URL中有jobId参数，展开对应岗位
    if (route.query.jobId) {
      const jobId = parseInt(route.query.jobId, 10)
      if (!isNaN(jobId) && !activeJobPanels.value.includes(jobId)) {
        activeJobPanels.value.push(jobId)
      }
    }
  } catch (error) {
    // 错误已由callApi处理
    console.error('加载岗位申请数据失败:', error)
    jobs.value = []
  }
}

// 加载按学生组织的申请
const loadStudentApplications = async () => {
  try {
    const result = await callApi(getCompanyApplications({
      groupBy: 'student',
      page: currentPage.value,
      pageSize: pageSize.value,
      status: studentStatusFilter.value,
      keyword: studentSearchKeyword.value,
      tag: studentTagFilter.value
    }))
    
    studentApplications.value = result.applications || []
    totalStudentApplications.value = result.total || 0
  } catch (error) {
    // 错误已由callApi处理
    console.error('加载学生申请数据失败:', error)
    studentApplications.value = []
    totalStudentApplications.value = 0
  }
}

// 筛选岗位申请
const filterJobApplications = () => {
  // 由computed属性filteredJobs自动处理筛选
}

// 筛选学生申请
const filterStudentApplications = () => {
  currentPage.value = 1
  loadStudentApplications()
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  loadStudentApplications()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadStudentApplications()
}

// 更新申请状态
const updateApplicationStatusHandler = async (application) => {
  if (application.updating) return
  
  application.updating = true
  try {
    await callApi(processApplication(
      application.id, 
      application.status, 
      ''
    ), {
      showSuccess: true,
      successMsg: '申请状态更新成功'
    })
    
    // 保存原状态，以便出错时恢复
    application.originalStatus = application.status
  } catch (error) {
    // 错误已由callApi处理
    console.error('更新申请状态失败:', error)
    
    // 恢复原状态
    application.status = application.originalStatus || 'pending'
  } finally {
    application.updating = false
  }
}

// 查看学生档案
const viewStudentProfile = (studentId) => {
  ElMessage.info('查看学生档案功能暂未实现')
  // TODO: 实现查看学生档案功能
}

// 下载简历
const downloadResume = async (application) => {
  if (!application.hasResume) {
    ElMessage.warning('该学生未上传简历文件')
    return
  }
  
  const loading = ElLoading.service({
    lock: true,
    text: '下载简历中...',
    background: 'rgba(255, 255, 255, 0.7)'
  })
  
  try {
    const response = await batchDownloadResumes([application.id])
    
    // 处理文件下载
    const blob = new Blob([response], { type: 'application/octet-stream' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${application.studentName || '学生'}_简历.zip`
    link.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success('简历下载成功')
  } catch (error) {
    ElMessage.error('下载简历失败: ' + (error.message || '未知错误'))
    console.error('下载简历失败:', error)
  } finally {
    loading.close()
  }
}

// 联系学生
const contactStudent = async (application) => {
  const loading = ElLoading.service({
    lock: true,
    text: '创建会话中...',
    background: 'rgba(255, 255, 255, 0.7)'
  })
  
  try {
    const result = await callApi(createChatSession(
      'SE', // 学生-企业会话
      application.studentId,
      application.jobId,
      '您好，我们已收到您的简历，想与您进一步沟通。'
    ))
    
    // 跳转到聊天页
    if (result && result.sessionId) {
      window.location.href = `/company/chat?sessionId=${result.sessionId}`
    } else {
      ElMessage.error('创建会话失败，请重试')
    }
  } catch (error) {
    // 错误已由callApi处理
    console.error('创建会话失败:', error)
  } finally {
    loading.close()
  }
}

// 处理更多操作
const handleMoreAction = async (command) => {
  const { type, application } = command
  
  if (!application) return
  
  switch (type) {
    case 'favorite':
      handleFavoriteStudent(application)
      break
    case 'blacklist':
      handleBlacklistStudent(application)
      break
    case 'notes':
      handleAddNotes(application)
      break
    default:
      break
  }
}

// 收藏学生
const handleFavoriteStudent = async (application) => {
  try {
    if (application.isFavorite) {
      // 取消收藏
      await callApi(toggleFavoriteStudent(application.studentId, false), {
        showSuccess: true,
        successMsg: '已取消收藏'
      })
      application.isFavorite = false
    } else {
      // 添加收藏
      await callApi(toggleFavoriteStudent(application.studentId, true), {
        showSuccess: true,
        successMsg: '已收藏学生'
      })
      application.isFavorite = true
    }
  } catch (error) {
    // 错误已由callApi处理
    console.error('收藏学生操作失败:', error)
  }
}

// 黑名单学生
const handleBlacklistStudent = async (application) => {
  try {
    if (application.isBlacklisted) {
      // 移出黑名单
      await callApi(toggleBlacklistStudent(application.studentId, '', false), {
        showSuccess: true,
        successMsg: '已将学生移出黑名单'
      })
      application.isBlacklisted = false
    } else {
      // 添加到黑名单
      // 先让用户输入原因
      ElMessageBox.prompt('请输入将该学生加入黑名单的原因', '加入黑名单', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /\S+/,
        inputErrorMessage: '请输入原因'
      }).then(async ({ value }) => {
        await callApi(toggleBlacklistStudent(application.studentId, value, true), {
          showSuccess: true,
          successMsg: '已将学生加入黑名单'
        })
        application.isBlacklisted = true
      }).catch(() => {})
    }
  } catch (error) {
    // 错误已由callApi处理
    console.error('黑名单操作失败:', error)
  }
}

// 添加备注
const handleAddNotes = (application) => {
  ElMessageBox.prompt(
    '请输入对该学生的备注信息',
    application.hasNotes ? '编辑备注' : '添加备注',
    {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValue: application.notes || '',
      inputType: 'textarea'
    }
  ).then(async ({ value }) => {
    try {
      await callApi(processApplication(
        application.id,
        application.status,
        value
      ), {
        showSuccess: true,
        successMsg: '备注保存成功'
      })
      
      application.notes = value
      application.hasNotes = !!value
    } catch (error) {
      // 错误已由callApi处理
      console.error('保存备注失败:', error)
    }
  }).catch(() => {})
}
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