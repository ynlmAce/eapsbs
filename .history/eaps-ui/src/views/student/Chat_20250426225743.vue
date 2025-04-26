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
        <div
          v-for="session in filteredSessions"
          :key="session.id"
          class="session-item"
          :class="{ active: currentSession && currentSession.id === session.id }"
          @click="selectSession(session)"
        >
          <el-badge :value="session.unreadCount" :hidden="!session.unreadCount" class="session-badge">
            <el-avatar :size="40" :src="getSessionAvatar(session)"></el-avatar>
          </el-badge>
          <div class="session-info">
            <div class="session-header">
              <span class="session-name">{{ getSessionName(session) }}</span>
              <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
            </div>
            <div class="session-message">{{ session.lastMessage }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-main">
      <template v-if="currentSession">
        <div class="chat-header">
          <div class="header-title">
            {{ getSessionName(currentSession) }}
            <el-tag v-if="currentSession.type === 'company' && currentSession.jobTitle" size="small">
              {{ currentSession.jobTitle }}
            </el-tag>
          </div>
          <div class="header-actions">
            <el-button size="small" @click="showSessionInfo = true">查看信息</el-button>
          </div>
        </div>
        
        <div class="chat-messages" ref="messagesContainer">
          <div v-for="(message, index) in currentMessages" :key="index" class="message-wrapper">
            <div class="message-time" v-if="showMessageTime(message, index)">
              {{ formatFullTime(message.sentAt) }}
            </div>
            <div 
              class="message" 
              :class="{ 'message-self': message.senderId === currentUserId, 'message-other': message.senderId !== currentUserId }"
            >
              <el-avatar 
                v-if="message.senderId !== currentUserId" 
                :size="30" 
                :src="getMessageAvatar(message)"
                class="message-avatar"
              ></el-avatar>
              
              <div class="message-content">
                <template v-if="message.contentType === 'text'">
                  <div class="message-text">{{ message.content }}</div>
                </template>
                
                <template v-else-if="message.contentType === 'image'">
                  <div class="message-image">
                    <el-image
                      :src="message.content"
                      :preview-src-list="[message.content]"
                      fit="cover"
                      lazy
                    ></el-image>
                  </div>
                </template>
                
                <template v-else-if="message.contentType === 'file'">
                  <div class="message-file">
                    <i class="el-icon-document"></i>
                    <a :href="message.content" target="_blank">{{ message.fileName || '文件' }}</a>
                  </div>
                </template>
              </div>
              
              <el-avatar 
                v-if="message.senderId === currentUserId" 
                :size="30" 
                :src="currentUserAvatar"
                class="message-avatar"
              ></el-avatar>
            </div>
          </div>
          
          <div class="loading-more" v-if="hasMoreMessages">
            <el-button type="text" @click="loadMoreMessages" :loading="loadingMore">加载更多消息</el-button>
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
            v-model="messageInput"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            resize="none"
            @keydown.enter.exact.prevent="sendTextMessage"
          />
          
          <div class="input-actions">
            <el-button type="primary" @click="sendTextMessage" :disabled="!messageInput.trim()">
              发送
            </el-button>
          </div>
        </div>
      </template>
      
      <div v-else class="no-session">
        <el-empty description="选择一个会话开始聊天"></el-empty>
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
          <h3>{{ getSessionName(currentSession) }}</h3>
          <el-tag size="small">{{ getSessionTypeText(currentSession.type) }}</el-tag>
        </div>
        
        <template v-if="currentSession.type === 'company'">
          <div class="detail-item">
            <span class="detail-label">企业名称：</span>
            <span class="detail-value">{{ currentSession.name }}</span>
          </div>
          
          <div v-if="currentSession.jobTitle" class="detail-item">
            <span class="detail-label">相关岗位：</span>
            <span class="detail-value">{{ currentSession.jobTitle }}</span>
          </div>
        </template>
        
        <template v-else-if="currentSession.type === 'counselor'">
          <div class="detail-item">
            <span class="detail-label">辅导员姓名：</span>
            <span class="detail-value">{{ currentSession.name }}</span>
          </div>
        </template>
        
        <div class="detail-actions">
          <el-popconfirm
            title="确定要删除此会话吗？"
            @confirm="deleteSession(currentSession.id)"
          >
            <template #reference>
              <el-button type="danger" plain>删除会话</el-button>
            </template>
          </el-popconfirm>
          
          <el-popconfirm
            title="确定要举报此会话吗？"
            @confirm="reportSession(currentSession.id)"
          >
            <template #reference>
              <el-button v-if="currentSession.type === 'company'" type="warning" plain>举报会话</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElLoading } from 'element-plus';
