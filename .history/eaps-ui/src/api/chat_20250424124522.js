import request from '@/utils/request'

/**
 * 获取会话列表
 * @returns {Promise} 会话列表
 */
export function getChatSessions() {
  return request({
    url: '/api/chat/sessions',
    method: 'post'
  })
}

/**
 * 获取会话消息
 * @param {Number} sessionId 会话ID
 * @param {Number} page 页码
 * @param {Number} pageSize 每页大小
 * @returns {Promise} 消息列表
 */
export function getChatMessages(sessionId, page = 1, pageSize = 20) {
  return request({
    url: '/api/chat/messages',
    method: 'post',
    data: {
      sessionId,
      page,
      pageSize
    }
  })
}

/**
 * 发送消息
 * @param {Number} sessionId 会话ID
 * @param {String} content 消息内容
 * @param {String} contentType 内容类型
 * @returns {Promise} 发送结果
 */
export function sendChatMessage(sessionId, content, contentType = 'text') {
  return request({
    url: '/api/chat/send',
    method: 'post',
    data: {
      sessionId,
      content,
      contentType
    }
  })
}

/**
 * 上传聊天附件
 * @param {FormData} formData 表单数据
 * @returns {Promise} 上传结果
 */
export function uploadChatFile(sessionId, file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('sessionId', sessionId)
  
  return request({
    url: '/api/chat/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 创建聊天会话
 * @param {String} type 会话类型
 * @param {Number} participantId 参与者ID
 * @param {Number} relatedJobId 关联岗位ID
 * @param {String} initialMessage 初始消息
 * @returns {Promise} 创建结果
 */
export function createChatSession(type, participantId, relatedJobId, initialMessage) {
  return request({
    url: '/api/chat/create',
    method: 'post',
    data: {
      type,
      participantId,
      relatedJobId,
      initialMessage
    }
  })
}

/**
 * 举报消息
 * @param {Number} messageId 消息ID
 * @param {String} reason 举报原因
 * @returns {Promise} 举报结果
 */
export function reportMessage(messageId, reason) {
  return request({
    url: '/api/report/submit',
    method: 'post',
    data: {
      reportedItemType: 'message',
      reportedItemId: messageId,
      reason
    }
  })
}

// 创建学生-企业会话
export const createStudentCompanySession = (data) => {
  return request({
    url: '/chat/sessions/student-company',
    method: 'post',
    data
  })
}

// 创建学生-辅导员会话
export const createStudentCounselorSession = (data) => {
  return request({
    url: '/chat/sessions/student-counselor',
    method: 'post',
    data
  })
}

// 创建学生群组
export const createStudentGroup = (data) => {
  return request({
    url: '/chat/groups',
    method: 'post',
    data
  })
}

// 加入学生群组
export const joinStudentGroup = (groupId) => {
  return request({
    url: `/chat/groups/${groupId}/members`,
    method: 'post'
  })
}

// 获取未读消息数
export const getUnreadCount = () => {
  return request({
    url: '/chat/messages/unread/count',
    method: 'get'
  })
}

// 标记消息为已读
export const markMessageRead = (sessionId) => {
  return request({
    url: `/chat/sessions/${sessionId}/read`,
    method: 'put'
  })
} 