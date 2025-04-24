package com.bs.eaps.dto.counselor;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 辅导员任务处理结果DTO
 */
@Data
public class TaskProcessResultDTO {

    /**
     * 是否处理成功
     */
    private Boolean success;

    /**
     * 操作ID
     */
    private Long operationId;

    /**
     * 处理时间
     */
    private LocalDateTime processedAt;
}