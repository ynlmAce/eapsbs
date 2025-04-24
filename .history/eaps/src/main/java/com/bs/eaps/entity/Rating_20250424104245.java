package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业评价实体类
 */
@Data
@TableName("rating")
public class Rating implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    @TableField("student_id")
    private Long studentId;

    /**
     * 企业ID
     */
    @TableField("company_id")
    private Long companyId;

    /**
     * 岗位ID
     */
    @TableField("job_id")
    private Long jobId;

    /**
     * 总体评分
     */
    @TableField("overall_score")
    private Float overallScore;

    /**
     * 岗位真实性评分
     */
    @TableField("job_authenticity_score")
    private Float jobAuthenticityScore;

    /**
     * 面试体验评分
     */
    @TableField("interview_experience_score")
    private Float interviewExperienceScore;

    /**
     * 工作环境评分
     */
    @TableField("work_environment_score")
    private Float workEnvironmentScore;

    /**
     * 福利兑现评分
     */
    @TableField("welfare_delivery_score")
    private Float welfareDeliveryScore;

    /**
     * 评价内容
     */
    @TableField("comment")
    private String comment;

    /**
     * 是否匿名
     */
    @TableField("is_anonymous")
    private Boolean isAnonymous;

    /**
     * 评价状态：active-正常, deleted-已删除
     */
    @TableField("status")
    private String status;

    /**
     * 提交时间
     */
    @TableField("submitted_at")
    private LocalDateTime submittedAt;
}