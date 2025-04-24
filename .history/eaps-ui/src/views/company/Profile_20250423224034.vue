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
              <el-input v-model="form.name" placeholder="请输入企业全称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="unifiedSocialCreditCode">
              <el-input v-model="form.unifiedSocialCreditCode" placeholder="请输入统一社会信用代码"></el-input>
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
            <img v-if="form.logoUrl" :src="form.logoUrl" class="logo-image" />
            <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
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
          <div v-if="form.certificationStatus === 'pending'" class="cert-pending-note">
            您的企业认证资料正在审核中，请耐心等待。审核通常需要1-3个工作日完成。
          </div>
          <div v-if="form.certificationStatus === 'rejected'" class="cert-rejected-note">
            <p>您的企业认证被驳回，原因：{{ form.rejectionReason || '信息有误，请修改后重新提交' }}</p>
            <p>修改相关信息后可重新提交认证申请</p>
          </div>
          <div v-if="form.certificationStatus === 'approved'" class="cert-approved-note">
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
  name: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  unifiedSocialCreditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    // 统一社会信用代码正则验证（简化版）
    { pattern: /^[0-9A-HJ-NPQRTUWXY]{18}$/, message: '请输入正确的统一社会信用代码', trigger: 'blur' }
  ],
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
      return '未认证'
    case 'pending':
      return '认证审核中'
    case 'approved':
      return '已认证'
    case 'rejected':
      return '认证被驳回'
    default:
      return '未知状态'
  }
})

// 认证状态类型
const getStatusType = computed(() => {
  switch (form.certificationStatus) {
    case 'uncertified':
      return 'info'
    case 'pending':
      return 'warning'
    case 'approved':
      return 'success'
    case 'rejected':
      return 'danger'
    default:
      return 'info'
  }
})

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

// 检查认证是否即将到期（30天内）
const isExpiryDateNear = computed(() => {
  if (!form.certificationExpiryDate) return false
  const expiryDate = new Date(form.certificationExpiryDate)
  const now = new Date()
  const diffDays = Math.ceil((expiryDate - now) / (1000 * 60 * 60 * 24))
  return diffDays > 0 && diffDays <= 30
})

// 在组件挂载时获取企业档案信息
onMounted(() => {
  // 模拟从服务器获取数据
  setTimeout(() => {
    form.name = '示例科技有限公司'
    form.unifiedSocialCreditCode = '91110000123456789X'
    form.industry = 'IT/互联网/通信'
    form.size = '200-500人'
    form.address = '北京市海淀区中关村科技园区'
    form.description = '示例科技是一家致力于人工智能技术研发的高新技术企业，成立于2015年，主要产品包括...'
    form.hrContact = {
      name: '李小姐',
      phone: '13900139000',
      email: 'hr@example.com',
      workTime: '周一至周五 9:00-18:00'
    }
    form.logoUrl = 'https://example.com/logo.png' // 示例URL，实际项目中应替换
    form.certificationStatus = 'approved'
    form.certificationExpiryDate = '2024-12-31'
    
    // 如果有营业执照，添加到文件列表
    if (form.licenseUrl) {
      licenseFileList.value = [{
        name: '营业执照.pdf',
        url: form.licenseUrl
      }]
    }
  }, 1000)

  /**
   * TODO: 实际实现时调用API获取企业档案信息
   * const fetchCompanyProfile = async () => {
   *   const res = await api.company.getProfile()
   *   if (res.success) {
   *     Object.assign(form, res.data)
   *     
   *     // 如果有营业执照，添加到文件列表
   *     if (form.licenseUrl) {
   *       licenseFileList.value = [{
   *         name: '营业执照.pdf',
   *         url: form.licenseUrl
   *       }]
   *     }
   *   }
   * }
   * fetchCompanyProfile()
   */
})

// Logo上传前验证
const beforeLogoUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('企业Logo只能是JPG或PNG格式!')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('企业Logo大小不能超过2MB!')
    return false
  }
  
  return true
}

