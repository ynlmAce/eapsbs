package com.bs.eaps.dto.job;

import lombok.Data;

/**
 * 企业申请查询DTO
 */
@Data
public class CompanyApplicationQueryDTO {

    /**
     * 岗位ID (可选)
     */
    private Long jobId;

    /**
     * 学生姓名 (可选)
     */
    private String studentName;

    /**
     * 状态
     * 可选值: all, unread, contacted, interviewed, rejected
     */
    private String status = "all";

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 分组方式 (可选)
     * 可选值: job, student
     */
    private String groupBy;

    /**
     * 岗位状态筛选 (可选)
     * 可选值: recruiting, pending, rejected, ended
     */
    private String jobStatusFilter;

    /**
     * 关键词搜索 (可选)
     */
    private String keyword;

    /**
     * 标签筛选 (可选)
     * 可选值: favorite, blacklist
     */
    private String tag;
}