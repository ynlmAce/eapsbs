package com.jobhelper.controller;

import com.jobhelper.common.ApiResponse;
import com.jobhelper.dto.rating.RatingListQueryDTO;
import com.jobhelper.dto.rating.RatingSubmitDTO;
import com.jobhelper.service.RatingService;
import com.jobhelper.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价模块控制器
 */
@RestController
@RequestMapping("/api/rating")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /**
     * 提交企业评价
     */
    @PostMapping("/submit")
    public ApiResponse submitRating(@RequestBody RatingSubmitDTO ratingDTO) {
        Long studentId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(ratingService.submitRating(studentId, ratingDTO));
    }

    /**
     * 获取企业评价列表
     */
    @PostMapping("/list")
    public ApiResponse getRatingList(@RequestBody RatingListQueryDTO queryDTO) {
        return ApiResponse.success(ratingService.getRatingList(queryDTO));
    }
}