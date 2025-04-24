package com.jobhelper.dto.counselor;

import lombok.Data;

/**
 * 操作历史查询DTO
 */
@Data
public class OperationHistoryQueryDTO {

    /**
     * 开始日期
     * 格式：yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期
     * 格式：yyyy-MM-dd
     */
    private String endDate;

    /**
     * 类型
     * all - 全部
     * companyCertification - 企业认证
     * jobAudit - 岗位审核
     * reportHandling - 举报处理
     */
    private String type = "all";

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}