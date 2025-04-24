package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.dto.job.*;
import com.bs.eaps.service.JobService;
import com.bs.eaps.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位模块控制器
 */
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * 获取岗位列表
     */
    @PostMapping("/list")
    public ApiResponse getJobList(@RequestBody JobQueryDTO queryDTO) {
        return ApiResponse.success(jobService.getJobList(queryDTO));
    }

    /**
     * 获取岗位详情
     */
    @PostMapping("/detail")
    public ApiResponse getJobDetail(@RequestBody JobDetailQueryDTO queryDTO) {
        return ApiResponse.success(jobService.getJobDetail(queryDTO.getJobId()));
    }

    /**
     * 发布新岗位
     */
    @PostMapping("/publish")
    public ApiResponse publishJob(@RequestBody JobPublishDTO jobDTO) {
        Long companyId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(jobService.publishJob(companyId, jobDTO));
    }

    /**
     * 学生投递简历
     */
    @PostMapping("/apply")
    public ApiResponse applyJob(@RequestBody JobApplicationDTO applicationDTO) {
        Long studentId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(jobService.applyJob(studentId, applicationDTO));
    }

    /**
     * 获取学生投递记录
     */
    @PostMapping("/application/list/student")
    public ApiResponse getStudentApplicationList(@RequestBody StudentApplicationQueryDTO queryDTO) {
        Long studentId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(jobService.getStudentApplicationList(studentId, queryDTO));
    }

    /**
     * 获取企业收到的申请
     */
    @PostMapping("/application/list/company")
    public ApiResponse getCompanyApplicationList(@RequestBody CompanyApplicationQueryDTO queryDTO) {
        Long companyId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(jobService.getCompanyApplicationList(companyId, queryDTO));
    }
}