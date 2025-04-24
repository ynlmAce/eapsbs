package com.employmentsupport.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位申请实体类
 */
@Data
@TableName("application")
public class Application implements Serializable {

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
     * 岗位ID
     */
    @TableField("job_id")
    private Long jobId;

    /**
     * 简历ID
     */
    @TableField("resume_id")
    private Long resumeId;

    /**
     * 求职信
     */
    @TableField("cover_letter")
    private String coverLetter;

    /**
     * 申请状态：submitted-已投递, viewed-企业已查看, invited-已邀请面试, rejected-已拒绝
     */
    @TableField("status")
    private String status;

    /**
     * 企业备注
     */
    @TableField("company_notes")
    private String companyNotes;

    /**
     * 投递时间
     */
    @TableField("applied_at")
    private LocalDateTime appliedAt;

    /**
     * 最后更新时间
     */
    @TableField("last_updated_at")
    private LocalDateTime lastUpdatedAt;
}