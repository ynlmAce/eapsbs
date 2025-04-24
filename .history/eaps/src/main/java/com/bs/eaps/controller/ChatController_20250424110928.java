package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.dto.chat.ChatCreateDTO;
import com.bs.eaps.dto.chat.ChatMessageQueryDTO;
import com.bs.eaps.dto.chat.ChatSendDTO;
import com.bs.eaps.service.ChatService;
import com.bs.eaps.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 聊天模块控制器
 */
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
        return ApiResponse.success(chatService.createChatSession(userId, createDTO));
    }
}