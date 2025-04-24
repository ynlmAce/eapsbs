package com.bs.eaps.dto.counselor;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 辅导员任务DTO
 */
@Data
public class TaskDTO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务类型：companyCertification, jobAudit, reportHandling
     */
    private String type;

    /**
     * 目标项类型：Company, Job, Rating, ChatMessage, ChatSession
     */
    private String targetItemType;

    /**
     * 目标项ID
     */
    private Long targetItemId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 优先级：high, normal, low
     */
    private String priority;

    /**
     * 状态：pending, processing, completed
     */
    private String status;
}