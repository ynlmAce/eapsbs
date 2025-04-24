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
}