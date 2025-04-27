<template>
  <div class="company-profile">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>企业信息档案</h3>
          <el-button type="primary" @click="saveProfile">保存信息</el-button>
        </div>
      </template>
      
      <el-form :model="form" label-width="120px" :rules="rules" ref="profileFormRef">
        <!-- 基本信息 -->
        <h4>基本信息</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入企业全称" readonly disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="unifiedSocialCreditCode">
              <el-input v-model="form.unifiedSocialCreditCode" placeholder="请输入统一社会信用代码" readonly disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="行业" prop="industry">
              <el-select v-model="form.industry" placeholder="请选择行业" style="width: 100%">
                <el-option label="IT/互联网/通信" value="IT/互联网/通信"></el-option>
                <el-option label="金融/银行/保险" value="金融/银行/保险"></el-option>
                <el-option label="教育/培训" value="教育/培训"></el-option>
                <el-option label="房地产/建筑" value="房地产/建筑"></el-option>
                <el-option label="医疗/健康" value="医疗/健康"></el-option>
                <el-option label="消费品" value="消费品"></el-option>
                <el-option label="汽车/机械/制造" value="汽车/机械/制造"></el-option>
                <el-option label="能源/环保" value="能源/环保"></el-option>
                <el-option label="政府/非盈利机构" value="政府/非盈利机构"></el-option>
                <el-option label="其他" value="其他"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业规模" prop="size">
              <el-select v-model="form.size" placeholder="请选择企业规模" style="width: 100%">
                <el-option label="少于50人" value="少于50人"></el-option>
                <el-option label="50-200人" value="50-200人"></el-option>
                <el-option label="200-500人" value="200-500人"></el-option>
                <el-option label="500-1000人" value="500-1000人"></el-option>
                <el-option label="1000-5000人" value="1000-5000人"></el-option>
                <el-option label="5000-10000人" value="5000-10000人"></el-option>
                <el-option label="10000人以上" value="10000人以上"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="公司地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入公司详细地址"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider></el-divider>
        
        <!-- 企业介绍 -->
        <h4>企业介绍</h4>
        <el-form-item prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="请详细介绍公司的业务范围、发展历程、企业文化等信息"></el-input>
        </el-form-item>
        
        <el-divider></el-divider>
        
        <!-- HR联系方式 -->
        <h4>HR联系方式</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="HR姓名" prop="hrContact.name">
              <el-input v-model="form.hrContact.name" placeholder="请输入HR姓名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="HR电话" prop="hrContact.phone">
              <el-input v-model="form.hrContact.phone" placeholder="请输入HR联系电话"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="HR邮箱" prop="hrContact.email">
              <el-input v-model="form.hrContact.email" placeholder="请输入HR邮箱"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作时间" prop="hrContact.workTime">
              <el-input v-model="form.hrContact.workTime" placeholder="例如：周一至周五 9:00-18:00"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider></el-divider>
        
        <!-- 企业Logo -->
        <h4>企业Logo</h4>
        <el-form-item prop="logo">
          <el-upload
            class="logo-uploader"
            action="#"
            :http-request="uploadLogo"
            :show-file-list="false"
            :before-upload="beforeLogoUpload"
          >
            <img 
              v-if="form.logoUrl" 
              :src="form.logoUrl" 
              class="logo-image" 
              alt="企业Logo" 
              @error="handleImageError"
            />
            <div v-else class="logo-uploader-icon">
              <el-icon><Plus /></el-icon>
              <div>上传Logo</div>
            </div>
          </el-upload>
          <div class="upload-tip">请上传公司Logo图片，建议尺寸200x200像素，格式为JPG、PNG</div>
        </el-form-item>
        
        <el-divider></el-divider>
        
        <!-- 营业执照 -->
        <h4>营业执照</h4>
        <el-form-item prop="license">
          <el-upload
            class="license-uploader"
            action="#"
            :http-request="uploadLicense"
            :file-list="licenseFileList"
            :limit="1"
            :on-exceed="handleExceed"
            :before-upload="beforeLicenseUpload"
          >
            <el-button type="primary">上传营业执照</el-button>
            <template #tip>
              <div class="el-upload__tip">请上传营业执照扫描件或照片，只能上传JPG/PNG/PDF文件，且不超过10MB</div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-divider></el-divider>
        
        <!-- 认证状态 -->
        <h4>认证状态</h4>
        <el-form-item>
          <el-tag :type="getStatusType" size="large">{{ getStatusText }}</el-tag>
          <div v-if="form.certificationStatus === 'pending' || form.certificationStatus === '待认证'" class="cert-pending-note">
            您的企业认证资料正在审核中，请耐心等待。审核通常需要1-3个工作日完成。
          </div>
          <div v-if="form.certificationStatus === 'rejected' || form.certificationStatus === 'failed' || form.certificationStatus === '认证失败'" class="cert-rejected-note">
            <p>您的企业认证被驳回，原因：{{ form.rejectionReason || '信息有误，请修改后重新提交' }}</p>
            <p>修改相关信息后可重新提交认证申请</p>
          </div>
          <div v-if="form.certificationStatus === 'approved' || form.certificationStatus === 'certified' || form.certificationStatus === '已认证'" class="cert-approved-note">
            <p>认证有效期至：{{ formatDate(form.certificationExpiryDate) }}</p>
            <p v-if="isExpiryDateNear" class="expiry-warning">认证即将到期，请及时更新企业资料</p>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCompanyProfile, updateCompanyProfile, uploadCompanyLogo, uploadCompanyLicense } from '@/api/company'
