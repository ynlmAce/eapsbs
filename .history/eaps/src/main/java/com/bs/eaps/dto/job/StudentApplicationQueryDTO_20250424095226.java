package com.bs.eaps.dto.job;

import lombok.Data;

/**
 * 学生申请查询DTO
 */
@Data
public class StudentApplicationQueryDTO {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 状态
     * 可选值: all, pending, viewed, invited, rejected
     */
    private String status = "all";
}