package com.bs.eaps.service;

import com.bs.eaps.dto.rating.RatingListQueryDTO;
import com.bs.eaps.dto.rating.RatingSubmitDTO;

/**
 * 评价服务接口
 */
public interface RatingService {

    /**
     * 提交企业评价
     * 
     * @param userId    用户ID
     * @param ratingDTO 评价信息
     * @return 提交结果
     */
    Object submitRating(Long userId, RatingSubmitDTO ratingDTO);

    /**
     * 获取企业评价列表
     * 
     * @param queryDTO 查询条件
     * @return 评价列表
     */
    Object getRatingList(RatingListQueryDTO queryDTO);

    /**
     * 获取企业评分概览
     * 
     * @param companyId 企业ID
     * @return 评分统计
     */
    Object getCompanyRatingOverview(Long companyId);

    /**
     * 获取企业评价分页列表
     * 
     * @param companyId 企业ID
     * @param page      页码
     * @param pageSize  每页条数
     * @param sortBy    排序方式
     * @return 评价分页数据
     */
    Object getCompanyRatings(Long companyId, Integer page, Integer pageSize, String sortBy);

    /**
     * 举报企业评价
     * 
     * @param userId   举报人ID
     * @param ratingId 评价ID
     * @param reason   举报原因
     * @return 是否举报成功
     */
    boolean reportRating(Long userId, Long ratingId, String reason);
}