package com.bs.eaps.dto.counselor;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 辅导员操作历史DTO
 */
@Data
public class OperationHistoryDTO {

    /**
     * 操作ID
     */
    private Long id;

    /**
     * 操作类型：companyCertification, jobAudit, reportHandling
     */
    private String operationType;

    /**
     * 目标项类型：Company, Job, Rating, ChatMessage, ChatSession
     */
    private String targetItemType;

    /**
     * 目标项ID
     */
    private Long targetItemId;

    /**
     * 目标名称
     */
    private String targetName;

    /**
     * 操作：approve, reject, delete
     */
    private String action;

    /**
     * 操作原因
     */
    private String reason;

    /**
     * 操作时间
     */
    private LocalDateTime timestamp;
}