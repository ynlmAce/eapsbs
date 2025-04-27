package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.dto.chat.ChatCreateDTO;
import com.bs.eaps.dto.chat.ChatMessageQueryDTO;
import com.bs.eaps.dto.chat.ChatSendDTO;
import com.bs.eaps.dto.chat.StudentCompanyChatDTO;
import com.bs.eaps.service.ChatService;
import com.bs.eaps.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 聊天模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取会话列表
     */
    @PostMapping("/sessions")
    public ApiResponse getChatSessions() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(chatService.getChatSessions(userId));
    }

    /**
     * 获取会话消息
     */
    @PostMapping("/messages")
    public ApiResponse getChatMessages(@RequestBody ChatMessageQueryDTO queryDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(chatService.getChatMessages(userId, queryDTO));
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ApiResponse sendChatMessage(@RequestBody ChatSendDTO sendDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(chatService.sendChatMessage(userId, sendDTO));
    }

    /**
     * 上传聊天附件
     */
    @PostMapping("/upload")
    public ApiResponse uploadChatFile(MultipartFile file, Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(chatService.uploadChatFile(userId, sessionId, file));
    }

    /**
     * 创建聊天会话
     */
    @PostMapping("/create")
    public ApiResponse createChatSession(@RequestBody ChatCreateDTO createDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("创建会话请求，用户ID: {}, 数据: {}", userId, createDTO);
        return ApiResponse.success(chatService.createChatSession(userId, createDTO));
    }

    /**
     * 标记会话消息为已读
     */
    @PostMapping("/read")
    public ApiResponse markSessionRead(@RequestBody Map<String, Object> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("标记会话消息已读请求，用户ID: {}, 数据: {}", userId, request);

        // 获取会话ID
        Long sessionId = null;
        if (request.containsKey("sessionId")) {
            if (request.get("sessionId") instanceof Number) {
                sessionId = ((Number) request.get("sessionId")).longValue();
            } else if (request.get("sessionId") instanceof String) {
                try {
                    sessionId = Long.parseLong((String) request.get("sessionId"));
                } catch (NumberFormatException e) {
                    log.error("解析会话ID失败", e);
                    return ApiResponse.businessError("无效的会话ID");
                }
            }
        }

        if (sessionId == null) {
            return ApiResponse.businessError("缺少会话ID");
        }

        try {
            // 调用服务层方法标记消息为已读
            ChatMessageQueryDTO queryDTO = new ChatMessageQueryDTO();
            queryDTO.setSessionId(sessionId);
            // 复用获取消息的方法，因为它已经包含了标记消息已读的逻辑
            chatService.getChatMessages(userId, queryDTO);
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("标记会话消息已读失败: sessionId={}", sessionId, e);
            return ApiResponse.error(500, "标记会话消息已读失败，请稍后重试");
        }
    }

    /**
     * 创建学生-企业聊天会话
     * 专用接口，方便学生端发起与企业的聊天
     */
    @PostMapping("/sessions/student-company")
    public ApiResponse createStudentCompanyChat(@RequestBody StudentCompanyChatDTO chatDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("创建学生-企业会话请求，学生ID: {}, 数据: {}", userId, chatDTO);
        return ApiResponse.success(chatService.createStudentCompanyChat(userId, chatDTO));
    }
}