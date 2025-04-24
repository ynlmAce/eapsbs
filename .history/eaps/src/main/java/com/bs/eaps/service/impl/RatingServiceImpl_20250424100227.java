package com.bs.eaps.service.impl;

import com.bs.eaps.dto.rating.RatingListQueryDTO;
import com.bs.eaps.dto.rating.RatingSubmitDTO;
import com.bs.eaps.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 评价服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    @Override
    @Transactional
    public Object submitRating(Long studentId, RatingSubmitDTO ratingDTO) {
        // TODO: 实现提交评价逻辑
        log.info("提交评价: studentId={}, ratingDTO={}", studentId, ratingDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("ratingId", 0);
        result.put("submittedAt", LocalDateTime.now());
        return result;
    }

    @Override
    public Object getRatingList(RatingListQueryDTO queryDTO) {
        // TODO: 实现获取评价列表逻辑
        log.info("获取评价列表: queryDTO={}", queryDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("averageScore", 0.0);
        result.put("jobAuthenticityAvg", 0.0);
        result.put("interviewExperienceAvg", 0.0);
        result.put("workEnvironmentAvg", 0.0);
        result.put("welfareDeliveryAvg", 0.0);
        result.put("list", java.util.Collections.emptyList());
        return result;
    }
}