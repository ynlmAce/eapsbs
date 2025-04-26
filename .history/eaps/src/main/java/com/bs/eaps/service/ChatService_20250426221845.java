package com.bs.eaps.service;

import com.bs.eaps.dto.chat.ChatCreateDTO;
import com.bs.eaps.dto.chat.ChatMessageQueryDTO;
import com.bs.eaps.dto.chat.ChatSendDTO;
import com.bs.eaps.dto.chat.StudentCompanyChatDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 获取会话列表
     * 
     * @param userId 用户ID
     * @return 会话列表
     */
    Object getChatSessions(Long userId);

    /**
     * 获取会话消息
     * 
     * @param userId   用户ID
     * @param queryDTO 查询条件
     * @return 消息列表
     */
    Object getChatMessages(Long userId, ChatMessageQueryDTO queryDTO);

    /**
     * 发送聊天消息
     * 
     * @param userId  用户ID
     * @param sendDTO 发送信息
     * @return 发送结果
     */
    Object sendChatMessage(Long userId, ChatSendDTO sendDTO);

    /**
     * 上传聊天附件
     * 
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param file      附件文件
     * @return 上传结果
     */
    Object uploadChatFile(Long userId, Long sessionId, MultipartFile file);

    /**
     * 创建聊天会话
     * 
     * @param userId    用户ID
     * @param createDTO 创建信息
     * @return 创建结果
     */
    Object createChatSession(Long userId, ChatCreateDTO createDTO);

    /**
     * 创建学生-企业聊天会话
     * 专门处理学生与企业之间的聊天创建
     * 
     * @param studentUserId 学生用户ID
     * @param chatDTO       聊天创建信息
     * @return 创建结果
     */
    Object createStudentCompanyChat(Long studentUserId, StudentCompanyChatDTO chatDTO);
}