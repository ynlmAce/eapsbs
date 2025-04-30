package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.dto.rating.RatingListQueryDTO;
import com.bs.eaps.dto.rating.RatingSubmitDTO;
import com.bs.eaps.service.RatingService;
import com.bs.eaps.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(ratingService.submitRating(userId, ratingDTO));
    }

    /**
     * 获取企业评价列表
     */
    @PostMapping("/list")
    public ApiResponse getRatingList(@RequestBody RatingListQueryDTO queryDTO) {
        return ApiResponse.success(ratingService.getRatingList(queryDTO));
    }

    /**
     * 获取企业评分概览
     */
    @PostMapping("/overview")
    public ApiResponse getCompanyRatingOverview(@RequestBody Map<String, Object> params) {
        Long companyId = params.get("companyId") == null ? null : Long.valueOf(params.get("companyId").toString());
        if (companyId == null) {
            return ApiResponse.businessError("企业ID不能为空");
        }
        return ApiResponse.success(ratingService.getCompanyRatingOverview(companyId));
    }

    /**
     * 获取企业评价列表（分页）
     */
    @PostMapping("/company-ratings")
    public ApiResponse getCompanyRatings(@RequestBody Map<String, Object> params) {
        Long companyId = params.get("companyId") == null ? null : Long.valueOf(params.get("companyId").toString());
        Integer page = params.get("page") == null ? 1 : Integer.valueOf(params.get("page").toString());
        Integer pageSize = params.get("pageSize") == null ? 10 : Integer.valueOf(params.get("pageSize").toString());
        String sortBy = params.get("sortBy") == null ? "time" : params.get("sortBy").toString();
        if (companyId == null) {
            return ApiResponse.businessError("企业ID不能为空");
        }
        return ApiResponse.success(ratingService.getCompanyRatings(companyId, page, pageSize, sortBy));
    }

    /**
     * 举报企业评价
     */
    @PostMapping("/report")
    public ApiResponse reportRating(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long ratingId = params.get("ratingId") == null ? null : Long.valueOf(params.get("ratingId").toString());
        String reason = params.get("reason") == null ? null : params.get("reason").toString();
        if (ratingId == null || reason == null || reason.isEmpty()) {
            return ApiResponse.businessError("举报参数不完整");
        }
        boolean result = ratingService.reportRating(userId, ratingId, reason);
        return ApiResponse.success(result);
    }
}