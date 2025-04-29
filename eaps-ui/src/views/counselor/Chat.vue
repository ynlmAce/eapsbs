<template>
  <div class="chat-container">
    <div class="chat-sidebar">
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索会话"
          prefix-icon="el-icon-search"
          clearable
        />
      </div>
      
      <div class="session-list">
        <div v-if="sessionsLoading" class="sessions-loading">
          <el-skeleton animated :rows="3" />
        </div>
        <div v-else-if="filteredSessions.length === 0" class="empty-sessions">
          <el-empty description="暂无会话" />
          <el-button class="refresh-button" @click="refreshSessions" type="primary" size="small">
            刷新会话列表
          </el-button>
        </div>
        <template v-else>
          <div
            v-for="session in filteredSessions"
            :key="session.id"
            class="session-item"
            :class="{ 'active': currentSession && currentSession.id === session.id }"
            @click="selectSession(session)"
          >
            <el-badge :value="session.unreadCount || null" :hidden="!session.unreadCount" class="session-badge">
              <el-avatar :size="40" :src="getSessionAvatar(session)"></el-avatar>
            </el-badge>
            <div class="session-info">
              <div class="session-header">
                <span class="session-name">{{ session.title || session.name }}</span>
                <span class="session-time">{{ formatTime(session.lastActiveAt || session.lastMessage?.sentAt) }}</span>
              </div>
              <div class="session-message">{{ session.lastMessage?.content || '' }}</div>
            </div>
          </div>
          <div class="session-actions">
            <el-button class="refresh-button" @click="refreshSessions" type="primary" size="small" text>
              刷新会话列表
            </el-button>
          </div>
        </template>
      </div>
    </div>
    
    <div class="chat-main">
      <template v-if="currentSession">
        <div class="chat-header">
          <div class="header-title">
            {{ currentSession.title || currentSession.name }}
            <el-tag v-if="currentSession.isReadOnly" size="small" type="info">只读</el-tag>
          </div>
          <div class="header-actions">
            <el-button size="small" @click="showSessionInfo = true">查看信息</el-button>
          </div>
        </div>
        
        <div class="chat-messages" ref="messageListRef">
          <!-- 错误状态显示 -->
          <div v-if="chatError" class="chat-error-container">
            <el-alert
              title="加载失败"
              :description="errorMessage"
              type="error"
              show-icon
              center
              :closable="false"
            />
            <el-button class="retry-button" type="primary" @click="retryLoad">重试</el-button>
          </div>
          
          <div v-else-if="messages.length === 0" class="empty-messages">
            <el-empty description="暂无消息"></el-empty>
          </div>
          
          <div v-else v-for="(message, index) in messages" :key="message.id" class="message-wrapper">
            <div class="message-time" v-if="showMessageTime(message, index)">
              {{ formatFullTime(message.sentAt) }}
            </div>
            <div class="message" :class="{ 'message-self': message.senderId === userId, 'message-other': message.senderId !== userId }">
              <el-avatar 
                v-if="message.senderId !== userId" 
                :size="30" 
                :src="message.senderAvatar"
                class="message-avatar"
              ></el-avatar>
              
              <div class="message-content">
                <template v-if="message.contentType === 'text'">
                  <div class="message-text">{{ message.content }}</div>
                </template>
                
                <template v-else-if="message.contentType === 'image'">
                  <div class="message-image">
                    <el-image
                      :src="message.filePath"
                      :preview-src-list="[message.filePath]"
                      fit="cover"
                      lazy
                    ></el-image>
                  </div>
                </template>
                
                <template v-else-if="message.contentType === 'file'">
                  <div class="message-file">
                    <i class="el-icon-document"></i>
                    <a :href="message.filePath" target="_blank">{{ message.fileName || message.content || '文件' }}</a>
                  </div>
                </template>
              </div>
              
              <el-avatar 
                v-if="message.senderId === userId" 
                :size="30" 
                :src="message.senderAvatar || userAvatar"
                class="message-avatar"
              ></el-avatar>
            </div>
          </div>
          
          <div class="loading-more" v-if="hasMoreMessages">
            <el-button type="text" @click="loadMoreMessages" :loading="loadingMore">加载更多消息</el-button>
          </div>
        </div>
        
        <div class="chat-input" v-if="!currentSession.isReadOnly">
          <div class="input-toolbar">
            <el-tooltip content="发送图片" placement="top">
              <el-button size="small" @click="triggerImageUpload">
                <i class="el-icon-picture"></i>
              </el-button>
            </el-tooltip>
            <el-tooltip content="发送文件" placement="top">
              <el-button size="small" @click="triggerFileUpload">
                <i class="el-icon-document"></i>
              </el-button>
            </el-tooltip>
            <input 
              type="file" 
              ref="imageInput" 
              accept="image/*" 
              style="display: none"
              @change="handleImageUpload"
            />
            <input 
              type="file" 
              ref="fileInput" 
              style="display: none"
              @change="handleFileUpload"
            />
          </div>
          
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
      </template>
      
      <div v-else class="no-session">
        <el-empty description="选择一个会话开始聊天"></el-empty>
        <el-button v-if="filteredSessions.length > 0" class="retry-button" type="primary" @click="autoSelectFirstSession">
          自动选择会话
        </el-button>
      </div>
    </div>
    
    <!-- 会话信息侧栏 -->
    <el-drawer
      v-model="showSessionInfo"
      title="会话信息"
      size="30%"
      :before-close="handleCloseSessionInfo"
      direction="rtl"
    >
      <div v-if="currentSession" class="session-detail">
        <div class="detail-header">
          <el-avatar :size="80" :src="getSessionAvatar(currentSession)"></el-avatar>
          <h3>{{ currentSession.title || currentSession.name }}</h3>
          <el-tag size="small">{{ getSessionTypeText(currentSession.type) }}</el-tag>
        </div>
        
        <div class="detail-item">
          <span class="detail-label">会话类型：</span>
          <span class="detail-value">{{ getSessionTypeText(currentSession.type) }}</span>
        </div>
        
        <div class="detail-actions">
          <el-popconfirm
            title="确定要删除此会话吗？"
            @confirm="deleteSession(currentSession.id)"
          >
            <template #reference>
              <el-button type="danger" plain>删除会话</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { getChatSessions, getChatMessages, sendChatMessage, markMessageRead, uploadChatFile } from '@/api/chat'
