<template>
  <div class="chat-container">
    <div class="chat-sidebar">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话"
          prefix-icon="el-icon-search"
          clearable
        />
      </div>
      
      <div class="session-list">
        <div v-if="loading" class="sessions-loading">
          <el-skeleton animated :rows="3" />
        </div>
        <div v-else-if="sessions.length === 0" class="empty-sessions">
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
            :class="{ active: currentSession && currentSession.id === session.id }"
            @click="selectSession(session)"
          >
            <el-badge :value="session.unread" :hidden="!session.unread" class="session-badge">
              <el-avatar :size="40" :src="session.avatar || defaultAvatar"></el-avatar>
            </el-badge>
            <div class="session-info">
              <div class="session-header">
                <span class="session-name">{{ session.name }}</span>
                <span class="session-time">{{ session.lastTime }}</span>
              </div>
              <div class="session-message">{{ session.lastMessage || '暂无消息' }}</div>
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
            {{ currentSession.participantInfo?.name || currentSession.name }}
            <el-tag v-if="currentSession.relatedJobTitle" size="small">
              {{ currentSession.relatedJobTitle }}
            </el-tag>
          </div>
          <div class="header-actions">
            <el-button size="small" @click="showSessionInfo = true">查看信息</el-button>
          </div>
        </div>
        
        <div ref="messagesWrapper" class="chat-messages-wrapper" @scroll="handleScroll">
          <div v-if="hasMoreMessages && !loadingMessages" class="load-more" @click="loadMoreMessages">
            <el-button type="text">加载更多消息</el-button>
          </div>
          
          <div v-if="chatError" class="chat-error">
            <el-alert
              :title="errorMessage"
              type="error"
              show-icon
              @close="chatError = false"
            />
          </div>
          
          <div ref="messageList" class="chat-messages">
            <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
              <div class="message-time" v-if="showMessageTime(msg, index)">
                {{ msg.time }}
              </div>
              <div class="message" :class="{ 'message-self': msg.isSelf, 'message-other': !msg.isSelf }">
                <el-avatar 
                  v-if="!msg.isSelf" 
                  :size="30" 
                  :src="msg.avatar"
                  class="message-avatar"
                ></el-avatar>
                
                <div class="message-content">
                  <div class="message-sender" v-if="!msg.isSelf" style="font-size:12px;color:#888;margin-bottom:2px;">
                    {{ msg.senderName }}
                  </div>
                  <!-- 文本消息 -->
                  <div v-if="msg.type === 'text'" class="message-text">{{ msg.content }}</div>
                  
                  <!-- 图片消息 -->
                  <div v-else-if="msg.type === 'image'" class="message-image">
                    <el-image
                      :src="msg.content"
                      :preview-src-list="[msg.content]"
                      fit="cover"
                      style="max-width: 200px; max-height: 200px;"
                      @error="handleImageError"
                    >
                      <template #error>
                        <div class="image-error">
                          <el-icon><Picture /></el-icon>
                          <span>图片加载失败</span>
                        </div>
                      </template>
                    </el-image>
                  </div>
                  
                  <!-- 文件消息 -->
                  <div v-else-if="msg.type === 'file' && msg.file" class="message-file">
                    <el-link
                      type="primary"
                      :href="msg.file.path"
                      target="_blank"
                      :underline="false"
                      class="file-link"
                    >
                      <el-icon><Document /></el-icon>
                      <span class="file-name">{{ msg.file.name || '文件' }}</span>
                      <span v-if="msg.file.size" class="file-size">({{ formatFileSize(msg.file.size) }})</span>
                    </el-link>
                  </div>
                  
                  <!-- 未知类型消息 -->
                  <div v-else class="message-unknown">
                    <el-tag type="info">不支持的消息类型</el-tag>
                  </div>
                </div>
                
                <el-avatar 
                  v-if="msg.isSelf" 
                  :size="30" 
                  :src="msg.avatar"
                  class="message-avatar"
                ></el-avatar>
              </div>
            </div>
          </div>
        </div>
        
        <div class="chat-input">
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
            @keydown.enter.exact.prevent="sendMessage"
          />
          
          <div class="input-actions">
            <el-button type="primary" @click="sendMessage" :disabled="!messageText.trim()">
              发送
            </el-button>
          </div>
        </div>
      </template>
      
      <div v-else class="no-session">
        <el-empty description="选择一个会话开始聊天"></el-empty>
        <el-button v-if="sessions.length > 0" class="retry-button" type="primary" @click="autoSelectFirstSession">
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
          <el-avatar :size="80" :src="currentSession.avatar || defaultAvatar"></el-avatar>
          <h3>{{ currentSession.name }}</h3>
          <el-tag size="small">{{ getSessionTypeText(currentSession.type) }}</el-tag>
        </div>
        
        <div v-if="currentSession.relatedJobTitle" class="detail-item">
          <span class="detail-label">相关岗位：</span>
          <span class="detail-value">{{ currentSession.relatedJobTitle }}</span>
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
import { getChatSessions, getChatMessages, sendChatMessage, uploadChatFile } from '@/api/chat'
import { callApi, callUploadApi } from '@/utils/apiUtils'
import { useUserStore } from '@/store/user'

