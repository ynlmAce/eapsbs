package com.bs.eaps.service.impl;

import com.bs.eaps.dto.rating.RatingListQueryDTO;
import com.bs.eaps.dto.rating.RatingSubmitDTO;
import com.bs.eaps.entity.Rating;
import com.bs.eaps.entity.JobPosting;
import com.bs.eaps.entity.StudentProfile;
import com.bs.eaps.mapper.RatingMapper;
import com.bs.eaps.mapper.JobPostingMapper;
import com.bs.eaps.mapper.StudentProfileMapper;
import com.bs.eaps.service.RatingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    private final RatingMapper ratingMapper;
    private final JobPostingMapper jobPostingMapper;
    private final StudentProfileMapper studentProfileMapper;

    @Override
    @Transactional
    public Object submitRating(Long userId, RatingSubmitDTO ratingDTO) {
        log.info("提交评价: userId={}, ratingDTO={}", userId, ratingDTO);

        // 通过用户ID查询学生档案ID
        LambdaQueryWrapper<StudentProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentProfile::getUserId, userId);
        StudentProfile studentProfile = studentProfileMapper.selectOne(queryWrapper);

        if (studentProfile == null) {
            log.error("找不到学生档案: userId={}", userId);
            throw new IllegalArgumentException("找不到学生档案信息，请先完善个人资料");
        }

        Long studentId = studentProfile.getId();
        log.info("找到学生档案ID: userId={}, studentId={}", userId, studentId);

        // 如果企业ID为空，但有岗位ID，则通过岗位ID查询企业ID
        if (ratingDTO.getCompanyId() == null && ratingDTO.getJobId() != null) {
            JobPosting jobPosting = jobPostingMapper.selectById(ratingDTO.getJobId());
            if (jobPosting != null) {
                ratingDTO.setCompanyId(jobPosting.getCompanyId());
            }
        }

        // 检查企业ID不能为空
        if (ratingDTO.getCompanyId() == null) {
            throw new IllegalArgumentException("企业ID不能为空");
        }

        if (ratingDTO.getOverallScore() == null || ratingDTO.getOverallScore() < 1 || ratingDTO.getOverallScore() > 5) {
            throw new IllegalArgumentException("总体评分必须在1-5之间");
        }

        // 创建评价实体
        Rating rating = new Rating();
        rating.setStudentId(studentId); // 使用查询到的学生档案ID
        rating.setCompanyId(ratingDTO.getCompanyId());
        rating.setJobId(ratingDTO.getJobId());

        // 设置各项评分
        rating.setOverallScore(ratingDTO.getOverallScore().floatValue());
        rating.setJobAuthenticityScore(ratingDTO.getJobAuthenticityScore().floatValue());
        rating.setInterviewExperienceScore(ratingDTO.getInterviewExperienceScore().floatValue());
        rating.setWorkEnvironmentScore(ratingDTO.getWorkEnvironmentScore().floatValue());
        rating.setWelfareDeliveryScore(ratingDTO.getWelfareDeliveryScore().floatValue());

        // 设置评价内容和匿名标志
        rating.setComment(ratingDTO.getComment());
        rating.setIsAnonymous(ratingDTO.getIsAnonymous());

        // 设置状态和时间
        rating.setStatus("active");
        rating.setSubmittedAt(LocalDateTime.now());

        // 保存到数据库
        ratingMapper.insert(rating);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("ratingId", rating.getId());
        result.put("submittedAt", rating.getSubmittedAt());

        log.info("评价提交成功: id={}, companyId={}", rating.getId(), rating.getCompanyId());
        return result;
    }

    @Override
    public Object getRatingList(RatingListQueryDTO queryDTO) {
        // 参数验证
        if (queryDTO.getCompanyId() == null) {
            throw new IllegalArgumentException("企业ID不能为空");
        }

        // 获取分页参数
        int page = queryDTO.getPage() != null ? queryDTO.getPage() : 1;
        int limit = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        String sortBy = queryDTO.getSortBy() != null ? queryDTO.getSortBy() : "submitted_at";

        // 创建分页对象
        Page<Map<String, Object>> pageParam = new Page<>(page, limit);

        // 查询企业评价列表
        Page<Map<String, Object>> ratings = ratingMapper.selectCompanyRatings(pageParam, queryDTO.getCompanyId(),
                sortBy);

        // 计算企业平均评分
        Map<String, Object> averageScores = ratingMapper.calculateCompanyAverageScore(queryDTO.getCompanyId());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", ratings.getTotal());
        result.put("list", ratings.getRecords());

        // 添加评分统计
        if (averageScores != null) {
            result.put("averageScore", averageScores.get("overallAvg"));
            result.put("jobAuthenticityAvg", averageScores.get("jobAuthenticityAvg"));
            result.put("interviewExperienceAvg", averageScores.get("interviewExperienceAvg"));
            result.put("workEnvironmentAvg", averageScores.get("workEnvironmentAvg"));
            result.put("welfareDeliveryAvg", averageScores.get("welfareDeliveryAvg"));
        } else {
            result.put("averageScore", 0.0);
            result.put("jobAuthenticityAvg", 0.0);
            result.put("interviewExperienceAvg", 0.0);
            result.put("workEnvironmentAvg", 0.0);
            result.put("welfareDeliveryAvg", 0.0);
        }

        return result;
    }
}