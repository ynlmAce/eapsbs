package com.jobhelper.dto.counselor;

import lombok.Data;

/**
 * 任务列表查询DTO
 */
@Data
public class TaskListQueryDTO {

    /**
     * 任务类型
     * companyCertification - 企业认证
     * jobAudit - 岗位审核
     * reportHandling - 举报处理
     */
    private String type;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}