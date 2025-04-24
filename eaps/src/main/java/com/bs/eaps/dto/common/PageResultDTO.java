package com.bs.eaps.dto.common;

import lombok.Data;
import java.util.List;

/**
 * 通用分页结果DTO
 */
@Data
public class PageResultDTO<T> {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据列表
     */
    private List<T> list;

    /**
     * 创建分页结果DTO
     * 
     * @param total 总记录数
     * @param list  当前页数据列表
     * @return 分页结果DTO
     */
    public static <T> PageResultDTO<T> of(Long total, List<T> list) {
        PageResultDTO<T> result = new PageResultDTO<>();
        result.setTotal(total);
        result.setList(list);
        return result;
    }
}