<template>
  <div class="chat-container">
    <div class="sidebar">
      <div class="session-list">
        <h3>消息列表</h3>
        <div v-if="sessions.length === 0" class="no-session">
          暂无聊天消息
        </div>
        <div 
          v-for="session in sessions" 
          :key="session.id" 
          :class="['session-item', { active: currentSession && currentSession.id === session.id }]"
          @click="selectSession(session)"
        >
          <div class="session-avatar">
            <el-avatar :size="40" :src="session.avatar || defaultAvatar"></el-avatar>
          </div>
          <div class="session-info">
            <div class="session-name">{{ session.name }}</div>
            <div class="session-last-message">{{ session.lastMessage || '暂无消息' }}</div>
          </div>
          <div class="session-time">{{ session.lastTime || '' }}</div>
          <div v-if="session.unread" class="unread-count">{{ session.unread }}</div>
        </div>
      </div>
    </div>
    
    <div class="chat-main">
      <div v-if="!currentSession" class="empty-chat">
        <div class="empty-chat-tip">
          <i class="el-icon-chat-dot-round"></i>
          <p>请选择一个会话</p>
        </div>
      </div>
      
      <template v-else>
        <div class="chat-header">
          <h3>{{ currentSession.name }}</h3>
          <div class="chat-header-info">
            {{ currentSessionInfo }}
          </div>
        </div>
        
        <div class="message-list" ref="messageList">
          <div v-if="messages.length === 0" class="no-message">
            暂无消息，开始聊天吧
          </div>
          <div 
            v-for="message in messages" 
            :key="message.id"
            :class="['message-item', { 'message-self': message.isSelf }]"
          >
            <div class="message-avatar">
              <el-avatar :size="36" :src="message.avatar || defaultAvatar"></el-avatar>
            </div>
            <div class="message-content">
              <div class="message-time">{{ message.time }}</div>
              <div class="message-bubble">
                <template v-if="message.type === 'text'">
                  {{ message.content }}
                </template>
                <template v-else-if="message.type === 'image'">
                  <img :src="message.content" class="message-image" @click="previewImage(message.content)" />
                </template>
                <template v-else-if="message.type === 'file'">
                  <div class="message-file" @click="downloadFile(message.file)">
                    <i class="el-icon-document"></i>
                    <span>{{ message.file.name }}</span>
                    <span class="file-size">{{ formatFileSize(message.file.size) }}</span>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
        
        <div class="message-input">
          <div class="input-tools">
            <el-tooltip content="发送图片" placement="top">
              <i class="el-icon-picture-outline" @click="triggerImageUpload"></i>
            </el-tooltip>
            <el-tooltip content="发送文件" placement="top">
              <i class="el-icon-upload2" @click="triggerFileUpload"></i>
            </el-tooltip>
            <input type="file" ref="imageInput" accept="image/*" style="display:none" @change="handleImageUpload" />
            <input type="file" ref="fileInput" style="display:none" @change="handleFileUpload" />
          </div>
          <div class="input-area">
            <el-input
              v-model="messageText"
              type="textarea"
              :rows="3"
              placeholder="请输入消息..."
              resize="none"
              @keyup.enter.native="sendMessage"
            ></el-input>
          </div>
          <div class="input-actions">
            <el-button type="primary" @click="sendMessage" :disabled="!messageText.trim()">发送</el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { getChatSessions, getChatMessages, sendChatMessage, uploadChatFile } from '@/api/chat'
import { callApi, callUploadApi } from '@/utils/apiUtils'

const route = useRoute()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const messageText = ref('')
const messageList = ref(null)
const imageInput = ref(null)
const fileInput = ref(null)
const loading = ref(false)
const loadingMessages = ref(false)
const sendingMessage = ref(false)
const currentSessionId = ref(null)

