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
import { ElMessage } from 'element-plus'

// 模拟数据 - 实际项目中应从API获取
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const sessions = ref([
  {
    id: '1',
    name: '张三',
    avatar: '',
    lastMessage: '您好，请问有什么可以帮助您的？',
    lastTime: '上午 10:30',
    unread: 2,
    type: 'student', // 用户类型：student, company, counselor
    relatedJobId: '101' // 如果是与企业的会话，可能关联一个岗位
  },
  {
    id: '2',
    name: '李四',
    avatar: '',
    lastMessage: '谢谢，我会考虑的',
    lastTime: '昨天',
    unread: 0,
    type: 'student'
  }
])

const currentSession = ref(null)
const messages = ref([])
const messageText = ref('')
const messageList = ref(null)
const imageInput = ref(null)
const fileInput = ref(null)

// 当前会话的附加信息
const currentSessionInfo = computed(() => {
  if (!currentSession.value) return ''
  
  if (currentSession.value.type === 'student') {
    return `学生`
  } else if (currentSession.value.type === 'company') {
    return `企业 - 岗位: ${currentSession.value.jobTitle || '未指定'}`
  } else {
    return `辅导员`
  }
})

// 选择会话
const selectSession = (session) => {
  currentSession.value = session
  
  // 模拟加载消息
  loadSessionMessages(session.id)
  
  // 清除未读计数
  const sessionIndex = sessions.value.findIndex(s => s.id === session.id)
  if (sessionIndex !== -1) {
    sessions.value[sessionIndex].unread = 0
  }
}

// 加载会话消息
const loadSessionMessages = (sessionId) => {
  // 实际项目中应从API获取
  messages.value = [
    {
      id: '101',
      sessionId: sessionId,
      senderId: 'self',
      isSelf: true,
      content: '您好，我对贵公司的岗位很感兴趣',
      type: 'text',
      time: '10:25',
      avatar: ''
    },
    {
      id: '102',
      sessionId: sessionId,
      senderId: 'other',
      isSelf: false,
      content: '您好，请问有什么可以帮助您的？',
      type: 'text',
      time: '10:30',
      avatar: ''
    }
  ]
  
  // 滚动到底部
  scrollToBottom()
}

// 发送消息
const sendMessage = () => {
  if (!messageText.value.trim()) return
  
  if (!currentSession.value) {
    ElMessage.warning('请先选择一个聊天对象')
    return
  }
  
  // 创建新消息
  const newMessage = {
    id: Date.now().toString(),
    sessionId: currentSession.value.id,
    senderId: 'self',
    isSelf: true,
    content: messageText.value,
    type: 'text',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    avatar: ''
  }
  
  // 添加到消息列表
  messages.value.push(newMessage)
  
  // 更新会话最后消息
  updateSessionLastMessage(currentSession.value.id, messageText.value)
  
  // 清空输入框
  messageText.value = ''
  
  // 滚动到底部
  scrollToBottom()
  
  // 实际项目中应调用API发送消息
  /**
   * TODO: 实际实现时调用API发送消息
   * try {
   *   await api.chat.sendMessage({
   *     sessionId: currentSession.value.id,
   *     content: messageText.value,
   *     type: 'text'
   *   })
   * } catch (error) {
   *   console.error('发送消息失败:', error)
   *   ElMessage.error('发送失败，请重试')
   * }
   */
}

// 更新会话最后消息
const updateSessionLastMessage = (sessionId, message) => {
  const sessionIndex = sessions.value.findIndex(s => s.id === sessionId)
  if (sessionIndex !== -1) {
    sessions.value[sessionIndex].lastMessage = message
    sessions.value[sessionIndex].lastTime = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight
    }
  })
}

// 触发图片上传
const triggerImageUpload = () => {
  imageInput.value.click()
}

// 处理图片上传
const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 实际项目中应调用API上传图片
  // 这里使用本地URL模拟
  const imageUrl = URL.createObjectURL(file)
  
  // 创建新图片消息
  const newMessage = {
    id: Date.now().toString(),
    sessionId: currentSession.value.id,
    senderId: 'self',
    isSelf: true,
    content: imageUrl,
    type: 'image',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    avatar: ''
  }
  
  // 添加到消息列表
  messages.value.push(newMessage)
  
  // 更新会话最后消息
  updateSessionLastMessage(currentSession.value.id, '[图片]')
  
  // 滚动到底部
  scrollToBottom()
  
  // 清空文件输入
  event.target.value = ''
}

// 触发文件上传
const triggerFileUpload = () => {
  fileInput.value.click()
}

// 处理文件上传
const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 创建新文件消息
  const newMessage = {
    id: Date.now().toString(),
    sessionId: currentSession.value.id,
    senderId: 'self',
    isSelf: true,
    content: '文件',
    type: 'file',
    file: {
      name: file.name,
      size: file.size,
      url: URL.createObjectURL(file) // 实际项目中应使用上传后的URL
    },
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    avatar: ''
  }
  
  // 添加到消息列表
  messages.value.push(newMessage)
  
  // 更新会话最后消息
  updateSessionLastMessage(currentSession.value.id, `[文件] ${file.name}`)
  
  // 滚动到底部
  scrollToBottom()
  
  // 清空文件输入
  event.target.value = ''
}

// 预览图片
const previewImage = (url) => {
  // 实际项目中可使用Element Plus的Image Viewer组件预览图片
  window.open(url, '_blank')
}

// 下载文件
const downloadFile = (file) => {
  // 实际项目中应提供真实的下载地址
  const a = document.createElement('a')
  a.href = file.url
  a.download = file.name
  a.click()
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 监听消息列表变化，自动滚动到底部
watch(messages, () => {
  scrollToBottom()
})

onMounted(() => {
  // 实际项目中应从API获取会话列表
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