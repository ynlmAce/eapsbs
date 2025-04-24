import request from './axios'

// 获取聊天会话列表
export const getChatSessions = () => {
  return request({
    url: '/chat/sessions',
    method: 'get'
  })
}

// 获取特定会话的消息
export const getChatMessages = (sessionId, params) => {
  return request({
    url: `/chat/sessions/${sessionId}/messages`,
    method: 'get',
    params
  })
}

// 发送消息
export const sendChatMessage = (sessionId, data) => {
  return request({
    url: `/chat/sessions/${sessionId}/messages`,
    method: 'post',
    data
  })
}

// 上传聊天文件
export const uploadChatFile = (sessionId, file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: `/chat/sessions/${sessionId}/files`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
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

// 举报不当消息
export const reportChatMessage = (messageId, data) => {
  return request({
    url: `/reports/messages/${messageId}`,
    method: 'post',
    data
  })
} 