import { callApi, callUploadApi } from '@/utils/apiUtils'

// 表单引用
const profileFormRef = ref(null)

// 营业执照文件列表
const licenseFileList = ref([])

// 表单数据
const form = reactive({
  name: '',
  unifiedSocialCreditCode: '',
  industry: '',
  size: '',
  address: '',
  description: '',
  hrContact: {
    name: '',
    phone: '',
    email: '',
    workTime: ''
  },
  logoUrl: '',
  licenseUrl: '',
  certificationStatus: 'uncertified', // uncertified, pending, approved, rejected
  certificationExpiryDate: null,
  rejectionReason: ''
})

// 表单验证规则
const rules = {
  name: [], // 不需要验证，因为这是只读字段
  unifiedSocialCreditCode: [], // 不需要验证，因为这是只读字段
  industry: [{ required: true, message: '请选择行业', trigger: 'change' }],
  size: [{ required: true, message: '请选择企业规模', trigger: 'change' }],
  address: [{ required: true, message: '请输入公司地址', trigger: 'blur' }],
  description: [{ required: true, message: '请输入企业介绍', trigger: 'blur' }],
  'hrContact.name': [{ required: true, message: '请输入HR姓名', trigger: 'blur' }],
  'hrContact.phone': [
    { required: true, message: '请输入HR联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  'hrContact.email': [
    { required: true, message: '请输入HR邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 认证状态文本
const getStatusText = computed(() => {
  switch (form.certificationStatus) {
    case 'uncertified':
    case '未认证':
      return '未认证'
    case 'pending':
    case '待认证':
      return '认证审核中'
    case 'approved':
    case 'certified':
    case '已认证':
      return '已认证'
    case 'rejected':
    case 'failed':
    case '认证失败':
      return '认证被驳回'
    case 'expired':
    case '已过期':
      return '认证已过期'
    default:
      console.log('未知认证状态:', form.certificationStatus)
      return '未知状态'
  }
})

// 认证状态类型
const getStatusType = computed(() => {
  switch (form.certificationStatus) {
    case 'uncertified':
    case '未认证':
      return 'info'
    case 'pending':
    case '待认证':
      return 'warning'
    case 'approved':
    case 'certified':
    case '已认证':
      return 'success'
    case 'rejected':
    case 'failed':
    case '认证失败':
      return 'danger'
    case 'expired':
    case '已过期':
      return 'danger'
    default:
      return 'info'
  }
})

// 认证有效期是否临近（30天内）
const isExpiryDateNear = computed(() => {
  if (!form.certificationExpiryDate) return false
  
  const expiryDate = new Date(form.certificationExpiryDate)
  const now = new Date()
  const diffTime = expiryDate - now
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  return diffDays > 0 && diffDays <= 30
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未设置'
  
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// Logo图片加载错误处理
const handleImageError = (e) => {
  console.error('Logo图片加载失败:', e);
  console.log('当前Logo URL:', form.logoUrl);
  ElMessage.warning('Logo图片加载失败，请重新上传');
}

// 页面加载时获取数据
onMounted(async () => {
  const loading = ElLoading.service({
    lock: true,
    text: '加载数据中...',
    background: 'rgba(255, 255, 255, 0.7)'
  })
  
  try {
    const profileData = await callApi(getCompanyProfile())
    console.log('获取到的企业信息:', profileData) // 调试信息
    
    // 填充表单数据
    form.name = profileData.name || ''
    form.unifiedSocialCreditCode = profileData.unifiedSocialCreditCode || ''
    form.industry = profileData.industry || ''
    form.size = profileData.size || ''
    form.address = profileData.address || ''
    form.description = profileData.description || ''
    
    // 解析HR联系方式
    try {
      form.hrContact = profileData.hrContact && typeof profileData.hrContact === 'object'
        ? profileData.hrContact
        : { name: '', phone: '', email: '', workTime: '' }
    } catch (e) {
      console.error('解析HR联系方式失败:', e)
      form.hrContact = { name: '', phone: '', email: '', workTime: '' }
    }
    
    // 设置Logo路径
    if (profileData.logoPath) {
      console.log('原始Logo路径:', profileData.logoPath)
      
      // 获取后端API的基础URL，而不是前端URL
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
      console.log('API基础URL:', apiBaseUrl)
      
      // 从URL中提取域名部分（去掉/api等路径）
      const urlParts = apiBaseUrl.match(/^(https?:\/\/[^\/]+)/)
      const backendBaseUrl = urlParts ? urlParts[1] : 'http://localhost:8080'
      console.log('后端服务器基础URL:', backendBaseUrl)
      
      // 构建完整路径 - 指向后端服务器
      const logoPath = profileData.logoPath.startsWith('/') 
        ? backendBaseUrl + profileData.logoPath 
        : backendBaseUrl + '/' + profileData.logoPath
      
      console.log('构建的完整Logo URL:', logoPath)
      form.logoUrl = logoPath
    } else {
      form.logoUrl = ''
    }
    
    // 设置营业执照路径
    if (profileData.licenseFilePath) {
      console.log('原始营业执照路径:', profileData.licenseFilePath)
      
      // 获取后端API的基础URL，而不是前端URL
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
      const urlParts = apiBaseUrl.match(/^(https?:\/\/[^\/]+)/)
      const backendBaseUrl = urlParts ? urlParts[1] : 'http://localhost:8080'
      
      const licensePath = profileData.licenseFilePath.startsWith('/') 
        ? backendBaseUrl + profileData.licenseFilePath 
        : backendBaseUrl + '/' + profileData.licenseFilePath
      
      form.licenseUrl = licensePath
    } else {
      form.licenseUrl = ''
    }
    
    // 设置认证状态
    form.certificationStatus = profileData.certificationStatus || 'uncertified'
    form.certificationExpiryDate = profileData.certificationExpiryDate || null
    form.rejectionReason = profileData.rejectionReason || ''
    
    // 如果有营业执照文件，添加到文件列表
    if (profileData.licenseFilePath) {
      licenseFileList.value = [{
        name: profileData.licenseFileName || '营业执照',
        url: form.licenseUrl
      }]
    }
  } catch (error) {
    // callApi 已处理错误消息，这里可以记录日志
    console.error('获取企业信息失败:', error)
  } finally {
    loading.close()
  }
})

// 保存企业信息
const saveProfile = async () => {
  try {
    const valid = await profileFormRef.value.validate()
    if (!valid) return
    
    const loading = ElLoading.service({
      lock: true,
      text: '保存数据中...',
      background: 'rgba(255, 255, 255, 0.7)'
    })
    
    // 构建要保存的数据
    const profileData = {
      // 不包含企业名称和统一社会信用代码，因为它们是只读的
      industry: form.industry,
      size: form.size,
      address: form.address,
      description: form.description,
      hrContact: form.hrContact
    }
    
    await callApi(updateCompanyProfile(profileData), {
      showSuccess: true,
      successMsg: '企业信息保存成功'
    })
    
    loading.close()
  } catch (error) {
    // callApi 已处理错误消息
    console.error('保存企业信息失败:', error)
  }
}

// 上传Logo前的验证
const beforeLogoUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('Logo只能是JPG或PNG格式!')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('Logo大小不能超过2MB!')
    return false
  }
  
  return true
}

// 上传Logo
const uploadLogo = async (params) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '上传Logo中...',
      background: 'rgba(255, 255, 255, 0.7)'
    })
    
    console.log('正在上传Logo:', params.file.name, params.file.type)
    
    const response = await callUploadApi(uploadCompanyLogo(params.file), {
      successMsg: 'Logo上传成功'
    })
    
    console.log('Logo上传响应:', response) // 调试信息
    
    // 构建完整路径 - 指向后端服务器
    if (response && response.filePath) {
      console.log('上传后返回的Logo路径:', response.filePath)
      
      // 获取后端API的基础URL，而不是前端URL
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
      const urlParts = apiBaseUrl.match(/^(https?:\/\/[^\/]+)/)
      const backendBaseUrl = urlParts ? urlParts[1] : 'http://localhost:8080'
      console.log('后端服务器基础URL:', backendBaseUrl)
      
      const logoPath = response.filePath.startsWith('/') 
        ? backendBaseUrl + response.filePath 
        : backendBaseUrl + '/' + response.filePath
      
      console.log('构建的完整Logo URL:', logoPath)
      form.logoUrl = logoPath
      
      // 验证URL是否可访问
      const img = new Image()
      img.onload = () => console.log('Logo图片加载成功')
      img.onerror = (e) => console.error('Logo图片加载失败:', e)
      img.src = logoPath
    }
    
    loading.close()
  } catch (error) {
    // callUploadApi 已处理错误消息
    console.error('Logo上传失败:', error)
    loading.close()
  }
}