// 上传Logo
const uploadLogo = (options) => {
  const { file } = options
  
  // 创建临时URL用于预览
  form.logoUrl = URL.createObjectURL(file)
  
  /**
   * TODO: 实际实现时上传文件到服务器
   * const formData = new FormData()
   * formData.append('file', file)
   * 
   * const uploadLogoFile = async () => {
   *   try {
   *     const res = await api.upload.logo(formData)
   *     if (res.success) {
   *       form.logoUrl = res.data.url
   *       ElMessage.success('Logo上传成功')
   *     } else {
   *       ElMessage.error(res.message || 'Logo上传失败')
   *     }
   *   } catch (error) {
   *     console.error('上传Logo失败:', error)
   *     ElMessage.error('系统错误，请稍后重试')
   *   }
   * }
   * uploadLogoFile()
   */
  
  // 模拟上传成功
  ElMessage.success('Logo上传成功')
}

// 营业执照上传前验证
const beforeLicenseUpload = (file) => {
  const isValidType = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'application/pdf'
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isValidType) {
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
const uploadLicense = (options) => {
  const { file } = options
  
  // 更新文件列表
  licenseFileList.value = [{
    name: file.name,
    url: URL.createObjectURL(file)
  }]
  
  /**
   * TODO: 实际实现时上传文件到服务器
   * const formData = new FormData()
   * formData.append('file', file)
   * 
   * const uploadLicenseFile = async () => {
   *   try {
   *     const res = await api.upload.license(formData)
   *     if (res.success) {
   *       form.licenseUrl = res.data.url
   *       // 重置认证状态为待审核
   *       form.certificationStatus = 'pending'
   *       ElMessage.success('营业执照上传成功，企业资质将进入审核流程')
   *     } else {
   *       ElMessage.error(res.message || '营业执照上传失败')
   *     }
   *   } catch (error) {
   *     console.error('上传营业执照失败:', error)
   *     ElMessage.error('系统错误，请稍后重试')
   *   }
   * }
   * uploadLicenseFile()
   */
  
  // 模拟上传成功
  form.licenseUrl = URL.createObjectURL(file)
  // 重置认证状态为待审核
  form.certificationStatus = 'pending'
  ElMessage.success('营业执照上传成功，企业资质将进入审核流程')
}

// 处理超出文件数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传1个营业执照文件')
}

// 保存企业信息
const saveProfile = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.error('表单填写有误，请检查')
    return
  }
  
  // 信息变更可能触发重新认证
  const needReauth = form.certificationStatus === 'rejected' || form.certificationStatus === 'uncertified'
  
  // 模拟API调用保存信息
  const loading = ElLoading.service({
    lock: true,
    text: '保存中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  setTimeout(() => {
    loading.close()
    if (needReauth && form.licenseUrl) {
      // 如果需要重新认证并且已上传营业执照
      form.certificationStatus = 'pending'
      ElMessage.success('企业信息保存成功，资质将进入审核流程')
    } else {
      ElMessage.success('企业信息保存成功')
    }
  }, 1500)

  /**
   * TODO: 实际实现时调用API保存企业档案信息
   * try {
   *   const loading = ElLoading.service({
   *     lock: true,
   *     text: '保存中...',
   *     background: 'rgba(0, 0, 0, 0.7)'
   *   })
   *   
   *   const res = await api.company.saveProfile(form)
   *   loading.close()
   *   
   *   if (res.success) {
   *     if (needReauth && form.licenseUrl) {
   *       // 如果需要重新认证并且已上传营业执照
   *       form.certificationStatus = 'pending'
   *       ElMessage.success('企业信息保存成功，资质将进入审核流程')
   *     } else {
   *       ElMessage.success('企业信息保存成功')
   *     }
   *   } else {
   *     ElMessage.error(res.message || '保存失败')
   *   }
   * } catch (error) {
   *   loading.close()
   *   console.error('保存企业信息失败:', error)
   *   ElMessage.error('系统错误，请稍后重试')
   * }
   */
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
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.logo-image {
  width: 200px;
  height: 200px;
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