import { userStore } from '@/stores/user'

// 路由和状态管理
const route = useRoute()
const router = useRouter()
const searchQuery = ref('')
const sessionList = ref([])
const sessionsLoading = ref(false)
const currentSession = ref(null)
const messages = ref([])
const messageText = ref('')
const messageListRef = ref(null)
const userId = ref(parseInt(localStorage.getItem('userId') || '0'))
const userAvatar = ref(localStorage.getItem('userAvatar') || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const userName = ref(localStorage.getItem('userName') || '辅导员')
const loadRetryCount = ref(0)
const MAX_RETRIES = 3
const showSessionInfo = ref(false)
const chatError = ref(false)
const errorMessage = ref('')
const hasMoreMessages = ref(false)
const loadingMore = ref(false)
const imageInput = ref(null)
const fileInput = ref(null)
let autoRetryTimer = null

// WebSocket 相关
let ws = null
const wsConnected = ref(false)
const wsUrl = computed(() => {
  const token = userStore.token;
  // 自动适配后端端口，开发环境用8080，生产环境用当前域名
  let base = window.location.origin.replace(/^http/, 'ws');
  // 支持多端口开发环境，统一替换为8080
  base = base.replace(/:(5173|5174|5175)/, ':8080');
  return `${base}/ws/chat?token=${encodeURIComponent(token)}`;
})

// 过滤后的会话列表
const filteredSessions = computed(() => {
  if (!searchQuery.value) return sessionList.value
  
  const keyword = searchQuery.value.toLowerCase()
  return sessionList.value.filter(session => 
    (session.title || session.name || '').toLowerCase().includes(keyword) || 
    (session.lastMessage?.content || '').toLowerCase().includes(keyword)
  )
})

// 获取会话头像
const getSessionAvatar = (session) => {
  if (session.participantType === 'student') {
    return session.participantAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  } else if (session.participantType === 'company') {
    return session.participantAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
  } else {
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
}

// 获取会话类型显示文本
const getSessionTypeText = (type) => {
  const typeMap = {
    'SE': '学生-企业会话',
    'SC': '学生-辅导员会话',
    'SS': '学生群组',
    'student': '学生',
    'company': '企业'
  }
  return typeMap[type] || '未知类型'
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  // 今天内的消息显示时分
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 昨天的消息显示"昨天"
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.getDate() === yesterday.getDate() && date.getMonth() === yesterday.getMonth() && date.getFullYear() === yesterday.getFullYear()) {
    return '昨天'
  }
  
  // 当年内的其他日期显示月日
  if (date.getFullYear() === now.getFullYear()) {
    return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  }
  
  // 其他情况，显示年月日
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 格式化完整时间
const formatFullTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  
  // 今天内的消息显示时分
  if (date.getDate() === now.getDate() && date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear()) {
    return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 昨天的消息
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.getDate() === yesterday.getDate() && date.getMonth() === yesterday.getMonth() && date.getFullYear() === yesterday.getFullYear()) {
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 其他日期
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 判断是否显示消息时间
const showMessageTime = (message, index) => {
  if (index === 0) return true
  
  const prevMessage = messages.value[index - 1]
  const currentTime = new Date(message.sentAt).getTime()
  const prevTime = new Date(prevMessage.sentAt).getTime()
  
  // 如果两条消息间隔超过5分钟，显示时间
  return currentTime - prevTime > 5 * 60 * 1000
}

// 获取会话列表
const fetchSessions = async (fromRetry = false) => {
  if (sessionsLoading.value && !fromRetry) return
  
  sessionsLoading.value = true
  console.log('开始加载会话列表，是否来自重试:', fromRetry)
  
  try {
    const response = await getChatSessions()
    
    if (response && response.list) {
      console.log('获取到会话列表:', response.list)
      sessionList.value = response.list
      
      // 重置重试计数
      loadRetryCount.value = 0
      
      // 处理URL中的会话ID
      handleSessionFromUrl()
      
      // 显示明确成功消息
      if (fromRetry) {
        ElMessage.success('会话列表已刷新')
      }
    } else {
      throw new Error('获取会话列表失败')
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
    ElMessage.error('获取会话列表失败，请稍后重试')
    handleLoadError()
  } finally {
    sessionsLoading.value = false
  }
}

// 处理从URL加载会话逻辑
const handleSessionFromUrl = async () => {
  // 如果有query参数，选择对应的会话
  const { sessionId } = route.query
  if (sessionId) {
    const sessionIdNum = parseInt(sessionId, 10)
    if (!isNaN(sessionIdNum)) {
      const session = sessionList.value.find(s => s.id === sessionIdNum)
      if (session) {
        // 使用延迟确保DOM已渲染完成
        setTimeout(() => {
          selectSession(session)
        }, 100)
        return
      } else if (sessionList.value.length > 0) {
        // 如果有会话但没找到指定ID，选择第一个会话
        setTimeout(() => {
          autoSelectFirstSession()
        }, 100)
      }
    }
  }
  
  // 如果没有URL参数但有会话，选择第一个
  if (sessionList.value.length > 0) {
    // 没有sessionId参数时，自动选择第一个会话
    autoSelectFirstSession()
  }
}

// 自动选择第一个会话
const autoSelectFirstSession = () => {
  if (sessionList.value.length > 0) {
    nextTick(() => {
      selectSession(sessionList.value[0])
    })
  } else {
    ElMessage.warning('暂无可用会话')
  }
}

// 处理加载错误
const handleLoadError = () => {
  loadRetryCount.value++
  
  if (loadRetryCount.value <= MAX_RETRIES) {
    console.log(`加载失败，${loadRetryCount.value}秒后自动重试...`)
    
    if (autoRetryTimer) {
      clearTimeout(autoRetryTimer)
    }
    
    autoRetryTimer = setTimeout(() => {
      fetchSessions(true)
    }, loadRetryCount.value * 1000)
  } else {
    console.error('多次重试失败，请手动刷新')
    ElMessage.error('加载失败，请手动刷新页面')
  }
}

// 手动刷新会话列表
const refreshSessions = async () => {
  // 清除当前会话
  currentSession.value = null
  messages.value = []
  chatError.value = false
  
  // 重新加载会话
  await fetchSessions(true)
}

// 选择会话
const selectSession = (session) => {
  // 防止重复选择相同会话
  if (currentSession.value && currentSession.value.id === session.id) return
  
  try {
    currentSession.value = session
    messages.value = []
    hasMoreMessages.value = false
    chatError.value = false
    
    // 更新URL参数，但不触发新的导航
    if (route.query.sessionId !== String(session.id)) {
      router.replace({
        query: { ...route.query, sessionId: session.id }
      })
    }
    
    // 加载会话消息
    fetchMessages(session.id)
  } catch (error) {
    console.error('选择会话失败:', error)
    ElMessage.error('选择会话失败，请重试')
  }
}

// 获取特定会话的消息
const fetchMessages = async (sessionId, isLoadMore = false) => {
  if (!sessionId) return
  
  messages.value = isLoadMore ? messages.value : []
  const loadingInstance = isLoadMore ? null : ElLoading.service({
    target: messageListRef.value,
    text: '加载消息中...'
  })
  
  try {
    // 构建查询参数
    const params = {
      page: 1,
      pageSize: 20,
      before: isLoadMore && messages.value.length > 0 ? messages.value[0].id : undefined
    }
    
    const response = await getChatMessages(sessionId, params)
    
    if (response && response.list) {
      const newMessages = response.list.map(msg => ({
        ...msg,
        senderAvatar: msg.senderAvatar || getDefaultAvatar(msg.senderType)
      }))
      
      if (isLoadMore) {
        // 在现有消息前添加更多消息
        messages.value = [...newMessages, ...messages.value]
      } else {
        // 设置消息列表
        messages.value = newMessages
      }
      
      hasMoreMessages.value = response.hasMore || false
      
      // 更新会话未读消息数，标记为已读
      if (currentSession.value) {
        await markMessageRead(sessionId)
        const index = sessionList.value.findIndex(session => session.id === currentSession.value.id)
        if (index !== -1) {
          sessionList.value[index].unreadCount = 0
        }
      }
      
      // 重置错误状态
      chatError.value = false
      errorMessage.value = ''
    } else {
      throw new Error('获取消息失败')
    }
  } catch (error) {
    console.error('获取消息失败:', error)
    ElMessage.error('获取消息失败，请稍后重试')
    chatError.value = true
    errorMessage.value = '获取消息失败，请稍后重试'
  } finally {
    if (loadingInstance) {
      loadingInstance.close()
    }
    
    // 滚动到底部
    if (!isLoadMore) {
      scrollToBottom()
    }
  }
}

// 重试加载函数
const retryLoad = () => {
  chatError.value = false
  errorMessage.value = ''
  
  if (currentSession.value) {
    // 如果会话存在，重新加载消息
    fetchMessages(currentSession.value.id)
  } else {
    // 如果会话不存在，重新加载会话列表
    fetchSessions(true)
  }
}

// 加载更多消息
const loadMoreMessages = () => {
  if (loadingMore.value || !hasMoreMessages.value || !currentSession.value) return
  loadingMore.value = true
  
  fetchMessages(currentSession.value.id, true).finally(() => {
    loadingMore.value = false
  })
}

// 根据发送者类型获取默认头像
const getDefaultAvatar = (senderType) => {
  if (senderType === 'student') {
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  } else if (senderType === 'company') {
    return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
  } else if (senderType === 'counselor') {
    return 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png'
  } else {
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
}

// 发送消息
const sendMessage = async () => {
  if (!messageText.value.trim() || !currentSession.value || currentSession.value.isReadOnly) return;
  const content = messageText.value.trim();
  messageText.value = '';
  // 不再本地插入临时消息，等待WebSocket推送
  if (ws && wsConnected.value) {
    try {
      ws.send(
        JSON.stringify({
          sessionId: currentSession.value.id,
          content,
          contentType: 'text',
        })
      );
      // 等待WebSocket推送
      return;
    } catch (e) {
      console.error('WebSocket发送失败，降级API', e);
    }
  }
  // 降级API
  try {
    const messageData = { content, contentType: 'text' };
    const res = await sendChatMessage(currentSession.value.id, messageData);
    // 不再本地插入消息，等待WebSocket推送
    if (!(res && typeof res === 'object' && (res.messageId || res.id))) {
      ElMessage.error(res?.message || '发送消息失败');
    }
  } catch (error) {
    ElMessage.error('发送消息失败，请稍后重试');
  }
};

// 触发图片上传
const triggerImageUpload = () => {
  if (imageInput.value) {
    imageInput.value.click()
  }
}

// 处理图片上传
const handleImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  // 检查文件大小
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }
  
  try {
    // 创建临时消息
    const tempMessage = {
      id: 'temp-' + Date.now(),
      sessionId: currentSession.value.id,
      senderId: userId.value,
      senderType: 'counselor',
      senderName: userName.value,
      senderAvatar: userAvatar.value,
      content: URL.createObjectURL(file),
      contentType: 'image',
      sentAt: new Date().toISOString(),
      status: 'uploading'
    }
    
    // 添加到消息列表
    messages.value.push(tempMessage)
    scrollToBottom()
    
    // 上传图片
    const res = await uploadChatFile(currentSession.value.id, file, 'image')
    
    if (res && res.fileUrl) {
      // 发送图片消息
      const messageData = {
        content: res.fileUrl,
        contentType: 'image'
      }
      const msgRes = await sendChatMessage(currentSession.value.id, messageData)
      
      // 更新临时消息
      const index = messages.value.findIndex(m => m.id === tempMessage.id)
      if (index !== -1) {
        messages.value[index].id = msgRes.messageId
        messages.value[index].status = 'sent'
        messages.value[index].filePath = res.fileUrl
        messages.value[index].content = null
      }
      
      // 更新会话列表
      updateSessionLastMessage('[图片]')
      
      ElMessage.success('图片发送成功')
    } else {
      throw new Error('图片上传失败')
    }
  } catch (error) {
    console.error('上传图片失败:', error)
    ElMessage.error('上传图片失败，请稍后重试')
    
    // 移除临时消息
    const index = messages.value.findIndex(m => m.id === 'temp-' + Date.now())
    if (index !== -1) {
      messages.value.splice(index, 1)
    }
  } finally {
    // 清空文件输入框，方便下次选择相同文件
    if (imageInput.value) {
      imageInput.value.value = ''
    }
  }
}

// 触发文件上传
const triggerFileUpload = () => {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

// 处理文件上传
const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 检查文件大小
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return
  }
  
  try {
    // 创建临时消息
    const tempMessage = {
      id: 'temp-' + Date.now(),
      sessionId: currentSession.value.id,
      senderId: userId.value,
      senderType: 'counselor',
      senderName: userName.value,
      senderAvatar: userAvatar.value,
      content: file.name,
      fileName: file.name,
      contentType: 'file',
      sentAt: new Date().toISOString(),
      status: 'uploading'
    }
    
    // 添加到消息列表
    messages.value.push(tempMessage)
    scrollToBottom()
    
    // 上传文件
    const res = await uploadChatFile(currentSession.value.id, file, 'file')
    
    if (res && res.fileUrl) {
      // 发送文件消息
      const messageData = {
        content: res.fileUrl,
        contentType: 'file',
        fileName: file.name
      }
      const msgRes = await sendChatMessage(currentSession.value.id, messageData)
      
      // 更新临时消息
      const index = messages.value.findIndex(m => m.id === tempMessage.id)
      if (index !== -1) {
        messages.value[index].id = msgRes.messageId
        messages.value[index].status = 'sent'
        messages.value[index].filePath = res.fileUrl
      }
      
      // 更新会话列表
      updateSessionLastMessage(`[文件: ${file.name}]`)
      
      ElMessage.success('文件发送成功')
    } else {
      throw new Error('文件上传失败')
    }
  } catch (error) {
    console.error('上传文件失败:', error)
    ElMessage.error('上传文件失败，请稍后重试')
    
    // 移除临时消息
    const index = messages.value.findIndex(m => m.id === 'temp-' + Date.now())
    if (index !== -1) {
      messages.value.splice(index, 1)
    }
  } finally {
    // 清空文件输入框，方便下次选择相同文件
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

// 更新会话列表中的最后一条消息
const updateSessionLastMessage = (messageText) => {
  if (!currentSession.value) return
  
  const index = sessionList.value.findIndex(s => s.id === currentSession.value.id)
  if (index !== -1) {
    sessionList.value[index].lastMessage = {
      content: messageText,
      sentAt: new Date().toISOString()
    }
    sessionList.value[index].lastActiveAt = new Date().toISOString()
    
    // 将会话移到顶部
    const currentSessionData = sessionList.value.splice(index, 1)[0]
    sessionList.value.unshift(currentSessionData)
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

// 关闭会话信息侧栏
const handleCloseSessionInfo = () => {
  showSessionInfo.value = false
}

// 删除会话
const deleteSession = async (sessionId) => {
  try {
    // 模拟删除成功
    setTimeout(() => {
      const index = sessionList.value.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        sessionList.value.splice(index, 1)
      }
      
      if (currentSession.value && currentSession.value.id === sessionId) {
        currentSession.value = null
        messages.value = []
      }
      
      showSessionInfo.value = false
      ElMessage.success('会话已删除')
      
      // 如果还有其他会话，自动选择第一个
      if (sessionList.value.length > 0) {
        autoSelectFirstSession()
      }
    }, 500)
  } catch (error) {
    console.error('删除会话失败:', error)
    ElMessage.error('删除会话失败，请稍后重试')
  }
}

// WebSocket 消息处理
const handleWsMessage = (event) => {
  try {
    const data = JSON.parse(event.data)
    if (data && data.type === 'chat_message') {
      // 判断消息是否属于当前会话
      if (currentSession.value && data.sessionId === currentSession.value.id) {
        // 插入消息到当前会话
        messages.value.push({
          ...data.message,
          senderAvatar: data.message.senderAvatar || getDefaultAvatar(data.message.senderType)
        })
        scrollToBottom()
        // 标记为已读
        markMessageRead(currentSession.value.id)
      } else {
        // 更新会话未读数
        const idx = sessionList.value.findIndex(s => s.id === data.sessionId)
        if (idx !== -1) {
          sessionList.value[idx].unreadCount = (sessionList.value[idx].unreadCount || 0) + 1
          sessionList.value[idx].lastMessage = data.message
          sessionList.value[idx].lastActiveAt = data.message.sentAt
        }
      }
    }
  } catch (e) {
    // 忽略解析错误
  }
}

// 建立WebSocket连接
const connectWebSocket = () => {
  if (ws) return
  try {
    ws = new window.WebSocket(wsUrl.value)
    ws.onopen = () => {
      wsConnected.value = true
    }
    ws.onmessage = handleWsMessage
    console.log(event.data)
    ws.onclose = () => {
      wsConnected.value = false
      ws = null
      // 可选：自动重连
      setTimeout(connectWebSocket, 5000)
    }
    ws.onerror = () => {
      wsConnected.value = false
      ws && ws.close()
      ws = null
    }
  } catch (e) {
    wsConnected.value = false
    ws = null
  }
}

// 断开WebSocket连接
const disconnectWebSocket = () => {
  if (ws) {
    ws.close()
    ws = null
    wsConnected.value = false
  }
}

// 组件挂载时加载会话列表
onMounted(() => {
  fetchSessions()
  connectWebSocket()
})

// 组件销毁前清理
onBeforeUnmount(() => {
  // 清理自动重试定时器
  if (autoRetryTimer) {
    clearTimeout(autoRetryTimer)
    autoRetryTimer = null
  }
  disconnectWebSocket()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 120px);
  overflow: hidden;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chat-sidebar {
  width: 300px;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.search-box {
  padding: 15px;
  border-bottom: 1px solid #e6e6e6;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 12px 15px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s;
}

.session-item:hover {
  background-color: #f5f7fa;
}

.session-item.active {
  background-color: #ecf5ff;
}

.session-badge {
  margin-right: 12px;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.session-name {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 12px;
  color: #909399;
}

.session-message {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  height: 60px;
  padding: 0 20px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background-color: #f5f7fa;
}

.message-wrapper {
  margin-bottom: 15px;
}

.message-time {
  text-align: center;
  margin-bottom: 4px;
  font-size: 12px;
  color: #909399;
}

.message {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
}

.message-self {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 10px;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 10px 12px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  word-break: break-word;
}

.message-self .message-text {
  background-color: #ecf5ff;
}

.message-image {
  max-width: 200px;
  border-radius: 4px;
  overflow: hidden;
}

.message-file {
  padding: 10px 12px;
  background-color: #fff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-self .message-text,
.message-self .message-file,
.message-self .message-unknown {
  background-color: #ecf5ff;
}

.loading-more {
  text-align: center;
  margin: 10px 0;
}

.chat-input {
  padding: 15px;
  border-top: 1px solid #e6e6e6;
}

.input-toolbar {
  margin-bottom: 10px;
}

.input-actions {
  text-align: right;
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.no-session {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #f5f7fa;
}

.session-detail {
  padding: 20px;
}

.detail-header {
  text-align: center;
  margin-bottom: 30px;
}

.detail-item {
  margin-bottom: 15px;
}

.detail-label {
  font-weight: 500;
  margin-right: 10px;
}

.detail-actions {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chat-error-container {
  text-align: center;
  padding: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  background-color: #f8f8f8;
  border-radius: 8px;
  margin: 20px;
}

.retry-button {
  margin-top: 20px;
  width: 120px;
}

.empty-messages {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #909399;
}

.sessions-loading {
  padding: 20px;
}

.empty-sessions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.session-actions {
  padding: 10px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}

.refresh-button {
  margin: 10px auto;
}

.readonly-notice {
  margin-top: 15px;
  padding: 15px;
}
</style> 