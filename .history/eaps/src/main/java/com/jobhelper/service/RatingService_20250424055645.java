package com.jobhelper.service;

import com.jobhelper.dto.rating.RatingListQueryDTO;
import com.jobhelper.dto.rating.RatingSubmitDTO;

/**
 * 评价服务接口
 */
public interface RatingService {

    /**
     * 提交企业评价
     * 
     * @param studentId 学生ID
     * @param ratingDTO 评价信息
     * @return 提交结果
     */
    Object submitRating(Long studentId, RatingSubmitDTO ratingDTO);

    /**
     * 获取企业评价列表
     * 
     * @param queryDTO 查询条件
     * @return 评价列表
     */
    Object getRatingList(RatingListQueryDTO queryDTO);
}