// 上传营业执照前的验证
const beforeLicenseUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isPDF = file.type === 'application/pdf'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isJPG && !isPNG && !isPDF) {
    ElMessage.error('营业执照只能是JPG、PNG或PDF格式!')
    return false
  }
  
  if (!isLt10M) {
    ElMessage.error('营业执照大小不能超过10MB!')
    return false
  }
  
  return true
}

// 上传营业执照
const uploadLicense = async (params) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '上传营业执照中...',
      background: 'rgba(255, 255, 255, 0.7)'
    })
    
    const response = await callUploadApi(uploadCompanyLicense(params.file), {
      successMsg: '营业执照上传成功，认证状态已更新为待审核'
    })
    
    // 构建完整路径 - 指向后端服务器
    if (response && response.filePath) {
      // 获取后端API的基础URL，而不是前端URL
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
      const urlParts = apiBaseUrl.match(/^(https?:\/\/[^\/]+)/)
      const backendBaseUrl = urlParts ? urlParts[1] : 'http://localhost:8080'
      
      const licensePath = response.filePath.startsWith('/') 
        ? backendBaseUrl + response.filePath 
        : backendBaseUrl + '/' + response.filePath
      
      form.licenseUrl = licensePath
    }
    
    // 更新文件列表
    licenseFileList.value = [{
      name: response.fileName || params.file.name,
      url: form.licenseUrl
    }]
    
    // 更新认证状态
    form.certificationStatus = 'pending'
    
    loading.close()
  } catch (error) {
    // callUploadApi 已处理错误消息
    console.error('营业执照上传失败:', error)
    loading.close()
  }
}

// 处理文件数量超出限制
const handleExceed = () => {
  ElMessage.warning('只能上传一个营业执照文件，请先删除当前文件')
}
</script>

<style scoped>
.company-profile {
  padding: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h4 {
  font-size: 16px;
  font-weight: bold;
  margin: 15px 0;
}

.logo-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 200px;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-uploader:hover {
  border-color: #409eff;
}

.logo-uploader-icon {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
  width: 100%;
  height: 100%;
}

.logo-uploader-icon .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.logo-image {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.cert-pending-note {
  margin-top: 10px;
  padding: 8px 16px;
  background-color: #fdf6ec;
  border-radius: 4px;
  color: #e6a23c;
}

.cert-rejected-note {
  margin-top: 10px;
  padding: 8px 16px;
  background-color: #fef0f0;
  border-radius: 4px;
  color: #f56c6c;
}

.cert-approved-note {
  margin-top: 10px;
  padding: 8px 16px;
  background-color: #f0f9eb;
  border-radius: 4px;
  color: #67c23a;
}

.expiry-warning {
  color: #e6a23c;
  font-weight: bold;
}
</style> 