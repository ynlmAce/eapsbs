package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bs.eaps.dto.job.*;
import com.bs.eaps.entity.JobPosting;
import com.bs.eaps.entity.Application;
import com.bs.eaps.entity.Company;
import com.bs.eaps.mapper.JobPostingMapper;
import com.bs.eaps.mapper.ApplicationMapper;
import com.bs.eaps.mapper.CompanyMapper;
import com.bs.eaps.service.JobService;
import com.bs.eaps.common.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 岗位服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobPostingMapper jobPostingMapper;
    private final ApplicationMapper applicationMapper;
    private final CompanyMapper companyMapper;

    @Override
    public Object getJobList(JobQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<JobPosting> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            queryWrapper.like(JobPosting::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(JobPosting::getDescription, queryDTO.getKeyword());
        }

        // 工作地点筛选
        if (queryDTO.getLocation() != null && !queryDTO.getLocation().isEmpty()) {
            queryWrapper.like(JobPosting::getLocation, queryDTO.getLocation());
        }

        // 其他筛选条件
        if (queryDTO.getEducation() != null && !queryDTO.getEducation().isEmpty()) {
            queryWrapper.eq(JobPosting::getEducation, queryDTO.getEducation());
        }

        if (queryDTO.getExperience() != null && !queryDTO.getExperience().isEmpty()) {
            queryWrapper.eq(JobPosting::getExperience, queryDTO.getExperience());
        }

        if (queryDTO.getJobType() != null && !queryDTO.getJobType().isEmpty()) {
            queryWrapper.eq(JobPosting::getJobType, queryDTO.getJobType());
        }

        // 薪资范围筛选
        if (queryDTO.getSalary() != null) {
            // 这里简化处理，实际可能需要更复杂的薪资范围匹配逻辑
            // ...
        }

        // 岗位状态 - 只返回招聘中的岗位
        queryWrapper.eq(JobPosting::getStatus, Constants.JobStatus.RECRUITING);

        // 有效期内的岗位
        queryWrapper.ge(JobPosting::getValidUntil, LocalDateTime.now());

        // 创建分页对象
        Page<JobPosting> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 执行分页查询
        Page<JobPosting> jobPage = jobPostingMapper.selectPage(page, queryWrapper);

        // 转换结果
        List<Map<String, Object>> jobList = jobPage.getRecords().stream().map(job -> {
            Map<String, Object> jobMap = new HashMap<>();

            // 获取企业信息
            Company company = companyMapper.selectById(job.getCompanyId());

            jobMap.put("id", job.getId());
            jobMap.put("title", job.getTitle());
            jobMap.put("companyId", job.getCompanyId());
            jobMap.put("companyName", company != null ? company.getName() : "");
            jobMap.put("location", job.getLocation());
            jobMap.put("salary", job.getSalary());
            jobMap.put("education", job.getEducation());
            jobMap.put("experience", job.getExperience());
            jobMap.put("jobType", job.getJobType());
            jobMap.put("publishedAt", job.getPublishedAt());
            jobMap.put("isCertified", company != null
                    && Constants.CertificationStatus.CERTIFIED.equals(company.getCertificationStatus()));

            // 评分信息
            // TODO: 获取企业评分
            jobMap.put("averageRating", 0.0);

            return jobMap;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", jobPage.getTotal());
        result.put("list", jobList);

        return result;
    }

    @Override
    public Object getJobDetail(Long jobId) {
        // 查询岗位详情
        JobPosting job = jobPostingMapper.selectById(jobId);
        if (job == null) {
            return new HashMap<>();
        }

        // 获取企业信息
        Company company = companyMapper.selectById(job.getCompanyId());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", job.getId());
        result.put("title", job.getTitle());
        result.put("companyId", job.getCompanyId());
        result.put("companyName", company != null ? company.getName() : "");
        result.put("companyLogo", company != null ? company.getLogoPath() : "");
        result.put("description", job.getDescription());
        result.put("requirement", job.getRequirement());
        result.put("location", job.getLocation());
        result.put("salary", job.getSalary());
        result.put("education", job.getEducation());
        result.put("experience", job.getExperience());
        result.put("jobType", job.getJobType());
        result.put("headcount", job.getHeadcount());
        result.put("workTime", job.getWorkTime());
        // JobPosting中没有这些字段，暂时返回空列表
        result.put("welfareTags", java.util.Collections.emptyList());
        result.put("jobTags", java.util.Collections.emptyList());
        result.put("validUntil", job.getValidUntil());
        result.put("contactPerson", job.getContactPerson());
        result.put("contactMethod", job.getShowContact() ? job.getContactMethod() : "");
        result.put("publishedAt", job.getPublishedAt());
        result.put("status", job.getStatus());

        // 企业信息
        Map<String, Object> companyInfo = new HashMap<>();
        if (company != null) {
            companyInfo.put("id", company.getId());
            companyInfo.put("name", company.getName());
            companyInfo.put("industry", company.getIndustry());
            companyInfo.put("size", company.getSize());
            companyInfo.put("isCertified",
                    Constants.CertificationStatus.CERTIFIED.equals(company.getCertificationStatus()));

            // TODO: 获取企业评分
            companyInfo.put("averageRating", 0.0);
        }
        result.put("companyInfo", companyInfo);

        return result;
    }

    @Override
    @Transactional
    public Object publishJob(Long companyId, JobPublishDTO jobDTO) {
        // 创建岗位对象
        JobPosting job = new JobPosting();
        job.setCompanyId(companyId);
        job.setTitle(jobDTO.getTitle());
        job.setDescription(jobDTO.getDescription());
        job.setRequirement(jobDTO.getRequirement());
        job.setLocation(jobDTO.getLocation());
        job.setSalary(jobDTO.getSalary());
        job.setEducation(jobDTO.getEducation());
        job.setExperience(jobDTO.getExperience());
        job.setJobType(jobDTO.getJobType());
        job.setHeadcount(jobDTO.getHeadcount());
        job.setWorkTime(jobDTO.getWorkTime());
        // JobPosting中没有这些字段，忽略welfareTags和jobTags
        // job.setWelfareTags(jobDTO.getWelfareTags());
        // job.setJobTags(jobDTO.getJobTags());
        job.setValidUntil(jobDTO.getValidUntil());
        job.setContactPerson(jobDTO.getContactPerson());
        job.setContactMethod(jobDTO.getContactMethod());
        job.setShowContact(jobDTO.getShowContact());
        job.setStatus(Constants.JobStatus.PENDING);
        job.setPublishedAt(LocalDateTime.now());
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        // 保存岗位
        jobPostingMapper.insert(job);

        // TODO: 创建辅导员审核任务

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("jobId", job.getId());
        result.put("status", job.getStatus());

        return result;
    }

    @Override
    @Transactional
    public Object applyJob(Long studentId, JobApplicationDTO applicationDTO) {
        // 检查是否已经投递过
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getStudentId, studentId)
                .eq(Application::getJobId, applicationDTO.getJobId());

        if (applicationMapper.selectCount(queryWrapper) > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "已经投递过该岗位");
            return result;
        }

        // 创建申请对象
        Application application = new Application();
        application.setStudentId(studentId);
        application.setJobId(applicationDTO.getJobId());
        application.setResumeId(applicationDTO.getResumeId());
        application.setCoverLetter(applicationDTO.getCoverLetter());
        application.setStatus(Constants.ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        // 保存申请
        applicationMapper.insert(application);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("status", application.getStatus());
        result.put("appliedAt", application.getAppliedAt());

        return result;
    }

    @Override
    public Object getStudentApplicationList(Long studentId, StudentApplicationQueryDTO queryDTO) {
        // TODO: 实现获取学生投递记录逻辑
        log.info("获取学生投递记录: studentId={}, queryDTO={}", studentId, queryDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("list", java.util.Collections.emptyList());
        return result;
    }

    @Override
    public Object getCompanyApplicationList(Long companyId, CompanyApplicationQueryDTO queryDTO) {
        // TODO: 实现获取企业收到的申请逻辑
        log.info("获取企业收到的申请: companyId={}, queryDTO={}", companyId, queryDTO);
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("list", java.util.Collections.emptyList());
        return result;
    }
}