// 从URL参数中获取会话ID
watch(() => route.query.sessionId, (sessionId) => {
  if (sessionId) {
    currentSessionId.value = sessionId
    // 如果已经加载了会话列表，选择对应会话
    if (sessions.value.length > 0) {
      const session = sessions.value.find(s => s.id === sessionId)
      if (session) {
        selectSession(session)
      }
    }
  }
}, { immediate: true })

// 当前会话的附加信息
const currentSessionInfo = computed(() => {
  if (!currentSession.value) return ''
  
  if (currentSession.value.type === 'SE') {
    return `学生-企业会话${currentSession.value.relatedJobTitle ? ` - 岗位: ${currentSession.value.relatedJobTitle}` : ''}`
  } else if (currentSession.value.type === 'SC') {
    return `学生-辅导员会话`
  } else if (currentSession.value.type === 'SS') {
    return `学生群组`
  } else {
    return ''
  }
})

// 选择会话
const selectSession = async (session) => {
  if (loadingMessages.value) return
  
  // 如果是同一个会话，则不重新加载
  if (currentSession.value && currentSession.value.id === session.id) return
  
  currentSession.value = session
  messages.value = []
  
  // 加载会话消息
  await loadSessionMessages(session.id)
  
  // 清除未读计数
  const sessionIndex = sessions.value.findIndex(s => s.id === session.id)
  if (sessionIndex !== -1) {
    sessions.value[sessionIndex].unread = 0
  }
}

// 加载会话列表
const loadSessions = async () => {
  loading.value = true
  
  try {
    const result = await callApi(getChatSessions())
    
    // 处理会话数据
    sessions.value = (result.list || []).map(session => ({
      id: session.id,
      name: session.title,
      avatar: session.participantInfo?.avatar || '',
      lastMessage: session.lastMessage?.content || '',
      lastTime: formatSessionTime(session.lastActiveAt),
      unread: session.unreadCount || 0,
      type: session.type,
      relatedJobId: session.relatedJobId,
      relatedJobTitle: session.lastMessage?.content?.includes(":") ? session.lastMessage.content.split(":")[0] : ''
    }))
    
    // 如果路由参数有会话ID，选择对应会话
    if (currentSessionId.value) {
      const session = sessions.value.find(s => s.id === currentSessionId.value)
      if (session) {
        selectSession(session)
      }
    }
  } catch (error) {
    // 错误已由callApi处理
    console.error('加载会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载会话消息
const loadSessionMessages = async (sessionId) => {
  loadingMessages.value = true
  
  try {
    const result = await callApi(getChatMessages({
      sessionId,
      page: 1,
      pageSize: 50
    }))
    
    // 处理消息数据
    messages.value = (result.list || []).map(msg => ({
      id: msg.id,
      content: msg.content,
      type: msg.contentType,
      time: formatMessageTime(msg.sentAt),
      isSelf: msg.isSelf,
      avatar: msg.senderAvatar || defaultAvatar,
      file: msg.contentType === 'file' ? {
        name: getFileNameFromPath(msg.filePath),
        path: msg.filePath,
        size: msg.fileSize || 0
      } : null
    }))
    
    // 滚动到最新消息
    await nextTick()
    scrollToBottom()
  } catch (error) {
    // 错误已由callApi处理
    console.error('加载消息失败:', error)
  } finally {
    loadingMessages.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!currentSession.value || !messageText.value.trim() || sendingMessage.value) return
  
  const text = messageText.value.trim()
  messageText.value = ''
  sendingMessage.value = true
  
  // 先在界面上显示发送中的消息
  const tempMessage = {
    id: 'temp-' + Date.now(),
    content: text,
    type: 'text',
    time: formatMessageTime(new Date()),
    isSelf: true,
    avatar: defaultAvatar,
    sending: true
  }
  
  messages.value.push(tempMessage)
  await nextTick()
  scrollToBottom()
  
  try {
    const result = await callApi(sendChatMessage({
      sessionId: currentSession.value.id,
      content: text,
      contentType: 'text'
    }))
    
    // 替换临时消息
    const index = messages.value.findIndex(m => m.id === tempMessage.id)
    if (index !== -1) {
      messages.value[index] = {
        id: result.messageId,
        content: text,
        type: 'text',
        time: formatMessageTime(result.sentAt || new Date()),
        isSelf: true,
        avatar: defaultAvatar
      }
    }
    
    // 更新会话列表中的最后一条消息
    updateSessionLastMessage(currentSession.value.id, text)
  } catch (error) {
    // 错误已由callApi处理，标记消息发送失败
    const index = messages.value.findIndex(m => m.id === tempMessage.id)
    if (index !== -1) {
      messages.value[index].failed = true
      messages.value[index].sending = false
    }
    console.error('发送消息失败:', error)
  } finally {
    sendingMessage.value = false
  }
}

// 触发图片上传
const triggerImageUpload = () => {
  imageInput.value.click()
}

// 触发文件上传
const triggerFileUpload = () => {
  fileInput.value.click()
}

// 处理图片上传
const handleImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return
  }
  
  // 检查文件大小 (限制为5MB)
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  
  await uploadAndSendFile(file, 'image')
  
  // 清空input，允许重复上传同一个文件
  event.target.value = ''
}

// 处理文件上传
const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 检查文件大小 (限制为10MB)
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return
  }
  
  await uploadAndSendFile(file, 'file')
  
  // 清空input，允许重复上传同一个文件
  event.target.value = ''
}

