package com.jobhelper.dto.job;

import lombok.Data;

/**
 * 岗位申请DTO
 */
@Data
public class JobApplicationDTO {

    /**
     * 岗位ID
     */
    private Long jobId;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 求职信
     */
    private String coverLetter;
}