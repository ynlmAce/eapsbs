package com.bs.eaps.dto.counselor;

import lombok.Data;

/**
 * 辅导员待处理任务统计DTO
 */
@Data
public class TaskCountDTO {

    /**
     * 企业认证任务数量
     */
    private Integer companyCertification;

    /**
     * 岗位审核任务数量
     */
    private Integer jobAudit;

    /**
     * 举报处理任务数量
     */
    private Integer reportHandling;

    /**
     * 任务总数
     */
    private Integer total;
}