// 上传并发送文件消息
const uploadAndSendFile = async (file, contentType) => {
  if (!currentSession.value) return
  
  // 先在界面上显示上传中的消息
  const tempMessage = {
    id: 'temp-' + Date.now(),
    content: contentType === 'image' ? URL.createObjectURL(file) : null,
    file: contentType === 'file' ? { name: file.name, size: file.size } : null,
    type: contentType,
    time: formatMessageTime(new Date()),
    isSelf: true,
    avatar: defaultAvatar,
    uploading: true
  }
  
  messages.value.push(tempMessage)
  await nextTick()
  scrollToBottom()
  
  try {
    // 上传文件
    const uploadResult = await callUploadApi(uploadChatFile(file, currentSession.value.id, contentType))
    
    // 发送文件消息
    const sendResult = await callApi(sendChatMessage({
      sessionId: currentSession.value.id,
      content: uploadResult.filePath,
      contentType: contentType,
      fileSize: file.size
    }))
    
    // 替换临时消息
    const index = messages.value.findIndex(m => m.id === tempMessage.id)
    if (index !== -1) {
      messages.value[index] = {
        id: sendResult.messageId,
        content: contentType === 'image' ? uploadResult.filePath : null,
        type: contentType,
        time: formatMessageTime(sendResult.sentAt || new Date()),
        isSelf: true,
        avatar: defaultAvatar,
        file: contentType === 'file' ? {
          name: file.name,
          path: uploadResult.filePath,
          size: file.size
        } : null
      }
    }
    
    // 更新会话列表中的最后一条消息
    const messageText = contentType === 'image' ? '[图片]' : `[文件: ${file.name}]`
    updateSessionLastMessage(currentSession.value.id, messageText)
    
    ElMessage.success(contentType === 'image' ? '图片发送成功' : '文件发送成功')
  } catch (error) {
    // 错误已由callApi处理，标记消息上传失败
    const index = messages.value.findIndex(m => m.id === tempMessage.id)
    if (index !== -1) {
      messages.value[index].failed = true
      messages.value[index].uploading = false
    }
    
    if (contentType === 'image' && tempMessage.content) {
      URL.revokeObjectURL(tempMessage.content)
    }
    
    console.error('发送文件消息失败:', error)
  }
}

// 预览图片
const previewImage = (imageUrl) => {
  window.open(imageUrl, '_blank')
}