// 导入API函数
import { getChatSessions, getChatMessages, sendChatMessage, markMessageRead, uploadChatFile, reportChatMessage } from '@/api/chat';
import { useUserStore } from '@/store/user';

// 路由和状态管理
const route = useRoute();
const userStore = useUserStore();
const searchKeyword = ref('');
const sessions = ref([]);
const currentSession = ref(null);
const currentMessages = ref([]);
const messageInput = ref('');
const showSessionInfo = ref(false);
const hasMoreMessages = ref(false);
const loadingMore = ref(false);
const messagesContainer = ref(null);
const imageInput = ref(null);
const fileInput = ref(null);

// 当前用户信息
const currentUserId = computed(() => userStore.userId || 0);
const currentUserAvatar = computed(() => userStore.userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png');

// 筛选后的会话列表
const filteredSessions = computed(() => {
  if (!searchKeyword.value) {
    return sessions.value;
  }
  
  const keyword = searchKeyword.value.toLowerCase();
  return sessions.value.filter(session => 
    getSessionName(session).toLowerCase().includes(keyword) || 
    (session.lastMessage && session.lastMessage.toLowerCase().includes(keyword))
  );
});

// 获取会话头像
const getSessionAvatar = (session) => {
  if (session.type === 'company') {
    return session.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png';
  } else if (session.type === 'counselor') {
    return session.avatar || 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png';
  } else {
    return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
  }
};

// 获取会话名称
const getSessionName = (session) => {
  return session.name || (session.type === 'company' ? '企业' : '辅导员');
};

// 获取会话类型显示文本
const getSessionTypeText = (type) => {
  const typeMap = {
    'company': '企业',
    'counselor': '辅导员',
    'group': '群聊'
  };
  return typeMap[type] || '未知类型';
};

// 获取消息发送者头像
const getMessageAvatar = (message) => {
  if (message.senderType === 'company') {
    return message.senderAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png';
  } else if (message.senderType === 'counselor') {
    return message.senderAvatar || 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png';
  } else {
    return message.senderAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
  }
};

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now - date;
  
  // 今天内的消息显示时分
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
  
  // 昨天的消息显示"昨天"
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.getDate() === yesterday.getDate() && date.getMonth() === yesterday.getMonth() && date.getFullYear() === yesterday.getFullYear()) {
    return '昨天';
  }
  
  // 当年内的其他日期显示月日
  if (date.getFullYear() === now.getFullYear()) {
    return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
  }
  
  // 其他年份的消息显示年月日
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};

