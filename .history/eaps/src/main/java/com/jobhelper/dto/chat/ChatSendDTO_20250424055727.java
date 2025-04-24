package com.jobhelper.dto.chat;

import lombok.Data;

/**
 * 聊天发送DTO
 */
@Data
public class ChatSendDTO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 内容
     */
    private String content;

    /**
     * 内容类型
     * text - 文本
     * image - 图片
     * file - 文件
     */
    private String contentType = "text";
}