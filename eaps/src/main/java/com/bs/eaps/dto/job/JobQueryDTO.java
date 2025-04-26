package com.bs.eaps.dto.job;

import lombok.Data;

/**
 * 岗位查询DTO
 */
@Data
public class JobQueryDTO {

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 工作地点
     */
    private String location;

    /**
     * 行业
     */
    private String industry;

    /**
     * 薪资范围
     */
    private SalaryRange salary;

    /**
     * 学历要求
     */
    private String education;

    /**
     * 工作经验
     */
    private String experience;

    /**
     * 工作类型
     */
    private String jobType;

    /**
     * 岗位状态
     * 可选值: all, recruiting, active, pending, rejected, ended, closed
     */
    private String status;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 薪资范围
     */
    @Data
    public static class SalaryRange {
        /**
         * 最低薪资
         */
        private Integer min;

        /**
         * 最高薪资
         */
        private Integer max;
    }
}