// 路由和状态管理
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const searchKeyword = ref('')
const sessions = ref([])
const loading = ref(false)
const currentSession = ref(null)
const messages = ref([])
const messageText = ref('')
const messageList = ref(null)
const imageInput = ref(null)
const fileInput = ref(null)
const loadingMessages = ref(false)
const loadingMore = ref(false)
const currentSessionId = ref(null)
const hasMoreMessages = ref(false)
const loadRetryCount = ref(0)
const MAX_RETRIES = 3
const showSessionInfo = ref(false)
let autoRetryTimer = null

// 错误处理状态
const chatError = ref(false)
const errorMessage = ref('')

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
});

let heartbeatTimer = null;
let lastPongTime = Date.now();
const HEARTBEAT_INTERVAL = 30000; // 30秒
const HEARTBEAT_TIMEOUT = 35000; // 35秒

function startHeartbeat() {
  stopHeartbeat();
  heartbeatTimer = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'ping' }));
      // 检查pong超时
      if (Date.now() - lastPongTime > HEARTBEAT_TIMEOUT) {
        ws.close(); // 触发重连
      }
    }
  }, HEARTBEAT_INTERVAL);
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
}

// 筛选后的会话列表
const filteredSessions = computed(() => {
  if (!searchKeyword.value) {
    return sessions.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return sessions.value.filter(session => 
    session.name.toLowerCase().includes(keyword) || 
    (session.lastMessage && session.lastMessage.toLowerCase().includes(keyword))
  )
})

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

// 获取会话类型显示文本
const getSessionTypeText = (type) => {
  if (type === 'SE') return '学生-企业会话'
  if (type === 'SC') return '学生-辅导员会话'
  if (type === 'SS') return '学生群组'
  return '未知类型'
}

// 加载会话列表
const loadSessions = async (fromRetry = false) => {
  if (loading.value && !fromRetry) return // 防止重复加载
  
  loading.value = true
  console.log('开始加载会话列表，是否来自重试:', fromRetry)
  
  try {
    const result = await callApi(getChatSessions())
    
    // 处理会话数据
    sessions.value = (result.list || []).map(session => ({
      id: session.id,
      name: session.title || '未命名会话',
      avatar: session.participantInfo?.avatar || '',
      lastMessage: session.lastMessage?.content || '',
      lastTime: formatSessionTime(session.lastActiveAt),
      unread: session.unreadCount || 0,
      type: session.type,
      relatedJobId: session.relatedJobId,
      relatedJobTitle: session.relatedJobTitle || ''
    }))
    
    // 重置重试计数
    loadRetryCount.value = 0
    
    // 如果路由参数有会话ID，选择对应会话
    if (currentSessionId.value) {
      const session = sessions.value.find(s => s.id === currentSessionId.value)
      if (session) {
        selectSession(session)
      } else if (sessions.value.length > 0) {
        // 如果找不到指定会话但有其他会话，选择第一个
        autoSelectFirstSession()
      }
    } else if (sessions.value.length > 0 && !currentSession.value) {
      // 如果没有指定会话ID但有会话列表且当前未选择会话，自动选择第一个
      autoSelectFirstSession()
    }
    
    // 显示明确成功消息
    if (fromRetry) {
      ElMessage.success('会话列表已刷新')
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败，请刷新页面重试')
    handleLoadError()
  } finally {
    loading.value = false
  }
}

// 自动选择第一个会话
const autoSelectFirstSession = () => {
  if (sessions.value.length > 0) {
    console.log('自动选择第一个会话')
    nextTick(() => {
      selectSession(sessions.value[0])
    })
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
      loadSessions(true)
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
  await loadSessions(true)
}

// 选择会话
const selectSession = async (session) => {
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
    await loadSessionMessages(session.id)
    
    // 清除未读计数
    const sessionIndex = sessions.value.findIndex(s => s.id === session.id)
    if (sessionIndex !== -1) {
      sessions.value[sessionIndex].unread = 0
    }
  } catch (error) {
    console.error('选择会话失败:', error)
    ElMessage.error('选择会话失败，请重试')
  }
}

// 加载会话消息
const loadSessionMessages = async (sessionId, isLoadMore = false) => {
  if (!sessionId) return;
  
  loadingMessages.value = true;
  try {
    // 构建查询参数
    const params = {
      page: 1,
      pageSize: 20,
      before: isLoadMore && messages.value.length > 0 ? messages.value[0].id : undefined
    }
    
    const { error, body, message } = await getChatMessages(sessionId, params);
    console.log('加载聊天消息结果:', { error, body, message });
    
    if (error !== 0) {
      errorMessage.value = message || '加载消息失败';
      chatError.value = true;
      return;
    }
    
    // 确保body.list是一个数组
    if (!body || !Array.isArray(body.list)) {
      console.warn('消息返回结构异常:', body);
      errorMessage.value = '消息数据结构异常';
      chatError.value = true;
      return;
    }
    
    console.log('处理消息数据，列表长度:', body.list.length);
    
    // 处理消息列表
    const newMessages = body.list.map(msg => {
      // 判断是否自己发送
      const isSelf = msg.senderId === userStore.userInfo.id;
      let senderName = isSelf
        ? (userStore.userInfo.name || '我')
        : (currentSession.value?.participantInfo?.name || '对方');
      let avatar = isSelf ? userStore.userInfo.avatar || defaultAvatar : (currentSession.value?.avatar || defaultAvatar);
      return {
        id: msg.id,
        content: msg.content,
        type: msg.contentType?.toLowerCase() || 'text',
        senderName: senderName,
        isSelf: isSelf,
        avatar: avatar,
        time: formatMessageTime(msg.sentAt || new Date()),
        sentAt: msg.sentAt,
        file: msg.contentType === 'FILE' ? {
          name: msg.fileName || '文件',
          path: msg.filePath || '',
          size: msg.fileSize || 0
        } : null
      };
    });
    
    console.log('处理后的消息数据:', newMessages);
    
    if (isLoadMore) {
      // 加载更多消息（将新消息添加到前面）
      messages.value = [...newMessages, ...messages.value];
    } else {
      // 首次加载
      messages.value = newMessages;
    }
    
    // 更新是否有更多消息的标志
    hasMoreMessages.value = body.hasMore || false;
    
    // 如果是首次加载，滚动到最底部
    if (!isLoadMore) {
      await nextTick(() => {
        scrollToBottom();
      });
    }
  } catch (error) {
    console.error('加载消息失败:', error);
    errorMessage.value = '加载消息失败，请稍后重试';
    chatError.value = true;
  } finally {
    loadingMessages.value = false;
  }
}

// 重试加载函数
const retryLoad = () => {
  chatError.value = false
  errorMessage.value = ''
  
  if (currentSession.value) {
    // 如果会话存在，重新加载消息
    loadSessionMessages(currentSession.value.id)
  } else {
    // 如果会话不存在，重新加载会话列表
    loadSessions(true)
  }
}

// 加载更多消息
const loadMoreMessages = () => {
  if (loadingMore.value || !hasMoreMessages.value || !currentSession.value) return
  loadingMore.value = true
  
  loadSessionMessages(currentSession.value.id, true).finally(() => {
    loadingMore.value = false
  })
}

// 判断是否显示消息时间
const showMessageTime = (message, index) => {
  if (index === 0) return true
  
  const prevMessage = messages.value[index - 1]
  const currentTime = new Date(message.sentAt || message.time).getTime()
  const prevTime = new Date(prevMessage.sentAt || prevMessage.time).getTime()
  
  // 如果两条消息间隔超过5分钟，显示时间
  return currentTime - prevTime > 5 * 60 * 1000
}

// 发送消息
const sendMessage = async () => {
  if (!messageText.value.trim() || !currentSession.value) return;
  const content = messageText.value;
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
    const res = await callApi(sendChatMessage(currentSession.value.id, messageData));
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
    sentAt: new Date().toISOString(),
    isSelf: true,
    avatar: userStore.userInfo?.avatar || defaultAvatar,
    uploading: true
  }
  
  messages.value.push(tempMessage)
  await nextTick()
  scrollToBottom()
  
  try {
    // 上传文件
    const uploadResult = await callUploadApi(uploadChatFile(file, currentSession.value.id, contentType))
    
    // 发送文件消息
    const sendResult = await callApi(sendChatMessage(currentSession.value.id, {
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
        sentAt: sendResult.sentAt || new Date().toISOString(),
        isSelf: true,
        avatar: userStore.userInfo?.avatar || defaultAvatar,
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

// 格式化消息时间
const formatMessageTime = (time) => {
  if (!time) return ''
  
  const date = new Date(time)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
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

// 关闭会话信息侧栏
const handleCloseSessionInfo = () => {
  showSessionInfo.value = false
}

// 删除会话
const deleteSession = async (sessionId) => {
  try {
    // 模拟删除成功
    setTimeout(() => {
      const index = sessions.value.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        sessions.value.splice(index, 1)
      }
      
      if (currentSession.value && currentSession.value.id === sessionId) {
        currentSession.value = null
        messages.value = []
      }
      
      showSessionInfo.value = false
      ElMessage.success('会话已删除')
      
      // 如果还有其他会话，自动选择第一个
      if (sessions.value.length > 0) {
        autoSelectFirstSession()
      }
    }, 500)
  } catch (error) {
    console.error('删除会话失败:', error)
    ElMessage.error('删除会话失败，请稍后重试')
  }
}

// 预览图片
const previewImage = (imageUrl) => {
  window.open(imageUrl, '_blank')
}

// WebSocket 消息处理
const handleWsMessage = (event) => {
  try {
    const data = JSON.parse(event.data);
    if (data.type === 'pong') {
      lastPongTime = Date.now();
      return;
    }
    if (data && data.type === 'chat_message' && data.sessionId && data.message) {
      if (
        currentSession.value &&
        data.sessionId === currentSession.value.id
      ) {
        const msg = data.message;
        const isSelf = msg.senderId === userStore.userInfo.id;
        let senderName = isSelf
          ? (userStore.userInfo.name || '我')
          : (currentSession.value?.participantInfo?.name || '对方');
        let avatar = isSelf ? userStore.userInfo.avatar || defaultAvatar : (currentSession.value?.avatar || defaultAvatar);
        if (!messages.value.some(m => m.id === msg.messageId || m.id === msg.id)) {
          messages.value.push({
            id: msg.messageId || msg.id,
            content: msg.content,
            type: (msg.contentType || 'text').toLowerCase(),
            senderName,
            isSelf,
            avatar,
            time: formatMessageTime(msg.sentAt || new Date()),
            sentAt: msg.sentAt,
            file: msg.contentType === 'FILE' ? {
              name: msg.fileName || '文件',
              path: msg.filePath || '',
              size: msg.fileSize || 0
            } : null
          });
          nextTick(scrollToBottom);
        }
      }
    }
  } catch (e) {
    console.error("WebSocket消息解析失败", e);
  }
};

// 建立WebSocket连接
const connectWebSocket = () => {
  if (ws) return;
  try {
    ws = new window.WebSocket(wsUrl.value);
    ws.onopen = () => {
      wsConnected.value = true;
      lastPongTime = Date.now();
      startHeartbeat();
    };
    ws.onmessage = handleWsMessage;
    ws.onclose = () => {
      wsConnected.value = false;
      ws = null;
      stopHeartbeat();
      setTimeout(connectWebSocket, 5000);
    };
    ws.onerror = () => {
      wsConnected.value = false;
      ws && ws.close();
      ws = null;
      stopHeartbeat();
    };
  } catch (e) {
    wsConnected.value = false;
    ws = null;
    stopHeartbeat();
  }
};

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
  loadSessions()
  connectWebSocket()
})

// 组件销毁前清理
onBeforeUnmount(() => {
  // 清理自动重试定时器
  if (autoRetryTimer) {
    clearTimeout(autoRetryTimer)
    autoRetryTimer = null
  }
  stopHeartbeat()
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

.chat-messages-wrapper {
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

.message-unknown {
  padding: 10px 12px;
  background-color: #fff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-self .message-text,
.message-self .message-file,
.message-self .message-unknown {
  background-color: #ecf5ff;
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

.chat-error {
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

.file-size {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}
</style> 