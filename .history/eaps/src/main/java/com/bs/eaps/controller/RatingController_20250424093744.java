package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.dto.rating.RatingListQueryDTO;
import com.bs.eaps.dto.rating.RatingSubmitDTO;
import com.bs.eaps.service.RatingService;
import com.bs.eaps.util.SecurityUtils;
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