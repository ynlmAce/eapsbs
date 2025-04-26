package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public Object getChatSessions(Long userId) {
        // TODO: 实现获取会话列表逻辑
        log.info("获取会话列表: userId={}", userId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", java.util.Collections.emptyList());
        return result;
    }

    @Override
    public Object getChatMessages(Long userId, ChatMessageQueryDTO queryDTO) {
        // TODO: 实现获取会话消息逻辑
        log.info("获取会话消息: userId={}, queryDTO={}", userId, queryDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("list", java.util.Collections.emptyList());
        return result;
    }

    @Override
    @Transactional
    public Object sendChatMessage(Long userId, ChatSendDTO sendDTO) {
        // TODO: 实现发送消息逻辑
        log.info("发送消息: userId={}, sendDTO={}", userId, sendDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("messageId", 0);
        result.put("sentAt", LocalDateTime.now());
        return result;
    }

    @Override
    @Transactional
    public Object uploadChatFile(Long userId, Long sessionId, MultipartFile file) {
        // TODO: 实现上传附件逻辑
        log.info("上传附件: userId={}, sessionId={}, fileName={}", userId, sessionId, file.getOriginalFilename());
        Map<String, Object> result = new HashMap<>();
        result.put("fileId", 0);
        result.put("fileName", file.getOriginalFilename());
        result.put("filePath", "uploads/chat/temp.file");
        result.put("contentType", file.getContentType().startsWith("image/") ? "image" : "file");
        result.put("uploadedAt", LocalDateTime.now());
        return result;
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
}