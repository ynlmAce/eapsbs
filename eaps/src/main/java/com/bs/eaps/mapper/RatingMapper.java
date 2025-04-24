package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bs.eaps.entity.Rating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 评价Mapper接口
 */
@Mapper
public interface RatingMapper extends BaseMapper<Rating> {

    /**
     * 查询企业评价列表
     * 
     * @param page      分页参数
     * @param companyId 企业ID
     * @param sortBy    排序字段
     * @return 评价列表
     */
    Page<Map<String, Object>> selectCompanyRatings(
            Page<Map<String, Object>> page,
            @Param("companyId") Long companyId,
            @Param("sortBy") String sortBy);

    /**
     * 计算企业平均评分
     * 
     * @param companyId 企业ID
     * @return 平均评分信息
     */
    Map<String, Object> calculateCompanyAverageScore(@Param("companyId") Long companyId);
}