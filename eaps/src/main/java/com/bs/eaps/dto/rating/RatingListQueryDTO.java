package com.bs.eaps.dto.rating;

import lombok.Data;

/**
 * 评价列表查询DTO
 */
@Data
public class RatingListQueryDTO {

    /**
     * 企业ID
     */
    private Long companyId;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 排序方式
     * 可选值: time, score
     */
    private String sortBy = "time";
}