package com.bs.eaps.dto.common;

import lombok.Data;

/**
 * 通用分页请求DTO
 */
@Data
public class PageRequestDTO {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页记录数
     */
    private Integer pageSize = 10;
}