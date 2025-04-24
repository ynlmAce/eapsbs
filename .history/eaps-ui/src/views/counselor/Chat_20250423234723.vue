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
            <el-input
              v-model="searchQuery"
              placeholder="搜索会话"
              prefix-icon="Search"
              clearable
            />
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
                <el-avatar :size="40" :src="session.avatar"></el-avatar>
              </el-badge>
              <div class="session-info">
                <div class="session-name">{{ session.name }}</div>
                <div class="session-preview">{{ session.lastMessage }}</div>
              </div>
              <div class="session-time">{{ formatTime(session.lastTime) }}</div>
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
              <span>{{ currentSession.name }}</span>
              <div class="header-actions">
                <el-button 
                  v-if="currentSession.isReadOnly"
                  size="small"
                  type="info"
                  plain
                  icon="Lock"
                  disabled
                >只读</el-button>
              </div>
            </div>
          </template>
          
          <div class="message-list" ref="messageListRef">
            <div v-for="(message, index) in messages" :key="index" class="message-item" :class="{ 'self': message.self }">
              <div class="message-avatar">
                <el-avatar :size="36" :src="message.avatar"></el-avatar>
              </div>
              <div class="message-content">
                <div class="message-sender">{{ message.sender }} <span class="message-time">{{ formatTime(message.time) }}</span></div>
                <div class="message-bubble">{{ message.content }}</div>
              </div>
            </div>
            <div v-if="messages.length === 0" class="empty-messages">
              <el-empty description="暂无消息"></el-empty>
            </div>
            <div v-if="messagesLoading" class="loading-messages">
              <el-icon class="rotating"><Loading /></el-icon> 加载更多消息...
            </div>
          </div>
          
          <div class="message-input" v-if="!currentSession.isReadOnly">
            <el-input
              v-model="messageText"
              type="textarea"
              :rows="3"
              placeholder="输入消息..."
              resize="none"
              @keyup.enter.shift.prevent
              @keyup.enter.exact.prevent="sendMessage"
            ></el-input>
            <div class="input-actions">
              <el-tooltip content="发送图片">
                <el-button icon="Picture" circle></el-button>
              </el-tooltip>
              <el-tooltip content="发送文件">
                <el-button icon="Document" circle></el-button>
              </el-tooltip>
              <el-button type="primary" :disabled="!messageText.trim()" @click="sendMessage">发送</el-button>
            </div>
          </div>
          
          <div class="readonly-notice" v-else>
            <el-alert
              title="此会话已归档，无法发送新消息"
              type="info"
              center
              :closable="false"
            />
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
import { Search, Loading, Picture, Document, Lock } from '@element-plus/icons-vue'

// 会话列表数据
const sessionsLoading = ref(false)
const sessionList = ref([])
const searchQuery = ref('')
const currentSession = ref(null)

// 消息相关数据
const messagesLoading = ref(false)
const messages = ref([])
const messageText = ref('')
const messageListRef = ref(null)

// 过滤后的会话列表
const filteredSessions = computed(() => {
  if (!searchQuery.value) return sessionList.value
  
  return sessionList.value.filter(session => 
    session.name.toLowerCase().includes(searchQuery.value.toLowerCase())
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
  
  // 如果是昨天
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天 ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  
  // 如果是今年
  if (date.getFullYear() === now.getFullYear()) {
    return (date.getMonth() + 1) + '月' + date.getDate() + '日'
  }
  
  // 其他情况
  return date.toLocaleDateString()
}

// 获取会话列表
const fetchSessions = async () => {
  sessionsLoading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      sessionList.value = [
        {
          id: 1,
          name: '张同学 (计算机科学)',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          lastMessage: '好的，老师，我会按照您的建议修改简历',
          lastTime: '2023-07-25 14:30:00',
          unreadCount: 2,
          isReadOnly: false
        },
        {
          id: 2,
          name: '李同学 (软件工程)',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          lastMessage: '请问老师，申请实习需要准备哪些材料？',
          lastTime: '2023-07-24 16:45:20',
          unreadCount: 0,
          isReadOnly: false
        },
        {
          id: 3,
          name: '王同学 (人工智能)',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          lastMessage: '谢谢老师的推荐信！',
          lastTime: '2023-07-20 10:20:15',
          unreadCount: 0,
          isReadOnly: true
        }
      ]
      
      sessionsLoading.value = false
    }, 800)
  } catch (error) {
    console.error('获取会话列表失败:', error)
    ElMessage.error('获取会话列表失败')
    sessionsLoading.value = false
  }
}

// 获取特定会话的消息
const fetchMessages = async (sessionId) => {
  messagesLoading.value = true
  messages.value = []
  
  try {
    // 模拟API调用
    setTimeout(() => {
      const mockMessages = [
        {
          id: 1,
          sender: '张同学',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          content: '老师，您好！我想请教一下我的简历有什么需要改进的地方吗？',
          time: '2023-07-25 14:15:30',
          self: false
        },
        {
          id: 2,
          sender: '张老师',
          avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
          content: '你好！我看了你的简历，总体来说内容比较全面，但有几点建议：1. 实习经历部分可以更详细描述你的具体贡献；2. 项目经验部分最好突出你解决的技术难点；3. 可以适当精简自我评价部分，让简历更加简洁。',
          time: '2023-07-25 14:20:45',
          self: true
        },
        {
          id: 3,
          sender: '张同学',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          content: '好的，老师，我会按照您的建议修改简历',
          time: '2023-07-25 14:30:00',
          self: false
        }
      ]
      
      messages.value = mockMessages
      messagesLoading.value = false
      
      // 滚动到底部
      scrollToBottom()
      
      // 更新会话未读消息数
      if (currentSession.value) {
        const index = sessionList.value.findIndex(session => session.id === currentSession.value.id)
        if (index !== -1) {
          sessionList.value[index].unreadCount = 0
        }
      }
    }, 1000)
  } catch (error) {
    console.error('获取消息失败:', error)
    ElMessage.error('获取消息失败')
    messagesLoading.value = false
  }
}

// 选择会话
const selectSession = (session) => {
  currentSession.value = session
  fetchMessages(session.id)
}

// 发送消息
const sendMessage = () => {
  if (!messageText.value.trim() || !currentSession.value || currentSession.value.isReadOnly) return
  
  // 创建新消息
  const newMessage = {
    id: messages.value.length + 1,
    sender: '张老师',
    avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    content: messageText.value,
    time: new Date().toLocaleString(),
    self: true
  }
  
  // 添加到消息列表
  messages.value.push(newMessage)
  
  // 更新会话列表中的最后一条消息
  const index = sessionList.value.findIndex(session => session.id === currentSession.value.id)
  if (index !== -1) {
    sessionList.value[index].lastMessage = messageText.value
    sessionList.value[index].lastTime = new Date().toLocaleString()
    
    // 将当前会话移到顶部
    const currentSessionData = sessionList.value.splice(index, 1)[0]
    sessionList.value.unshift(currentSessionData)
  }
  
  // 清空输入框
  messageText.value = ''
  
  // 滚动到底部
  scrollToBottom()
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
  justify-content: space-between;
  margin-top: 10px;
}

.rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-messages,
.loading-messages {
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

.header-actions {
  display: flex;
  align-items: center;
}
</style> 