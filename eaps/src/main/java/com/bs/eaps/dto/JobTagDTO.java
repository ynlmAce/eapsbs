package com.bs.eaps.dto;

import lombok.Data;

/**
 * 岗位标签数据传输对象
 */
@Data
public class JobTagDTO {
    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 标签状态 (0-禁用, 1-启用)
     */
    private Integer status;
}