// 格式化完整时间
const formatFullTime = (timestamp) => {
  if (!timestamp) return '';
  
  const date = new Date(timestamp);
  const now = new Date();
  
  // 今天内的消息显示时分
  if (date.getDate() === now.getDate() && date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear()) {
    return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
  
  // 昨天的消息
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.getDate() === yesterday.getDate() && date.getMonth() === yesterday.getMonth() && date.getFullYear() === yesterday.getFullYear()) {
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
  
  // 其他日期
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

// 判断是否显示消息时间
const showMessageTime = (message, index) => {
  if (index === 0) return true;
  
  const prevMessage = currentMessages.value[index - 1];
  const currentTime = new Date(message.sentAt).getTime();
  const prevTime = new Date(prevMessage.sentAt).getTime();
  
  // 如果两条消息间隔超过5分钟，显示时间
  return currentTime - prevTime > 5 * 60 * 1000;
};

// 加载会话列表
const loadSessions = async () => {
  try {
    const res = await getChatSessions();
    
    if (res && res.error === 0) {
      sessions.value = res.body.list || [];
      
      // 如果有query参数，选择对应的会话
      const { sessionId } = route.query;
      if (sessionId) {
        const session = sessions.value.find(s => s.id === parseInt(sessionId));
        if (session) {
          selectSession(session);
        }
      }
    } else {
      ElMessage.error(res?.message || '加载会话列表失败');
    }
  } catch (error) {
    console.error('加载会话列表失败:', error);
    ElMessage.error('加载会话列表失败，请稍后重试');
  }
};

// 选择会话
const selectSession = async (session) => {
  if (currentSession.value && currentSession.value.id === session.id) return;
  
  currentSession.value = session;
  currentMessages.value = [];
  hasMoreMessages.value = false;
  
  // 标记为已读
  if (session.unreadCount > 0) {
    try {
      await markMessageRead(session.id);
      session.unreadCount = 0;
    } catch (error) {
      console.error('标记消息已读失败:', error);
    }
  }
  
  // 加载消息记录
  loadMessages();
};

// 加载消息记录
const loadMessages = async (isLoadMore = false) => {
  if (!currentSession.value) return;
  
  const loadingInstance = ElLoading.service({
    target: messagesContainer.value,
    text: '加载消息中...'
  });
  
  try {
    const params = {
      sessionId: currentSession.value.id,
      page: isLoadMore ? 1 : 1, // 根据实际API参数调整
      pageSize: 20,
      before: isLoadMore && currentMessages.value.length > 0 ? currentMessages.value[0].id : undefined
    };
    
    const res = await getChatMessages(currentSession.value.id, params.page, params.pageSize);
    
    if (res && res.error === 0) {
      const messages = res.body.list || [];
      
      if (isLoadMore) {
        currentMessages.value = [...messages, ...currentMessages.value];
      } else {
        currentMessages.value = messages;
      }
      
      hasMoreMessages.value = res.body.hasMore || false;
    } else {
      ElMessage.error(res?.message || '加载消息记录失败');
    }
    
    loadingInstance.close();
    
    // 滚动到底部
    if (!isLoadMore) {
      nextTick(() => {
        scrollToBottom();
      });
    }
  } catch (error) {
    console.error('加载消息记录失败:', error);
    ElMessage.error('加载消息记录失败，请稍后重试');
    loadingInstance.close();
  }
};

// 加载更多消息
const loadMoreMessages = () => {
  if (loadingMore.value || !hasMoreMessages.value) return;
  loadingMore.value = true;
  
  loadMessages(true).finally(() => {
    loadingMore.value = false;
  });
};

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

// 发送文本消息
const sendTextMessage = async () => {
  if (!messageInput.value.trim() || !currentSession.value) return;
  
  const content = messageInput.value;
  messageInput.value = '';
  
  const tempMessage = {
    id: `temp-${Date.now()}`,
    sessionId: currentSession.value.id,
    senderId: currentUserId.value,
    senderType: 'student',
    senderAvatar: currentUserAvatar.value,
    content,
    contentType: 'text',
    sentAt: new Date().toISOString(),
    status: 'sending'
  };
  
  currentMessages.value.push(tempMessage);
  nextTick(() => {
    scrollToBottom();
  });
  
  try {
    const res = await sendChatMessage(currentSession.value.id, content);
    
    if (res && res.error === 0) {
      const index = currentMessages.value.findIndex(m => m.id === tempMessage.id);
      if (index !== -1) {
        const newMessage = res.body;
        currentMessages.value[index] = { 
          ...newMessage, 
          status: 'sent' 
        };
      }
      
      // 更新会话列表中的最新消息
      const sessionIndex = sessions.value.findIndex(s => s.id === currentSession.value.id);
      if (sessionIndex !== -1) {
        sessions.value[sessionIndex].lastMessage = content;
        sessions.value[sessionIndex].lastMessageTime = Date.now();
        
        // 将当前会话移到顶部
        const currentSessionData = sessions.value.splice(sessionIndex, 1)[0];
        sessions.value.unshift(currentSessionData);
      }
    } else {
      // 更新消息状态为失败
      const index = currentMessages.value.findIndex(m => m.id === tempMessage.id);
      if (index !== -1) {
        currentMessages.value[index].status = 'failed';
      }
      
      ElMessage.error(res?.message || '发送消息失败');
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    
    // 更新消息状态为失败
    const index = currentMessages.value.findIndex(m => m.id === tempMessage.id);
    if (index !== -1) {
      currentMessages.value[index].status = 'failed';
    }
    
    ElMessage.error('发送消息失败，请稍后重试');
  }
};

// 触发图片上传
const triggerImageUpload = () => {
  if (imageInput.value) {
    imageInput.value.click();
  }
};

// 处理图片上传
const handleImageUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件');
    return;
  }
  
  // 检查文件大小
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB');
    return;
  }
  
  try {
    const res = await uploadChatFile(currentSession.value.id, file);
    
    if (res && res.error === 0) {
      // 发送图片消息
      const imageUrl = res.body.fileUrl;
      const msgRes = await sendChatMessage(currentSession.value.id, imageUrl, 'image');
      
      if (msgRes && msgRes.error === 0) {
        // 消息发送成功，刷新消息列表
        loadMessages();
      } else {
        ElMessage.error(msgRes?.message || '发送图片消息失败');
      }
    } else {
      ElMessage.error(res?.message || '上传图片失败');
    }
  } catch (error) {
    console.error('上传图片失败:', error);
    ElMessage.error('上传图片失败，请稍后重试');
  } finally {
    // 清空文件输入框，方便下次选择相同文件
    if (imageInput.value) {
      imageInput.value.value = '';
    }
  }
};

