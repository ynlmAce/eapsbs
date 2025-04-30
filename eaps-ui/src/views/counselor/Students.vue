<template>
  <div class="students-page">
    <h2 class="page-title">学生管理</h2>
    <el-form :inline="true" :model="filterForm" class="filter-form">
      <el-form-item label="姓名">
        <el-input v-model="filterForm.name" placeholder="请输入" clearable />
      </el-form-item>
      <el-form-item label="学号">
        <el-input v-model="filterForm.studentId" placeholder="请输入" clearable />
      </el-form-item>
      <el-form-item label="专业">
        <el-input v-model="filterForm.major" placeholder="请输入" clearable />
      </el-form-item>
      <el-form-item label="年级">
        <el-input v-model="filterForm.grade" placeholder="请输入" clearable />
      </el-form-item>
      <el-form-item label="分配状态">
        <el-select v-model="filterForm.assigned" placeholder="全部" clearable>
          <el-option label="未分配" value="unassigned" />
          <el-option label="已分配" value="assigned" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="studentList"
      v-loading="loading"
      row-key="id"
      @selection-change="handleSelectionChange"
      style="width: 100%"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="studentId" label="学号" />
      <el-table-column prop="major" label="专业" />
      <el-table-column prop="grade" label="年级" />
      <el-table-column prop="counselorName" label="辅导员">
        <template #default="scope">
          <span v-if="scope.row.counselorName">{{ scope.row.counselorName }}</span>
          <span v-else class="unassigned">未分配</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.counselorId" size="small" type="primary" @click="contactStudent(row)">联系学生</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="actions">
      <el-button
        type="primary"
        :disabled="selectedStudentIds.length === 0"
        @click="assignToMe"
      >关联到我</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStudentList, assignStudentsToCounselor } from '@/api/student'
import { getCounselorProfile } from '@/api/counselor'
import { createStudentCounselorSession } from '@/api/chat'
import { useRouter } from 'vue-router'

const filterForm = reactive({
  name: '',
  studentId: '',
  major: '',
  grade: '',
  assigned: ''
})
const studentList = ref([])
const loading = ref(false)
const selectedStudentIds = ref([])
const counselorProfileId = ref(null)
const router = useRouter()

const handleSearch = async () => {
  loading.value = true
  const params = { ...filterForm }
  // 转换分配状态为后端可识别的参数
  if (params.assigned === 'unassigned') params.counselorAssigned = false
  if (params.assigned === 'assigned') params.counselorAssigned = true
  delete params.assigned
  const res = await getStudentList(params)
  if (res && res.error === 0 && res.body && Array.isArray(res.body.list)) {
    studentList.value = res.body.list
  } else {
    studentList.value = []
  }
  loading.value = false
}
const resetFilter = () => {
  Object.assign(filterForm, { name: '', studentId: '', major: '', grade: '', assigned: '' })
  handleSearch()
}
const handleSelectionChange = (rows) => {
  selectedStudentIds.value = rows.map(r => r.id)
}
onMounted(async () => {
  // 获取辅导员档案主键ID
  const res = await getCounselorProfile()
  if (res && res.error === 0 && res.body && res.body.id) {
    counselorProfileId.value = res.body.id
  }
  handleSearch()
})
const assignToMe = async () => {
  if (selectedStudentIds.value.length === 0) return
  if (!counselorProfileId.value) {
    ElMessage.error('未获取到辅导员档案ID')
    return
  }
  const res = await assignStudentsToCounselor({
    counselorId: counselorProfileId.value,
    studentIds: selectedStudentIds.value
  })
  if (res && res.error === 0) {
    ElMessage.success('分配成功')
    handleSearch()
  } else {
    ElMessage.error(res?.message || '分配失败')
  }
}
const contactStudent = async (student) => {
         alert('contactStudent 被调用');
     console.log('联系学生参数：', student); 
  if (!student || !student.userId || !counselorProfileId.value) {
    ElMessage.error('参数缺失，无法发起会话')
    return
  }
  const res = await createStudentCounselorSession({ counselorId: counselorProfileId.value, studentUserId: student.userId })
  let sessionId = null
  if (res && res.error === 0) {
    if (res.body && typeof res.body === 'object' && res.body.sessionId) {
      sessionId = res.body.sessionId
    } else if (typeof res.body === 'number') {
      sessionId = res.body
    }
  }
  if (sessionId) {
    router.push({ path: '/counselor/chat', query: { sessionId } })
  } else {
    ElMessage.error(res?.message || '发起会话失败')
  }
}
</script>

<style scoped>
.students-page {
  padding: 20px;
}
.page-title {
  margin-bottom: 20px;
  font-weight: bold;
  color: #303133;
}
.filter-form {
  margin-bottom: 20px;
}
.actions {
  margin-top: 20px;
}
.unassigned {
  color: #f56c6c;
}
</style> 