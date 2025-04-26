import request from '@/utils/request'
import { handleResponse } from '@/utils/api'

/**
 * 获取聊天会话列表
 * @param {Object} params - 查询参数
 * @param {string} [params.type] - 会话类型（SE: 学生-企业, SC: 学生-辅导员, SS: 学生群组）
 * @param {number} [params.relatedJobId] - 关联岗位ID（学生-企业会话）
 * @param {number} [params.groupId] - 关联群组ID（学生群组会话）
 * @param {number} [params.participantId] - 会话参与者ID
 * @returns {Promise} - 返回会话列表
 */
export function getChatSessions(params = {}) {
  return request({
    url: '/chat/sessions',
    method: 'post',
    data: params || {}
  }).then(handleResponse)
}

/**
 * 获取指定会话的聊天消息
 * @param {number} sessionId - 会话ID
 * @param {Object} params - 查询参数
 * @param {number} [params.lastMessageId] - 上次加载的最后一条消息ID（用于加载更多）
 * @param {number} [params.limit] - 每次加载的消息数量
 * @returns {Promise} - 返回消息列表
 */
export function getChatMessages(sessionId, params = {}) {
  return request({
    url: `/chat/messages/${sessionId}`,
    method: 'post',
    data: params || {}
  }).then(handleResponse)
}

/**
 * 发送聊天消息
 * @param {number} sessionId - 会话ID
 * @param {Object} messageData - 消息数据
 * @param {string} messageData.content - 消息内容
 * @param {string} messageData.contentType - 消息类型（text, image, file）
 * @returns {Promise} - 返回发送结果
 */
export function sendChatMessage(sessionId, messageData) {
  return request({
    url: `/chat/message/send/${sessionId}`,
    method: 'post',
    data: messageData || {}
  }).then(handleResponse)
}

/**
 * 上传聊天文件（图片或文档）
 * @param {number} sessionId - 会话ID
 * @param {File} file - 要上传的文件
 * @param {string} contentType - 内容类型（image 或 file）
 * @returns {Promise} - 返回上传结果，包含文件路径
 */
export function uploadChatFile(sessionId, file, contentType = 'file') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('contentType', contentType)
  
  return request({
    url: `/chat/file/upload/${sessionId}`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(handleResponse)
}

/**
 * 创建新的聊天会话
 * @param {string} type - 会话类型（SE: 学生-企业, SC: 学生-辅导员, SS: 学生群组）
 * @param {number} participantId - 目标用户ID（对方ID）
 * @param {number} [relatedJobId] - 关联岗位ID（学生-企业会话）
 * @param {string} [initialMessage] - 初始消息内容
 * @returns {Promise} - 返回创建的会话信息
 */
export function createChatSession(type, participantId, relatedJobId, initialMessage) {
  // 直接调试所有参数
  console.log('创建会话传入参数:', {
    type, 
    participantId, 
    relatedJobId, 
    initialMessage
  });
  
  // 根据传入的参数构建会话数据对象
  const sessionData = {
    type: type,
    participantId: Number(participantId)  // 确保为数字类型
  }
  
  // 确保岗位ID是数字类型
  if (relatedJobId) {
    sessionData.relatedJobId = Number(relatedJobId)
  }
  
  // 添加初始消息(如果有)
  if (initialMessage && initialMessage.trim() !== '') {
    sessionData.initialMessage = initialMessage
  }
  
  console.log('创建聊天会话请求数据:', sessionData)
  
  return request({
    url: '/api/chat/create',
    method: 'post',
    data: sessionData
  }).then(handleResponse)
}

/**
 * 更新聊天会话状态
 * @param {number} sessionId - 会话ID
 * @param {Object} statusData - 状态数据
 * @param {boolean} [statusData.isRead] - 是否已读
 * @param {string} [statusData.status] - 会话状态
 * @returns {Promise} - 返回更新结果
 */
export function updateChatSessionStatus(sessionId, statusData) {
  return request({
    url: `/chat/session/status/${sessionId}`,
    method: 'post',
    data: statusData || {}
  }).then(handleResponse)
}

/**
 * 删除聊天消息
 * @param {number} messageId - 消息ID
 * @returns {Promise} - 返回删除结果
 */
export function deleteChatMessage(messageId) {
  return request({
    url: `/chat/message/delete/${messageId}`,
    method: 'post',
    data: {}
  }).then(handleResponse)
}

/**
 * 举报聊天消息
 * @param {number} messageId - 消息ID
 * @param {Object} reportData - 举报数据
 * @param {string} reportData.reason - 举报原因
 * @returns {Promise} - 返回举报结果
 */
export function reportChatMessage(messageId, reportData) {
  return request({
    url: `/chat/message/report/${messageId}`,
    method: 'post',
    data: reportData || {}
  }).then(handleResponse)
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