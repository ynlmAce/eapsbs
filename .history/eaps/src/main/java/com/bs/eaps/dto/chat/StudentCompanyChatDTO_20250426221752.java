package com.bs.eaps.dto.chat;

import lombok.Data;

/**
 * 学生-企业聊天DTO
 * 专用于学生创建与企业的聊天会话
 */
@Data
public class StudentCompanyChatDTO {
    /**
     * 企业档案ID
     */
    private Long companyId;

    /**
     * 相关岗位ID
     */
    private Long jobId;

    /**
     * 初始消息(可选)
     */
    private String initialMessage;
}
