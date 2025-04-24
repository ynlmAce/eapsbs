package com.jobhelper.dto.rating;

import lombok.Data;

/**
 * 评价提交DTO
 */
@Data
public class RatingSubmitDTO {

    /**
     * 企业ID
     */
    private Long companyId;

    /**
     * 岗位ID (可选)
     */
    private Long jobId;

    /**
     * 总体评分
     */
    private Double overallScore;

    /**
     * 岗位真实性评分
     */
    private Double jobAuthenticityScore;

    /**
     * 面试体验评分
     */
    private Double interviewExperienceScore;

    /**
     * 工作环境评分
     */
    private Double workEnvironmentScore;

    /**
     * 福利兑现评分
     */
    private Double welfareDeliveryScore;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous = true;
}