// 下载文件
const downloadFile = (file) => {
  if (!file || !file.path) {
    ElMessage.error('文件信息不完整，无法下载')
    return
  }
  
  // 创建下载链接
  const link = document.createElement('a')
  link.href = file.path
  link.download = file.name || '未命名文件'
  link.target = '_blank'
  link.click()
}

// 更新会话列表中的最后一条消息
const updateSessionLastMessage = (sessionId, message) => {
  const index = sessions.value.findIndex(s => s.id === sessionId)
  if (index !== -1) {
    sessions.value[index].lastMessage = message
    sessions.value[index].lastTime = formatSessionTime(new Date())
    
    // 将会话移到顶部
    const session = sessions.value[index]
    sessions.value.splice(index, 1)
    sessions.value.unshift(session)
  }
}

// 格式化会话时间
const formatSessionTime = (time) => {
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 今天内，显示时间
  if (date.toDateString() === now.toDateString()) {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }
  
  // 7天内，显示星期
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = ['日', '一', '二', '三', '四', '五', '六']
    return `星期${days[date.getDay()]}`
  }
  
  // 当年内，显示月日
  if (date.getFullYear() === now.getFullYear()) {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
  
  // 其他情况，显示年月日
  return `${date.getFullYear()}/${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')}`
}

// 格式化消息时间
const formatMessageTime = (time) => {
  if (!time) return ''
  
  const date = new Date(time)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 从文件路径中获取文件名
const getFileNameFromPath = (path) => {
  if (!path) return '未命名文件'
  
  const parts = path.split('/')
  return parts[parts.length - 1]
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  let formattedSize = size
  
  while (formattedSize >= 1024 && index < units.length - 1) {
    formattedSize /= 1024
    index++
  }
  
  return `${formattedSize.toFixed(2)} ${units[index]}`
}

// 滚动到底部
const scrollToBottom = () => {
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}

// 组件挂载时加载会话列表
onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 120px);
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.sidebar {
  width: 280px;
  border-right: 1px solid #e6e6e6;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-list h3 {
  padding: 15px;
  margin: 0;
  border-bottom: 1px solid #e6e6e6;
}

.session-item {
  display: flex;
  padding: 12px 15px;
  cursor: pointer;
  position: relative;
  border-bottom: 1px solid #f0f0f0;
}

.session-item:hover {
  background-color: #ecf5ff;
}

.session-item.active {
  background-color: #ecf5ff;
}

.session-avatar {
  margin-right: 12px;
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.session-last-message {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.unread-count {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #f56c6c;
  color: #fff;
  border-radius: 10px;
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  font-size: 12px;
  padding: 0 6px;
}

.no-session {
  padding: 20px;
  text-align: center;
  color: #909399;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.chat-header {
  padding: 15px;
  border-bottom: 1px solid #e6e6e6;
}

.chat-header h3 {
  margin: 0;
  margin-bottom: 5px;
}

.chat-header-info {
  font-size: 12px;
  color: #909399;
}

.message-list {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

.no-message {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
}

.message-self {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 10px;
}

.message-content {
  max-width: 60%;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
  text-align: center;
}

.message-bubble {
  padding: 10px 15px;
  border-radius: 4px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  word-break: break-word;
}

.message-self .message-bubble {
  background-color: #ecf5ff;
}

.message-image {
  max-width: 100%;
  max-height: 200px;
  cursor: pointer;
}

.message-file {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.message-file i {
  margin-right: 5px;
}

.file-size {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.message-input {
  padding: 15px;
  border-top: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.input-tools {
  display: flex;
  padding-bottom: 10px;
}

.input-tools i {
  font-size: 20px;
  margin-right: 15px;
  color: #606266;
  cursor: pointer;
}

.input-tools i:hover {
  color: #409eff;
}

.input-area {
  margin-bottom: 10px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
}

.empty-chat {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.empty-chat-tip {
  text-align: center;
  color: #909399;
}

.empty-chat-tip i {
  font-size: 48px;
  margin-bottom: 10px;
}
</style> 