<template>
  <div class="new-post-page">
    <h2>发新帖</h2>
    <el-alert v-if="errorMsg" type="error" :closable="false" show-icon>{{ errorMsg }}</el-alert>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" class="new-post-form">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入帖子标题" />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="10" maxlength="2000" show-word-limit placeholder="请输入帖子内容" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submitPost">发布</el-button>
        <el-button @click="goBack">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { createForumPost } from '@/api/forum'

const router = useRouter()
const formRef = ref()
const form = ref({
  title: '',
  content: ''
})
const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度2-100字', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 5, max: 2000, message: '内容长度5-2000字', trigger: 'blur' }
  ]
}
const submitting = ref(false)
const errorMsg = ref('')

const submitPost = () => {
  errorMsg.value = ''
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await createForumPost(form.value)
      router.push('/forum')
    } catch (e) {
      errorMsg.value = e.message || '发帖失败'
    } finally {
      submitting.value = false
    }
  })
}
const goBack = () => {
  router.back()
}
</script>

<style scoped>
.new-post-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem 1rem;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.new-post-form {
  margin-top: 2rem;
}
</style> 