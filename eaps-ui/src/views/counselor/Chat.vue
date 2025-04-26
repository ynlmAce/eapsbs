<template>
  <div class="chat-page">
    <h2 class="page-title">消息中心</h2>
    
    <el-row :gutter="20" class="chat-container">
      <!-- 会话列表 -->
      <el-col :span="6">
        <el-card class="session-list-card">
          <template #header>
            <div class="card-header">
              <span>会话列表</span>
            </div>
          </template>
          
          <div class="search-box">
            <el-input v-model="searchQuery" placeholder="搜索会话" clearable />
          </div>
          
          <div class="session-list" v-loading="sessionsLoading">
            <div
              v-for="session in filteredSessions"
              :key="session.id"
              class="session-item"
              :class="{ 'active': currentSession && currentSession.id === session.id }"
              @click="selectSession(session)"
            >
              <el-badge :value="session.unreadCount || null" :hidden="!session.unreadCount">
                <el-avatar :size="40"></el-avatar>
              </el-badge>
              <div class="session-info">
                <div class="session-name">{{ session.title || session.name }}</div>
                <div class="session-preview">{{ session.lastMessage?.content || '' }}</div>
              </div>
              <div class="session-time">{{ formatTime(session.lastActiveAt || session.lastMessage?.sentAt) }}</div>
            </div>
            <el-empty v-if="filteredSessions.length === 0" description="暂无会话"></el-empty>
          </div>
        </el-card>
      </el-col>
      
      <!-- 聊天区域 -->
      <el-col :span="18">
        <el-card class="chat-area-card" v-if="currentSession">
          <template #header>
            <div class="card-header">
              <span>{{ currentSession.title || currentSession.name }}</span>
              <div v-if="currentSession.isReadOnly">
                <el-tag type="info" size="small">只读</el-tag>
              </div>
            </div>
          </template>
          
          <div class="message-list" ref="messageListRef" v-loading="messagesLoading">
            <div v-for="message in messages" :key="message.id" class="message-item" :class="{ 'self': message.senderId === userId }">
              <div class="message-avatar">
                <el-avatar :size="36" :src="message.senderAvatar"></el-avatar>
              </div>
              <div class="message-content">
                <div class="message-sender">{{ message.senderName }} <span class="message-time">{{ formatTime(message.sentAt) }}</span></div>
                <div class="message-bubble">
                  <span v-if="message.contentType === 'text'">{{ message.content }}</span>
                  <img v-else-if="message.contentType === 'image'" :src="message.filePath" class="message-image" />
                  <div v-else-if="message.contentType === 'file'" class="message-file">
                    <i class="el-icon-document"></i>
                    <a :href="message.filePath" target="_blank">{{ message.content }}</a>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="messages.length === 0" class="empty-messages">
              <el-empty description="暂无消息"></el-empty>
            </div>
          </div>
          
          <div class="message-input" v-if="!currentSession.isReadOnly">
            <el-input
              v-model="messageText"
              type="textarea"
              :rows="3"
              placeholder="输入消息..."
              resize="none"
              @keyup.enter.exact.prevent="sendMessage"
            ></el-input>
            <div class="input-actions">
              <el-button type="primary" :disabled="!messageText.trim()" @click="sendMessage">发送</el-button>
            </div>
          </div>
          
          <div class="readonly-notice" v-else>
            <el-alert title="此会话已归档，无法发送新消息" type="info" :closable="false" />
          </div>
        </el-card>
        
        <el-empty v-else description="请选择一个会话" class="no-session-placeholder"></el-empty>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getChatSessions, getChatMessages, sendChatMessage, markMessageRead } from '@/api/chat'

// 会话列表数据
const sessionsLoading = ref(false)
const messagesLoading = ref(false)
const sessionList = ref([])
const searchQuery = ref('')
const currentSession = ref(null)

// 消息相关数据
const messages = ref([])
const messageText = ref('')
const messageListRef = ref(null)
const userId = ref(parseInt(localStorage.getItem('userId') || '0'))

