package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bs.eaps.dto.chat.ChatCreateDTO;
import com.bs.eaps.dto.chat.ChatMessageQueryDTO;
import com.bs.eaps.dto.chat.ChatSendDTO;
import com.bs.eaps.dto.chat.StudentCompanyChatDTO;
import com.bs.eaps.entity.ChatMessage;
import com.bs.eaps.entity.ChatSession;
import com.bs.eaps.mapper.ChatMessageMapper;
import com.bs.eaps.mapper.ChatSessionMapper;
import com.bs.eaps.mapper.CompanyProfileMapper;
import com.bs.eaps.service.ChatService;
import com.bs.eaps.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.util.Random;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final CompanyProfileMapper companyProfileMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Object getChatSessions(Long userId) {
        log.info("获取会话列表: userId={}", userId);

        // 参数校验
        if (userId == null) {
            log.error("获取会话列表失败: userId为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }

        try {
            // 查询用户参与的所有会话
            LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatSession::getStatus, "active"); // 只查询活跃的会话

            // 用户可能是会话的参与者1或参与者2
            queryWrapper.and(wrapper -> wrapper
                    .eq(ChatSession::getParticipant1Id, userId)
                    .or()
                    .eq(ChatSession::getParticipant2Id, userId));

            // 按最后活跃时间降序排序
            queryWrapper.orderByDesc(ChatSession::getLastActiveAt);

            // 查询会话列表
            List<ChatSession> sessionList = chatSessionMapper.selectList(queryWrapper);
            log.info("查询到会话数量: {}", sessionList.size());

            // 转换为前端需要的格式
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (ChatSession session : sessionList) {
                Map<String, Object> sessionMap = new HashMap<>();
                sessionMap.put("id", session.getId());
                sessionMap.put("type", session.getType());

                // 确定会话类型和对方ID
                Long otherParticipantId;
                if (userId.equals(session.getParticipant1Id())) {
                    otherParticipantId = session.getParticipant2Id();
                } else {
                    otherParticipantId = session.getParticipant1Id();
                }

                // 根据会话类型设置额外信息
                if ("SE".equals(session.getType())) {
                    sessionMap.put("relatedJobId", session.getRelatedJobId());
                    // TODO: 可以查询企业名称等信息添加到结果中
                    sessionMap.put("name", "企业用户"); // 后续可以替换为实际企业名
                } else if ("SC".equals(session.getType())) {
                    // TODO: 可以查询辅导员名称等信息添加到结果中
                    sessionMap.put("name", "辅导员"); // 后续可以替换为实际辅导员名
                } else if ("SS".equals(session.getType())) {
                    sessionMap.put("groupId", session.getGroupId());
                    // TODO: 可以查询群组名称等信息添加到结果中
                    sessionMap.put("name", "学生群组"); // 后续可以替换为实际群组名
                }

                // 查询最新消息
                LambdaQueryWrapper<ChatMessage> messageQuery = new LambdaQueryWrapper<>();
                messageQuery.eq(ChatMessage::getSessionId, session.getId());
                messageQuery.orderByDesc(ChatMessage::getSentAt);
                messageQuery.last("LIMIT 1");

                ChatMessage lastMessage = chatMessageMapper.selectOne(messageQuery);
                if (lastMessage != null) {
                    sessionMap.put("lastMessage", lastMessage.getContent());
                    sessionMap.put("lastMessageTime", lastMessage.getSentAt());
                } else {
                    sessionMap.put("lastMessage", "");
                    sessionMap.put("lastMessageTime", session.getCreatedAt());
                }

                // 查询未读消息数(仅统计对方发送的消息)
                LambdaQueryWrapper<ChatMessage> unreadQuery = new LambdaQueryWrapper<>();
                unreadQuery.eq(ChatMessage::getSessionId, session.getId());
                unreadQuery.eq(ChatMessage::getIsRead, false); // 直接使用字段名
                unreadQuery.ne(ChatMessage::getSenderId, userId); // 不是自己发的

                Long unreadCount = chatMessageMapper.selectCount(unreadQuery);
                sessionMap.put("unreadCount", unreadCount.intValue());

                // 添加到结果列表
                resultList.add(sessionMap);
            }

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", resultList);

            return result;
        } catch (Exception e) {
            log.error("获取会话列表异常", e);
            throw new RuntimeException("获取会话列表失败，请稍后再试");
        }
    }

    @Override
    public Object getChatMessages(Long userId, ChatMessageQueryDTO queryDTO) {
        log.info("获取会话消息: userId={}, queryDTO={}", userId, queryDTO);

        // 参数校验
        if (userId == null) {
            log.error("获取会话消息失败: userId为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (queryDTO == null || queryDTO.getSessionId() == null) {
            log.error("获取会话消息失败: 会话ID为空");
            throw new IllegalArgumentException("会话ID不能为空");
        }

        Long sessionId = queryDTO.getSessionId();

        try {
            // 1. 验证用户是否有权限访问该会话
            ChatSession chatSession = chatSessionMapper.selectById(sessionId);
            if (chatSession == null) {
                log.error("获取会话消息失败: 会话不存在, sessionId={}", sessionId);
                throw new IllegalArgumentException("会话不存在");
            }

            // 检查用户是否为会话参与者
            if (!userId.equals(chatSession.getParticipant1Id()) && !userId.equals(chatSession.getParticipant2Id())) {
                log.error("获取会话消息失败: 用户无权访问该会话, userId={}, sessionId={}", userId, sessionId);
                throw new IllegalArgumentException("无权访问该会话");
            }

            // 2. 构建查询条件
            LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatMessage::getSessionId, sessionId);

            // 支持分页
            Integer page = queryDTO.getPage() == null ? 1 : queryDTO.getPage();
            Integer pageSize = queryDTO.getPageSize() == null ? 20 : queryDTO.getPageSize();

            // 如果提供了before参数(上一条消息ID)，只查询ID小于before的消息
            if (queryDTO.getBefore() != null) {
                queryWrapper.lt(ChatMessage::getId, queryDTO.getBefore());
            }

            // 按时间降序排序，查询最近的消息
            queryWrapper.orderByDesc(ChatMessage::getSentAt);

            // 计算总消息数
            Long total = chatMessageMapper.selectCount(queryWrapper);

            // 计算分页偏移量
            int offset = (page - 1) * pageSize;
            queryWrapper.last("LIMIT " + offset + "," + pageSize);

            // 执行查询
            List<ChatMessage> messages = chatMessageMapper.selectList(queryWrapper);

            // 反转消息列表，使其按时间正序排列
            Collections.reverse(messages);

            // 3. 构建返回结果
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (ChatMessage message : messages) {
                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("id", message.getId());
                messageMap.put("sessionId", message.getSessionId());
                messageMap.put("senderId", message.getSenderId());
                messageMap.put("content", message.getContent());
                messageMap.put("contentType", message.getContentType());
                messageMap.put("filePath", message.getFilePath());
                messageMap.put("sentAt", message.getSentAt());

                // 判断发送者类型，可以进一步查询发送者姓名等信息
                if (message.getSenderId().equals(userId)) {
                    messageMap.put("senderType", "self");
                } else {
                    // 判断对方角色，可以从会话类型推断
                    if ("SE".equals(chatSession.getType())) {
                        messageMap.put("senderType", "company");
                    } else if ("SC".equals(chatSession.getType())) {
                        messageMap.put("senderType", "counselor");
                    } else {
                        messageMap.put("senderType", "student");
                    }
                }

                resultList.add(messageMap);
            }

            // 4. 标记消息为已读（仅对方发送的消息）
            LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ChatMessage::getSessionId, sessionId);
            updateWrapper.ne(ChatMessage::getSenderId, userId); // 不是自己发送的消息
            updateWrapper.set(ChatMessage::getIsRead, true); // 标记为已读

            chatMessageMapper.update(null, updateWrapper);

            // 5. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", resultList);
            result.put("hasMore", offset + messages.size() < total);

            return result;
        } catch (IllegalArgumentException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            log.error("获取会话消息异常", e);
            throw new RuntimeException("获取会话消息失败，请稍后再试");
        }
    }

    @Override
    @Transactional
    public Object sendChatMessage(Long userId, ChatSendDTO sendDTO) {
        log.info("发送消息: userId={}, sendDTO={}", userId, sendDTO);

        // 参数校验
        if (userId == null) {
            log.error("发送消息失败: userId为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (sendDTO == null) {
            log.error("发送消息失败: sendDTO为空");
            throw new IllegalArgumentException("消息内容不能为空");
        }

        if (sendDTO.getSessionId() == null) {
            log.error("发送消息失败: 会话ID为空");
            throw new IllegalArgumentException("会话ID不能为空");
        }

        if (sendDTO.getContent() == null || sendDTO.getContent().trim().isEmpty()) {
            log.error("发送消息失败: 消息内容为空");
            throw new IllegalArgumentException("消息内容不能为空");
        }

        // 设置默认内容类型
        String contentType = sendDTO.getContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            contentType = "text"; // 默认为文本类型
        }

        Long sessionId = sendDTO.getSessionId();

        try {
            // 1. 验证用户是否有权限发送消息到该会话
            ChatSession chatSession = chatSessionMapper.selectById(sessionId);
            if (chatSession == null) {
                log.error("发送消息失败: 会话不存在, sessionId={}", sessionId);
                throw new IllegalArgumentException("会话不存在");
            }

            // 检查用户是否为会话参与者
            if (!userId.equals(chatSession.getParticipant1Id()) && !userId.equals(chatSession.getParticipant2Id())) {
                log.error("发送消息失败: 用户无权访问该会话, userId={}, sessionId={}", userId, sessionId);
                throw new IllegalArgumentException("无权访问该会话");
            }

            // 检查会话是否为只读
            if (chatSession.getIsReadonly() != null && chatSession.getIsReadonly()) {
                log.error("发送消息失败: 会话为只读状态, sessionId={}", sessionId);
                throw new IllegalArgumentException("会话为只读状态，无法发送消息");
            }

            // 2. 创建并保存消息
            LocalDateTime now = LocalDateTime.now();
            ChatMessage message = new ChatMessage();
            message.setSessionId(sessionId);
            message.setSenderId(userId);
            message.setContent(sendDTO.getContent());
            message.setContentType(contentType);

            // 如果是文件类型消息，设置文件路径
            if ("file".equals(contentType) || "image".equals(contentType)) {
                message.setFilePath(sendDTO.getContent()); // 对于文件/图片消息，content中存储的是文件路径
            }

            message.setSentAt(now);
            message.setIsRead(false); // 新消息默认未读

            chatMessageMapper.insert(message);
            Long messageId = message.getId();
            log.info("消息保存成功: messageId={}", messageId);

            // 3. 更新会话的最后活跃时间
            chatSession.setLastActiveAt(now);
            chatSessionMapper.updateById(chatSession);

            // 4. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("messageId", messageId);
            result.put("sessionId", sessionId);
            result.put("senderId", userId);
            result.put("contentType", contentType);
            result.put("content", sendDTO.getContent());
            result.put("sentAt", now);

            // 5. 发布事件
            eventPublisher.publishEvent(new com.bs.eaps.websocket.ChatMessageSentEvent(this, sessionId, result));

            return result;
        } catch (IllegalArgumentException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            log.error("发送消息异常", e);
            throw new RuntimeException("发送消息失败，请稍后再试");
        }
    }

    @Override
    @Transactional
    public Object uploadChatFile(Long userId, Long sessionId, MultipartFile file) {
        log.info("上传附件: userId={}, sessionId={}, fileName={}", userId, sessionId, file.getOriginalFilename());

        // 参数校验
        if (userId == null) {
            log.error("上传附件失败: userId为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (sessionId == null) {
            log.error("上传附件失败: 会话ID为空");
            throw new IllegalArgumentException("会话ID不能为空");
        }

        if (file == null || file.isEmpty()) {
            log.error("上传附件失败: 文件为空");
            throw new IllegalArgumentException("文件不能为空");
        }

        try {
            // 1. 验证用户是否有权限上传文件到该会话
            ChatSession chatSession = chatSessionMapper.selectById(sessionId);
            if (chatSession == null) {
                log.error("上传附件失败: 会话不存在, sessionId={}", sessionId);
                throw new IllegalArgumentException("会话不存在");
            }

            // 检查用户是否为会话参与者
            if (!userId.equals(chatSession.getParticipant1Id()) && !userId.equals(chatSession.getParticipant2Id())) {
                log.error("上传附件失败: 用户无权访问该会话, userId={}, sessionId={}", userId, sessionId);
                throw new IllegalArgumentException("无权访问该会话");
            }

            // 检查会话是否为只读
            if (chatSession.getIsReadonly() != null && chatSession.getIsReadonly()) {
                log.error("上传附件失败: 会话为只读状态, sessionId={}", sessionId);
                throw new IllegalArgumentException("会话为只读状态，无法上传文件");
            }

            // 2. 处理文件上传
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成文件名: 时间戳_用户ID_随机数.扩展名
            String newFileName = System.currentTimeMillis() + "_" + userId + "_"
                    + new Random().nextInt(10000) + fileExtension;

            // 确定文件存储路径
            String uploadDir = "uploads/chat/";
            String contentType = file.getContentType();
            boolean isImage = contentType != null && contentType.startsWith("image/");

            if (isImage) {
                uploadDir += "images/";
            } else {
                uploadDir += "files/";
            }

            // 确保目录存在
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 构建完整的文件路径
            String filePath = uploadDir + newFileName;
            File destFile = new File(filePath);

            // 保存文件
            file.transferTo(destFile);
            log.info("文件上传成功: path={}", filePath);

            // 更新会话的最后活跃时间
            LocalDateTime now = LocalDateTime.now();
            chatSession.setLastActiveAt(now);
            chatSessionMapper.updateById(chatSession);

            // 3. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("fileId", System.currentTimeMillis()); // 模拟文件ID
            result.put("fileName", originalFilename);
            result.put("filePath", filePath);
            result.put("fileUrl", "/api/files/" + filePath); // 构建访问URL
            result.put("fileSize", file.getSize());
            result.put("contentType", isImage ? "image" : "file");
            result.put("uploadedAt", now);

            return result;
        } catch (IllegalArgumentException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            log.error("上传附件异常", e);
            throw new RuntimeException("上传附件失败，请稍后再试");
        }
    }

    @Override
    @Transactional
    public Object createChatSession(Long userId, ChatCreateDTO createDTO) {
        log.info("创建会话: userId={}, createDTO={}", userId, createDTO);

        // 添加参数校验
        if (userId == null) {
            log.error("创建会话失败: userId为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (createDTO == null) {
            log.error("创建会话失败: createDTO为空");
            throw new IllegalArgumentException("会话创建信息不能为空");
        }

        if (createDTO.getType() == null || createDTO.getType().isEmpty()) {
            log.error("创建会话失败: 会话类型为空");
            throw new IllegalArgumentException("会话类型不能为空");
        }

        if (createDTO.getParticipantId() == null) {
            log.error("创建会话失败: 参与者ID为空");
            throw new IllegalArgumentException("参与者ID不能为空");
        }

        if ("SE".equals(createDTO.getType()) && createDTO.getRelatedJobId() == null) {
            log.error("创建会话失败: 学生-企业会话缺少岗位ID");
            throw new IllegalArgumentException("学生-企业会话必须提供岗位ID");
        }

        // 1. 检查是否已存在相同的会话
        ChatSession existingSession = null;
        String sessionType = createDTO.getType();

        if ("SE".equals(sessionType) || "SC".equals(sessionType)) {
            // 对于学生-企业或学生-辅导员的会话，检查是否已存在
            LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatSession::getType, sessionType);

            if ("SE".equals(sessionType) && createDTO.getRelatedJobId() != null) {
                // 如果是学生-企业会话，且提供了岗位ID，则按岗位ID查找
                queryWrapper.eq(ChatSession::getRelatedJobId, createDTO.getRelatedJobId());
            }

            // 查找会话的双方（不考虑顺序）
            queryWrapper.and(wrapper -> wrapper.and(w -> w.eq(ChatSession::getParticipant1Id, userId)
                    .eq(ChatSession::getParticipant2Id, createDTO.getParticipantId()))
                    .or(w -> w.eq(ChatSession::getParticipant1Id, createDTO.getParticipantId())
                            .eq(ChatSession::getParticipant2Id, userId)));

            try {
                existingSession = chatSessionMapper.selectOne(queryWrapper);
            } catch (Exception e) {
                log.error("查询会话异常", e);
                throw new RuntimeException("系统错误，请稍后再试");
            }
        }

        // 2. 如果不存在，则创建新会话
        ChatSession chatSession;
        LocalDateTime now = LocalDateTime.now();

        if (existingSession == null) {
            chatSession = new ChatSession();
            chatSession.setType(sessionType);
            chatSession.setParticipant1Id(userId);
            chatSession.setParticipant2Id(createDTO.getParticipantId());

            if ("SE".equals(sessionType) && createDTO.getRelatedJobId() != null) {
                chatSession.setRelatedJobId(createDTO.getRelatedJobId());
            } else if ("SS".equals(sessionType)) {
                // 对于学生群组会话，participantId即为群组ID
                chatSession.setGroupId(createDTO.getParticipantId());
            }

            chatSession.setStatus("active");
            chatSession.setIsReadonly(false);
            chatSession.setCreatedAt(now);
            chatSession.setLastActiveAt(now);

            try {
                chatSessionMapper.insert(chatSession);
                log.info("创建新会话: sessionId={}", chatSession.getId());
            } catch (Exception e) {
                log.error("创建会话异常", e);
                throw new RuntimeException("创建会话失败，请稍后再试");
            }
        } else {
            // 已存在会话，更新最后活跃时间
            chatSession = existingSession;
            chatSession.setLastActiveAt(now);
            try {
                chatSessionMapper.updateById(chatSession);
                log.info("使用已存在会话: sessionId={}", chatSession.getId());
            } catch (Exception e) {
                log.error("更新会话异常", e);
                throw new RuntimeException("更新会话失败，请稍后再试");
            }
        }

        // 3. 如果有初始消息，添加消息记录
        Long messageId = null;
        if (createDTO.getInitialMessage() != null && !createDTO.getInitialMessage().isEmpty()) {
            ChatMessage message = new ChatMessage();
            message.setSessionId(chatSession.getId());
            message.setSenderId(userId);
            message.setContent(createDTO.getInitialMessage());
            message.setContentType("text");
            message.setSentAt(now);

            try {
                chatMessageMapper.insert(message);
                messageId = message.getId();
                log.info("添加初始消息: messageId={}", messageId);
            } catch (Exception e) {
                log.error("添加初始消息异常", e);
                // 消息添加失败不影响会话创建，仅记录错误
            }
        }

        // 4. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", chatSession.getId());
        result.put("createdAt", chatSession.getCreatedAt());
        if (messageId != null) {
            result.put("messageId", messageId);
        }

        return result;
    }

    /**
     * 创建学生-企业聊天会话
     * 专门处理学生与企业之间的聊天创建
     * 
     * @param studentUserId 学生用户ID
     * @param chatDTO       聊天创建信息
     * @return 创建结果
     */
    @Override
    @Transactional
    public Object createStudentCompanyChat(Long studentUserId, StudentCompanyChatDTO chatDTO) {
        log.info("创建学生-企业会话: studentUserId={}, companyId={}, jobId={}",
                studentUserId, chatDTO.getCompanyId(), chatDTO.getJobId());

        // 参数校验
        if (studentUserId == null) {
            log.error("创建会话失败: 学生用户ID为空");
            throw new IllegalArgumentException("学生用户ID不能为空");
        }

        if (chatDTO == null) {
            log.error("创建会话失败: 聊天DTO为空");
            throw new IllegalArgumentException("聊天创建信息不能为空");
        }

        if (chatDTO.getCompanyId() == null) {
            log.error("创建会话失败: 企业ID为空");
            throw new IllegalArgumentException("企业ID不能为空");
        }

        if (chatDTO.getJobId() == null) {
            log.error("创建会话失败: 岗位ID为空");
            throw new IllegalArgumentException("岗位ID不能为空");
        }

        try {
            // 1. 通过企业档案ID查询企业用户ID
            Long companyUserId = companyProfileMapper.selectUserIdByCompanyId(chatDTO.getCompanyId());

            if (companyUserId == null) {
                log.error("创建会话失败: 未找到企业用户ID");
                throw new IllegalArgumentException("未找到对应的企业用户");
            }

            log.info("查询到企业用户ID: companyId={}, companyUserId={}", chatDTO.getCompanyId(), companyUserId);

            // 2. 创建ChatCreateDTO并设置参数
            ChatCreateDTO createDTO = new ChatCreateDTO();
            createDTO.setType("SE");
            createDTO.setParticipantId(companyUserId);
            createDTO.setRelatedJobId(chatDTO.getJobId());
            createDTO.setInitialMessage(chatDTO.getInitialMessage());

            // 3. 调用现有方法创建会话
            return createChatSession(studentUserId, createDTO);
        } catch (Exception e) {
            log.error("创建学生-企业会话异常", e);
            throw new RuntimeException("创建聊天会话失败，请稍后再试");
        }
    }

    @Override
    public List<Long> getParticipantIdsBySessionId(Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        List<Long> ids = new ArrayList<>();
        if (session != null) {
            if (session.getParticipant1Id() != null)
                ids.add(session.getParticipant1Id());
            if (session.getParticipant2Id() != null
                    && !session.getParticipant2Id().equals(session.getParticipant1Id())) {
                ids.add(session.getParticipant2Id());
            }
        }
        return ids;
    }
}