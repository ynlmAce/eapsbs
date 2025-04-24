package com.jobhelper.dto.chat;

import lombok.Data;

/**
 * 聊天消息查询DTO
 */
@Data
public class ChatMessageQueryDTO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 20;
}