// 过滤后的会话列表
const filteredSessions = computed(() => {
  if (!searchQuery.value) return sessionList.value
  
  return sessionList.value.filter(session => 
    (session.title || session.name).toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  
  // 如果是今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  
  // 其他情况
  return date.toLocaleDateString()
}

// 获取会话列表
const fetchSessions = async () => {
  sessionsLoading.value = true
  try {
    const response = await getChatSessions()
    if (response && response.list) {
      sessionList.value = response.list
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
    ElMessage.error('获取会话列表失败')
  } finally {
    sessionsLoading.value = false
  }
}

// 获取特定会话的消息
const fetchMessages = async (sessionId) => {
  messages.value = []
  messagesLoading.value = true
  
  try {
    const response = await getChatMessages(sessionId)
    
    if (response && response.list) {
      messages.value = response.list
      
      // 更新会话未读消息数，标记为已读
      if (currentSession.value) {
        await markMessageRead(sessionId)
        const index = sessionList.value.findIndex(session => session.id === currentSession.value.id)
        if (index !== -1) {
          sessionList.value[index].unreadCount = 0
        }
      }
    }
  } catch (error) {
    console.error('获取消息失败:', error)
    ElMessage.error('获取消息失败')
  } finally {
    messagesLoading.value = false
    // 滚动到底部
    scrollToBottom()
  }
}

// 选择会话
const selectSession = (session) => {
  currentSession.value = session
  fetchMessages(session.id)
}

// 发送消息
const sendMessage = async () => {
  if (!messageText.value.trim() || !currentSession.value || currentSession.value.isReadOnly) return
  
  try {
    // 发送消息到服务器
    const result = await sendChatMessage(currentSession.value.id, messageText.value)
    
    if (result) {
      // 添加到消息列表
      const newMessage = {
        id: result.messageId,
        sessionId: currentSession.value.id,
        senderId: userId.value,
        senderName: localStorage.getItem('userName') || '辅导员',
        content: messageText.value,
        contentType: 'text',
        sentAt: result.sentAt || new Date().toISOString()
      }
      
      messages.value.push(newMessage)
      
      // 更新会话列表中的最后一条消息
      const index = sessionList.value.findIndex(session => session.id === currentSession.value.id)
      if (index !== -1) {
        sessionList.value[index].lastMessage = {
          content: messageText.value,
          sentAt: newMessage.sentAt
        }
        sessionList.value[index].lastActiveAt = newMessage.sentAt
        
        // 将当前会话移到顶部
        const currentSessionData = sessionList.value.splice(index, 1)[0]
        sessionList.value.unshift(currentSessionData)
      }
      
      // 清空输入框
      messageText.value = ''
      
      // 滚动到底部
      scrollToBottom()
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  scrollToBottom()
})

// 页面加载时获取数据
onMounted(() => {
  fetchSessions()
})
</script>

<style scoped>
.chat-page {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-weight: bold;
  color: #303133;
}

.chat-container {
  height: calc(100vh - 140px);
}

.session-list-card, 
.chat-area-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-box {
  margin-bottom: 15px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.session-item:hover {
  background-color: #f5f7fa;
}

.session-item.active {
  background-color: #ecf5ff;
}

.session-info {
  flex: 1;
  margin-left: 12px;
  overflow: hidden;
}

.session-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.session-preview {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-avatar {
  margin-right: 12px;
}

.message-item.self .message-avatar {
  margin-right: 0;
  margin-left: 12px;
}

.message-content {
  max-width: 70%;
}

.message-item.self .message-content {
  align-items: flex-end;
}

.message-sender {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.message-item.self .message-sender {
  text-align: right;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.message-bubble {
  background-color: #f5f7fa;
  padding: 10px 15px;
  border-radius: 10px;
  word-break: break-word;
}

.message-item.self .message-bubble {
  background-color: #ecf5ff;
  color: #409EFF;
}

.message-input {
  margin-top: 15px;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.empty-messages {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  height: 100px;
}

.no-session-placeholder {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.readonly-notice {
  margin-top: 15px;
}

.message-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 4px;
}

.message-file {
  display: flex;
  align-items: center;
}

.message-file i {
  margin-right: 5px;
}

.message-file a {
  color: inherit;
  text-decoration: underline;
}
</style> 