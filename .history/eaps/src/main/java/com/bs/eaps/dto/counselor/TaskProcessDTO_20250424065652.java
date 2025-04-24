package com.bs.eaps.dto.counselor;

import lombok.Data;

/**
 * 辅导员任务处理DTO
 */
@Data
public class TaskProcessDTO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 操作：approve, reject, delete
     */
    private String action;

    /**
     * 操作原因
     */
    private String reason;

    /**
     * 操作备注
     */
    private String notes;
}