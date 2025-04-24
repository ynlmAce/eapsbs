package com.jobhelper.dto.counselor;

import lombok.Data;

/**
 * 任务处理DTO
 */
@Data
public class TaskProcessDTO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 处理动作
     * approve - 通过
     * reject - 驳回
     * delete - 删除
     */
    private String action;

    /**
     * 处理理由
     */
    private String reason;

    /**
     * 备注
     * 可选
     */
    private String notes;
}