// 触发文件上传
const triggerFileUpload = () => {
  if (fileInput.value) {
    fileInput.value.click();
  }
};

// 处理文件上传
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  // 检查文件大小
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB');
    return;
  }
  
  try {
    const res = await uploadChatFile(currentSession.value.id, file);
    
    if (res && res.error === 0) {
      // 发送文件消息
      const fileData = res.body;
      const msgRes = await sendChatMessage(
        currentSession.value.id, 
        fileData.fileUrl, 
        'file', 
        { fileName: file.name }
      );
      
      if (msgRes && msgRes.error === 0) {
        // 消息发送成功，刷新消息列表
        loadMessages();
      } else {
        ElMessage.error(msgRes?.message || '发送文件消息失败');
      }
    } else {
      ElMessage.error(res?.message || '上传文件失败');
    }
  } catch (error) {
    console.error('上传文件失败:', error);
    ElMessage.error('上传文件失败，请稍后重试');
  } finally {
    // 清空文件输入框，方便下次选择相同文件
    if (fileInput.value) {
      fileInput.value.value = '';
    }
  }
};

// 关闭会话信息侧栏
const handleCloseSessionInfo = () => {
  showSessionInfo.value = false;
};

// 删除会话
const deleteSession = async (sessionId) => {
  try {
    // 实际开发时使用API调用
    // await deleteSessionById(sessionId);
    
    // 模拟删除成功
    setTimeout(() => {
      const index = sessions.value.findIndex(s => s.id === sessionId);
      if (index !== -1) {
        sessions.value.splice(index, 1);
      }
      
      if (currentSession.value && currentSession.value.id === sessionId) {
        currentSession.value = null;
        currentMessages.value = [];
      }
      
      showSessionInfo.value = false;
      ElMessage.success('会话已删除');
    }, 500);
  } catch (error) {
    console.error('删除会话失败:', error);
    ElMessage.error('删除会话失败，请稍后重试');
  }
};

// 举报会话
const reportSession = async (sessionId) => {
  try {
    // 实际开发时使用API调用
    // await reportSessionById(sessionId);
    
    // 模拟举报成功
    setTimeout(() => {
      showSessionInfo.value = false;
      ElMessage.success('举报已提交，我们会尽快处理');
    }, 500);
  } catch (error) {
    console.error('举报会话失败:', error);
    ElMessage.error('举报会话失败，请稍后重试');
  }
};

// 举报消息
const reportChatMsg = async (messageId, reason = '内容不当') => {
  try {
    const res = await reportChatMessage(messageId, reason);
    
    if (res && res.error === 0) {
      ElMessage.success('举报已提交，我们会尽快处理');
    } else {
      ElMessage.error(res?.message || '举报失败');
    }
  } catch (error) {
    console.error('举报消息失败:', error);
    ElMessage.error('举报失败，请稍后重试');
  }
};

// 监听路由变化
watch(() => route.params, (newParams) => {
  const { type, id } = newParams;
  if (type && id) {
    const session = sessions.value.find(s => s.type === type && s.id === parseInt(id));
    if (session) {
      selectSession(session);
    }
  }
}, { deep: true });

// 生命周期钩子
onMounted(() => {
  loadSessions();
});
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
  margin: 10px 0;
  font-size: 12px;
  color: #909399;
}

.message {
  display: flex;
  margin-bottom: 8px;
}

.message-self {
  justify-content: flex-end;
}

.message-other {
  justify-content: flex-start;
}

.message-avatar {
  margin: 0 8px;
}

.message-content {
  max-width: 70%;
  border-radius: 4px;
  overflow: hidden;
}

.message-text {
  padding: 10px 12px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message-self .message-text {
  background-color: #ecf5ff;
}

.message-image {
  width: 200px;
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
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.no-session {
  display: flex;
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
</style> 