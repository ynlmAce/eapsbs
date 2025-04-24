package com.bs.eaps.service;

import com.bs.eaps.dto.job.*;

/**
 * 岗位服务接口
 */
public interface JobService {

    /**
     * 获取岗位列表
     * 
     * @param queryDTO 查询条件
     * @return 岗位列表
     */
    Object getJobList(JobQueryDTO queryDTO);

    /**
     * 获取岗位详情
     * 
     * @param jobId 岗位ID
     * @return 岗位详情
     */
    Object getJobDetail(Long jobId);

    /**
     * 发布新岗位
     * 
     * @param companyId 企业ID
     * @param jobDTO    岗位信息
     * @return 发布结果
     */
    Object publishJob(Long companyId, JobPublishDTO jobDTO);

    /**
     * 学生投递简历
     * 
     * @param studentId      学生ID
     * @param applicationDTO 申请信息
     * @return 投递结果
     */
    Object applyJob(Long studentId, JobApplicationDTO applicationDTO);

    /**
     * 获取学生投递记录
     * 
     * @param studentId 学生ID
     * @param queryDTO  查询条件
     * @return 投递记录列表
     */
    Object getStudentApplicationList(Long studentId, StudentApplicationQueryDTO queryDTO);

    /**
     * 获取企业收到的申请
     * 
     * @param companyId 企业ID
     * @param queryDTO  查询条件
     * @return 申请列表
     */
    Object getCompanyApplicationList(Long companyId, CompanyApplicationQueryDTO queryDTO);
}