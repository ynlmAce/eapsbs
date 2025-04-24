package com.jobhelper.dto.chat;

import lombok.Data;

/**
 * 聊天会话创建DTO
 */
@Data
public class ChatCreateDTO {

    /**
     * 会话类型
     * SE - 学生-企业
     * SC - 学生-辅导员
     * SS - 学生群组
     */
    private String type;

    /**
     * 参与者ID
     * 对方用户ID，SS类型时为群组ID
     */
    private Long participantId;

    /**
     * 关联岗位ID
     * 仅SE类型需要
     */
    private Long relatedJobId;

    /**
     * 初始消息
     * 可选
     */
    private String initialMessage;
}