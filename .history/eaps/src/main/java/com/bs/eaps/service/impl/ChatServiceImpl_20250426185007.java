package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bs.eaps.dto.chat.ChatCreateDTO;
import com.bs.eaps.dto.chat.ChatMessageQueryDTO;
import com.bs.eaps.dto.chat.ChatSendDTO;
import com.bs.eaps.entity.ChatMessage;
import com.bs.eaps.entity.ChatSession;
import com.bs.eaps.mapper.ChatMessageMapper;
import com.bs.eaps.mapper.ChatSessionMapper;
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

            existingSession = chatSessionMapper.selectOne(queryWrapper);
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

            chatSessionMapper.insert(chatSession);
            log.info("创建新会话: sessionId={}", chatSession.getId());
        } else {
            // 已存在会话，更新最后活跃时间
            chatSession = existingSession;
            chatSession.setLastActiveAt(now);
            chatSessionMapper.updateById(chatSession);
            log.info("使用已存在会话: sessionId={}", chatSession.getId());
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

            chatMessageMapper.insert(message);
            messageId = message.getId();
            log.info("添加初始消息: messageId={}", messageId);
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
}