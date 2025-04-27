package com.bs.eaps.dto.counselor;

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

    /**
     * 是否包含所有状态
     * true - 包括所有状态的任务
     * false - 只包括pending状态的任务
     */
    private boolean includeAll = true;

    /**
     * 状态筛选
     * all - 全部状态
     * pending - 待审核/待认证
     * active/approved/certified - 已通过/已认证
     * rejected - 已驳回
     * closed/expired - 已结束/已过期
     */
    private String statusFilter = "all";

    /**
     * 获取jobStatus（为了兼容旧代码）
     * 
     * @return 状态筛选值
     */
    public String getJobStatus() {
        return statusFilter;
    }
}